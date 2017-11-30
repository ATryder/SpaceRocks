precision mediump float;

uniform vec4 m_Color;

uniform float m_maxOffset;
uniform float m_minOffset;

varying vec2 texCoord;
varying float focus;

void main() {
    vec4 col = m_Color;
    
    float x = texCoord.x - 0.5;
    float y = texCoord.y - 0.5;
    float r = sqrt((x*x) + (y*y));
    
    r = max(1.0 - (r * 2.0), 0.0);
    float f = 1.0 - min(((m_maxOffset - m_minOffset) * focus) + m_minOffset, 0.999);
    col.a *= min(r/f, 1.0);
    
    if (col.a < 0.001) {
        discard;
    } else {
        gl_FragColor = col;
    }
}
