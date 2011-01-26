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

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;
import com.googlecode.android_scripting.IntentBuilders;
import com.googlecode.android_scripting.Log;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Map.Entry;
import java.util.concurrent.CopyOnWriteArrayList;

import org.apache.commons.codec.binary.Base64Codec;

/**
 * A repository maintaining all currently scheduled triggers. This includes, for example, alarms or
 * observers of arriving text messages etc. This class is responsible for serializing the list of
 * triggers to the shared preferences store, and retrieving it from there.
 * 
 * @author Felix Arends (felix.arends@gmail.com)
 * @author Damon Kohler (damonkohler@gmail.com)
 */
public class TriggerRepository {
  /**
   * The list of triggers is serialized to the shared preferences entry with this name.
   */
  private static final String TRIGGERS_PREF_KEY = "TRIGGERS";

  private final SharedPreferences mPreferences;
  private final Context mContext;

  /**
   * An interface for objects that are notified when a trigger is added to the repository.
   */
  public interface TriggerRepositoryObserver {
    /**
     * Invoked just before the trigger is added to the repository.
     * 
     * @param trigger
     *          The trigger about to be added to the repository.
     */
    void onPut(Trigger trigger);

    /**
     * Invoked just after the trigger has been removed from the repository.
     * 
     * @param trigger
     *          The trigger that has just been removed from the repository.
     */
    void onRemove(Trigger trigger);
  }

  private final Multimap<String, Trigger> mTriggers;
  private final CopyOnWriteArrayList<TriggerRepositoryObserver> mTriggerObservers =
      new CopyOnWriteArrayList<TriggerRepositoryObserver>();

  public TriggerRepository(Context context) {
    mContext = context;
    mPreferences = PreferenceManager.getDefaultSharedPreferences(context);
    String triggers = mPreferences.getString(TRIGGERS_PREF_KEY, null);
    mTriggers = deserializeTriggersFromString(triggers);
  }

  /** Returns a list of all triggers. The list is unmodifiable. */
  public synchronized Multimap<String, Trigger> getAllTriggers() {
    return Multimaps.unmodifiableMultimap(mTriggers);
  }

  /**
   * Adds a new trigger to the repository.
   * 
   * @param trigger
   *          the {@link Trigger} to add
   */
  public synchronized void put(Trigger trigger) {
    notifyOnAdd(trigger);
    mTriggers.put(trigger.getEventName(), trigger);
    storeTriggers();
    ensureTriggerServiceRunning();
  }

  /** Removes a specific {@link Trigger}. */
  public synchronized void remove(final Trigger trigger) {
    mTriggers.get(trigger.getEventName()).remove(trigger);
    storeTriggers();
    notifyOnRemove(trigger);
  }

  /** Ensures that the {@link TriggerService} is running */
  private void ensureTriggerServiceRunning() {
    Intent startTriggerServiceIntent = IntentBuilders.buildTriggerServiceIntent();
    mContext.startService(startTriggerServiceIntent);
  }

  /** Notify all {@link TriggerRepositoryObserver}s that a {@link Trigger} was added. */
  private void notifyOnAdd(Trigger trigger) {
    for (TriggerRepositoryObserver observer : mTriggerObservers) {
      observer.onPut(trigger);
    }
  }

  /** Notify all {@link TriggerRepositoryObserver}s that a {@link Trigger} was removed. */
  private void notifyOnRemove(Trigger trigger) {
    for (TriggerRepositoryObserver observer : mTriggerObservers) {
      observer.onRemove(trigger);
    }
  }

  /** Writes the list of triggers to the shared preferences. */
  private synchronized void storeTriggers() {
    SharedPreferences.Editor editor = mPreferences.edit();
    final String triggerValue = serializeTriggersToString(mTriggers);
    if (triggerValue != null) {
      editor.putString(TRIGGERS_PREF_KEY, triggerValue);
    }
    editor.commit();
  }

  /** Deserializes the {@link Multimap} of {@link Trigger}s from a base 64 encoded string. */
  @SuppressWarnings("unchecked")
  private Multimap<String, Trigger> deserializeTriggersFromString(String triggers) {
    if (triggers == null) {
      return ArrayListMultimap.<String, Trigger> create();
    }
    try {
      final ByteArrayInputStream inputStream =
          new ByteArrayInputStream(Base64Codec.decodeBase64(triggers.getBytes()));
      final ObjectInputStream objectInputStream = new ObjectInputStream(inputStream);
      return (Multimap<String, Trigger>) objectInputStream.readObject();
    } catch (Exception e) {
      Log.e(e);
    }
    return ArrayListMultimap.<String, Trigger> create();
  }

  /** Serializes the list of triggers to a Base64 encoded string. */
  private String serializeTriggersToString(Multimap<String, Trigger> triggers) {
    try {
      final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
      final ObjectOutputStream objectOutputStream = new ObjectOutputStream(outputStream);
      objectOutputStream.writeObject(triggers);
      return new String(Base64Codec.encodeBase64(outputStream.toByteArray()));
    } catch (IOException e) {
      Log.e(e);
      return null;
    }
  }

  /** Returns {@code true} iff the list of triggers is empty. */
  public synchronized boolean isEmpty() {
    return mTriggers.isEmpty();
  }

  /** Adds a {@link TriggerRepositoryObserver}. */
  public void addObserver(TriggerRepositoryObserver observer) {
    mTriggerObservers.add(observer);
  }

  /**
   * Adds the given {@link TriggerRepositoryObserver} and invokes
   * {@link TriggerRepositoryObserver#onPut} for all existing triggers.
   * 
   * @param observer
   *          The observer to add.
   */
  public synchronized void bootstrapObserver(TriggerRepositoryObserver observer) {
    addObserver(observer);
    for (Entry<String, Trigger> trigger : mTriggers.entries()) {
      observer.onPut(trigger.getValue());
    }
  }

  /**
   * Removes a {@link TriggerRepositoryObserver}.
   */
  public void removeObserver(TriggerRepositoryObserver observer) {
    mTriggerObservers.remove(observer);
  }
}