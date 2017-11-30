precision mediump float;

uniform vec4 g_LightColor;
uniform vec4 g_AmbientLightColor;

uniform sampler2D m_DiffuseMap;
uniform sampler2D m_NormalMap;

varying vec2 texCoord;
varying vec4 lightDir;
varying mat3 tbn;

void main() {
    vec4 norm = texture2D(m_NormalMap, texCoord);
    vec3 n = normalize(tbn * normalize(norm.xyz * vec3(2.0,2.0,2.0) - vec3(1.0,1.0,1.0)));

    vec4 diffCol = texture2D(m_DiffuseMap, texCoord);
    float NdotL = max(0.0, dot(n, lightDir.xyz));

    vec4 color = (diffCol * g_LightColor) * NdotL * lightDir.w;
    color += (diffCol * g_AmbientLightColor) * (1.0 - NdotL);
    
    gl_FragColor = vec4(color.rgb, 1.0);
}

