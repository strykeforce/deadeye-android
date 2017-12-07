package org.team2767.deadeye.opengl;

import android.content.Context;

import org.team2767.deadeye.R;

import javax.inject.Inject;

import static android.opengl.GLES20.GL_TEXTURE0;
import static android.opengl.GLES20.GL_TEXTURE_2D;
import static android.opengl.GLES20.glActiveTexture;
import static android.opengl.GLES20.glBindTexture;
import static android.opengl.GLES20.glGetAttribLocation;
import static android.opengl.GLES20.glGetUniformLocation;
import static android.opengl.GLES20.glUniform1i;

/**
 * Shader program that fills fragments with an image texture.
 */
public class ImageShaderProgram extends AbstractShaderProgram {

    // uniform locations
    private final int uTextureUnitLocation;

    // attribute locations
    private final int aPositionLocation;
    private final int aTextureCoordsLocation;

    @Inject
    public ImageShaderProgram(Context context) {
        super(context, R.raw.texture_vertex_shader, R.raw.image_fragment_shader);

        uTextureUnitLocation = glGetUniformLocation(program, U_TEXTURE_UNIT);
        aPositionLocation = glGetAttribLocation(program, A_POSITION);
        aTextureCoordsLocation = glGetAttribLocation(program, A_TEXTURE_COORDS);

    }

    public void setTexture(int textureId) {
        glActiveTexture(GL_TEXTURE0); // active texture unit is texture unit 0 for subsequent calls
        glBindTexture(GL_TEXTURE_2D, textureId); // bind our texture to active texture unit
        glUniform1i(uTextureUnitLocation, 0); // tell this sampler to use texture unit 0
    }

    public int getPositionLocation() {
        return aPositionLocation;
    }

    public int getTextureCoordsLocation() {
        return aTextureCoordsLocation;
    }

}
