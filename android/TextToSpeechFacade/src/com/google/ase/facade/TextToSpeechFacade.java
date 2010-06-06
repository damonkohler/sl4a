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

import java.util.concurrent.CountDownLatch;

import android.app.Service;
import android.os.SystemClock;
import android.speech.tts.TextToSpeech;
import android.speech.tts.TextToSpeech.OnInitListener;

import com.google.ase.jsonrpc.RpcReceiver;
import com.google.ase.rpc.Rpc;
import com.google.ase.rpc.RpcParameter;

public class TextToSpeechFacade implements RpcReceiver {

  private final TextToSpeech mTts;
  private final CountDownLatch mOnInitLock;

  public TextToSpeechFacade(Service service) {
    mOnInitLock = new CountDownLatch(1);
    mTts = new TextToSpeech(service, new OnInitListener() {
      @Override
      public void onInit(int arg0) {
        mOnInitLock.countDown();
      }
    });
  }

  @Override
  public void shutdown() {
    waitForSpeechToFinish();
    mTts.shutdown();
  }

  private void waitForSpeechToFinish() {
    if (mTts != null) {
      while (mTts.isSpeaking()) {
        SystemClock.sleep(100);
      }
    }
  }

  @Rpc(description = "Speaks the provided message via TTS.")
  public void speak(@RpcParameter(name = "message") String message) throws InterruptedException {
    mOnInitLock.await();
    mTts.speak(message, TextToSpeech.QUEUE_ADD, null);
  }
}
