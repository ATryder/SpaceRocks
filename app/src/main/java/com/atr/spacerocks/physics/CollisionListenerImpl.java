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

import com.atr.spacerocks.control.PlayerCont;
import org.dyn4j.dynamics.contact.ContactListener;
import org.dyn4j.dynamics.contact.ContactPoint;
import org.dyn4j.dynamics.contact.PersistedContactPoint;
import org.dyn4j.dynamics.contact.SolvedContactPoint;

/**
 *
 * @author Adam T. Ryder
 * <a href="http://1337atr.weebly.com">http://1337atr.weebly.com</a>
 */
public class CollisionListenerImpl implements ContactListener {
    private final CollisionHandler handler;
    
    public CollisionListenerImpl(CollisionHandler handler) {
        this.handler = handler;
    }
    
    /*@Override
    public boolean collision(Body body1, BodyFixture fixture1,
            Body body2, BodyFixture fixture2) {
        return true;
    }
    
    @Override
    public boolean collision(Body body1, BodyFixture fixture1,
            Body body2, BodyFixture fixture2, Manifold manifold) {
        return true;
    }
    
    @Override
    public boolean collision(Body body1, BodyFixture fixture1,
            Body body2, BodyFixture fixture2, Penetration penetration) {
        if (body1.getUserData() instanceof PlayerCont) {
            handler.addPlayerCollision(body1, body2);
        } else if (body2.getUserData() instanceof PlayerCont) {
            handler.addPlayerCollision(body2, body1);
        }
        
        return true;
    }
    
    @Override
    public boolean collision(ContactConstraint contactConstraint) {
        return true;
    }*/
    
    @Override
    public boolean begin(ContactPoint point) {
        if (point.getBody1().getUserData() instanceof PlayerCont) {
            handler.addPlayerCollision(point.getBody1(), point.getBody2());
        } else if (point.getBody2().getUserData() instanceof PlayerCont) {
            handler.addPlayerCollision(point.getBody2(), point.getBody1());
        }
        
        return true;
    }
    
    @Override
    public void end(ContactPoint point) {
    }
    
    @Override
    public boolean persist(PersistedContactPoint point) {
        return true;
    }
    
    @Override
    public void postSolve(SolvedContactPoint point) {
    }
    
    @Override
    public boolean preSolve(ContactPoint point) {
        return true;
    }
    
    @Override
    public void sensed(ContactPoint point) {
    }
}
