precision mediump float;

uniform float m_Radius;
uniform float m_X;
uniform float m_Y;

uniform vec4 m_Color;
uniform vec4 m_Color2;
uniform vec4 m_Color3;

uniform float m_Pos1;
uniform float m_Pos2;

uniform float m_Alpha;

varying vec2 texCoord;
#ifdef VERTCOL
    varying vec4 vertCol;
#endif

void main() {
    float x = texCoord.x - m_X;
    float y = texCoord.y - m_Y;
    float r = sqrt((x*x) + (y*y));
    r = r * (1.0 / m_Radius);

    float perc = min(max(r - m_Pos1, 0.0) / (m_Pos2 - m_Pos1), 1.0);
    vec4 col = mix(m_Color, m_Color2, perc);
    
    perc = min(max(r - m_Pos2, 0.0) / (1.0 - m_Pos2), 1.0);
    col = mix(col, m_Color3, perc);
    col.a *= m_Alpha;
    #ifdef VERTCOL
        col *= vertCol;
    #endif
    
    if (col.a < 0.01) {
        discard;
    } else {
        gl_FragColor = col;
    }
}