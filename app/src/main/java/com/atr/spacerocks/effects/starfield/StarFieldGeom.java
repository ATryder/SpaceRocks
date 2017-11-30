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
package com.atr.spacerocks.effects.starfield;

import com.atr.spacerocks.effects.starfield.StarField.Star;
import com.jme3.scene.Geometry;
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
public class StarFieldGeom extends Geometry {
    protected StarFieldGeom(Star[] stars) {
        super("Star Field");
        setMesh(new StarFieldMesh(stars));
    }
    
    protected void updateStars(Star[] stars) {
        ((StarFieldMesh)mesh).updateMesh(stars);
    }
    
    private class StarFieldMesh extends Mesh {
        private int ind = 0;
        
        private StarFieldMesh(Star[] stars) {
            createMesh(stars);
        }
        
        private void createMesh(Star[] stars) {
            FloatBuffer verts = BufferUtils.createVector3Buffer(stars.length * 4);
            FloatBuffer tex = BufferUtils.createVector2Buffer(stars.length * 4);
            ShortBuffer indices = BufferUtils.createShortBuffer(stars.length * 6);
            
            short index = 0;
            for (Star star : stars) {
                float left = star.loc.x + (star.size / 2);
                float right = star.loc.x - (star.size / 2);
                float top = star.loc.z + (star.size / 2);
                float bottom = star.loc.z - (star.size / 2);
                
                //bottom left
                verts.put(left);
                verts.put(star.loc.y);
                verts.put(bottom);
                
                tex.put(0);
                tex.put(0);
                
                //bottom right
                verts.put(right);
                verts.put(star.loc.y);
                verts.put(bottom);
                
                tex.put(1);
                tex.put(0);
                
                //top right
                verts.put(right);
                verts.put(star.loc.y);
                verts.put(top);
                
                tex.put(1);
                tex.put(1);
                
                //top left
                verts.put(left);
                verts.put(star.loc.y);
                verts.put(top);
                
                tex.put(0);
                tex.put(1);
                
                indices.put(index++);
                indices.put(index++);
                indices.put(index);
                indices.put(index++);
                indices.put(index++);
                indices.put((short)(index - 4));
            }
            
            verts.flip();
            VertexBuffer vb = new VertexBuffer(Type.Position);
            vb.setupData(VertexBuffer.Usage.Stream, 3, VertexBuffer.Format.Float, verts);
            setBuffer(vb);
            
            tex.flip();
            vb = new VertexBuffer(Type.TexCoord);
            vb.setupData(VertexBuffer.Usage.Static, 2, VertexBuffer.Format.Float, tex);
            setBuffer(vb);
            
            indices.flip();
            vb = new VertexBuffer(Type.Index);
            vb.setupData(VertexBuffer.Usage.Static, 3, VertexBuffer.Format.UnsignedShort,
                    indices);
            setBuffer(vb);
            
            updateCounts();
        }
        
        private void updateMesh(Star[] stars) {
            VertexBuffer vb = getBuffer(Type.Position);
            FloatBuffer verts = (FloatBuffer)vb.getData();
            verts.clear();
            
            ind = 0;
            for (Star star : stars) {
                if (!star.updateNeeded) {
                    ind += 12;
                    verts.position(ind);
                    continue;
                }
                star.updateNeeded = false;
                
                float left = star.loc.x + (star.size / 2);
                float right = star.loc.x - (star.size / 2);
                float top = star.loc.z + (star.size / 2);
                float bottom = star.loc.z - (star.size / 2);
                
                //bottom left
                verts.put(left);
                verts.put(star.loc.y);
                verts.put(bottom);
                
                //bottom right
                verts.put(right);
                verts.put(star.loc.y);
                verts.put(bottom);
                
                //top right
                verts.put(right);
                verts.put(star.loc.y);
                verts.put(top);
                
                //top left
                verts.put(left);
                verts.put(star.loc.y);
                verts.put(top);
                
                ind += 12;
            }
            
            getBuffer(Type.TexCoord).getData().clear();
            
            verts.clear();
            vb.updateData(verts);
        }
    }
}
