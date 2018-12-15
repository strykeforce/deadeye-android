package org.team2767.deadeye.opengl;

import static android.opengl.GLES20.glGetError;

import timber.log.Timber;

class GLUtil {

  static void checkError() {
    int error = glGetError();
    if (error != 0) {
      StackTraceElement[] stackTraceElements = Thread.currentThread().getStackTrace();
      if (stackTraceElements.length < 4) {
        return;
      }
      StackTraceElement stackTraceElement = stackTraceElements[3];
      Timber.e(
          "%s.%s: GL error: 0x%x",
          stackTraceElement.getClassName(), stackTraceElement.getMethodName(), error);
    }
  }
}
