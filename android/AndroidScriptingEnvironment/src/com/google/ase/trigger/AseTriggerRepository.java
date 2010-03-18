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
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.codec.binary.Base64;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.google.ase.AseLog;

public class AseTriggerRepository {
  private static final String TRIGGERS_PREF_KEY = "TRIGGERS";

  private final SharedPreferences mPreferences;
  
  public static interface TriggerFilter {
    boolean matches(Trigger trigger);
  }

  public AseTriggerRepository(Context context) {
    mPreferences = PreferenceManager.getDefaultSharedPreferences(context);
  }

  public synchronized List<Trigger> getAllTriggers() {
    final String triggers = mPreferences.getString(TRIGGERS_PREF_KEY, null);
    return deserializeTriggersFromString(triggers);
  }

  public synchronized void addTrigger(Trigger trigger) {
    List<Trigger> triggers = getAllTriggers();
    triggers.add(trigger);
    storeTriggers(triggers);
  }

  private void storeTriggers(List<Trigger> triggers) {
    SharedPreferences.Editor editor = mPreferences.edit();
    final String triggerValue = serializeTriggersToString(triggers);
    if (triggerValue != null) {
      editor.putString(TRIGGERS_PREF_KEY, triggerValue);
    }
    editor.commit();
  }

  public synchronized void removeTrigger(Trigger trigger) {
    List<Trigger> triggers = getAllTriggers();
    triggers.remove(trigger);
    storeTriggers(triggers);
  }

  @SuppressWarnings("unchecked")
  private List<Trigger> deserializeTriggersFromString(String triggers) {
    if (triggers == null) {
      return new ArrayList<Trigger>();
    }

    try {
      final ByteArrayInputStream inputStream =
          new ByteArrayInputStream(Base64.decodeBase64(triggers.getBytes()));
      final ObjectInputStream objectInputStream = new ObjectInputStream(inputStream);
      return (List<Trigger>) objectInputStream.readObject();
    } catch (IOException e) {
      AseLog.e(e);
    } catch (ClassNotFoundException e) {
      AseLog.e(e);
    }
    return new ArrayList<Trigger>();
  }

  private String serializeTriggersToString(List<Trigger> triggers) {
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

  public void removeTriggers(TriggerFilter triggerFilter) {
    List<Trigger> allTriggers = new ArrayList<Trigger>();
    for (Trigger trigger : getAllTriggers()) {
      if (!triggerFilter.matches(trigger)) {
        allTriggers.add(trigger);
      }
    }
    storeTriggers(allTriggers);
  }
}
