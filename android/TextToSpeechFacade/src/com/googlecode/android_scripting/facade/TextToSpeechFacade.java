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

package com.googlecode.android_scripting.facade;

import android.os.SystemClock;
import android.speech.tts.TextToSpeech;
import android.speech.tts.TextToSpeech.OnInitListener;

import com.googlecode.android_scripting.jsonrpc.RpcReceiver;
import com.googlecode.android_scripting.rpc.Rpc;
import com.googlecode.android_scripting.rpc.RpcMinSdk;
import com.googlecode.android_scripting.rpc.RpcParameter;

import java.util.concurrent.CountDownLatch;

/**
 * Provides Text To Speech services for API 4 or more.
 */

@RpcMinSdk(4)
public class TextToSpeechFacade extends RpcReceiver {

  private final TextToSpeech mTts;
  private final CountDownLatch mOnInitLock;

  public TextToSpeechFacade(FacadeManager manager) {
    super(manager);
    mOnInitLock = new CountDownLatch(1);
    mTts = new TextToSpeech(manager.getService(), new OnInitListener() {
      @Override
      public void onInit(int arg0) {
        mOnInitLock.countDown();
      }
    });
  }

  @Override
  public void shutdown() {
    while (mTts.isSpeaking()) {
      SystemClock.sleep(100);
    }
    mTts.shutdown();
  }

  @Rpc(description = "Speaks the provided message via TTS.")
  public void ttsSpeak(@RpcParameter(name = "message") String message) throws InterruptedException {
    mOnInitLock.await();
    if (message != null) {
      mTts.speak(message, TextToSpeech.QUEUE_ADD, null);
    }
  }

  @Rpc(description = "Returns True if speech is currently in progress.")
  public Boolean ttsIsSpeaking() throws InterruptedException {
    mOnInitLock.await();
    return mTts.isSpeaking();
  }

}
