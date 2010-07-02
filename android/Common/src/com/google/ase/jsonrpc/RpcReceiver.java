// Copyright 2010 Google Inc. All Rights Reserved.

package com.google.ase.jsonrpc;

/**
 * 
 * Ensures that all the subclasses agree on the common constructor.
 */
public abstract class RpcReceiver {
  public RpcReceiver(RpcReceiverManager manager) {
    // Required for reflection.
  }

  /** Invoked when the receiver is shut down. */
  public abstract void shutdown();
}
