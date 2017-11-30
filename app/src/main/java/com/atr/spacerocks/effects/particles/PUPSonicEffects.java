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
import static com.atr.spacerocks.util.Options.Detail.Low;
import static com.atr.spacerocks.util.Options.Detail.Medium;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.renderer.RenderManager;
import com.jme3.renderer.ViewPort;
import com.jme3.scene.control.AbstractControl;

/**
 *
 * @author Adam T. Ryder
 * <a href="http://1337atr.weebly.com">http://1337atr.weebly.com</a>
 */
public class PUPSonicEffects extends AbstractControl {
    private static final ColorRGBA col1 = new ColorRGBA(0.05f, 0.45f, 1f, 1);
    private static final ColorRGBA col2 = new ColorRGBA(0, 0.25f, 1, 0.9f);
    private static final ColorRGBA col3 = new ColorRGBA(0, 0.25f, 1, 0);
    
    private final SonicParticle[] particles;
    private int numDead = 0;
    
    private final QuadParticleMesh mesh;
    
    public PUPSonicEffects(GameState gameState) {
        switch (Options.getParticleDetail()) {
            case Low:
                particles = new SonicParticle[12];
                break;
            case Medium:
                particles = new SonicParticle[23];
                break;
            default:
                particles = new SonicParticle[45];
        }
        
        CircleEmitterShape ces = new CircleEmitterShape(Vector3f.ZERO, 3, Axis.XY);
        ces.setRotation(gameState.getPlayer().spatial.getLocalRotation());
        ces.setOnCircumference(true);
        
        Vector3f store = new Vector3f();
        for (int i = 0; i < particles.length; i++) {
            SonicParticle sp = new SonicParticle(GMath.randomFloat(1.2f, 1.5f),
                    ces.getPoint(store), GMath.randomFloat(2.2f, 3f));
            particles[i] = sp;
        }
        
        QuadParticles geom = new QuadParticles("SonicParticles", particles);
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
        
        mesh = (QuadParticleMesh)geom.getMesh();
    }
    
    @Override
    public void controlUpdate(float tpf) {
        numDead = 0;
        for (SonicParticle sp : particles) {
            if (!sp.isAlive() || !sp.update(tpf))
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
    
    private class SonicParticle extends Particle {
        
        public SonicParticle(float lifeTime, Vector3f location, float size) {
            super(lifeTime, location, size);
            
            velocity.set(location);
            velocity.normalizeLocal();
            velocity.multLocal(75);
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
            
            perc = GMath.smoothStopFloat(perc, 1, 0);
            velocity.mult(tpf * perc, tmp);
            loc.addLocal(tmp);
            
            return alive;
        }
    }
}
