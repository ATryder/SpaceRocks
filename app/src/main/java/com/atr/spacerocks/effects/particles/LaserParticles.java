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

import com.atr.spacerocks.effects.LaserCannons.LaserBeam;
import com.jme3.math.Vector3f;
import com.jme3.scene.Mesh;
import com.jme3.scene.VertexBuffer;
import com.jme3.util.BufferUtils;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

/**
 *
 * @author Adam T. Ryder
 * <a href="http://1337atr.weebly.com">http://1337atr.weebly.com</a>
 */
public class LaserParticles extends Mesh {
    private static final float width = 2;
    private static final float length = 2;
    private static final float capLength = width;
    
    private final Vector3f tmp = new Vector3f();
    
    public LaserParticles(LaserBeam[] beams) {
        createMesh(beams);
    }
    
    public static float getLength() {
        return length + (capLength * 2);
    }
    
    private void createMesh(LaserBeam[] beams) {
        FloatBuffer verts = BufferUtils.createVector3Buffer(8 * beams.length);
        FloatBuffer tex = BufferUtils.createVector2Buffer(8 * beams.length);
        ShortBuffer indices = BufferUtils.createShortBuffer(18 * beams.length);
        
        int index = 0;
        for (LaserBeam beam : beams) {
            verts.put(width / 2);
            verts.put(0);
            verts.put(0);
            tex.put(5);
            tex.put(5);
            
            verts.put(-width / 2);
            verts.put(0);
            verts.put(0);
            tex.put(5);
            tex.put(5);
            
            verts.put(width / 2);
            verts.put(0);
            verts.put(capLength);
            tex.put(5);
            tex.put(5);
            
            verts.put(-width / 2);
            verts.put(0);
            verts.put(capLength);
            tex.put(5);
            tex.put(5);
            
            verts.put(width / 2);
            verts.put(0);
            verts.put(capLength + length);
            tex.put(5);
            tex.put(5);
            
            verts.put(-width / 2);
            verts.put(0);
            verts.put(capLength + length);
            tex.put(5);
            tex.put(5);
            
            verts.put(width / 2);
            verts.put(0);
            verts.put((capLength*2) + length);
            tex.put(5);
            tex.put(5);
            
            verts.put(-width / 2);
            verts.put(0);
            verts.put((capLength*2) + length);
            tex.put(5);
            tex.put(5);
            
            for (int i = 0; i < 3 ; i++) {
                indices.put((short)index);
                indices.put((short)(index + 1));
                indices.put((short)(index + 2));
                
                indices.put((short)(index + 1));
                indices.put((short)(index + 3));
                indices.put((short)(index + 2));
                
                index += 2;
            }
            index += 2;
        }
        
        verts.flip();
        VertexBuffer vb = new VertexBuffer(VertexBuffer.Type.Position);
        vb.setupData(VertexBuffer.Usage.Stream, 3, VertexBuffer.Format.Float, verts);
        setBuffer(vb);

        tex.flip();
        vb = new VertexBuffer(VertexBuffer.Type.TexCoord);
        vb.setupData(VertexBuffer.Usage.Stream, 2, VertexBuffer.Format.Float, tex);
        setBuffer(vb);

        indices.flip();
        vb = new VertexBuffer(VertexBuffer.Type.Index);
        vb.setupData(VertexBuffer.Usage.Static, 3, VertexBuffer.Format.UnsignedShort,
                indices);
        setBuffer(vb);
    }
    
    public void updateMesh(LaserBeam[] beams) {
        VertexBuffer vb = getBuffer(VertexBuffer.Type.Position);
        FloatBuffer verts = (FloatBuffer)vb.getData();
        verts.clear();
        
        vb = getBuffer(VertexBuffer.Type.TexCoord);
        FloatBuffer tex = (FloatBuffer)vb.getData();
        tex.clear();
        
        int index = 0;
        for (LaserBeam beam : beams) {
            if (!beam.alive) {
                tex.put(5);
                tex.put(5);
                tex.put(5);
                tex.put(5);
                tex.put(5);
                tex.put(5);
                tex.put(5);
                tex.put(5);
                tex.put(5);
                tex.put(5);
                tex.put(5);
                tex.put(5);
                tex.put(5);
                tex.put(5);
                tex.put(5);
                tex.put(5);
                
                index += 24;
                verts.position(index);
                
                continue;
            }
            
            tmp.set(width / 2, 0, 0);
            beam.rot.mult(tmp, tmp);
            tmp.addLocal(beam.loc);
            verts.put(tmp.x);
            verts.put(0);
            verts.put(tmp.z);
            tex.put(-0.5f);
            tex.put(0.5f);
            
            tmp.set(-width / 2, 0, 0);
            beam.rot.mult(tmp, tmp);
            tmp.addLocal(beam.loc);
            verts.put(tmp.x);
            verts.put(0);
            verts.put(tmp.z);
            tex.put(0.5f);
            tex.put(0.5f);
            
            tmp.set(width / 2, 0, capLength);
            beam.rot.mult(tmp, tmp);
            tmp.addLocal(beam.loc);
            verts.put(tmp.x);
            verts.put(0);
            verts.put(tmp.z);
            tex.put(-0.5f);
            tex.put(0);
            
            tmp.set(-width / 2, 0, capLength);
            beam.rot.mult(tmp, tmp);
            tmp.addLocal(beam.loc);
            verts.put(tmp.x);
            verts.put(0);
            verts.put(tmp.z);
            tex.put(0.5f);
            tex.put(0);
            
            tmp.set(width / 2, 0, capLength + length);
            beam.rot.mult(tmp, tmp);
            tmp.addLocal(beam.loc);
            verts.put(tmp.x);
            verts.put(0);
            verts.put(tmp.z);
            tex.put(-0.5f);
            tex.put(0);
            
            tmp.set(-width / 2, 0, capLength + length);
            beam.rot.mult(tmp, tmp);
            tmp.addLocal(beam.loc);
            verts.put(tmp.x);
            verts.put(0);
            verts.put(tmp.z);
            tex.put(0.5f);
            tex.put(0);
            
            tmp.set(width / 2, 0, (capLength*2) + length);
            beam.rot.mult(tmp, tmp);
            tmp.addLocal(beam.loc);
            verts.put(tmp.x);
            verts.put(0);
            verts.put(tmp.z);
            tex.put(-0.5f);
            tex.put(0.5f);
            
            tmp.set(-width / 2, 0, (capLength*2) + length);
            beam.rot.mult(tmp, tmp);
            tmp.addLocal(beam.loc);
            verts.put(tmp.x);
            verts.put(0);
            verts.put(tmp.z);
            tex.put(0.5f);
            tex.put(0.5f);
            
            index += 24;
        }
        
        verts.clear();
        vb = getBuffer(VertexBuffer.Type.Position);
        vb.updateData(verts);
        
        tex.clear();
        vb = getBuffer(VertexBuffer.Type.TexCoord);
        vb.updateData(tex);
    }
}
