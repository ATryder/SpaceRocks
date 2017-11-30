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
package com.atr.spacerocks.gameobject.powerup;

import com.atr.spacerocks.state.GameState;
import com.jme3.math.ColorRGBA;
import com.jme3.scene.Node;
import com.jme3.scene.control.AbstractControl;
import org.dyn4j.dynamics.Body;

/**
 *
 * @author Adam T. Ryder
 * <a href="http://1337atr.weebly.com">http://1337atr.weebly.com</a>
 */
public abstract class PUP {
    public enum PUPType {
        HEALTH,
        ENERGY,
        SONIC,
        BLACKHOLE
    }
    
    public final GameState gameState;
    public final PUPType type;
    
    public PUP(GameState gameState, PUPType type) {
        this.gameState = gameState;
        this.type = type;
        gameState.getTracker().pupDropped(type);
    }
    
    public static PUP createPUP(PUPType pupType, GameState gameState) {
        switch (pupType) {
            case ENERGY:
                return new EnergyPUP(gameState);
            case HEALTH:
                return new HealthPUP(gameState);
            case SONIC:
                return new SonicPUP(gameState);
            case BLACKHOLE:
                return new BlackHole(gameState);
            default:
                return new EnergyPUP(gameState);
        }
    }
    
    public abstract Node createSpatial();
    public abstract Body createBody();
    
    public abstract AbstractControl createEffects();
    
    public abstract ColorRGBA getCol1();
    public abstract ColorRGBA getCol2();
    public abstract ColorRGBA getCol3();
    
    public void activate() {
        gameState.getTracker().pupPicked(type);
    }
    
    public abstract String soundPath();
}
