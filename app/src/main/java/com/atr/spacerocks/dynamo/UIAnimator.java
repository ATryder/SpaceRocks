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
package com.atr.spacerocks.dynamo;

import com.atr.math.GMath;
import com.atr.spacerocks.util.Callback;
import com.atr.spacerocks.util.Tools;
import com.jme3.math.Vector3f;
import com.jme3.renderer.RenderManager;
import com.jme3.renderer.ViewPort;
import com.jme3.scene.Spatial;
import com.jme3.scene.control.AbstractControl;
import com.simsilica.lemur.GuiGlobals;
import com.simsilica.lemur.Panel;

/**
 *
 * @author Adam T. Ryder
 * <a href="http://1337atr.weebly.com">http://1337atr.weebly.com</a>
 */
public class UIAnimator extends AbstractControl {
    public enum DIR {
        UP,
        DOWN,
        LEFT,
        RIGHT
    }
    
    public final boolean moveIn;
    public final DIR direction;
    private boolean init = true;
    private boolean frameTwo = false;
    private boolean fin = false;
    private final float moveAmount;
    
    private final Vector3f fromLoc = new Vector3f();
    private final Vector3f toLoc = new Vector3f();
    private final Vector3f loc = new Vector3f();
    
    private float alpha;
    
    private float delayLength;
    private float delay = 0;
    private float length;
    private float tme = 0;
    private float perc = 0;
    
    private final Callback cb;
    
    public UIAnimator(boolean moveIn, DIR direction, Callback cb) {
        this(moveIn, direction, 64, cb);
    }
    
    public UIAnimator(boolean moveIn, DIR direction, int distance, Callback cb) {
        this(moveIn, direction, 0.5f, 0f, distance, cb);
    }
    
    public UIAnimator(boolean moveIn, DIR direction, float animLength, float delay, int distance,
            Callback cb) {
        this.moveIn = moveIn;
        alpha = moveIn ? 0 : 1;
        this.direction = direction;
        
        moveAmount = GuiGlobals.getInstance().dpInt(distance);
        
        this.cb = cb;
        this.length = animLength;
        this.delayLength = delay;
    }
    
    @Override
    public void setSpatial(Spatial s) {
        super.setSpatial(s);
        if (spatial == null)
            return;
        
        if (moveIn) {
            ((Panel) spatial).setAlpha(0);
        } else
            frameTwo = true;
    }
    
    @Override
    public void controlUpdate(float tpf) {
        if (init) {
            if (moveIn) {
                toLoc.set(spatial.getLocalTranslation());
                fromLoc.set(toLoc);
                switch(direction) {
                    case UP:
                        fromLoc.y += moveAmount;
                        break;
                    case DOWN:
                        fromLoc.y -= moveAmount;
                        break;
                    case LEFT:
                        fromLoc.x -= moveAmount;
                        break;
                    case RIGHT:
                        fromLoc.x += moveAmount;
                        break;
                }
            } else {
                fromLoc.set(spatial.getLocalTranslation());
                toLoc.set(fromLoc);
                switch(direction) {
                    case UP:
                        toLoc.y += moveAmount;
                        break;
                    case DOWN:
                        toLoc.y -= moveAmount;
                        break;
                    case LEFT:
                        toLoc.x -= moveAmount;
                        break;
                    case RIGHT:
                        toLoc.x += moveAmount;
                        break;
                }
            }
            
            init = false;

            return;
        }

        if (!frameTwo) {
            frameTwo = true;
            return;
        }

        if (fin) {
            spatial.removeControl(this);

            if (cb != null)
                cb.call();

            return;
        }

        if (delayLength > Tools.EPSILON && delay < delayLength) {
            delay += tpf;
            if (delay >= delayLength) {
                tme += delay - delayLength;
            } else
                return;
        } else
            tme += tpf;
        
        perc = tme / length;
        if (perc >= 1) {
            if (moveIn) {
                alpha = 1;
                ((Panel)spatial).setAlpha(alpha);
                spatial.setLocalTranslation(toLoc);
                spatial.removeControl(this);

                if (cb != null)
                    cb.call();

                return;
            }

            fin = true;
            alpha = 0;
            ((Panel)spatial).setAlpha(alpha);
            spatial.setLocalTranslation(toLoc);
            
            return;
        }
        
        if (moveIn) {
            GMath.smoothStopVector3(perc, loc, fromLoc, toLoc);
            alpha = GMath.smoothStopFloat(perc, 0, 1);
        } else {
            GMath.smoothStartVector3(perc, loc, fromLoc, toLoc);
            alpha = GMath.smoothStartFloat(perc, 1, 0);
        }
        
        spatial.setLocalTranslation(loc);
        ((Panel)spatial).setAlpha(alpha);
    }
    
    @Override
    public void controlRender(RenderManager rm, ViewPort vp) {
        
    }
}
