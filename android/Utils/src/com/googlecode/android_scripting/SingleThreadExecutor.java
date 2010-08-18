// Copyright 2010 Google Inc. All Rights Reserved.

package com.googlecode.android_scripting;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class SingleThreadExecutor extends ThreadPoolExecutor {

  public SingleThreadExecutor() {
    super(1, 1, 0L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>());
  }

  @Override
  protected void afterExecute(Runnable r, Throwable t) {
    if (t != null) {
      throw new RuntimeException(t);
    }
  }
}