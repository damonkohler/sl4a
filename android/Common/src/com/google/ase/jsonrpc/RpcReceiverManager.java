// Copyright 2010 Google Inc. All Rights Reserved.

package com.google.ase.jsonrpc;

/**
 * @author raaar@google.com (Your Name Here)
 *
 */
public interface RpcReceiverManager {
  public RpcReceiver getReceiverInstance(Class<? extends RpcReceiver> receiverClass);
}
