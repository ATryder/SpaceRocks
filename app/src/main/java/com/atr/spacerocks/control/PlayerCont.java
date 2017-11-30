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

import com.atr.spacerocks.SpaceRocks;
import com.jme3.math.Vector3f;
import com.jme3.renderer.Camera;
import org.dyn4j.dynamics.Body;

/**
 *
 * @author Adam T. Ryder
 * <a href="http://1337atr.weebly.com">http://1337atr.weebly.com</a>
 */
public class PlayerCont extends BodyCont {
    private final Camera gameCam;
    
    public PlayerCont(SpaceRocks app, Body body) {
        super(body);
        
        gameCam = app.getCamera();
    }
    
    @Override
    public void controlUpdate(float tpf) {
        super.controlUpdate(tpf);
        
        Vector3f camLoc = gameCam.getLocation();
        camLoc.x = spatial.getWorldTranslation().x;
        camLoc.z = spatial.getWorldTranslation().z;
        gameCam.setLocation(camLoc);
    }
    
    @Override
    public void destroy(boolean effects) {
        
    }
}
