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
package com.atr.spacerocks.effects;

import com.atr.spacerocks.control.BodyCont;
import com.atr.spacerocks.gameobject.Player;
import com.atr.spacerocks.effects.particles.LaserParticles;
import com.atr.spacerocks.gameobject.powerup.BlackHole;
import com.atr.spacerocks.sound.SoundFX;
import com.atr.spacerocks.state.GameState;
import com.atr.spacerocks.util.SRay;
import com.atr.spacerocks.util.Tools;
import com.jme3.collision.CollisionResult;
import com.jme3.collision.CollisionResults;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.FastMath;
import com.jme3.math.Quaternion;
import com.jme3.math.Ray;
import com.jme3.math.Vector3f;
import com.jme3.renderer.queue.RenderQueue;
import com.jme3.scene.Geometry;
import com.jme3.scene.Spatial;

/**
 *
 * @author Adam T. Ryder
 * <a href="http://1337atr.weebly.com">http://1337atr.weebly.com</a>
 */
public class LaserCannons {
    private static final float minSpawnTime = 0.1f;
    private static final float maxSpawnTime = 0.2f;
    
    private final Player player;
    private final GameState gameState;
    
    private final Vector3f laser1 = new Vector3f(-1.12324f, 0, -5.04729f);
    private final Vector3f laser2 = new Vector3f(1.12324f, 0, -5.04729f);
    private final Vector3f tmpV = new Vector3f();
    private final Vector3f tmpV2 = new Vector3f();
    private final Vector3f tmp = new Vector3f();
    private boolean side1 = true;
    
    private final CollisionResults collisionResults = new CollisionResults();
    private final Ray ray = new Ray();
    private final SRay sRay = new SRay();
    
    private final Vector3f velocity = new Vector3f(0, 0, 250);
    
    private final LaserBeam[] lasers = new LaserBeam[32];
    private short lastDeadLaser = 0;
    private int count = 0;
    
    private final LaserParticles mesh;
    private final Geometry laserGeom;
    
    private float spawnTime = maxSpawnTime;
    
    private float force = 0;
    
    public LaserCannons(Player player, GameState gameState) {
        this.player = player;
        this.gameState = gameState;
        
        for (int i = 0; i < lasers.length; i++) {
            LaserBeam beam = new LaserBeam();
            beam.lastDead = (short)(i + 1);
            beam.index = (short)i;
            lasers[i] = beam;
        }
        lasers[lasers.length - 1].lastDead = 0;
        
        mesh = new LaserParticles(lasers);
        laserGeom = new Geometry("Laser Beams", mesh);
        Material mat = new Material(gameState.getApp().getAssetManager(),
                "MatDefs/Unshaded/circle_glow.j3md");
        mat.setColor("Color", new ColorRGBA(1f, 0.749f, 0.455f, 1f));
        mat.setColor("Color2", new ColorRGBA(1f, 0.143f, 0.031f, 0.5f));
        mat.setColor("Color3", new ColorRGBA(1f, 0f, 0f, 0f));
        mat.setFloat("Pos1", 0.134f);
        mat.setFloat("Pos2", 0.648f);
        laserGeom.setMaterial(mat);
        
        laserGeom.setQueueBucket(RenderQueue.Bucket.Transparent);
        laserGeom.setCullHint(Spatial.CullHint.Never);
        
        gameState.getApp().getRootNode().attachChild(laserGeom);
    }
    
    public void reCenter(Vector3f center) {
        //laserGeom.setLocalTranslation(0, 0, 0);
        for (LaserBeam beam : lasers) {
            beam.loc.subtractLocal(center);
        }
    }
    
    public void update(float tpf) {
        for (LaserBeam beam : lasers) {
            if (beam.alive)
                beam.update(tpf);
        }
        
        spawnBeams(tpf);
        
        mesh.updateMesh(lasers);
    }
    
    private void spawnBeams(float tpf) {
        spawnTime += tpf;
        if (!gameState.getHUD().isFiring()) {
            if (spawnTime > maxSpawnTime)
                spawnTime = maxSpawnTime;
            
            return;
        }
        
        float nextSpawn = ((maxSpawnTime - minSpawnTime) * (1 - gameState.getHUD().getEnergy()))
                + minSpawnTime;
        
        gameState.getHUD().setEnergyPercent(gameState.getHUD().getEnergy() - (0.02f * tpf));
        
        if (spawnTime < nextSpawn)
            return;
        
        /*do {
            spawnTime -= nextSpawn;
        } while(spawnTime >= nextSpawn);*/
        spawnTime = 0;
        
        tmpV.set(side1 ? laser1 : laser2);
        side1 = !side1;
        player.spatial.getChild(0).localToWorld(tmpV, tmpV);
        
        count = 0;
        LaserBeam beam = null;
        do {
            LaserBeam b = lasers[lastDeadLaser];
            lastDeadLaser = b.lastDead;
            if (!b.alive) {
                beam = b;
                break;
            }
            count++;
        } while (count < lasers.length);
        
        if (beam == null)
            return;
        
        beam.set(tmpV, player.spatial.getLocalRotation());
        SoundFX.playLaser(gameState.getApp().getAssetManager());
    }
    
    public class LaserBeam {
        private final float maxZ = GameState.FIELDHEIGHTHALF
                + GameState.ASTEROID_PADDING + GameState.ASTEROID_PADDING;
        private final float maxX = ((GameState.FIELDHEIGHT *
                ((float)gameState.getApp().getWidth() / gameState.getApp().getHeight()))
                / 2f) + GameState.ASTEROID_PADDING + GameState.ASTEROID_PADDING;
        
        public final Vector3f loc = new Vector3f();
        public final Quaternion rot = new Quaternion();
        
        public boolean alive = false;
        public short lastDead = 0;
        public short index = 0;
        
        private LaserBeam() {
        }
        
        private void set(Vector3f loc, Quaternion rot) {
            this.loc.set(loc);
            this.rot.set(rot);
            alive = true;
        }
        
        private void update(float tpf) {
            velocity.mult(tpf, tmpV);
            rot.mult(tmpV, tmpV);
            tmpV.addLocal(loc);
            for (BlackHole bh : gameState.getTracker().getBlackHoles()) {
                if (!bh.getController().isActive())
                    continue;
                
                bh.getController().getSpatial().getLocalTranslation()
                        .subtract(tmpV, tmp).normalizeLocal();
                force = Tools.GRAVITATIONALCONSTANT
                        * ((BlackHole.MASS * 0.04f)
                        / FastMath.sqr(tmpV.distance(bh.getController()
                                .getSpatial().getLocalTranslation()) / 2)) * tpf;
                tmp.multLocal(force);
                tmpV.addLocal(tmp);
                
                sRay.set(loc, tmpV.subtract(loc, tmp));
                if (sRay.intersectsCircle(bh.getLocation(), BlackHole.SINGULARITY, tmp)) {
                    alive = false;
                    break;
                }
            }
            
            if (alive) {
                rot.lookAt(tmpV.subtract(loc, tmp), Vector3f.UNIT_Y);

                tmpV2.set(0, 0, LaserParticles.getLength() + tmpV.distance(loc));
                rot.mult(tmpV2, tmpV2);
                tmpV2.addLocal(loc);

                float limit = tmpV2.distance(loc);
                tmpV2.subtractLocal(loc);

                collisionResults.clear();
                ray.setOrigin(loc);
                ray.setDirection(tmpV2.normalizeLocal());
                ray.setLimit(limit);
                gameState.collideNode.collideWith(ray, collisionResults);
                for (CollisionResult hit : collisionResults) {
                    BodyCont cont = hit.getGeometry().getUserData(BodyCont.key);
                    if (cont != null && cont.weaponHit(tmpV2.normalizeLocal())) {
                        gameState.getTracker().laserDestroy();
                        alive = false;
                        break;
                    }
                }
            }
            
            if (alive) {
                loc.set(tmpV);

                tmpV.subtractLocal(player.spatial.getLocalTranslation());
                alive = !(tmpV.x > maxX
                    || tmpV.x < -maxX
                    || tmpV.z > maxZ
                    || tmpV.z < -maxZ);
            }
            
            if (alive)
                return;
            
            if (lastDead != lastDeadLaser)
                lastDead = lastDeadLaser;
            lastDeadLaser = index;
        }
    }
}
