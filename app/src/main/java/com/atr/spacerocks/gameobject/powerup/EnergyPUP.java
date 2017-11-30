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

import com.atr.spacerocks.effects.particles.PUPEnergyEffects;
import com.atr.spacerocks.state.GameState;
import com.atr.spacerocks.util.Asteroid;
import com.jme3.math.ColorRGBA;
import com.jme3.scene.Node;
import com.jme3.scene.control.AbstractControl;
import org.dyn4j.dynamics.Body;
import org.dyn4j.dynamics.BodyFixture;
import org.dyn4j.geometry.MassType;

/**
 *
 * @author Adam T. Ryder
 * <a href="http://1337atr.weebly.com">http://1337atr.weebly.com</a>
 */
public class EnergyPUP extends PUP {
    private static final ColorRGBA col1 = new ColorRGBA(0.6f, 0.2f, 1f, 1f);
    private static final ColorRGBA col2 = new ColorRGBA(0.4f, 0f, 1f, 0.9f);
    private static final ColorRGBA col3 = new ColorRGBA(0.4f, 0f, 1f, 0f);
    
    public EnergyPUP(GameState gameState) {
        super(gameState, PUPType.ENERGY);
    }
    
    @Override
    public Node createSpatial() {
        return gameState.getApp().PUP_Energy.clone(false);
    }
    
    @Override
    public Body createBody() {
        BodyFixture bf = new BodyFixture(
                org.dyn4j.geometry.Geometry.createRectangle(0.85456 * gameState.getApp().PUP_Energy.getLocalScale().x,
                        2 * gameState.getApp().PUP_Energy.getLocalScale().x));
        bf.setDensity(Asteroid.DENSITY);
        bf.setFriction(0);
        Body body = new Body();
        body.addFixture(bf);
        body.setAngularDamping(0);
        body.setLinearDamping(0);
        body.setMass(MassType.NORMAL);
        
        return body;
    }
    
    @Override
    public AbstractControl createEffects() {
        return new PUPEnergyEffects(gameState);
    }
    
    @Override
    public ColorRGBA getCol1() {
        return col1;
    }
    
    @Override
    public ColorRGBA getCol2() {
        return col2;
    }
    
    @Override
    public ColorRGBA getCol3() {
        return col3;
    }
    
    @Override
    public void activate() {
        super.activate();
        gameState.getHUD().setEnergyPercent(gameState.getHUD().getEnergy() + 0.1f);
    }
    
    @Override
    public String soundPath() {
        return "Sound/EnergyPickup.wav";
    }
}
