package fengshihao.com.fcameralib;

import android.view.TextureView;

public interface ICamera {

  void setCameraId(String id);

  boolean open();

  void setPreview(TextureView view);

  void onResume();

  void onPause();

}
