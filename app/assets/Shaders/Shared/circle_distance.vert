uniform mat4 g_WorldViewProjectionMatrix;
uniform vec3 g_CameraPosition;
uniform mat4 g_WorldMatrix;

uniform float m_minDist;
uniform float m_Focus;

attribute vec3 inPosition;
attribute vec2 inTexCoord;

varying vec2 texCoord;
varying float focus;

void main() {
    texCoord = inTexCoord;
    vec4 pos = vec4(inPosition, 1.0);
    
    float d = distance(g_CameraPosition.xyz, vec4(g_WorldMatrix * pos).xyz);
    if (d < m_minDist) {
        focus = 0.0;
    } else if (d < m_Focus) {
        focus = (d - m_minDist) / (m_Focus - m_minDist);
    } else {
        focus = 1.0;
    }
    
    gl_Position = g_WorldViewProjectionMatrix * pos;
}

