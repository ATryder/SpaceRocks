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
package com.atr.spacerocks.ui;

import com.atr.math.GMath;
import com.jme3.math.FastMath;
import com.jme3.math.Quaternion;
import com.jme3.renderer.RenderManager;
import com.jme3.renderer.ViewPort;
import com.jme3.scene.Node;
import com.jme3.scene.control.AbstractControl;
import com.simsilica.lemur.Panel;

/**
 *
 * @author Adam T. Ryder
 * <a href="http://1337atr.weebly.com">http://1337atr.weebly.com</a>
 */
public class UIWobble extends AbstractControl {
    public final Node zNode;
    public final Node yNode;
    
    private final float zLength = 4f;
    private final float yLength = 3f;
    private float zTme = zLength / 2;
    private float yTme = yLength / 2;
    
    private final Quaternion zQuat = new Quaternion();
    private final Quaternion yQuat = new Quaternion();
    
    private int zReverse = 1;
    private int yReverse = 1;
    
    private final float zRotation = 2.5f;
    private final float yRotation = 10f;
    
    public UIWobble(Node node, Panel panel) {
        zNode = node;
        yNode = new Node("UI Wobble y-axis");
        yNode.attachChild(panel);
        zNode.attachChild(yNode);
    }
    
    @Override
    public void controlUpdate(float tpf) {
        zTme += tpf;
        if (zTme > zLength) {
            int floor = (int)Math.floor(zTme / zLength);
            zTme = zTme - (floor * zLength);
            zReverse = zReverse - (zReverse * 2);
        }
        
        yTme += tpf;
        if (yTme > yLength) {
            int floor = (int)Math.floor(yTme / yLength);
            yTme = yTme - (floor * yLength);
            yReverse = yReverse - (yReverse * 2);
        }
        
        float rot = GMath.smoothFloat(zTme / zLength, -zRotation * zReverse, zRotation * zReverse);
        zQuat.fromAngles(0, 0, rot * FastMath.DEG_TO_RAD);
        zNode.setLocalRotation(zQuat);
        
        rot = GMath.smoothFloat(yTme / yLength, -yRotation * yReverse, yRotation * yReverse);
        yQuat.fromAngles(0, rot * FastMath.DEG_TO_RAD, 0);
        yNode.setLocalRotation(yQuat);
    }
    
    @Override
    public void controlRender(RenderManager renderManager, ViewPort viewPort) {
        
    }
}
