package fengshihao.com.fcameralib;

import java.lang.ref.WeakReference;

import android.app.Application;
import android.content.Context;
import android.hardware.camera2.CameraManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

public class CameraHelper {

  private static final String TAG = "CameraHelper" ;
  private static CameraHelper mInstance = new CameraHelper();
  private CameraHelper() {}

  public static CameraHelper getInstance() {
    return mInstance;
  }

  @NonNull
  private WeakReference<Context> mActivity = new WeakReference<>(null);
  private Application mApp = null;

  public void setContext(Application ctx) {
    Log.d(TAG, "setContext() called with: ctx = [" + ctx + "]");
    mApp = ctx;
  }

  @Nullable
  Context getApplication() {
    return mApp;
  }

  CameraManager getCameraManger() {
    if (mApp == null) {
      return null;
    }

    return (CameraManager) mApp.getSystemService(Context.CAMERA_SERVICE);
  }

  public static ICamera createCamera() {
    return new Camera2Wrapper();
  }
}
