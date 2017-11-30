precision mediump float;

uniform vec4 g_LightColor;
uniform vec4 g_AmbientLightColor;

uniform sampler2D m_Texture;
uniform vec4 m_Color;

uniform float m_Glow;
uniform float m_ColMult;
uniform float m_GlowMult;

varying vec2 texCoord;
varying vec4 lightDir;
varying mat3 tbn;

void main() {
    vec4 tex = texture2D(m_Texture, texCoord);
    vec3 n = normalize(tbn * normalize(tex.xyz * vec3(2.0,2.0,2.0) - vec3(1.0,1.0,1.0)));
    float NdotL = max(0.0, dot(n, lightDir.xyz));
    
    float diffMult = 1.0 - (m_ColMult * (1.0 - tex.a));
    vec4 diff = m_Color * max(tex.a + diffMult, 1.0);
    vec4 col = (diff * g_LightColor) * NdotL * lightDir.w;
    col += (diff * g_AmbientLightColor) * (1.0 - NdotL);
    
    float glowMult = 1.0 - (m_GlowMult * (1.0 - tex.a));
    col = mix(col, diff, max(tex.a + glowMult, 1.0) * m_Glow);
    
    gl_FragColor = vec4(col.rgb, 1.0);
}
