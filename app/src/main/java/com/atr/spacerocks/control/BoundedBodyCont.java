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

import com.atr.spacerocks.state.GameState;
import com.jme3.math.Vector3f;
import com.jme3.renderer.Camera;
import org.dyn4j.dynamics.Body;

/**
 *
 * @author Adam T. Ryder
 * <a href="http://1337atr.weebly.com">http://1337atr.weebly.com</a>
 */
public abstract class BoundedBodyCont extends BodyCont {
    protected final GameState state;
    protected final Camera gameCam;
    
    protected final float maxX;
    protected final float maxZ;
    
    private final Vector3f locCheck = new Vector3f();
    
    public BoundedBodyCont (Body body, float maxX, float maxZ,
            GameState gameState) {
        super(body);
        
        this.state = gameState;
        gameCam = state.getApp().getCamera();
        
        this.maxX = maxX;
        this.maxZ = maxZ;
    }
    
    @Override
    public void controlUpdate(float tpf) {
        super.controlUpdate(tpf);
        
        spatial.getLocalTranslation().subtract(gameCam.getLocation(), locCheck);
        if (locCheck.x > maxX
                || locCheck.x < -maxX
                || locCheck.z > maxZ
                || locCheck.z < -maxZ)
            destroy(false);
    }
}
