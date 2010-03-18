/*
 * Copyright (C) 2009 Google Inc.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package com.google.ase.facade;

import java.io.IOException;

import android.media.MediaRecorder;

import com.google.ase.jsonrpc.Rpc;
import com.google.ase.jsonrpc.RpcParameter;
import com.google.ase.jsonrpc.RpcReceiver;

/**
 * A facade for media related RPCs.
 * 
 * @author Felix Arends (felix.arends@gmail.com)
 *
 */
public class MediaFacade implements RpcReceiver {
  private final MediaRecorder mAudioRecorder = new MediaRecorder();

  @Rpc(description = "Records an audio snippet and saves it to the given location.")
  public void startAudioRecording(@RpcParameter(name = "path of target file") final String targetPath)
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
  public void shutdown() {
    mAudioRecorder.release();
  }
}
