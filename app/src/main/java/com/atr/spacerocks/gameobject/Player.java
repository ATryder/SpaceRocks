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
package com.atr.spacerocks.gameobject;

import com.atr.math.GMath;
import com.atr.spacerocks.control.AstCont;
import com.atr.spacerocks.control.PUPCont;
import com.atr.spacerocks.control.PlayerCont;
import com.atr.spacerocks.effects.EngineTrail;
import com.atr.spacerocks.effects.Flash;
import com.atr.spacerocks.effects.LaserCannons;
import com.atr.spacerocks.effects.particles.PlayerExplosion;
import com.atr.spacerocks.effects.particles.Sparks;
import com.atr.spacerocks.state.GameState;
import com.atr.spacerocks.util.Asteroid;
import com.atr.spacerocks.util.Tools;
import com.jme3.math.FastMath;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector2f;
import com.jme3.math.Vector3f;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import org.dyn4j.dynamics.Body;
import org.dyn4j.geometry.Transform;
import org.dyn4j.geometry.Vector2;

/**
 *
 * @author Adam T. Ryder
 * <a href="http://1337atr.weebly.com">http://1337atr.weebly.com</a>
 */
public class Player {
    private final float DEG_PER_SEC = 180;
    private final float ROLL_PER_SEC = 360;
    
    public final Node spatial;
    private final Spatial roll;
    public final Body body;
    
    public final GameState gameState;
    
    private final Quaternion rot = new Quaternion();
    private final Vector3f maxVelocity = new Vector3f(0, 0, 64);
    private final Vector3f currentVelocityAbs = new Vector3f();
    private final Vector3f currentVelocity = new Vector3f();
    
    private float lastThumb = 0;
    private float toAngle = 0;
    private float fromAngle = 0;
    private float currentAngle = 0;
    private float rotLength = 0;
    private float rotTme = 0;
    
    private final float rollMax = 45;
    private float toRoll = 0;
    private float fromRoll = 0;
    private float currentRoll = 0;
    private float rollLength = 0;
    private float rollTme = 0;
    
    public final float maxHP = 100;
    private float hp = maxHP;
    
    private final EngineTrail engineTrail;
    private final LaserCannons cannons;
    
    public Player(Spatial spatial, Body playerBody, GameState gameState) {
        this.spatial = (Node)spatial;
        roll = this.spatial.getChild(0);
        body = playerBody;
        this.gameState = gameState;
        
        toAngle = gameState.getHUD().getThumbAngle();
        fromAngle = toAngle;
        currentAngle = toAngle;
        lastThumb = toAngle;
        
        body.getTransform().setTranslationX(0);
        body.getTransform().setTranslationY(0);
        body.getTransform().setRotation(0);
        spatial.setLocalTranslation(0, 0, 0);
        spatial.addControl(new PlayerCont(gameState.getApp(), body));
        
        engineTrail = new EngineTrail(gameState.getApp().getAssetManager());
        gameState.getApp().getRootNode().attachChild(engineTrail);
        
        cannons = new LaserCannons(this, gameState);
    }
    
    public void addHealth(float amount) {
        hp += amount;
        hp = hp > maxHP ? maxHP : hp;
        gameState.getHUD().setHealthPercent(hp / maxHP);
    }
    
    public float getHealth() {
        return hp;
    }
    
    public Vector3f getCurrentVelocity() {
        return currentVelocityAbs;
    }
    
    public void updateTrail(float tpf) {
        engineTrail.update(roll, gameState.getHUD().getThumbMag() > Tools.EPSILON, tpf);
    }
    
    public void reCenterTrail(Vector3f center) {
        engineTrail.reCenter(center);
        //engineTrail.setLocalTranslation(0, 0, 0);
    }
    
    public void updateLasers(float tpf) {
        cannons.update(tpf);
    }
    
    public void reCenterLasers(Vector3f center) {
        cannons.reCenter(center);
    }
    
    public void updatePlayer(float tpf) {
        Transform transform = body.getTransform();
        
        float mag = gameState.getHUD().getThumbMag();
        float angle = rotate(tpf);
        if (FastMath.abs(toAngle - currentAngle) < Tools.EPSILON) {
            roll(0, tpf);
        } else if (toAngle < currentAngle) {
            roll(-rollMax, tpf);
        } else
            roll(rollMax, tpf);
        
        transform.setRotation(angle);
        rot.fromAngles(0, angle, 0);
        rot.mult(maxVelocity, currentVelocityAbs);
        currentVelocityAbs.multLocal(mag);
        currentVelocityAbs.mult(tpf, currentVelocity);
        
        transform.setTranslationX(transform.getTranslationX() + currentVelocity.x);
        transform.setTranslationY(transform.getTranslationY() + currentVelocity.z);
    }
    
    private float rotate(float tpf) {
        float thumb = gameState.getHUD().getThumbAngle() - 90;
        if (thumb < 0)
            thumb += 360;
        
        float change = thumb - lastThumb;
        if (lastThumb < 90 && thumb >= 270) {
            change = thumb - (lastThumb + 360);
        } else if (lastThumb >= 270 && thumb < 90) {
            change = (thumb + 360) - lastThumb;
        }
        lastThumb = thumb;
        float toAngle = change + this.toAngle;
        
        if (toAngle < currentAngle) {
            while(currentAngle - toAngle > 180)
                toAngle += 360;
        } else {
            while (toAngle - currentAngle > 180)
                toAngle -= 360;
        }
        
        if (this.toAngle > currentAngle && toAngle <= currentAngle
                || this.toAngle < currentAngle && toAngle >= currentAngle)
            rotTme = 0;
        this.toAngle = toAngle;
        
        if (rotTme < Tools.EPSILON) {
            while (currentAngle > 360)
                currentAngle -= 360;
            while (toAngle > 360)
                toAngle -= 360;
            fromAngle = currentAngle;
        }
        
        rotLength = FastMath.abs(toAngle - fromAngle) / DEG_PER_SEC;
        if (rotLength > 0) {
            rotTme += tpf;
            float perc = rotTme / rotLength;
            
            if (perc < 1) {
                currentAngle = GMath.smoothFloat(perc, fromAngle, toAngle);
            } else {
                currentAngle = toAngle;
                fromAngle = toAngle;
                rotTme = 0;
            }
        } else {
            rotTme = 0;
            currentAngle = toAngle;
            fromAngle = toAngle;
        }
        
        float angle = currentAngle;
        while (angle >= 360)
            angle -= 360;
        
        if (angle > 180)
            return -(360 - angle) * FastMath.DEG_TO_RAD;

        return angle * FastMath.DEG_TO_RAD;
    }
    
    private void roll(float angle, float tpf) {
        if (currentRoll == angle) {
            rollTme = 0;
            return;
        }
        
        if (angle != toAngle) {
            fromRoll = currentRoll;
            toRoll = angle;
            rollTme = 0;
        }
        
        rollLength = FastMath.abs(fromRoll - toRoll) / ROLL_PER_SEC;
        if (rollLength > 0) {
            rollTme += tpf;
            float perc = rollTme / rollLength;
            
            if (perc < 1) {
                currentRoll = GMath.smoothFloat(perc, fromRoll, toRoll);
            } else {
                currentRoll = toRoll;
                fromRoll = toRoll;
                rollTme = 0;
            }
        } else {
            rollTme = 0;
            fromRoll = toRoll;
            currentRoll = toRoll;
        }
        
        Quaternion quat = roll.getLocalRotation();
        quat.fromAngles(0, FastMath.PI, currentRoll * FastMath.DEG_TO_RAD);
        roll.setLocalRotation(quat);
    }
    
    public void collide(Vector2f playerVelocity, Body otherBody, Vector2f otherVelocity) {
        if (otherBody.getUserData() instanceof AstCont) {
            AstCont asteroid = (AstCont)otherBody.getUserData();
            
            float pMag = playerVelocity.length();
            float oMag = otherVelocity.length();
            
            float force = asteroid.getSpatial().getLocalScale().x / Asteroid.minScale;
            if (oMag > Tools.EPSILON) {
                if (pMag > Tools.EPSILON) {
                    playerVelocity.normalizeLocal();
                    otherVelocity.normalizeLocal();
                    float angle = FastMath.abs(playerVelocity.angleBetween(otherVelocity));

                    pMag = (angle <= FastMath.PI / 2) ? -(1 - (angle / (FastMath.PI / 2))) * pMag :
                            ((angle - (FastMath.PI / 2)) / (FastMath.PI / 2)) * pMag;
                }
                
                force *= Math.max(oMag + pMag, 0.0f);
            } else
                force *= Math.max(pMag, 0.0f);
            
            Vector2 ov = new Vector2();
            otherVelocity.set((float)otherBody.getTransform().getTranslationX(),
                    (float)otherBody.getTransform().getTranslationY());
            otherVelocity.subtractLocal(spatial.getLocalTranslation().x,
                    spatial.getLocalTranslation().z).normalizeLocal()
                    .multLocal(force * 0.6f);
            ov.x += otherVelocity.x;
            ov.y += otherVelocity.y;
            otherBody.setLinearVelocity(ov);
            
            force *= Asteroid.BASE_DAMAGE;
            hp -= (force >= hp) ? hp : force;
            gameState.getHUD().setHealthPercent(hp / maxHP);
        } else if (otherBody.getUserData() instanceof PUPCont) {
            ((PUPCont)otherBody.getUserData()).destroy(true);
        }
    }
    
    public void destroy() {
        Flash flash = new Flash(gameState);
        flash.setLocalTranslation(spatial.getLocalTranslation());
        flash.setLocalScale(9);
        gameState.noCollideNode.attachChild(flash);
        
        new Sparks(20, 8, gameState, spatial.getLocalTranslation());
        
        new PlayerExplosion(spatial.getLocalTranslation(), gameState);
    }
}
