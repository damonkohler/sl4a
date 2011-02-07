/*
 * Copyright (C) 2010 Google Inc.
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
import android.hardware.Camera;
import android.hardware.Camera.AutoFocusCallback;
import android.hardware.Camera.Parameters;
import android.hardware.Camera.PictureCallback;
import android.net.Uri;
import android.os.Bundle;
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
import com.googlecode.android_scripting.rpc.RpcParameter;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.concurrent.CountDownLatch;

/**
 * Access Camera functions.
 * 
 */
public class CameraFacade extends RpcReceiver {

  private final Service mService;
  private final Parameters mParameters;

  private class BooleanResult {
    boolean mmResult = false;
  }

  public CameraFacade(FacadeManager manager) {
    super(manager);
    mService = manager.getService();
    Camera camera = Camera.open();
    try {
      mParameters = camera.getParameters();
    } finally {
      camera.release();
    }
  }

  @Rpc(description = "Take a picture and save it to the specified path.", returns = "A map of Booleans autoFocus and takePicture where True indicates success.")
  public Bundle cameraCapturePicture(@RpcParameter(name = "targetPath") final String targetPath,
      @RpcParameter(name = "useAutoFocus") @RpcDefault("true") Boolean useAutoFocus)
      throws InterruptedException {
    final BooleanResult autoFocusResult = new BooleanResult();
    final BooleanResult takePictureResult = new BooleanResult();

    Camera camera = Camera.open();
    camera.setParameters(mParameters);

    try {
      Method method = camera.getClass().getMethod("setDisplayOrientation", int.class);
      method.invoke(camera, 90);
    } catch (Exception e) {
      Log.e(e);
    }

    try {
      FutureActivityTask<SurfaceHolder> previewTask = setPreviewDisplay(camera);
      camera.startPreview();
      if (useAutoFocus) {
        autoFocus(autoFocusResult, camera);
      }
      takePicture(new File(targetPath), takePictureResult, camera);
      previewTask.finish();
    } catch (Exception e) {
      Log.e(e);
    } finally {
      camera.release();
    }

    Bundle result = new Bundle();
    result.putBoolean("autoFocus", autoFocusResult.mmResult);
    result.putBoolean("takePicture", takePictureResult.mmResult);
    return result;
  }

  private FutureActivityTask<SurfaceHolder> setPreviewDisplay(Camera camera) throws IOException,
      InterruptedException {
    FutureActivityTask<SurfaceHolder> task = new FutureActivityTask<SurfaceHolder>() {
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
            setResult(view.getHolder());
          }

          @Override
          public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
          }
        });
      }
    };
    FutureActivityTaskExecutor taskQueue =
        ((BaseApplication) mService.getApplication()).getTaskExecutor();
    taskQueue.execute(task);
    camera.setPreviewDisplay(task.getResult());
    return task;
  }

  private void takePicture(final File file, final BooleanResult takePictureResult,
      final Camera camera) throws InterruptedException {
    final CountDownLatch latch = new CountDownLatch(1);
    camera.takePicture(null, null, new PictureCallback() {
      @Override
      public void onPictureTaken(byte[] data, Camera camera) {
        if (!FileUtils.makeDirectories(file.getParentFile(), 0755)) {
          takePictureResult.mmResult = false;
          return;
        }
        try {
          FileOutputStream output = new FileOutputStream(file);
          output.write(data);
          output.close();
          takePictureResult.mmResult = true;
        } catch (FileNotFoundException e) {
          Log.e("Failed to save picture.", e);
          takePictureResult.mmResult = false;
          return;
        } catch (IOException e) {
          Log.e("Failed to save picture.", e);
          takePictureResult.mmResult = false;
          return;
        } finally {
          latch.countDown();
        }
      }
    });
    latch.await();
  }

  private void autoFocus(final BooleanResult result, final Camera camera)
      throws InterruptedException {
    final CountDownLatch latch = new CountDownLatch(1);
    {
      camera.autoFocus(new AutoFocusCallback() {
        @Override
        public void onAutoFocus(boolean success, Camera camera) {
          result.mmResult = success;
          latch.countDown();
        }
      });
      latch.await();
    }
  }

  @Override
  public void shutdown() {
    // Nothing to clean up.
  }

  @Rpc(description = "Starts the image capture application to take a picture and saves it to the specified path.")
  public void cameraInteractiveCapturePicture(
      @RpcParameter(name = "targetPath") final String targetPath) {
    Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
    File file = new File(targetPath);
    intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(file));
    AndroidFacade facade = mManager.getReceiver(AndroidFacade.class);
    facade.startActivityForResult(intent);
  }
}
