package fengshihao.com.fcameralib;

import java.util.Collections;
import java.util.Objects;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.CaptureResult;
import android.hardware.camera2.TotalCaptureResult;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.util.Log;
import android.view.Surface;
import android.view.TextureView;

import static fengshihao.com.fcameralib.Camera2States.State;

class Camera2Wrapper implements ICamera {

  private static final String TAG = "Camera2Wrapper";
  private String mCameraId;
  private CameraDevice mCameraDevice;
  private TextureView mPreviewView;
  private Surface mPreviewSurface;
  private CameraCaptureSession mCaptureSession;
  private CaptureRequest.Builder mPreviewRequestBuilder;
  private CaptureRequest mPreviewRequest;

  @NonNull
  private final Camera2States mStates = new Camera2States();

  private int mPreviewWidth;
  private int mPreviewHeight;


  @SuppressLint("MissingPermission")
  public boolean open() {
    Log.d(TAG, "open() called");
    if (!isReadyToOpen()) {
      return false;
    }

    if (!mStates.canGotoState(State.OPENING)) {
      Log.d(TAG, "open: can't open now is " + mStates.current());
      return false;
    }

    CameraManager manager = CameraHelper.getInstance().getCameraManger();
    if (manager == null) {
      return false;
    }

    try {
      manager.openCamera(mCameraId, mStateCallback, null);
    } catch (CameraAccessException e) {
      Log.e(TAG, "open: ", e);
      return false;
    }
    mStates.toState(State.OPENING);
    return true;
  }

  private void startIfReady() {
    if (mPreviewSurface == null) {
      Log.d(TAG, "startIfReady: mPreviewSurface is not ready");
      return;
    }

    if (mCameraDevice == null) {
      Log.d(TAG, "startIfReady: mCameraDevice is not ready");
      return;
    }

    Log.d(TAG, "startIfReady: ");
    prepareRequestBuilder();

    try {
      mCameraDevice.createCaptureSession(Collections.singletonList(mPreviewSurface),
          new CameraCaptureSession.StateCallback() {

            @Override
            public void onConfigured(@NonNull CameraCaptureSession cameraCaptureSession) {
              // The camera is already closed
              if (null == mCameraDevice) {
                return;
              }

              mCaptureSession = cameraCaptureSession;
              try {
                // Auto focus should be continuous for camera preview.
                mPreviewRequestBuilder.set(CaptureRequest.CONTROL_AF_MODE,
                    CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE);

                // Finally, we start displaying the camera preview.
                mPreviewRequest = mPreviewRequestBuilder.build();
                mCaptureSession.setRepeatingRequest(mPreviewRequest, mCaptureCallback, null);
              } catch (CameraAccessException e) {
                Log.e(TAG, "onConfigured: ", e);
              }
            }

            @Override
            public void onConfigureFailed(@NonNull CameraCaptureSession cameraCaptureSession) {
              Log.d(TAG, "onConfigureFailed: ");
            }
          }, null);
    } catch (CameraAccessException e) {
      Log.e(TAG, "startIfReady: ", e);
    }
  }

  private CameraCaptureSession.CaptureCallback mCaptureCallback
      = new CameraCaptureSession.CaptureCallback() {

    @Override
    public void onCaptureProgressed(@NonNull CameraCaptureSession session,
                                    @NonNull CaptureRequest request,
                                    @NonNull CaptureResult partialResult) {
      if (mCaptureSession != session) {
        Log.w(TAG, "onCaptureProgressed: now this session");
        return;
      }
      Log.v(TAG, "onCaptureProgressed: " + partialResult);

    }

    @Override
    public void onCaptureCompleted(@NonNull CameraCaptureSession session,
                                   @NonNull CaptureRequest request,
                                   @NonNull TotalCaptureResult result) {
      Log.v(TAG, "onCaptureProgressed: " + result);
    }

  };

  private void prepareRequestBuilder() {
    Log.d(TAG, "prepareRequestBuilder: ");
    try {
      mPreviewRequestBuilder
          = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
    } catch (CameraAccessException e) {
      Log.e(TAG, "prepareRequestBuilder: ", e);
      return;
    }
    mPreviewRequestBuilder.addTarget(mPreviewSurface);
  }

  @Override
  public void setPreview(TextureView view) {
    if (mPreviewView == view) {
      return;
    }
    Log.d(TAG, "setPreview: ");

    mPreviewView = view;
    if (view.isAvailable()) {
      viewSizeUpdate(view.getWidth(), view.getHeight());
    } else {
      view.setSurfaceTextureListener(new TextureView.SurfaceTextureListener() {
        @Override
        public void onSurfaceTextureAvailable(SurfaceTexture texture, int w, int h) {
          Log.d(TAG, "onSurfaceTextureAvailable()  w = [" + w + "], h = [" + h + "]");
          viewSizeUpdate(w, h);
          startIfReady();
        }

        @Override
        public void onSurfaceTextureSizeChanged(SurfaceTexture texture, int w, int h) {
          Log.d(TAG, "onSurfaceTextureSizeChanged()  w = [" + w + "], h = [" + h + "]");
          viewSizeUpdate(w, h);
        }

        @Override
        public boolean onSurfaceTextureDestroyed(SurfaceTexture texture) {
          Log.d(TAG, "onSurfaceTextureDestroyed: ");
          return false;
        }

        @Override
        public void onSurfaceTextureUpdated(SurfaceTexture texture) {
          Log.v(TAG, "onSurfaceTextureUpdated: ");
        }
      });

      //view.setSurfaceTexture(new SurfaceTexture());
    }
  }

  private void viewSizeUpdate(int w, int h) {
    Log.d(TAG, "viewSizeUpdate() called with: w = [" + w + "], h = [" + h + "]");
    if (mPreviewWidth == w && mPreviewHeight == h) {
      Log.w(TAG, "viewSizeUpdate: no update");
      return;
    }
    SurfaceTexture texture = mPreviewView.getSurfaceTexture();
    texture.setDefaultBufferSize(h, w);
    mPreviewSurface = new Surface(texture);
    mPreviewWidth = w;
    mPreviewHeight = h;
  }

  private boolean isReadyToOpen() {
    if (TextUtils.isEmpty(mCameraId)) {
      Log.e(TAG, "open: set camera id first ");
      return false;
    }

    if (ContextCompat.checkSelfPermission(
        Objects.requireNonNull(CameraHelper.getInstance().getApplication())
        , Manifest.permission.CAMERA)
        != PackageManager.PERMISSION_GRANTED) {
      Log.e(TAG, "open: no permission to open camera");
      return false;
    }

    return true;
  }

  public void setCameraId(String id) {
    Log.d(TAG, "setCameraId() called with: id = [" + id + "]");
    // TODO: 2018/12/6 check it whether could be change.
    mCameraId = id;
  }

  private final CameraDevice.StateCallback mStateCallback = new CameraDevice.StateCallback() {

    @Override
    public void onOpened(@NonNull CameraDevice cameraDevice) {
      Log.d(TAG, "onOpened() called with: cameraDevice = ["
          + cameraDevice + "]");
      setCameraDevice(cameraDevice);
      startIfReady();
    }

    @Override
    public void onDisconnected(@NonNull CameraDevice cameraDevice) {
      Log.d(TAG, "onDisconnected() called with: cameraDevice = ["
          + cameraDevice + "]");
      setCameraDevice(null);
    }

    @Override
    public void onError(@NonNull CameraDevice cameraDevice, int error) {
      Log.e(TAG, "onError() called with: cameraDevice = [" + cameraDevice
          + "], error = [" + error + "]");
      setCameraDevice(null);
    }
  };

  private void setCameraDevice(CameraDevice device) {
    Log.d(TAG, "setCameraDevice() called with: device = [" + device + "]");

    if (mCameraDevice != null) {
      if (device == null) {
        closeCameraDevice();
      }
    }

    if (device != null) {
      mCameraDevice = device;
      mStates.toState(State.OPEN);
    }
  }

  private void closeCameraDevice() {
    if (mCameraDevice == null) {
      return;
    }
    Log.d(TAG, "closeCameraDevice() called");
    mCameraDevice.close();
    mCameraDevice = null;
    closeCameraSession();
    mStates.toState(State.IDLE);
  }

  private void closeCameraSession() {
    if (mCaptureSession != null) {
      Log.d(TAG, "closeCameraSession: ");
      mCaptureSession.close();
      mCaptureSession = null;
    }
  }

  @Override
  public void onResume() {

  }

  @Override
  public void onPause() {

  }

  @Override
  public void close() {
    Log.d(TAG, "close: ");
    closeCameraDevice();
  }
}
