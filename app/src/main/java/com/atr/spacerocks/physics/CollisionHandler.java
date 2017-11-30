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
package com.atr.spacerocks.physics;

import com.atr.spacerocks.state.GameState;
import com.jme3.math.Vector2f;
import java.util.LinkedList;
import org.dyn4j.dynamics.Body;

/**
 *
 * @author Adam T. Ryder
 * <a href="http://1337atr.weebly.com">http://1337atr.weebly.com</a>
 */
public class CollisionHandler {
    private final GameState gameState;
    
    private final LinkedList<Collision> collisions = new LinkedList<Collision>();
    
    public CollisionHandler(GameState gameState) {
        this.gameState = gameState;
    }
    
    protected void addPlayerCollision(Body playerBody, Body body2) {
        Collision c = new Collision(body2);
        if (!collisions.contains(c))
            collisions.add(c);
    }
    
    public void handleCollisions() {
        for (Collision c : collisions) {
            gameState.getPlayer().collide(c.body1Velocity, c.body, c.body2Velocity);
        }
        
        collisions.clear();
    }
    
    private class Collision {
        public final Body body;
        
        public final Vector2f body1Velocity;
        public final Vector2f body2Velocity;
        
        private Collision(Body body) {
            this.body = body;
            
            body1Velocity = new Vector2f(gameState.getPlayer().getCurrentVelocity().x,
                    gameState.getPlayer().getCurrentVelocity().z);
            body2Velocity = new Vector2f((float)body.getLinearVelocity().x,
                    (float)body.getLinearVelocity().y);
        }
        
        @Override
        public boolean equals(Object other) {
            /*return body1.equals(((Collision)other).body1)
                    && body2.equals(((Collision)other).body2);*/
            
            return body.equals(((Collision)other).body);
        }
    }
}
