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
package com.atr.spacerocks.tutorial;

import com.atr.math.GMath;
import com.atr.spacerocks.SpaceRocks;
import com.atr.spacerocks.effects.particles.Particle;
import com.atr.spacerocks.effects.particles.QuadParticleMesh;
import com.atr.spacerocks.effects.particles.QuadParticles;
import com.atr.spacerocks.effects.particles.emitter.CircleEmitterShape;
import com.atr.spacerocks.effects.particles.emitter.CircleEmitterShape.Axis;
import com.atr.spacerocks.gameobject.powerup.BlackHole;
import static com.atr.spacerocks.gameobject.powerup.BlackHole.SINGULARITY;
import com.atr.spacerocks.shape.CenterQuad;
import com.atr.spacerocks.util.Options;
import com.atr.spacerocks.util.Options.Detail;
import com.atr.spacerocks.util.Tools;
import com.jme3.material.Material;
import com.jme3.material.RenderState;
import com.jme3.math.ColorRGBA;
import com.jme3.math.FastMath;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.renderer.RenderManager;
import com.jme3.renderer.ViewPort;
import com.jme3.renderer.queue.RenderQueue;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.control.AbstractControl;

/**
 *
 * @author Adam T. Ryder
 * <a href="http://1337atr.weebly.com">http://1337atr.weebly.com</a>
 */
public class TutorialBlackHole extends AbstractControl {
    private final float spawnRate = Options.getParticleDetail() == Detail.High ? 1/25f
            : Options.getParticleDetail() == Detail.Medium ? 1/12f : 1/5f;
    private float spawnTme = 0;
    
    private final CircleEmitterShape leftEmitter;
    private final CircleEmitterShape rightEmitter;
    private final Quaternion armQuat = new Quaternion();
    private float angle = 0;
    private final float rotationPerSecond = 315 * FastMath.DEG_TO_RAD
            * (FastMath.nextRandomFloat() >= 0.5 ? 1 : -1);
    
    private static final float maxParticleLife = 0.8f;
    private static final float minParticleLife = 0.5f;
    private static final int particlesPerIteration = 5;
    
    private final BlackHoleParticle[] particles;
    private int lastDead = particlesPerIteration * 2;
    
    private final Vector3f store = new Vector3f();
    
    private int numDead = 0;
    private int count = 0;
    
    private final QuadParticleMesh mesh;
    
    private boolean active = true;
    private float fadeLength = 0.4f;
    private float fadeTme = 0;
    private Geometry eventHorizon;
    private ColorRGBA col = new ColorRGBA(1, 1, 1, 1);
    
    public TutorialBlackHole(Vector3f location, SpaceRocks app) {
        Geometry g = new Geometry("EventHorizon", new CenterQuad(SINGULARITY*2 + 8, SINGULARITY*2 + 8));
        Material mat = new Material(app.getAssetManager(),
                "MatDefs/Unshaded/circle_glow.j3md");
        mat.setColor("Color", new ColorRGBA(1f, 1f, 1f, 1f));
            mat.setColor("Color2", new ColorRGBA(1f, 1f, 1f, 1f));
            mat.setColor("Color3", new ColorRGBA(1f, 1f, 1f, 1f));
        mat.setFloat("Pos1", 0f);
        mat.setFloat("Pos2", 0.7f);
        mat.setFloat("X", 0.5f);
        mat.setFloat("Y", 0.5f);
        mat.getAdditionalRenderState().setDepthTest(false);
        mat.getAdditionalRenderState().setBlendMode(RenderState.BlendMode.Modulate);
        g.setMaterial(mat);
        g.setQueueBucket(RenderQueue.Bucket.Translucent);
        g.setCullHint(Spatial.CullHint.Dynamic);
        
        Node n = new Node("BlackHole");
        n.attachChild(g);
        n.setCullHint(Spatial.CullHint.Never);
        eventHorizon = g;
        
        leftEmitter = new CircleEmitterShape(new Vector3f(-BlackHole.effectsRadius, 0, 0),
                BlackHole.armRadius, Axis.YZ);
        rightEmitter = new CircleEmitterShape(new Vector3f(BlackHole.effectsRadius, 0, 0),
                BlackHole.armRadius, Axis.YZ);
        
        particles = new BlackHoleParticle[(int)Math.ceil(maxParticleLife / spawnRate)
                * particlesPerIteration * 2];
        for (int i = 0; i < particles.length; i++) {
            BlackHoleParticle p;
            if (i < particlesPerIteration * 2) {
                if (i % 2 == 0) {
                    leftEmitter.getPoint(store);
                } else
                    rightEmitter.getPoint(store);
                p = new BlackHoleParticle(store, (short)i);
            } else {
                p = new BlackHoleParticle((short)i);
            }
            
            particles[i] = p;
        }
        
        QuadParticles geom = new QuadParticles("BlackHoleParticles", particles);
        mat = new Material(app.getAssetManager(),
                "MatDefs/Unshaded/circle_glow.j3md");
        mat.setColor("Color", BlackHole.col1);
        mat.setColor("Color2", BlackHole.col2);
        mat.setColor("Color3", BlackHole.col3);
        mat.setFloat("Pos1", 0.3f);
        mat.setFloat("Pos2", 0.6f);
        mat.setFloat("X", 0.5f);
        mat.setFloat("Y", 0.5f);
        mat.setBoolean("useVertCol", true);
        
        geom.setMaterial(mat);
        geom.setQueueBucket(RenderQueue.Bucket.Transparent);
        n.attachChild(geom);
        
        mesh = (QuadParticleMesh)geom.getMesh();
        
        n.addControl(this);
        
        app.getRootNode().attachChild(n);
        
        //sound = SoundFX.playBlackHoleSound(blackHole.gameState, location);
    }
    
    public boolean isActive() {
        return active;
    }
    
    public void deactivate() {
        active = false;
        fadeTme = 0;
    }
    
    @Override
    public void controlUpdate(float tpf) {
        /*if (active) {
            numDead = 0;
            for (BlackHoleParticle p : particles) {
                if (!p.isAlive() || !p.update(tpf))
                    numDead++;
            }
        } else
            numDead = particles.length + 1;*/
        numDead = 0;
        for (BlackHoleParticle p : particles) {
            if (!p.isAlive() || !p.update(tpf))
                numDead++;
        }
        
        fadeTme += tpf;
        if (active) {
            if (fadeTme < fadeLength) {
                col.r = GMath.smoothFloat(fadeTme / fadeLength, 1, 0);
                col.g = col.r;
                col.b = col.g;
                eventHorizon.getMaterial().setColor("Color", col);
                eventHorizon.getMaterial().setColor("Color2", col);
            } else if (col.r > Tools.EPSILON) {
                col.set(0, 0, 0, 1);
                eventHorizon.getMaterial().setColor("Color", col);
                eventHorizon.getMaterial().setColor("Color2", col);
            }
        } else {
            if (fadeTme < fadeLength) {
                col.r = GMath.smoothFloat(fadeTme / fadeLength, 0, 1);
                col.g = col.r;
                col.b = col.g;
                eventHorizon.getMaterial().setColor("Color", col);
                eventHorizon.getMaterial().setColor("Color2", col);
            } else if (col.r < 1) {
                col.set(1, 1, 1, 1);
                eventHorizon.getMaterial().setColor("Color", col);
                eventHorizon.getMaterial().setColor("Color2", col);
            }
        }
        
        if (active) {
            spawnTme += tpf;
            angle += rotationPerSecond * tpf;
            armQuat.fromAngles(0, angle, 0);

            if (spawnTme >= spawnRate) {
                numDead = 0;
                do {
                    spawnTme -= spawnRate;
                } while(spawnTme >= spawnRate);

                spawnParticles(tpf);
            }
        }
        
        if (!active && fadeTme >= fadeLength && numDead == particles.length) {
            spatial.removeFromParent();
        } else
            mesh.updateMesh(particles);
    }
    
    private void spawnParticles(float tpf) {
        for (int i = 0; i < particlesPerIteration * 2; i++) {
            count = 0;
            BlackHoleParticle particle = null;
            do {
                BlackHoleParticle p = particles[lastDead];
                lastDead = p.lastDeadParticle;
                if (!p.isAlive()) {
                    particle = p;
                    break;
                }
                count++;
            } while (count < particles.length);
            
            if (particle == null)
                break;
            
            if (i % 2 == 0) {
                leftEmitter.getPoint(store);
            } else
                rightEmitter.getPoint(store);
            armQuat.mult(store, store);
            particle.reInit(store, spawnTme - tpf);
            particle.update(tpf);
        }
    }
    
    @Override
    public void controlRender(RenderManager rm, ViewPort vp) {
        
    }
    
    private class BlackHoleParticle extends Particle {
        private final short index;
        private short lastDeadParticle;
        private float initialSize;
        
        public BlackHoleParticle(short index) {
            super();
            this.index = index;
            lastDeadParticle = index < particles.length - 1 ? (short)(index + 1) : 0;
        }
        
        public BlackHoleParticle(Vector3f location, short index) {
            super(GMath.randomFloat(minParticleLife, maxParticleLife),
                    location, GMath.randomFloat(5f, 25f));
            
            tmp.set(location);
            
            this.index = index;
            lastDeadParticle = index < particles.length - 1 ? (short)(index + 1) : 0;
            col.a = 0;
            initialSize = size;
        }
        
        public void reInit(Vector3f location, float time) {
            super.reInit(GMath.randomFloat(minParticleLife, maxParticleLife),
                    location, GMath.randomFloat(5f, 25f));
            tmp.set(location);
            tme = time;
            col.a = 0;
            initialSize = size;
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
            if (perc < 0.5f) {
                col.a = GMath.smoothFloat(perc / 0.5f, 0, 1);
            } else
                col.a = 1;
            
            GMath.smoothStartVector3(perc, loc, tmp, Vector3f.ZERO);
            size = GMath.smoothStartFloat(perc, initialSize, initialSize * 0.3f);
            
            return alive;
        }
    }
}
