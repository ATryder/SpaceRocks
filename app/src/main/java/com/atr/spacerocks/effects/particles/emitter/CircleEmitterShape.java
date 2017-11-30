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
public class CircleEmitterShape extends EmitterShape {
    public enum Axis {
        XZ,
        XY,
        YZ,
    }
    
    private float radius = 1f;
    private final Quaternion quat = new Quaternion();
    private final Quaternion quat2 = new Quaternion();
    private Axis axis;
    private boolean circumference = false;
    
    public CircleEmitterShape(Vector3f location, float radius) {
        this(location, radius, Axis.XY);
    }
    
    public CircleEmitterShape(Vector3f location, float radius, Axis axis) {
        super(location);
        this.radius = radius;
        this.axis = axis;
    }
    
    public void setOnCircumference(boolean onCircumference) {
        circumference = onCircumference;
    }
    
    public boolean isOnCircumference() {
        return circumference;
    }
    
    public void setRadius(float radius) {
        this.radius = radius;
    }
    
    public float getRadius() {
        return radius;
    }
    
    public void setRotation(Quaternion rotation) {
        quat2.set(rotation);
    }
    
    public Quaternion getRotation() {
        return quat2;
    }
    
    @Override
    public Vector3f getPoint(Vector3f store) {
        switch(axis) {
            case XZ:
                if (!circumference) {
                    store.set(0, 0, GMath.randomFloat(-radius, radius));
                } else
                    store.set(0, 0, radius);
                quat.fromAngles(0, GMath.randomFloat(-FastMath.PI, FastMath.PI), 0);
                break;
            case XY:
                if (!circumference) {
                    store.set(0, GMath.randomFloat(-radius, radius), 0);
                } else
                    store.set(0, radius, 0);
                quat.fromAngles(0, 0, GMath.randomFloat(-FastMath.PI, FastMath.PI));
                break;
            default:
                if (!circumference) {
                    store.set(0, 0, GMath.randomFloat(-radius, radius));
                } else
                    store.set(radius, 0, 0);
                quat.fromAngles(GMath.randomFloat(-FastMath.PI, FastMath.PI), 0, 0);
        }
        quat.mult(store, store);
        quat2.mult(store, store);
        store.addLocal(location);
        
        return store;
    }
}
