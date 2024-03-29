package org.team2767.deadeye;

import static android.opengl.GLES20.GL_COLOR_BUFFER_BIT;
import static android.opengl.GLES20.GL_FRAMEBUFFER;
import static android.opengl.GLES20.glBindFramebuffer;
import static android.opengl.GLES20.glClear;

import android.graphics.SurfaceTexture;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import com.google.auto.factory.AutoFactory;
import com.google.auto.factory.Provided;
import io.reactivex.subjects.PublishSubject;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;
import org.team2767.deadeye.di.Injector;
import org.team2767.deadeye.opengl.CameraShaderProgram;
import org.team2767.deadeye.opengl.DisplayRectangle;
import org.team2767.deadeye.opengl.FrameBufferHelper;
import org.team2767.deadeye.opengl.TextureHelper;
import org.team2767.deadeye.opengl.TextureShaderProgram;
import timber.log.Timber;

/** Renderer. */
@SuppressWarnings({"unused", "FieldCanBeLocal"})
@AutoFactory
public class DeadeyeRenderer
    implements GLSurfaceView.Renderer, SurfaceTexture.OnFrameAvailableListener {

  private final DeadeyeView deadeyeView;

  private final DisplayRectangle displayRectangle;
  private final Camera camera;
  private final FrameProcessorFactory frameProcessorFactory;
  private final PublishSubject<byte[]> visionDataSubject;
  private TextureShaderProgram textureProgram;
  private CameraShaderProgram cameraProgram;
  private int cameraTextureId;
  private int feedbackTextureId;
  private int targetTextureId;
  private int targetFrameBufferId; // draws to targetTextureId
  private SurfaceTexture surfaceTexture;
  private int width;
  private int height;
  private FrameProcessor frameProcessor;

  DeadeyeRenderer(
      DeadeyeView deadeyeView,
      @Provided DisplayRectangle displayRectangle,
      @Provided Camera camera,
      @Provided FrameProcessorFactory frameProcessorFactory) {
    this.deadeyeView = deadeyeView;
    this.displayRectangle = displayRectangle;
    this.camera = camera;
    this.frameProcessorFactory = frameProcessorFactory;
    visionDataSubject = Injector.get().network().getVisionDataSubject();
  }

  @Override
  public void onSurfaceCreated(GL10 unused, EGLConfig eglConfig) {
    GLES20.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);

    textureProgram = Injector.get().textureShaderProgram();
    cameraProgram = Injector.get().cameraShaderProgram();
  }

  @Override
  public void onSurfaceChanged(GL10 unused, int width, int height) {
    Timber.d("Viewport is %d x %d", width, height);
    this.width = width;
    this.height = height;

    targetTextureId = TextureHelper.initTexture(Camera.WIDTH, Camera.HEIGHT);
    feedbackTextureId = TextureHelper.initTexture(Camera.WIDTH, Camera.HEIGHT);
    cameraTextureId = TextureHelper.initImageTexture();

    textureProgram.setTexture(feedbackTextureId);
    cameraProgram.setTexture(cameraTextureId);

    surfaceTexture = new SurfaceTexture(cameraTextureId);
    surfaceTexture.setOnFrameAvailableListener(this);

    targetFrameBufferId = FrameBufferHelper.initFrameBuffer(targetTextureId);

    camera.stop();
    camera.start(surfaceTexture);

    if (frameProcessor != null) {
      frameProcessor.release();
    }
    frameProcessor = frameProcessorFactory.create(feedbackTextureId, Camera.WIDTH, Camera.HEIGHT);
  }

  @Override
  public void onFrameAvailable(SurfaceTexture surfaceTexture) {
    deadeyeView.requestRender();
  }

  @Override
  public void onDrawFrame(GL10 unused) {
    surfaceTexture.updateTexImage();

    // draw camera texture to framebuffer texture
    glBindFramebuffer(GL_FRAMEBUFFER, targetFrameBufferId);
    GLES20.glViewport(0, 0, Camera.WIDTH, Camera.HEIGHT);
    glClear(GL_COLOR_BUFFER_BIT);
    cameraProgram.useProgram();
    displayRectangle.bindAttributes(cameraProgram);
    displayRectangle.draw();

    // on entry, process() will assume frame buffer to read pixels from is bound
    // on return, the feedback texture is bound to texture unit 0
    frameProcessor.process();

    // send vision data to network
    int latency = camera.latencyForFrameWithTimeStamp(surfaceTexture.getTimestamp());
    byte[] frameData = frameProcessor.getBytes(latency);
    FrameProcessor.dumpFrameData(frameData);
    visionDataSubject.onNext(frameData);

    // draw framebuffer texture to screen
    glBindFramebuffer(GL_FRAMEBUFFER, 0);
    GLES20.glViewport(0, 0, width, height);
    glClear(GL_COLOR_BUFFER_BIT);

    textureProgram.useProgram();
    displayRectangle.bindAttributes(textureProgram);
    displayRectangle.draw();
  }

  public void setHueRange(int low, int high) {
    frameProcessor.setHueRange(low, high);
  }

  public void setSaturationRange(int low, int high) {
    frameProcessor.setSaturationRange(low, high);
  }

  public void setValueRange(int low, int high) {
    frameProcessor.setValueRange(low, high);
  }

  void setMonitor(FrameProcessor.Monitor monitor) {
    frameProcessor.setMonitor(monitor);
  }

  void setContours(FrameProcessor.Contours contours) {
    frameProcessor.setContours(contours);
  }
}
