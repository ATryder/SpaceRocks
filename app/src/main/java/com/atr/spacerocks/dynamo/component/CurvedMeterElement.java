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

import com.jme3.math.ColorRGBA;
import com.simsilica.lemur.GuiGlobals;
import com.simsilica.lemur.Panel;
import com.simsilica.lemur.core.GuiControl;
import com.simsilica.lemur.style.ElementId;

/**
 *
 * @author Adam T. Ryder
 * <a href="http://1337atr.weebly.com">http://1337atr.weebly.com</a>
 */
public class CurvedMeterElement extends Panel {
    public static final String ELEMENT_ID = "curvedmeter";
    public static final String LAYER_METER = "curvedmeter";
    
    private final CurvedMeterComponent cmc;
    
    public CurvedMeterElement(float angle, float endAngle, float gapAngle, float gap,
            int divisions, float meterWidth, float valueWidth,
            float scale, ColorRGBA valCol1, ColorRGBA valCol2,
            ColorRGBA meterColor, float height) {
        super(false, new ElementId(ELEMENT_ID), null);
        
        getControl(GuiControl.class).setLayerOrder(LAYER_INSETS, 
                                                   LAYER_BORDER, 
                                                   LAYER_BACKGROUND,
                                                   LAYER_PADDING,
                                                   LAYER_METER);
        
        cmc = new CurvedMeterComponent(angle, endAngle, gapAngle, gap, divisions,
                meterWidth, valueWidth, scale, valCol1, valCol2, meterColor, height);
        
        cmc.setLayer(3);
        getControl(GuiControl.class).setComponent(LAYER_METER, cmc);
        GuiGlobals.getInstance().getStyles().applyStyles(this, getElementId(), getStyle());
    }
    
    @Override
    public void setAlpha(float alpha) {
        super.setAlpha(alpha);
        cmc.setAlpha(alpha);
    }
    
    public void setPercent(float perc) {
        cmc.setPercent(perc);
    }
    
    public float getPercent() {
        return cmc.getPercent();
    }
}
