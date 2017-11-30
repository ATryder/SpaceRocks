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

import com.atr.math.GMath;
import com.atr.spacerocks.util.Callback;
import com.atr.spacerocks.util.Tools;
import com.jme3.app.state.AbstractAppState;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.renderer.Camera;
import com.jme3.renderer.RenderManager;
import com.jme3.renderer.ViewPort;
import com.jme3.renderer.queue.RenderQueue;
import com.jme3.scene.Node;
import com.simsilica.lemur.Container;
import com.simsilica.lemur.GuiGlobals;
import com.simsilica.lemur.component.QuadBackgroundComponent;
import com.simsilica.lemur.component.StackLayout;

/**
 *
 * @author Adam T. Ryder
 * <a href="http://1337atr.weebly.com">http://1337atr.weebly.com</a>
 */
public class Fade extends AbstractAppState {
    private final ViewPort viewPort;
    private final Node node = new Node("Fade Root");
    
    private final GuiGlobals gui;
    private final Container container;
    private final StackLayout layout;
    
    private float fadeLength = 1f;
    private float fadeTme = 0f;
    private boolean in = false;
    private int frameSkip = 0;
    
    private Callback cb;
    private boolean fin = false;
    
    public Fade(Camera guiCam, RenderManager renderManager) {
        Camera cam = guiCam.clone();
        viewPort = renderManager.createPostView("Fade ViewPort", cam);
        viewPort.setClearFlags(false, false, false);
        viewPort.attachScene(node);
        
        gui = GuiGlobals.getInstance();
        gui.setupGuiComparators(viewPort);
        
        layout = new StackLayout(true);
        container = new Container(layout);
        QuadBackgroundComponent qbc = new QuadBackgroundComponent(ColorRGBA.Black.clone(),
                0, 0, 0.03f, false);
        container.setBackground(qbc);
        container.setLocalTranslation(0, guiCam.getHeight(), 0);
        container.setPreferredSize(new Vector3f(guiCam.getWidth(), guiCam.getHeight(), 35));
        node.attachChild(container);
        node.setQueueBucket(RenderQueue.Bucket.Gui);
        
        setEnabled(false);
        viewPort.setEnabled(false);
        
        node.updateGeometricState();
    }
    
    public void fadeOut(Callback cb) {
        fade(false, 1f, cb);
    }
    
    public void fadeIn(Callback cb) {
        fade(true, 1f, cb);
    }
    
    public void fade(boolean fadeIn, float fadeLength, Callback cb) {
        this.fadeLength = fadeLength;
        fadeTme = 0;
        fin = false;
        in = fadeIn;
        frameSkip = 0;
        if (fadeLength >= Tools.EPSILON) {
            container.setAlpha(fadeIn ? 1 : 0);
        } else
            container.setAlpha(fadeIn ? 0 : 1);
        setEnabled(true);
        viewPort.setEnabled(true);
        this.cb = cb;
        
        node.updateGeometricState();
    }
    
    public StackLayout getLayout() {
        return layout;
    }
    
    @Override
    public void update(float tpf) {
        if (!fin) {
            fadeTme += tpf;
            if (!in) {
                if (fadeTme < fadeLength) {
                    if (fadeLength >= Tools.EPSILON) {
                        container.setAlpha(GMath.smoothFloat(fadeTme / fadeLength, 0, 1));
                    } else
                        container.setAlpha(1);
                } else if (frameSkip == 2) {
                    fin = true;
                    frameSkip = 0;
                    if (cb != null)
                        cb.call();
                } else {
                    container.setAlpha(1);
                    frameSkip++;
                }
            } else {
                if (frameSkip < 2) {
                    frameSkip++;
                    fadeTme = 0;
                } else if (fadeTme < fadeLength) {
                    if (fadeLength >= Tools.EPSILON) {
                        container.setAlpha(GMath.smoothFloat(fadeTme / fadeLength, 1, 0));
                    } else
                        container.setAlpha(0);
                } else {
                    container.setAlpha(0);
                    fin = true;
                    frameSkip = 0;
                    setEnabled(false);
                    viewPort.setEnabled(false);
                    layout.clearChildren();
                    if (cb != null)
                        cb.call();
                }
            }
        }
        
        node.updateLogicalState(tpf);
        node.updateGeometricState();
    }
}
