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

import com.atr.spacerocks.SpaceRocks;
import com.atr.spacerocks.dynamo.UIAnimator;
import com.atr.spacerocks.dynamo.UIAnimator.DIR;
import com.atr.spacerocks.ui.UI;
import com.atr.spacerocks.util.Callback;
import com.jme3.app.state.AbstractAppState;
import com.jme3.math.Vector3f;
import com.simsilica.lemur.Button;
import com.simsilica.lemur.Command;
import com.simsilica.lemur.Container;
import com.simsilica.lemur.FillMode;
import com.simsilica.lemur.GuiGlobals;
import com.simsilica.lemur.Insets3f;
import com.simsilica.lemur.Label;
import com.simsilica.lemur.component.StackLayout;
import com.simsilica.lemur.component.VBoxLayout;
import com.simsilica.lemur.component.VBoxLayout.HAlign;
import com.simsilica.lemur.style.ElementId;

/**
 *
 * @author Adam T. Ryder
 * <a href="http://1337atr.weebly.com">http://1337atr.weebly.com</a>
 */
public class Credits extends AbstractAppState {
    private static final float DELAY = 0.35f;
    private static final float INLENGTH = 1.5f;
    private static final float OUTLENGTH = 1f;
    
    private final Container container;
    private final Container stackContainer;
    private final Container textContainer;
    private final Button backButton;
    
    private final GuiGlobals gui;
    private final SpaceRocks app;
    private final Credits credits;
    
    private final ElementId titleId = new ElementId("titlelabel");
    
    private boolean active = false;
    private boolean closing = false;
    private boolean transitioning = true;
    
    private int currentCredit = 0;
    
    private final float length = 7f;
    private float tme = 0;
    
    private Label l1;
    private Label l2;
    private Label l3;
    private Label l4;
    
    private final Callback textOutCB = new Callback() {
        @Override
        public void call() {
            if (!closing && active) {
                addCredit();
                return;
            } else if (!closing)
                return;
            
            active = false;
            backButton.addControl(new UIAnimator(false, DIR.DOWN,
                    1f, 0.2f, gui.dpInt(64), new Callback() {
                        @Override
                        public void call() {
                            app.getStateManager().detach(credits);
                            UI.displayMainMenu();
                        }
                    }));
        }
    };
    
    private final Callback textInCB = new Callback() {
        @Override
        public void call() {
            transitioning = false;
            if (closing)
                tme = length;
        }
    };
    
    public Credits(final SpaceRocks app) {
        credits = this;
        this.app = app;
        gui = GuiGlobals.getInstance();
        
        container = new Container(new VBoxLayout(0, FillMode.First, true, HAlign.Left, true));
        container.setPadding(new Insets3f(gui.dpInt(32), 0, gui.dpInt(32), 0));
        container.setPreferredSize(new Vector3f(app.getWidth(), app.getHeight(), 50));
        stackContainer = new Container(new StackLayout());
        container.addChild(stackContainer, true, true);
        
        textContainer = new Container(new VBoxLayout(gui.dpInt(5), FillMode.None, false,
            HAlign.Center, true));
        stackContainer.addChild(textContainer);
        
        backButton = new Button(SpaceRocks.res.getString("back"));
        backButton.setEnabled(false);
        
        backButton.addClickCommands(new Command<Button>() {
            @Override
            public void execute(Button source) {
                if (!UI.isEnabled())
                    return;
                closing = true;
                backButton.setEnabled(false);
                tme = length;
            }
        });
        
        container.addChild(backButton, HAlign.Center, false, false);
        UI.container.addChild(container);
        
        backButton.addControl(new UIAnimator(true, DIR.DOWN, 1f, 0.3f,
                gui.dpInt(64), new Callback() {
                    @Override
                    public void call() {
                        active = true;
                        backButton.setEnabled(true);
                        
                        addCredit();
                    }
                }));
    }
    
    @Override
    public void update(float tpf) {
        if (!active || transitioning)
            return;
        
        tme += tpf;
        if (tme < length)
            return;
        
        tme = 0;
        removeCredit();
    }
    
    private void removeCredit() {
        transitioning = true;
        l1.addControl(new UIAnimator(false,
                l4 == null ? DIR.RIGHT : DIR.UP, OUTLENGTH, 0f, gui.dpInt(64),
            textOutCB));
        if (l2 != null) {
            l2.addControl(new UIAnimator(false, DIR.LEFT, OUTLENGTH, 0f, gui.dpInt(64),
                null));
        }
        if (l3 != null) {
            l3.addControl(new UIAnimator(false, DIR.RIGHT, OUTLENGTH, 0f, gui.dpInt(64),
                null));
        }
        if (l4 != null) {
            l4.addControl(new UIAnimator(false, DIR.DOWN,OUTLENGTH, 0f, gui.dpInt(64),
                null));
        }
        
        currentCredit++;
    }
    
    private void addCredit() {
        textContainer.clearChildren();
        l1 = null;
        l2 = null;
        l3 = null;
        l4 = null;
        
        switch(currentCredit) {
            case 0:
                l1 = new Label(SpaceRocks.res.getString("developedby"), titleId);
                l2 = new Label("Adam T. Ryder");
                textContainer.addChild(l1);
                textContainer.addChild(l2);
                l1.addControl(new UIAnimator(true, DIR.LEFT, INLENGTH, DELAY, gui.dpInt(64),
                    textInCB));
                l2.addControl(new UIAnimator(true, DIR.RIGHT, INLENGTH, DELAY, gui.dpInt(64),
                    null));
                break;
            case 1:
                l1 = new Label(SpaceRocks.res.getString("poweredby"), titleId);
                l2 = new Label("jMonkeyEngine 3.1");
                textContainer.addChild(l1);
                textContainer.addChild(l2);
                l1.addControl(new UIAnimator(true, DIR.LEFT, INLENGTH, DELAY, gui.dpInt(64),
                    textInCB));
                l2.addControl(new UIAnimator(true, DIR.RIGHT, INLENGTH, DELAY, gui.dpInt(64),
                    null));
                break;
            case 2:
                l1 = new Label(SpaceRocks.res.getString("frenchtranslation"), titleId);
                l2 = new Label("Jennifer S. Howard");
                textContainer.addChild(l1);
                textContainer.addChild(l2);
                l1.addControl(new UIAnimator(true, DIR.LEFT, INLENGTH, DELAY, gui.dpInt(64),
                    textInCB));
                l2.addControl(new UIAnimator(true, DIR.RIGHT, INLENGTH, DELAY, gui.dpInt(64),
                    null));
                break;
            case 3:
                l1 = new Label("Nativity in Glass", titleId);
                l2 = new Label(SpaceRocks.res.getString("artist") + ": Sci-Fi Industries");
                l3 = new Label(SpaceRocks.res.getString("album") + ": Half Day Half Minute");
                l4 = new Label("CC BY-NC-SA 3.0");
                textContainer.addChild(l1);
                textContainer.addChild(l2);
                textContainer.addChild(l3);
                textContainer.addChild(l4);
                l1.addControl(new UIAnimator(true, DIR.UP, INLENGTH, DELAY, gui.dpInt(64),
                    textInCB));
                l2.addControl(new UIAnimator(true, DIR.RIGHT, INLENGTH, DELAY, gui.dpInt(64),
                    null));
                l3.addControl(new UIAnimator(true, DIR.LEFT, INLENGTH, DELAY, gui.dpInt(64),
                    null));
                l4.addControl(new UIAnimator(true, DIR.DOWN, INLENGTH, DELAY, gui.dpInt(64),
                    null));
                break;
            case 4:
                l1 = new Label("Azimutez", titleId);
                l2 = new Label(SpaceRocks.res.getString("artist") + ": Sci-Fi Industries");
                l3 = new Label(SpaceRocks.res.getString("album") + ": Blame the Lord");
                l4 = new Label("CC BY-NC-SA 3.0");
                textContainer.addChild(l1);
                textContainer.addChild(l2);
                textContainer.addChild(l3);
                textContainer.addChild(l4);
                l1.addControl(new UIAnimator(true, DIR.UP, INLENGTH, DELAY, gui.dpInt(64),
                    textInCB));
                l2.addControl(new UIAnimator(true, DIR.RIGHT, INLENGTH, DELAY, gui.dpInt(64),
                    null));
                l3.addControl(new UIAnimator(true, DIR.LEFT, INLENGTH, DELAY, gui.dpInt(64),
                    null));
                l4.addControl(new UIAnimator(true, DIR.DOWN, INLENGTH, DELAY, gui.dpInt(64),
                    null));
                break;
            case 5:
                l1 = new Label("Ribeira Grande", titleId);
                l2 = new Label(SpaceRocks.res.getString("artist") + ": Sci-Fi Industries");
                l3 = new Label(SpaceRocks.res.getString("album") + ": Drum, Cone and Barricade");
                l4 = new Label("CC BY-NC-SA 3.0");
                textContainer.addChild(l1);
                textContainer.addChild(l2);
                textContainer.addChild(l3);
                textContainer.addChild(l4);
                l1.addControl(new UIAnimator(true, DIR.UP, INLENGTH, DELAY, gui.dpInt(64),
                    textInCB));
                l2.addControl(new UIAnimator(true, DIR.RIGHT, INLENGTH, DELAY, gui.dpInt(64),
                    null));
                l3.addControl(new UIAnimator(true, DIR.LEFT, INLENGTH, DELAY, gui.dpInt(64),
                    null));
                l4.addControl(new UIAnimator(true, DIR.DOWN, INLENGTH, DELAY, gui.dpInt(64),
                    null));
                break;
            case 6:
                l1 = new Label("Te Motive", titleId);
                l2 = new Label(SpaceRocks.res.getString("artist") + ": Sci-Fi Industries");
                l3 = new Label(SpaceRocks.res.getString("album") + ": Half Day Half Minute");
                l4 = new Label("CC BY-NC-SA 3.0");
                textContainer.addChild(l1);
                textContainer.addChild(l2);
                textContainer.addChild(l3);
                textContainer.addChild(l4);
                l1.addControl(new UIAnimator(true, DIR.UP, INLENGTH, DELAY, gui.dpInt(64),
                    textInCB));
                l2.addControl(new UIAnimator(true, DIR.RIGHT, INLENGTH, DELAY, gui.dpInt(64),
                    null));
                l3.addControl(new UIAnimator(true, DIR.LEFT, INLENGTH, DELAY, gui.dpInt(64),
                    null));
                l4.addControl(new UIAnimator(true, DIR.DOWN, INLENGTH, DELAY, gui.dpInt(64),
                    null));
                break;
            default:
                active = false;
                backButton.setEnabled(false);
                backButton.addControl(new UIAnimator(false, DIR.DOWN,
                        1f, 0.2f, gui.dpInt(64), new Callback() {
                            @Override
                            public void call() {
                                app.getStateManager().detach(credits);
                                UI.displayMainMenu();
                            }
                        }));
        }
    }
}
