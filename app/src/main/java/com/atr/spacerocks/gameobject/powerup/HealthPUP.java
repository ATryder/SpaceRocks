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

import com.atr.spacerocks.effects.particles.PUPHealthEffects;
import com.atr.spacerocks.state.GameState;
import com.atr.spacerocks.util.Asteroid;
import com.jme3.math.ColorRGBA;
import com.jme3.scene.Node;
import com.jme3.scene.control.AbstractControl;
import org.dyn4j.dynamics.Body;
import org.dyn4j.dynamics.BodyFixture;
import org.dyn4j.geometry.MassType;
import org.dyn4j.geometry.Triangle;
import org.dyn4j.geometry.Vector2;

/**
 *
 * @author Adam T. Ryder
 * <a href="http://1337atr.weebly.com">http://1337atr.weebly.com</a>
 */
public class HealthPUP extends PUP {
    private static final ColorRGBA col1 = new ColorRGBA(1f, 0.55f, 0.5f, 1f);
    private static final ColorRGBA col2 = new ColorRGBA(1f, 0.05f, 0.05f, 0.9f);
    private static final ColorRGBA col3 = new ColorRGBA(1f, 0.05f, 0.05f, 0f);
    
    public HealthPUP(GameState gameState) {
        super(gameState, PUPType.HEALTH);
    }
    
    @Override
    public Node createSpatial() {
        return gameState.getApp().PUP_Health.clone(false);
    }
    
    @Override
    public Body createBody() {
        Triangle t = org.dyn4j.geometry.Geometry.createTriangle(new Vector2(-2.04281f * gameState.getApp().PUP_Health.getLocalScale().x, 0f),
                new Vector2(1.0869f * gameState.getApp().PUP_Health.getLocalScale().x, -1.61222f * gameState.getApp().PUP_Health.getLocalScale().x),
                new Vector2(1.0869f * gameState.getApp().PUP_Health.getLocalScale().x, 1.61222f * gameState.getApp().PUP_Health.getLocalScale().x));
        BodyFixture bf = new BodyFixture(t);
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
        return new PUPHealthEffects(gameState);
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
        gameState.getPlayer().addHealth(10);
    }
    
    @Override
    public String soundPath() {
        return "Sound/HealthPickup.wav";
    }
}
