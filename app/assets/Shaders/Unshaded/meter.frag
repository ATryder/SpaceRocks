#ifdef USEAA
    #ifdef GL_FRAGMENT_PRECISION_HIGH
        #extension GL_OES_standard_derivatives:enable
        precision highp float;
    #else
        precision mediump float;
    #endif
#else
    precision mediump float;
#endif

uniform float m_Percent;
uniform float m_Alpha;
uniform bool m_useAA;

varying vec4 vertColor;
varying vec2 texCoord;
varying vec2 texCoord2;

#ifdef USEAA
    #ifdef GL_FRAGMENT_PRECISION_HIGH
        float AA(in float x, in float sx, in float sy) {
            vec2 px = vec2(dFdx(texCoord2.x), dFdx(texCoord2.y));
            vec2 py = vec2(dFdy(texCoord2.x), dFdy(texCoord2.y));
            float fy = (((2.0*(1.0 - texCoord2.y)) * sy) + ((2.0*texCoord2.y) * (1.0 - sy)))*py.x - py.y;
            float fx = (((2.0*(1.0 - texCoord2.x)) * sx) + ((2.0*texCoord2.x) * (1.0 - sx)))*px.x - px.y;
            float sq = sqrt(fx*fx + fy*fy);
            float sd = x/sq;

            return sd + 0.5;
        }
    #endif
#endif

void main() {
    vec4 col = vertColor;
    col.a *= step(texCoord.y, m_Percent) * m_Alpha;
    
    #ifdef USEAA
        #ifdef GL_FRAGMENT_PRECISION_HIGH
            float sx = step(texCoord2.x, 0.5);
            float sy = step(texCoord2.y, 0.5);
            float x = (texCoord2.x * sx) + ((1.0 - texCoord2.x) * (1.0 - sx));
            col.a *= clamp(AA(x, sx, sy), 0.0, 1.0);
        #endif
    #endif
    
    if (col.a < 0.01) {
        discard;
    } else {
        gl_FragColor = col;
    }
}
