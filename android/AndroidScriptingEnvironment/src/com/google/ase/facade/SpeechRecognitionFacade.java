/*
 * Copyright (C) 2009 Google Inc.
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

package com.google.ase.facade;

import java.util.ArrayList;

import android.content.Intent;
import android.speech.RecognizerIntent;

import com.google.ase.jsonrpc.Rpc;
import com.google.ase.jsonrpc.RpcParameter;
import com.google.ase.jsonrpc.RpcOptional;
import com.google.ase.jsonrpc.RpcReceiver;

/**
 * A facade containing RPC implementations related to the speech-to-text
 * functionality of Android.
 *
 * @author Felix Arends (felix.arends@gmail.com)
 *
 */
public class SpeechRecognitionFacade implements RpcReceiver {
  private final AndroidFacade mAndroidFacade;

  /**
   * @param activityLauncher
   *          a helper object that launches activities in a blocking manner
   */
  public SpeechRecognitionFacade(final AndroidFacade facade) {
    this.mAndroidFacade = facade;
  }

  @Rpc(description = "Recognizes user's speech and returns the most likely result.", returns = "An empty string in case the speech cannot be recongnized.")
  public String recognizeSpeech(
      @RpcParameter(name = "prompt", description = "text prompt to show to the user when asking them to speak") @RpcOptional final String prompt,
      @RpcParameter(name = "language", description = "language override to inform the recognizer that it should expect speech in a language different than the one set in the java.util.Locale.getDefault()") @RpcOptional final String language,
      @RpcParameter(name = "languageModel", description = "informs the recognizer which speech model to prefer (see android.speech.RecognizeIntent)") @RpcOptional final String languageModel) {
    final Intent recognitionIntent =
        new Intent(android.speech.RecognizerIntent.ACTION_RECOGNIZE_SPEECH);

    // Setup intent parameters (if provided).
    if (language != null) {
      recognitionIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "");
    }
    if (languageModel != null) {
      recognitionIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, "");
    }
    if (prompt != null) {
      recognitionIntent.putExtra(RecognizerIntent.EXTRA_PROMPT, "");
    }

    // Run the activity an retrieve the result.
    final Intent data = mAndroidFacade.startActivityForResult(recognitionIntent);

    // The result consists of an array-list containing one entry for each
    // possible result. The most likely result is the first entry.
    ArrayList<String> results =
        data.getStringArrayListExtra(android.speech.RecognizerIntent.EXTRA_RESULTS);

    if (results == null || results.size() == 0) {
      return "";
    }

    return results.get(0);
  }

  @Override
  public void shutdown() {
  }
}
