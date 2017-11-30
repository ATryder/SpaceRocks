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
package com.atr.spacerocks.effects.starfield;

import com.atr.math.GMath;
import com.atr.spacerocks.util.Options;
import com.atr.spacerocks.util.Options.Detail;
import com.jme3.math.FastMath;
import com.jme3.math.Vector2f;
import com.jme3.math.Vector3f;
import com.jme3.renderer.Camera;
import com.jme3.renderer.queue.RenderQueue;
import com.jme3.scene.Spatial;
import java.util.Arrays;
import java.util.Comparator;

/**
 *
 * @author Adam T. Ryder
 * <a href="http://1337atr.weebly.com">http://1337atr.weebly.com</a>
 */
public class StarField {
    private final int NUMSTARS = Options.getParticleDetail() == Detail.High ? 225
            : Options.getParticleDetail() == Detail.Medium ? 112
            : 67;
    private static final float MINSIZE = 0.4f;
    private static final float MAXSIZE = 0.8f;
    private static final float MINDIST = 0.8f;
    
    private final Edge leftEdge;
    private final Edge rightEdge;
    private final Edge topEdge;
    private final Edge bottomEdge;
    
    private final Star[] stars = new Star[NUMSTARS];
    private StarFieldGeom sfg;
    
    public class Star {
        public final Vector3f loc = new Vector3f();
        public float size;
        
        public final float distX;
        public final float distZ;
        
        public final float left;
        public final float right;
        public final float top;
        public final float bottom;
        
        public boolean updateNeeded = false;
        
        public Star(Vector3f pos, float size, float distX, float distZ,
                float left, float right, float top, float bottom) {
            loc.set(pos);
            this.size = size;
            this.distX = distX;
            this.distZ = distZ;
            
            this.left = left;
            this.right = right;
            this.top = top;
            this.bottom = bottom;
        }
    }
    
    private class Edge {
        public final Vector3f a;
        public final Vector3f b;
        
        private Edge(Vector3f p1, Vector3f p2) {
            a = p1;
            b = p2;
        }
        
        public Vector3f getPointFromStart(float dist, Vector3f store) {
            return store.set(b).subtractLocal(a).multLocal(dist).addLocal(a);
        }
    }
    
    public StarField(Camera cam, float fov) {
        float maxZ = cam.getLocation().y + 50;
        float minZ = cam.getLocation().y - 100;
        
        Camera camera = new Camera(cam.getWidth(), cam.getHeight());
        camera.setLocation(cam.getLocation().clone());
        camera.setRotation(cam.getRotation().clone());
        camera.setFrustumPerspective(fov, (float)cam.getWidth() / cam.getHeight(), minZ, maxZ);
        
        Vector3f v1 = camera.getWorldCoordinates(new Vector2f(0, camera.getHeight() / 2f), 0);
        v1.x -= camera.getLocation().x;
        v1.z -= camera.getLocation().z;
        Vector3f v2 = camera.getWorldCoordinates(new Vector2f(0, camera.getHeight() / 2f), 1);
        v2.x -= camera.getLocation().x;
        v2.z -= camera.getLocation().z;
        leftEdge = new Edge(v1, v2);
        
        v1 = camera.getWorldCoordinates(new Vector2f(camera.getWidth(), camera.getHeight() / 2f), 0);
        v1.x -= camera.getLocation().x;
        v1.z -= camera.getLocation().z;
        v2 = camera.getWorldCoordinates(new Vector2f(camera.getWidth(), camera.getHeight() / 2f), 1);
        v2.x -= camera.getLocation().x;
        v2.z -= camera.getLocation().z;
        rightEdge = new Edge(v1, v2);
        
        v1 = camera.getWorldCoordinates(new Vector2f(camera.getWidth() / 2f, camera.getHeight()), 0);
        v1.x -= camera.getLocation().x;
        v1.z -= camera.getLocation().z;
        v2 = camera.getWorldCoordinates(new Vector2f(camera.getWidth() / 2f, camera.getHeight()), 1);
        v2.x -= camera.getLocation().x;
        v2.z -= camera.getLocation().z;
        topEdge = new Edge(v1, v2);
        
        v1 = camera.getWorldCoordinates(new Vector2f(camera.getWidth() / 2f, 0), 0);
        v1.x -= camera.getLocation().x;
        v1.z -= camera.getLocation().z;
        v2 = camera.getWorldCoordinates(new Vector2f(camera.getWidth() / 2f, 0), 1);
        v2.x -= camera.getLocation().x;
        v2.z -= camera.getLocation().z;
        bottomEdge = new Edge(v1, v2);
    }
    
    public StarFieldGeom createStarField() {
        Vector3f pos = new Vector3f();
        for (int i = 0; i < NUMSTARS; i++) {
            float distX;
            float distZ;
            
            float left;
            float right;
            float top;
            float bottom;
            do {
                float y = FastMath.nextRandomFloat();

                leftEdge.getPointFromStart(y, pos);
                left = pos.x + MAXSIZE;
                rightEdge.getPointFromStart(y, pos);
                right = pos.x - MAXSIZE;
                distX = left - right;

                topEdge.getPointFromStart(y, pos);
                top = pos.z + MAXSIZE;
                bottomEdge.getPointFromStart(y, pos);
                bottom = pos.z - MAXSIZE;
                distZ = top - bottom;

                pos.x = GMath.randomFloat(right, left);
                pos.z = GMath.randomFloat(bottom, top);
            } while (!checkDist(pos));
            
            Star star = new Star(pos, GMath.randomFloat(MINSIZE, MAXSIZE),
                    distX, distZ, left, right, top, bottom);
            stars[i] = star;
        }
        
        Arrays.sort(stars, new StarComparator());
        
        sfg = new StarFieldGeom(stars);
        sfg.setCullHint(Spatial.CullHint.Never);
        sfg.setQueueBucket(RenderQueue.Bucket.Transparent);
        return sfg;
    }
    
    private boolean checkDist(Vector3f pos) {
        for (Star star : stars) {
            if (star == null)
                return true;
            
            if (star.loc.distance(pos) < MINDIST)
                return false;
        }
        
        return true;
    }
    
    public void updateStars(Vector3f camLoc) {
        boolean updated = false;
        for (Star star : stars) {
            float left = star.left + camLoc.x;
            float right = star.right + camLoc.x;
            float top = star.top + camLoc.z;
            float bottom = star.bottom + camLoc.z;
            
            if (star.loc.x > left) {
                star.updateNeeded = true;
                updated = true;
                do {
                    star.loc.x -= star.distX;
                } while (star.loc.x > left);
            }
            if (star.loc.x < right) {
                star.updateNeeded = true;
                updated = true;
                do {
                    star.loc.x += star.distX;
                } while (star.loc.x < right);
            }
            if (star.loc.z > top) {
                star.updateNeeded = true;
                updated = true;
                do {
                    star.loc.z -= star.distZ;
                } while (star.loc.z > top);
            }
            if (star.loc.z < bottom) {
                star.updateNeeded = true;
                updated = true;
                do {
                    star.loc.z += star.distZ;
                } while (star.loc.z < bottom);
            }
        }
        
        if (updated)
            sfg.updateStars(stars);
    }
    
    public void centerStarField(Vector3f currentCenter) {
        for (Star star : stars) {
            star.loc.x -= currentCenter.x;
            star.loc.z -= currentCenter.z;
            star.updateNeeded = true;
        }
        
        sfg.updateStars(stars);
    }
    
    private class StarComparator implements Comparator<Star> {
        @Override
        public int compare(Star a, Star b) {
            if (a.loc.y == b.loc.y)
                return 0;
            
            return a.loc.y > b.loc.y ? 1 : -1;
        }
    }
}
