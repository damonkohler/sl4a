/*
 * Copyright (C) 2010 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package com.googlecode.android_scripting.trigger;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;

import org.apache.commons.codec.binary.Base64;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.googlecode.android_scripting.IntentBuilders;
import com.googlecode.android_scripting.Log;

/**
 * A repository maintaining all currently scheduled triggers. This includes, for example, alarms or
 * observers of arriving text messages etc. This class is responsible for serializing the list of
 * triggers to the shared preferences store, and retrieving it from there.
 * 
 * @author Felix Arends (felix.arends@gmail.com)
 * 
 */
public class TriggerRepository {
  /**
   * An interface for objects that are notified when a trigger is added to the repository.
   */
  public interface TriggerRepositoryObserver {
    void onAddTrigger(Trigger trigger);

    void onRemoveTrigger(Trigger trigger);
  }

  private final CopyOnWriteArrayList<Trigger> mTriggers = new CopyOnWriteArrayList<Trigger>();
  private final CopyOnWriteArrayList<TriggerRepositoryObserver> mTriggerObservers =
      new CopyOnWriteArrayList<TriggerRepositoryObserver>();

  /**
   * The list of triggers is serialized to the shared preferences entry with this name.
   */
  private static final String TRIGGERS_PREF_KEY = "TRIGGERS";

  /**
   * Each trigger has an id to make it identifiable. The next id is stored to the shared
   * preferences. This is the key for the corresponding shared preferences entry.
   */
  private static final String NEXT_TRIGGER_ID_KEY = "NEXT_TRIGGER_ID";

  private final SharedPreferences mPreferences;

  private Context mContext;

  /** Interface for filters over triggers */
  public static interface TriggerFilter {
    boolean matches(Trigger trigger);
  }

  public TriggerRepository(Context context) {
    mContext = context;
    mPreferences = PreferenceManager.getDefaultSharedPreferences(context);
    String triggers = mPreferences.getString(TRIGGERS_PREF_KEY, null);
    mTriggers.addAll(deserializeTriggersFromString(triggers));
  }

  /** Returns a list of all triggers. The list is unmodifiable. */
  public synchronized List<Trigger> getAllTriggers() {
    return Collections.unmodifiableList(mTriggers);
  }

  /**
   * Adds a new trigger to the repository. This function also calls the {@link Trigger.#install()}
   * method of the trigger.
   */
  public synchronized void addTrigger(Trigger trigger) {
    mTriggers.add(trigger);
    storeTriggers();
    notifyOnTriggerAdd(trigger);
    ensureTriggerServiceRunning();
  }

  /** Ensures that the {@link TriggerService} is running */
  private void ensureTriggerServiceRunning() {
    Intent startTriggerServiceIntent = IntentBuilders.buildTriggerServiceIntent();
    mContext.startService(startTriggerServiceIntent);
  }

  /** Notify all {@link TriggerRepositoryObserver}s that a {@link Trigger} was added. */
  private void notifyOnTriggerAdd(Trigger trigger) {
    for (TriggerRepositoryObserver observer : mTriggerObservers) {
      observer.onAddTrigger(trigger);
    }
  }

  /** Notify all {@link TriggerRepositoryObserver}s that a {@link Trigger} was added. */
  private void notifyOnTriggerRemove(Trigger trigger) {
    for (TriggerRepositoryObserver observer : mTriggerObservers) {
      observer.onRemoveTrigger(trigger);
    }
  }

  /** Writes the list of triggers to the shared preferences. */
  private void storeTriggers() {
    SharedPreferences.Editor editor = mPreferences.edit();
    final String triggerValue = serializeTriggersToString(mTriggers);
    if (triggerValue != null) {
      editor.putString(TRIGGERS_PREF_KEY, triggerValue);
    }
    editor.commit();
  }

  /** Removes a specific trigger. */
  public synchronized void removeTrigger(final UUID id) {
    removeTriggers(new TriggerFilter() {
      @Override
      public boolean matches(Trigger trigger) {
        return trigger.getId().equals(id);
      }
    });
  }

  /** Deserializes the list of triggers from a base 64 encoded string. */
  @SuppressWarnings("unchecked")
  private List<Trigger> deserializeTriggersFromString(String triggers) {
    if (triggers == null) {
      return new ArrayList<Trigger>();
    }

    try {
      final ByteArrayInputStream inputStream =
          new ByteArrayInputStream(Base64.decodeBase64(triggers.getBytes()));
      final ObjectInputStream objectInputStream = new ObjectInputStream(inputStream);
      List<Trigger> result = (List<Trigger>) objectInputStream.readObject();
      return result;
    } catch (IOException e) {
      Log.e(e);
    } catch (ClassNotFoundException e) {
      Log.e(e);
    }
    return new ArrayList<Trigger>();
  }

  /** Serializes the list of triggers to a Base64 encoded string. */
  private String serializeTriggersToString(List<Trigger> triggers) {
    try {
      final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
      final ObjectOutputStream objectOutputStream = new ObjectOutputStream(outputStream);
      objectOutputStream.writeObject(triggers);
      return new String(Base64.encodeBase64(outputStream.toByteArray()));
    } catch (IOException e) {
      Log.e(e);
      return null;
    }
  }

  /** Removes all triggers that match the filters */
  public synchronized void removeTriggers(TriggerFilter triggerFilter) {
    new ArrayList<Trigger>();
    for (Trigger trigger : mTriggers) {
      if (triggerFilter.matches(trigger)) {
        trigger.remove();
        mTriggers.remove(trigger);
        notifyOnTriggerRemove(trigger);
      }
    }
    storeTriggers();
  }

  /** Returns the currently stored index. */
  private long readIndex() {
    return mPreferences.getLong(NEXT_TRIGGER_ID_KEY, 0);
  }

  /** Write a new index to the store. */
  private void writeIndex(long newIndex) {
    SharedPreferences.Editor editor = mPreferences.edit();
    editor.putLong(NEXT_TRIGGER_ID_KEY, newIndex);
    editor.commit();
  }

  /** Returns a new unique id for use with the next trigger. */
  public synchronized long createNewId() {
    long newId = readIndex();
    writeIndex(newId + 1);
    return newId;
  }

  /** Returns the {@link TriggerInfo} object with the given id. */
  public Trigger getById(UUID id) {
    for (Trigger trigger : mTriggers) {
      if (trigger.getId().equals(id)) {
        return trigger;
      }
    }
    return null;
  }

  /** Returns {@code true} iff the list of triggers is empty. */
  public boolean isEmpty() {
    return mTriggers.isEmpty();
  }

  /** Registers a listener that is invoked when a new trigger gets added. */
  public void registerAddTriggerListener(TriggerRepositoryObserver listener) {
    mTriggerObservers.add(listener);
  }

  /**
   * Unregisters a previously registered listener. This is a no-op if the listener hasn't been
   * registered.
   */
  public void unregisterAddListener(TriggerRepositoryObserver addTriggerListener) {
    mTriggerObservers.remove(addTriggerListener);
  }
}
