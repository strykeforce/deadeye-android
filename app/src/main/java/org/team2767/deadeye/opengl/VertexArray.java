package org.team2767.deadeye.opengl;

import android.support.annotation.NonNull;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import static android.opengl.GLES20.GL_FLOAT;
import static android.opengl.GLES20.glEnableVertexAttribArray;
import static android.opengl.GLES20.glVertexAttribPointer;

/**
 * Container for OpenGL per-vertex data.
 */
class VertexArray {

    public final static int FLOAT_BYTES = Float.SIZE / Byte.SIZE;

    private final FloatBuffer floatBuffer;

    public VertexArray(@NonNull float[] vertexData) {
        floatBuffer = ByteBuffer
                .allocateDirect(vertexData.length * FLOAT_BYTES)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer()
                .put(vertexData);
    }

    public void setVertexAttributePointer(int dataOffset, int attributeLocation, int componentCount, int stride) {
        floatBuffer.position(dataOffset);
        glVertexAttribPointer(attributeLocation, componentCount, GL_FLOAT, false, stride, floatBuffer);
        glEnableVertexAttribArray(attributeLocation);
        floatBuffer.position(0);
    }
}
