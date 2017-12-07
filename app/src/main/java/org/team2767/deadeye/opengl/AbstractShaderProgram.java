package org.team2767.deadeye.opengl;

import android.content.Context;

import static android.opengl.GLES20.glUseProgram;

/**
 * Superclass for OpenGL shader programs.
 */
abstract class AbstractShaderProgram {
    // uniform constants
    protected static final String U_TEXTURE_UNIT = "u_TextureUnit";

    // attribute constants
    protected static final String A_POSITION = "a_Position";
    protected static final String A_COLOR = "a_Color";
    protected static final String A_TEXTURE_COORDS = "a_TextureCoordinates";

    protected final int program;

    protected AbstractShaderProgram(Context context, int vertexShaderResourceId, int fragmentShaderResourceId) {
        program = ShaderHelper.buildProgram(
                ResourceHelper.readShaderSource(context, vertexShaderResourceId),
                ResourceHelper.readShaderSource(context, fragmentShaderResourceId));
    }

    public void useProgram() {
        glUseProgram(program);
    }
}
