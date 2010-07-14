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

import android.content.Intent;
import android.hardware.Camera;
import android.hardware.Camera.AutoFocusCallback;
import android.hardware.Camera.PictureCallback;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;


import com.googlecode.android_scripting.Sl4aLog;
import com.googlecode.android_scripting.jsonrpc.RpcReceiver;
import com.googlecode.android_scripting.rpc.Rpc;
import com.googlecode.android_scripting.rpc.RpcDefault;
import com.googlecode.android_scripting.rpc.RpcParameter;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.concurrent.CountDownLatch;

public class CameraFacade extends RpcReceiver {

  private final AndroidFacade mAndroidFacade;

  private class BooleanResult {
    boolean mmResult = false;
  }

  public CameraFacade(FacadeManager manager) {
    super(manager);
    mAndroidFacade = manager.getReceiver(AndroidFacade.class);
  }

  @Rpc(description = "Take a picture and save it to the specified path.", returns = "A map of Booleans autoFocus and takePicture where True indicates success.")
  public Bundle cameraCapturePicture(@RpcParameter(name = "path") final String path,
      @RpcParameter(name = "useAutoFocus") @RpcDefault("true") Boolean useAutoFocus)
      throws InterruptedException {
    final BooleanResult autoFocusResult = new BooleanResult();
    final BooleanResult takePictureResult = new BooleanResult();

    final Camera camera = Camera.open();
    try {
      camera.startPreview();
      if (useAutoFocus) {
        autoFocus(autoFocusResult, camera);
      }
      takePicture(path, takePictureResult, camera);
    } finally {
      camera.release();
    }

    Bundle result = new Bundle();
    result.putBoolean("autoFocus", autoFocusResult.mmResult);
    result.putBoolean("takePicture", takePictureResult.mmResult);
    return result;
  }

  private void takePicture(final String path, final BooleanResult takePictureResult,
      final Camera camera) throws InterruptedException {
    final CountDownLatch latch = new CountDownLatch(1);
    camera.takePicture(null, null, new PictureCallback() {
      @Override
      public void onPictureTaken(byte[] data, Camera camera) {
        try {
          FileOutputStream output = new FileOutputStream(path);
          output.write(data);
          output.close();
          takePictureResult.mmResult = true;
        } catch (FileNotFoundException e) {
          Sl4aLog.e("Failed to save picture.", e);
          takePictureResult.mmResult = false;
          return;
        } catch (IOException e) {
          Sl4aLog.e("Failed to save picture.", e);
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
  public void cameraInteractiveCapturePicture(@RpcParameter(name = "path") final String path) {
    Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
    File file = new File(path);
    intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(file));
    mAndroidFacade.startActivityForResult(intent);
  }

  @Rpc(description = "Starts the video capture application to record a video and saves it to the specified path.")
  public void cameraInteractiveCaptureVideo(@RpcParameter(name = "path") final String path) {
    Intent intent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
    File file = new File(path);
    intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(file));
    mAndroidFacade.startActivityForResult(intent);
  }
}
