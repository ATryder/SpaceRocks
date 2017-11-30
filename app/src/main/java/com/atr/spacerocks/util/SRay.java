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
package com.atr.spacerocks.util;

import com.jme3.math.FastMath;
import com.jme3.math.Vector3f;

/**
 *
 * @author Adam T. Ryder
 * <a href="http://1337atr.weebly.com">http://1337atr.weebly.com</a>
 */
public class SRay {
    public final Vector3f point = new Vector3f();
    public final Vector3f direction = new Vector3f();
    float length = Float.POSITIVE_INFINITY;
    
    float a;
    float b;
    float c;
    float t1;
    float t2;
    float d;
    
    public SRay() {
        
    }
    
    public SRay(Vector3f point, Vector3f direction) {
        set(point, direction);
    }
    
    public void set(Vector3f point, Vector3f direction) {
        this.point.set(point);
        this.direction.set(direction);
    }
    
    public void setLength(float length) {
        this.length = length;
    }
    
    public Vector3f intersects(SRay ray, Vector3f store) {
        float u = (point.z*ray.direction.x + ray.direction.z*ray.point.x
                - ray.point.z*ray.direction.x - ray.direction.z*point.x)
                / (direction.x*ray.direction.z - direction.z*ray.direction.x);
        
        float v = (point.x + direction.x * u - ray.point.x) / ray.direction.x;
        
        if (u < 0 || v < 0)
            return null;
        
        store.x = point.x + direction.x * u;
        store.z = point.z + direction.z * u;
        
        if (point.distance(store) > length)
            return null;
        
        return store;
    }
    
    public boolean intersectsCircle(Vector3f circleCenter, float radius, Vector3f store) {
        point.subtract(circleCenter, store);
        a = direction.dot(direction);
        b = 2 * store.dot(direction);
        c = store.dot(store) - (radius*radius);
        
        d = b*b-4*a*c;
        
        if (d < 0)
            return false;
        
        d = FastMath.sqrt(d);
        t1 = (-b - d)/(2*a);
        t2 = (-b + d)/(2*a);
        
        return (t1 >= 0 && t1 <= 1) || (t2 >= 0 && t2 <= 1);
    }
}
