package com.googlecode.android_scripting.facade;

import android.app.Service;
import android.content.Context;
import android.os.Bundle;
import android.telephony.PhoneStateListener;
import android.telephony.SignalStrength;
import android.telephony.TelephonyManager;

import com.googlecode.android_scripting.MainThread;
import com.googlecode.android_scripting.jsonrpc.RpcReceiver;
import com.googlecode.android_scripting.rpc.Rpc;
import com.googlecode.android_scripting.rpc.RpcMinSdk;
import com.googlecode.android_scripting.rpc.RpcStartEvent;
import com.googlecode.android_scripting.rpc.RpcStopEvent;

import java.util.concurrent.Callable;

/**
 * Exposes SignalStrength functionality.
 * 
 * @author Joerg Zieren (joerg.zieren@gmail.com)
 */
@RpcMinSdk(7)
public class SignalStrengthFacade extends RpcReceiver {
  private final Service mService;
  private final TelephonyManager mTelephonyManager;
  private final EventFacade mEventFacade;
  private final PhoneStateListener mPhoneStateListener;
  private Bundle mSignalStrengths;

  public SignalStrengthFacade(FacadeManager manager) {
    super(manager);
    mService = manager.getService();
    mEventFacade = manager.getReceiver(EventFacade.class);
    mTelephonyManager =
        (TelephonyManager) manager.getService().getSystemService(Context.TELEPHONY_SERVICE);
    mPhoneStateListener = MainThread.run(mService, new Callable<PhoneStateListener>() {
      @Override
      public PhoneStateListener call() throws Exception {
        return new PhoneStateListener() {
          @Override
          public void onSignalStrengthsChanged(SignalStrength signalStrength) {
            mSignalStrengths = new Bundle();
            mSignalStrengths.putInt("gsm_signal_strength", signalStrength.getGsmSignalStrength());
            mSignalStrengths.putInt("gsm_bit_error_rate", signalStrength.getGsmBitErrorRate());
            mSignalStrengths.putInt("cdma_dbm", signalStrength.getCdmaDbm());
            mSignalStrengths.putInt("cdma_ecio", signalStrength.getCdmaEcio());
            mSignalStrengths.putInt("evdo_dbm", signalStrength.getEvdoDbm());
            mSignalStrengths.putInt("evdo_ecio", signalStrength.getEvdoEcio());
            mEventFacade.postEvent("signal_strengths", mSignalStrengths.clone());
          }
        };
      }
    });
  }

  @Rpc(description = "Starts tracking signal strengths.")
  @RpcStartEvent("signal_strengths")
  public void startTrackingSignalStrengths() {
    mTelephonyManager.listen(mPhoneStateListener, PhoneStateListener.LISTEN_SIGNAL_STRENGTHS);
  }

  @Rpc(description = "Returns the current signal strengths.", returns = "A map of \"gsm_signal_strength\"")
  public Bundle readSignalStrengths() {
    return mSignalStrengths;
  }

  @Rpc(description = "Stops tracking signal strength.")
  @RpcStopEvent("signal_strengths")
  public void stopTrackingSignalStrengths() {
    mTelephonyManager.listen(mPhoneStateListener, PhoneStateListener.LISTEN_NONE);
  }

  @Override
  public void shutdown() {
    stopTrackingSignalStrengths();
  }
}
