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

import com.atr.jme.font.TrueTypeBMP;
import com.atr.jme.font.util.Style;
import com.atr.spacerocks.SpaceRocks;
import com.atr.spacerocks.ui.UI;
import com.atr.spacerocks.util.Callback;
import com.jme3.app.state.AbstractAppState;
import com.jme3.math.Vector3f;
import com.simsilica.lemur.Button;
import com.simsilica.lemur.Container;
import com.simsilica.lemur.FillMode;
import com.simsilica.lemur.GuiGlobals;
import com.simsilica.lemur.Insets3f;
import com.simsilica.lemur.Label;
import com.simsilica.lemur.ProgressBar;
import com.simsilica.lemur.component.HBoxLayout;
import com.simsilica.lemur.component.HBoxLayout.VAlign;
import com.simsilica.lemur.style.Attributes;
import com.simsilica.lemur.style.ElementId;
import com.simsilica.lemur.style.Styles;

import java.util.LinkedList;

/**
 *
 * @author Adam T. Ryder
 * <a href="http://1337atr.weebly.com">http://1337atr.weebly.com</a>
 */
public class Restart extends AbstractAppState {
    private final SpaceRocks app;
    
    private final LinkedList<TrueTypeBMP> fonts = new LinkedList<TrueTypeBMP>();
    private int currentFont = 0;
    
    private final ProgressBar pb;
    
    private boolean active = false;
    
    public Restart(SpaceRocks app) {
        this.app = app;
        UI.setEnabled(false);
        SpaceRocks.PAUSED.set(true);
        
        UI.fade(false, 0, new Callback() {
            @Override
            public void call() {
                active = true;
            }
        });
        
        GuiGlobals g = GuiGlobals.getInstance();
        Styles styles = g.getStyles();
        String defaultPreText = SpaceRocks.res.getString("preloadchars");
        g.setTrueTypePreloadCharacters("0123456789%");
        
        Attributes attrs = styles.getSelector("progress", "label", "spacerocks");
        if (attrs.get("font") == null) {
            attrs.set("font", g.loadFontDP("Interface/Fonts/space age.ttf", Style.Italic, 12, g.dpInt(2)));
        }
        TrueTypeBMP ttf = attrs.get("font");
        ttf.reloadTexture();
        
        HBoxLayout hbox = new HBoxLayout(0, FillMode.ForcedEven, false, VAlign.Center, true);
        Container container = new Container(hbox);
        container.setPadding(new Insets3f(0, g.dpInt(16), 0, g.dpInt(16)));
        container.setLocalTranslation(0, app.getHeight(), 0);
        container.setPreferredSize(new Vector3f(app.getWidth(), app.getHeight(), 3));
        container.setUserData("layer", 2);
        
        pb = new ProgressBar();
        float perc = 0;
        pb.setProgressPercent(perc);
        pb.setMessage("0%");
        container.addChild(pb, VAlign.Center, false, true);
        app.getStateManager().getState(Fade.class).getLayout().addChild(container);
        
        g.setTrueTypePreloadCharacters(defaultPreText);
        
        ttf = (TrueTypeBMP)new Label("reload").getFont();
        fonts.add(ttf);
        ttf = (TrueTypeBMP)new Label("Reload", new ElementId("titlelabel")).getFont();
        if (!fonts.contains(ttf))
            fonts.add(ttf);
        ttf = (TrueTypeBMP)new Label("Reload", new ElementId("cliplabel")).getFont();
        if (!fonts.contains(ttf))
            fonts.add(ttf);
        ttf = (TrueTypeBMP)new Label("Reload", new ElementId("letterpicker").child("label")).getFont();
        if (!fonts.contains(ttf))
            fonts.add(ttf);
        ttf = (TrueTypeBMP)new Button("Reload").getFont();
        if (!fonts.contains(ttf))
            fonts.add(ttf);
    }
    
    @Override
    public void update(float tpf) {
        if (!active)
            return;
        
        if (currentFont == fonts.size()) {
            app.getStateManager().detach(this);
            UI.setEnabled(true);
            if (app.getStateManager().getState(GameState.class) == null
                    || !app.getUIView().isEnabled())
                SpaceRocks.PAUSED.set(false);
            UI.fade(true, 0.5f, null);
            
            return;
        }
        
        fonts.get(currentFont).reloadTexture();
        
        currentFont++;
        float perc = (float)currentFont / fonts.size();
        pb.setProgressPercent(perc);
        pb.setMessage(Integer.toString((int)Math.floor(perc * 100)) + "%");
    }
}
