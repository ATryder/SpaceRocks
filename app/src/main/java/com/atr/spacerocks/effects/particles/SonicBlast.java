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

import com.atr.math.GMath;
import com.atr.spacerocks.control.AstCont;
import com.atr.spacerocks.effects.particles.emitter.CircleEmitterShape;
import com.atr.spacerocks.effects.particles.emitter.CircleEmitterShape.Axis;
import com.atr.spacerocks.state.GameState;
import com.atr.spacerocks.util.Options;
import com.atr.spacerocks.util.Options.Detail;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.FastMath;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.renderer.RenderManager;
import com.jme3.renderer.ViewPort;
import com.jme3.renderer.queue.RenderQueue;
import com.jme3.scene.Spatial;
import com.jme3.scene.control.AbstractControl;

/**
 *
 * @author Adam T. Ryder
 * <a href="http://1337atr.weebly.com">http://1337atr.weebly.com</a>
 */
public class SonicBlast extends AbstractControl {
    private static final ColorRGBA col1 = new ColorRGBA(0.05f, 0.75f, 1f, 1);
    private static final ColorRGBA col2 = new ColorRGBA(0, 0.25f, 1, 0.9f);
    private static final ColorRGBA col3 = new ColorRGBA(0, 0.25f, 1, 0);
    
    private static final float minRadius = 8;
    private final float maxRadius;
    
    private final SonicBlastParticle[] particles;
    private int numDead = 0;
    
    private final QuadParticleMesh mesh;
    
    private static final float shockLength = 1.35f;
    private float shockTme = 0;
    private final CircleEmitterShape emitter = new CircleEmitterShape(Vector3f.ZERO,
            minRadius, Axis.XZ);
    
    private static final float spawnRate = 1/25f;
    private float spawnTme = 0;
    private static final int particlesPerIteration = Options.getParticleDetail() == Detail.High ? 30
            : Options.getParticleDetail() == Detail.Medium ? 15 : 8;
    private static final float maxParticleLife = 0.25f;
    private static final float minParticleLife = 0.2f;
    private static final float maxSize = 15f;
    private static final float minSize = 5f;
    
    private int lastDead = particlesPerIteration;
    private int count = 0;
    
    private final GameState gameState;
    
    private final Vector3f store = new Vector3f();
    
    public SonicBlast(GameState gameState) {
        this.gameState = gameState;
        
        emitter.setOnCircumference(true);
        particles = new SonicBlastParticle[(int)Math.ceil(maxParticleLife / spawnRate)
                * particlesPerIteration];
        
        for (int i = 0; i < particles.length; i++) {
            SonicBlastParticle p;
            if (i < particlesPerIteration) {
                p = new SonicBlastParticle(
                        GMath.randomFloat(minParticleLife, maxParticleLife),
                        emitter.getPoint(store), GMath.randomFloat(minSize, maxSize),
                        (short)i);
            } else {
                p = new SonicBlastParticle(
                        maxParticleLife,
                        Vector3f.ZERO, maxSize,
                        (short)i);
                p.setAlive(false);
            }
            
            particles[i] = p;
        }
        
        float aspect = (float)gameState.getApp().getWidth() / gameState.getApp().getHeight();
        maxRadius = (GameState.FIELDHEIGHTHALF * aspect) + GameState.ASTEROID_PADDING;
        
        QuadParticles geom = new QuadParticles("SonicBlastParticles", particles);
        Material mat = new Material(gameState.getApp().getAssetManager(),
                "MatDefs/Unshaded/circle_glow.j3md");
        mat.setColor("Color", col1);
        mat.setColor("Color2", col2);
        mat.setColor("Color3", col3);
        mat.setFloat("Pos1", 0.3f);
        mat.setFloat("Pos2", 0.6f);
        mat.setFloat("X", 0.5f);
        mat.setFloat("Y", 0.5f);
        mat.setBoolean("useVertCol", true);
        
        geom.setMaterial(mat);
        geom.addControl(this);
        geom.setQueueBucket(RenderQueue.Bucket.Transparent);
        
        mesh = (QuadParticleMesh)geom.getMesh();
    }
    
    @Override
    public void controlUpdate(float tpf) {
        numDead = 0;
        for (SonicBlastParticle p : particles) {
            if (!p.isAlive() || !p.update(tpf))
                numDead++;
        }
        
        shockTme += tpf;
        spawnTme += tpf;
        if (spawnTme >= spawnRate && shockTme < shockLength) {
            numDead = 0;
            float perc = shockTme / shockLength;
            emitter.setRadius(((maxRadius - minRadius) * perc) + minRadius);
            do {
                spawnTme -= spawnRate;
            } while(spawnTme >= spawnRate);
            
            spawnParticles(tpf, spawnTme);
            
            for (Spatial s : gameState.collideNode.getChildren()) {
                AstCont ast = s.getControl(AstCont.class);
                if (ast == null
                        || s.getLocalTranslation().distance(spatial.getLocalTranslation())
                        > emitter.getRadius())
                    continue;

                ast.destroy(true);
                gameState.getTracker().blastDestroy();
            }
        }
        
        if (shockTme >= shockLength && numDead == particles.length) {
            spatial.removeFromParent();
            gameState.getTracker().finishSonicBlast();
        } else {
            mesh.updateMesh(particles);
        }
    }
    
    private void spawnParticles(float tpf, float spawnTme) {
        for (int i = 0; i < particlesPerIteration; i++) {
            count = 0;
            SonicBlastParticle particle = null;
            do {
                SonicBlastParticle p = particles[lastDead];
                lastDead = p.lastDeadParticle;
                if (!p.isAlive()) {
                    particle = p;
                    break;
                }
                count++;
            } while (count < particles.length);
            
            if (particle == null)
                break;
            
            particle.reInit(GMath.randomFloat(minParticleLife, maxParticleLife),
                    emitter.getPoint(store), GMath.randomFloat(minSize, maxSize));
            particle.tme = spawnTme - tpf;
            particle.update(tpf);
        }
    }
    
    @Override
    public void controlRender(RenderManager rm, ViewPort vp) {
        
    }
    
    private class SonicBlastParticle extends Particle {
        private final short index;
        private short lastDeadParticle;
        
        private final Vector3f prevVel = new Vector3f();
        private final Quaternion quat = new Quaternion();
        
        public SonicBlastParticle(float lifeTime, Vector3f location, float size,
                short index) {
            super(lifeTime, location, size);
            
            velocity.set(0, 0, GMath.randomFloat(45, 75));
            quat.fromAngles(GMath.randomFloat(-FastMath.PI, FastMath.PI),
                    GMath.randomFloat(-FastMath.PI, FastMath.PI),
                    GMath.randomFloat(-FastMath.PI, FastMath.PI));
            quat.mult(velocity, velocity);
            
            this.index = index;
            lastDeadParticle = index < particles.length - 1 ? (short)(index + 1) : 0;
            col.a = 0;
        }
        
        @Override
        public void reInit(float lifeTime, Vector3f location, float size) {
            super.reInit(lifeTime, location, size);
            
            velocity.set(0, 0, GMath.randomFloat(25, 45));
            quat.fromAngles(GMath.randomFloat(-FastMath.PI, FastMath.PI),
                    GMath.randomFloat(-FastMath.PI, FastMath.PI),
                    GMath.randomFloat(-FastMath.PI, FastMath.PI));
            quat.mult(velocity, velocity);
        }
        
        @Override
        public boolean update(float tpf) {
            tme += tpf;
            if (tme >= lifeTime) {
                alive = false;
                col.a = 0;
                if (lastDead != lastDeadParticle)
                    lastDeadParticle = (short)lastDead;
                lastDead = index;
                return false;
            }
            
            perc = tme / lifeTime;
            if (perc > 0.5f) {
                col.a = GMath.smoothFloat((perc - 0.5f) / 0.5f, 1, 0);
            } else if (perc < 0.2f) {
                col.a = GMath.smoothFloat(perc / 0.2f, 0, 1);
            } else
                col.a = 1;
            velocity.mult(tpf, tmp);
            loc.addLocal(tmp);
            
            return alive;
        }
    }
}
