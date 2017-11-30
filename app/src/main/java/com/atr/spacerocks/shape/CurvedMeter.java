/*
 * Free Public License 1.0.0
 * Permission to use, copy, modify, and/or distribute this software
 * for any purpose with or without fee is hereby granted.
 *
 * THE SOFTWARE IS PROVIDED "AS IS" AND THE AUTHOR DISCLAIMS ALL
 * WARRANTIES WITH REGARD TO THIS SOFTWARE INCLUDING ALL IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS. IN NO EVENT SHALL
 * THE AUTHOR BE LIABLE FOR ANY SPECIAL, DIRECT, INDIRECT, OR
 * CONSEQUENTIAL DAMAGES OR ANY DAMAGES WHATSOEVER RESULTING FROM
 * LOSS OF USE, DATA OR PROFITS, WHETHER IN AN ACTION OF CONTRACT,
 * NEGLIGENCE OR OTHER TORTIOUS ACTION, ARISING OUT OF OR IN
 * CONNECTION WITH THE USE OR PERFORMANCE OF THIS SOFTWARE.
 */
package com.atr.spacerocks.shape;

import com.atr.math.GMath;
import com.jme3.math.ColorRGBA;
import com.jme3.math.FastMath;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.scene.Mesh;
import com.jme3.scene.VertexBuffer;
import com.jme3.util.BufferUtils;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

/**
 *
 * @author Adam T. Ryder
 * <a href="http://1337atr.weebly.com">http://1337atr.weebly.com</a>
 */
public class CurvedMeter extends Mesh {
    private final ColorRGBA col1;
    private final ColorRGBA col2;
    private final ColorRGBA meterColor;
    
    private final float meterAngle;
    private final float meterEndAngle;
    private final float gapAngle;
    private final float gap;
    private final float meterWidth;
    private final float valueWidth;
    private final int side;
    private final float radius;
    
    private final int div;
    
    private float width = 0;
    private float height = 0;
    
    /**
     * 
     * @param angle Overall angle of the meter and value
     * @param endAngle The height of the meter's end caps
     * @param gapAngle Gap between the meter's end caps and
     * meter value
     * @param gap Gap between the meter and value
     * @param divisions Number of subdivisions for the curve
     * @param meterWidth Width of the meter
     * @param valueWidth Width of the value
     * @param scale Scale of the overall meter and value
     * @param height The height of the meter
     * @param valCol1 Bottom color of the value
     * @param valCol2 Top color of the value
     * @param meterColor Color of the meter
     */
    public CurvedMeter(float angle, float endAngle, float gapAngle, float gap,
            int divisions, float meterWidth, float valueWidth,
            float scale, float height, ColorRGBA valCol1, ColorRGBA valCol2,
            ColorRGBA meterColor) {
        meterAngle = angle * FastMath.DEG_TO_RAD;
        meterEndAngle = endAngle * FastMath.DEG_TO_RAD * Math.abs(scale);
        this.gapAngle = gapAngle * FastMath.DEG_TO_RAD * Math.abs(scale);
        this.gap = gap * scale;
        div = divisions;
        this.meterWidth = meterWidth * scale;
        this.valueWidth = valueWidth * scale;
        this.side = scale >= 0 ? 1 : -1;
        
        this.radius = (height / (2 * FastMath.sin(meterAngle / 2))) * this.side;
        col1 = valCol1;
        col2 = valCol2;
        this.meterColor = meterColor;
        
        createMesh();
    }
    
    public float getRadius() {
        return radius;
    }
    
    public float getWidth() {
        return width;
    }
    
    public float getHeight() {
        return height;
    }
    
    private void createMesh() {
        int mCol = meterColor.asIntABGR();
        ColorRGBA vCol = col1.clone();
        
        Vector3f v = new Vector3f();
        Quaternion quat = new Quaternion();
        
        FloatBuffer verts = BufferUtils.createVector3Buffer((div * 4) + 8);
        FloatBuffer tex = BufferUtils.createVector2Buffer((div * 4) + 8);
        FloatBuffer tex2 = BufferUtils.createVector2Buffer((div * 4) + 8);
        ByteBuffer col = BufferUtils.createByteBuffer(((div * 4) + 8) * 4);
        ShortBuffer indices = BufferUtils.createShortBuffer(((div - 1) * 12) + 12);
        
        //Bottom meter cap
        float angle = -meterAngle / 2;
        quat.fromAngles(0, 0, angle);
        v.set(radius, 0, 0);
        quat.mult(v, v);
        verts.put(v.x);
        verts.put(v.y);
        verts.put(v.z);
        col.putInt(mCol);
        tex.put(1);
        tex.put(-1);
        tex2.put(1);
        tex2.put(0);
        
        height = Math.abs(v.y * 2);
        
        v.set(radius - meterWidth - gap - valueWidth, 0, 0);
        quat.mult(v, v);
        verts.put(v.x);
        verts.put(v.y);
        verts.put(v.z);
        col.putInt(mCol);
        tex.put(0);
        tex.put(-1);
        tex2.put(0);
        tex2.put(0);
        
        width = Math.abs(radius - v.x);
        
        angle += meterEndAngle;
        quat.fromAngles(0, 0, angle);
        v.set(radius, 0, 0);
        quat.mult(v, v);
        verts.put(v.x);
        verts.put(v.y);
        verts.put(v.z);
        col.putInt(mCol);
        tex.put(1);
        tex.put(-1);
        tex2.put(1);
        tex2.put(meterEndAngle / meterAngle);
        
        v.set(radius - meterWidth - gap - valueWidth, 0, 0);
        quat.mult(v, v);
        verts.put(v.x);
        verts.put(v.y);
        verts.put(v.z);
        col.putInt(mCol);
        tex.put(0);
        tex.put(-1);
        tex2.put(0);
        tex2.put(meterEndAngle / meterAngle);
        
        //top meter cap
        angle = (meterAngle / 2) - meterEndAngle;
        quat.fromAngles(0, 0, angle);
        v.set(radius, 0, 0);
        quat.mult(v, v);
        verts.put(v.x);
        verts.put(v.y);
        verts.put(v.z);
        col.putInt(mCol);
        tex.put(1);
        tex.put(-1);
        tex2.put(1);
        tex2.put((meterAngle - meterEndAngle) / meterAngle);
        
        v.set(radius - meterWidth - gap - valueWidth, 0, 0);
        quat.mult(v, v);
        verts.put(v.x);
        verts.put(v.y);
        verts.put(v.z);
        col.putInt(mCol);
        tex.put(0);
        tex.put(-1);
        tex2.put(0);
        tex2.put((meterAngle - meterEndAngle) / meterAngle);
        
        angle = meterAngle / 2;
        quat.fromAngles(0, 0, angle);
        v.set(radius, 0, 0);
        quat.mult(v, v);
        verts.put(v.x);
        verts.put(v.y);
        verts.put(v.z);
        col.putInt(mCol);
        tex.put(1);
        tex.put(-1);
        tex2.put(1);
        tex2.put(1);
        
        v.set(radius - meterWidth - gap - valueWidth, 0, 0);
        quat.mult(v, v);
        verts.put(v.x);
        verts.put(v.y);
        verts.put(v.z);
        col.putInt(mCol);
        tex.put(0);
        tex.put(-1);
        tex2.put(0);
        tex2.put(1);
        
        //meter
        angle = -(meterAngle / 2) + meterEndAngle;
        float step = (meterAngle - (meterEndAngle * 2)) / (div - 1);
        float wholeAngle = meterEndAngle;
        for (int i = 0; i < div; i++) {
            quat.fromAngles(0, 0, angle);
            v.set(radius, 0, 0);
            quat.mult(v, v);
            verts.put(v.x);
            verts.put(v.y);
            verts.put(v.z);
            col.putInt(mCol);
            tex.put(1);
            tex.put(-1);
            tex2.put(1);
            tex2.put(wholeAngle / meterAngle);
            
            v.set(radius - meterWidth, 0, 0);
            quat.mult(v, v);
            verts.put(v.x);
            verts.put(v.y);
            verts.put(v.z);
            col.putInt(mCol);
            tex.put(0);
            tex.put(-1);
            tex2.put(0);
            tex2.put(wholeAngle / meterAngle);
            
            angle += step;
            wholeAngle += step;
        }
        
        //meter value
        angle = -(meterAngle / 2) + meterEndAngle + gapAngle;
        step = (meterAngle - ((meterEndAngle + gapAngle) * 2)) / (div - 1);
        for (int i = 0; i < div; i++) {
            float perc = (float)i / (div - 1);
            perc = side > 0 ? perc : 1 - perc;
            GMath.smoothRGBA(perc, vCol, col1, col2);
            int iCol = vCol.asIntABGR();
            
            quat.fromAngles(0, 0, angle);
            v.set(radius - meterWidth - gap, 0, 0);
            quat.mult(v, v);
            verts.put(v.x);
            verts.put(v.y);
            verts.put(v.z);
            col.putInt(iCol);
            tex.put(1);
            tex.put(perc);
            tex2.put(1);
            tex2.put(perc);
            
            v.set(radius - meterWidth - gap - valueWidth, 0, 0);
            quat.mult(v, v);
            verts.put(v.x);
            verts.put(v.y);
            verts.put(v.z);
            col.putInt(iCol);
            tex.put(0);
            tex.put(perc);
            tex2.put(0);
            tex2.put(perc);
            
            angle += step;
        }
        
        Short index = 8;
        indices.put((short)1);
        indices.put((short)0);
        indices.put((short)2);
        indices.put((short)2);
        indices.put((short)3);
        indices.put((short)1);

        indices.put((short)5);
        indices.put((short)4);
        indices.put((short)6);
        indices.put((short)6);
        indices.put((short)7);
        indices.put((short)5);

        for (int i = 0; i < div - 1; i++) {
            indices.put((short)(index + 1));
            indices.put(index);
            indices.put((short)(index + 2));
            indices.put((short)(index + 2));
            indices.put((short)(index + 3));
            indices.put((short)(index + 1));

            index = (short)(index + 2);
        }

        index = (short)(index + 2);

        for (int i = 0; i < div - 1; i++) {
            indices.put((short)(index + 1));
            indices.put(index);
            indices.put((short)(index + 2));
            indices.put((short)(index + 2));
            indices.put((short)(index + 3));
            indices.put((short)(index + 1));

            index = (short)(index + 2);
        }
        
        verts.flip();
        VertexBuffer vb = new VertexBuffer(VertexBuffer.Type.Position);
        vb.setupData(VertexBuffer.Usage.Static, 3, VertexBuffer.Format.Float, verts);
        setBuffer(vb);
        
        tex.flip();
        vb = new VertexBuffer(VertexBuffer.Type.TexCoord);
        vb.setupData(VertexBuffer.Usage.Static, 2, VertexBuffer.Format.Float, tex);
        setBuffer(vb);
        
        tex2.flip();
        vb = new VertexBuffer(VertexBuffer.Type.TexCoord2);
        vb.setupData(VertexBuffer.Usage.Static, 2, VertexBuffer.Format.Float, tex2);
        setBuffer(vb);
        
        col.flip();
        vb = new VertexBuffer(VertexBuffer.Type.Color);
        vb.setupData(VertexBuffer.Usage.Static, 4, VertexBuffer.Format.UnsignedByte, col);
        vb.setNormalized(true);
        setBuffer(vb);
        
        indices.flip();
        vb = new VertexBuffer(VertexBuffer.Type.Index);
        vb.setupData(VertexBuffer.Usage.Static, 3, VertexBuffer.Format.UnsignedShort,
                indices);
        setBuffer(vb);
        
        updateBound();
    }
}
