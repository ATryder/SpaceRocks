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

import com.atr.math.GMath;
import com.atr.spacerocks.gameobject.powerup.PUP;
import com.atr.spacerocks.gameobject.powerup.PUP.PUPType;
import com.atr.spacerocks.sound.SoundFX;
import com.atr.spacerocks.state.GameState;
import com.atr.spacerocks.util.Options;
import com.jme3.math.Vector3f;
import com.jme3.scene.Spatial;
import com.jme3.scene.control.AbstractControl;
import org.dyn4j.geometry.Vector2;

/**
 *
 * @author Adam T. Ryder
 * <a href="http://1337atr.weebly.com">http://1337atr.weebly.com</a>
 */
public class PUPCont extends BoundedBodyCont {
    public final PUP pup;
    
    public PUPCont(PUP pup, float maxX, float maxZ,
            GameState gameState, Vector3f location,
            Vector2 velocity) {
        super(pup.createBody(), maxX, maxZ, gameState);
        
        this.pup = pup;
        
        Spatial s = pup.createSpatial();
        s.setLocalTranslation(location);
        body.getTransform().setTranslationX(location.x);
        body.getTransform().setTranslationY(location.z);
        body.setLinearVelocity(velocity);
        body.setAngularVelocity(GMath.randomFloat(-5, 5));
        s.addControl(this);
        gameState.addSimulatedBody(this);
    }
    
    @Override
    public void destroy(boolean effects) {
        state.removeSimulatedBody(this);
        if (!effects)
            return;
        
        pup.activate();
        AbstractControl pupCont = pup.createEffects();
        pupCont.getSpatial().setLocalTranslation(spatial.getLocalTranslation());
        state.noCollideNode.attachChild(pupCont.getSpatial());
        
        if (pup.type != PUPType.BLACKHOLE)
            SoundFX.playSound(state, pup.soundPath(), spatial.getLocalTranslation(),
                    Options.getSFXVolume() * 0.8f);
    }
}
