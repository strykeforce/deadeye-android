#version 100
#extension GL_OES_EGL_image_external : require

uniform samplerExternalOES u_TextureUnit;
varying vec2 v_TextureCoordinates;

void main() {
    gl_FragColor = texture2D(u_TextureUnit, v_TextureCoordinates);
}
