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
import com.atr.spacerocks.effects.particles.emitter.SphereEmitterShape;
import com.atr.spacerocks.state.GameState;
import com.atr.spacerocks.util.Options;
import static com.atr.spacerocks.util.Options.Detail.Low;
import static com.atr.spacerocks.util.Options.Detail.Medium;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.FastMath;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.renderer.RenderManager;
import com.jme3.renderer.ViewPort;
import com.jme3.renderer.queue.RenderQueue;
import com.jme3.scene.control.AbstractControl;

/**
 *
 * @author Adam T. Ryder
 * <a href="http://1337atr.weebly.com">http://1337atr.weebly.com</a>
 */
public class PUPHealthEffects extends AbstractControl {
    private static final ColorRGBA col1 = new ColorRGBA(1, 0.35f, 0.3f, 1);
    private static final ColorRGBA col2 = new ColorRGBA(1, 0, 0.05f, 0.9f);
    private static final ColorRGBA col3 = new ColorRGBA(1, 0, 0.05f, 0);
    
    private final HealthParticle[] particles;
    private int numDead = 0;
    
    private final QuadParticleMesh mesh;
    
    public PUPHealthEffects(GameState gameState) {
        switch (Options.getParticleDetail()) {
            case Low:
                particles = new HealthParticle[5];
                break;
            case Medium:
                particles = new HealthParticle[FastMath.nextRandomInt(5, 15)];
                break;
            default:
                particles = new HealthParticle[FastMath.nextRandomInt(15, 25)];
        }
        
        SphereEmitterShape ses = new SphereEmitterShape(Vector3f.ZERO, 3);
        
        Vector3f store = new Vector3f();
        for (int i = 0; i < particles.length; i++) {
            HealthParticle hp = new HealthParticle(GMath.randomFloat(1.2f, 2f),
                    ses.getPoint(store), GMath.randomFloat(1.3f, 1.8f));
            particles[i] = hp;
        }
        
        QuadParticles geom = new QuadParticles("HealthParticles", particles);
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
        geom.setQueueBucket(RenderQueue.Bucket.Transparent);
        geom.addControl(this);
        
        mesh = (QuadParticleMesh)geom.getMesh();
    }
    
    @Override
    public void controlUpdate(float tpf) {
        numDead = 0;
        for (HealthParticle hp : particles) {
            if (!hp.isAlive() || !hp.update(tpf))
                numDead++;
        }
        
        if (numDead == particles.length) {
            spatial.removeFromParent();
        } else
            mesh.updateMesh(particles);
    }
    
    @Override
    public void controlRender(RenderManager rm, ViewPort vp) {
        
    }
    
    private class HealthParticle extends Particle {
        private final Vector3f prevVel = new Vector3f();
        private final Quaternion quat = new Quaternion();
        
        private float modTme = 0;
        private float modLength;
        
        public HealthParticle(float lifeTime, Vector3f location, float size) {
            super(lifeTime, location, size);
            
            velocity.set(0, 0, GMath.randomFloat(45, 75));
            quat.fromAngles(GMath.randomFloat(-FastMath.PI, FastMath.PI),
                    GMath.randomFloat(-FastMath.PI, FastMath.PI),
                    GMath.randomFloat(-FastMath.PI, FastMath.PI));
            quat.mult(velocity, velocity);
            
            modLength = GMath.randomFloat(0.18f, 0.36f);
        }
        
        @Override
        public boolean update(float tpf) {
            tme += tpf;
            if (tme >= lifeTime) {
                alive = false;
                col.a = 0;
                return false;
            }
            
            perc = tme / lifeTime;
            if (perc > 0.5f)
                col.a = GMath.smoothFloat((perc - 0.5f) / 0.5f, 1, 0);
            
            modTme += tpf;
            if (modTme >= modLength) {
                do {
                    modTme -= modLength;
                } while (modTme >= modLength);
                modLength = GMath.randomFloat(0.18f, 0.36f);
                prevVel.set(velocity);
                quat.fromAngles(GMath.randomFloat(-FastMath.PI, FastMath.PI),
                        GMath.randomFloat(-FastMath.PI, FastMath.PI),
                        GMath.randomFloat(-FastMath.PI, FastMath.PI));
                quat.mult(velocity, velocity);
            }
            
            perc = modTme / modLength;
            GMath.smoothVector3(perc, tmp, prevVel, velocity);
            tmp.multLocal(tpf);
            loc.addLocal(tmp);
            
            return alive;
        }
    }
}
