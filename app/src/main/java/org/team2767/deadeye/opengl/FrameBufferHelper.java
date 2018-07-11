package org.team2767.deadeye.opengl;

import timber.log.Timber;

import static android.opengl.GLES20.GL_COLOR_ATTACHMENT0;
import static android.opengl.GLES20.GL_FRAMEBUFFER;
import static android.opengl.GLES20.GL_FRAMEBUFFER_COMPLETE;
import static android.opengl.GLES20.GL_TEXTURE_2D;
import static android.opengl.GLES20.glBindFramebuffer;
import static android.opengl.GLES20.glCheckFramebufferStatus;
import static android.opengl.GLES20.glFramebufferTexture2D;
import static android.opengl.GLES20.glGenFramebuffers;

public class FrameBufferHelper {

    public static int initFrameBuffer(int textureId) {
        final int[] frameBufferIds = new int[1];
        glGenFramebuffers(1, frameBufferIds, 0);

        // subsequent calls apply to this new framebuffer
        glBindFramebuffer(GL_FRAMEBUFFER, frameBufferIds[0]);
        glFramebufferTexture2D(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, GL_TEXTURE_2D, textureId, 0);

        int status = glCheckFramebufferStatus(GL_FRAMEBUFFER);
        if (status != GL_FRAMEBUFFER_COMPLETE) {
            Timber.e("Could not generate a new OpenGL frame buffer object.");
        }

        // unbind from frame buffer
        glBindFramebuffer(GL_FRAMEBUFFER, 0);
        return frameBufferIds[0];
    }
}
