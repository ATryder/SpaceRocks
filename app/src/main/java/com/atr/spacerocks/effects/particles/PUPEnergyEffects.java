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
import com.atr.spacerocks.effects.particles.emitter.CircleEmitterShape;
import com.atr.spacerocks.effects.particles.emitter.CircleEmitterShape.Axis;
import com.atr.spacerocks.state.GameState;
import com.atr.spacerocks.util.Options;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.FastMath;
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
public class PUPEnergyEffects extends AbstractControl {
    private static final ColorRGBA col1 = new ColorRGBA(0.8f, 0.2f, 1f, 1);
    private static final ColorRGBA col2 = new ColorRGBA(0.6f, 0.05f, 1f, 0.9f);
    private static final ColorRGBA col3 = new ColorRGBA(0.4f, 0f, 1f, 0);
    
    private static final int numChild = 4;
    
    private final EnergyParticle[] particles;
    private int numDead = 0;
    
    private final QuadParticleMesh mesh;
    
    public PUPEnergyEffects(GameState gameState) {
        switch(Options.getParticleDetail()) {
            case Low:
                particles = new EnergyParticle[2 * (numChild + 1)];
                break;
            case Medium:
                particles = new EnergyParticle[FastMath.nextRandomInt(2, 4) * (numChild + 1)];
                break;
            default:
                particles = new EnergyParticle[FastMath.nextRandomInt(4, 7) * (numChild + 1)];
        }
        CircleEmitterShape ces = new CircleEmitterShape(Vector3f.ZERO, 3, Axis.XZ);
        ces.setOnCircumference(true);
        
        Vector3f store = new Vector3f();
        for (int i = 0; i < particles.length; i++) {
            EnergyParticle ep = new EnergyParticle(0.8f,
                    ces.getPoint(store), 2f, i);
            if (i == 0 || i % (numChild + 1) == 0) {
                ep.parent = true;
            } else {
                ep.setAlive(false);
            }
            particles[i] = ep;
        }
        
        QuadParticles geom = new QuadParticles("EnergyParticles", particles);
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
        for (EnergyParticle ep : particles) {
            if (!ep.isAlive() || !ep.update(tpf))
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
    
    private class EnergyParticle extends Particle {
        private final int index;
        private int numChildren = 0;
        public boolean parent = false;
        private final Vector3f initialLoc = new Vector3f();
        
        private static final float spawnInterval = 0.05f;
        private float spawnTime = 0;
        
        public EnergyParticle(float lifeTime, Vector3f location, float size, int index) {
            super(lifeTime, location, size);
            
            this.index = index;
            
            initialLoc.set(location);
            velocity.set(location);
            velocity.normalizeLocal();
            velocity.multLocal(125);
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
            
            velocity.mult(tpf, tmp);
            loc.addLocal(tmp);
            
            if (!parent || numChildren == numChild)
                return alive;
            
            spawnTime += tpf;
            if (spawnTime < spawnInterval)
                return alive;
            
            do {
                spawnTime -= spawnInterval;
                numChildren += 1;
                if (spawnTime < lifeTime) {
                    velocity.mult(spawnTime - tpf, tmp);
                    tmp.addLocal(initialLoc);
                    EnergyParticle p = particles[index + numChildren];
                    p.reInit(lifeTime, tmp, size);
                    p.tme = spawnTime - tpf;
                    p.velocity.set(velocity);
                }
            } while(numChildren < numChild && spawnTime >= spawnInterval);
            
            return alive;
        }
    }
}
