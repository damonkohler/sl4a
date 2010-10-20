package com.googlecode.android_scripting.facade;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.Executor;

import android.app.Service;
import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.hardware.Camera;
import android.hardware.Camera.Parameters;
import android.hardware.Camera.PreviewCallback;
import android.hardware.Camera.Size;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.WindowManager;
import android.view.SurfaceHolder.Callback;

import com.googlecode.android_scripting.BaseApplication;
import com.googlecode.android_scripting.FutureActivityTaskExecutor;
import com.googlecode.android_scripting.SingleThreadExecutor;
import com.googlecode.android_scripting.future.FutureActivityTask;
import com.googlecode.android_scripting.jsonrpc.RpcReceiver;
import com.googlecode.android_scripting.rpc.Rpc;
import com.googlecode.android_scripting.rpc.RpcDefault;
import com.googlecode.android_scripting.rpc.RpcOptional;
import com.googlecode.android_scripting.rpc.RpcParameter;

public class WebCamFacade extends RpcReceiver {

  private final Service mService;
  private final Executor mJpegCompressionExecutor = new SingleThreadExecutor();
  private final ByteArrayOutputStream mJpegCompressionBuffer = new ByteArrayOutputStream();

  private boolean mStreaming;
  private byte[] mJpegData;
  private int mPreviewHeight;
  private int mPreviewWidth;
  private int mJpegQuality;

  private MjpegServer mJpegServer;
  private FutureActivityTask<SurfaceHolder> mPreviewTask;
  private Camera mCamera;

  private final PreviewCallback mPreviewCallback = new PreviewCallback() {
    @Override
    public void onPreviewFrame(final byte[] data, final Camera camera) {
      mJpegCompressionExecutor.execute(new Runnable() {
        @Override
        public void run() {
          mJpegData = compressYuvToJpeg(data);
          if (mStreaming) {
            camera.setOneShotPreviewCallback(mPreviewCallback);
          }
        }
      });
    }
  };
  private Parameters mParameters;

  private byte[] compressYuvToJpeg(final byte[] yuvData) {
    mJpegCompressionBuffer.reset();
    YuvImage yuvImage =
        new YuvImage(yuvData, ImageFormat.NV21, mPreviewWidth, mPreviewHeight, null);
    yuvImage.compressToJpeg(new Rect(0, 0, mPreviewWidth, mPreviewHeight), mJpegQuality,
        mJpegCompressionBuffer);
    return mJpegCompressionBuffer.toByteArray();
  }

  public WebCamFacade(FacadeManager manager) {
    super(manager);
    mService = manager.getService();
  }

  @Rpc(description = "Starts an MJPEG stream and returns a Tuple of address and port for the stream.")
  public InetSocketAddress webcamStart(
      @RpcParameter(name = "resolutionLevel", description = "increasing this number provides higher resolution") @RpcDefault("0") Integer resolutionLevel,
      @RpcParameter(name = "jpegQuality", description = "a number from 0-100") @RpcDefault("20") Integer jpegQuality,
      @RpcParameter(name = "port", description = "If port is specified, the webcam service will bind to port, otherwise it will pick any available port.") @RpcOptional Integer port)
      throws Exception {
    try {
      openCamera();
      setCameraParameters(resolutionLevel, jpegQuality);
      // TODO(damonkohler): Rotate image based on orientation.
      mPreviewTask = createPreviewTask();
      mCamera.startPreview();
      mStreaming = true;
      mCamera.setOneShotPreviewCallback(mPreviewCallback);
      mJpegServer = new MjpegServer(new JpegProvider() {
        @Override
        public byte[] getJpeg() {
          return mJpegData;
        }
      });
      return mJpegServer.startPublic(port);
    } catch (Exception e) {
      mStreaming = false;
      releaseCamera();
      throw e;
    }
  }

  private void setCameraParameters(Integer resolutionLevel, Integer jpegQuality) {
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
  }

  private void releaseCamera() {
    if (mCamera != null) {
      mCamera.release();
      mCamera = null;
    }
    mParameters = null;
  }

  private void openCamera() {
    mCamera = Camera.open();
    mParameters = mCamera.getParameters();
  }

  @Rpc(description = "Stops the webcam stream.")
  public void webcamStop() {
    mStreaming = false;
    if (mJpegServer != null) {
      mJpegServer.shutdown();
      mJpegServer = null;
    }
    if (mPreviewTask != null) {
      mPreviewTask.finish();
      mPreviewTask = null;
    }
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

  @Override
  public void shutdown() {
    webcamStop();
  }
}
