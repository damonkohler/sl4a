package com.google.ase;

import java.io.IOException;

import android.media.MediaRecorder;

import com.google.ase.jsonrpc.Rpc;
import com.google.ase.jsonrpc.RpcParameter;

/**
 * A facade for media related RPCs.
 * 
 * @author Felix Arends (felix.arends@gmail.com)
 *
 */
public class AndroidMediaFacade implements RpcFacade {
  private final MediaRecorder mAudioRecorder = new MediaRecorder();

  @Rpc(description = "Records an audio snippet and saves it to the given location.")
  public void startAudioRecording(@RpcParameter("path of target file") final String targetPath)
      throws IOException, InterruptedException {
    mAudioRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
    mAudioRecorder.setOutputFormat(MediaRecorder.OutputFormat.DEFAULT);
    mAudioRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.DEFAULT);
    mAudioRecorder.setOutputFile(targetPath);
    mAudioRecorder.prepare();
    mAudioRecorder.start();
  }
  
  @Rpc(description = "Stops a previously started recording of audio.")
  public void stopAudioRecording() {
    mAudioRecorder.stop();
    mAudioRecorder.reset();
  }

  @Override
  public void initialize() {
  }

  @Override
  public void shutdown() {
    mAudioRecorder.release();
  }
}
