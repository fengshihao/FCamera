package fengshihao.com.fcameralib;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;

import android.app.Application;
import android.content.Context;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.util.Size;
import android.view.TextureView;

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
    printInfo();
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

  static void printInfo() {
    try {
      CameraManager manager = getInstance().getCameraManger();
      String [] cameras = manager.getCameraIdList();
      Log.d(TAG, "printInfo: get cameras=" + Arrays.toString(cameras));
      for (String cid : cameras) {
        CameraCharacteristics cinfo = manager.getCameraCharacteristics(cid);
        Integer facing = cinfo.get(CameraCharacteristics.LENS_FACING);

        StreamConfigurationMap map = cinfo.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
        if (map == null) {
          Log.w(TAG, "printInfo: no StreamConfigurationMap");
          continue;
        }
        int[] outputFormats = map.getOutputFormats();

        Size[] outputSizes = map.getOutputSizes(SurfaceTexture.class);

        Log.d(TAG, "printInfo: cid=" + cid + " facing=" + facing);
        Log.d(TAG, "printInfo out formats=" + Arrays.toString(outputFormats));

        for (Size sz : outputSizes) {
          Log.d(TAG, "printInfo: output w=" + sz.getWidth() + " h=" + sz.getHeight());
        }


      }
    } catch (CameraAccessException e) {
      Log.e(TAG, "printInfo: ", e);
    }
  }
}
