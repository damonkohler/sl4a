package com.google.ase.facade;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import android.app.ActivityManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;

import com.google.ase.jsonrpc.RpcReceiver;
import com.google.ase.rpc.Rpc;
import com.google.ase.rpc.RpcParameter;

public class ActivityManagerFacade implements RpcReceiver {

  private final Service mService;
  private final AndroidFacade mAndroidFacade;
  private final ActivityManager mActivityManager;

  public ActivityManagerFacade(Service service, AndroidFacade androidFacade) {
    mService = service;
    mAndroidFacade = androidFacade;
    mActivityManager = (ActivityManager) mService.getSystemService(Context.ACTIVITY_SERVICE);
  }

  @Rpc(description = "Start activity with the given class name (i.e. Browser, Maps, etc.).")
  public void launch(@RpcParameter(name = "className") String className) {
    Intent intent = new Intent(Intent.ACTION_MAIN);
    String packageName = className.substring(0, className.lastIndexOf("."));
    intent.setClassName(packageName, className);
    mAndroidFacade.startActivity(intent);
  }

  @Rpc(description = "Returns a list of packages running activities or services.", returns = "List of packages running activities.")
  public List<String> getRunningPackages() {
    Set<String> runningPackages = new HashSet<String>();
    List<ActivityManager.RunningAppProcessInfo> appProcesses =
        mActivityManager.getRunningAppProcesses();
    for (ActivityManager.RunningAppProcessInfo info : appProcesses) {
      runningPackages.addAll(Arrays.asList(info.pkgList));
    }
    List<ActivityManager.RunningServiceInfo> serviceProcesses =
        mActivityManager.getRunningServices(Integer.MAX_VALUE);
    for (ActivityManager.RunningServiceInfo info : serviceProcesses) {
      runningPackages.add(info.service.getPackageName());
    }
    return new ArrayList<String>(runningPackages);
  }

  @Rpc(description = "Force stops a package.")
  public void forceStopPackage(
      @RpcParameter(name = "packageName", description = "name of package") String packageName) {
    mActivityManager.restartPackage(packageName);
  }

  @Override
  public void shutdown() {
  }
}
