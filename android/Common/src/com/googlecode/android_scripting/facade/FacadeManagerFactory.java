package com.googlecode.android_scripting.facade;

import android.app.Service;
import android.content.Intent;

import com.googlecode.android_scripting.jsonrpc.RpcReceiver;
import com.googlecode.android_scripting.jsonrpc.RpcReceiverManagerFactory;

import java.util.Collection;

public class FacadeManagerFactory implements RpcReceiverManagerFactory {

  private final int mSdkLevel;
  private final Service mService;
  private final Intent mIntent;
  private final Collection<Class<? extends RpcReceiver>> mClassList;

  public FacadeManagerFactory(int sdkLevel, Service service, Intent intent,
      Collection<Class<? extends RpcReceiver>> classList) {
    mSdkLevel = sdkLevel;
    mService = service;
    mIntent = intent;
    mClassList = classList;
  }

  public FacadeManager create() {
    return new FacadeManager(mSdkLevel, mService, mIntent, mClassList);
  }
}
