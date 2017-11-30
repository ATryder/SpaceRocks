precision mediump float;

uniform vec4 g_LightColor;
uniform vec4 g_AmbientLightColor;

uniform sampler2D m_DiffuseMap;
uniform sampler2D m_NormalMap;
uniform sampler2D m_SpecCol;
uniform vec4 m_CanopyColor;

uniform float m_Shininess;
uniform float m_WardFallOff;
uniform float m_WardShininess;

varying vec2 texCoord;
varying vec4 lightDir;
varying vec3 viewDir;
varying mat3 tbn;

float tangDot(in vec3 v1, in vec3 v2){
    float d = dot(v1,v2);
    return d;
}

float lightComputeWardIsoSpecular(in vec3 norm, in vec3 viewdir, in vec3 lightDir, in float shiny, in float fallOff){
    vec3 R = reflect(-normalize(lightDir), norm);
    float Iso = (1.0 / (4.0 * 3.14 * fallOff * fallOff));
    return pow(max(tangDot(R, normalize(viewdir)), 0.0), shiny) * Iso;
}

float lightComputePhongSpecular(in vec3 norm, in vec3 viewdir, in vec3 lightDir, in float shiny){
    vec3 R = reflect(-normalize(lightDir), norm);
    return pow(max(tangDot(R, normalize(viewdir)), 0.0), shiny);
}

void main() {
    vec4 norm = texture2D(m_NormalMap, texCoord);
    vec3 n = normalize(tbn * normalize(norm.xyz * vec3(2.0,2.0,2.0) - vec3(1.0,1.0,1.0)));

    vec4 diffCol = texture2D(m_DiffuseMap, texCoord);
    float NdotL = max(0.0, dot(n, lightDir.xyz));

    vec4 color = (diffCol * g_LightColor) * NdotL * lightDir.w;
    color += (diffCol * g_AmbientLightColor) * (1.0 - NdotL);
    color += diffCol.a * texture2D(m_SpecCol, texCoord) * g_LightColor * lightComputePhongSpecular(n, viewDir, lightDir.xyz, m_Shininess) * lightDir.w;

    float rim = 1.0 - max(dot(viewDir, n), 0.0);
    vec3 canopy = (m_CanopyColor.rgb * g_LightColor.rgb) * NdotL * lightDir.w;
    canopy += (g_AmbientLightColor.rgb * m_CanopyColor.rgb) * (1.0 - NdotL);
    canopy = (canopy * (pow(rim, 1.76) + 0.01)) + ((canopy * pow(rim, 4.0)) * 4.0);
    canopy += diffCol.a * g_LightColor.rgb * lightComputeWardIsoSpecular(n, viewDir, lightDir.xyz, m_WardShininess, m_WardFallOff) * lightDir.w;

    gl_FragColor = vec4(mix(color.rgb, canopy, norm.a), 1.0);
}
