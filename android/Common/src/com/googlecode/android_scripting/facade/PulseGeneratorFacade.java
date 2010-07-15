package com.googlecode.android_scripting.facade;

import com.cellbots.PulseGenerator;
import com.googlecode.android_scripting.jsonrpc.RpcReceiver;
import com.googlecode.android_scripting.jsonrpc.RpcReceiverManager;
import com.googlecode.android_scripting.rpc.Rpc;
import com.googlecode.android_scripting.rpc.RpcParameter;

public class PulseGeneratorFacade extends RpcReceiver {

  private PulseGenerator mPulseGenerator;

  public PulseGeneratorFacade(RpcReceiverManager manager) {
    super(manager);
    mPulseGenerator = new PulseGenerator();
  }

  @Override
  public void shutdown() {
  }

  @Rpc(description = "Wraps PulseGenerator run().")
  public void pulseGeneratorRun() {
    mPulseGenerator.run();
  }

  @Rpc(description = "Wraps PulseGenerator stop().")
  public void pulseGeneratorStop() {
    mPulseGenerator.stop();
  }

  @Rpc(description = "Wraps PulseGenerator togglePlayback().")
  public void pulseGeneratorTogglePlayback() {
    mPulseGenerator.togglePlayback();
  }

  @Rpc(description = "Wraps PulseGenerator toggleInverted().")
  public void pulseGeneratorToggleInverted() {
    mPulseGenerator.toggleInverted();
  }

  @Rpc(description = "Wraps PulseGenerator isPlaying().")
  public Boolean pulseGeneratorIsPlaying() {
    return mPulseGenerator.isPlaying();
  }

  @Rpc(description = "Wraps PulseGenerator setLeftPulsePercent().")
  public void pulseGeneratorSetLeftPulsePercent(@RpcParameter(name = "percent") Integer percent) {
    mPulseGenerator.setLeftPulsePercent(percent);
  }

  @Rpc(description = "Wraps PulseGenerator getLeftPulsePercent().")
  public Integer pulseGeneratorGetLeftPulsePercent() {
    return mPulseGenerator.getLeftPulsePercent();
  }

  @Rpc(description = "Wraps PulseGenerator getLeftPulseMs().")
  public Float pulseGeneratorGetLeftPulseMs() {
    return mPulseGenerator.getLeftPulseMs();
  }

  @Rpc(description = "Wraps PulseGenerator getLeftPulseSamples().")
  public Integer pulseGeneratorGetLeftPulseSamples() {
    return mPulseGenerator.getLeftPulseSamples();
  }

  @Rpc(description = "Wraps PulseGenerator setRightPulsePercent().")
  public void pulseGeneratorSetRightPulsePercent(@RpcParameter(name = "percent") Integer percent) {
    mPulseGenerator.setRightPulsePercent(percent);
  }

  @Rpc(description = "Wraps PulseGenerator getRightPulseMs().")
  public Float pulseGeneratorGetRightPulseMs() {
    return mPulseGenerator.getRightPulseMs();
  }

  @Rpc(description = "Wraps PulseGenerator getRightPulsePercent().")
  public Integer pulseGeneratorGetRightPulsePercent() {
    return mPulseGenerator.getRightPulsePercent();
  }

  @Rpc(description = "Wraps PulseGenerator getRightPulseSamples().")
  public Integer pulseGeneratorGetRightPulseSamples() {
    return mPulseGenerator.getRightPulseSamples();
  }

  @Rpc(description = "Wraps PulseGenerator getHz().")
  public Float pulseGeneratorGetHz() {
    return mPulseGenerator.getHz();
  }

  @Rpc(description = "Wraps PulseGenerator getHzSamples().")
  public Integer pulseGeneratorGetHzSamples() {
    return mPulseGenerator.getHzSamples();
  }

  @Rpc(description = "Wraps PulseGenerator setHz().")
  public void pulseGenteratorSetHz(@RpcParameter(name = "hz") Float hz) {
    mPulseGenerator.setHz(hz);
  }

  @Rpc(description = "Wraps PulseGenerator setHzPercent().")
  public void pulseGeneratorSetHzPercent(@RpcParameter(name = "percent") Integer percent) {
    mPulseGenerator.setHzPercent(percent);
  }
}
