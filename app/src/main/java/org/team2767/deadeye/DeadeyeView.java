package org.team2767.deadeye;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.util.AttributeSet;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import org.team2767.deadeye.di.Injector;
import timber.log.Timber;

/** Deadeye main view. */
public class DeadeyeView extends GLSurfaceView {

  private final DeadeyeRenderer renderer;

  public DeadeyeView(@NonNull Context context, @Nullable AttributeSet attrs) {
    super(context, attrs);

    setEGLContextClientVersion(2);

    renderer = Injector.get().deadeyeRendererFactory().create(this);
    setRenderer(renderer);
    setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);

    Timber.tag("LifeCycles");
    Timber.d("DeadeyeView constructed");
  }

  public DeadeyeView(Context context) {
    this(context, null);
  }

  public void setHueRange(int low, int high) {
    renderer.setHueRange(low, high);
  }

  public void setSaturationRange(int low, int high) {
    renderer.setSaturationRange(low, high);
  }

  public void setValueRange(int low, int high) {
    renderer.setValueRange(low, high);
  }

  public void setMonitor(FrameProcessor.Monitor monitor) {
    renderer.setMonitor(monitor);
  }

  public void setContour(FrameProcessor.Contours contour) {
    renderer.setContours(contour);
  }
}
