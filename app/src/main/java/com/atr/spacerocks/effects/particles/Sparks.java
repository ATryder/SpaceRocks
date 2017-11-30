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
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
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
public class Sparks extends AbstractControl {
    public static final ColorRGBA SPARK_COL1 = new ColorRGBA(1f, 0.9f, 0.6f, 1f);
    public static final ColorRGBA SPARK_COL2 = new ColorRGBA(1f, 0.749f, 0.455f, 0.9f);
    public static final ColorRGBA SPARK_COL3 = new ColorRGBA(1f, 0.749f, 0.455f, 0f);
    
    private final Spark[] particles;
    private int numDead = 0;
    
    private final QuadParticleMesh mesh;
    
    public Sparks(int numParticles, float radius, GameState gameState,
            Vector3f location) {
        this(numParticles, radius, gameState, location,
                SPARK_COL1, SPARK_COL2, SPARK_COL3);
    }
    
    public Sparks(int numParticles, float radius, GameState gameState,
            Vector3f location, ColorRGBA col1, ColorRGBA col2, ColorRGBA col3) {
        particles = new Spark[numParticles];
        SphereEmitterShape ses = new SphereEmitterShape(Vector3f.ZERO, radius);
        
        Vector3f store = new Vector3f();
        for (int i = 0; i < numParticles; i++) {
            Spark spark = new Spark(GMath.randomFloat(0.8f, 1f), ses.getPoint(store),
                    GMath.randomFloat(1f, 1.6f));
            particles[i] = spark;
        }
        
        QuadParticles geom = new QuadParticles("Sparks", particles);
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
        geom.setLocalTranslation(location);
        geom.setQueueBucket(RenderQueue.Bucket.Transparent);
        geom.addControl(this);
        
        mesh = (QuadParticleMesh)geom.getMesh();
        
        gameState.noCollideNode.attachChild(geom);
    }
    
    @Override
    public void controlUpdate(float tpf) {
        numDead = 0;
        for (Spark spark : particles) {
            if (!spark.isAlive() || !spark.update(tpf))
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
    
    private class Spark extends Particle {
        
        public Spark(float lifeTime, Vector3f location, float size) {
            super(lifeTime, location, size);
            
            velocity.set(location);
            velocity.normalizeLocal();
            velocity.multLocal(GMath.randomFloat(65, 75));
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
            
            return alive;
        }
    }
}
