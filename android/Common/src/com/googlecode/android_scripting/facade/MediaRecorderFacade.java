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

package com.googlecode.android_scripting.facade;

import java.io.IOException;
import java.lang.reflect.Field;

import android.media.MediaRecorder;

import com.googlecode.android_scripting.jsonrpc.RpcReceiver;
import com.googlecode.android_scripting.rpc.Rpc;
import com.googlecode.android_scripting.rpc.RpcParameter;

/**
 * A facade for recording media.
 * 
 * @author Felix Arends (felix.arends@gmail.com)
 * @author Damon Kohler (damonkohler@gmail.com)
 */
public class MediaRecorderFacade extends RpcReceiver {

  private final MediaRecorder mMediaRecorder = new MediaRecorder();

  public MediaRecorderFacade(FacadeManager manager) {
    super(manager);
  }

  @Rpc(description = "Records audio from the microphone and saves it to the given location.")
  public void recorderStartMicrophone(@RpcParameter(name = "targetPath") String targetPath)
      throws IOException {
    startAudioRecording(targetPath, MediaRecorder.AudioSource.MIC);
  }

  @Rpc(description = "Records audio from the phone and saves it to the given location.")
  public void recorderStartPhone(@RpcParameter(name = "targetPath") String targetPath)
      throws Exception {
    // This is only possible starting with API level 4.
    Field source = Class.forName("android.media.MediaRecorder$AudioSource").getField("VOICE_CALL");
    startAudioRecording(targetPath, source.getInt(null));
  }

  private void startAudioRecording(String targetPath, int source) throws IOException {
    mMediaRecorder.setAudioSource(source);
    mMediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.DEFAULT);
    mMediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.DEFAULT);
    mMediaRecorder.setOutputFile(targetPath);
    mMediaRecorder.prepare();
    mMediaRecorder.start();
  }

  @Rpc(description = "Stops a previously started recording.")
  public void recorderStop() {
    mMediaRecorder.stop();
    mMediaRecorder.reset();
  }

  @Override
  public void shutdown() {
    mMediaRecorder.release();
  }
}
