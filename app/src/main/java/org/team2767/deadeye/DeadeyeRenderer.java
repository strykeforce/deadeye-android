package org.team2767.deadeye;

import android.content.Context;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.util.Log;

import com.google.auto.factory.AutoFactory;

import org.team2767.deadeye.di.Injector;
import org.team2767.deadeye.opengl.TextureHelper;
import org.team2767.deadeye.opengl.TextureShaderProgram;
import org.team2767.deadeye.opengl.TextureSurface;

import javax.inject.Inject;
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

    private TextureSurface textureSurface;
    private TextureShaderProgram textureProgram;

    private int texture;

    public DeadeyeRenderer(DeadeyeView deadeyeView) {
        Log.d(TAG, "constructor finished");
        this.deadeyeView = deadeyeView;
    }

    @Override
    public void onSurfaceCreated(GL10 unused, EGLConfig eglConfig) {
        // set clear color
        // set 2D shader program
        // set camera shader program
        GLES20.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);

        textureSurface = new TextureSurface();

        Context context = Injector.get().appContext();
        textureProgram = new TextureShaderProgram(context);
        texture = TextureHelper.loadTexture(context, R.drawable.sf_logo_surface);

        Log.d(TAG, "onSurfaceCreated() finished");
    }

    @Override
    public void onSurfaceChanged(GL10 unused, int width, int height) {
        GLES20.glViewport(0, 0, width, height);
    }

    @Override
    public void onDrawFrame(GL10 unused) {
        GLES20.glClear(GL_COLOR_BUFFER_BIT);
        textureProgram.useProgram();
        textureProgram.setTexture(texture);
        textureSurface.bindAttributes(textureProgram);
        textureSurface.draw();
    }
}
