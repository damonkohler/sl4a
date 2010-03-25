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

package com.google.ase.trigger;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.codec.binary.Base64;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.google.ase.AseLog;

/**
 * A repository maintaining all currently scheduled triggers. This includes, for example, alarms or
 * observers of arriving text messages etc. This class is responsible for serializing the list of
 * triggers to the shared preferences store, and retrieving it from there.
 * 
 * @author Felix Arends (felix.arends@gmail.com)
 * 
 */
public class TriggerRepository {
  /** Holds trigger object and meta-information. */
  public static class TriggerInfo implements Serializable {
    private static final long serialVersionUID = 8103773194726113518L;
    private final long mId;
    private final Trigger mTrigger;
    private transient TriggerRepository mRepository;

    public TriggerInfo(TriggerRepository repository, long id, Trigger trigger) {
      mRepository = repository;
      mId = id;
      mTrigger = trigger;
    }

    public long getId() {
      return mId;
    }

    public Trigger getTrigger() {
      return mTrigger;
    }
    
    private void setRepostiory(TriggerRepository repository) {
      this.mRepository = repository;
    }

    /** Removes this trigger from the repository */
    public void remove() {
      mRepository.removeTrigger(mId);
    }
  }

  /**
   * The list of triggers is serialzied to the shared preferences entry with this name.
   */
  private static final String TRIGGERS_PREF_KEY = "TRIGGERS";

  /**
   * Each trigger has an id to make it identifiable. The next id is stored to the shared
   * preferences. This is the key for the corresponding shared preferences entry.
   */
  private static final String NEXT_TRIGGER_ID_KEY = "NEXT_TRIGGER_ID";

  private final SharedPreferences mPreferences;

  public static interface TriggerFilter {
    boolean matches(TriggerInfo trigger);
  }

  public TriggerRepository(Context context) {
    mPreferences = PreferenceManager.getDefaultSharedPreferences(context);
  }

  public synchronized List<TriggerInfo> getAllTriggers() {
    final String triggers = mPreferences.getString(TRIGGERS_PREF_KEY, null);
    return deserializeTriggersFromString(triggers);
  }

  /** Adds a new trigger to the repository. */
  public synchronized TriggerInfo addTrigger(Trigger trigger) {
    final TriggerInfo info = new TriggerInfo(this, createNewId(), trigger);
    final List<TriggerInfo> triggers = getAllTriggers();
    triggers.add(info);
    storeTriggers(triggers);
    return info;
  }

  /** Writes the list of triggers to the shared preferences. */
  private void storeTriggers(List<TriggerInfo> triggers) {
    SharedPreferences.Editor editor = mPreferences.edit();
    final String triggerValue = serializeTriggersToString(triggers);
    if (triggerValue != null) {
      editor.putString(TRIGGERS_PREF_KEY, triggerValue);
    }
    editor.commit();
  }

  /** Removes a specific trigger. */
  public synchronized void removeTrigger(long id) {
    List<TriggerInfo> triggers = getAllTriggers();

    TriggerInfo itemToRemove = null;
    for (TriggerInfo info : triggers) {
      if (info.getId() == id) {
        itemToRemove = info;
        break;
      }
    }

    triggers.remove(itemToRemove);
    storeTriggers(triggers);
  }

  /** Deserializes the list of triggers from a base 64 encoded string. */
  @SuppressWarnings("unchecked")
  private List<TriggerInfo> deserializeTriggersFromString(String triggers) {
    if (triggers == null) {
      return new ArrayList<TriggerInfo>();
    }

    try {
      final ByteArrayInputStream inputStream =
          new ByteArrayInputStream(Base64.decodeBase64(triggers.getBytes()));
      final ObjectInputStream objectInputStream = new ObjectInputStream(inputStream);
      List<TriggerInfo> result = (List<TriggerInfo>) objectInputStream.readObject();
      for (TriggerInfo info : result) {
        info.setRepostiory(this);
      }
      return result;
    } catch (IOException e) {
      AseLog.e(e);
    } catch (ClassNotFoundException e) {
      AseLog.e(e);
    }
    return new ArrayList<TriggerInfo>();
  }

  /** Serializes the list of triggers to a Base64 encoded string. */
  private String serializeTriggersToString(List<TriggerInfo> triggers) {
    try {
      final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
      final ObjectOutputStream objectOutputStream = new ObjectOutputStream(outputStream);
      objectOutputStream.writeObject(triggers);
      return new String(Base64.encodeBase64(outputStream.toByteArray()));
    } catch (IOException e) {
      AseLog.e(e);
      return null;
    }
  }

  /** Removes all triggers that match the filters */
  public synchronized void removeTriggers(TriggerFilter triggerFilter) {
    List<TriggerInfo> allTriggers = new ArrayList<TriggerInfo>();
    for (TriggerInfo info : getAllTriggers()) {
      if (!triggerFilter.matches(info)) {
        allTriggers.add(info);
      }
    }
    storeTriggers(allTriggers);
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
  public TriggerInfo getById(long id) {
    for (TriggerInfo info : getAllTriggers()) {
      if (info.getId() == id) {
        return info;
      }
    }

    return null;
  }
}
