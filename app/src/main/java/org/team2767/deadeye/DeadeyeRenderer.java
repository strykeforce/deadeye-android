package org.team2767.deadeye;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.SurfaceTexture;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.support.v4.app.ActivityCompat;

import com.google.auto.factory.AutoFactory;
import com.google.auto.factory.Provided;

import org.team2767.deadeye.di.Injector;
import org.team2767.deadeye.opengl.CameraShaderProgram;
import org.team2767.deadeye.opengl.DisplayRectangle;
import org.team2767.deadeye.opengl.TextureHelper;
import org.team2767.deadeye.opengl.TextureShaderProgram;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import hugo.weaving.DebugLog;
import timber.log.Timber;

import static android.opengl.GLES20.GL_COLOR_BUFFER_BIT;

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
    private int targetTextureId;
    private int feedbackTextureId;

    private SurfaceTexture surfaceTexture;

    private boolean frameAvailable;

    private int width;
    private int height;

    public DeadeyeRenderer(DeadeyeView deadeyeView, @Provided DisplayRectangle displayRectangle,
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
        GLES20.glViewport(0, 0, width, height);
        this.width = width;
        this.height = height;

        targetTextureId = TextureHelper.initTexture(width, height);
        feedbackTextureId = TextureHelper.initTexture(width, height);
        cameraTextureId = TextureHelper.initImageTexture();

        cameraProgram.setTexture(cameraTextureId);

        surfaceTexture = new SurfaceTexture(cameraTextureId);
        surfaceTexture.setOnFrameAvailableListener(this);

        camera.stop();
        camera.start(surfaceTexture);

        Timber.d("width = %d height = %d", width, height);
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

        GLES20.glClear(GL_COLOR_BUFFER_BIT);
        
        cameraProgram.useProgram();
        displayRectangle.bindAttributes(cameraProgram);
        displayRectangle.draw();
    }

}
