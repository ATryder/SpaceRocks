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
import com.atr.spacerocks.effects.Flash;
import com.atr.spacerocks.effects.particles.Sparks;
import com.atr.spacerocks.gameobject.powerup.BlackHole;
import com.atr.spacerocks.gameobject.powerup.PUP;
import com.atr.spacerocks.gameobject.powerup.PUP.PUPType;
import com.atr.spacerocks.sound.SoundFX;
import com.atr.spacerocks.state.GameState;
import com.atr.spacerocks.util.Asteroid;
import com.atr.spacerocks.util.Options;
import com.atr.spacerocks.util.Options.Detail;
import com.atr.spacerocks.util.Tracker;
import com.jme3.math.FastMath;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.scene.Node;
import org.dyn4j.dynamics.Body;

/**
 *
 * @author Adam T. Ryder
 * <a href="http://1337atr.weebly.com">http://1337atr.weebly.com</a>
 */
public class AstCont extends BoundedBodyCont {
    private PUP pup;
    
    private Node pupCrystal;
    
    public AstCont(Body body, float maxX, float maxZ,
            GameState gameState) {
        super(body, maxX, maxZ, gameState);
    }
    
    public void setPUP(PUPType pupType) {
        pup = PUP.createPUP(pupType, state);
        
        if (pupType == PUPType.HEALTH) {
            pupCrystal = state.getApp().blueAsteroidPup.clone(false);
        } else
            pupCrystal = state.getApp().redAsteroidPup.clone(false);
        
        state.noCollideNode.attachChild(pupCrystal);
        pupCrystal.setLocalScale(spatial.getLocalScale());
        pupCrystal.setLocalTranslation(spatial.getLocalTranslation());
        pupCrystal.setLocalRotation(spatial.getLocalRotation());
    }
    
    public PUP getPUP() {
        return pup;
    }
    
    @Override
    public void controlUpdate(float tpf) {
        super.controlUpdate(tpf);
        
        if (pupCrystal != null) {
            pupCrystal.setLocalTranslation(spatial.getLocalTranslation());
            pupCrystal.setLocalRotation(spatial.getLocalRotation());
        }
    }
    
    @Override
    public void destroy(boolean effects) {
        state.removeSimulatedBody(this);
        
        if (pupCrystal != null) {
            pupCrystal.removeFromParent();
        }
        
        if (!effects)
            return;
        
        state.getTracker().destroyedAsteroid();
        
        if (pup != null) {
            if (pup.type != PUPType.BLACKHOLE) {
                new PUPCont(pup, maxX, maxZ, state, spatial.getLocalTranslation(),
                    body.getLinearVelocity());
            } else
                new BlackHoleCont((BlackHole)pup, spatial.getLocalTranslation());
        }
        
        Flash flash = (pup == null) ? new Flash(state)
                : new Flash(state, pup.getCol1(), pup.getCol2(), pup.getCol3());
        flash.setLocalTranslation(spatial.getLocalTranslation());
        flash.setLocalScale(spatial.getLocalScale().x * 2);
        state.noCollideNode.attachChild(flash);
        
        if (Options.getParticleDetail() != Detail.Low) {
            if (spatial.getLocalScale().x >= Asteroid.minSplit) {
                if (Options.getParticleDetail() == Detail.High) {
                    if (pup == null) {
                        new Sparks(FastMath.nextRandomInt(5, 10), 3 * spatial.getLocalScale().x, state,
                            spatial.getLocalTranslation());
                    } else {
                        new Sparks(FastMath.nextRandomInt(10, 15), 3 * spatial.getLocalScale().x, state,
                            spatial.getLocalTranslation(), pup.getCol1(), pup.getCol2(),
                            pup.getCol3());
                    }
                } else {
                    if (pup == null) {
                        new Sparks(3, 3 * spatial.getLocalScale().x, state,
                            spatial.getLocalTranslation());
                    } else {
                        new Sparks(3, 3 * spatial.getLocalScale().x, state,
                            spatial.getLocalTranslation(), pup.getCol1(), pup.getCol2(),
                            pup.getCol3());
                    }
                }
            } else if (spatial.getLocalScale().x >= Asteroid.minScale) {
                if (Options.getParticleDetail() == Detail.High) {
                    if (pup == null) {
                        new Sparks(3, 3 * spatial.getLocalScale().x, state,
                            spatial.getLocalTranslation());
                    } else {
                        new Sparks(3, 3 * spatial.getLocalScale().x, state,
                            spatial.getLocalTranslation(), pup.getCol1(), pup.getCol2(),
                            pup.getCol3());
                    }
                }
            }
        }
        
        SoundFX.playSound(state, "Sound/ExplosionRock.wav", spatial.getLocalTranslation(),
                Options.getSFXVolume() * 2f);
    }
    
    @Override
    public boolean weaponHit(Vector3f hitDirection) {
        destroy(true);
        
        if (pupCrystal == null
                && hitDirection != null && spatial.getLocalScale().x >= Asteroid.minSplit
                && state.getNumAsteroids() < Tracker.MAXAST * 2) {
            float scale = spatial.getLocalScale().x / 2.05f;
            Quaternion quat = new Quaternion();
            quat.lookAt(hitDirection, Vector3f.UNIT_Y);
            Vector3f loc = new Vector3f(scale + 0.05f, 0, 0);
            quat.mult(loc, loc);
            loc.addLocal(spatial.getLocalTranslation());
            AstCont ast = state.getApp().asteroids[FastMath.nextRandomInt(0,
                    state.getApp().asteroids.length - 1)].getAsteroid(scale, loc, state);
            
            loc.set(GMath.randomFloat(10, 25), 0, 0);
            quat.mult(loc, loc);
            loc.x += (float)body.getLinearVelocity().x;
            loc.z += (float)body.getLinearVelocity().y;
            ast.body.setLinearVelocity(loc.x, loc.z);
            ast.body.setAngularVelocity(GMath.randomFloat(-5, 5));
            
            state.addSimulatedBody(ast);
            
            loc.set(-scale - 0.05f, 0, 0);
            quat.mult(loc, loc);
            loc.addLocal(spatial.getLocalTranslation());
            ast = state.getApp().asteroids[FastMath.nextRandomInt(0,
                    state.getApp().asteroids.length - 1)].getAsteroid(scale, loc, state);
            
            loc.set(-GMath.randomFloat(10, 25), 0, 0);
            quat.mult(loc, loc);
            loc.x += (float)body.getLinearVelocity().x;
            loc.z += (float)body.getLinearVelocity().y;
            ast.body.setLinearVelocity(loc.x, loc.z);
            ast.body.setAngularVelocity(GMath.randomFloat(-5, 5));
            
            state.addSimulatedBody(ast);
        }
        
        return true;
    }
}
