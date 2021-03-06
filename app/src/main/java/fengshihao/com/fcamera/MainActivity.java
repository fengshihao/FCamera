package fengshihao.com.fcamera;

import android.content.res.Configuration;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.TextureView;

import fengshihao.com.fcameralib.CameraHelper;
import fengshihao.com.fcameralib.ICamera;

public class MainActivity extends AppCompatActivity {

  private static final String TAG = "MainActivity";

  private TextureView mCameraView;

  private ICamera mCamera;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    Log.d(TAG, "onCreate() called with: savedInstanceState = [" + savedInstanceState + "]");
    setContentView(R.layout.activity_main);
    mCameraView = findViewById(R.id.TextureView_camera);
    mCamera = CameraHelper.createCamera();
    CameraHelper.getInstance().setContext(getApplication());

  }
  
  @Override
  protected void onSaveInstanceState(Bundle outBundle) {
    super.onSaveInstanceState(outBundle);
    Log.d(TAG, "onSaveInstanceState: ");
  }

  @Override
  protected void onRestoreInstanceState(Bundle savedInstanceState) {
    super.onRestoreInstanceState(savedInstanceState);
    Log.d(TAG, "onRestoreInstanceState: ");
  }


  @Override
  protected void onResume() {
    super.onResume();
    Log.d(TAG, "onResume() called");
    mCamera.setCameraId("1");
    mCamera.setPreview(mCameraView);
    mCamera.open();
  }

  @Override
  protected void onStart() {
    super.onStart();
    Log.d(TAG, "onStart() called");
  }

  @Override
  protected void onPause() {
    super.onPause();
    Log.d(TAG, "onPause() called");
  }


  @Override
  protected void onStop() {
    super.onStop();
    Log.d(TAG, "onStop() called");
  }

  @Override
  protected void onDestroy() {
    super.onDestroy();
    Log.d(TAG, "onDestroy() called");
    mCamera.close();
  }

  @Override
  public void onConfigurationChanged(Configuration config) {
    super.onConfigurationChanged(config);
    Log.d(TAG, "onConfigurationChanged: ");
  }

}
