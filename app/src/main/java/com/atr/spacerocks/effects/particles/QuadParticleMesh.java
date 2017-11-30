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
package com.atr.spacerocks.effects.particles;

import com.jme3.math.Vector3f;
import com.jme3.scene.Mesh;
import com.jme3.scene.VertexBuffer;
import com.jme3.scene.VertexBuffer.Type;
import com.jme3.util.BufferUtils;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

/**
 *
 * @author Adam T. Ryder
 * <a href="http://1337atr.weebly.com">http://1337atr.weebly.com</a>
 */
public class QuadParticleMesh extends Mesh {
    private final Vector3f tmp = new Vector3f();
    private int ind = 0;
    private int ind2 = 0;
    
    public QuadParticleMesh(Particle[] particles) {
        createMesh(particles);
    }
    
    private void createMesh(Particle[] particles) {
        FloatBuffer verts = BufferUtils.createVector3Buffer(particles.length * 4);
        FloatBuffer tex = BufferUtils.createVector2Buffer(particles.length * 4);
        ByteBuffer col = BufferUtils.createByteBuffer(particles.length * 4 * 4);
        ShortBuffer indices = BufferUtils.createShortBuffer(particles.length * 6);
        
        short index = 0;
        for (Particle p : particles) {
            if (!p.isAlive())
                p.finalized = true;
            int iCol = p.col.asIntABGR();
            tmp.set(p.halfSize, 0, -p.halfSize);
            p.localToWorld(tmp, tmp);
            verts.put(tmp.x);
            verts.put(tmp.y);
            verts.put(tmp.z);
            tex.put(0);
            tex.put(0);
            col.putInt(iCol);
            
            tmp.set(-p.halfSize, 0, -p.halfSize);
            p.localToWorld(tmp, tmp);
            verts.put(tmp.x);
            verts.put(tmp.y);
            verts.put(tmp.z);
            tex.put(1);
            tex.put(0);
            col.putInt(iCol);
            
            tmp.set(p.halfSize, 0, p.halfSize);
            p.localToWorld(tmp, tmp);
            verts.put(tmp.x);
            verts.put(tmp.y);
            verts.put(tmp.z);
            tex.put(0);
            tex.put(1);
            col.putInt(iCol);
            
            tmp.set(-p.halfSize, 0, p.halfSize);
            p.localToWorld(tmp, tmp);
            verts.put(tmp.x);
            verts.put(tmp.y);
            verts.put(tmp.z);
            tex.put(1);
            tex.put(1);
            col.putInt(iCol);
            
            indices.put(index++);
            indices.put(index++);
            indices.put(index);
            indices.put(index++);
            indices.put((short)(index - 2));
            indices.put(index++);
        }
        
        verts.flip();
        VertexBuffer vb = new VertexBuffer(Type.Position);
        vb.setupData(VertexBuffer.Usage.Stream, 3, VertexBuffer.Format.Float, verts);
        setBuffer(vb);
        
        tex.flip();
        vb = new VertexBuffer(Type.TexCoord);
        vb.setupData(VertexBuffer.Usage.Static, 2, VertexBuffer.Format.Float, tex);
        setBuffer(vb);
        
        col.flip();
        vb = new VertexBuffer(Type.Color);
        vb.setupData(VertexBuffer.Usage.Stream, 4, VertexBuffer.Format.UnsignedByte, col);
        vb.setNormalized(true);
        setBuffer(vb);
        
        indices.flip();
        vb = new VertexBuffer(Type.Index);
        vb.setupData(VertexBuffer.Usage.Static, 3, VertexBuffer.Format.UnsignedShort, indices);
        setBuffer(vb);
    }
    
    public void updateMesh(Particle[] particles) {
        VertexBuffer vb = getBuffer(Type.Position);
        FloatBuffer verts = (FloatBuffer)vb.getData();
        verts.clear();
        
        vb = getBuffer(Type.Color);
        ByteBuffer col = (ByteBuffer)vb.getData();
        col.clear();
        
        ind = 0;
        ind2 = 0;
        for (Particle p : particles) {
            int iCol = p.col.asIntABGR();
            if (!p.isAlive()) {
                ind += 12;
                ind2 += 16;
                if (!p.finalized) {
                    col.putInt(iCol);
                    col.putInt(iCol);
                    col.putInt(iCol);
                    col.putInt(iCol);
                    p.finalized = true;
                } else
                    col.position(ind2);
                verts.position(ind);
                continue;
            }
            
            tmp.set(p.halfSize, 0, -p.halfSize);
            p.localToWorld(tmp, tmp);
            verts.put(tmp.x);
            verts.put(tmp.y);
            verts.put(tmp.z);
            col.putInt(iCol);
            
            tmp.set(-p.halfSize, 0, -p.halfSize);
            p.localToWorld(tmp, tmp);
            verts.put(tmp.x);
            verts.put(tmp.y);
            verts.put(tmp.z);
            col.putInt(iCol);
            
            tmp.set(p.halfSize, 0, p.halfSize);
            p.localToWorld(tmp, tmp);
            verts.put(tmp.x);
            verts.put(tmp.y);
            verts.put(tmp.z);
            col.putInt(iCol);
            
            tmp.set(-p.halfSize, 0, p.halfSize);
            p.localToWorld(tmp, tmp);
            verts.put(tmp.x);
            verts.put(tmp.y);
            verts.put(tmp.z);
            col.putInt(iCol);
            
            ind += 12;
            ind2 += 16;
        }
        
        getBuffer(Type.TexCoord).getData().clear();
        
        verts.clear();
        getBuffer(Type.Position).updateData(verts);
        
        col.clear();
        getBuffer(Type.Color).updateData(col);
    }
}
