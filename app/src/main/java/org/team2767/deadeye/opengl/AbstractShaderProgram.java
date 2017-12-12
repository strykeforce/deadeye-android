package org.team2767.deadeye.opengl;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.RawRes;

import org.team2767.deadeye.R;

import timber.log.Timber;

import static android.opengl.GLES20.GL_TEXTURE0;
import static android.opengl.GLES20.glActiveTexture;
import static android.opengl.GLES20.glBindTexture;
import static android.opengl.GLES20.glGetUniformLocation;
import static android.opengl.GLES20.glUniform1i;
import static android.opengl.GLES20.glUseProgram;

/**
 * Superclass for OpenGL shader programs.
 */
abstract class AbstractShaderProgram {
    // uniform constants
    private static final String U_TEXTURE_UNIT = "u_TextureUnit";

    // attribute constants
    static final String A_POSITION = "a_Position";
    static final String A_TEXTURE_COORDS = "a_TextureCoordinates";

    final int program;

    AbstractShaderProgram(@NonNull Context context,
                          @RawRes int fragmentShaderResourceId) {
        program = ShaderHelper.buildProgram(
                ResourceHelper.readShaderSource(context, R.raw.texture_vertex_shader),
                ResourceHelper.readShaderSource(context, fragmentShaderResourceId));
        GLUtil.checkError();
    }

    void setTexture(int type, int textureId) {
        glActiveTexture(GL_TEXTURE0); // active texture unit is texture unit 0 for subsequent calls
        glBindTexture(type, textureId); // bind our texture to active texture unit
        int uniformLocation = glGetUniformLocation(program, U_TEXTURE_UNIT);
        if (uniformLocation == -1) {
            Timber.e("Not an active uniform variable: %s", U_TEXTURE_UNIT);
        }

        // set up uniform
        glUseProgram(program);
        glUniform1i(uniformLocation, 0); // tell this sampler to use texture unit 0
    }

    public void useProgram() {
        glUseProgram(program);
    }

    public abstract int getPositionLocation();

    public abstract int getTextureCoordsLocation();

}
