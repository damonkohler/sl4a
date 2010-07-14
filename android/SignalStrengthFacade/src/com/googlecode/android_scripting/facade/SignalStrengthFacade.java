package com.googlecode.android_scripting.facade;

import android.content.Context;
import android.os.Bundle;
import android.telephony.PhoneStateListener;
import android.telephony.SignalStrength;
import android.telephony.TelephonyManager;

import com.googlecode.android_scripting.facade.EventFacade;
import com.googlecode.android_scripting.facade.FacadeManager;
import com.googlecode.android_scripting.jsonrpc.RpcReceiver;
import com.googlecode.android_scripting.rpc.Rpc;

/**
 * Exposes SignalStrength functionality.
 * 
 * @author Joerg Zieren (joerg.zieren@gmail.com)
 */
public class SignalStrengthFacade extends RpcReceiver {
  private final TelephonyManager mTelephonyManager;
  private final EventFacade mEventFacade;
  private Bundle mSignalStrengths;

  private final PhoneStateListener mPhoneStateListener = new PhoneStateListener() {
    @Override
    public void onSignalStrengthsChanged(SignalStrength signalStrength) {
      mSignalStrengths = new Bundle();
      mSignalStrengths.putInt("gsm_signal_strength", signalStrength.getGsmSignalStrength());
      mEventFacade.postEvent("signal_strengths", mSignalStrengths);
    }
  };

  public SignalStrengthFacade(FacadeManager manager) {
    super(manager);
    mEventFacade = manager.getReceiver(EventFacade.class);
    mTelephonyManager =
        (TelephonyManager) manager.getService().getSystemService(Context.TELEPHONY_SERVICE);
  }

  @Rpc(description = "Starts tracking signal strengths.")
  public void startTrackingSignalStrengths() {
    mTelephonyManager.listen(mPhoneStateListener, PhoneStateListener.LISTEN_SIGNAL_STRENGTHS);
  }

  @Rpc(description = "Returns the current signal strengths.", returns = "A map of \"gsm_signal_strength\"")
  public Bundle readSignalStrengths() {
    return mSignalStrengths;
  }

  @Rpc(description = "Stops tracking signal strength.")
  public void stopTrackingSignalStrengths() {
    mTelephonyManager.listen(mPhoneStateListener, PhoneStateListener.LISTEN_NONE);
  }

  @Override
  public void shutdown() {
    stopTrackingSignalStrengths();
  }
}
