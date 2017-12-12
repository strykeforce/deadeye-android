package org.team2767.deadeye.opengl;

import android.content.Context;
import android.support.annotation.NonNull;

import org.team2767.deadeye.R;

import javax.inject.Inject;

import static android.opengl.GLES11Ext.GL_TEXTURE_EXTERNAL_OES;
import static android.opengl.GLES20.glGetAttribLocation;

/**
 * Shader program that fills fragments with a camera image texture provided by {@code SurfaceTexture}.
 * {@code SurfaceTexture} uses the {@code GL_TEXTURE_EXTERNAL_OES} texture target so we a separate
 * shader program with that shader unit extension enabled.
 * https://developer.android.com/reference/android/graphics/SurfaceTexture.html
 */
@SuppressWarnings("unused")
public class CameraShaderProgram extends AbstractShaderProgram {

    // attribute locations
    private final int aPositionLocation;
    private final int aTextureCoordsLocation;

    @Inject
    public CameraShaderProgram(@NonNull Context context) {
        super(context, R.raw.camera_fragment_shader);

        aPositionLocation = glGetAttribLocation(program, A_POSITION);
        aTextureCoordsLocation = glGetAttribLocation(program, A_TEXTURE_COORDS);

        GLUtil.checkError();
    }

    public void setTexture(int textureId) {
        setTexture(GL_TEXTURE_EXTERNAL_OES, textureId);
        GLUtil.checkError();
    }

    @Override
    public int getPositionLocation() {
        return aPositionLocation;
    }

    @Override
    public int getTextureCoordsLocation() {
        return aTextureCoordsLocation;
    }

}
