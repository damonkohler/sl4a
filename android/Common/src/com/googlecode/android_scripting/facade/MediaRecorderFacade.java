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
import android.view.SurfaceHolder.Callback;
import android.view.ViewGroup.LayoutParams;

import com.googlecode.android_scripting.BaseApplication;
import com.googlecode.android_scripting.TaskQueue;
import com.googlecode.android_scripting.activity.ScriptingLayerServiceHelper;
import com.googlecode.android_scripting.future.FutureActivityTask;
import com.googlecode.android_scripting.future.FutureResult;
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
      + "Duration specifies the maximum duration of the recording session. "
      + "If duration is not provided this method will return immediately and the recording will only be stopped "
      + "when recorderStop is called or when a scripts exits. "
      + "Otherwise it will block for the time period equal to the duration argument.")
  public void recorderCaptureVideo(@RpcParameter(name = "targetPath") String targetPath,
      @RpcParameter(name = "duration") @RpcOptional Double duration,
      @RpcParameter(name = "recordAudio") @RpcDefault("true") Boolean recordAudio) throws Exception {
    int ms = convertSecondsToMilliseconds(duration);
    startVideoRecording(targetPath, ms, recordAudio);
  }

  private void startVideoRecording(String targetPath, int milliseconds, boolean withAudio)
      throws Exception {
    mMediaRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);
    if (withAudio) {
      int audioSource = MediaRecorder.AudioSource.MIC;
      try {
        Field source =
            Class.forName("android.media.MediaRecorder$AudioSource").getField("CAMCORDER");
        audioSource = source.getInt(null);
      } catch (Exception e) {
        e.printStackTrace();
      }
      mMediaRecorder.setAudioSource(audioSource);
      mMediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.DEFAULT);
      mMediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.DEFAULT);
    } else {
      mMediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.DEFAULT);
    }
    mMediaRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.DEFAULT);

    mMediaRecorder.setOutputFile(targetPath);
    if (milliseconds > 0) {
      mMediaRecorder.setMaxDuration(milliseconds);
    }
    prepare();
    mMediaRecorder.start();
    if (milliseconds > 0) {
      new CountDownLatch(1).await(milliseconds, TimeUnit.MILLISECONDS);
    }
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

  private void prepare() throws Exception {
    FutureActivityTask<Exception> task = new FutureActivityTask<Exception>() {
      @Override
      public void run(final ScriptingLayerServiceHelper activity,
          final FutureResult<Exception> result) {
        final SurfaceView view = new SurfaceView(activity);
        activity.setContentView(view, new LayoutParams(LayoutParams.FILL_PARENT,
            LayoutParams.FILL_PARENT));
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
              result.set(null);
            } catch (IOException e) {
              result.set(e);
            }
            activity.taskDone(getTaskId());
          }

          @Override
          public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
          }
        });
      }
    };
    TaskQueue taskQueue = ((BaseApplication) mService.getApplication()).getTaskQueue();
    taskQueue.offer(task);

    Exception e = task.getFutureResult().get();
    if (e != null) {
      throw e;
    }
  }

  private int convertSecondsToMilliseconds(Double seconds) {
    if (seconds == null) {
      return 0;
    }
    return (int) (seconds * 1000L);
  }
}
