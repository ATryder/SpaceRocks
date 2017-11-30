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

import com.jme3.math.ColorRGBA;
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
public class Arrow extends Mesh {
    public Arrow(float baseWidth, float triWidth, float baseHeight, float triHeight,
            ColorRGBA col1, ColorRGBA col2, boolean faceZ) {
        createMesh(baseWidth, triWidth, baseHeight, triHeight, col1, col2,
                faceZ);
    }
    
    private void createMesh(float baseWidth, float triWidth, float baseHeight,
            float triHeight, ColorRGBA col1, ColorRGBA col2, boolean faceZ) {
        float bWidth = baseWidth / 2;
        float tWidth = triWidth / 2;
        float totalHeight = baseHeight + triHeight;
        ColorRGBA tmpCol = new ColorRGBA(col1);
        
        FloatBuffer verts = BufferUtils.createVector3Buffer(7);
        FloatBuffer tex = BufferUtils.createVector2Buffer(7);
        ByteBuffer col = BufferUtils.createByteBuffer(28);
        ShortBuffer indices = BufferUtils.createShortBuffer(9);
        
        verts.put(bWidth);
        verts.put(0);
        verts.put(0);
        tex.put(0);
        tex.put(0);
        col.putInt(tmpCol.asIntABGR());
        
        verts.put(-bWidth);
        verts.put(0);
        verts.put(0);
        tex.put(1);
        tex.put(0);
        col.putInt(tmpCol.asIntABGR());
        
        tmpCol.interpolateLocal(col1, col2, baseHeight / totalHeight);
        verts.put(bWidth);
        verts.put(faceZ ? baseHeight : 0);
        verts.put(faceZ ? 0 : baseHeight);
        tex.put(0);
        tex.put(baseHeight / totalHeight);
        col.putInt(tmpCol.asIntABGR());
        
        verts.put(-bWidth);
        verts.put(faceZ ? baseHeight : 0);
        verts.put(faceZ ? 0 : baseHeight);
        tex.put(1);
        tex.put(baseHeight / totalHeight);
        col.putInt(tmpCol.asIntABGR());
        
        verts.put(tWidth);
        verts.put(faceZ ? baseHeight : 0);
        verts.put(faceZ ? 0 : baseHeight);
        tex.put(0);
        tex.put(baseHeight / totalHeight);
        col.putInt(tmpCol.asIntABGR());
        
        verts.put(-tWidth);
        verts.put(faceZ ? baseHeight : 0);
        verts.put(faceZ ? 0 : baseHeight);
        tex.put(1);
        tex.put(baseHeight / totalHeight);
        col.putInt(tmpCol.asIntABGR());
        
        verts.put(0);
        verts.put(faceZ ? totalHeight : 0);
        verts.put(faceZ ? 0 : totalHeight);
        tex.put(0.5f);
        tex.put(1);
        col.putInt(col2.asIntABGR());
        
        if (!faceZ) {
            indices.put((short)0);
            indices.put((short)1);
            indices.put((short)3);
            indices.put((short)3);
            indices.put((short)2);
            indices.put((short)0);
            indices.put((short)4);
            indices.put((short)5);
            indices.put((short)6);
        } else {
            indices.put((short)3);
            indices.put((short)1);
            indices.put((short)0);
            indices.put((short)0);
            indices.put((short)2);
            indices.put((short)3);
            indices.put((short)6);
            indices.put((short)5);
            indices.put((short)4);
        }
        
        verts.flip();
        VertexBuffer vb = new VertexBuffer(VertexBuffer.Type.Position);
        vb.setupData(VertexBuffer.Usage.Stream, 3, VertexBuffer.Format.Float, verts);
        setBuffer(vb);
        
        tex.flip();
        vb = new VertexBuffer(VertexBuffer.Type.TexCoord);
        vb.setupData(VertexBuffer.Usage.Static, 2, VertexBuffer.Format.Float, tex);
        setBuffer(vb);
        
        col.flip();
        vb = new VertexBuffer(VertexBuffer.Type.Color);
        vb.setupData(VertexBuffer.Usage.Stream, 4, VertexBuffer.Format.UnsignedByte, col);
        vb.setNormalized(true);
        setBuffer(vb);
        
        indices.flip();
        vb = new VertexBuffer(VertexBuffer.Type.Index);
        vb.setupData(VertexBuffer.Usage.Static, 3, VertexBuffer.Format.UnsignedShort, indices);
        setBuffer(vb);
        
        updateBound();
    }
}
