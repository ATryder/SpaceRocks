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
package com.atr.spacerocks;

import com.atr.math.GMath;
import com.atr.spacerocks.sound.Music;
import com.atr.spacerocks.state.Initialize;
import com.atr.spacerocks.state.Fade;
import com.atr.spacerocks.state.GameState;
import com.atr.spacerocks.tutorial.TutorialState;
import com.atr.spacerocks.util.Asteroid;
import com.atr.spacerocks.util.JmeToHarness;
import com.atr.spacerocks.util.Options;
import com.atr.spacerocks.util.Options.Detail;
import com.jme3.app.FlyCamAppState;
import com.jme3.app.SimpleApplication;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.renderer.Camera;
import com.jme3.renderer.RenderManager;
import com.jme3.renderer.ViewPort;
import com.jme3.renderer.queue.RenderQueue;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.simsilica.lemur.GuiGlobals;
import com.simsilica.lemur.event.BasePickState;
import java.util.concurrent.atomic.AtomicBoolean;
import org.dyn4j.dynamics.Body;

/**
 *
 * @author Adam T. Ryder
 * <a href="http://1337atr.weebly.com">http://1337atr.weebly.com</a>
 */
public class SpaceRocks extends SimpleApplication {
    public static AtomicBoolean PAUSED = new AtomicBoolean(false);
    public static int width;
    public static int height;
    
    public static JmeToHarness res;
    
    public static Camera uiCam = new Camera();
    public static ViewPort uiView;
    public static final Node uiNode = new Node("3D UI Scene") {
        @Override
        public int attachChildAt(Spatial child, int index) {
            uiView.setEnabled(true);
            return super.attachChildAt(child, index);
        }
        
        @Override
        public Spatial detachChildAt(int index) {
            Spatial child = super.detachChildAt(index);
            uiView.setEnabled(!this.getChildren().isEmpty());
            
            return child;
        }
    };
    
    private static final AtomicBoolean phySync = new AtomicBoolean(false);
    
    private final Node node = new Node("Game Scene");
    
    public Spatial player;
    public Body playerBody;
    public Asteroid[] asteroids;
    public Node blueAsteroidPup;
    public Node redAsteroidPup;
    
    public Node PUP_Health;
    public Node PUP_Sonic;
    public Node PUP_Energy;

    public static void main(String[] args) {
    }
    
    public void setJmeToHarness(JmeToHarness toHarness) {
        res = toHarness;
    }
    
    public int getWidth() {
        return width;
    }
    
    public int getHeight() {
        return height;
    }
    
    public Node getUINode() {
        return uiNode;
    }
    
    public ViewPort getUIView() {
        return uiView;
    }
    
    public Camera getUICam() {
        return uiCam;
    }

    @Override
    public void simpleInitApp() {
        res.loadPrefs();
        setDisplayStatView(false);
        setDisplayFps(false);
        setPauseOnLostFocus(true);
        stateManager.detach(stateManager.getState(FlyCamAppState.class));
        
        Music.initialize(assetManager);
        Music.playMenuTrack();
        
        width = settings.getWidth();
        height = settings.getHeight();
        
        //stateManager.attach(new Fade(guiViewPort.getCamera(), assetManager, renderManager));
        
        float fov = 49.134f;
        float altitude = GMath.fastCamAltitude(fov, height);
        //float altitude = altitude = (height / 2f) / (float)Math.tan((fov / 2) * FastMath.DEG_TO_RAD);
        
        uiNode.setQueueBucket(RenderQueue.Bucket.Transparent);
        uiCam = new Camera(width, height);
        uiCam.setFrustumPerspective(fov, width / (float)height,
                altitude >= 401 ? altitude - 400 : 1, altitude + 400);
        uiCam.setLocation(new Vector3f(width / 2f, height / 2f, altitude));
        uiCam.setRotation(new Quaternion(0f, 1f, 0f, 0f));
        uiView = renderManager.createPostView("3D UI ViewPort", uiCam);
        uiView.setBackgroundColor(ColorRGBA.BlackNoAlpha);
        uiView.setClearFlags(false, true, true);
        uiView.attachScene(uiNode);

        GuiGlobals.initialize(this);
        GuiGlobals gui = GuiGlobals.getInstance();
        Options.supportDerivatives = gui.isSupportDerivatives();
        gui.setSupportDerivatives(Options.supportDerivatives && Options.getUIDetail() == Detail.High);
        BasePickState bps = stateManager.getState(BasePickState.class);
        bps.setIncludeDefaultCollisionRoots(false);
        bps.addCollisionRoot(guiViewPort, BasePickState.PICK_LAYER_GUI);
        bps.addCollisionRoot(uiView, BasePickState.PICK_LAYER_GUI);
        gui.setupGuiComparators(uiView);
        gui.setupGuiComparators(guiViewPort);
        gui.getStyles().setDefaultStyle("spacerocks");
        
        node.setCullHint(Spatial.CullHint.Dynamic);
        viewPort.clearScenes();
        viewPort.attachScene(node);
        viewPort.setBackgroundColor(ColorRGBA.Black);
        viewPort.setClearFlags(true, true, true);
        
        stateManager.attach(new Initialize(this));
        stateManager.attach(new Fade(guiViewPort.getCamera(), renderManager));
    }
    
    public void setMesh(Spatial player, Body playerBody, Asteroid[] asteroids,
            Node blueAsteroidPup, Node redAsteroidPup, Node PUP_Health,
            Node PUP_Sonic, Node PUP_Energy) {
        this.player = player;
        this.playerBody = playerBody;
        this.asteroids = asteroids;
        this.redAsteroidPup = redAsteroidPup;
        this.blueAsteroidPup = blueAsteroidPup;
        
        this.PUP_Health = PUP_Health;
        this.PUP_Sonic = PUP_Sonic;
        this.PUP_Energy = PUP_Energy;
    }

    @Override
    public void simpleUpdate(float tpf) {
        Music.update(tpf);
        
        if (uiView.isEnabled()) {
            uiNode.updateLogicalState(tpf);
            uiNode.updateGeometricState();
        }
        
        if (stateManager.getState(GameState.class) == null)
            node.updateLogicalState(tpf);
        node.updateGeometricState();
    }

    @Override
    public void simpleRender(RenderManager rm) {
        //TODO: add render code
    }
    
    @Override
    public Node getRootNode() {
        return node;
    }
    
    /**
     * Called when the application is resumed after having lost focus and after
     * having been initially created. Called from onResume()
     */
    @Override
    public void gainFocus() {
        super.gainFocus();
        /*this.enqueue(new Callable<Object>() {
            @Override
            public Object call() {
                res.loadPrefs();
                GuiGlobals.getInstance().setSupportDerivatives(Options.supportDerivatives
                        && Options.getUIDetail() == Detail.High);
                
                return null;
            }
        });*/
    }
    
    /**
     * Called when the application restarts after having been stopped such as when
     * the device is locked. Re-load fonts here. Called from onRestart()
     */
    @Override
    public void restart() {
        super.restart();
        //Re-load stuff here
        //stateManager.attach(new Restart(this));
    }
    
    @Override
    public void destroy() {
        GameState gameState = stateManager.getState(GameState.class);
        if (gameState != null) {
            gameState.destroy();
            stateManager.detach(gameState);
        } else if (stateManager.getState(TutorialState.class) != null) {
            stateManager.getState(TutorialState.class).destroy();
            stateManager.detach(stateManager.getState(TutorialState.class));
        }
        stateManager.detach(stateManager.getState(Fade.class));
        node.detachAllChildren();
        uiNode.detachAllChildren();
        guiNode.detachAllChildren();
        
        super.destroy();
    }
}
