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

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executor;

import android.app.Service;
import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.hardware.Camera;
import android.hardware.Camera.Parameters;
import android.hardware.Camera.PreviewCallback;
import android.hardware.Camera.Size;
import android.util.Base64;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.WindowManager;
import android.view.SurfaceHolder.Callback;

import com.googlecode.android_scripting.BaseApplication;
import com.googlecode.android_scripting.FutureActivityTaskExecutor;
import com.googlecode.android_scripting.Log;
import com.googlecode.android_scripting.SingleThreadExecutor;
import com.googlecode.android_scripting.SimpleServer.SimpleServerObserver;
import com.googlecode.android_scripting.future.FutureActivityTask;
import com.googlecode.android_scripting.jsonrpc.RpcReceiver;
import com.googlecode.android_scripting.rpc.Rpc;
import com.googlecode.android_scripting.rpc.RpcDefault;
import com.googlecode.android_scripting.rpc.RpcMinSdk;
import com.googlecode.android_scripting.rpc.RpcOptional;
import com.googlecode.android_scripting.rpc.RpcParameter;

/**
 * Manages access to camera streaming.
 * <br>
 * <h3>Usage Notes</h3>
 * <br><b>webCamStart</b> and <b>webCamStop</b> are used to start and stop an Mpeg stream on a given port. <b>webcamAdjustQuality</b> is used to ajust the quality of the streaming video.
 * <br><b>cameraStartPreview</b> is used to get access to the camera preview screen. It will generate "preview" events as images become available.
 * <br>The preview has two modes: data or file. If you pass a non-blank, writable file path to the <b>cameraStartPreview</b> it will store jpg images in that folder. 
 * It is up to the caller to clean up these files after the fact. If no file element is provided, 
 * the event will include the image data as a base64 encoded string. 
 * <h3>Event details</h3>
 * <br>The data element of the preview event will be a map, with the following elements defined.
 * <ul>
 * <li><b>format</b> - currently always "jpeg"
 * <li><b>width</b> - image width (in pixels)
 * <li><b>height</b> - image height (in pixels)
 * <li><b>quality</b> - JPEG quality. Number from 1-100
 * <li><b>filename</b> - Name of file where image was saved. Only relevant if filepath defined.
 * <li><b>error</b> - included if there was an IOException saving file, ie, disk full or path write protected.
 * <li><b>encoding</b> - Data encoding. If filepath defined, will be "file" otherwise "base64"
 * <li><b>data</b> - Base64 encoded image data.
 * </ul>
 *<br>Note that "filename", "error" and "data" are mutual exclusive.
 *<br>
 *<br>The webcam and preview modes use the same resources, so you can't use them both at the same time. Stop one mode before starting the other.
 *
 * @author Damon Kohler (damonkohler@gmail.com) (probably)
 * @author Robbie Matthews (rjmatthews62@gmail.com)
 *
 */
@RpcMinSdk(8)
public class WebCamFacade extends RpcReceiver {

  private final Service mService;
  private final Executor mJpegCompressionExecutor = new SingleThreadExecutor();
  private final ByteArrayOutputStream mJpegCompressionBuffer = new ByteArrayOutputStream();

  private volatile byte[] mJpegData;

  private CountDownLatch mJpegDataReady;
  private boolean mStreaming;
  private int mPreviewHeight;
  private int mPreviewWidth;
  private int mJpegQuality;

  private MjpegServer mJpegServer;
  private FutureActivityTask<SurfaceHolder> mPreviewTask;
  private Camera mCamera;
  private Parameters mParameters;
  private final EventFacade mEventFacade;
  private boolean mPreview;
  private File mDest;
  
  private final PreviewCallback mPreviewCallback = new PreviewCallback() {
    @Override
    public void onPreviewFrame(final byte[] data, final Camera camera) {
      mJpegCompressionExecutor.execute(new Runnable() {
        @Override
        public void run() {
          mJpegData = compressYuvToJpeg(data);
          mJpegDataReady.countDown();
          if (mStreaming) {
            camera.setOneShotPreviewCallback(mPreviewCallback);
          }
        }
      });
    }
  };

  private final PreviewCallback mPreviewEvent = new PreviewCallback() {
    @Override
    public void onPreviewFrame(final byte[] data, final Camera camera) {
      mJpegCompressionExecutor.execute(new Runnable() {
        @Override
        public void run() {
          mJpegData = compressYuvToJpeg(data);
          Map<String,Object> map = new HashMap<String, Object>(); 
          map.put("format", "jpeg");
          map.put("width", mPreviewWidth);
          map.put("height", mPreviewHeight);
          map.put("quality", mJpegQuality);
          if (mDest!=null) {
            try {
              File dest=File.createTempFile("prv",".jpg",mDest);
              OutputStream output = new FileOutputStream(dest);
              output.write(mJpegData);
              output.close();
              map.put("encoding","file");
              map.put("filename",dest.toString());
            } catch (IOException e) {
              map.put("error", e.toString());
            }
          }
          else {
            map.put("encoding","Base64");
            map.put("data", Base64.encodeToString(mJpegData, Base64.DEFAULT));
          }
          mEventFacade.postEvent("preview", map);
          if (mPreview) {
            camera.setOneShotPreviewCallback(mPreviewEvent);
          }
        }
      });
    }
  };
  
  public WebCamFacade(FacadeManager manager) {
    super(manager);
    mService = manager.getService();
    mJpegDataReady = new CountDownLatch(1);
    mEventFacade = manager.getReceiver(EventFacade.class);
  }

  private byte[] compressYuvToJpeg(final byte[] yuvData) {
    mJpegCompressionBuffer.reset();
    YuvImage yuvImage =
        new YuvImage(yuvData, ImageFormat.NV21, mPreviewWidth, mPreviewHeight, null);
    yuvImage.compressToJpeg(new Rect(0, 0, mPreviewWidth, mPreviewHeight), mJpegQuality,
        mJpegCompressionBuffer);
    return mJpegCompressionBuffer.toByteArray();
  }

  @Rpc(description = "Starts an MJPEG stream and returns a Tuple of address and port for the stream.")
  public InetSocketAddress webcamStart(
      @RpcParameter(name = "resolutionLevel", description = "increasing this number provides higher resolution") @RpcDefault("0") Integer resolutionLevel,
      @RpcParameter(name = "jpegQuality", description = "a number from 0-100") @RpcDefault("20") Integer jpegQuality,
      @RpcParameter(name = "port", description = "If port is specified, the webcam service will bind to port, otherwise it will pick any available port.") @RpcDefault("0") Integer port)
      throws Exception {
    try {
      openCamera(resolutionLevel, jpegQuality);
      return startServer(port);
    } catch (Exception e) {
      webcamStop();
      throw e;
    }
  }

  private InetSocketAddress startServer(Integer port) {
    mJpegServer = new MjpegServer(new JpegProvider() {
      @Override
      public byte[] getJpeg() {
        try {
          mJpegDataReady.await();
        } catch (InterruptedException e) {
          Log.e(e);
        }
        return mJpegData;
      }
    });
    mJpegServer.addObserver(new SimpleServerObserver() {
      @Override
      public void onDisconnect() {
        if (mJpegServer.getNumberOfConnections() == 0 && mStreaming) {
          stopStream();
        }
      }

      @Override
      public void onConnect() {
        if (!mStreaming) {
          startStream();
        }
      }
    });
    return mJpegServer.startPublic(port);
  }

  private void stopServer() {
    if (mJpegServer != null) {
      mJpegServer.shutdown();
      mJpegServer = null;
    }
  }

  @Rpc(description = "Adjusts the quality of the webcam stream while it is running.")
  public void webcamAdjustQuality(
      @RpcParameter(name = "resolutionLevel", description = "increasing this number provides higher resolution") @RpcDefault("0") Integer resolutionLevel,
      @RpcParameter(name = "jpegQuality", description = "a number from 0-100") @RpcDefault("20") Integer jpegQuality)
      throws Exception {
    if (mStreaming == false) {
      throw new IllegalStateException("Webcam not streaming.");
    }
    stopStream();
    releaseCamera();
    openCamera(resolutionLevel, jpegQuality);
    startStream();
  }

  private void openCamera(Integer resolutionLevel, Integer jpegQuality) throws IOException,
      InterruptedException {
    mCamera = Camera.open();
    mParameters = mCamera.getParameters();
    mParameters.setPictureFormat(ImageFormat.JPEG);
    mParameters.setPreviewFormat(ImageFormat.JPEG);
    List<Size> supportedPreviewSizes = mParameters.getSupportedPreviewSizes();
    Collections.sort(supportedPreviewSizes, new Comparator<Size>() {
      @Override
      public int compare(Size o1, Size o2) {
        return o1.width - o2.width;
      }
    });
    Size previewSize =
        supportedPreviewSizes.get(Math.min(resolutionLevel, supportedPreviewSizes.size() - 1));
    mPreviewHeight = previewSize.height;
    mPreviewWidth = previewSize.width;
    mParameters.setPreviewSize(mPreviewWidth, mPreviewHeight);
    mJpegQuality = Math.min(Math.max(jpegQuality, 0), 100);
    mCamera.setParameters(mParameters);
    // TODO(damonkohler): Rotate image based on orientation.
    mPreviewTask = createPreviewTask();
    mCamera.startPreview();
  }

  private void startStream() {
    mStreaming = true;
    mCamera.setOneShotPreviewCallback(mPreviewCallback);
  }

  private void stopStream() {
    mJpegDataReady = new CountDownLatch(1);
    mStreaming = false;
    if (mPreviewTask != null) {
      mPreviewTask.finish();
      mPreviewTask = null;
    }
  }

  private void releaseCamera() {
    if (mCamera != null) {
      mCamera.release();
      mCamera = null;
    }
    mParameters = null;
  }

  @Rpc(description = "Stops the webcam stream.")
  public void webcamStop() {
    stopServer();
    stopStream();
    releaseCamera();
  }

  private FutureActivityTask<SurfaceHolder> createPreviewTask() throws IOException,
      InterruptedException {
    FutureActivityTask<SurfaceHolder> task = new FutureActivityTask<SurfaceHolder>() {
      @Override
      public void onCreate() {
        super.onCreate();
        final SurfaceView view = new SurfaceView(getActivity());
        getActivity().setContentView(view);
        getActivity().getWindow().setSoftInputMode(
            WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
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
    FutureActivityTaskExecutor taskExecutor =
        ((BaseApplication) mService.getApplication()).getTaskExecutor();
    taskExecutor.execute(task);
    mCamera.setPreviewDisplay(task.getResult());
    return task;
  }

  @Rpc(description = "Start Preview Mode. Throws 'preview' events.",returns="True if successful")
  public boolean cameraStartPreview(
          @RpcParameter(name = "resolutionLevel", description = "increasing this number provides higher resolution") @RpcDefault("0") Integer resolutionLevel,
          @RpcParameter(name = "jpegQuality", description = "a number from 0-100") @RpcDefault("20") Integer jpegQuality,
          @RpcParameter(name = "filepath", description = "Path to store jpeg files.") @RpcOptional String filepath)
      throws InterruptedException {
    mDest=null;
    if (filepath!=null && (filepath.length()>0)) {
      mDest = new File(filepath);
      if (!mDest.exists()) mDest.mkdirs();
      if (!(mDest.isDirectory() && mDest.canWrite())) {
        return false;
      }
    }
      
    try {
      openCamera(resolutionLevel, jpegQuality);
    } catch (IOException e) {
      Log.e(e);
      return false;
    }
    startPreview();
    return true;
  }

  @Rpc(description = "Stop the preview mode.")
  public void cameraStopPreview() {
    stopPreview();
  }

  private void startPreview() {
    mPreview = true;
    mCamera.setOneShotPreviewCallback(mPreviewEvent);
  }
  
  private void stopPreview() {
    mPreview = false;
    if (mPreviewTask!=null)
    {
      mPreviewTask.finish();
      mPreviewTask=null;
    }
    releaseCamera();
  }

  @Override
  public void shutdown() {
    mPreview=false;
    webcamStop();
  }
}
