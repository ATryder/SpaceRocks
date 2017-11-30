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
package com.atr.spacerocks.control;

import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.renderer.RenderManager;
import com.jme3.renderer.ViewPort;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.control.AbstractControl;
import org.dyn4j.dynamics.Body;
import org.dyn4j.geometry.Transform;

/**
 *
 * @author Adam T. Ryder
 * <a href="http://1337atr.weebly.com">http://1337atr.weebly.com</a>
 */
public abstract class BodyCont extends AbstractControl {
    public static final String key = "BodyControl";
    
    protected Body body;
    
    public BodyCont(Body body) {
        this.body = body;
        body.setUserData(this);
    }
    
    @Override
    public void setSpatial(Spatial spatial) {
        super.setSpatial(spatial);
        if (spatial == null)
            return;
        
        if (spatial instanceof Node) {
            Node n = (Node)spatial;
            for (Spatial s : n.getChildren()) {
                if (s instanceof Geometry) {
                    s.setUserData(key, this);
                }
            }
        } else if (spatial instanceof Geometry) {
            spatial.setUserData(key, this);
        }
    }
    
    public Body getBody() {
        return body;
    }
    
    @Override
    public void controlUpdate(float tpf) {
        Transform transform = body.getTransform();
        spatial.setLocalTranslation((float)transform.getTranslationX(),
                0,
                (float)transform.getTranslationY());
        
        Quaternion quat = spatial.getLocalRotation();
        quat.fromAngles(0, (float)transform.getRotation(), 0);
        spatial.setLocalRotation(quat);
    }
    
    public abstract void destroy(boolean effects);
    
    public boolean weaponHit(Vector3f hitDirection) {
        return false;
    }
    
    @Override
    public void controlRender(RenderManager rm, ViewPort vp) {
        
    }
}
