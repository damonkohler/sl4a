package com.googlecode.android_scripting.facade;

import android.media.AudioManager;
import android.media.ToneGenerator;

import com.googlecode.android_scripting.jsonrpc.RpcReceiver;
import com.googlecode.android_scripting.rpc.Rpc;
import com.googlecode.android_scripting.rpc.RpcDefault;
import com.googlecode.android_scripting.rpc.RpcParameter;

/**
 * Generate DTMF tones.
 * 
 */
public class ToneGeneratorFacade extends RpcReceiver {

  private final ToneGenerator mToneGenerator;

  public ToneGeneratorFacade(FacadeManager manager) {
    super(manager);
    mToneGenerator = new ToneGenerator(AudioManager.STREAM_MUSIC, 100);
  }

  @Rpc(description = "Generate DTMF tones for the given phone number.")
  public void generateDtmfTones(
      @RpcParameter(name = "phoneNumber") String phoneNumber,
      @RpcParameter(name = "toneDuration", description = "duration of each tone in milliseconds") @RpcDefault("100") Integer toneDuration)
      throws InterruptedException {
    try {
      for (int i = 0; i < phoneNumber.length(); i++) {
        switch (phoneNumber.charAt(i)) {
        case '0':
          mToneGenerator.startTone(ToneGenerator.TONE_DTMF_0);
          Thread.sleep(toneDuration);
          break;
        case '1':
          mToneGenerator.startTone(ToneGenerator.TONE_DTMF_1);
          Thread.sleep(toneDuration);
          break;
        case '2':
          mToneGenerator.startTone(ToneGenerator.TONE_DTMF_2);
          Thread.sleep(toneDuration);
          break;
        case '3':
          mToneGenerator.startTone(ToneGenerator.TONE_DTMF_3);
          Thread.sleep(toneDuration);
          break;
        case '4':
          mToneGenerator.startTone(ToneGenerator.TONE_DTMF_4);
          Thread.sleep(toneDuration);
          break;
        case '5':
          mToneGenerator.startTone(ToneGenerator.TONE_DTMF_5);
          Thread.sleep(toneDuration);
          break;
        case '6':
          mToneGenerator.startTone(ToneGenerator.TONE_DTMF_6);
          Thread.sleep(toneDuration);
          break;
        case '7':
          mToneGenerator.startTone(ToneGenerator.TONE_DTMF_7);
          Thread.sleep(toneDuration);
          break;
        case '8':
          mToneGenerator.startTone(ToneGenerator.TONE_DTMF_8);
          Thread.sleep(toneDuration);
          break;
        case '9':
          mToneGenerator.startTone(ToneGenerator.TONE_DTMF_9);
          Thread.sleep(toneDuration);
          break;
        case '*':
          mToneGenerator.startTone(ToneGenerator.TONE_DTMF_S);
          Thread.sleep(toneDuration);
          break;
        case '#':
          mToneGenerator.startTone(ToneGenerator.TONE_DTMF_P);
          Thread.sleep(toneDuration);
          break;
        default:
          throw new RuntimeException("Cannot generate tone for '" + phoneNumber.charAt(i) + "'");
        }
      }
    } finally {
      mToneGenerator.stopTone();
    }
  }

  @Override
  public void shutdown() {
  }
}
