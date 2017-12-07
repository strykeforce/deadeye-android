package org.team2767.deadeye;

import android.content.Context;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.util.Log;

import com.google.auto.factory.AutoFactory;
import com.google.auto.factory.Provided;

import org.team2767.deadeye.di.Injector;
import org.team2767.deadeye.opengl.CameraShaderProgram;
import org.team2767.deadeye.opengl.TextureHelper;
import org.team2767.deadeye.opengl.TextureShaderProgram;
import org.team2767.deadeye.opengl.DisplayRectangle;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import static android.opengl.GLES20.GL_COLOR_BUFFER_BIT;

/**
 * Renderer.
 */
@AutoFactory
public class DeadeyeRenderer implements GLSurfaceView.Renderer {

    private final static String TAG = "DeadeyeRender";

    private final DeadeyeView deadeyeView;

    private final DisplayRectangle displayRectangle;
    private TextureShaderProgram textureProgram;
    private CameraShaderProgram cameraProgram;

    private int texture;
    private int width;
    private int height;

    public DeadeyeRenderer(DeadeyeView deadeyeView, @Provided DisplayRectangle displayRectangle) {
        Log.d(TAG, "constructor finished");
        this.deadeyeView = deadeyeView;
        this.displayRectangle = displayRectangle;
    }

    @Override
    public void onSurfaceCreated(GL10 unused, EGLConfig eglConfig) {
        GLES20.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);

        textureProgram = Injector.get().textureShaderProgram();
        cameraProgram = Injector.get().cameraShaderProgram();

        texture = TextureHelper.loadTexture(Injector.get().appContext(), R.drawable.sf_logo_surface);
        Log.d(TAG, "onSurfaceCreated() finished");
    }

    @Override
    public void onSurfaceChanged(GL10 unused, int width, int height) {
        // called after the surface is created and whenever the OpenGL ES surface size changes
        GLES20.glViewport(0, 0, width, height);
        this.width = width;
        this.height = height;
        // open camera
        // init surface texture
        Log.d(TAG, "onSurfaceChanged finished, width = " + width + " height = " + height);
    }

    @Override
    public void onDrawFrame(GL10 unused) {
        GLES20.glClear(GL_COLOR_BUFFER_BIT);
        textureProgram.useProgram();
        textureProgram.setTexture(texture);
        displayRectangle.bindAttributes(textureProgram);
        displayRectangle.draw();
    }
}
