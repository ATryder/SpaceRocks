uniform mat4 g_WorldViewProjectionMatrix;
attribute vec3 inPosition;
attribute vec2 inTexCoord;
#ifdef VERTCOL
    attribute vec4 inColor;
#endif

varying vec2 texCoord;
#ifdef VERTCOL
    varying vec4 vertCol;
#endif

void main() {
    texCoord = inTexCoord;
    #ifdef VERTCOL
        vertCol = inColor;
    #endif
    gl_Position = g_WorldViewProjectionMatrix * vec4(inPosition, 1.0);
}