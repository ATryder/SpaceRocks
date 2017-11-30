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
package com.atr.spacerocks.state;

import com.atr.math.GMath;
import com.atr.spacerocks.SpaceRocks;
import com.atr.spacerocks.control.AstCont;
import com.atr.spacerocks.control.BodyCont;
import com.atr.spacerocks.control.MainMenuEffects;
import com.atr.spacerocks.effects.starfield.StarField;
import com.atr.spacerocks.effects.starfield.StarFieldGeom;
import com.atr.spacerocks.gameobject.Player;
import com.atr.spacerocks.physics.CollisionHandler;
import com.atr.spacerocks.physics.CollisionListenerImpl;
import com.atr.spacerocks.ui.HUD;
import com.atr.spacerocks.ui.UI;
import com.atr.spacerocks.util.Callback;
import com.jme3.app.Application;
import com.jme3.app.state.AbstractAppState;
import com.jme3.app.state.AppStateManager;
import com.jme3.light.AmbientLight;
import com.jme3.light.DirectionalLight;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector2f;
import com.jme3.math.Vector3f;
import com.jme3.renderer.Camera;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import org.dyn4j.dynamics.Body;
import org.dyn4j.dynamics.World;
import org.dyn4j.geometry.Vector2;
import com.atr.spacerocks.physics.DynSim;
import com.atr.spacerocks.sound.Music;
import com.atr.spacerocks.sound.SoundFX;
import com.atr.spacerocks.util.Tools;
import com.atr.spacerocks.util.Tracker;
import com.jme3.material.Material;
import org.dyn4j.collision.broadphase.Sap;
import org.dyn4j.collision.narrowphase.Sat;
import org.dyn4j.dynamics.ContinuousDetectionMode;

/**
 *
 * @author Adam T. Ryder
 * <a href="http://1337atr.weebly.com">http://1337atr.weebly.com</a>
 */
public class GameState extends AbstractAppState {
    private static final float MAXTRAVEL = 10000;
    public static final float FIELDHEIGHT = 176;
    public static final float FIELDHEIGHTHALF = FIELDHEIGHT / 2f;
    public static final float ASTEROID_PADDING = 32;
    
    private final SpaceRocks app;
    public final Node rootNode;
    public final Node collideNode;
    public final Node noCollideNode;
    
    private DynSim sim;
    private Thread simThread;
    
    private final Player player;
    
    private final HUD hud;
    
    private final StarField starField;
    private final StarFieldGeom starFieldGeom;
    
    private final Tracker aTrack;
    
    private final CollisionHandler collisionHandler;
    
    private final DirectionalLight directional;
    private final AmbientLight ambient;
    
    public GameState(SpaceRocks app) {
        this.app = app;
        rootNode = app.getRootNode();
        collideNode = new Node("Collide Root");
        collideNode.setCullHint(Spatial.CullHint.Dynamic);
        noCollideNode = new Node("No Collide Root");
        noCollideNode.setCullHint(Spatial.CullHint.Never);
        rootNode.attachChild(collideNode);
        rootNode.attachChild(noCollideNode);
        
        hud = new HUD(app, this);
        
        player = new Player(app.player.clone(false), app.playerBody, this);
        
        aTrack = new Tracker(this, player);
        
        directional = new DirectionalLight();
        directional.setDirection(new Vector3f(0.5859197f, -0.7671219f, 0.261935f));
        directional.setColor(new ColorRGBA(1f, 1f, 1f, 1f));
        rootNode.addLight(directional);
        
        ambient = new AmbientLight();
        ambient.setColor(new ColorRGBA(0.18f, 0.18f, 0.18f, 1f));
        rootNode.addLight(ambient);
        
        Camera gameCam = app.getCamera();
        float fov = 60f;
        float altitude = GMath.fastCamAltitude(fov, FIELDHEIGHT);
        float near = altitude - 100;
        float far = altitude + 100;
        gameCam.setFrustumPerspective(fov, (float)gameCam.getWidth() / gameCam.getHeight(), near, far);
        gameCam.setLocation(new Vector3f(0, altitude, 0));
        gameCam.setRotation(new Quaternion(new float[]{1.5708176978793961078628152543852f, 0f, 0f}));
        
        starField = new StarField(gameCam, fov);
        starFieldGeom = starField.createStarField();
        Material starMat = new Material(app.getAssetManager(), "MatDefs/Unshaded/circle_distance.j3md");
        starMat.setColor("Color", new ColorRGBA(1, 1, 1, 0.75f));
        starMat.setFloat("minOffset", 0.1f);
        starMat.setFloat("maxOffset", 0.99f);
        starMat.setFloat("minDist", altitude - 100);
        starMat.setFloat("Focus", altitude + 10);
        starFieldGeom.setMaterial(starMat);
        
        collisionHandler = new CollisionHandler(this);
    }
    
    @Override
    public void initialize(AppStateManager stateManager, Application app) {
        super.initialize(stateManager, app);
        
        startSimulation();
    }
    
    public SpaceRocks getApp() {
        return app;
    }
    
    public HUD getHUD() {
        return hud;
    }
    
    public Player getPlayer() {
        return player;
    }
    
    public int getNumAsteroids() {
        return aTrack.getNumAsteroids();
    }
    
    public Tracker getTracker() {
        return aTrack;
    }
    
    public void startSimulation() {
        World world;
        if (sim != null) {
            if (simThread.isAlive()) {
                sim.active.set(false);
                try {
                    simThread.join();
                } catch (Exception e) {

                }
            }
            world = sim.getWorld();
        } else {
            world = new World();
            world.setBounds(null);
            world.addBody(player.body);
            world.setGravity(new Vector2(0, 0));
            world.setBroadphaseDetector(new Sap());
            world.setNarrowphaseDetector(new Sat());
            world.getSettings().setContinuousDetectionMode(ContinuousDetectionMode.NONE);
            world.getSettings().setAutoSleepingEnabled(false);
            world.getSettings().setStepFrequency(1f / 60f);
            
            world.addListener(new CollisionListenerImpl(collisionHandler));
            
            rootNode.attachChild(player.spatial);
            rootNode.attachChild(starFieldGeom);
        }
        
        sim = new DynSim(world, this);
        simThread = new Thread(sim);
        simThread.start();
    }
    
    @Override
    public void update(float tpf) {
        if (!SpaceRocks.PAUSED.get()) {
            while(sim.updating())
                continue;
            
            rootNode.updateLogicalState(tpf);
            collisionHandler.handleCollisions();
            
            if (player.spatial.getLocalTranslation().x > MAXTRAVEL
                    || player.spatial.getLocalTranslation().x < -MAXTRAVEL
                    || player.spatial.getLocalTranslation().z > MAXTRAVEL
                    || player.spatial.getLocalTranslation().z < -MAXTRAVEL)
                centerScene(player.spatial.getLocalTranslation().clone());
            
            if (player.getHealth() < Tools.EPSILON && hud.isEnabled()) {
                hud.setEnabled(false);
                hud.disableFiring();
                player.spatial.removeFromParent();
                sim.getWorld().removeBody(player.body);
                aTrack.setTracking(false);
                player.destroy();
            } else if (hud.isEnabled())
                player.updatePlayer(tpf);
            
            aTrack.update(tpf);
            
            player.updateLasers(tpf);
            
            sim.setUpdating(tpf);
            starField.updateStars(app.getCamera().getLocation());
            player.updateTrail(tpf);
        } else {
            while(sim.updating())
                continue;
            sim.setTpf(tpf);
        }
    }
    
    public void addSimulatedBody(BodyCont bodyCont) {
        collideNode.attachChild(bodyCont.getSpatial());
        sim.getWorld().addBody(bodyCont.getBody());
        
        if (bodyCont instanceof AstCont)
            aTrack.addAsteroid();
    }
    
    public void removeSimulatedBody(BodyCont bodyCont) {
        if (collideNode.detachChild(bodyCont.getSpatial()) < 0)
            return;
        
        sim.getWorld().removeBody(bodyCont.getBody());
        
        if (bodyCont instanceof AstCont)
            aTrack.removeAsteroid();
    }
    
    public void centerScene(Vector3f currentCenter) {
        for (Spatial s : collideNode.getChildren()) {
            Vector3f loc = s.getLocalTranslation();
            Vector2f newLoc = new Vector2f(loc.x - currentCenter.x, loc.z - currentCenter.z);
            s.setLocalTranslation(newLoc.x, 0, newLoc.y);
            
            BodyCont bc = s.getControl(BodyCont.class);
            if (bc != null) {
                Body body = bc.getBody();
                body.getTransform().setTranslationX(newLoc.x);
                body.getTransform().setTranslationY(newLoc.y);
                
                /*if (bc instanceof PlayerCont) {
                    loc = app.getCamera().getLocation();
                    loc.x = newLoc.x;
                    loc.z = newLoc.y;
                    app.getCamera().setLocation(loc);
                }*/
            }
        }
        
        for (Spatial s : noCollideNode.getChildren()) {
            Vector3f loc = s.getLocalTranslation();
            Vector2f newLoc = new Vector2f(loc.x - currentCenter.x, loc.z - currentCenter.z);
            s.setLocalTranslation(newLoc.x, 0, newLoc.y);
        }
        
        Vector3f newLoc = app.getCamera().getLocation();
        newLoc.x -= currentCenter.x;
        newLoc.z -= currentCenter.z;
        player.body.getTransform().setTranslationX(newLoc.x);
        player.body.getTransform().setTranslationY(newLoc.z);
        player.spatial.setLocalTranslation(newLoc.x, 0, newLoc.z);
        app.getCamera().setLocation(newLoc);
        
        //starFieldGeom.setLocalTranslation(0, 0, 0);
        starField.centerStarField(currentCenter);
        player.reCenterTrail(currentCenter);
        player.reCenterLasers(currentCenter);
    }
    
    public void quit() {
        final GameState gState = this;
        hud.setEnabled(false);
        Music.fadeOut(1, new Callback() {
            @Override
            public void call() {
                Music.playMenuTrack();
            }
        });
        UI.fadeOut(new Callback() {
            @Override
            public void call() {
                SoundFX.stopAllSounds();
                sim.active.set(false);
                rootNode.removeLight(directional);
                rootNode.removeLight(ambient);
                rootNode.detachAllChildren();
                sim.getWorld().removeAllBodies();
                app.getStateManager().detach(gState);
                hud.quit();
                UI.container.clearChildren();
                
                MainMenuEffects.intantiateEffects(app);
                
                UI.fadeIn(new Callback() {
                    @Override
                    public void call() {
                        SpaceRocks.PAUSED.set(false);
                        UI.displayMainMenu();
                    }
                });
            }
        });
    }
    
    public void destroy() {
        if (sim != null)
            sim.active.set(false);
        SoundFX.stopAllSounds();
    }
}
