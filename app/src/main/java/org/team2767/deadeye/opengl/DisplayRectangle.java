package org.team2767.deadeye.opengl;

import javax.inject.Inject;

import static android.opengl.GLES20.GL_TRIANGLE_STRIP;
import static android.opengl.GLES20.glDrawArrays;

/**
 * OpenGL rectangle with preview texture that is displayed by {@code DeadeyeView}.
 */
public class DisplayRectangle {

    private final static int POSITION_COMPONENT_COUNT = 2;
    private final static int TEXTURE_COORDS_COMPONENT_COUNT = 2;

    private final static int STRIDE =
            (POSITION_COMPONENT_COUNT + TEXTURE_COORDS_COMPONENT_COUNT) * VertexArray.FLOAT_BYTES;

    private static final float[] VERTEX_DATA = { // triangle strip
            // X, Y, S, T
            -1f, -1f, 0f, 1f,
            1f, -1f, 1f, 1f,
            -1f, 1f, 0f, 0f,
            1f, 1f, 1f, 0f
    };

    private final static int VERTEX_COUNT =
            VERTEX_DATA.length / (POSITION_COMPONENT_COUNT + TEXTURE_COORDS_COMPONENT_COUNT);


    private final VertexArray vertexArray;

    @Inject
    public DisplayRectangle() {
        vertexArray = new VertexArray(VERTEX_DATA);
    }

    public void bindAttributes(TextureShaderProgram program) {
        vertexArray.setVertexAttribPointer(0, program.getPositionLocation(),
                POSITION_COMPONENT_COUNT, STRIDE);

        vertexArray.setVertexAttribPointer(POSITION_COMPONENT_COUNT, program.getTextureCoordsLocation(),
                TEXTURE_COORDS_COMPONENT_COUNT, STRIDE);
    }

    public void draw() {
        glDrawArrays(GL_TRIANGLE_STRIP, 0, VERTEX_COUNT);
    }

}
