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

import com.atr.spacerocks.SpaceRocks;
import com.atr.spacerocks.dynamo.component.CurvedMeterElement;
import com.atr.spacerocks.sound.SoundFX;
import com.atr.spacerocks.sound.SoundNode;
import com.atr.spacerocks.state.GameState;
import com.jme3.math.ColorRGBA;
import com.jme3.math.FastMath;
import com.jme3.math.Vector3f;
import com.jme3.scene.Node;
import com.simsilica.lemur.Button;
import com.simsilica.lemur.Button.ButtonAction;
import com.simsilica.lemur.Command;
import com.simsilica.lemur.Container;
import com.simsilica.lemur.FillMode;
import com.simsilica.lemur.GuiGlobals;
import com.simsilica.lemur.ImageBox;
import com.simsilica.lemur.Insets3f;
import com.simsilica.lemur.Label;
import com.simsilica.lemur.Spacer;
import com.simsilica.lemur.ThumbStick;
import com.simsilica.lemur.component.GradientBackgroundComponent;
import com.simsilica.lemur.component.HBoxLayout;
import com.simsilica.lemur.component.HBoxLayout.VAlign;
import com.simsilica.lemur.component.VBoxLayout;
import com.simsilica.lemur.component.VBoxLayout.HAlign;
import com.simsilica.lemur.core.GuiControl;
import com.simsilica.lemur.style.ElementId;

/**
 *
 * @author Adam T. Ryder
 * <a href="http://1337atr.weebly.com">http://1337atr.weebly.com</a>
 */
public class HUD {
    private final GuiGlobals gui;
    private final Node rootNode;
    private final SpaceRocks app;
    private final GameState gameState;
    
    private final Container container;
    private final HBoxLayout layout;
    
    private final Container midContainer;
    private final VBoxLayout midLayout;
    
    private final Container statContainer;
    
    private final Label asteroidCount;
    private final Label sonicBlastCount;
    private final StringBuilder stringBuilder = new StringBuilder();
    private final Button menuButton;
    
    private final ThumbStick thumbStick;
    private final Button fireButton;
    private final Button sonicBlastButton;
    private final ColorRGBA shadowOn = new ColorRGBA(0.135f, 0.547f, 1, 1);
    private final ColorRGBA shadowOff = ColorRGBA.BlackNoAlpha.clone();
    
    private final CurvedMeterElement hpMeter;
    private final CurvedMeterElement energyMeter;
    
    private boolean enabled = true;
    
    private boolean fireOn = false;
    
    public HUD(SpaceRocks app, GameState state) {
        gui = GuiGlobals.getInstance();
        rootNode = app.getGuiNode();
        this.app = app;
        this.gameState = state;
        
        layout = new HBoxLayout(gui.dpInt(5), FillMode.First, false, VAlign.Top, true);
        container = new Container(layout);
        container.setPreferredSize(new Vector3f(app.getWidth(), app.getHeight(), 35));
        container.setLocalTranslation(0, app.getHeight(), 0);
        container.setPadding(new Insets3f(gui.dpInt(3), gui.dpInt(6), gui.dpInt(3), gui.dpInt(6)));
        rootNode.attachChild(container);
        
        ColorRGBA meterCol = new ColorRGBA(0.98f, 0.98f, 0.98f, 1f);
        energyMeter = new CurvedMeterElement(60, 0.235f, 0.24f, 3, 32, 3, 12,
                -gui.dp(1), new ColorRGBA(0.5f, 0f, 1, 0.65f), 
                new ColorRGBA(0.8f, 0.3f, 1, 0.65f), meterCol, app.getHeight());
        layout.addChild(energyMeter, false, false);
        energyMeter.setPercent(1);
        
        midLayout = new VBoxLayout(gui.dpInt(5), FillMode.First, false, HAlign.Left, true);
        midContainer = new Container(midLayout);
        layout.addChild(midContainer, true, true);
        
        hpMeter = new CurvedMeterElement(60, 0.235f, 0.24f, 3, 32, 3, 12,
                gui.dp(1), new ColorRGBA(0.5f, 0f, 1, 0.65f),
                new ColorRGBA(0.8f, 0.3f, 1, 0.65f), meterCol, app.getHeight());
        layout.addChild(hpMeter, false, false);
        
        HBoxLayout hBox = new HBoxLayout(gui.dpInt(5), FillMode.First, false, VAlign.Center, true);
        statContainer = new Container(hBox);
        menuButton = new Button("", new ElementId("menubutton"));
        final HUD hud = this;
        menuButton.addClickCommands(new Command<Button>() {
            @Override
            public void execute(Button source) {
                for (SoundNode sound : SoundFX.blackHoleSounds)
                    sound.pause();
                UI.displayInGameMenu(hud);
            }
        });
        statContainer.addChild(menuButton, VAlign.Center, false, false);
        statContainer.addChild(new ImageBox(gui.loadTextureDP("Interface/Texture/icon_asteroid.png",
                false, false)), VAlign.Center, false, false);
        asteroidCount = new Label("0");
        statContainer.addChild(asteroidCount, VAlign.Center, false, false);
        statContainer.addChild(new Spacer(), true, true);
        statContainer.addChild(new ImageBox(gui.loadTextureDP("Interface/Texture/icon_bomb.png",
                false, false)), VAlign.Center, false, false);
        sonicBlastCount = new Label("0");
        statContainer.addChild(sonicBlastCount, VAlign.Center, false, false);
        midLayout.addChild(statContainer, true, false);
        
        midLayout.addChild(new Spacer(), true, true);
        
        hBox = new HBoxLayout(gui.dpInt(10), FillMode.First, false, VAlign.Center, true);
        Container c = new Container(hBox);
        thumbStick = new ThumbStick();
        c.addChild(thumbStick, false, false);
        c.addChild(new Spacer(), true, true);
        c.setPadding(new Insets3f(0, gui.dpInt(9), gui.dpInt(26), 0));
        
        midLayout.addChild(c, true, false);
        
        Command<Button> pressedCommand = new Command<Button>() {
            
            @Override
            public void execute(Button source) {
                if (fireOn) {
                    fireOn = false;
                    ((GradientBackgroundComponent)source.getBackground())
                            .setShadowColor(shadowOff);
                } else {
                    fireOn = true;
                    ((GradientBackgroundComponent)source.getBackground())
                            .setShadowColor(shadowOn);
                }
            }
        };
        fireOn = false;
        fireButton = new Button("", new ElementId("firebutton"));
        ((GradientBackgroundComponent)fireButton.getBackground())
                .setShadowColor(shadowOff);
        fireButton.addClickCommands(pressedCommand);
        c.addChild(fireButton, VAlign.Bottom, false, false);
        
        pressedCommand = new Command<Button>() {
            
            @Override
            public void execute(Button source) {
                if(source.isPressed()) {
                    ((GradientBackgroundComponent)source.getBackground())
                            .setShadowColor(shadowOn);
                } else {
                    ((GradientBackgroundComponent)source.getBackground())
                            .setShadowColor(shadowOff);
                    
                    gameState.getTracker().useSonicBlast();
                }
            }
        };
        
        sonicBlastButton = new Button("", new ElementId("sonicblastbutton"));
        ((GradientBackgroundComponent)sonicBlastButton.getBackground())
                .setShadowColor(shadowOff);
        sonicBlastButton.addCommands(ButtonAction.Down, pressedCommand);
        sonicBlastButton.addCommands(ButtonAction.Up, pressedCommand);
        c.addChild(sonicBlastButton, VAlign.Bottom, false, false);
        
        Vector3f size = container.getControl(GuiControl.class).getPreferredSize().clone();
        size.subtractLocal(container.getControl(GuiControl.class).adjustSize(size.clone(),
                true));
        container.getControl(GuiControl.class).setSize(size);
        midLayout.removeChild(statContainer);
        
        statContainer.setPreferredSize(statContainer.getSize());
        statContainer.setLocalTranslation(midContainer.getLocalTranslation().x,
                app.getHeight() + midContainer.getLocalTranslation().y,
                0.03f);
        rootNode.attachChild(statContainer);
    }
    
    public void updateAsteroidCount() {
        stringBuilder.delete(0, stringBuilder.length());
        stringBuilder.append(gameState.getTracker().getDestroyedAsteroids());
        asteroidCount.setText(stringBuilder.toString());
    }
    
    public void updateSonicBlastCount() {
        stringBuilder.delete(0, stringBuilder.length());
        stringBuilder.append(gameState.getTracker().getCurrentSonicBlast());
        sonicBlastCount.setText(stringBuilder.toString());
    }
    
    public boolean isFiring() {
        return fireOn;
    }
    
    public float getEnergy() {
        return energyMeter.getPercent();
    }
    
    public void setEnergyPercent(float perc) {
        perc = FastMath.clamp(perc, 0, 1);
        energyMeter.setPercent(perc);
    }
    
    public void setHealthPercent(float perc) {
        hpMeter.setPercent(perc);
    }
    
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
        thumbStick.setEnabled(enabled);
        fireButton.setEnabled(enabled);
        sonicBlastButton.setEnabled(enabled);
        menuButton.setEnabled(enabled);
    }
    
    public void disableFiring() {
        fireOn = false;
        ((GradientBackgroundComponent)fireButton.getBackground())
                .setShadowColor(shadowOff);
    }
    
    public boolean isEnabled() {
        return enabled;
    }
    
    public float getThumbMag() {
        return thumbStick.getMagnitude();
    }
    
    public float getThumbAngle() {
        return thumbStick.getAngleDegrees360();
    }
    
    public void quit() {
        rootNode.detachAllChildren();
    }
    
    public Vector3f getThumbStickPos() {
        return thumbStick.getWorldTranslation();
    }
    
    public Vector3f getThumbStickSize() {
        return thumbStick.getSize();
    }
    
    public Vector3f getFireButtonPos() {
        return fireButton.getWorldTranslation();
    }
    
    public Vector3f getBombButtonPos() {
        return sonicBlastButton.getWorldTranslation();
    }
    
    public Vector3f getButtonSize() {
        return fireButton.getSize();
    }
    
    public Vector3f getEnergyPos() {
        return energyMeter.getWorldTranslation();
    }
    
    public Vector3f getMeterSize() {
        return energyMeter.getSize();
    }
    
    public Vector3f getHPPos() {
        return hpMeter.getWorldTranslation();
    }
}
