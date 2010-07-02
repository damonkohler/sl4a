// Copyright 2010 Google Inc. All Rights Reserved.

package com.google.ase.jsonrpc;

public abstract class RpcReceiver {

  public RpcReceiver(RpcReceiverManager manager) {
    // To make reflection easier, we ensures that all the subclasses agree on this common
    // constructor.
  }

  /** Invoked when the receiver is shut down. */
  public abstract void shutdown();
}
