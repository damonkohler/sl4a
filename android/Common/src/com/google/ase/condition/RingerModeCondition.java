package com.google.ase.condition;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;

import com.google.ase.AseLog;
import com.google.ase.trigger.ConditionListener;

public class RingerModeCondition implements Condition {
  private ConditionListener mBeginListener;
  private ConditionListener mEndListener;
  private final AudioManager mAudioManager;
  private Context mContext;
  private final Configuration mConfiguration;
  private boolean mInCondition;
  
  public static class Configuration implements ConditionConfiguration {
    int mmMode;
    
    public Configuration(int mode) {
      mmMode = mode;
    }

    public int getMode() {
      return mmMode;
    }

    @Override
    public Condition getCondition(Context context) {
      return new RingerModeCondition(context, this);
    }
  }

  private RingerModeCondition(Context context, Configuration configuration) {
    mAudioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
    mContext = context;
    mConfiguration = configuration;
  }

  @Override
  public void addBeginListener(ConditionListener listener) {
    mBeginListener = listener;
  }

  @Override
  public void addEndListener(ConditionListener listener) {
    mEndListener = listener;
  }

  @Override
  public void start() {
    if (mAudioManager.getRingerMode() == mConfiguration.getMode()) {
      mInCondition = true;
      invokeBegin();
    } else {
      mInCondition = false;
      invokeEnd();
    }

    mContext.registerReceiver(new BroadcastReceiver() {

      @Override
      public void onReceive(Context context, Intent intent) {
        int ringerMode = intent.getIntExtra(AudioManager.EXTRA_RINGER_MODE, -1);

        switch (ringerMode) {
        case AudioManager.RINGER_MODE_NORMAL:
        case AudioManager.RINGER_MODE_SILENT:
        case AudioManager.RINGER_MODE_VIBRATE:
          if (mConfiguration.getMode() == ringerMode && !mInCondition) {
            invokeBegin();
            mInCondition = true;
          } else if (mInCondition){
            mInCondition = false;
            invokeEnd();
          }
        default:
          AseLog.e("Invalid ringer mode.");
        }
      }
    }, new IntentFilter(AudioManager.RINGER_MODE_CHANGED_ACTION));
  }

  private void invokeBegin() {
    if (mBeginListener != null) {
      mBeginListener.run();
    }
  }

  private void invokeEnd() {
    if (mEndListener != null) {
      mEndListener.run();
    }
  }

  @Override
  public void stop() {
  }
}
