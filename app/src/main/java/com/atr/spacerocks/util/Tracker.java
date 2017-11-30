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
package com.atr.spacerocks.util;

import com.atr.math.GMath;
import com.atr.spacerocks.control.AstCont;
import com.atr.spacerocks.effects.particles.SonicBlast;
import com.atr.spacerocks.gameobject.powerup.PUP.PUPType;
import com.atr.spacerocks.gameobject.Player;
import com.atr.spacerocks.gameobject.powerup.BlackHole;
import com.atr.spacerocks.sound.SoundFX;
import com.atr.spacerocks.state.GameState;
import com.jme3.math.FastMath;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import java.util.LinkedList;
import org.dyn4j.dynamics.Body;

/**
 *
 * @author Adam T. Ryder
 * <a href="http://1337atr.weebly.com">http://1337atr.weebly.com</a>
 */
public class Tracker {
    private static final int LEFT = 0;
    private static final int RIGHT = 1;
    private static final int TOP = 2;
    private static final int BOTTOM = 3;
    private static final float RANDROT = 32;
    
    public static final int MAXAST = 32;
    
    private final GameState gameState;
    private final Player player;
    
    private float astSpawn = 0;
    private int numAsteroids = 0;
    private final Vector3f astSpawnLoc = new Vector3f();
    
    private final SRay sRay = new SRay();
    private final SRay pRay = new SRay();
    
    private final Quaternion quat = new Quaternion();
    private Vector3f targetPoint = new Vector3f();
    
    private final float maxAstPerSec = 12f;
    private final float minAstPerSec = 1f;
    private final float astRampLength = 10 * 60f;
    private float astRamp = 0f;
    private final float spawnRampLength = 10f;
    private float spawnRamp = 0f;
    
    private float healthPerSec = 1/10f;
    private float healthSpawn = 0;
    
    private float pupPerSec = 1/1.5f;
    private float pupSpawn = 0;
    
    private float blackHolePerSec = 1/60f;
    private float blackHoleSpawn = 0;
    
    private long spawnedAst = 0;
    private long destroyedAst = 0;
    private long laserAst = 0;
    private long blastAst = 0;
    private long bhAst = 0;
    
    private long droppedHealth = 0;
    private long pickedHealth = 0;
    private long droppedEnergy = 0;
    private long pickedEnergy = 0;
    private long droppedBlast = 0;
    private long pickedBlast = 0;
    private long droppedBlackHoles = 0;
    private long spawnedBlackHoles = 0;
    
    private int currentBlast = 0;
    private int blasting = 0;
    
    private boolean tracking = true;
    
    private final LinkedList<BlackHole> blackHoles = new LinkedList<BlackHole>();
    
    public Tracker(GameState gameState, Player player) {
        this.gameState = gameState;
        this.player = player;
    }
    
    public void setTracking(boolean tracking) {
        this.tracking = tracking;
    }
    
    public boolean isTracking() {
        return tracking;
    }
    
    public int getNumAsteroids() {
        return numAsteroids;
    }
    
    public void addAsteroid() {
        numAsteroids++;
        if (tracking)
            spawnedAst++;
    }
    
    public void removeAsteroid() {
        numAsteroids--;
    }
    
    public void destroyedAsteroid() {
        if (!tracking)
            return;
        
        destroyedAst++;
        gameState.getHUD().updateAsteroidCount();
    }
    
    public long getDestroyedAsteroids() {
        return destroyedAst;
    }
    
    public long getLaserDestroyedAsteroids() {
        return laserAst;
    }
    
    public long getBlastDestroyedAsteroids() {
        return blastAst;
    }
    
    public long getBHDestroyedAsteroids() {
        return bhAst;
    }
    
    public long getTotalSpawnedAsteroids() {
        return spawnedAst;
    }
    
    public void laserDestroy() {
        if (!tracking)
            return;
        laserAst++;
    }
    
    public void blastDestroy() {
        if (!tracking)
            return;
        blastAst++;
    }
    
    public void bhDestroy() {
        if (!tracking)
            return;
        bhAst++;
    }
    
    public void pupDropped(PUPType pup) {
        if (!tracking)
            return;
        switch (pup) {
            case ENERGY:
                droppedEnergy++;
                break;
            case HEALTH:
                droppedHealth++;
                break;
            case SONIC:
                droppedBlast++;
                break;
            case BLACKHOLE:
                droppedBlackHoles++;
        }
    }
    
    public void pupPicked(PUPType pup) {
        if (!tracking)
            return;
        switch (pup) {
            case ENERGY:
                pickedEnergy++;
                break;
            case HEALTH:
                pickedHealth++;
                break;
            case SONIC:
                currentBlast++;
                pickedBlast++;
                gameState.getHUD().updateSonicBlastCount();
                break;
            case BLACKHOLE:
                spawnedBlackHoles++;
        }
    }
    
    public void addBlackHole(BlackHole blackHole) {
        blackHoles.add(blackHole);
    }
    
    public void removeBlackHole(BlackHole blackHole) {
        blackHoles.remove(blackHole);
    }
    
    public LinkedList<BlackHole> getBlackHoles() {
        return blackHoles;
    }
    
    public int getCurrentSonicBlast() {
        return currentBlast;
    }
    
    public void useSonicBlast() {
        if (currentBlast == 0)
            return;
        
        blasting++;
        currentBlast--;
        gameState.getHUD().updateSonicBlastCount();
        SonicBlast blast = new SonicBlast(gameState);
        blast.getSpatial().setLocalTranslation(player.spatial.getLocalTranslation());
        gameState.noCollideNode.attachChild(blast.getSpatial());
        
        /*SoundFX.playSound(gameState, "Sound/SonicBlast.wav",
                player.spatial.getLocalTranslation(), true);*/
        SoundFX.playSonicBlast(gameState.getApp().getAssetManager());
    }
    
    public void finishSonicBlast() {
        blasting -= 1;
        if (blasting > 0)
            return;
        
        rampSpawn();
    }
    
    public void rampSpawn() {
        spawnRamp = 0;
    }
    
    public long getDroppedHealth() {
        return droppedHealth;
    }
    
    public long getPickedHealth() {
        return pickedHealth;
    }
    
    public long getDroppedEnergy() {
        return droppedEnergy;
    }
    
    public long getPickedEnergy() {
        return pickedEnergy;
    }
    
    public long getDroppedBlast() {
        return droppedBlast;
    }
    
    public long getPickedBlast() {
        return pickedBlast;
    }
    
    public long getDroppedBH() {
        return droppedBlackHoles;
    }
    
    public long getSpawnedBH() {
        return spawnedBlackHoles;
    }
    
    public void update(float tpf) {
        if (blasting > 0)
            return;
        
        blackHoleSpawn += tpf * blackHolePerSec;
        if (numAsteroids < MAXAST && blackHoleSpawn >= 1) {
            if (blackHoles.isEmpty()) {
                AstCont a = generateAsteroid(tpf, GMath.randomFloat(Asteroid.minScale, Asteroid.maxScale));
                a.setPUP(PUPType.BLACKHOLE);
            }
            blackHoleSpawn = 0;
            blackHolePerSec = 1f / GMath.randomFloat(30f, 90f);
        } else if (blackHoleSpawn > 1)
            blackHoleSpawn = 1;
        
        healthSpawn += tpf * healthPerSec;
        /*while (numAsteroids < MAXAST && healthSpawn >= 1) {
            AstCont a = generateAsteroid(tpf, Asteroid.maxScale);
            a.setPUP(PUPType.HEALTH);
            healthSpawn -= 1;
        }*/
        if (numAsteroids < MAXAST && healthSpawn >= 1) {
            AstCont a = generateAsteroid(tpf, Asteroid.maxScale);
            a.setPUP(PUPType.HEALTH);
            healthSpawn = 0;
        } else if (healthSpawn > 1)
            healthSpawn = 1;
        
        pupSpawn += tpf * pupPerSec;
        /*while (numAsteroids < MAXAST && pupSpawn >= 1) {
            AstCont a = generateAsteroid(tpf, GMath.randomFloat(Asteroid.minScale, Asteroid.maxScale));
            if (currentBlast == 0 && FastMath.nextRandomFloat() < 0.0435f) {
                a.setPUP(PUPType.SONIC);
            } else
                a.setPUP(PUPType.ENERGY);
            pupSpawn -= 1;
        }*/
        if (numAsteroids < MAXAST && pupSpawn >= 1) {
            AstCont a = generateAsteroid(tpf, GMath.randomFloat(Asteroid.minScale, Asteroid.maxScale));
            if (currentBlast < 2 && FastMath.nextRandomFloat() < 0.07f) {
                a.setPUP(PUPType.SONIC);
            } else
                a.setPUP(PUPType.ENERGY);
            pupSpawn = 0;
        } else if (pupSpawn > 1)
            pupSpawn = 1;
        
        astRamp += tpf;
        astRamp = astRamp > astRampLength ? astRampLength : astRamp;
        spawnRamp += tpf;
        spawnRamp = spawnRamp > spawnRampLength ? spawnRampLength : spawnRamp;
        astSpawn += tpf * ((((maxAstPerSec - minAstPerSec) * (astRamp / astRampLength))
                * (spawnRamp / spawnRampLength))
                + minAstPerSec);
        
        if (numAsteroids < MAXAST && astSpawn >= 1) {
            generateAsteroid(tpf, GMath.randomFloat(Asteroid.minScale, Asteroid.maxScale));
            astSpawn = 0;
        } else if (astSpawn > 1) {
            astSpawn = 1;
        }
    }
    
    private AstCont spawnAsteroid(float tpf, int side, boolean target, float scale) {
        AstCont cont = gameState.getApp().
                asteroids[FastMath.nextRandomInt(0, gameState.getApp()
                        .asteroids.length - 1)].getAsteroid(scale, astSpawnLoc, gameState);
        gameState.addSimulatedBody(cont);
        
        float velocity = GMath.randomFloat(55, 75);
        
        targetPoint.set(0, 0, 0);
        if (target) {
            if (gameState.getHUD().getThumbMag() > Tools.EPSILON) {
                targetPoint = new Vector3f((float)player.body.getTransform().getTranslationX(),
                        0,
                        (float)player.body.getTransform().getTranslationY());
            } else {
                float dist = astSpawnLoc.distance(targetPoint);
                float timeToPoint = dist / velocity;
                player.getCurrentVelocity().mult(timeToPoint, targetPoint);
                targetPoint.x += (float)player.body.getTransform().getTranslationX();
                targetPoint.z += (float)player.body.getTransform().getTranslationY();
            }

            targetPoint.subtractLocal(astSpawnLoc).normalizeLocal();
        } else {
            switch (side) {
                case RIGHT:
                    targetPoint.x = 1;
                    if (astSpawnLoc.z < player.spatial.getLocalTranslation().z) {
                        quat.fromAngles(0, GMath.randomFloat(-RANDROT, 0) * FastMath.DEG_TO_RAD, 0);
                    } else if (astSpawnLoc.z > player.spatial.getLocalTranslation().z) {
                        quat.fromAngles(0, GMath.randomFloat(0, RANDROT) * FastMath.DEG_TO_RAD, 0);
                    } else
                        quat.fromAngles(0, GMath.randomFloat(-RANDROT, RANDROT) * FastMath.DEG_TO_RAD, 0);
                    break;
                case LEFT:
                    targetPoint.x = -1;
                    if (astSpawnLoc.z > player.spatial.getLocalTranslation().z) {
                        quat.fromAngles(0, GMath.randomFloat(-RANDROT, 0) * FastMath.DEG_TO_RAD, 0);
                    } else if (astSpawnLoc.z < player.spatial.getLocalTranslation().z) {
                        quat.fromAngles(0, GMath.randomFloat(0, RANDROT) * FastMath.DEG_TO_RAD, 0);
                    } else
                        quat.fromAngles(0, GMath.randomFloat(-RANDROT, RANDROT) * FastMath.DEG_TO_RAD, 0);
                    break;
                case TOP:
                    targetPoint.z = -1;
                    if (astSpawnLoc.x < player.spatial.getLocalTranslation().x) {
                        quat.fromAngles(0, GMath.randomFloat(-RANDROT, 0) * FastMath.DEG_TO_RAD, 0);
                    } else if (astSpawnLoc.x > player.spatial.getLocalTranslation().x) {
                        quat.fromAngles(0, GMath.randomFloat(0, RANDROT) * FastMath.DEG_TO_RAD, 0);
                    } else
                        quat.fromAngles(0, GMath.randomFloat(-RANDROT, RANDROT) * FastMath.DEG_TO_RAD, 0);
                    break;
                default:
                    targetPoint.z = 1;
                    if (astSpawnLoc.x > player.spatial.getLocalTranslation().x) {
                        quat.fromAngles(0, GMath.randomFloat(-RANDROT, 0) * FastMath.DEG_TO_RAD, 0);
                    } else if (astSpawnLoc.x < player.spatial.getLocalTranslation().x) {
                        quat.fromAngles(0, GMath.randomFloat(0, RANDROT) * FastMath.DEG_TO_RAD, 0);
                    } else
                        quat.fromAngles(0, GMath.randomFloat(-RANDROT, RANDROT) * FastMath.DEG_TO_RAD, 0);
                    break;
            }
            quat.mult(targetPoint, targetPoint);
        }
        
        Body body = cont.getBody();
        body.setLinearVelocity(targetPoint.x * velocity, targetPoint.z * velocity);
        body.setAngularVelocity(GMath.randomFloat(-5, 5));
        
        return cont;
    }
    
    private AstCont generateAsteroid(float tpf, float scale) {
        astSpawnLoc.set(0, 0, 0);
        
        int side;
        if (FastMath.nextRandomFloat() >= 0.7f) {
            side = setDirSpawnLoc();
            
            if (FastMath.nextRandomFloat() > 0.15f) {
                setSpawnLoc(side);
                return spawnAsteroid(tpf, side, FastMath.nextRandomFloat() > 0.65f,
                        scale);
            } else
                return spawnAsteroid(tpf, side, true, scale);
        }
        
        side = setSpawnLoc(FastMath.nextRandomInt(0, 3));
        return spawnAsteroid(tpf, side, FastMath.nextRandomFloat() > 0.65f, scale);
    }
    
    private int setSpawnLoc(int side) {
        float fieldWidth = GameState.FIELDHEIGHT * ((float)gameState.getApp().getWidth()
                / gameState.getApp().getHeight());
        float fieldWidthHalf = fieldWidth / 2f;
        astSpawnLoc.y = 0;
        switch (side) {
            case RIGHT:
                astSpawnLoc.x = (player.spatial.getLocalTranslation().x - fieldWidthHalf) - GameState.ASTEROID_PADDING;
                astSpawnLoc.z = GMath.randomFloat((player.spatial.getLocalTranslation().z - GameState.FIELDHEIGHTHALF) - GameState.ASTEROID_PADDING,
                        player.spatial.getLocalTranslation().z + GameState.FIELDHEIGHTHALF + GameState.ASTEROID_PADDING);
                break;
            case LEFT:
                astSpawnLoc.x = player.spatial.getLocalTranslation().x + fieldWidthHalf + GameState.ASTEROID_PADDING;
                astSpawnLoc.z = GMath.randomFloat((player.spatial.getLocalTranslation().z - GameState.FIELDHEIGHTHALF) - GameState.ASTEROID_PADDING,
                        player.spatial.getLocalTranslation().z + GameState.FIELDHEIGHTHALF + GameState.ASTEROID_PADDING);
                break;
            case TOP:
                astSpawnLoc.z = player.spatial.getLocalTranslation().z + GameState.FIELDHEIGHTHALF + GameState.ASTEROID_PADDING;
                astSpawnLoc.x = GMath.randomFloat((player.spatial.getLocalTranslation().x - fieldWidthHalf) - GameState.ASTEROID_PADDING,
                        player.spatial.getLocalTranslation().x + fieldWidthHalf + GameState.ASTEROID_PADDING);
                break;
            default:
                astSpawnLoc.z = (player.spatial.getLocalTranslation().z - GameState.FIELDHEIGHTHALF) - GameState.ASTEROID_PADDING;
                astSpawnLoc.x = GMath.randomFloat((player.spatial.getLocalTranslation().x - fieldWidthHalf) - GameState.ASTEROID_PADDING,
                        player.spatial.getLocalTranslation().x + fieldWidthHalf + GameState.ASTEROID_PADDING);
        }
        
        return side;
    }
    
    private int setDirSpawnLoc() {
        float fieldWidth = GameState.FIELDHEIGHT * ((float)gameState.getApp().getWidth()
                / gameState.getApp().getHeight());
        float fieldWidthHalf = fieldWidth / 2f;
        astSpawnLoc.set(0, 0, 0);
        
        player.spatial.getLocalRotation().mult(Vector3f.UNIT_Z, targetPoint);
        pRay.set(player.spatial.getLocalTranslation(), targetPoint);

        targetPoint.set((player.spatial.getLocalTranslation().x - fieldWidthHalf)
                - GameState.ASTEROID_PADDING,
                0,
                (player.spatial.getLocalTranslation().z -
                        GameState.FIELDHEIGHTHALF) - GameState.ASTEROID_PADDING);
        sRay.set(targetPoint, Vector3f.UNIT_Z);
        sRay.setLength(GameState.FIELDHEIGHT);

        if (sRay.intersects(pRay, astSpawnLoc) != null)
            return RIGHT;
        
        targetPoint.set((player.spatial.getLocalTranslation().x + fieldWidthHalf)
                + GameState.ASTEROID_PADDING,
                0,
                (player.spatial.getLocalTranslation().z -
                        GameState.FIELDHEIGHTHALF) - GameState.ASTEROID_PADDING);
        sRay.set(targetPoint, Vector3f.UNIT_Z);
        
        if (sRay.intersects(pRay, astSpawnLoc) != null)
            return LEFT;
        
        targetPoint.set((player.spatial.getLocalTranslation().x - fieldWidthHalf)
                - GameState.ASTEROID_PADDING,
                0,
                player.spatial.getLocalTranslation().z +
                        GameState.FIELDHEIGHTHALF + GameState.ASTEROID_PADDING);
        sRay.set(targetPoint, Vector3f.UNIT_X);
        sRay.setLength(fieldWidth);
        
        if (sRay.intersects(pRay, astSpawnLoc) != null)
            return TOP;
        
        targetPoint.set((player.spatial.getLocalTranslation().x - fieldWidthHalf)
                - GameState.ASTEROID_PADDING,
                0,
                (player.spatial.getLocalTranslation().z -
                        GameState.FIELDHEIGHTHALF) - GameState.ASTEROID_PADDING);
        sRay.set(targetPoint, Vector3f.UNIT_X);
        
        if (sRay.intersects(pRay, astSpawnLoc) != null)
            return BOTTOM;
        
        return setSpawnLoc(FastMath.nextRandomInt(0, 3));
    }
}
