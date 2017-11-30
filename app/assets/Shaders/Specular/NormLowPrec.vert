uniform mat4 g_WorldViewProjectionMatrix;
uniform mat4 g_WorldViewMatrix;
uniform mat4 g_ViewMatrix;
uniform mat3 g_NormalMatrix;
uniform vec4 g_LightPosition;
uniform mediump vec4 g_LightColor;

attribute vec3 inPosition;
attribute vec2 inTexCoord;
attribute vec3 inNormal;
attribute vec4 inTangent;

varying vec2 texCoord;
varying vec4 lightDir;
varying vec3 viewDir;
varying mat3 tbn;

void lightComputeDir(in vec3 worldPos, in vec4 color, in vec4 position, out vec4 vLightDir){
    //color.w: directional = 0, point = 1, spot = 2
    if (color.w < 0.5) {
        vec3 tempVec = -position.xyz;
        vLightDir = vec4(normalize(tempVec), 1.0);
    } else {
        vec3 tempVec = position.xyz - worldPos;
        //position.w is the inverse radius, 1/r
        vLightDir = vec4(normalize(tempVec), 1.0 - min(length(tempVec) / (1.0 / position.w), 1.0));
    }
}

void main() {
    texCoord = inTexCoord;
    gl_Position = g_WorldViewProjectionMatrix * vec4(inPosition, 1.0);
    
    vec3 wvPosition = (g_WorldViewMatrix * vec4(inPosition, 1.0)).xyz;
    
    vec4 wvLightPos = (g_ViewMatrix * vec4(g_LightPosition.xyz,clamp(g_LightColor.w,0.0,1.0)));
    wvLightPos.w = g_LightPosition.w;
    lightComputeDir(wvPosition.xyz, g_LightColor, wvLightPos, lightDir);

    viewDir = normalize(-wvPosition);
    
    vec3 t = normalize(g_NormalMatrix * inTangent.xyz);
    vec3 n = normalize(g_NormalMatrix * inNormal);
    tbn = mat3(t, cross(n, t), n);
}