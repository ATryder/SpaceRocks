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

uniform bool m_useAA;

varying vec4 vertCol;
varying vec2 texCoord;

#ifdef USEAA
    #ifdef GL_FRAGMENT_PRECISION_HIGH
        float AA(in float x, in float sx, in float sy) {
            vec2 px = vec2(dFdx(texCoord.x), dFdx(texCoord.y));
            vec2 py = vec2(dFdy(texCoord.x), dFdy(texCoord.y));
            float fy = (((2.0*(1.0 - texCoord.y)) * sy) + ((2.0*texCoord.y) * (1.0 - sy)))*py.x - py.y;
            float fx = (((2.0*(1.0 - texCoord.x)) * sx) + ((2.0*texCoord.x) * (1.0 - sx)))*px.x - px.y;
            float sq = sqrt(fx*fx + fy*fy);
            float sd = x/sq;

            return sd + 0.5;
        }
    #endif
#endif

void main() {
    vec4 col = vertCol;
    
    #ifdef USEAA
        #ifdef GL_FRAGMENT_PRECISION_HIGH
            float sx = step(texCoord.x, 0.5);
            float sy = step(texCoord.y, 0.5);
            float x = (texCoord.x * sx) + ((1.0 - texCoord.x) * (1.0 - sx));
            col.a *= clamp(AA(x, sx, sy), 0.0, 1.0);
        #endif
    #endif
    
    if (col.a < 0.01) {
        discard;
    } else {
        gl_FragColor = col;
    }
}
