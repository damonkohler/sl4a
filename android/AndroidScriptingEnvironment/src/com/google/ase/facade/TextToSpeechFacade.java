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

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import android.content.Context;
import android.content.Intent;

import com.google.ase.AseLog;
import com.google.ase.exception.AseException;
import com.google.ase.jsonrpc.Rpc;
import com.google.ase.jsonrpc.RpcParameter;
import com.google.ase.jsonrpc.RpcReceiver;

public class TextToSpeechFacade implements RpcReceiver {

  private final Context mContext;
  private final CountDownLatch mLock;
  private final Tts mTts;

  private interface Tts {
    public void speak(String message);
    public void shutdown();
  }

  private class EyesFreeTts implements Tts {

    public EyesFreeTts() {
      mLock.countDown();
    }

    @Override
    public void shutdown() {
      // Nothing to do.
    }

    @Override
    public void speak(String message) {
      Intent intent = new Intent("com.google.tts.makeBagel");
      intent.putExtra("message", message);
      mContext.startActivity(intent);
    }
  }

  @SuppressWarnings("unchecked")
  private class NativeTts implements Tts, InvocationHandler {

    private Class mClass;
    private Object mObject;

    public NativeTts() {
      Class initListener;
      try {
        mClass = Class.forName("android.speech.tts.TextToSpeech");
        initListener = Class.forName("android.speech.tts.TextToSpeech$OnInitListener");
      } catch (ClassNotFoundException e) {
        AseLog.e("Failed to load TTS classes.", e);
        return;
      }

      Class parameterTypes[] = new Class[2];
      parameterTypes[0] = Context.class;
      parameterTypes[1] = initListener;

      Object proxy =
          Proxy.newProxyInstance(initListener.getClassLoader(), new Class[] { initListener }, this);

      Object paramValues[] = new Object[2];
      paramValues[0] = mContext;
      paramValues[1] = initListener.cast(proxy);

      try {
        Constructor constructor = mClass.getConstructor(parameterTypes);
        mObject = constructor.newInstance(paramValues);
      } catch (Exception e) {
        AseLog.e("Failed to create TTS instance.", e);
        return;
      }
    }

    @Override
    public void shutdown() {
      try {
        Method m = mClass.getMethod("shutdown", new Class[] {});
        m.invoke(mObject, new Object[] {});
      } catch (Exception e) {
        AseLog.e("Failed to shutdown TTS.", e);
      }
    }

    @Override
    public void speak(String message) {
      try {
        Method m =
            mClass.getMethod("speak", new Class[] { String.class, Integer.TYPE, HashMap.class });
        m.invoke(mObject, new Object[] { message, 1, null });
      } catch (Exception e) {
        AseLog.e("Failed to speak with TTS.", e);
      }
    }

    @Override
    public Object invoke(Object arg0, Method arg1, Object[] arg2) throws Throwable {
      mLock.countDown();
      return null;
    }
  }

  public TextToSpeechFacade(Context context) {
    mContext = context;
    mLock = new CountDownLatch(1);
    if (android.os.Build.VERSION.SDK.equals("3")) {
      AseLog.v("Loading Cupcake TTS.");
      mTts = new EyesFreeTts();
    } else {
      AseLog.v("Loading Donut+ TTS.");
      mTts = new NativeTts();
    }
  }

  @Rpc(description = "Speaks the provided message via TTS")
  public void speak(@RpcParameter(name = "message to speak") String message) throws AseException {
    try {
      if (!mLock.await(10, TimeUnit.SECONDS)) {
        throw new AseException("TTS initialization timed out.");
      }
    } catch (InterruptedException e) {
      throw new AseException("TTS initialization interrupted.");
    }
    mTts.speak(message);
  }

  @Override
  public void shutdown() {
    mTts.shutdown();
  }
}
