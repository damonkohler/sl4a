package com.google.ase.facade;

import android.app.Service;
import android.content.Context;
import android.os.Bundle;
import android.telephony.PhoneStateListener;
import android.telephony.SignalStrength;
import android.telephony.TelephonyManager;

import com.google.ase.jsonrpc.RpcReceiver;
import com.google.ase.rpc.Rpc;

/**
 * Exposes SignalStrength functionality.
 * 
 * @author Joerg Zieren (joerg.zieren@gmail.com)
 */
public class SignalStrengthFacade implements RpcReceiver {
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

  public SignalStrengthFacade(Service service, EventFacade eventFacade) {
    mEventFacade = eventFacade;
    mTelephonyManager = (TelephonyManager) service.getSystemService(Context.TELEPHONY_SERVICE);
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
