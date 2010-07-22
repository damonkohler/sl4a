package com.googlecode.android_scripting.facade;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;

import com.googlecode.android_scripting.jsonrpc.RpcReceiver;
import com.googlecode.android_scripting.rpc.Rpc;
import com.googlecode.android_scripting.rpc.RpcMinSdk;

import java.lang.reflect.Field;

public class BatteryManagerFacade extends RpcReceiver {
  private final Service mService;

  private int mBatteryStatus = 1;
  private int mBatteryHealth = 1;
  private int mPlugType = -1;

  private boolean mBatteryPresent = false;
  private int mBatteryLevel = -1;
  private int mBatteryMaxLevel = 0;
  private int mBatteryVoltage = -1;
  private int mBatteryTemperature = -1;
  private String mBatteryTechnology = null;

  private int sdkVersion = 3;

  public BatteryManagerFacade(FacadeManager manager) {
    super(manager);
    mService = manager.getService();
    sdkVersion = manager.getSdkLevel();
    IntentFilter filter = new IntentFilter();
    filter.addAction(Intent.ACTION_BATTERY_CHANGED);
    mService.registerReceiver(new BatteryStateListener(), filter);
  }

  private class BatteryStateListener extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
      mBatteryStatus = intent.getIntExtra("status", 1);
      mBatteryHealth = intent.getIntExtra("health", 1);
      mPlugType = intent.getIntExtra("plugged", -1);
      if (sdkVersion >= 5) {
        mBatteryPresent =
            intent.getBooleanExtra(getBatteryManagerFieldValue("EXTRA_PRESENT"), false);
        mBatteryLevel = intent.getIntExtra(getBatteryManagerFieldValue("EXTRA_LEVEL"), -1);
        mBatteryMaxLevel = intent.getIntExtra(getBatteryManagerFieldValue("EXTRA_SCALE"), 0);
        mBatteryVoltage = intent.getIntExtra(getBatteryManagerFieldValue("EXTRA_VOLTAGE"), -1);
        mBatteryTemperature =
            intent.getIntExtra(getBatteryManagerFieldValue("EXTRA_TEMPERATURE"), -1);
        mBatteryTechnology = intent.getStringExtra(getBatteryManagerFieldValue("EXTRA_TECHNOLOGY"));
      }
    }
  }

  private String getBatteryManagerFieldValue(String name) {
    try {
      Field f = BatteryManager.class.getField(name);
      return f.get(null).toString();
    } catch (Exception e) {
    }
    return null;
  }

  @Override
  public void shutdown() {
  }

  @Rpc(description = "Returns battery status:" + "\n\t 1 - unknown; " + "\n\t 2 - charging; "
      + "\n\t 3 - discharging; " + "\n\t 4 - not charging; " + "\n\t 5 - full;")
  public Integer batteryGetStatus() {
    return mBatteryStatus;
  }

  @Rpc(description = "Returns battery health:" + "\n\t 1 - unknown; " + "\n\t 2 - good; "
      + "\n\t 3 - overheat; " + "\n\t 4 - dead; " + "\n\t 5 - over voltage;"
      + "\n\t 6 - unspecified failure;")
  public Integer batteryGetHealth() {
    return mBatteryHealth;
  }

  /** Power source is an AC charger. */
  public static final int BATTERY_PLUGGED_AC = 1;
  /** Power source is a USB port. */
  public static final int BATTERY_PLUGGED_USB = 2;

  @Rpc(description = "Returns plug type:" + "\n\t -1 - unknown; " + "\n\t 0 - unplugged; "
      + "\n\t 1 - power source is an AC charger; " + "\n\t 2 - power source is a USB port;")
  public Integer batteryGetPlugType() {
    return mPlugType;
  }

  @Rpc(description = "Returns true if battery is present")
  @RpcMinSdk(5)
  public Boolean batteryCheckPresent() {
    return mBatteryPresent;
  }

  @Rpc(description = "Returns current battery level (percentage)")
  @RpcMinSdk(5)
  public Integer batteryGetLevel() {
    if (mBatteryMaxLevel == 100 || mBatteryMaxLevel == 0) {
      return mBatteryLevel;
    } else {
      return (int) (mBatteryLevel * 100.0 / mBatteryMaxLevel);
    }
  }

  @Rpc(description = "Returns current battery voltage")
  @RpcMinSdk(5)
  public Integer batteryGetVoltage() {
    return mBatteryVoltage;
  }

  @Rpc(description = "Returns current battery temperature")
  @RpcMinSdk(5)
  public Integer batteryGetTemperature() {
    return mBatteryTemperature;
  }

  @Rpc(description = "Returns battery technology")
  @RpcMinSdk(5)
  public String batteryGetTechnology() {
    return mBatteryTechnology;
  }

}
