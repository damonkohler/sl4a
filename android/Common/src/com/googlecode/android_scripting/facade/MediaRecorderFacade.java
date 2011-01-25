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

import android.app.Service;
import android.content.Intent;
import android.media.MediaRecorder;
import android.net.Uri;
import android.provider.MediaStore;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.WindowManager;
import android.view.SurfaceHolder.Callback;

import com.googlecode.android_scripting.BaseApplication;
import com.googlecode.android_scripting.FileUtils;
import com.googlecode.android_scripting.FutureActivityTaskExecutor;
import com.googlecode.android_scripting.Log;
import com.googlecode.android_scripting.future.FutureActivityTask;
import com.googlecode.android_scripting.jsonrpc.RpcReceiver;
import com.googlecode.android_scripting.rpc.Rpc;
import com.googlecode.android_scripting.rpc.RpcDefault;
import com.googlecode.android_scripting.rpc.RpcOptional;
import com.googlecode.android_scripting.rpc.RpcParameter;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * A facade for recording media.
 * 
 * @author Felix Arends (felix.arends@gmail.com)
 * @author Damon Kohler (damonkohler@gmail.com)
 */
public class MediaRecorderFacade extends RpcReceiver {

  private final MediaRecorder mMediaRecorder = new MediaRecorder();
  private final Service mService;

  public MediaRecorderFacade(FacadeManager manager) {
    super(manager);
    mService = manager.getService();
  }

  @Rpc(description = "Records audio from the microphone and saves it to the given location.")
  public void recorderStartMicrophone(@RpcParameter(name = "targetPath") String targetPath)
      throws IOException {
    startAudioRecording(targetPath, MediaRecorder.AudioSource.MIC);
  }

  @Rpc(description = "Records video (and optionally audio) from the camera and saves it to the given location. "
      + "\nDuration specifies the maximum duration of the recording session. "
      + "\nIf duration is not provided this method will return immediately and the recording will only be stopped "
      + "\nwhen recorderStop is called or when a scripts exits. "
      + "\nOtherwise it will block for the time period equal to the duration argument.")
  public void recorderCaptureVideo(@RpcParameter(name = "targetPath") String targetPath,
      @RpcParameter(name = "duration") @RpcOptional Double duration,
      @RpcParameter(name = "recordAudio") @RpcDefault("true") Boolean recordAudio) throws Exception {
    int ms = convertSecondsToMilliseconds(duration);
    startVideoRecording(new File(targetPath), ms, recordAudio);
  }

  private void startVideoRecording(File file, int milliseconds, boolean withAudio) throws Exception {
    mMediaRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);
    if (withAudio) {
      int audioSource = MediaRecorder.AudioSource.MIC;
      try {
        Field source =
            Class.forName("android.media.MediaRecorder$AudioSource").getField("CAMCORDER");
        audioSource = source.getInt(null);
      } catch (Exception e) {
        Log.e(e);
      }
      mMediaRecorder.setAudioSource(audioSource);
      mMediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.DEFAULT);
      mMediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.DEFAULT);
    } else {
      mMediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.DEFAULT);
    }
    mMediaRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.DEFAULT);
    if (!FileUtils.makeDirectories(file.getParentFile(), 0755)) {
      throw new RuntimeException(String
          .format("Cannot create directories for %s.", file.toString()));
    }
    mMediaRecorder.setOutputFile(file.getAbsolutePath());
    if (milliseconds > 0) {
      mMediaRecorder.setMaxDuration(milliseconds);
    }
    FutureActivityTask<Exception> prepTask = prepare();
    mMediaRecorder.start();
    if (milliseconds > 0) {
      new CountDownLatch(1).await(milliseconds, TimeUnit.MILLISECONDS);
    }
    prepTask.finish();
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

  @Rpc(description = "Starts the video capture application to record a video and saves it to the specified path.")
  public void startInteractiveVideoRecording(@RpcParameter(name = "path") final String path) {
    Intent intent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
    File file = new File(path);
    intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(file));
    AndroidFacade facade = mManager.getReceiver(AndroidFacade.class);
    facade.startActivityForResult(intent);
  }

  @Override
  public void shutdown() {
    mMediaRecorder.release();
  }

  // TODO(damonkohler): This shares a lot of code with the CameraFacade. It's probably worth moving
  // it there.
  private FutureActivityTask<Exception> prepare() throws Exception {
    FutureActivityTask<Exception> task = new FutureActivityTask<Exception>() {
      @Override
      public void onCreate() {
        super.onCreate();
        final SurfaceView view = new SurfaceView(getActivity());
        getActivity().setContentView(view);
        getActivity().getWindow().setSoftInputMode(
            WindowManager.LayoutParams.SOFT_INPUT_STATE_UNCHANGED);
        view.getHolder().setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        view.getHolder().addCallback(new Callback() {
          @Override
          public void surfaceDestroyed(SurfaceHolder holder) {
          }

          @Override
          public void surfaceCreated(SurfaceHolder holder) {
            try {
              mMediaRecorder.setPreviewDisplay(view.getHolder().getSurface());
              mMediaRecorder.prepare();
              setResult(null);
            } catch (IOException e) {
              setResult(e);
            }
          }

          @Override
          public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
          }
        });
      }
    };

    FutureActivityTaskExecutor taskExecutor =
        ((BaseApplication) mService.getApplication()).getTaskExecutor();
    taskExecutor.execute(task);

    Exception e = task.getResult();
    if (e != null) {
      throw e;
    }
    return task;
  }

  private int convertSecondsToMilliseconds(Double seconds) {
    if (seconds == null) {
      return 0;
    }
    return (int) (seconds * 1000L);
  }
}
