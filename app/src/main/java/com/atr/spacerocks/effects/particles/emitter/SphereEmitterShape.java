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
package com.atr.spacerocks.effects.particles.emitter;

import com.atr.math.GMath;
import com.jme3.math.FastMath;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;

/**
 *
 * @author Adam T. Ryder
 * <a href="http://1337atr.weebly.com">http://1337atr.weebly.com</a>
 */
public class SphereEmitterShape extends EmitterShape {
    private float radius = 1f;
    private final Quaternion quatY = new Quaternion();
    private final Quaternion quatX = new Quaternion();
    
    public SphereEmitterShape(Vector3f location, float radius) {
        super(location);
        this.radius = radius;
    }
    
    public void setRadius(float radius) {
        this.radius = radius;
    }
    
    public float getRadius() {
        return radius;
    }
    
    @Override
    public Vector3f getPoint(Vector3f store) {
        store.set(0, 0, GMath.randomFloat(-radius, radius));
        quatY.fromAngles(0, GMath.randomFloat(-FastMath.PI, FastMath.PI), 0);
        quatX.fromAngles(GMath.randomFloat(-FastMath.PI, FastMath.PI), 0, 0);
        quatX.mult(store, store);
        quatY.mult(store, store);
        store.addLocal(location);
        
        return store;
    }
}
