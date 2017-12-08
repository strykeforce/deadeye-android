package org.team2767.deadeye.opengl;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.RawRes;

import static android.opengl.GLES20.glUseProgram;

/**
 * Superclass for OpenGL shader programs.
 */
abstract class AbstractShaderProgram {
    // uniform constants
    static final String U_TEXTURE_UNIT = "u_TextureUnit";

    // attribute constants
    static final String A_POSITION = "a_Position";
    static final String A_TEXTURE_COORDS = "a_TextureCoordinates";

    final int program;

    AbstractShaderProgram(@NonNull Context context, @RawRes int vertexShaderResourceId,
                          @RawRes int fragmentShaderResourceId) {
        program = ShaderHelper.buildProgram(
                ResourceHelper.readShaderSource(context, vertexShaderResourceId),
                ResourceHelper.readShaderSource(context, fragmentShaderResourceId));
    }

    public void useProgram() {
        glUseProgram(program);
    }

    public abstract int getPositionLocation();

    public abstract int getTextureCoordsLocation();

}
