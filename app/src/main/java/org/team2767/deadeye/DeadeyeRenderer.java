package org.team2767.deadeye;

import android.graphics.SurfaceTexture;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;

import com.google.auto.factory.AutoFactory;
import com.google.auto.factory.Provided;

import org.team2767.deadeye.di.Injector;
import org.team2767.deadeye.opengl.CameraShaderProgram;
import org.team2767.deadeye.opengl.DisplayRectangle;
import org.team2767.deadeye.opengl.FrameBufferHelper;
import org.team2767.deadeye.opengl.TextureHelper;
import org.team2767.deadeye.opengl.TextureShaderProgram;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import hugo.weaving.DebugLog;
import timber.log.Timber;

import static android.opengl.GLES20.GL_COLOR_BUFFER_BIT;
import static android.opengl.GLES20.GL_FRAMEBUFFER;
import static android.opengl.GLES20.glBindFramebuffer;
import static android.opengl.GLES20.glClear;

/**
 * Renderer.
 */
@SuppressWarnings({"unused", "FieldCanBeLocal"})
@AutoFactory
public class DeadeyeRenderer implements GLSurfaceView.Renderer, SurfaceTexture.OnFrameAvailableListener {

    private final DeadeyeView deadeyeView;

    private final DisplayRectangle displayRectangle;
    private final Camera camera;

    private TextureShaderProgram textureProgram;
    private CameraShaderProgram cameraProgram;

    private int cameraTextureId;
    private int feedbackTextureId;
    private int targetTextureId;
    private int targetFrameBufferId; // draws to targetTextureId

    private SurfaceTexture surfaceTexture;

    private boolean frameAvailable;

    private int width;
    private int height;

    private FrameProcessor frameProcessor;

    DeadeyeRenderer(DeadeyeView deadeyeView, @Provided DisplayRectangle displayRectangle,
                    @Provided Camera camera) {
        this.deadeyeView = deadeyeView;
        this.displayRectangle = displayRectangle;
        this.camera = camera;
        Timber.tag("LifeCycles");
        Timber.d("DeadeyeRenderer constructed");
    }

    @Override
    @DebugLog
    public void onSurfaceCreated(GL10 unused, EGLConfig eglConfig) {
        GLES20.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);

        textureProgram = Injector.get().textureShaderProgram();
        cameraProgram = Injector.get().cameraShaderProgram();

        Timber.d("onSurfaceCreated() finished");
    }

    @Override
    @DebugLog
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

        frameProcessor = new FrameProcessor(feedbackTextureId, Camera.WIDTH, Camera.HEIGHT,
                40, 80, 100, 255, 30, 255);
    }

    @Override
    public void onFrameAvailable(SurfaceTexture surfaceTexture) {
        frameAvailable = true;
        deadeyeView.requestRender();
    }

    @Override
    public void onDrawFrame(GL10 unused) {

        if (frameAvailable) {
            surfaceTexture.updateTexImage();
            frameAvailable = false;
        }

        // draw camera texture to framebuffer texture
        glBindFramebuffer(GL_FRAMEBUFFER, targetFrameBufferId);
        GLES20.glViewport(0, 0, Camera.WIDTH, Camera.HEIGHT);
        glClear(GL_COLOR_BUFFER_BIT);

        cameraProgram.useProgram();
        displayRectangle.bindAttributes(cameraProgram);
        displayRectangle.draw();

        frameProcessor.process();

        // draw framebuffer texture to screen
        glBindFramebuffer(GL_FRAMEBUFFER, 0);
        GLES20.glViewport(0, 0, width, height);
        glClear(GL_COLOR_BUFFER_BIT);

        textureProgram.useProgram();
        displayRectangle.bindAttributes(textureProgram);
        displayRectangle.draw();
    }

}
