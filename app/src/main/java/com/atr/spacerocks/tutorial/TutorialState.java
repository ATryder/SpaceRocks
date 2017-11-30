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
package com.atr.spacerocks.tutorial;

import com.atr.math.GMath;
import com.atr.spacerocks.SpaceRocks;
import com.atr.spacerocks.control.MainMenuEffects;
import com.atr.spacerocks.dynamo.UIAnimator;
import com.atr.spacerocks.dynamo.UIAnimator.DIR;
import com.atr.spacerocks.effects.starfield.StarField;
import com.atr.spacerocks.shape.Arrow;
import com.atr.spacerocks.sound.SoundFX;
import static com.atr.spacerocks.state.GameState.FIELDHEIGHT;
import com.atr.spacerocks.ui.HUD;
import com.atr.spacerocks.ui.UI;
import com.atr.spacerocks.util.Asteroid;
import com.atr.spacerocks.util.Callback;
import com.atr.spacerocks.util.JmeToHarness;
import com.jme3.app.state.AbstractAppState;
import com.jme3.light.AmbientLight;
import com.jme3.light.DirectionalLight;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.FastMath;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.renderer.Camera;
import com.jme3.renderer.queue.RenderQueue;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.Spatial.CullHint;
import com.simsilica.lemur.Button;
import com.simsilica.lemur.Command;
import com.simsilica.lemur.Container;
import com.simsilica.lemur.FillMode;
import com.simsilica.lemur.GuiGlobals;
import com.simsilica.lemur.Insets3f;
import com.simsilica.lemur.Label;
import com.simsilica.lemur.component.HBoxLayout;
import com.simsilica.lemur.component.HBoxLayout.VAlign;
import com.simsilica.lemur.component.VBoxLayout;
import com.simsilica.lemur.component.VBoxLayout.HAlign;
import java.util.LinkedList;
import com.atr.jme.font.util.StringContainer;

/**
 *
 * @author Adam T. Ryder
 * <a href="http://1337atr.weebly.com">http://1337atr.weebly.com</a>
 */
public class TutorialState extends AbstractAppState {
    private static final ColorRGBA arrowCol1 = new ColorRGBA(0.5f, 0f, 1, 1f);
    private static final ColorRGBA arrowCol2 = new ColorRGBA(0.8f, 0.3f, 1, 1f);
    
    private final SpaceRocks app;
    private final HUD hud;
    
    private final Node rootNode;
    
    private final DirectionalLight directional;
    private final AmbientLight ambient;
    
    private final GuiGlobals gui;
    
    private int currentLesson = 0;
    private boolean quitting = false;
    
    private final Button nextButton;
    private final Button previousButton;
    private final Container container;
    private final Container buttonContainer;
    private final Label label = new Label("");
    
    private final JmeToHarness res;
    
    private final Node node = new Node("Tutorial Node");
    
    private final Material arrowMat;
    private final LinkedList<Geometry> arrows = new LinkedList<Geometry>();
    
    private TutorialBlackHole tutorialBlackHole;
    
    private final Command<Button> nextCommand = new Command<Button>() {
        @Override
        public void execute(Button source) {
            if (!UI.isEnabled())
                return;
            disable();
            
            container.addControl(new UIAnimator(false, DIR.RIGHT, 1f, 0f,
                    GuiGlobals.getInstance().dpInt(64), new Callback() {
                        @Override
                        public void call() {
                            nextLesson();
                        }
                    }));
        }
    };
    
    private final Command<Button> previousCommand = new Command<Button>() {
        @Override
        public void execute(Button source) {
            if (!UI.isEnabled())
                return;
            disable();
            
            container.addControl(new UIAnimator(false, DIR.RIGHT, 1f, 0f,
                    GuiGlobals.getInstance().dpInt(64), new Callback() {
                        @Override
                        public void call() {
                            previousLesson();
                        }
                    }));
        }
    };
    
    public TutorialState(SpaceRocks app) {
        this.app = app;
        res = SpaceRocks.res;
        hud = new HUD(app, null);
        hud.setEnabled(false);
        
        gui = GuiGlobals.getInstance();
        
        rootNode = app.getRootNode();
        
        directional = new DirectionalLight();
        directional.setDirection(new Vector3f(0.5859197f, -0.7671219f, 0.261935f));
        directional.setColor(new ColorRGBA(1f, 1f, 1f, 1f));
        rootNode.addLight(directional);
        
        ambient = new AmbientLight();
        ambient.setColor(new ColorRGBA(0.18f, 0.18f, 0.18f, 1f));
        rootNode.addLight(ambient);
        
        Container cont = new Container(new VBoxLayout(0, FillMode.None, false,
                HAlign.Center, true));
        cont.setPreferredSize(new Vector3f(app.getWidth() * 0.67f, app.getHeight(), 50));
        
        container = new Container(new VBoxLayout(gui.dpInt(10), FillMode.Last, false,
                HAlign.Left, true));
        container.setPadding(new Insets3f(gui.dpInt(32), 0, 0, 0));
        
        label.setMaxWidth(app.getWidth() * 0.67f);
        label.setWrapMode(StringContainer.WrapMode.Word);
        
        nextButton = new Button(res.getString("next"));
        nextButton.addClickCommands(nextCommand);
        previousButton = new Button(res.getString("previous"));
        previousButton.addClickCommands(previousCommand);
        disable();
        
        buttonContainer = new Container(new HBoxLayout(gui.dpInt(5), FillMode.None, false,
                VAlign.Top, true));
        
        buttonContainer.addChild(previousButton);
        buttonContainer.addChild(nextButton);
        
        container.addChild(label, false, false);
        container.addChild(buttonContainer, HAlign.Right, false, false);
        cont.addChild(container);
        
        UI.container.addChild(cont);
        
        Camera gameCam = app.getCamera();
        float fov = 60f;
        float altitude = GMath.fastCamAltitude(fov, FIELDHEIGHT);
        float near = altitude - 100;
        float far = altitude + 100;
        gameCam.setFrustumPerspective(fov, (float)gameCam.getWidth() / gameCam.getHeight(), near, far);
        gameCam.setLocation(new Vector3f(0, altitude, 0));
        gameCam.setRotation(new Quaternion(new float[]{1.5708176978793961078628152543852f, 0f, 0f}));
        
        StarField starField = new StarField(gameCam, fov);
        Geometry starFieldGeom = starField.createStarField();
        Material starMat = new Material(app.getAssetManager(), "MatDefs/Unshaded/circle_distance.j3md");
        starMat.setColor("Color", new ColorRGBA(1, 1, 1, 0.75f));
        starMat.setFloat("minOffset", 0.1f);
        starMat.setFloat("maxOffset", 0.99f);
        starMat.setFloat("minDist", altitude - 100);
        starMat.setFloat("Focus", altitude + 10);
        starFieldGeom.setMaterial(starMat);
        
        rootNode.attachChild(starFieldGeom);
        rootNode.attachChild(node);
        node.setCullHint(CullHint.Dynamic);
        
        arrowMat = new Material(app.getAssetManager(), "MatDefs/Unshaded/arrow.j3md");
        arrowMat.setBoolean("useAA", gui.isSupportDerivatives());
        arrowMat.setFloat("Percent", 0f);
        
        displayLesson();
    }
    
    private void enable() {
        if (quitting)
                return;
        if (nextButton != null)
            nextButton.setEnabled(true);
        if (previousButton != null)
            previousButton.setEnabled(true);
    }
    
    private void disable() {
        if (nextButton != null)
            nextButton.setEnabled(false);
        if (previousButton != null)
            previousButton.setEnabled(false);
    }
    
    private void nextLesson() {
        currentLesson++;
        displayLesson();
    }
    
    private void previousLesson() {
        currentLesson--;
        displayLesson();
    }
    
    private void displayLesson() {
        nextButton.setText(res.getString("next"));
        Geometry arrow;
        Vector3f v1;
        Vector3f v2;
        Spatial spatial;
        switch(currentLesson) {
            case 0:
                label.setText(res.getString("tutorial0"));
                node.detachAllChildren();
                break;
            case 1:
                node.detachAllChildren();
                clearArrows();
                
                label.setText(res.getString("tutorial1"));
                app.player.setLocalTranslation(0, 0, -22);
                app.player.setLocalRotation(
                        app.player.getLocalRotation()
                                .fromAngles(0, 0, 0));
                node.attachChild(app.player);
                
                new TutorialSparks(15, 7, app, new Vector3f(0, 0, -22));
                new TutorialFlash(app, new Vector3f(0, 0, -22), 16);
                
                arrow = new Geometry("arrow",
                        new Arrow(3, 8, 6, 4, arrowCol1, arrowCol2, false));
                arrow.setLocalTranslation(0, 0, -1);
                arrow.rotate(0, FastMath.PI, 0);
                arrow.addControl(new ArrowCont());
                arrow.setQueueBucket(RenderQueue.Bucket.Transparent);
                arrowMat.setFloat("Percent", 0);
                arrow.setMaterial(arrowMat);
                arrows.add(arrow);
                node.attachChild(arrow);
                break;
            case 2:
                clearArrows();
                label.setText(res.getString("tutorial2"));
                
                arrow = new Geometry("arrow",
                        new Arrow(gui.dpInt(8), gui.dpInt(16), gui.dpInt(4), gui.dpInt(8),
                                arrowCol1, arrowCol2, true));
                arrow.rotate(0, 0, FastMath.PI);
                v1 = hud.getThumbStickPos();
                v2 = hud.getThumbStickSize();
                arrow.setLocalTranslation(v1.x + (v2.x / 2),
                        v1.y + gui.dpInt(17), 0);
                arrow.addControl(new ArrowCont());
                arrowMat.setFloat("Percent", 0);
                arrow.setMaterial(arrowMat);
                arrows.add(arrow);
                app.getGuiNode().attachChild(arrow);
                break;
            case 3:
                clearArrows();
                label.setText(res.getString("tutorial3"));
                
                arrow = new Geometry("arrow",
                        new Arrow(gui.dpInt(8), gui.dpInt(16), gui.dpInt(4), gui.dpInt(8),
                                arrowCol1, arrowCol2, true));
                arrow.rotate(0, 0, FastMath.PI);
                v1 = hud.getFireButtonPos();
                v2 = hud.getButtonSize();
                arrow.setLocalTranslation(v1.x + (v2.x / 2),
                        v1.y + gui.dpInt(17), 0);
                arrow.addControl(new ArrowCont());
                arrowMat.setFloat("Percent", 0);
                arrow.setMaterial(arrowMat);
                arrows.add(arrow);
                app.getGuiNode().attachChild(arrow);
                break;
            case 4:
                clearArrows();
                label.setText(res.getString("tutorial4"));
                
                arrow = new Geometry("arrow",
                        new Arrow(gui.dpInt(8), gui.dpInt(16), gui.dpInt(4), gui.dpInt(8),
                                arrowCol1, arrowCol2, true));
                arrow.rotate(0, 0, FastMath.PI);
                v1 = hud.getBombButtonPos();
                v2 = hud.getButtonSize();
                arrow.setLocalTranslation(v1.x + (v2.x / 2),
                        v1.y + gui.dpInt(17), 0);
                arrow.addControl(new ArrowCont());
                arrowMat.setFloat("Percent", 0);
                arrow.setMaterial(arrowMat);
                arrows.add(arrow);
                app.getGuiNode().attachChild(arrow);
                break;
            case 5:
                clearArrows();
                label.setText(res.getString("tutorial5"));
                
                arrow = new Geometry("arrow",
                        new Arrow(gui.dpInt(8), gui.dpInt(16), gui.dpInt(4), gui.dpInt(8),
                                arrowCol1, arrowCol2, true));
                arrow.rotate(0, 0, -FastMath.PI * 0.5f);
                v1 = hud.getBombButtonPos();
                v2 = hud.getButtonSize();
                arrow.setLocalTranslation(v1.x + v2.x - gui.dpInt(17),
                        app.getHeight() / 2, 0);
                arrow.addControl(new ArrowCont());
                arrowMat.setFloat("Percent", 0);
                arrow.setMaterial(arrowMat);
                arrows.add(arrow);
                app.getGuiNode().attachChild(arrow);
                break;
            case 6:
                clearArrows();
                node.detachAllChildren();
                label.setText(res.getString("tutorial6"));
                app.player.setLocalTranslation(0, 0, -22);
                app.player.setLocalRotation(
                        app.player.getLocalRotation()
                                .fromAngles(0, 0, 0));
                node.attachChild(app.player);
                
                arrow = new Geometry("arrow",
                        new Arrow(gui.dpInt(8), gui.dpInt(16), gui.dpInt(4), gui.dpInt(8),
                                arrowCol1, arrowCol2, true));
                arrow.rotate(0, 0, FastMath.PI * 0.5f);
                v1 = hud.getThumbStickPos();
                arrow.setLocalTranslation(v1.x + gui.dpInt(17),
                        app.getHeight() / 2, 0);
                arrow.addControl(new ArrowCont());
                arrowMat.setFloat("Percent", 0);
                arrow.setMaterial(arrowMat);
                arrows.add(arrow);
                app.getGuiNode().attachChild(arrow);
                break;
            case 7:
                clearArrows();
                node.detachAllChildren();
                label.setText(res.getString("tutorial7"));
                
                v1 = new Vector3f(24, 0, -22);
                spatial = app.PUP_Energy.clone(false);
                spatial.setLocalTranslation(v1);
                spatial.rotate(0, FastMath.PI, 0);
                new TutorialSparks(15, 7, app, v1);
                new TutorialFlash(app, v1, 16);
                node.attachChild(spatial);
                
                v1.set(0, 0, -22);
                spatial = app.PUP_Health.clone(false);
                spatial.setLocalTranslation(v1);
                spatial.rotate(0, -FastMath.PI / 2, 0);
                new TutorialSparks(15, 7, app, v1);
                new TutorialFlash(app, v1, 16);
                node.attachChild(spatial);
                
                v1.set(-24, 0, -22);
                spatial = app.PUP_Sonic.clone(false);
                spatial.setLocalTranslation(v1);
                new TutorialSparks(15, 7, app, v1);
                new TutorialFlash(app, v1, 16);
                node.attachChild(spatial);
                break;
            case 8:
                clearArrows();
                label.setText(res.getString("tutorial8"));
                
                arrow = new Geometry("arrow",
                        new Arrow(3, 8, 6, 4, arrowCol1, arrowCol2, false));
                arrow.setLocalTranslation(24, 0, -2);
                arrow.rotate(0, FastMath.PI, 0);
                arrow.addControl(new ArrowCont());
                arrow.setQueueBucket(RenderQueue.Bucket.Transparent);
                arrowMat.setFloat("Percent", 0);
                arrow.setMaterial(arrowMat);
                arrows.add(arrow);
                node.attachChild(arrow);
                break;
            case 9:
                label.setText(res.getString("tutorial9"));
                arrow = arrows.get(0);
                arrow.setLocalTranslation(0, 0, -2);
                break;
            case 10:
                label.setText(res.getString("tutorial10"));
                clearArrows();
                node.detachAllChildren();
                
                v1 = new Vector3f(24, 0, -22);
                spatial = app.PUP_Energy.clone(false);
                spatial.setLocalTranslation(v1);
                spatial.rotate(0, FastMath.PI, 0);
                node.attachChild(spatial);
                
                v1.set(0, 0, -22);
                spatial = app.PUP_Health.clone(false);
                spatial.setLocalTranslation(v1);
                spatial.rotate(0, -FastMath.PI / 2, 0);
                node.attachChild(spatial);
                
                v1.set(-24, 0, -22);
                spatial = app.PUP_Sonic.clone(false);
                spatial.setLocalTranslation(v1);
                node.attachChild(spatial);
                
                arrow = new Geometry("arrow",
                        new Arrow(3, 8, 6, 4, arrowCol1, arrowCol2, false));
                arrow.setLocalTranslation(-24, 0, -2);
                arrow.rotate(0, FastMath.PI, 0);
                arrow.addControl(new ArrowCont());
                arrow.setQueueBucket(RenderQueue.Bucket.Transparent);
                arrowMat.setFloat("Percent", 0);
                arrow.setMaterial(arrowMat);
                arrows.add(arrow);
                node.attachChild(arrow);
                break;
            case 11:
                if (tutorialBlackHole != null) {
                    tutorialBlackHole.deactivate();
                    tutorialBlackHole = null;
                }
                clearArrows();
                node.detachAllChildren();
                label.setText(res.getString("tutorial11"));
                
                v1 = new Vector3f(Asteroid.maxScale * 4, 0, -22);
                spatial = app.asteroids[3].getSpatial();
                ((Node)spatial).attachChild(app.redAsteroidPup.clone(false));
                spatial.setLocalScale(Asteroid.maxScale);
                spatial.setLocalTranslation(v1);
                new TutorialSparks(15, 7, app, v1);
                new TutorialFlash(app, v1, 16);
                node.attachChild(spatial);
                
                v1.set(-Asteroid.maxScale * 4, 0, -22);
                spatial = app.asteroids[1].getSpatial();
                ((Node)spatial).attachChild(app.blueAsteroidPup.clone(false));
                spatial.setLocalScale(Asteroid.maxScale);
                spatial.setLocalTranslation(v1);
                new TutorialSparks(15, 7, app, v1);
                new TutorialFlash(app, v1, 16);
                node.attachChild(spatial);
                break;
            case 12:
                if (tutorialBlackHole == null) {
                    tutorialBlackHole = new TutorialBlackHole(Vector3f.ZERO, app);
                }
                clearArrows();
                node.detachAllChildren();
                label.setText(res.getString("tutorial12"));
                break;
            case 13:
                if (tutorialBlackHole == null) {
                    tutorialBlackHole = new TutorialBlackHole(Vector3f.ZERO, app);
                }
                label.setText(res.getString("tutorial13"));
                break;
            case 14:
                if (tutorialBlackHole != null) {
                    tutorialBlackHole.deactivate();
                    tutorialBlackHole = null;
                }
                label.setText(res.getString("tutorial14"));
                break;
            default:
                quit();
        }
        
        if (quitting)
            return;
        
        container.addControl(new UIAnimator(true, DIR.LEFT, 1f, 0f,
                GuiGlobals.getInstance().dpInt(64), new Callback() {
                    @Override
                    public void call() {
                        enable();
                    }
                }));
    }
    
    private void clearArrows() {
        for (Geometry arrow : arrows)
            arrow.removeFromParent();
        arrows.clear();
    }
    
    public void quit() {
        final TutorialState tState = this;
        quitting = true;
        disable();
        UI.fadeOut(new Callback() {
            @Override
            public void call() {
                SoundFX.stopAllSounds();
                clearArrows();
                rootNode.removeLight(directional);
                rootNode.removeLight(ambient);
                rootNode.detachAllChildren();
                app.getStateManager().detach(tState);
                hud.quit();
                UI.container.clearChildren();
                
                MainMenuEffects.intantiateEffects(app);
                
                UI.fadeIn(new Callback() {
                    @Override
                    public void call() {
                        UI.displayMainMenu();
                    }
                });
            }
        });
    }
    
    public void destroy() {
        SoundFX.stopAllSounds();
    }
}
