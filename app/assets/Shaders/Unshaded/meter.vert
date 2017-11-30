uniform mat4 g_WorldViewProjectionMatrix;
attribute vec3 inPosition;
attribute vec4 inColor;
attribute vec2 inTexCoord;
attribute vec2 inTexCoord2;

varying vec4 vertColor;
varying vec2 texCoord;
varying vec2 texCoord2;

void main() {
    vertColor = inColor;
    texCoord = inTexCoord;
    texCoord2 = inTexCoord2;
    gl_Position = g_WorldViewProjectionMatrix * vec4(inPosition, 1.0);
}
