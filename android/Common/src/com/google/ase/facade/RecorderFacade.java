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

import android.media.MediaRecorder;

import com.google.ase.jsonrpc.RpcReceiver;
import com.google.ase.rpc.Rpc;
import com.google.ase.rpc.RpcParameter;

import java.io.IOException;
import java.lang.reflect.Field;

/**
 * A facade for recording audio.
 * 
 * @author Felix Arends (felix.arends@gmail.com)
 * @author Damon Kohler (damonkohler@gmail.com)
 */
public class RecorderFacade extends RpcReceiver {

  public RecorderFacade(FacadeManager manager) {
    super(manager);
  }

  private final MediaRecorder mAudioRecorder = new MediaRecorder();

  @Rpc(description = "Records audio from the microphone and saves it to the given location.")
  public void recorderStartMicrophone(@RpcParameter(name = "targetPath") String targetPath)
      throws IOException {
    startRecording(targetPath, MediaRecorder.AudioSource.MIC);
  }

  @Rpc(description = "Records audio from the phone and saves it to the given location.")
  public void recorderStartPhone(@RpcParameter(name = "targetPath") String targetPath)
      throws Exception {
    // This is only possible starting with API level 4.
    Field source = Class.forName("android.media.MediaRecorder$AudioSource").getField("VOICE_CALL");
    startRecording(targetPath, source.getInt(null));
  }

  private void startRecording(String targetPath, int source) throws IOException {
    mAudioRecorder.setAudioSource(source);
    mAudioRecorder.setOutputFormat(MediaRecorder.OutputFormat.DEFAULT);
    mAudioRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.DEFAULT);
    mAudioRecorder.setOutputFile(targetPath);
    mAudioRecorder.prepare();
    mAudioRecorder.start();
  }

  @Rpc(description = "Stops a previously started recording.")
  public void recorderStop() {
    mAudioRecorder.stop();
    mAudioRecorder.reset();
  }

  @Override
  public void shutdown() {
    mAudioRecorder.release();
  }
}
