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

import com.atr.spacerocks.control.AstCont;
import com.atr.spacerocks.state.GameState;
import com.jme3.math.Vector3f;
import com.jme3.scene.Mesh;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.VertexBuffer;
import java.nio.FloatBuffer;
import java.util.List;
import org.dyn4j.dynamics.Body;
import org.dyn4j.dynamics.BodyFixture;
import org.dyn4j.geometry.Convex;
import org.dyn4j.geometry.Geometry;
import org.dyn4j.geometry.MassType;
import org.dyn4j.geometry.Polygon;
import org.dyn4j.geometry.Vector2;
import org.dyn4j.geometry.decompose.Bayazit;

/**
 *
 * @author Adam T. Ryder
 * <a href="http://1337atr.weebly.com">http://1337atr.weebly.com</a>
 */
public class Asteroid {
    public static final double DENSITY = 1;
    public static final float minScale = 4;
    public static final float maxScale = 8;
    public static final float minSplit = 6;
    public static final float BASE_DAMAGE = 1 / 55f;
    
    public final Node asteroid;
    public final Polygon[] shapes;
    
    public Asteroid(Node asteroid, Mesh shape) {
        this.asteroid = asteroid;
        shapes = createVerts(shape);
    }
    
    private Polygon[] createVerts(Mesh mesh) {
        FloatBuffer fb = mesh.getFloatBuffer(VertexBuffer.Type.Position);
        Vector2[] verts = new Vector2[fb.capacity() / 3];
        fb.rewind();
        
        int i = 0;
        while(fb.hasRemaining()) {
            float x = fb.get();
            fb.get();
            float y = fb.get();
            
            verts[i++] = new Vector2(x, y);
        }
        
        verts = Geometry.cleanse(verts);
        
        List<Convex> polyList = new Bayazit().decompose(verts);
        Polygon[] polys = new Polygon[polyList.size()];
        
        i = 0;
        for (Convex c : polyList)
            polys[i++] = (Polygon)c;
        
        return polys;
    }
    
    public AstCont getAsteroid(float scale, Vector3f location,
            GameState gameState) {
        Body body = new Body();
        for (Polygon p : shapes) {
            BodyFixture fixture = new BodyFixture(Geometry.scale(p, scale));
            fixture.setDensity(DENSITY);
            fixture.setFriction(0);
            body.addFixture(fixture);
        }
        body.setMass(MassType.NORMAL);
        body.setAngularDamping(0);
        body.setLinearDamping(0);
        body.setAutoSleepingEnabled(false);
        body.getTransform().setTranslationX(location.x);
        body.getTransform().setTranslationY(location.z);
        
        float maxZ = GameState.FIELDHEIGHTHALF + GameState.ASTEROID_PADDING + GameState.ASTEROID_PADDING;
        float maxX = ((GameState.FIELDHEIGHT *
                ((float)gameState.getApp().getWidth() / gameState.getApp().getHeight()))
                / 2f) + GameState.ASTEROID_PADDING + GameState.ASTEROID_PADDING;
        AstCont cont = new AstCont(body, maxX, maxZ, gameState);
        Spatial spatial = asteroid.clone(false);
        spatial.setLocalScale(scale);
        spatial.setLocalTranslation(location.x, 0, location.z);
        spatial.addControl(cont);
        
        return cont;
    }
    
    public Node getSpatial() {
        return asteroid.clone(false);
    }
}
