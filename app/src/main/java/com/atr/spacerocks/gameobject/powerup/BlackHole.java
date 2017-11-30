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

import com.atr.spacerocks.control.BlackHoleCont;
import com.atr.spacerocks.shape.CenterQuad;
import com.atr.spacerocks.state.GameState;
import com.jme3.material.Material;
import com.jme3.material.RenderState;
import com.jme3.math.ColorRGBA;
import com.jme3.math.FastMath;
import com.jme3.math.Vector3f;
import com.jme3.renderer.queue.RenderQueue;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.control.AbstractControl;
import java.util.LinkedList;
import org.dyn4j.dynamics.Body;

/**
 *
 * @author Adam T. Ryder
 * <a href="http://1337atr.weebly.com">http://1337atr.weebly.com</a>
 */
public class BlackHole extends PUP {
    public static final ColorRGBA col1 = new ColorRGBA(0.55f, 1f, 0.75f, 1f);
    public static final ColorRGBA col2 = new ColorRGBA(0.05f, 1f, 0.05f, 0.75f);
    public static final ColorRGBA col3 = new ColorRGBA(0.05f, 1f, 0.05f, 0f);
    
    public static final float MASS = FastMath.sqr(162000000);
    public static final float SINGULARITY = 10;
    
    public static final float armRadius = 48;
    public static final float effectsRadius = SINGULARITY * 7;
    
    private BlackHoleCont controller;
    
    private LinkedList<PUP> pups = new LinkedList<PUP>();
    
    public BlackHole(GameState gameState) {
        super(gameState, PUPType.BLACKHOLE);
    }
    
    public void addPUP(PUP pup) {
        if (pup.type != PUPType.BLACKHOLE)
            pups.add(pup);
    }
    
    public LinkedList<PUP> getPUPs() {
        return pups;
    }
    
    public void setController(BlackHoleCont controller) {
        this.controller = controller;
    }
    
    public BlackHoleCont getController() {
        return controller;
    }
    
    public Vector3f getLocation() {
        return controller.getSpatial().getLocalTranslation();
    }
    
    @Override
    public Node createSpatial() {
        Geometry g = new Geometry("EventHorizon", new CenterQuad(SINGULARITY*2 + 8, SINGULARITY*2 + 8));
        Material mat = new Material(gameState.getApp().getAssetManager(),
                "MatDefs/Unshaded/circle_glow.j3md");
        mat.setColor("Color", new ColorRGBA(1f, 1f, 1f, 1f));
            mat.setColor("Color2", new ColorRGBA(1f, 1f, 1f, 1f));
            mat.setColor("Color3", new ColorRGBA(1f, 1f, 1f, 1f));
        mat.setFloat("Pos1", 0f);
        mat.setFloat("Pos2", 0.7f);
        mat.setFloat("X", 0.5f);
        mat.setFloat("Y", 0.5f);
        mat.getAdditionalRenderState().setDepthTest(false);
        mat.getAdditionalRenderState().setBlendMode(RenderState.BlendMode.Modulate);
        g.setMaterial(mat);
        g.setQueueBucket(RenderQueue.Bucket.Translucent);
        g.setCullHint(Spatial.CullHint.Dynamic);
        
        Node n = new Node("BlackHole");
        n.attachChild(g);
        n.setCullHint(Spatial.CullHint.Never);
        
        return n;
    }
    
    @Override
    public Body createBody() {
        return null;
    }
    
    @Override
    public AbstractControl createEffects() {
        return null;
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
        gameState.getTracker().addBlackHole(this);
    }
    
    @Override
    public String soundPath() {
        return "Sound/BlackHole.wav";
    }
}
