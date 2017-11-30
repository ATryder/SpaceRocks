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

import com.atr.spacerocks.SpaceRocks;
import com.atr.spacerocks.effects.FlashCont;
import com.atr.spacerocks.shape.CenterQuad;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.renderer.RenderManager;
import com.jme3.renderer.ViewPort;
import com.jme3.renderer.queue.RenderQueue;
import com.jme3.scene.Geometry;
import com.jme3.scene.Spatial;
import com.jme3.scene.control.AbstractControl;

/**
 *
 * @author Adam T. Ryder
 * <a href="http://1337atr.weebly.com">http://1337atr.weebly.com</a>
 */
public class TutorialFlash extends AbstractControl {
    
    private static final float LENGTH = 0.2f;
    
    private final Material mat;
    
    private float tme = 0;
    
    public TutorialFlash(SpaceRocks app, Vector3f location, float radius) {
        this(app, location, radius,
                FlashCont.FLASH_COL1, FlashCont.FLASH_COL2, FlashCont.FLASH_COL3);
    }
    
    public TutorialFlash(SpaceRocks app, Vector3f location, float radius, ColorRGBA col1,
            ColorRGBA col2, ColorRGBA col3) {
        
        mat = new Material(app.getAssetManager(),
                "MatDefs/Unshaded/circle_glow.j3md");
        mat.setColor("Color", col1);
        mat.setColor("Color2", col2);
        mat.setColor("Color3", col3);
        mat.setFloat("Pos1", 0.7f);
        mat.setFloat("Pos2", 0.85f);
        mat.setFloat("Radius", 0.001f);
        mat.setFloat("X", 0.5f);
        mat.setFloat("Y", 0.5f);
        
        mat.getAdditionalRenderState().setDepthTest(false);
        
        Geometry g = new Geometry("Flash", new CenterQuad(radius * 2, radius * 2));
        g.setCullHint(Spatial.CullHint.Dynamic);
        g.setQueueBucket(RenderQueue.Bucket.Transparent);
        g.setLocalTranslation(location);
        g.setMaterial(mat);
        g.addControl(this);
        
        app.getRootNode().attachChild(g);
    }
    
    @Override
    public void controlUpdate(float tpf) {
        tme += tpf;
        if (tme >= LENGTH) {
            spatial.removeFromParent();
            return;
        }
        
        float perc = tme / LENGTH;
        mat.setFloat("Alpha", 1f - perc);
        mat.setFloat("Radius", (0.499f * perc) + 0.001f);
    }
    
    @Override
    public void controlRender(RenderManager rm, ViewPort vp) {
        
    }
}
