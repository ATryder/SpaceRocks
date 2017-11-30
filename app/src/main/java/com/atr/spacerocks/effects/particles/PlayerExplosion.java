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
import com.atr.spacerocks.sound.SoundFX;
import com.atr.spacerocks.state.GameState;
import com.atr.spacerocks.ui.UI;
import com.atr.spacerocks.util.Options;
import com.atr.spacerocks.util.Options.Detail;
import com.atr.spacerocks.util.TopGuns;
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
public class PlayerExplosion extends AbstractControl {
    private static final float particleLife = 1f;
    private static final int numArms = 7;
    private static final float spawnRate = 0.05f;
    private final int particlesPerArm = Options.getParticleDetail() == Detail.High ? 42
            : Options.getParticleDetail() == Detail.Medium ? 21 : 12;
    
    private float spawnTme = 0;
    private int spawned = 1;
    
    private final ExpParticle[] particles;
    private int lastDead = numArms;
    private int numDead = 0;
    
    private final Quaternion quat = new Quaternion();
    private float angle = 0;
    private float rotationRate = 90 * FastMath.DEG_TO_RAD;
    private float armAngle = 0;
    private final Vector3f emitLoc = new Vector3f(0, 0, 3);
    private final Vector3f spawnLoc = new Vector3f();
    
    private final QuadParticleMesh mesh;
    
    private final GameState gameState;
    
    private int count = 0;
    
    private final ColorRGBA[] colors = {new ColorRGBA(1, 0.35f, 0.05f, 1),
                                        new ColorRGBA(0.05f, 1f, 0.35f, 1),
                                        new ColorRGBA(1, 1, 0.05f, 1),
                                        new ColorRGBA(0.05f, 0.37f, 1, 1)};
    private int spawnCol = 0;
    private int spawnColCount = 0;
    
    public PlayerExplosion(Vector3f location, GameState gameState) {
        this.gameState = gameState;
        particles = new ExpParticle[(int)Math.ceil(particleLife / spawnRate) * numArms];
        for (int i = 0; i < particles.length; i++) {
            ExpParticle p;
            if (i < numArms) {
                quat.fromAngles(0, armAngle, 0);
                quat.mult(emitLoc, spawnLoc);
                p = new ExpParticle(spawnLoc, (short)i);
                armAngle += (FastMath.PI * 2) / numArms;
            } else
                p = new ExpParticle((short)i);
            
            particles[i] = p;
        }
        armAngle = 0;
        
        QuadParticles geom = new QuadParticles("PlayerExplosion", particles);
        Material mat = new Material(gameState.getApp().getAssetManager(),
                "MatDefs/Unshaded/circle_glow.j3md");
        mat.setColor("Color", new ColorRGBA(2f, 2f, 2f, 1));
        mat.setColor("Color2", new ColorRGBA(1, 1, 1, 0.9f));
        mat.setColor("Color3", new ColorRGBA(1, 1, 1, 0));
        mat.setFloat("Pos1", 0.3f);
        mat.setFloat("Pos2", 0.6f);
        mat.setFloat("X", 0.5f);
        mat.setFloat("Y", 0.5f);
        mat.setBoolean("useVertCol", true);
        
        geom.setMaterial(mat);
        geom.setQueueBucket(RenderQueue.Bucket.Transparent);
        geom.addControl(this);
        geom.setLocalTranslation(location);
        
        mesh = (QuadParticleMesh)geom.getMesh();
        
        gameState.noCollideNode.attachChild(geom);
        
        SoundFX.playSound(gameState, "Sound/PlayerExplosion.wav", location, true,
                Options.getSFXVolume() * 6f);
    }
    
    @Override
    public void controlUpdate(float tpf) {
        numDead = 0;
        for (ExpParticle p : particles) {
            if (!p.isAlive() || !p.update(tpf))
                numDead++;
        }
        
        spawnTme += tpf;
        angle += rotationRate * tpf;
        spawnLoop: while (spawnTme >= spawnRate && spawned < particlesPerArm) {
            spawnColCount++;
            if (spawnColCount == 3) {
                spawnColCount = 0;
                spawnCol++;
                if (spawnCol >= colors.length)
                    spawnCol = 0;
            }
            spawnTme -= spawnRate;
            spawned++;
            for (int i = 0; i < numArms; i++) {
                ExpParticle particle = null;
                count = 0;
                do {
                    ExpParticle p = particles[lastDead];
                    lastDead = p.lastDeadParticle;
                    if (!p.isAlive()) {
                        particle = p;
                        break;
                    }
                    count++;
                } while(count < particles.length);

                if (particle == null)
                    break spawnLoop;
                
                armAngle = (((FastMath.PI * 2) / numArms) * i) + angle;
                quat.fromAngles(0, armAngle, 0);
                quat.mult(emitLoc, spawnLoc);
                particle.reInit(spawnLoc, spawnTme - tpf, colors[spawnCol]);
                particle.update(tpf);
            }
        }
        
        if (numDead == particles.length && spawned == particlesPerArm) {
            spatial.removeFromParent();
            int topGun = TopGuns.isNewTopGun(gameState.getTracker().getDestroyedAsteroids());
            if (topGun >= 0) {
                UI.displayNewTopGun(gameState.getTracker(), topGun);
            } else
                UI.displayEndGame(gameState.getTracker());
        } else
            mesh.updateMesh(particles);
    }
    
    @Override
    public void controlRender(RenderManager rm, ViewPort vp) {
        
    }
    
    private class ExpParticle extends Particle {
        private final short index;
        private short lastDeadParticle;
        
        public ExpParticle(short index) {
            super();
            
            this.index = index;
            lastDeadParticle = index < particles.length - 1 ? (short)(index + 1) : 0;
        }
        
        public ExpParticle(Vector3f location, short index) {
            super(particleLife, location, 2.2f);
            
            col.set(colors[0]);
            velocity.set(location);
            velocity.normalizeLocal();
            velocity.multLocal(105);
            
            this.index = index;
            lastDeadParticle = index < particles.length - 1 ? (short)(index + 1) : 0;
        }
        
        public void reInit(Vector3f location, float time, ColorRGBA color) {
            super.reInit(particleLife, location, 2.2f);
            col.set(color);
            this.tme = time;
            velocity.set(location);
            velocity.normalizeLocal();
            velocity.multLocal(105);
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
            if (perc > 0.5f)
                col.a = GMath.smoothFloat((perc - 0.5f) / 0.5f, 1, 0);
            velocity.mult(tpf, tmp);
            loc.addLocal(tmp);
            
            return alive;
        }
    }
}
