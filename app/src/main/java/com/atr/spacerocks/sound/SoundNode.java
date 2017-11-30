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
package com.atr.spacerocks.sound;

import com.atr.math.GMath;
import com.atr.spacerocks.util.Options;
import com.jme3.asset.AssetManager;
import com.jme3.audio.AudioNode;
import com.jme3.audio.AudioSource.Status;
import com.jme3.math.Vector3f;
import com.jme3.scene.Node;

/**
 *
 * @author Adam T. Ryder
 * <a href="http://1337atr.weebly.com">http://1337atr.weebly.com</a>
 */
public class SoundNode {
    private final AudioNode node;
    
    private float tme;
    private boolean fade = false;
    private float fadeLength = 0.5f;
    
    private boolean paused = false;
    
    private float volume = 1;
    
    public SoundNode(Vector3f position, String path, AssetManager am,
            Node parent, float volume) {
        this(position, path, false, am, parent, volume);
    }
    
    public SoundNode(Vector3f position, String path, 
            boolean stream, AssetManager am, Node parent, float volume) {
        node = new AudioNode(am, path, stream, false);
        node.setVolume(volume);
        this.volume = volume;
        node.setPositional(true);
        node.setLocalTranslation(position);
        node.setLooping(false);
        node.setRefDistance(152);
        node.setMaxDistance(1000f);
        node.setReverbEnabled(false);
        
        parent.attachChild(node);
        node.play();
        
        node.addControl(new SoundControl(this));
    }
    
    public void setVolume(float volume) {
        node.setVolume(volume);
        this.volume = volume;
    }
    
    public void fadeOut(float length) {
        fadeLength = length;
        fade = true;
        tme = 0;
    }
    
    public void fadeOut() {
        fadeOut(fadeLength);
    }
    
    public boolean isFading() {
        return fade;
    }
    
    public void pause() {
        if (node.getStatus() != Status.Playing)
            return;
        paused = true;
        node.pause();
    }
    
    public void unPause() {
        paused = false;
        if (node.getStatus() != Status.Paused)
            return;
        
        node.play();
    }
    
    public boolean update(float tpf) {
        if (paused)
            return true;
        
        if (fade) {
            tme += tpf;
            float perc = tme / fadeLength;
            if (perc >= 1) {
                node.stop();
                return false;
            }
            
            node.setVolume(GMath.smoothFloat(perc, volume, 0));
        }
        
        return node.getStatus() != Status.Stopped;
    }
    
    public void finish() {
        fxFinish();
        SoundFX.removeSound(this);
    }
    
    protected void fxFinish() {
        if (node.getStatus() != Status.Playing)
            node.stop();
        node.removeFromParent();
    }
}
