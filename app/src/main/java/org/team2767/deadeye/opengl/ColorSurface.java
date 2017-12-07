package org.team2767.deadeye.opengl;

import static android.opengl.GLES20.GL_TRIANGLE_STRIP;
import static android.opengl.GLES20.glDrawArrays;

public class ColorSurface {

    private final static int POSITION_COMPONENT_COUNT = 2;
    private final static int COLOR_COMPONENT_COUNT = 3;

    private final static int STRIDE =
            (POSITION_COMPONENT_COUNT + COLOR_COMPONENT_COUNT) * VertexArray.FLOAT_BYTES;

    private static final float[] VERTEX_DATA = { // triangle strip
            // X, Y, R, G, B
            -0.4f, -0.4f, 1f, 1f, 1f,
            0.4f, -0.4f, 1f, 1f, 1f,
            -0.4f, 0.4f, 0.953f, 0.737f, 0.086f,
            0.4f, 0.4f, 0.953f, 0.737f, 0.086f};

    private final static int VERTEX_COUNT =
            VERTEX_DATA.length / (POSITION_COMPONENT_COUNT + COLOR_COMPONENT_COUNT);

    private final VertexArray vertexArray;

    public ColorSurface() {
        vertexArray = new VertexArray(VERTEX_DATA);
    }

    public void bindAttributes(ColorShaderProgram program) {
        vertexArray.setVertexAttribPointer(0, program.getPositionAttributeLocation(),
                POSITION_COMPONENT_COUNT, STRIDE);
        vertexArray.setVertexAttribPointer(POSITION_COMPONENT_COUNT, program.getColorAttributeLocation(),
                COLOR_COMPONENT_COUNT, STRIDE);
    }

    public void draw() {
        glDrawArrays(GL_TRIANGLE_STRIP, 0, VERTEX_COUNT);
    }

}
