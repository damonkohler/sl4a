// Copyright 2010 Google Inc. All Rights Reserved.

package com.googlecode.android_scripting.jsonrpc;

public abstract class RpcReceiver {

  protected final RpcReceiverManager mManager;

  public RpcReceiver(RpcReceiverManager manager) {
    // To make reflection easier, we ensures that all the subclasses agree on this common
    // constructor.
    mManager = manager;
  }

  /** Invoked when the receiver is shut down. */
  public abstract void shutdown();
}
