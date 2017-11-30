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
package com.atr.spacerocks.effects.particles;

import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;

/**
 *
 * @author Adam T. Ryder
 * <a href="http://1337atr.weebly.com">http://1337atr.weebly.com</a>
 */
public abstract class Particle {
    public float size;
    public float halfSize;
    protected final Vector3f loc = new Vector3f();
    protected final ColorRGBA col = new ColorRGBA();
    protected final Vector3f velocity = new Vector3f();
    protected final Vector3f tmp = new Vector3f();
    
    protected float lifeTime;
    protected float tme = 0;
    
    protected boolean alive = true;
    protected boolean finalized = false;
    
    protected float perc = 0f;
    
    public Particle() {
        alive = false;
        finalized = true;
        col.set(0, 0, 0, 0);
        size = 0.01f;
        halfSize = 0.005f;
        lifeTime = 1f;
    }
    
    public Particle(float lifeTime, Vector3f location, float size) {
        loc.set(location);
        this.lifeTime = lifeTime;
        this.size = size;
        this.halfSize = size / 2;
        col.set(1, 1, 1, 1);
    }
    
    public void reInit(float lifeTime, Vector3f location, float size) {
        loc.set(location);
        this.lifeTime = lifeTime;
        this.size = size;
        this.halfSize = size / 2;
        setAlive(true);
    }
    
    public void setColor(ColorRGBA color) {
        col.set(color);
    }
    
    public ColorRGBA getColor() {
        return col;
    }
    
    public void setAlive(boolean alive) {
        this.alive = alive;
        finalized = false;
        if (!alive) {
            col.set(0, 0, 0, 0);
            tme = lifeTime;
        } else {
            col.set(1, 1, 1, 1);
            tme = 0;
        }
    }
    
    public boolean isAlive() {
        return alive;
    }
    
    public Vector3f localToWorld(Vector3f in, Vector3f store) {
        in.add(loc, store);
        return store;
    }
    
    public abstract boolean update(float tpf);
}
