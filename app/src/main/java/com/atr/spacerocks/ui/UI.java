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
package com.atr.spacerocks.ui;

import com.atr.math.GMath;
import com.atr.spacerocks.state.Fade;
import com.atr.spacerocks.SpaceRocks;
import com.atr.spacerocks.dynamo.UIAnimator;
import com.atr.spacerocks.dynamo.UIAnimator.DIR;
import com.atr.spacerocks.dynamo.component.LetterPicker;
import com.atr.spacerocks.sound.Music;
import com.atr.spacerocks.sound.SoundFX;
import com.atr.spacerocks.sound.SoundNode;
import com.atr.spacerocks.state.Credits;
import com.atr.spacerocks.state.GameState;
import com.atr.spacerocks.state.Initialize;
import com.atr.spacerocks.tutorial.TutorialState;
import static com.atr.spacerocks.ui.UI.container;
import com.atr.spacerocks.util.Callback;
import com.atr.spacerocks.util.JmeToHarness;
import com.atr.spacerocks.util.Options;
import com.atr.spacerocks.util.Options.Detail;
import com.atr.spacerocks.util.TopGun;
import com.atr.spacerocks.util.TopGuns;
import com.atr.spacerocks.util.Tracker;
import com.jme3.math.ColorRGBA;
import com.jme3.math.FastMath;
import com.jme3.math.Vector3f;
import com.jme3.renderer.RenderManager;
import com.jme3.renderer.ViewPort;
import com.jme3.scene.Node;
import com.jme3.scene.control.AbstractControl;
import com.simsilica.lemur.Button;
import com.simsilica.lemur.ComboBox;
import com.simsilica.lemur.Command;
import com.simsilica.lemur.Container;
import com.simsilica.lemur.FillMode;
import com.simsilica.lemur.GuiGlobals;
import com.simsilica.lemur.Insets3f;
import com.simsilica.lemur.Label;
import com.simsilica.lemur.Slider;
import com.simsilica.lemur.Spacer;
import com.simsilica.lemur.component.HBoxLayout;
import com.simsilica.lemur.component.HBoxLayout.VAlign;
import com.simsilica.lemur.component.StackLayout;
import com.simsilica.lemur.component.VBoxLayout;
import com.simsilica.lemur.component.VBoxLayout.HAlign;
import com.simsilica.lemur.style.ElementId;

/**
 *
 * @author Adam T. Ryder
 * <a href="http://1337atr.weebly.com">http://1337atr.weebly.com</a>
 */
public final class UI {
    private static SpaceRocks sr;
    private static Fade fade;
    
    private static boolean enabled = true;
    
    private static JmeToHarness res;
    
    private static GuiGlobals gui;
    
    public static final StackLayout stack = new StackLayout(0.03f, false) {
        @Override
        public <T extends Node> T addChild(T n, Object[] constraints) {
            SpaceRocks.uiView.setEnabled(true);
            
            return super.addChild(n, constraints);
        }
        
        @Override
        public void removeChild(Node n) {
            super.removeChild(n);
            SpaceRocks.uiView.setEnabled(!this.getChildren().isEmpty());
        }
        
        @Override
        public void clearChildren() {
            super.clearChildren();
            SpaceRocks.uiView.setEnabled(!this.getChildren().isEmpty());
        }
    };
    
    public static final Container container = new Container(stack);
    
    public static void initialize(SpaceRocks app) {
        sr = app;
        res = SpaceRocks.res;
        fade = app.getStateManager().getState(Fade.class);
        gui = GuiGlobals.getInstance();
        
        container.setPreferredSize(new Vector3f(app.getWidth(), app.getHeight(), 100));
        container.setLocalTranslation(-app.getWidth() / 2f, app.getHeight() / 2f, 0);
        
        Node wobble = new Node("UI Wobble x-axis");
        wobble.setLocalTranslation(app.getWidth() / 2f, app.getHeight() / 2f, 0);
        wobble.addControl(new UIWobble(wobble, container));
        
        app.getUINode().attachChild(wobble);
        
        displayMainMenu();
    }
    
    public static void setEnabled(boolean e) {
        enabled = e;
        if (sr.getStateManager().getState(GameState.class) == null)
            return;
        
        if (!sr.getUIView().isEnabled() || !enabled)
            sr.getStateManager().getState(GameState.class).getHUD().setEnabled(enabled);
    }
    
    public static boolean isEnabled() {
        return enabled;
    }
    
    public static void fadeIn(Callback cb) {
        fade.fadeIn(cb);
    }
    
    public static void fadeOut(Callback cb) {
        fade.fadeOut(cb);
    }
    
    public static void fade(boolean fadeIn, float fadeLength, Callback cb) {
        fade.fade(fadeIn, fadeLength, cb);
    }
    
    private static void startTutorial() {
        fadeOut(new Callback() {
            @Override
            public void call() {
                sr.getRootNode().detachAllChildren();
                container.clearChildren();
                sr.getStateManager().attach(new TutorialState(sr));
                
                fadeIn(null);
            }
        });
    }
    
    private static void startGame() {
        Music.fadeOut(1, new Callback() {
            @Override
            public void call() {
                Music.playGameTracks();
            }
        });
        fadeOut(new Callback() {
            @Override
            public void call() {
                sr.getRootNode().detachAllChildren();
                container.clearChildren();
                sr.getStateManager().attach(new GameState(sr));
                
                fadeIn(null);
            }
        });
    }
    
    public static void displayMainMenu() {
        container.clearChildren();
        
        final Container h = new Container(new HBoxLayout(gui.dpInt(15),
                FillMode.None, false, VAlign.Center, true));
        final Container v1 = new Container(new VBoxLayout(gui.dpInt(15),
                FillMode.None, true, HAlign.Left, true));
        final Container v2 = new Container(new VBoxLayout(gui.dpInt(15),
                FillMode.None, true, HAlign.Left, true));
        h.addChild(v1);
        h.addChild(v2);
        container.addChild(h);
        
        final Button playButton = new Button(res.getString("start_game"));
        final Button tutorialButton = new Button(res.getString("tutorial"));
        final Button topGunButton = new Button(res.getString("topguns"));
        final Button optionsButton = new Button(res.getString("options"));
        final Button creditsButton = new Button(res.getString("credits"));
        final Button donateButton = new Button(res.getString("donate"));
        
        playButton.addClickCommands(new Command<Button>() {
            @Override
            public void execute(Button source) {
                if (!enabled)
                    return;
                removeMainMenu(new Callback() {
                    @Override
                    public void call() {
                        startGame();
                    }
                });
            }
        });
        
        tutorialButton.addClickCommands(new Command<Button>() {
            @Override
            public void execute(Button source) {
                if (!enabled)
                    return;
                removeMainMenu(new Callback() {
                    @Override
                    public void call() {
                        startTutorial();
                    }
                });
            }
        });
        
        topGunButton.addClickCommands(new Command<Button>() {
            @Override
            public void execute(Button source) {
                if (!enabled)
                    return;
                removeMainMenu(new Callback() {
                    @Override
                    public void call() {
                        displayTopGuns(new Callback() {
                            @Override
                            public void call() {
                                displayMainMenu();
                            }
                        });
                    }
                });
            }
        });
        
        optionsButton.addClickCommands(new Command<Button>() {
            @Override
            public void execute(Button source) {
                if (!enabled)
                    return;
                removeMainMenu(new Callback() {
                    @Override
                    public void call() {
                        displayOptions(new Callback() {
                            @Override
                            public void call() {
                                displayMainMenu();
                            }
                        });
                    }
                });
            }
        });
        
        creditsButton.addClickCommands(new Command<Button>() {
            @Override
            public void execute(Button source) {
                if (!enabled)
                    return;
                removeMainMenu(new Callback() {
                    @Override
                    public void call() {
                        container.clearChildren();
                        sr.getStateManager().attach(new Credits(sr));
                    }
                });
            }
        });
        
        donateButton.addClickCommands(new Command<Button>() {
            @Override
            public void execute(Button source) {
                if (!enabled)
                    return;
                removeMainMenu(new Callback() {
                    @Override
                    public void call() {
                        displayDonateDialog();
                    }
                });
            }
        });
        
        playButton.setEnabled(false);
        tutorialButton.setEnabled(false);
        topGunButton.setEnabled(false);
        optionsButton.setEnabled(false);
        creditsButton.setEnabled(false);
        donateButton.setEnabled(false);
        v1.addChild(playButton);
        v1.addChild(tutorialButton);
        v1.addChild(topGunButton);
        v2.addChild(optionsButton);
        v2.addChild(creditsButton);
        v2.addChild(donateButton);
        
        Callback cb = new Callback() {
            @Override
            public void call() {
                playButton.setEnabled(true);
                tutorialButton.setEnabled(true);
                topGunButton.setEnabled(true);
                optionsButton.setEnabled(true);
                creditsButton.setEnabled(true);
                donateButton.setEnabled(true);
            }
        };
        
        playButton.addControl(new UIAnimator(true, DIR.LEFT, 0.5f, 0, 64, null));
        tutorialButton.addControl(new UIAnimator(true, DIR.LEFT, 0.5f, 0.1f, 64, null));
        topGunButton.addControl(new UIAnimator(true, DIR.LEFT, 0.5f, 0.2f, 64, null));
        optionsButton.addControl(new UIAnimator(true, DIR.RIGHT, 0.5f, 0.0f, 64, null));
        creditsButton.addControl(new UIAnimator(true, DIR.RIGHT, 0.5f, 0.1f, 64, null));
        donateButton.addControl(new UIAnimator(true, DIR.RIGHT, 0.5f, 0.2f, 64, cb));
    }
    
    private static void removeMainMenu(Callback callback) {
        Button playButton = (Button)((Container)((Container)container.getChild(0))
                .getChild(0)).getChild(0);
        Button tutorialButton = (Button)((Container)((Container)container.getChild(0))
                .getChild(0)).getChild(1);
        Button topGunButton = (Button)((Container)((Container)container.getChild(0))
                .getChild(0)).getChild(2);
        Button optionsButton = (Button)((Container)((Container)container.getChild(0))
                .getChild(1)).getChild(0);
        Button creditsButton = (Button)((Container)((Container)container.getChild(0))
                .getChild(1)).getChild(1);
        Button donateButton = (Button)((Container)((Container)container.getChild(0))
                .getChild(1)).getChild(2);
        
        playButton.setEnabled(false);
        tutorialButton.setEnabled(false);
        topGunButton.setEnabled(false);
        optionsButton.setEnabled(false);
        creditsButton.setEnabled(false);
        donateButton.setEnabled(false);
        
        playButton.addControl(new UIAnimator(false, DIR.LEFT, 0.5f, 0, 64, null));
        tutorialButton.addControl(new UIAnimator(false, DIR.LEFT, 0.5f, 0.1f, 64, null));
        topGunButton.addControl(new UIAnimator(false, DIR.LEFT, 0.5f, 0.2f, 64, null));
        optionsButton.addControl(new UIAnimator(false, DIR.RIGHT, 0.5f, 0.0f, 64, null));
        creditsButton.addControl(new UIAnimator(false, DIR.RIGHT, 0.5f, 0.1f, 64, null));
        donateButton.addControl(new UIAnimator(false, DIR.RIGHT, 0.5f, 0.2f, 64, callback));
    }
    
    public static void displayInGameMenu(final HUD hud) {
        container.clearChildren();
        hud.setEnabled(false);
        SpaceRocks.PAUSED.set(true);
        final Container c = new Container(new VBoxLayout(gui.dpInt(15),
                FillMode.None, true, HAlign.Center, true));
        
        
        final Button resume = new Button(res.getString("resume"));
        final Button volumeOptions = new Button(res.getString("volume"));
        final Button topGuns = new Button(res.getString("topguns"));
        final Button mainMenu = new Button(res.getString("menu"));
        
        resume.addClickCommands(new Command<Button>() {
            @Override
            public void execute(Button source) {
                if (!enabled)
                    return;
                removeInGameMenu(hud);
            }
        });
        
        topGuns.addClickCommands(new Command<Button>() {
            @Override
            public void execute(Button source) {
                if (!enabled)
                    return;
                removeInGameMenu(new Callback() {
                    @Override
                    public void call() {
                        displayTopGuns(new Callback() {
                            @Override
                            public void call() {
                                displayInGameMenu(hud);
                            }
                        });
                    }
                });
            }
        });
        
        volumeOptions.addClickCommands(new Command<Button>() {
            @Override
            public void execute(Button source) {
                if (!enabled)
                    return;
                removeInGameMenu(new Callback() {
                    @Override
                    public void call() {
                        displayVolume(new Callback() {
                            @Override
                            public void call() {
                                displayInGameMenu(hud);
                            }
                        });
                    }
                });
            }
        });
        
        mainMenu.addClickCommands(new Command<Button>() {
            @Override
            public void execute(Button source) {
                if (!enabled)
                    return;
                volumeOptions.setEnabled(false);
                mainMenu.setEnabled(false);
                resume.setEnabled(false);
                topGuns.setEnabled(false);
                
                sr.getStateManager().getState(GameState.class).quit();
            }
        });
        
        volumeOptions.setEnabled(false);
        mainMenu.setEnabled(false);
        resume.setEnabled(false);
        topGuns.setEnabled(false);
        
        c.addChild(resume);
        c.addChild(volumeOptions);
        c.addChild(topGuns);
        c.addChild(mainMenu);
        
        container.addChild(c);
        
        resume.addControl(new UIAnimator(true, DIR.LEFT, 0.5f, 0, 64, null));
        volumeOptions.addControl(new UIAnimator(true, DIR.LEFT, 0.5f, 0.1f, 64, null));
        topGuns.addControl(new UIAnimator(true, DIR.LEFT, 0.5f, 0.2f, 64, null));
        
        Callback cb = new Callback() {
            @Override
            public void call() {
                resume.setEnabled(true);
                volumeOptions.setEnabled(true);
                topGuns.setEnabled(true);
                mainMenu.setEnabled(true);
            }
        };
        
        mainMenu.addControl(new UIAnimator(true, DIR.LEFT, 0.5f, 0.3f, 64, cb));
    }
    
    private static void removeInGameMenu(final HUD hud) {
        removeInGameMenu(new Callback() {
            @Override
            public void call() {
                container.clearChildren();
                if (enabled) {
                    hud.setEnabled(true);
                    SpaceRocks.PAUSED.set(false);
                    for (SoundNode sound : SoundFX.blackHoleSounds)
                        sound.unPause();
                }
            }
        });
    }
    
    private static void removeInGameMenu(Callback cb) {
        Container c = (Container)container.getChild(0);
        
        final Button resume = (Button)c.getChild(0);
        final Button volumeOptions = (Button)c.getChild(1);
        final Button topGuns = (Button)c.getChild(2);
        final Button mainMenu = (Button)c.getChild(3);
        resume.setEnabled(false);
        volumeOptions.setEnabled(false);
        topGuns.setEnabled(false);
        mainMenu.setEnabled(false);
        
        resume.addControl(new UIAnimator(false, DIR.DOWN, 0.5f, 0.3f, 96, cb));
        volumeOptions.addControl(new UIAnimator(false, DIR.DOWN, 0.5f, 0.2f, 96, null));
        topGuns.addControl(new UIAnimator(false, DIR.DOWN, 0.5f, 0.1f, 96, null));
        mainMenu.addControl(new UIAnimator(false, DIR.DOWN, 0.5f, 0f, 96, null));
        /*resume.addControl(new UIAnimator(false, DIR.RIGHT, 0.5f, 0f, 64, null));
        volumeOptions.addControl(new UIAnimator(false, DIR.RIGHT, 0.5f, 0.1f, 64, null));
        mainMenu.addControl(new UIAnimator(false, DIR.RIGHT, 0.5f, 0.2f, 64, cb));*/
    }
    
    public static void displayEndGame(Tracker tracker) {
        container.clearChildren();
        ElementId eid = new ElementId("cliplabel");
        
        final Container window = new Container(new VBoxLayout(gui.dpInt(5), FillMode.First,
                true, HAlign.Left, true));
        window.setUserData("layer", 1);
        window.setBackground(Initialize.blackBack.clone());
        window.setPadding(new Insets3f(gui.dpInt(5), gui.dpInt(5), gui.dpInt(5), gui.dpInt(5)));
        window.addChild(new Label(res.getString("game_over"), new ElementId("titlelabel")),
                HAlign.Center, false, false);
        window.addChild(new Spacer(new Vector3f(5, gui.dpInt(10), 0)), false, true);
        
        Container hc = new Container(new HBoxLayout(gui.dpInt(10), FillMode.None,
                false, VAlign.Top, true));
        window.addChild(hc, false, false);
        hc.setUserData("layer", 2);
        
        Container lc = new Container(new VBoxLayout(gui.dpInt(5), FillMode.None, true,
                HAlign.Left, true));
        lc.setUserData("layer", 3);
        Container rc = new Container(new VBoxLayout(gui.dpInt(5), FillMode.None, true,
                HAlign.Left, true));
        rc.setUserData("layer", 3);
        hc.addChild(lc, false, false);
        hc.addChild(rc, false, false);
        
        //asteroid stats
        lc.addChild(new Label(res.getString("rocks"), new ElementId("titlelabel")), HAlign.Center,
                false, false);
        lc.addChild(new Spacer(new Vector3f(5, gui.dpInt(3), 0)));
        
        Container c = new Container(new HBoxLayout(gui.dpInt(15), FillMode.First, false,
                VAlign.Center, true));
        c.setUserData("layer", 4);
        c.addChild(new Label(res.getString("laser") + ":", eid), false, true);
        c.addChild(new Label(Long.toString(tracker.getLaserDestroyedAsteroids()), eid), false, false);
        lc.addChild(c, true, false);
        
        c = new Container(new HBoxLayout(gui.dpInt(15), FillMode.First, false,
                VAlign.Center, true));
        c.setUserData("layer", 4);
        c.addChild(new Label(res.getString("sonic_blast") + ":", eid), false, true);
        c.addChild(new Label(Long.toString(tracker.getBlastDestroyedAsteroids()), eid), false, false);
        lc.addChild(c, true, false);
        
        c = new Container(new HBoxLayout(gui.dpInt(15), FillMode.First, false,
                VAlign.Center, true));
        c.setUserData("layer", 4);
        c.addChild(new Label(res.getString("black_hole") + ":", eid), false, true);
        c.addChild(new Label(Long.toString(tracker.getBHDestroyedAsteroids()), eid), false, false);
        lc.addChild(c, true, false);
        
        c = new Container(new HBoxLayout(gui.dpInt(15), FillMode.First, false,
                VAlign.Center, true));
        c.setUserData("layer", 4);
        c.addChild(new Label(res.getString("total") + ":", eid), false, true);
        c.addChild(new Label(Long.toString(tracker.getDestroyedAsteroids()), eid), false, false);
        lc.addChild(c, true, false);
        int astPerc = 0;
        if (tracker.getTotalSpawnedAsteroids() > 0) {
            astPerc = (int)Math.round(((double)tracker.getDestroyedAsteroids()
                    / tracker.getTotalSpawnedAsteroids()) * 100);
        }
        lc.addChild(new Label(Integer.toString(astPerc) + "%", eid), HAlign.Right, false, false);
        
        //PUP stats
        rc.addChild(new Label(res.getString("pups"), new ElementId("titlelabel")), HAlign.Center,
                false, false);
        rc.addChild(new Spacer(new Vector3f(5, gui.dpInt(3), 0)));
        
        c = new Container(new HBoxLayout(gui.dpInt(15), FillMode.First, false,
                VAlign.Center, true));
        c.setUserData("layer", 4);
        c.addChild(new Label(res.getString("health") + ":", eid), false, true);
        int healthPerc = 0;
        if (tracker.getDroppedHealth() > 0) {
            healthPerc = (int)Math.round(((double)tracker.getPickedHealth()
                    / tracker.getDroppedHealth()) * 100);
        }
        c.addChild(new Label(Integer.toString(healthPerc) + "%", eid), false, false);
        rc.addChild(c, true, false);
        
        c = new Container(new HBoxLayout(gui.dpInt(15), FillMode.First, false,
                VAlign.Center, true));
        c.setUserData("layer", 4);
        c.addChild(new Label(res.getString("energy") + ":", eid), false, true);
        int energyPerc = 0;
        if (tracker.getDroppedEnergy() > 0) {
            energyPerc = (int)Math.round(((double)tracker.getPickedEnergy()
                    / tracker.getDroppedEnergy()) * 100);
        }
        c.addChild(new Label(Integer.toString(energyPerc) + "%", eid), false, false);
        rc.addChild(c, true, false);
        
        c = new Container(new HBoxLayout(gui.dpInt(15), FillMode.First, false,
                VAlign.Center, true));
        c.setUserData("layer", 4);
        c.addChild(new Label(res.getString("sonic_blast") + ":", eid), false, true);
        int blastPerc = 0;
        if (tracker.getDroppedBlast() > 0) {
            blastPerc = (int)Math.round(((double)tracker.getPickedBlast()
                    / tracker.getDroppedBlast()) * 100);
        }
        c.addChild(new Label(Integer.toString(blastPerc) + "%", eid), false, false);
        rc.addChild(c, true, false);
        
        c = new Container(new HBoxLayout(gui.dpInt(15), FillMode.First, false,
                VAlign.Center, true));
        c.setUserData("layer", 4);
        c.addChild(new Label(res.getString("black_hole") + ":", eid), false, true);
        int bhPerc = 0;
        if (tracker.getDroppedBH() > 0) {
            bhPerc = (int)Math.round(((double)tracker.getSpawnedBH()
                    / tracker.getDroppedBH()) * 100);
        }
        c.addChild(new Label(Integer.toString(bhPerc) + "%", eid), false, false);
        rc.addChild(c, true, false);
        int pupPerc = 0;
        if (tracker.getDroppedHealth() + tracker.getDroppedEnergy()
            + tracker.getDroppedBlast() + tracker.getDroppedBH() > 0) {
            pupPerc = (int)Math.round(((double)(tracker.getPickedHealth()
                    + tracker.getPickedEnergy() + tracker.getPickedBlast()
                    + tracker.getSpawnedBH())
                    / (tracker.getDroppedHealth() + tracker.getDroppedEnergy()
                    + tracker.getDroppedBlast() + tracker.getDroppedBH())) * 100);
        }
        rc.addChild(new Label(Integer.toString(pupPerc) + "%", eid), HAlign.Right, false, false);
        
        //button
        window.addChild(new Spacer(new Vector3f(5, gui.dpInt(20), 0)), false, true);
        final Button mainMenu = new Button(res.getString("menu"));
        mainMenu.setUserData("layer", 2);
        mainMenu.addClickCommands(new Command<Button>() {
            @Override
            public void execute(Button source) {
                if (!enabled)
                    return;
                mainMenu.setEnabled(false);
                
                sr.getStateManager().getState(GameState.class).quit();
            }
        });
        mainMenu.setEnabled(false);
        window.addChild(mainMenu, HAlign.Right, false, false);
        
        container.addChild(window);
        Callback cb = new Callback() {
            @Override
            public void call() {
                mainMenu.setEnabled(true);
            }
        };
        
        addRandomAnimWindow(window, true, 0.5f, 0.2f, cb);
    }
    
    public static void displayNewTopGun(final Tracker tracker, final int listPosition) {
        container.clearChildren();
        final Container window = new Container(new VBoxLayout(gui.dpInt(5), FillMode.First,
                true, HAlign.Center, true));
        window.setUserData("layer", 1);
        window.setBackground(Initialize.blackBack.clone());
        window.setPadding(new Insets3f(gui.dpInt(5), gui.dpInt(5), gui.dpInt(5), gui.dpInt(5)));
        window.addChild(new Label(res.getString("topgun"), new ElementId("titlelabel")),
                false, false);
        window.addChild(new Spacer(new Vector3f(5, gui.dpInt(1), 0)), false, true);
        
        Label text = new Label(res.getString("newtopgun"));
        text.setMaxWidth(sr.getWidth() * 0.67f);
        window.addChild(text, false, false);
        window.addChild(new Spacer(new Vector3f(5, gui.dpInt(4), 0)), false, true);
        
        Container c = new Container(new HBoxLayout(gui.dpInt(15), FillMode.None, false,
                VAlign.Center, true));
        c.setUserData("layer", 2);
        final LetterPicker picker1 = new LetterPicker(res.getString("letterpicker"));
        picker1.setUserData("layer", 3);
        c.addChild(picker1, false, false);
        final LetterPicker picker2 = new LetterPicker(res.getString("letterpicker"));
        picker2.setUserData("layer", 3);
        c.addChild(picker2, false, false);
        final LetterPicker picker3 = new LetterPicker(res.getString("letterpicker"));
        picker3.setUserData("layer", 3);
        c.addChild(picker3, false, false);
        
        window.addChild(c, false, false);
        
        window.addChild(new Spacer(new Vector3f(5, gui.dpInt(20), 0)), false, true);
        final Button confirm = new Button(res.getString("confirm"));
        confirm.setUserData("layer", 2);
        confirm.addClickCommands(new Command<Button>() {
            @Override
            public void execute(Button source) {
                if (!enabled)
                    return;
                StringBuilder initials = new StringBuilder(picker1.getSelection());
                initials.append(picker2.getSelection());
                initials.append(picker3.getSelection());
                TopGuns.setTopGun(listPosition, initials.toString(),
                        tracker.getDestroyedAsteroids());
                res.savePrefs();
                
                picker1.setEnabled(false);
                picker2.setEnabled(false);
                picker3.setEnabled(false);
                confirm.setEnabled(false);
                addRandomAnimWindow(window, false, new Callback() {
                    @Override
                    public void call() {
                        displayTopGuns(listPosition, new Callback() {
                            @Override
                            public void call() {
                                displayEndGame(tracker);
                            }
                        });
                    }
                });
            }
        });
        picker1.setEnabled(false);
        picker2.setEnabled(false);
        picker3.setEnabled(false);
        confirm.setEnabled(false);
        window.addChild(confirm, HAlign.Right, false, false);
        
        Callback cb = new Callback() {
            @Override
            public void call() {
                picker1.setEnabled(true);
                picker2.setEnabled(true);
                picker3.setEnabled(true);
                confirm.setEnabled(true);
            }
        };
        
        container.addChild(window);
        addRandomAnimWindow(window, true, 0.5f, 1f, cb);
    }
    
    public static void displayTopGuns(Callback closeAction) {
        displayTopGuns(-1, closeAction);
    }
    
    public static void displayTopGuns(int highlight, final Callback closeAction) {
        container.clearChildren();
        final Container window = new Container(new VBoxLayout(gui.dpInt(5), FillMode.First,
                true, HAlign.Left, true));
        window.setUserData("layer", 1);
        window.setBackground(Initialize.blackBack.clone());
        window.setPadding(new Insets3f(gui.dpInt(5), gui.dpInt(5), gui.dpInt(5), gui.dpInt(5)));
        window.addChild(new Label(res.getString("topguns"), new ElementId("titlelabel")),
                HAlign.Center, false, false);
        window.addChild(new Spacer(new Vector3f(5, gui.dpInt(5), 0)), false, true);
        
        final Label l1 = new Label("");
        final Label l2 = new Label("");
        final Label l3 = new Label("");
        Container h = new Container(new HBoxLayout(gui.dpInt(5), FillMode.First,
                false, VAlign.Top, true));
        Container num = new Container(new VBoxLayout(gui.dpInt(5), FillMode.None,
                false, HAlign.Left, true));
        Container ini = new Container(new VBoxLayout(gui.dpInt(5), FillMode.None,
                false, HAlign.Left, true));
        Container rck = new Container(new VBoxLayout(gui.dpInt(5), FillMode.None,
                false, HAlign.Left, true));
        
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < TopGuns.topGuns.length; i++) {
            sb.append(i + 1);
            sb.append(".) ");
            if (i == highlight) {
                l1.setText(sb.toString());
                num.addChild(l1);
            } else {
                num.addChild(new Label(sb.toString()));
            }
            
            sb.delete(0, sb.length());
        }
        
        int count = 0;
        for (TopGun tg : TopGuns.topGuns) {
            sb.append(tg.rocks);
            if (count == highlight) {
                l2.setText(tg.initials);
                ini.addChild(l2);
                l3.setText(sb.toString());
                rck.addChild(l3);
            } else {
                ini.addChild(new Label(tg.initials));
                rck.addChild(new Label(sb.toString()));
            }
            
            sb.delete(0, sb.length());
            count++;
        }
        h.addChild(num, false, false);
        h.addChild(ini, false, true);
        h.addChild(rck, false, false);
        window.addChild(h, true, false);
        
        window.addChild(new Spacer(new Vector3f(5, gui.dpInt(20), 0)), false, true);
        final Button close = new Button(res.getString("close"));
        close.setUserData("layer", 2);
        close.setInsets(new Insets3f(0f, gui.dpInt(96), 0f, 0f));
        close.addClickCommands(new Command<Button>() {
            @Override
            public void execute(Button source) {
                if (!enabled)
                    return;
                close.setEnabled(false);
                addRandomAnimWindow(window, false, closeAction);
            }
        });
        close.setEnabled(false);
        window.addChild(close, HAlign.Right, false, false);
        
        Callback cb = new Callback() {
            @Override
            public void call() {
                close.setEnabled(true);
            }
        };
        
        container.addChild(window);
        addRandomAnimWindow(window, true, 0.5f, 0.2f, cb);
        
        if (highlight < 0 || highlight >= TopGuns.topGuns.length)
            return;
        
        window.addControl(new AbstractControl() {
            private final ColorRGBA highlightCol1 = new ColorRGBA(0.157f, 1f, 0.6117f, 1f);
            private final ColorRGBA highlightCol2 = new ColorRGBA(1f, 0.157f, 0.6117f, 1f);
            private final ColorRGBA colStore = new ColorRGBA();
            
            private final float length = 0.8f;
            private float tme;
            private boolean reverse = false;
            
            @Override
            public void controlUpdate(float tpf) {
                tme += tpf;
                if (tme >= length) {
                    reverse = !reverse;
                    do {
                        tme -= length;
                    } while (tme >= length);
                }
                
                if (reverse) {
                    GMath.smoothRGBA(tme / length, colStore, highlightCol2, highlightCol1);
                } else
                    GMath.smoothRGBA(tme / length, colStore, highlightCol1, highlightCol2);
                
                l1.setColor(colStore);
                l2.setColor(colStore);
                l3.setColor(colStore);
            }
            
            @Override
            public void controlRender(RenderManager rm, ViewPort vp) {
                
            }
        });
    }
    
    public static void displayVolume(final Callback closeAction) {
        container.clearChildren();
        final float musicVol = Options.getMusicVolume();
        final float sfxVol = Options.getSFXVolume();
        
        final Container window = new Container(new VBoxLayout(gui.dpInt(5), FillMode.First,
                true, HAlign.Center, true));
        window.setUserData("layer", 1);
        window.setBackground(Initialize.blackBack.clone());
        window.setPadding(new Insets3f(gui.dpInt(5), gui.dpInt(5), gui.dpInt(5), gui.dpInt(5)));
        window.addChild(new Label(res.getString("volume"), new ElementId("titlelabel")),
                HAlign.Center, false, false);
        window.addChild(new Spacer(new Vector3f(sr.getWidth() * 0.67f, gui.dpInt(5), 0)),
                false, true);
        
        final Slider musicSlider = new Slider();
        musicSlider.getModel().setPercent(Options.getMusicVolume());
        final Slider sfxSlider = new Slider();
        sfxSlider.getModel().setPercent(Options.getSFXVolume());
        musicSlider.setEnabled(false);
        sfxSlider.setEnabled(false);
        musicSlider.setUserData("layer", 2);
        sfxSlider.setUserData("layer", 2);
        
        window.addChild(new Label(res.getString("music")), false, false);
        window.addChild(musicSlider, true, false);
        window.addChild(new Spacer(new Vector3f(5, gui.dpInt(10), 0)), false, true);
        window.addChild(new Label(res.getString("sound")), false, false);
        window.addChild(sfxSlider, true, false);
        
        window.addControl(new AbstractControl() {
            @Override
            public void controlUpdate(float tpf) {
                Options.setMusicVolume((float)musicSlider.getModel().getPercent());
                Options.setSFXVolume((float)sfxSlider.getModel().getPercent());
                Music.setVolume(Options.getMusicVolume());
            }
            
            @Override
            public void controlRender(RenderManager rm, ViewPort vp) {
                
            }
        });
        
        window.addChild(new Spacer(new Vector3f(5, gui.dpInt(20), 0)), false, true);
        Container c = new Container(new HBoxLayout(gui.dpInt(5), FillMode.None, false,
                    VAlign.Top, true));
        c.setUserData("layer", 2);
        final Button cancel = new Button(res.getString("cancel"));
        final Button confirm = new Button(res.getString("confirm"));
        cancel.setUserData("layer", 3);
        confirm.setUserData("layer", 3);
        
        cancel.addClickCommands(new Command<Button>() {
            @Override
            public void execute(Button source) {
                if (!enabled)
                    return;
                window.removeControl(window.getControl(1));
                cancel.setEnabled(false);
                Options.setMusicVolume(musicVol);
                Options.setSFXVolume(sfxVol);
                Music.setVolume(Options.getMusicVolume());
                confirm.setEnabled(false);
                musicSlider.setEnabled(false);
                sfxSlider.setEnabled(false);
                addRandomAnimWindow(window, false, closeAction);
            }
        });
        
        confirm.addClickCommands(new Command<Button>() {
            @Override
            public void execute(Button source) {
                if (!enabled)
                    return;
                window.removeControl(window.getControl(1));
                cancel.setEnabled(false);
                confirm.setEnabled(false);
                musicSlider.setEnabled(false);
                sfxSlider.setEnabled(false);
                res.savePrefs();
                addRandomAnimWindow(window, false, closeAction);
            }
        });
        cancel.setEnabled(false);
        confirm.setEnabled(false);
        c.addChild(cancel, false, false);
        c.addChild(confirm, false, false);
        window.addChild(c, HAlign.Right, false, false);
        
        Callback cb = new Callback() {
            @Override
            public void call() {
                cancel.setEnabled(true);
                confirm.setEnabled(true);
                musicSlider.setEnabled(true);
                sfxSlider.setEnabled(true);
            }
        };
        
        container.addChild(window);
        addRandomAnimWindow(window, true, 0.5f, 0.2f, cb);
    }
    
    public static void displayOptions(final Callback closeAction) {
        container.clearChildren();
        final float musicVol = Options.getMusicVolume();
        final float sfxVol = Options.getSFXVolume();
        
        final Container window = new Container(new VBoxLayout(gui.dpInt(5), FillMode.First,
                true, HAlign.Center, true));
        window.setUserData("layer", 1);
        window.setBackground(Initialize.blackBack.clone());
        window.setPadding(new Insets3f(gui.dpInt(5), gui.dpInt(5), gui.dpInt(5), gui.dpInt(5)));
        window.addChild(new Label(res.getString("options"), new ElementId("titlelabel")),
                HAlign.Center, false, false);
        window.addChild(new Spacer(new Vector3f(sr.getWidth() * 0.67f, gui.dpInt(5), 0)),
                false, true);
        
        final Slider musicSlider = new Slider();
        musicSlider.getModel().setPercent(Options.getMusicVolume());
        final Slider sfxSlider = new Slider();
        sfxSlider.getModel().setPercent(Options.getSFXVolume());
        musicSlider.setEnabled(false);
        sfxSlider.setEnabled(false);
        musicSlider.setUserData("layer", 2);
        sfxSlider.setUserData("layer", 2);
        
        window.addChild(new Label(res.getString("music")), false, false);
        window.addChild(musicSlider, true, false);
        window.addChild(new Label(res.getString("sound")), false, false);
        window.addChild(sfxSlider, true, false);
        
        window.addControl(new AbstractControl() {
            @Override
            public void controlUpdate(float tpf) {
                Options.setMusicVolume((float)musicSlider.getModel().getPercent());
                Options.setSFXVolume((float)sfxSlider.getModel().getPercent());
                Music.setVolume(Options.getMusicVolume());
            }
            
            @Override
            public void controlRender(RenderManager rm, ViewPort vp) {
                
            }
        });
        
        window.addChild(new Spacer(new Vector3f(5, gui.dpInt(10), 0)), false, false);
        Container comboCont = new Container(new VBoxLayout(gui.dpInt(10), FillMode.None, true, HAlign.Left, true));
        comboCont.setUserData("layer", 3);
        
        final ComboBox pDetail = new ComboBox();
        pDetail.setMargins(gui.dpInt(7));
        pDetail.addItems(res.getString("low"), res.getString("medium"), res.getString("high"));
        pDetail.setSelection(Options.getParticleDetailInt());
        pDetail.setEnabled(false);
        pDetail.setUserData("layer", 5);
        Container cont = new Container(new HBoxLayout(0, FillMode.First, false,
                VAlign.Center, true));
        cont.setUserData("layer", 4);
        cont.addChild(new Label(res.getString("particledetail")), false, false);
        cont.addChild(new Spacer(new Vector3f(gui.dpInt(5), 0, 0)), false, true);
        cont.addChild(pDetail, false, false);
        comboCont.addChild(cont);
        
        final ComboBox uDetail = new ComboBox();
        uDetail.setMargins(gui.dpInt(7));
        uDetail.addItems(res.getString("low"), res.getString("high"));
        uDetail.setSelection(Options.supportDerivatives ? Options.getUIDetailInt()
                : 0);
        uDetail.setEnabled(false);
        uDetail.setUserData("layer", 4);
        if (Options.supportDerivatives) {
            cont = new Container(new HBoxLayout(0, FillMode.First, false,
                    VAlign.Center, true));
            cont.setUserData("layer", 3);
            cont.addChild(new Label(res.getString("uidetail")), false, false);
            cont.addChild(new Spacer(new Vector3f(gui.dpInt(5), 0, 0)), false, true);
            cont.addChild(uDetail, false, false);
            comboCont.addChild(cont);
            
            uDetail.addClickCommands(new Command<Button>() {
                @Override
                public void execute(Button source) {
                    if (pDetail.isOpen())
                        pDetail.setOpen(false);
                }
            });
            
            pDetail.addClickCommands(new Command<Button>() {
                @Override
                public void execute(Button source) {
                    if (uDetail.isOpen())
                        uDetail.setOpen(false);
                }
            });
        }
        window.addChild(comboCont, HAlign.Left, false, false);
        
        window.addChild(new Spacer(new Vector3f(5, gui.dpInt(20), 0)), false, true);
        Container c = new Container(new HBoxLayout(gui.dpInt(5), FillMode.None, false,
                    VAlign.Top, true));
        c.setUserData("layer", 2);
        final Button cancel = new Button(res.getString("cancel"));
        final Button confirm = new Button(res.getString("confirm"));
        cancel.setUserData("layer", 3);
        confirm.setUserData("layer", 3);
        
        cancel.addClickCommands(new Command<Button>() {
            @Override
            public void execute(Button source) {
                if (!enabled)
                    return;
                window.removeControl(window.getControl(1));
                cancel.setEnabled(false);
                Options.setMusicVolume(musicVol);
                Options.setSFXVolume(sfxVol);
                Music.setVolume(Options.getMusicVolume());
                confirm.setEnabled(false);
                musicSlider.setEnabled(false);
                sfxSlider.setEnabled(false);
                pDetail.setEnabled(false);
                if (Options.supportDerivatives)
                    uDetail.setEnabled(false);
                addRandomAnimWindow(window, false, closeAction);
            }
        });
        
        confirm.addClickCommands(new Command<Button>() {
            @Override
            public void execute(Button source) {
                if (!enabled)
                    return;
                window.removeControl(window.getControl(1));
                cancel.setEnabled(false);
                confirm.setEnabled(false);
                musicSlider.setEnabled(false);
                sfxSlider.setEnabled(false);
                pDetail.setEnabled(false);
                Options.setParticleDetail(pDetail.getSelection());
                if (Options.supportDerivatives) {
                    uDetail.setEnabled(false);
                    Options.setUIDetail(uDetail.getSelection());
                    GuiGlobals.getInstance().setSupportDerivatives(Options.supportDerivatives
                            && Options.getUIDetail() == Detail.High);
                } else
                    Options.setUIDetail(Detail.Low);
                res.savePrefs();
                addRandomAnimWindow(window, false, closeAction);
            }
        });
        cancel.setEnabled(false);
        confirm.setEnabled(false);
        c.addChild(cancel, false, false);
        c.addChild(confirm, false, false);
        window.addChild(c, HAlign.Right, false, false);
        
        Callback cb = new Callback() {
            @Override
            public void call() {
                cancel.setEnabled(true);
                confirm.setEnabled(true);
                musicSlider.setEnabled(true);
                sfxSlider.setEnabled(true);
                pDetail.setEnabled(true);
                uDetail.setEnabled(true);
            }
        };
        
        container.addChild(window);
        addRandomAnimWindow(window, true, 0.5f, 0.2f, cb);
    }
    
    private static void displayDonateDialog() {
        container.clearChildren();
        
        final Container window = new Container(new VBoxLayout(gui.dpInt(5), FillMode.None,
                false, HAlign.Left, true));
        window.setUserData("layer", 1);
        window.setBackground(Initialize.blackBack.clone());
        window.setPadding(new Insets3f(gui.dpInt(5), gui.dpInt(5), gui.dpInt(5), gui.dpInt(5)));
        window.addChild(new Label(res.getString("donate"), new ElementId("titlelabel")),
                HAlign.Center, false, false);
        window.addChild(new Spacer(new Vector3f(sr.getWidth() * 0.67f, gui.dpInt(5), 0)));
        
        Label label = new Label(res.getString("donateprompt"));
        label.setMaxWidth(sr.getWidth() * 0.67f);
        window.addChild(label);
        
        window.addChild(new Spacer(new Vector3f(5, gui.dpInt(20), 0)), false, false);
        Container c = new Container(new HBoxLayout(gui.dpInt(5), FillMode.None, false,
                    VAlign.Top, true));
        c.setUserData("layer", 2);
        final Button cancel = new Button(res.getString("cancel"));
        final Button continueButton = new Button(res.getString("continuebutton"));
        cancel.setUserData("layer", 3);
        continueButton.setUserData("layer", 3);
        
        cancel.addClickCommands(new Command<Button>() {
            @Override
            public void execute(Button source) {
                if (!enabled)
                    return;
                continueButton.setEnabled(false);
                cancel.setEnabled(false);
                addRandomAnimWindow(window, false, new Callback() {
                    @Override
                    public void call() {
                        displayMainMenu();
                    }
                });
            }
        });
        
        continueButton.addClickCommands(new Command<Button>() {
            @Override
            public void execute(Button source) {
                if (!enabled)
                    return;
                cancel.setEnabled(false);
                continueButton.setEnabled(false);
                addRandomAnimWindow(window, false, new Callback() {
                    @Override
                    public void call() {
                        displayMainMenu();
                        res.launchDonate();
                    }
                });
            }
        });
        cancel.setEnabled(false);
        continueButton.setEnabled(false);
        c.addChild(cancel, false, false);
        c.addChild(continueButton, false, false);
        window.addChild(c, HAlign.Right, false, false);
        
        Callback cb = new Callback() {
            @Override
            public void call() {
                cancel.setEnabled(true);
                continueButton.setEnabled(true);
            }
        };
        
        container.addChild(window);
        addRandomAnimWindow(window, true, 0.5f, 0.2f, cb);
    }
    
    private static void addRandomAnimWindow(Container window, boolean moveIn, Callback cb) {
        addRandomAnimWindow(window, moveIn, 0.5f, 0f, cb);
    }
    
    private static void addRandomAnimWindow(Container window, boolean moveIn, float length,
            float delay, Callback cb) {
        DIR dir;
        switch(FastMath.nextRandomInt(0, 3)) {
            case 0:
                dir = DIR.RIGHT;
                break;
            case 1:
                dir = DIR.UP;
                break;
            case 2:
                dir = DIR.DOWN;
                break;
            default:
                dir = DIR.LEFT;
        }
        window.addControl(new UIAnimator(moveIn, dir, length, delay, gui.dpInt(64), cb));
    }
}
