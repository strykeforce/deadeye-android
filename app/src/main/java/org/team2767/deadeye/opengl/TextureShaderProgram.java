package org.team2767.deadeye.opengl;

import static android.opengl.GLES20.GL_TEXTURE_2D;
import static android.opengl.GLES20.glGetAttribLocation;

import android.content.Context;
import androidx.annotation.NonNull;
import javax.inject.Inject;
import org.team2767.deadeye.R;

/** Shader program that fills fragments with a texture. */
public class TextureShaderProgram extends AbstractShaderProgram {

  // attribute locations
  private final int aPositionLocation;
  private final int aTextureCoordsLocation;

  @Inject
  public TextureShaderProgram(@NonNull Context context) {
    super(context, R.raw.texture_fragment_shader, GL_TEXTURE_2D);

    aPositionLocation = glGetAttribLocation(program, A_POSITION);
    aTextureCoordsLocation = glGetAttribLocation(program, A_TEXTURE_COORDS);

    GLUtil.checkError();
  }

  @Override
  public int getPositionLocation() {
    return aPositionLocation;
  }

  @Override
  public int getTextureCoordsLocation() {
    return aTextureCoordsLocation;
  }
}
