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

import com.atr.spacerocks.shape.CurvedMeter;
import com.atr.spacerocks.util.Tools;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.FastMath;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.simsilica.lemur.GuiGlobals;
import com.simsilica.lemur.component.AbstractGuiComponent;
import com.simsilica.lemur.core.GuiControl;

/**
 *
 * @author Adam T. Ryder
 * <a href="http://1337atr.weebly.com">http://1337atr.weebly.com</a>
 */
public class CurvedMeterComponent extends AbstractGuiComponent
                           implements Cloneable {
    
    private float zOffset = 0.01f;
    
    private final ColorRGBA col1;
    private final ColorRGBA col2;
    private final ColorRGBA meterColor;
    
    private final float meterAngle;
    private final float meterEndAngle;
    private final float gapAngle;
    private final float gap;
    private final float meterWidth;
    private final float valueWidth;
    private final float scale;
    
    private final int div;
    
    private CurvedMeter meter;
    private Geometry geom;
    
    private final Material material;
    private float percent = 1;
    
    private float alpha = 1;
    
    private int layer = 0;
    
    public CurvedMeterComponent(float angle, float endAngle, float gapAngle, float gap,
            int divisions, float meterWidth, float valueWidth,
            float scale, ColorRGBA valCol1, ColorRGBA valCol2,
            ColorRGBA meterColor, float height) {
        meterAngle = angle;
        meterEndAngle = endAngle;
        this.gapAngle = gapAngle;
        this.gap = gap;
        div = divisions;
        this.meterWidth = meterWidth;
        this.valueWidth = valueWidth;
        this.scale = scale;
        
        col1 = valCol1;
        col2 = valCol2;
        this.meterColor = meterColor;
        
        meter = new CurvedMeter(meterAngle, meterEndAngle,
                gapAngle, gap, div, meterWidth, valueWidth, scale,
                height, col1, col2, meterColor);
        geom = new Geometry("Curved Meter", meter);
        material = new Material(GuiGlobals.getInstance().getAssetManager(),
                "MatDefs/Unshaded/curvedmeter.j3md");
        material.setBoolean("useAA", GuiGlobals.getInstance().isSupportDerivatives());
        geom.setMaterial(material);
    }
    
    public void setZOffset(float offset) {
        zOffset = offset;
        invalidate();
    }
    
    public float getZOffset() {
        return zOffset;
    }
    
    @Override
    public CurvedMeterComponent clone() {
        CurvedMeterComponent cmc = new CurvedMeterComponent(meterAngle, meterEndAngle,
                gapAngle, gap, div, meterWidth, valueWidth, scale,
                col1.clone(), col2.clone(), meterColor.clone(), meter.getHeight());
        cmc.alpha = alpha;
        
        return cmc;
    }
    
    @Override
    public void attach( GuiControl parent ) {
        super.attach(parent);
        getNode().attachChild(geom);
    }

    @Override
    public void detach( GuiControl parent ) {
        if( geom != null )
            getNode().detachChild(geom);
        super.detach(parent);
    }
    
    public void setAlpha( float f ) {
        alpha = f;
        material.setFloat("Alpha", alpha);
    }
    
    public float getAlpha() {
        return alpha;
    }
    
    public void setPercent(float perc) {
        percent = FastMath.clamp(perc, 0f, 1f);
        material.setFloat("Percent", percent);
    }
    
    public float getPercent() {
        return percent;
    }
    
    @Override
    public void adjustSize(Vector3f size, boolean prefCalculated) {
        if (size.y - meter.getHeight() > Tools.EPSILON ||
                meter.getHeight() - size.y > Tools.EPSILON) {
            meter = new CurvedMeter(meterAngle, meterEndAngle,
                    gapAngle, gap, div, meterWidth, valueWidth, scale,
                    size.y, col1, col2, meterColor);
            if (geom != null) {
                getNode().detachChild(geom);
            }
            geom = new Geometry("Curved Meter", meter);
            geom.setMaterial(material);
            geom.setUserData("layer", layer);
            getNode().attachChild(geom);
        }
        
        size.x -= meter.getWidth();
        size.y -= meter.getHeight();
        size.z -= Math.abs(zOffset);
    }
    
    @Override
    public void calculatePreferredSize(Vector3f size) {
        size.x += meter.getWidth();
        size.y += meter.getHeight();
        size.z += Math.abs(zOffset);
    }
    
    @Override
    public void reshape(Vector3f pos, Vector3f size) {
        if (size.x - meter.getWidth() > Tools.EPSILON
                || meter.getWidth() - size.x > Tools.EPSILON
                || size.y - meter.getHeight() > Tools.EPSILON
                || meter.getHeight() - size.y > Tools.EPSILON) {
            meter = new CurvedMeter(meterAngle, meterEndAngle,
                    gapAngle, gap, div, meterWidth, valueWidth, scale,
                    size.y, col1, col2, meterColor);
            if (geom != null) {
                getNode().detachChild(geom);
            }
            geom = new Geometry("Curved Meter", meter);
            geom.setMaterial(material);
            geom.setUserData("layer", layer);
            getNode().attachChild(geom);
        }
        
        float x = meter.getRadius() > 0 ? meter.getRadius() - meter.getWidth()
                : meter.getRadius();
        geom.setLocalTranslation(pos.x - x, pos.y - (meter.getHeight() / 2), pos.z);
        
        pos.x += meter.getWidth();
        pos.y -= meter.getHeight();
        pos.z += zOffset;
        size.x -= meter.getWidth();
        size.y -= meter.getHeight();
        size.z -= Math.abs(zOffset);
    }
    
    public void setLayer(int layer) {
        if (geom != null)
            geom.setUserData("layer", layer);
        
        this.layer = layer;
    }
}
