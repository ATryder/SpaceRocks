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
package com.atr.spacerocks.dynamo.component;

import com.atr.jme.font.TrueTypeBMP;
import com.atr.jme.font.TrueTypeFont;
import com.atr.jme.font.glyph.GlyphBMP;
import com.jme3.math.Vector3f;
import com.jme3.scene.Spatial;
import com.simsilica.lemur.Button;
import com.simsilica.lemur.FillMode;
import com.simsilica.lemur.GuiGlobals;
import com.simsilica.lemur.Label;
import com.simsilica.lemur.Panel;
import com.simsilica.lemur.component.VBoxLayout;
import com.simsilica.lemur.component.VBoxLayout.HAlign;
import com.simsilica.lemur.core.GuiControl;
import com.simsilica.lemur.event.CursorButtonEvent;
import com.simsilica.lemur.event.CursorEventControl;
import com.simsilica.lemur.event.CursorListener;
import com.simsilica.lemur.event.CursorMotionEvent;
import com.simsilica.lemur.style.ElementId;
import com.simsilica.lemur.style.Styles;

/**
 *
 * @author Adam T. Ryder
 * <a href="http://1337atr.weebly.com">http://1337atr.weebly.com</a>
 */
public class LetterPicker extends Panel {
    public static final String ELEMENT_ID = "letterpicker";
    
    private boolean enabled = true;
    
    private final Button up;
    private final Button down;
    private final Label letterDisplay;
    
    private final StringBuilder letters;
    private final StringBuilder letter = new StringBuilder();
    private int position = 0;
    
    public LetterPicker(String letters) {
        super(false, new ElementId(ELEMENT_ID), null);
        
        Styles styles = GuiGlobals.getInstance().getStyles();
        styles.initializeStyles(getClass());
        
        this.letters = new StringBuilder(letters);
        letter.appendCodePoint(letters.codePointAt(0));
        
        
        VBoxLayout layout = new VBoxLayout(
                GuiGlobals.getInstance().dpInt(12), FillMode.None, false, HAlign.Center, true);
        
        up = new Button("", getElementId().child("button"));
        CursorEventControl.addListenersToSpatial(up, new ButtonMonitor(true));
        down = new Button("", getElementId().child("button"));
        CursorEventControl.addListenersToSpatial(down, new ButtonMonitor(false));
        letterDisplay = new Label(letter.toString(), getElementId().child("label"));
        
        TrueTypeBMP ttf = (TrueTypeBMP)letterDisplay.getFont();
        GlyphBMP[] glyphs = (GlyphBMP[])ttf.getGlyphs(letters);
        float maxWidth = 0;
        for (GlyphBMP glyph : glyphs) {
            if (glyph.getXAdvance() > maxWidth)
                maxWidth = glyph.getXAdvance();
        }
        
        letterDisplay.setPreferredSize(new Vector3f(maxWidth + 1,
                ttf.getScaledLineHeightInt(), 5));
        
        layout.addChild(up);
        layout.addChild(letterDisplay);
        layout.addChild(down);
        
        getControl(GuiControl.class).setLayout(layout);
        
        //styles.applyStyles(this, getElementId().getId(), null);
    }
    
    public void setEnabled(boolean enabled) {
        if (this.enabled == enabled)
            return;
        
        this.enabled = enabled;
    }
    
    public String getSelection() {
        return letter.toString();
    }
    
    private class ButtonMonitor implements CursorListener {
        private final boolean dirUp;
        
        private boolean pressed = false;
        private long interval = 0;
        private final long rate = 1000 / 8;
        private long lastMillis;
        
        public ButtonMonitor(boolean upButton) {
            this.dirUp = upButton;
            lastMillis = System.currentTimeMillis();
        }
        
        @Override
        public void cursorEntered( CursorMotionEvent event, Spatial target, Spatial capture ) {
        }
        
        @Override
        public void cursorExited( CursorMotionEvent event, Spatial target, Spatial capture ) {
        }
        
        @Override
        public void cursorMoved( CursorMotionEvent event, Spatial target, Spatial capture ) {
            event.setConsumed();
            if (!enabled)
                return;
            
            if (event.getCollision() == null || !pressed) {
                lastMillis = System.currentTimeMillis();
                interval = 0;
                return;
            }
            
            if (interval >= rate) {
                if (dirUp) {
                    position = (position > 0) ? position - 1 : letters.length() - 1;
                } else
                    position = (position < letters.length() - 1) ? position + 1 : 0;
                letter.delete(0, letter.length());
                letter.appendCodePoint(letters.codePointAt(position));
                letterDisplay.setText(letter.toString());
                interval = 0;
            } else
                interval += System.currentTimeMillis() - lastMillis;
            lastMillis = System.currentTimeMillis();
        }
        
        @Override
        public void cursorButtonEvent( CursorButtonEvent event, Spatial target, Spatial capture ) {
            event.setConsumed();
            if (!enabled)
                return;
            
            if (target == null || !target.equals(capture)) {
                pressed = false;
                return;
            }
            
            lastMillis = System.currentTimeMillis();
            pressed = event.isPressed();
            if (event.isPressed()) {
                interval = rate;
            } else
                interval = 0;
        }
    }
}
