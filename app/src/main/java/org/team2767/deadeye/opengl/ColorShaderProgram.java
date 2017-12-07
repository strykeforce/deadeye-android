package org.team2767.deadeye.opengl;

import android.content.Context;

import org.team2767.deadeye.R;

import javax.inject.Inject;

import static android.opengl.GLES20.glGetAttribLocation;

/**
 * Shader program that colors fragments with a solid color.
 */
public class ColorShaderProgram extends AbstractShaderProgram {

    // Attribute locations
    private final int aPositionLocation;
    private final int aColorLocation;

    @Inject
    public ColorShaderProgram(Context context) {
        super(context, R.raw.color_vertex_shader, R.raw.color_fragment_shader);
        // Retrieve attribute locations for the shader program.
        aPositionLocation = glGetAttribLocation(program, A_POSITION);
        aColorLocation = glGetAttribLocation(program, A_COLOR);
    }

    public int getPositionAttributeLocation() {
        return aPositionLocation;
    }

    public int getColorAttributeLocation() {
        return aColorLocation;
    }
}
