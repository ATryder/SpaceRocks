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
package com.atr.spacerocks.control;

import com.atr.math.GMath;
import com.atr.spacerocks.SpaceRocks;
import com.atr.spacerocks.effects.particles.Particle;
import com.atr.spacerocks.effects.particles.ZFacingParticleMesh;
import com.atr.spacerocks.effects.particles.ZFacingParticles;
import com.atr.spacerocks.effects.particles.emitter.CircleEmitterShape;
import com.atr.spacerocks.effects.particles.emitter.CircleEmitterShape.Axis;
import com.jme3.asset.AssetManager;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.renderer.Camera;
import com.jme3.renderer.RenderManager;
import com.jme3.renderer.ViewPort;
import com.jme3.renderer.queue.RenderQueue;
import com.jme3.scene.Node;
import com.jme3.scene.control.AbstractControl;

/**
 *
 * @author Adam T. Ryder
 * <a href="http://1337atr.weebly.com">http://1337atr.weebly.com</a>
 */
public class MainMenuEffects extends AbstractControl {
    private static final ColorRGBA col1 = new ColorRGBA(1, 1, 1, 1);
    private static final ColorRGBA col2 = new ColorRGBA(0.123f, 0.486f, 1, 0.9f);
    private static final ColorRGBA col3 = new ColorRGBA(0, 0, 1, 0);
    
    private static final float LIFESPAN = 0.75f;
    private static final float SPAWNRATE = 1/480f;
    private static final float DISTANCE = 200;
    private static final float MINSIZE = 0.5f;
    private static final float MAXSIZE = 1.5f;
    
    private final Camera cam;
    
    private final MMParticle[] particles;
    private final CircleEmitterShape ces;
    
    private int lastDead = 0;
    private int count = 0;
    
    private float spawnTme = 0;
    
    private final Vector3f emitterPoint = new Vector3f();
    
    private final ZFacingParticleMesh mesh;
    
    private MainMenuEffects(Camera cam, AssetManager assetManager,
            Node rootNode) {
        this.cam = cam;
        
        ces = new CircleEmitterShape(Vector3f.ZERO, 35, Axis.XY);
        ces.setOnCircumference(true);
        particles = new MMParticle[(int)Math.ceil(LIFESPAN / SPAWNRATE)];
        for (int i = 0; i < particles.length; i++)
            particles[i] = new MMParticle((short)i);
        
        ZFacingParticles geom = new ZFacingParticles("MainMenuParticles", particles);
        Material mat = new Material(assetManager, "MatDefs/Unshaded/circle_glow.j3md");
        mat.setColor("Color", col1);
        mat.setColor("Color2", col2);
        mat.setColor("Color3", col3);
        mat.setFloat("Pos1", 0.3f);
        mat.setFloat("Pos2", 0.6f);
        mat.setFloat("X", 0.5f);
        mat.setFloat("Y", 0.5f);
        mat.setBoolean("useVertCol", true);
        
        geom.setMaterial(mat);
        geom.setQueueBucket(RenderQueue.Bucket.Transparent);
        rootNode.attachChild(geom);
        
        mesh = (ZFacingParticleMesh)geom.getMesh();
        
        geom.addControl(this);
    }
    
    public static void intantiateEffects(SpaceRocks app) {
        Camera cam = app.getCamera();
        cam.setFrustumPerspective(60, (float)app.getWidth() / app.getHeight(),
                1, 200);
        cam.setLocation(new Vector3f(0f, 0f, 200f));
        cam.lookAt(new Vector3f(0f, 0f, 0f), Vector3f.UNIT_Y);
        
        new MainMenuEffects(cam, app.getAssetManager(), app.getRootNode());
    }
    
    @Override
    public void controlUpdate(float tpf) {
        for (MMParticle p : particles) {
            if (p.isAlive())
                p.update(tpf);
        }
        
        spawnTme += tpf;
        while (spawnTme >= SPAWNRATE) {
            if (!spawnParticles(tpf))
                break;
        }
        
        mesh.updateMesh(particles);
    }
    
    private boolean spawnParticles(float tpf) {
        spawnTme -= SPAWNRATE;
        count = 0;
        MMParticle particle = null;
        do {
            MMParticle p = particles[lastDead];
            lastDead = p.lastDeadParticle;
            if (!p.isAlive()) {
                particle = p;
                break;
            }
            count++;
        } while (count < particles.length);
        
        if (particle == null)
            return false;
        
        ces.getPoint(emitterPoint);
        particle.reInit(emitterPoint, spawnTme - tpf);
        particle.update(tpf);
        
        return true;
    }
    
    @Override
    public void controlRender(RenderManager rm, ViewPort vp) {
        
    }
    
    private class MMParticle extends Particle {
        private final short index;
        private short lastDeadParticle;
        private final Quaternion quat = new Quaternion();
        
        private MMParticle(short index) {
            super();
            
            this.index = index;
            lastDeadParticle = index < particles.length - 1 ? (short)(index + 1) : 0;
        }
        
        private MMParticle(Vector3f location, short index) {
            super(LIFESPAN, location, GMath.randomFloat(MINSIZE, MAXSIZE));
            
            this.index = index;
            lastDeadParticle = index < particles.length - 1 ? (short)(index + 1) : 0;
        }
        
        private void reInit(Vector3f location, float time) {
            super.reInit(LIFESPAN, location, GMath.randomFloat(MINSIZE, MAXSIZE));
            tme = time;
        }
        
        @Override
        public Vector3f localToWorld(Vector3f in, Vector3f store) {
            quat.mult(in, store);
            
            return super.localToWorld(store, store);
        }
        
        @Override
        public boolean update(float tpf) {
            tme += tpf;
            if (tme >= this.lifeTime) {
                alive = false;
                col.a = 0;
                if (lastDead != lastDeadParticle)
                    lastDeadParticle = (short)lastDead;
                lastDead = index;
                return false;
            }
            
            perc = tme / this.lifeTime;
            if (perc < 0.3f) {
                col.a = GMath.smoothFloat(perc / 0.3f, 0, 1);
            } else
                col.a = 1;
            
            loc.z = DISTANCE * perc;
            
            cam.getLocation().subtract(loc, tmp);
            quat.lookAt(tmp, Vector3f.UNIT_Y);
            
            return alive;
        }
    }
}
