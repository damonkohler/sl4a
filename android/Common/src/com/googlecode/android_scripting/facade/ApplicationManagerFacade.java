package com.googlecode.android_scripting.facade;

import android.app.ActivityManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;

import com.googlecode.android_scripting.jsonrpc.RpcReceiver;
import com.googlecode.android_scripting.rpc.Rpc;
import com.googlecode.android_scripting.rpc.RpcParameter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Facade for managing Applications.
 * 
 */
public class ApplicationManagerFacade extends RpcReceiver {

  private final AndroidFacade mAndroidFacade;
  private final ActivityManager mActivityManager;
  private final PackageManager mPackageManager;

  public ApplicationManagerFacade(FacadeManager manager) {
    super(manager);
    Service service = manager.getService();
    mAndroidFacade = manager.getReceiver(AndroidFacade.class);
    mActivityManager = (ActivityManager) service.getSystemService(Context.ACTIVITY_SERVICE);
    mPackageManager = service.getPackageManager();
  }

  @Rpc(description = "Returns a list of all launchable application class names.")
  public Map<String, String> getLaunchableApplications() {
    Intent intent = new Intent(Intent.ACTION_MAIN);
    intent.addCategory(Intent.CATEGORY_LAUNCHER);
    List<ResolveInfo> resolveInfos = mPackageManager.queryIntentActivities(intent, 0);
    Map<String, String> applications = new HashMap<String, String>();
    for (ResolveInfo info : resolveInfos) {
      applications.put(info.loadLabel(mPackageManager).toString(), info.activityInfo.name);
    }
    return applications;
  }

  @Rpc(description = "Start activity with the given class name.")
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
