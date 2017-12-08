package org.team2767.deadeye.opengl;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.annotation.NonNull;
import android.support.annotation.RawRes;

import timber.log.Timber;

import static android.opengl.GLES11Ext.GL_TEXTURE_EXTERNAL_OES;
import static android.opengl.GLES20.GL_CLAMP_TO_EDGE;
import static android.opengl.GLES20.GL_LINEAR;
import static android.opengl.GLES20.GL_LINEAR_MIPMAP_LINEAR;
import static android.opengl.GLES20.GL_NEAREST;
import static android.opengl.GLES20.GL_RGBA;
import static android.opengl.GLES20.GL_TEXTURE_2D;
import static android.opengl.GLES20.GL_TEXTURE_MAG_FILTER;
import static android.opengl.GLES20.GL_TEXTURE_MIN_FILTER;
import static android.opengl.GLES20.GL_TEXTURE_WRAP_S;
import static android.opengl.GLES20.GL_TEXTURE_WRAP_T;
import static android.opengl.GLES20.GL_UNSIGNED_BYTE;
import static android.opengl.GLES20.glBindTexture;
import static android.opengl.GLES20.glDeleteTextures;
import static android.opengl.GLES20.glGenTextures;
import static android.opengl.GLES20.glGenerateMipmap;
import static android.opengl.GLES20.glTexImage2D;
import static android.opengl.GLES20.glTexParameteri;
import static android.opengl.GLUtils.texImage2D;

public class TextureHelper {

    /**
     * Loads a texture from a resource ID, returning the OpenGL ID for that texture.
     * Returns 0 if the load failed.
     *
     * @param context    the application context
     * @param resourceId the resource id
     * @return a handle to the loaded texture
     */
    @SuppressWarnings("unused")
    public static int loadTexture(@NonNull Context context, @RawRes int resourceId) {
        final int[] textureObjectIds = generateTextureObjectIds();
        if (textureObjectIds == null) {
            return 0;
        }

        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inScaled = false;

        // Read in the resource
        final Bitmap bitmap = BitmapFactory.decodeResource(
                context.getResources(), resourceId, options);

        if (bitmap == null) {
            Timber.w("Resource ID %d could not be decoded.", resourceId);

            glDeleteTextures(1, textureObjectIds, 0);

            return 0;
        }

        // bind to the texture so subsequent calls will apply to this texture
        glBindTexture(GL_TEXTURE_2D, textureObjectIds[0]);

        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR_MIPMAP_LINEAR);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);

        // load the bitmap into the bound texture
        texImage2D(GL_TEXTURE_2D, 0, bitmap, 0);

        glGenerateMipmap(GL_TEXTURE_2D);

        bitmap.recycle();

        // done, unbind the texture
        glBindTexture(GL_TEXTURE_2D, 0);

        return textureObjectIds[0];
    }

    public static int initImageTexture() {
        final int[] textureObjectIds = generateTextureObjectIds();
        if (textureObjectIds == null) {
            return 0;
        }

        // bind to the texture so subsequent calls will apply to this texture
        glBindTexture(GL_TEXTURE_EXTERNAL_OES, textureObjectIds[0]);
        glTexParameteri(GL_TEXTURE_EXTERNAL_OES, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
        glTexParameteri(GL_TEXTURE_EXTERNAL_OES, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
        glTexParameteri(GL_TEXTURE_EXTERNAL_OES, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
        glTexParameteri(GL_TEXTURE_EXTERNAL_OES, GL_TEXTURE_MAG_FILTER, GL_NEAREST);

        // done, unbind the texture
        glBindTexture(GL_TEXTURE_EXTERNAL_OES, 0);

        return textureObjectIds[0];
    }

    public static int initTexture(int width, int height) {
        final int[] textureObjectIds = generateTextureObjectIds();
        if (textureObjectIds == null) {
            return 0;
        }

        // bind to the texture so subsequent calls will apply to this texture
        glBindTexture(GL_TEXTURE_2D, textureObjectIds[0]);

        // define texture, mipmap level = 0, border = 0
        glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, width, height, 0, GL_RGBA, GL_UNSIGNED_BYTE, null);

        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);

        // done, unbind the texture
        glBindTexture(GL_TEXTURE_2D, 0);

        return textureObjectIds[0];
    }

    private static int[] generateTextureObjectIds() {
        final int[] textureObjectIds = new int[1];
        glGenTextures(1, textureObjectIds, 0);

        if (textureObjectIds[0] == 0) {
            Timber.e("Could not generate a new OpenGL texture object.");
            return null;
        }
        return textureObjectIds;
    }
}
