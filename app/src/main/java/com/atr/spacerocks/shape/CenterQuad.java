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

import com.jme3.scene.Mesh;
import com.jme3.scene.VertexBuffer;
import com.jme3.scene.VertexBuffer.Type;
import com.jme3.util.BufferUtils;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

/**
 *
 * @author Adam T. Ryder
 * <a href="http://1337atr.weebly.com">http://1337atr.weebly.com</a>
 */
public class CenterQuad extends Mesh {
    public CenterQuad(float width, float height) {
        createMesh(width / 2, height / 2);
    }
    
    private void createMesh(float width, float height) {
        FloatBuffer verts = BufferUtils.createVector3Buffer(4);
        FloatBuffer tex = BufferUtils.createVector2Buffer(4);
        ShortBuffer indices = BufferUtils.createShortBuffer(6);
        
        verts.put(width);
        verts.put(0);
        verts.put(-height);
        tex.put(0);
        tex.put(0);
        
        verts.put(-width);
        verts.put(0);
        verts.put(-height);
        tex.put(1);
        tex.put(0);
        
        verts.put(width);
        verts.put(0);
        verts.put(height);
        tex.put(0);
        tex.put(1);
        
        verts.put(-width);
        verts.put(0);
        verts.put(height);
        tex.put(1);
        tex.put(1);
        
        indices.put((short)0);
        indices.put((short)1);
        indices.put((short)3);
        indices.put((short)3);
        indices.put((short)2);
        indices.put((short)0);
        
        verts.flip();
        VertexBuffer vb = new VertexBuffer(Type.Position);
        vb.setupData(VertexBuffer.Usage.Static, 3, VertexBuffer.Format.Float, verts);
        setBuffer(vb);
        
        tex.flip();
        vb = new VertexBuffer(Type.TexCoord);
        vb.setupData(VertexBuffer.Usage.Static, 2, VertexBuffer.Format.Float, tex);
        setBuffer(vb);
        
        indices.flip();
        vb = new VertexBuffer(Type.Index);
        vb.setupData(VertexBuffer.Usage.Static, 3, VertexBuffer.Format.UnsignedShort, indices);
        setBuffer(vb);
        
        updateBound();
    }
}
