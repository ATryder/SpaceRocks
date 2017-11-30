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
package com.atr.spacerocks.effects;

import com.atr.math.GMath;
import com.jme3.asset.AssetManager;
import com.jme3.material.Material;
import com.jme3.material.RenderState;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.renderer.queue.RenderQueue;
import com.jme3.scene.Geometry;
import com.jme3.scene.Mesh;
import com.jme3.scene.Spatial;
import com.jme3.scene.VertexBuffer;
import com.jme3.util.BufferUtils;
import com.simsilica.lemur.GuiGlobals;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

/**
 *
 * @author Adam T. Ryder
 * <a href="http://1337atr.weebly.com">http://1337atr.weebly.com</a>
 */
public class EngineTrail extends Geometry {
    private static final float POINT_LIFE = 1f;
    private static final float FPS = 1 / 25f;
    
    private final float engine1Local = -1.26233f;
    private final float engine2Local = 1.26233f;
    private final Vector3f midPoint = new Vector3f(0, -0.01642f, 4.26027f);
    private final Vector3f tmp = new Vector3f();
    
    private final TrailPoint[] trail = new TrailPoint[60];
    
    private float frameTme = 0;
    
    public EngineTrail(AssetManager assetManager) {
        for (int i = 0; i < trail.length; i++) {
            trail[i] = new TrailPoint(i);
            trail[i].time = POINT_LIFE;
        }
        
        mesh = new TrailMesh();
        
        Material mat = new Material(assetManager, "MatDefs/Unshaded/trail.j3md");
        mat.setBoolean("useAA", GuiGlobals.getInstance().isSupportDerivatives());
        mat.getAdditionalRenderState().setDepthWrite(false);
        mat.getAdditionalRenderState().setBlendMode(RenderState.BlendMode.AlphaAdditive);
        setMaterial(mat);
        setCullHint(Spatial.CullHint.Never);
        setQueueBucket(RenderQueue.Bucket.Transparent);
    }
    
    public void update(Spatial node, boolean isMoving, float tpf) {
        frameTme += tpf;
        
        if (isMoving) {
            if (frameTme >= FPS) {
                for (int i = trail.length - 1; i > 0; i--) {
                    trail[i].copyFrom(trail[i - 1]);
                    trail[i].time += tpf;
                    trail[i].updateColor();
                }

                frameTme = 0;
            }
            
            TrailPoint first = trail[0];
            node.localToWorld(midPoint, first.loc);
            first.rot.set(node.getLocalRotation());
            first.pRot.set(node.getParent().getLocalRotation());
            first.updateColor();
            first.time = 0;
        } else {
            for (TrailPoint tp : trail) {
                tp.time += tpf;
                tp.updateColor();
            }
        }
        
        ((TrailMesh)mesh).updateMesh();
    }
    
    public void reCenter(Vector3f center) {
        for (TrailPoint tp : trail) {
            tp.loc.subtractLocal(center);
        }
    }
    
    private class TrailPoint {
        public final Vector3f loc = new Vector3f();
        public final Quaternion rot = new Quaternion();
        public final Quaternion pRot = new Quaternion();
        public final ColorRGBA col = new ColorRGBA(0.82f, 0, 1, 0);
        public float time = 0;
        public final float trailPerc;
        public final float scale;
        
        public TrailPoint(float listPos) {
            trailPerc = listPos / (trail.length - 1);
            float t = trailPerc / 0.1f;
            scale = GMath.smoothStopFloat(t, 0.5f, 1f);
        }
        
        public void copyFrom(TrailPoint tp) {
            loc.set(tp.loc);
            rot.set(tp.rot);
            pRot.set(tp.pRot);
            col.set(tp.col);
            time = tp.time;
        }
        
        public void updateColor() {
            float lifePerc = time < POINT_LIFE ? 1 - (time / POINT_LIFE) : 0;
            col.a = lifePerc;
            
            col.g = GMath.smoothStopFloat(trailPerc / 0.2f, 0.6f, 0f);
            col.r = GMath.smoothStopFloat(trailPerc / 0.5f, 1, 0.7f);
        }
    }
    
    private class TrailMesh extends Mesh {
        private final float width = 0.41908f;
        
        private TrailMesh() {
            createMesh();
        }
        
        private void createMesh() {
            FloatBuffer verts = BufferUtils.createVector3Buffer(trail.length * 4);
            FloatBuffer tex = BufferUtils.createVector2Buffer(trail.length * 4);
            ByteBuffer col = BufferUtils.createByteBuffer(trail.length * 4 * 4);
            ShortBuffer indices = BufferUtils.createShortBuffer((trail.length - 1) * 12);
            
            Short index = 0;
            float texInd = 0;
            for (TrailPoint tp : trail) {
                float texPerc = texInd / (trail.length - 1);
                
                //left engine left
                tmp.x = engine1Local;
                tmp.y = 0;
                tmp.z = 0;
                
                tp.rot.mult(tmp, tmp);
                
                tmp.x -= width * tp.scale;
                tp.pRot.mult(tmp, tmp);
                tmp.addLocal(tp.loc);
                
                verts.put(tmp.x);
                verts.put(tmp.y);
                verts.put(tmp.z);
                tex.put(0);
                tex.put(texPerc);
                
                //left engine right
                tmp.x = engine1Local;
                tmp.y = 0;
                tmp.z = 0;
                
                tp.rot.mult(tmp, tmp);
                
                tmp.x += width * tp.scale;
                tp.pRot.mult(tmp, tmp);
                tmp.addLocal(tp.loc);
                
                verts.put(tmp.x);
                verts.put(tmp.y);
                verts.put(tmp.z);
                tex.put(1);
                tex.put(texPerc);
                
                int colInt = tp.col.asIntABGR();
                col.putInt(colInt);
                col.putInt(colInt);
                
                //right engine left
                tmp.x = engine2Local;
                tmp.y = 0;
                tmp.z = 0;
                
                tp.rot.mult(tmp, tmp);
                
                tmp.x -= width * tp.scale;
                tp.pRot.mult(tmp, tmp);
                tmp.addLocal(tp.loc);
                
                verts.put(tmp.x);
                verts.put(tmp.y);
                verts.put(tmp.z);
                tex.put(0);
                tex.put(texPerc);
                
                //right engine right
                tmp.x = engine2Local;
                tmp.y = 0;
                tmp.z = 0;
                
                tp.rot.mult(tmp, tmp);
                
                tmp.x += width * tp.scale;
                tp.pRot.mult(tmp, tmp);
                tmp.addLocal(tp.loc);
                
                verts.put(tmp.x);
                verts.put(tmp.y);
                verts.put(tmp.z);
                tex.put(1);
                tex.put(texPerc);
                
                col.putInt(colInt);
                col.putInt(colInt);
                
                if (index < (trail.length - 1) * 4) {
                    indices.put(index);
                    indices.put((short)(index + 1));
                    indices.put((short)(index + 5));
                    indices.put((short)(index + 5));
                    indices.put((short)(index + 4));
                    indices.put(index);

                    indices.put((short)(index + 2));
                    indices.put((short)(index + 3));
                    indices.put((short)(index + 7));
                    indices.put((short)(index + 7));
                    indices.put((short)(index + 6));
                    indices.put((short)(index + 2));

                    index = (short)(index + 4);
                }
                
                texInd++;
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
            vb.setupData(VertexBuffer.Usage.Static, 3, VertexBuffer.Format.UnsignedShort,
                    indices);
            setBuffer(vb);
            
            updateCounts();
        }
        
        public void updateMesh() {
            VertexBuffer vb = getBuffer(VertexBuffer.Type.Position);
            FloatBuffer verts = (FloatBuffer)vb.getData();
            verts.clear();
            
            VertexBuffer cb = getBuffer(VertexBuffer.Type.Color);
            ByteBuffer col = (ByteBuffer)cb.getData();
            col.clear();
            
            for (TrailPoint tp : trail) {
                //left engine left
                tmp.x = engine1Local;
                tmp.y = 0;
                tmp.z = 0;
                
                tp.rot.mult(tmp, tmp);
                
                tmp.x -= width * tp.scale;
                tp.pRot.mult(tmp, tmp);
                tmp.addLocal(tp.loc);
                
                verts.put(tmp.x);
                verts.put(tmp.y);
                verts.put(tmp.z);
                
                //left engine right
                tmp.x = engine1Local;
                tmp.y = 0;
                tmp.z = 0;
                
                tp.rot.mult(tmp, tmp);
                
                tmp.x += width * tp.scale;
                tp.pRot.mult(tmp, tmp);
                tmp.addLocal(tp.loc);
                
                verts.put(tmp.x);
                verts.put(tmp.y);
                verts.put(tmp.z);
                
                int colInt = tp.col.asIntABGR();
                col.putInt(colInt);
                col.putInt(colInt);
                
                //right engine left
                tmp.x = engine2Local;
                tmp.y = 0;
                tmp.z = 0;
                
                tp.rot.mult(tmp, tmp);
                
                tmp.x -= width * tp.scale;
                tp.pRot.mult(tmp, tmp);
                tmp.addLocal(tp.loc);
                
                verts.put(tmp.x);
                verts.put(tmp.y);
                verts.put(tmp.z);
                
                //right engine right
                tmp.x = engine2Local;
                tmp.y = 0;
                tmp.z = 0;
                
                tp.rot.mult(tmp, tmp);
                
                tmp.x += width * tp.scale;
                tp.pRot.mult(tmp, tmp);
                tmp.addLocal(tp.loc);
                
                verts.put(tmp.x);
                verts.put(tmp.y);
                verts.put(tmp.z);
                
                col.putInt(colInt);
                col.putInt(colInt);
            }
            
            getBuffer(VertexBuffer.Type.TexCoord).getData().clear();
            
            verts.clear();
            vb.updateData(verts);
            col.clear();
            cb.updateData(col);
        }
    }
}
