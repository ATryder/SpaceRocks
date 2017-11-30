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

import com.atr.jme.font.util.StringContainer;
import com.atr.jme.font.util.Style;
import com.atr.math.GMath;
import com.atr.spacerocks.SpaceRocks;
import com.atr.spacerocks.control.MainMenuEffects;
import com.atr.spacerocks.shape.CenterQuad;
import com.atr.spacerocks.ui.UI;
import com.atr.spacerocks.util.Asteroid;
import com.jme3.app.state.AbstractAppState;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.FastMath;
import com.jme3.math.Vector3f;
import com.jme3.renderer.queue.RenderQueue;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.simsilica.lemur.Button;
import com.simsilica.lemur.Command;
import com.simsilica.lemur.Container;
import com.simsilica.lemur.FillMode;
import com.simsilica.lemur.GuiGlobals;
import com.simsilica.lemur.HAlignment;
import com.simsilica.lemur.Insets3f;
import com.simsilica.lemur.ProgressBar;
import com.simsilica.lemur.VAlignment;
import com.simsilica.lemur.component.GradientBackgroundComponent;
import com.simsilica.lemur.component.HBoxLayout;
import com.simsilica.lemur.component.HBoxLayout.VAlign;
import com.simsilica.lemur.component.IconComponent;
import com.simsilica.lemur.component.ShadowBackgroundComponent;
import com.simsilica.lemur.style.Attributes;
import com.simsilica.lemur.style.ColorStop;
import com.simsilica.lemur.style.Styles;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.dyn4j.dynamics.Body;
import org.dyn4j.dynamics.BodyFixture;
import org.dyn4j.geometry.MassType;
import org.dyn4j.geometry.Polygon;
import org.dyn4j.geometry.Vector2;

/**
 *
 * @author Adam T. Ryder
 * <a href="http://1337atr.weebly.com">http://1337atr.weebly.com</a>
 */
public class Initialize extends AbstractAppState {
    public final SpaceRocks app;
    private final float maxSteps = 12;
    private int currentStep = 1;
    
    private final GuiGlobals g;
    private Attributes attrs;
    private final Styles styles;
    
    private final ProgressBar pb;
    private final Container container;
    
    public static GradientBackgroundComponent blackBack;
    public static GradientBackgroundComponent whiteShiny;
    public static GradientBackgroundComponent blueBack;
    public static GradientBackgroundComponent plainBlack;
    
    public final Command<Button> pressedCommand;
    
    private final float fadeLength = 0.7f;
    private float fadeTme = 0;
    
    private Node sr2;
    private Body srBody;
    private Asteroid[] asteroids;
    private Node blueAsteroidPup;
    private Node redAsteroidPup;
    
    private Node PUP_Health;
    private Node PUP_Sonic;
    private Node PUP_Energy;
    
    public Initialize(SpaceRocks app) {
        this.app = app;
        
        g = GuiGlobals.getInstance();
        styles = g.getStyles();
        String defaultPreText = SpaceRocks.res.getString("preloadchars");
        g.setTrueTypePreloadCharacters("0123456789%");
        
        float zOffset = 0.05f;
        
        blackBack = new GradientBackgroundComponent(g.dpInt(3), g.dpInt(10), true,
                0, 45,
                new ColorStop(new ColorRGBA(0.1f, 0.1f, 0.1f, 1f), 0),
                new ColorStop(new ColorRGBA(0, 0, 0, 1), 1),
                new ColorStop(new ColorRGBA(0.235f, 0.9f, 1f, 1), 0, true),
                new ColorStop(new ColorRGBA(0.235f, 0.6f, 1f, 1), 0.25f, true),
                new ColorStop(new ColorRGBA(0.235f, 0.6f, 1f, 1), 0.75f, true),
                new ColorStop(new ColorRGBA(0.235f, 0.9f, 1f, 1), 1, true));
        blackBack.setZOffset(zOffset);
        
        whiteShiny = blackBack.clone();
        whiteShiny.setRadialGradient(false);
        whiteShiny.setInnerColors(
                new ColorStop(new ColorRGBA(1, 1, 1, 1), 0.5f),
                new ColorStop(new ColorRGBA(0.9f, 0.9f, 0.9f, 1), 0.51f),
                new ColorStop(new ColorRGBA(0.9f, 0.9f, 0.9f, 1), 0.8f),
                new ColorStop(new ColorRGBA(0.6f, 0.6f, 0.6f, 1), 1));
        
        blueBack = new GradientBackgroundComponent(0, g.dpInt(10), false,
                0, 45,
                new ColorStop(new ColorRGBA(0.235f, 0.6f, 1f, 1), 0f),
                new ColorStop(new ColorRGBA(0.435f, 0.8f, 1f, 1), 0.5f),
                new ColorStop(new ColorRGBA(0.235f, 0.6f, 1f, 1), 0.51f),
                new ColorStop(new ColorRGBA(0.235f, 0.6f, 1f, 1), 0.8f),
                new ColorStop(new ColorRGBA(0.235f, 0.3f, 1f, 1), 1));
        blueBack.setZOffset(zOffset);
        
        plainBlack = new GradientBackgroundComponent(0, g.dpInt(2), false, 0, 0,
                new ColorStop(new ColorRGBA(0, 0, 0, 0.83f), 0));
        plainBlack.setZOffset(zOffset);
        
        pressedCommand = new Command<Button>() {
            @Override
            public void execute(Button source) {
                if( source.isPressed() ) {
                    source.move(1, -1, 0);
                } else {
                    source.move(-1, 1, 0);
                }
            }
        };
        
        //progress bar
        attrs = GuiGlobals.getInstance().getStyles().getSelector("progress", "container", "spacerocks");
        GradientBackgroundComponent gbc = whiteShiny.clone();
        gbc.setRadius(g.dpInt(7));
        attrs.set("background", gbc);
        attrs.set("padding", new Insets3f(g.dpInt(5), g.dpInt(5), g.dpInt(5), g.dpInt(5)));
        
        attrs = styles.getSelector("progress", "label", "spacerocks");
        attrs.set("textHAlignment", HAlignment.Center);
        attrs.set("textVAlignment", VAlignment.Center);
        attrs.set("font", g.loadFontDP("Interface/Fonts/space age.ttf", Style.Italic, 12, g.dpInt(2)));
        attrs.set("color", new ColorRGBA(1, 1, 1, 0.55f));
        attrs.set("outlineColor", new ColorRGBA(0.3f, 0.3f, 0.3f, 1f));
        attrs.set("wrap", StringContainer.WrapMode.Clip);
        
        attrs = styles.getSelector("progress", "value", "spacerocks");
        gbc = blueBack.clone();
        gbc.setRadius(g.dpInt(5));
        attrs.set("background", gbc);
        
        g.setTrueTypePreloadCharacters(defaultPreText);
        g.strongTrueType = true;
        
        HBoxLayout hbox = new HBoxLayout(0, FillMode.ForcedEven, false, VAlign.Center, true);
        container = new Container(hbox);
        container.setPadding(new Insets3f(0, g.dpInt(16), 0, g.dpInt(16)));
        container.setLocalTranslation(0, app.getHeight(), 0);
        container.setPreferredSize(new Vector3f(app.getWidth(), app.getHeight(), 3));
        
        pb = new ProgressBar();
        float perc = 1f / maxSteps;
        pb.setProgressPercent(perc);
        pb.setMessage(Integer.toString((int)Math.floor(perc * 100)) + "%");
        container.addChild(pb, VAlign.Center, false, true);
        attrs = styles.getSelector("progress", "label", "spacerocks");
        attrs.set("font", null);
        
        app.getUINode().attachChild(container);
    }
    
    @Override
    public void update(float tpf) {
        if (currentStep > maxSteps) {
            fadeTme += tpf;
            if (fadeTme >= fadeLength) {
                app.getUINode().detachAllChildren();
                app.getStateManager().detach(this);
                
                app.setMesh(sr2, srBody, asteroids, blueAsteroidPup, redAsteroidPup,
                        PUP_Health, PUP_Sonic, PUP_Energy);
                
                UI.initialize(app);
                MainMenuEffects.intantiateEffects(app);
                
                return;
            }
            
            float perc = fadeTme / fadeLength;
            float scale = GMath.smoothStartFloat(perc, 1, 0.5f);
            container.setLocalScale(scale, scale, 1);
            container.setLocalTranslation((app.getWidth() / 2f) - (app.getWidth() * scale / 2f),
                    (app.getHeight() / 2f) + (app.getHeight() * scale / 2f), 0);
            container.setAlpha(GMath.smoothStartFloat(perc, 1, 0));
            
            return;
        }
        
        if (currentStep == 1) {
            Attributes attrs = styles.getSelector("spacerocks");
            attrs.set("font", g.loadFontDP("Interface/Fonts/space age.ttf", Style.Plain, 6, 0));
            
            //Label
            attrs = styles.getSelector("label", "spacerocks");
            attrs.set("wrap", StringContainer.WrapMode.WordClip);
            attrs.set("color", new ColorRGBA(1f, 1f, 1f, 1));
            attrs.set("textHAlignment", HAlignment.Left);
            attrs.set("textVAlignment", VAlignment.Center);
            
            //Clip label
            attrs = styles.getSelector("cliplabel", "spacerocks");
            attrs.set("wrap", StringContainer.WrapMode.Clip);
            attrs.set("color", new ColorRGBA(1f, 1f, 1f, 1));
            attrs.set("textHAlignment", HAlignment.Left);
            attrs.set("textVAlignment", VAlignment.Center);
            
            //Title label
            attrs = styles.getSelector("titlelabel", "spacerocks");
            attrs.set("font", g.loadFontDP("Interface/Fonts/space age.ttf", Style.Plain, 8, g.dpInt(2)));
            attrs.set("wrap", StringContainer.WrapMode.Clip);
            attrs.set("color", new ColorRGBA(0.8f, 0, 1, 1));
            attrs.set("outlineColor", new ColorRGBA(1f, 1f, 1f, 1));
            attrs.set("textHAlignment", HAlignment.Left);
            attrs.set("textVAlignment", VAlignment.Center);

            //TextField
            attrs = styles.getSelector("textField", "spacerocks");
            GradientBackgroundComponent gbc = blackBack.clone();
            gbc.setColor(new ColorRGBA(0, 0, 0, 0));
            attrs.set("background", gbc);
            attrs.set("color", new ColorRGBA(0.3f, 0.3f, 0.3f, 1));
            attrs.set("padding", new Insets3f(g.dpInt(3), g.dpInt(5), g.dpInt(3), g.dpInt(5)));
            attrs.set("cursorColor", new ColorRGBA(0.235f, 0.8f, 0.97f, 1));

            attrs = styles.getSelector("container", "spacerocks");
            attrs.set("background", null);
        } else if (currentStep == 2) {
            
            Map<Button.ButtonAction, List<Command<Button>>> stdButtonCommands =
                    new HashMap<Button.ButtonAction, List<Command<Button>>>();
            List<Command<Button>> list = new ArrayList<Command<Button>>(1);
            list.add(new Command<Button>() {
                @Override
                public void execute(Button source) {
                    ((GradientBackgroundComponent)source.getBackground()).setInnerColors(
                            new ColorStop(new ColorRGBA(1f, 0.6f, 0.235f, 1), 0f),
                            new ColorStop(new ColorRGBA(1f, 0.8f, 0.435f, 1), 0.5f),
                            new ColorStop(new ColorRGBA(1f, 0.6f, 0.235f, 1), 0.51f),
                            new ColorStop(new ColorRGBA(1f, 0.6f, 0.235f, 1), 0.8f),
                            new ColorStop(new ColorRGBA(1f, 0.3f, 0.235f, 1), 1));
                }
            });
            stdButtonCommands.put(Button.ButtonAction.Down, list);
            list = new ArrayList<Command<Button>>(1);
            list.add(new Command<Button>() {
                @Override
                public void execute(Button source) {
                    ((GradientBackgroundComponent)source.getBackground()).setInnerColors(
                            whiteShiny.getColors().clone());
                }
            });
            stdButtonCommands.put(Button.ButtonAction.Up, list);
            list = new ArrayList<Command<Button>>(1);
            
            //button
            attrs = styles.getSelector("button", "spacerocks");
            attrs.set("font", g.loadFontDP("Interface/Fonts/space age.ttf", Style.Plain, 8, g.dpInt(2)));
            attrs.set("background", whiteShiny.clone());
            attrs.set("color", new ColorRGBA(0.35f, 0.35f, 0.35f, 0));
            attrs.set("outlineColor", new ColorRGBA(0.35f, 0.35f, 0.35f, 1));
            attrs.set("highlightColor", null);
            attrs.set("padding", new Insets3f(g.dpInt(3), g.dpInt(15), g.dpInt(3), g.dpInt(15)));
            attrs.set("shadowColor", null);
            attrs.set("textHAlignment", HAlignment.Center);
            attrs.set("textVAlignment", VAlignment.Center);
            attrs.set("wrap", StringContainer.WrapMode.Clip);
            attrs.set("buttonCommands", stdButtonCommands);

            //slider
            attrs = styles.getSelector("slider", "spacerocks");
            GradientBackgroundComponent gbc = new GradientBackgroundComponent(0, 0, false, 0, 0,
                    new ColorStop(new ColorRGBA(1, 1, 1, 0), 0.455f),
                    new ColorStop(new ColorRGBA(1f, 1f, 1f, 1), 0.465f),
                    new ColorStop(new ColorRGBA(1f, 1f, 1f, 1), 0.535f),
                    new ColorStop(new ColorRGBA(1, 1, 1, 0), 0.545f));
            attrs.set("background", gbc);
            attrs.set("scrollRate", 48);
            attrs.set("delta", 1f);

            attrs = styles.getSelector("slider", "button", "spacerocks");
            gbc = new GradientBackgroundComponent(g.dpInt(2), 10000, true, 0, 0,
                    new ColorStop(new ColorRGBA(0.235f, 0.3f, 1f, 1), 0.5f),
                    new ColorStop(new ColorRGBA(0.435f, 0.8f, 1f, 1), 1f),
                    new ColorStop(new ColorRGBA(1, 1, 1, 1), 0, true));
            attrs.set("background", gbc);
            attrs.set("text", "");
            attrs.set("padding", new Insets3f(g.dpInt(13), g.dpInt(13), g.dpInt(13), g.dpInt(13)));
            attrs.set("buttonCommands", null);

            attrs = styles.getSelector("slider.thumb.button", "spacerocks");
            gbc = new GradientBackgroundComponent(0, 10000, true, 0, 0,
                    new ColorStop(new ColorRGBA(0.5f, 0f, 1, 1), 0.5f),
                    new ColorStop(new ColorRGBA(0.8f, 0.3f, 1, 1), 1f));
            attrs.set("background", gbc);
            attrs.set("padding", new Insets3f(g.dpInt(13), g.dpInt(13), g.dpInt(13), g.dpInt(13)));
            attrs.set("buttonCommands", null);
            
            attrs = styles.getSelector("letterpicker", "label", "spacerocks");
            attrs.set("font", g.loadFontDP("Interface/Fonts/space age.ttf", Style.Plain, 8, g.dpInt(2)));
            attrs.set("color", new ColorRGBA(1, 0, 0.05f, 1));
            attrs.set("outlineColor", new ColorRGBA(1, 1, 1, 1));
            attrs.set("wrap", StringContainer.WrapMode.NoWrap);
            attrs.set("textHAlignment", HAlignment.Center);
            attrs.set("textVAlignment", VAlignment.Center);
            
            attrs = styles.getSelector("letterpicker", "button", "spacerocks");
            gbc = new GradientBackgroundComponent(g.dpInt(2), 10000, true, 0, 0,
                    new ColorStop(new ColorRGBA(0.5f, 0f, 1, 1), 0.5f),
                    new ColorStop(new ColorRGBA(0.8f, 0.3f, 1, 1), 1f),
                    new ColorStop(new ColorRGBA(1, 1, 1, 1), 0, true));
            attrs.set("background", gbc);
            attrs.set("padding", new Insets3f(g.dpInt(16), g.dpInt(16), g.dpInt(16), g.dpInt(16)));
            attrs.set("buttonCommands", null);
            
        } else if (currentStep == 3) {
            
            //checkbox
            /*attrs = styles.getSelector("checkbox", "spacerocks");
            attrs.set("onView", new IconComponent("com/simsilica/lemur/icons/Advent/Advent-check-on.png", 1f,
                    g.dpInt(5), 0, 0.01f, false, true));
            attrs.set("offView", new IconComponent("com/simsilica/lemur/icons/Advent/Advent-check-off.png", 1f,
                    g.dpInt(5), 0, 0.01f, false, true));
            attrs.set("color", new ColorRGBA(1f, 1f, 1f, 1));
            attrs.set("highlightColor", null);*/
        } else if (currentStep == 4) {
            
            //combobox
            attrs = styles.getSelector("comboBox", "panel", "spacerocks");
            attrs.set("background", blackBack.clone());
            attrs.set("padding", new Insets3f(g.dpInt(3), g.dpInt(10), g.dpInt(3), g.dpInt(10)));
            
            /*stdButtonCommands.clear();
            list.add(pressedCommand);
            stdButtonCommands.put(Button.ButtonAction.Down, list);
            list.clear();
            list.add(pressedCommand);
            stdButtonCommands.put(Button.ButtonAction.Up, list);
            list.clear();*/

            attrs = styles.getSelector("comboBox", "spacerocks");
            attrs.set("font", g.loadFontDP("Interface/Fonts/space age.ttf", Style.Plain, 8, g.dpInt(2)));
            attrs.set("background", blueBack.clone());
            attrs.set("color", new ColorRGBA(1f, 1f, 1f, 0));
            attrs.set("outlineColor", new ColorRGBA(1f, 1f, 1f, 1));
            attrs.set("highlightColor", null);
            attrs.set("padding", new Insets3f(g.dpInt(3), g.dpInt(15), g.dpInt(3), g.dpInt(15)));
            attrs.set("shadowColor", null);
            attrs.set("textHAlignment", HAlignment.Center);
            attrs.set("textVAlignment", VAlignment.Center);
            attrs.set("wrap", StringContainer.WrapMode.Clip);

            attrs = styles.getSelector("comboBox", "listItem", "spacerocks");
            GradientBackgroundComponent gbc = new GradientBackgroundComponent(0, 0, false, 0, 0,
                                new ColorStop(new ColorRGBA(0, 0, 0, 0), 0));
            attrs.set("font", g.loadFontDP("Interface/Fonts/space age.ttf", Style.Plain, 8, g.dpInt(2)));
            attrs.set("background", gbc);
            attrs.set("color", new ColorRGBA(1f, 1f, 1f, 0));
            attrs.set("outlineColor", new ColorRGBA(1f, 1f, 1f, 1));
            attrs.set("highlightColor", null);
            attrs.set("textHAlignment", HAlignment.Center);
            attrs.set("textVAlignment", VAlignment.Center);
            attrs.set("padding", new Insets3f(g.dpInt(2), g.dpInt(5), g.dpInt(2), g.dpInt(5)));
            attrs.set("wrap", StringContainer.WrapMode.Clip);
            //attrs.set("buttonCommands", stdButtonCommands);

            attrs = styles.getSelector("comboBox", "listItemSelected", "spacerocks");
            gbc = blueBack.clone();
            gbc.setBorderThickness(0);
            gbc.setRadius(g.dpInt(3));
            attrs.set("font", g.loadFontDP("Interface/Fonts/space age.ttf", Style.Plain, 8, g.dpInt(2)));
            attrs.set("background", gbc);
            attrs.set("color", new ColorRGBA(1f, 1f, 1f, 0));
            attrs.set("outlineColor", new ColorRGBA(1f, 1f, 1f, 1));
            attrs.set("highlightColor", null);
            attrs.set("textHAlignment", HAlignment.Center);
            attrs.set("textVAlignment", VAlignment.Center);
            attrs.set("padding", new Insets3f(g.dpInt(2), g.dpInt(5), g.dpInt(2), g.dpInt(5)));
            attrs.set("wrap", StringContainer.WrapMode.Clip);
        } else if (currentStep == 5) {
            
            Map<Button.ButtonAction, List<Command<Button>>> stdButtonCommands =
                    new HashMap<Button.ButtonAction, List<Command<Button>>>();
            List<Command<Button>> list = new ArrayList<Command<Button>>();
            list.add(pressedCommand);
            stdButtonCommands.put(Button.ButtonAction.Down, list);
            list.clear();
            list.add(pressedCommand);
            stdButtonCommands.put(Button.ButtonAction.Up, list);
            list.clear();

            //tabbedpanel
            /*attrs = styles.getSelector("tabbedPanel", "spacerocks");
            attrs.set("activationColor", new ColorRGBA(1f, 1f, 1f, 1));

            attrs = styles.getSelector("tab.button", "spacerocks");
            attrs.set("color", new ColorRGBA(0.35f, 0.35f, 0.35f, 0));
            attrs.set("buttonCommands", stdButtonCommands);*/

            attrs = styles.getSelector("thumbStick", "spacerocks");
            GradientBackgroundComponent gbc = new GradientBackgroundComponent(0, 100000, true, 0, 0,
                                new ColorStop(new ColorRGBA(0, 0, 0, 0.85f), 0.75f),
                                new ColorStop(new ColorRGBA(0.4f, 0.4f, 0.4f, 1), 0.78f),
                                new ColorStop(new ColorRGBA(0.2f, 0.2f, 0.2f, 1), 1));
            gbc.setRadialGradientPos(1, 1);
            gbc.setRadialGradientScale(0.5f, 0.5f);
            attrs.set("background", gbc);
            attrs.set("padding", new Insets3f(g.dpInt(10), g.dpInt(10), g.dpInt(10), g.dpInt(10)));

            attrs = styles.getSelector("thumbStick", "panel", "spacerocks");
            gbc = new GradientBackgroundComponent(0, 100000, true, 0, 0,
                                new ColorStop(new ColorRGBA(0.07f, 0.07f, 0.07f, 1), 0.2f),
                                new ColorStop(new ColorRGBA(0.2f, 0.2f, 0.2f, 1), 0.7f),
                                new ColorStop(new ColorRGBA(0.45f, 0.45f, 0.45f, 1), 0.81f),
                                new ColorStop(new ColorRGBA(0.3f, 0.3f, 0.3f, 1), 0.9f),
                                new ColorStop(new ColorRGBA(0.15f, 0.15f, 0.15f, 1), 1));
            gbc.setRadialGradientPos(1f, 1f);
            gbc.setRadialGradientScale(0.5f, 0.5f);
            attrs.set("background", gbc);
            attrs.set("padding", new Insets3f(g.dpInt(24), g.dpInt(24), g.dpInt(24), g.dpInt(24)));
            
            /*GradientBackgroundComponent actionButton = 
                    new GradientBackgroundComponent(g.dpInt(4), 1000000, true,
                    0, 45,
                    new ColorStop(new ColorRGBA(1f, 0.8f, 0.235f, 1), 0.5f),
                    new ColorStop(new ColorRGBA(1f, 0.6f, 0.235f, 1), 0.51f),
                    new ColorStop(new ColorRGBA(1f, 0.6f, 0.235f, 1), 0.8f),
                    new ColorStop(new ColorRGBA(1f, 0.3f, 0.235f, 1), 1),
                    new ColorStop(new ColorRGBA(0.235f, 0.235f, 0.235f, 1), 0, true));*/
            GradientBackgroundComponent actionButton = 
                    new GradientBackgroundComponent(g.dpInt(4), 1000000, true,
                    0, 45,
                    new ColorStop(new ColorRGBA(1f, 0.3f, 0.235f, 1), 0.5f),
                    new ColorStop(new ColorRGBA(1f, 0.8f, 0.235f, 1), 1f),
                    new ColorStop(new ColorRGBA(0.235f, 0.235f, 0.235f, 1), 0, true));
            /*new ColorStop(new ColorRGBA(0.235f, 0.3f, 1f, 1), 0.5f),
                    new ColorStop(new ColorRGBA(0.435f, 0.8f, 1f, 1), 1f)*/
            ShadowBackgroundComponent actionShadow =
                    new ShadowBackgroundComponent(g.dpInt(10), ColorRGBA.BlackNoAlpha.clone());
            Insets3f actionPadding = new Insets3f(g.dpInt(20),
                    g.dpInt(20), g.dpInt(20), g.dpInt(20));
            attrs = styles.getSelector("firebutton", "spacerocks");
            attrs.set("background", actionButton.clone());
            attrs.set("backgroundShadow", actionShadow.clone());
            attrs.set("padding", actionPadding);
            attrs.set("buttonCommands", null);
            
            attrs = styles.getSelector("sonicblastbutton", "spacerocks");
            actionButton.setInnerColors(new ColorStop(new ColorRGBA(0.235f, 0.3f, 1f, 1), 0.5f),
                    new ColorStop(new ColorRGBA(0.435f, 0.8f, 1f, 1), 1f));
            /*new GradientBackgroundComponent(g.dpInt(2), 10000, true, 0, 0,
                    new ColorStop(new ColorRGBA(0.235f, 0.3f, 1f, 1), 0.5f),
                    new ColorStop(new ColorRGBA(0.435f, 0.8f, 1f, 1), 1f),
                    new ColorStop(new ColorRGBA(1, 1, 1, 1), 0, true));*/
            attrs.set("background", actionButton.clone());
            attrs.set("backgroundShadow", actionShadow.clone());
            attrs.set("padding", actionPadding);
            attrs.set("buttonCommands", null);
            
            attrs = styles.getSelector("menubutton", "spacerocks");
            attrs.set("background", null);
            attrs.set("shadowColor", null);
            attrs.set("padding", null);
            attrs.set("icon", new IconComponent("Interface/Texture/menu_button.png", true));
            attrs.set("buttonCommands", null);
        } else if (currentStep == 6) {
            
            sr2 = (Node)((Node)app.getAssetManager().loadModel("Models/SR2_jME.j3o")).getChild(0);
            sr2.removeFromParent();
            sr2.getChild(0).rotate(0, FastMath.PI, 0);
            
            Vector2[] verts = new Vector2[6];
            verts[0] = new Vector2(-1.01851, 8.19423);
            verts[1] = new Vector2(-5.72006, -5.07746);
            verts[2] = new Vector2(-6.30621, -8.16247);
            
            verts[5] = new Vector2(1.01851, 8.19423);
            verts[4] = new Vector2(5.72006, -5.07746);
            verts[3] = new Vector2(6.30621, -8.16247);
            
            verts = org.dyn4j.geometry.Geometry.cleanse(verts);
            srBody = new Body();
            Polygon polygon = org.dyn4j.geometry.Geometry.createPolygon(verts);
            BodyFixture fixture = new BodyFixture(polygon);
            fixture.setFriction(0);
            srBody.addFixture(fixture);
            srBody.setAngularDamping(0);
            srBody.setLinearDamping(0);
            srBody.setMass(MassType.INFINITE);
        } else if (currentStep == 8) {
            
            Node rootAsteroids = (Node)((Node)app.getAssetManager().loadModel("Models/Asteroids.j3o")).getChild(0);
            Node ast = (Node)rootAsteroids.getChild(0);
            Node shapes = (Node)rootAsteroids.getChild(1);
            asteroids = new Asteroid[ast.getChildren().size()];
            
            for (int i = 0; i < asteroids.length; i++) {
                Node asteroid = (Node)ast.getChild(i);
                Asteroid a = new Asteroid(asteroid,
                        ((Geometry)((Node)shapes.getChild(i)).getChild(0)).getMesh());
                asteroids[i] = a;
            }
            
            for (Asteroid a : asteroids)
                a.asteroid.removeFromParent();
        } else if (currentStep == 9) {
            Node node = (Node)app.getAssetManager().loadModel("Models/AsteroidCrystal.j3o");
            blueAsteroidPup = (Node)((Node)node.getChild(0)).getChild(0);
            
            redAsteroidPup = (Node)blueAsteroidPup.clone();
            Material mat = app.getAssetManager().loadMaterial("Materials/crystal_red.j3m");
            ((Geometry)redAsteroidPup.getChild(0)).setMaterial(mat);
            
            Geometry g = new Geometry("PUPGlow", new CenterQuad(4.5f, 4.5f));
            mat = new Material(app.getAssetManager(),
                "MatDefs/Unshaded/circle_glow.j3md");
            mat.setColor("Color", new ColorRGBA(0.2f, 0.6f, 1f, 1f));
            mat.setColor("Color2", new ColorRGBA(0f, 0.4f, 0.8f, 0.5f));
            mat.setColor("Color3", new ColorRGBA(0f, 0.4f, 0.8f, 0f));
            mat.setFloat("Pos1", 0.3f);
            mat.setFloat("Pos2", 0.6f);
            mat.setFloat("X", 0.5f);
            mat.setFloat("Y", 0.5f);
            mat.setFloat("Alpha", 0.45f);

            mat.getAdditionalRenderState().setDepthTest(false);
            g.setMaterial(mat);
            g.setQueueBucket(RenderQueue.Bucket.Transparent);
            blueAsteroidPup.attachChild(g);
            
            g = g.clone();
            mat = g.getMaterial();
            mat.setColor("Color", new ColorRGBA(1f, 0.6f, 0.2f, 1f));
            mat.setColor("Color2", new ColorRGBA(0.8f, 0.4f, 0f, 0.5f));
            mat.setColor("Color3", new ColorRGBA(0.8f, 0.4f, 0f, 0f));
            redAsteroidPup.attachChild(g);
        } else if (currentStep == 10) {
            PUP_Health = (Node)((Node)((Node)app.getAssetManager()
                    .loadModel("Models/PUP/PUP_Health.j3o"))
                    .getChild(0)).getChild(0);
            PUP_Health.setLocalScale(3);

            Geometry g = new Geometry("PUPGlow", new CenterQuad(6, 6));
            Material mat = new Material(app.getAssetManager(),
                    "MatDefs/Unshaded/circle_glow.j3md");
            mat.setColor("Color", new ColorRGBA(1f, 0.2f, 0.2f, 1f));
                mat.setColor("Color2", new ColorRGBA(0.8f, 0f, 0f, 0.5f));
                mat.setColor("Color3", new ColorRGBA(0.8f, 0f, 0f, 0f));
            mat.setFloat("Pos1", 0.0f);
            mat.setFloat("Pos2", 0.6f);
            mat.setFloat("X", 0.5f);
            mat.setFloat("Y", 0.5f);
            mat.setFloat("Alpha", 0.65f);
            mat.getAdditionalRenderState().setDepthTest(false);
            g.setMaterial(mat);
            g.setQueueBucket(RenderQueue.Bucket.Transparent);

            PUP_Health.attachChild(g);
        } else if (currentStep == 11) {
            PUP_Sonic = (Node)((Node)((Node)app.getAssetManager()
                    .loadModel("Models/PUP/PUP_Sonic.j3o"))
                    .getChild(0)).getChild(0);
            PUP_Sonic.setLocalScale(4);
            
            Geometry g = new Geometry("PUPGlow", new CenterQuad(5f, 5f));
            Material mat = new Material(app.getAssetManager(),
                    "MatDefs/Unshaded/circle_glow.j3md");
            mat.setColor("Color", new ColorRGBA(0f, 0.25f, 1f, 1f));
                mat.setColor("Color2", new ColorRGBA(0f, 0.05f, 0.8f, 0.5f));
                mat.setColor("Color3", new ColorRGBA(0f, 0.05f, 0.8f, 0f));
            mat.setFloat("Pos1", 0.0f);
            mat.setFloat("Pos2", 0.6f);
            mat.setFloat("X", 0.5f);
            mat.setFloat("Y", 0.5f);
            mat.setFloat("Alpha", 0.65f);
            mat.getAdditionalRenderState().setDepthTest(false);
            g.setMaterial(mat);
            g.setQueueBucket(RenderQueue.Bucket.Transparent);
            
            PUP_Sonic.attachChild(g);
        } else if (currentStep == 12) {
            PUP_Energy = (Node)((Node)((Node)app.getAssetManager()
                    .loadModel("Models/PUP/PUP_Energy.j3o"))
                    .getChild(0)).getChild(0);
            PUP_Energy.setLocalScale(5);
            
            Geometry g = new Geometry("PUPGlow", new CenterQuad(4f, 4f));
            Material mat = new Material(app.getAssetManager(),
                    "MatDefs/Unshaded/circle_glow.j3md");
            mat.setColor("Color", new ColorRGBA(0.6f, 0.2f, 1f, 1f));
                mat.setColor("Color2", new ColorRGBA(0.4f, 0f, 1f, 0.4f));
                mat.setColor("Color3", new ColorRGBA(0.4f, 0f, 1f, 0f));
            mat.setFloat("Pos1", 0.2f);
            mat.setFloat("Pos2", 0.6f);
            mat.setFloat("X", 0.5f);
            mat.setFloat("Y", 0.5f);
            mat.setFloat("Alpha", 0.65f);
            mat.getAdditionalRenderState().setDepthTest(false);
            g.setMaterial(mat);
            g.setQueueBucket(RenderQueue.Bucket.Transparent);
            
            PUP_Energy.attachChild(g);

            System.gc();
        }
        
        currentStep++;
        
        float perc = Math.min(currentStep, maxSteps) / maxSteps;
        pb.setProgressPercent(perc);
        pb.setMessage(Integer.toString((int)Math.floor(perc * 100)) + "%");
    }
}
