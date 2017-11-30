precision mediump float;

uniform vec4 g_LightColor;
uniform vec4 g_AmbientLightColor;

uniform vec4 m_Color;

varying vec4 lightDir;
varying vec3 viewDir;
varying vec3 normal;

float tangDot(in vec3 v1, in vec3 v2){
    float d = dot(v1,v2);
    return d;
}

void main() {
    float NdotL = max(0.0, dot(normal, lightDir.xyz));

    float rim = 1.0 - max(dot(viewDir, normal), 0.0);
    vec3 col = (m_Color.rgb * g_LightColor.rgb) * NdotL * lightDir.w;
    col += (g_AmbientLightColor.rgb * m_Color.rgb) * (1.0 - NdotL);
    col = (col * (pow(rim, 1.16) + 0.03) * 2.0);

    gl_FragColor = vec4(col, 1.0);
}

