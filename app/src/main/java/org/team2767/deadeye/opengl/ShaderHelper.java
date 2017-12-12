package org.team2767.deadeye.opengl;


import org.team2767.deadeye.BuildConfig;

import timber.log.Timber;

import static android.opengl.GLES20.GL_COMPILE_STATUS;
import static android.opengl.GLES20.GL_FRAGMENT_SHADER;
import static android.opengl.GLES20.GL_LINK_STATUS;
import static android.opengl.GLES20.GL_VALIDATE_STATUS;
import static android.opengl.GLES20.GL_VERTEX_SHADER;
import static android.opengl.GLES20.glAttachShader;
import static android.opengl.GLES20.glCompileShader;
import static android.opengl.GLES20.glCreateProgram;
import static android.opengl.GLES20.glCreateShader;
import static android.opengl.GLES20.glDeleteProgram;
import static android.opengl.GLES20.glDeleteShader;
import static android.opengl.GLES20.glGetProgramInfoLog;
import static android.opengl.GLES20.glGetProgramiv;
import static android.opengl.GLES20.glGetShaderInfoLog;
import static android.opengl.GLES20.glGetShaderiv;
import static android.opengl.GLES20.glLinkProgram;
import static android.opengl.GLES20.glShaderSource;
import static android.opengl.GLES20.glValidateProgram;


class ShaderHelper {
    /**
     * Loads and compiles a vertex shader, returning the OpenGL object ID.
     */
    private static int compileVertexShader(String shaderCode) {
        return compileShader(GL_VERTEX_SHADER, shaderCode);
    }

    /**
     * Loads and compiles a fragment shader, returning the OpenGL object ID.
     */
    private static int compileFragmentShader(String shaderCode) {
        return compileShader(GL_FRAGMENT_SHADER, shaderCode);
    }

    /**
     * Compiles a shader, returning the OpenGL object ID.
     */
    private static int compileShader(int type, String shaderCode) {
        // Create a new shader object.
        final int shaderObjectId = glCreateShader(type);

        if (shaderObjectId == 0) {
            Timber.w("Could not create new shader.");
            return 0;
        }

        // Pass in the shader source.
        glShaderSource(shaderObjectId, shaderCode);

        // Compile the shader.
        glCompileShader(shaderObjectId);

        // Get the compilation status.
        final int[] compileStatus = new int[1];
        glGetShaderiv(shaderObjectId, GL_COMPILE_STATUS, compileStatus, 0);

        if (BuildConfig.DEBUG) {
            Timber.v("Results of compiling source:\n%s\n%s", shaderCode,
                    glGetShaderInfoLog(shaderObjectId));
        }

        if (compileStatus[0] == 0) {
            glDeleteShader(shaderObjectId);
            Timber.w("Compilation of shader failed.");
            return 0;
        }

        return shaderObjectId;
    }

    /**
     * Links a vertex shader and a fragment shader together into an OpenGL
     * program. Returns the OpenGL program object ID, or 0 if linking failed.
     */
    private static int linkProgram(int vertexShaderId, int fragmentShaderId) {
        final int programObjectId = glCreateProgram();

        if (programObjectId == 0) {
            Timber.w("Could not create new program");
            return 0;
        }

        glAttachShader(programObjectId, vertexShaderId);
        glAttachShader(programObjectId, fragmentShaderId);

        glLinkProgram(programObjectId);

        // Get the link status.
        final int[] linkStatus = new int[1];
        glGetProgramiv(programObjectId, GL_LINK_STATUS,
                linkStatus, 0);

        if (BuildConfig.DEBUG) {
            Timber.v("Results of linking program: %s", glGetProgramInfoLog(programObjectId));
        }

        // Verify the link status.
        if (linkStatus[0] == 0) {
            glDeleteProgram(programObjectId);
            Timber.w("Linking of program failed.");
            return 0;
        }

        return programObjectId;
    }

    /**
     * Validates an OpenGL program. Should only be called when developing the
     * application.
     */
    private static void validateProgram(int programObjectId) {
        glValidateProgram(programObjectId);
        final int[] validateStatus = new int[1];
        glGetProgramiv(programObjectId, GL_VALIDATE_STATUS,
                validateStatus, 0);
        Timber.v("Results of validating program: %d\nLog: %s", validateStatus[0],
                glGetProgramInfoLog(programObjectId));

    }

    /**
     * Helper function that compiles the shaders, links and validates the
     * program, returning the program ID.
     */
    static int buildProgram(String vertexShaderSource, String fragmentShaderSource) {
        int program;

        int vertexShader = compileVertexShader(vertexShaderSource);
        int fragmentShader = compileFragmentShader(fragmentShaderSource);

        program = linkProgram(vertexShader, fragmentShader);

        if (BuildConfig.DEBUG) {
            validateProgram(program);
        }

        return program;
    }
}
