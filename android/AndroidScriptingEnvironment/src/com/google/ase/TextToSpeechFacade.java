package com.google.ase;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import android.content.Context;
import android.content.Intent;

public class TextToSpeechFacade {

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

  public void speak(String message) throws AseException {
    try {
      if (!mLock.await(10, TimeUnit.SECONDS)) {
        throw new AseException("TTS initialization timed out.");
      }
    } catch (InterruptedException e) {
      throw new AseException("TTS initialization interrupted.");
    }
    mTts.speak(message);
  }

  public void shutdown() {
    mTts.shutdown();
  }
}
