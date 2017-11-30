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

import com.atr.spacerocks.state.GameState;
import com.atr.spacerocks.util.Options;
import com.jme3.asset.AssetManager;
import com.jme3.audio.AudioNode;
import com.jme3.math.Vector3f;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Adam T. Ryder
 * <a href="http://1337atr.weebly.com">http://1337atr.weebly.com</a>
 */
public final class SoundFX {
    public static final List<SoundNode> blackHoleSounds = new ArrayList<SoundNode>();
    public static final List<SoundNode> sounds = new ArrayList<SoundNode>();
    
    private static AudioNode laserSound;
    private static AudioNode sonicBlastSound;
    
    public static void stopAllSounds() {
        for (SoundNode sound : blackHoleSounds)
            sound.fxFinish();
        blackHoleSounds.clear();
        
        for (SoundNode sound : sounds)
            sound.fxFinish();
        sounds.clear();
    }
    
    public static void removeSound(SoundNode sound) {
        sounds.remove(sound);
        blackHoleSounds.remove(sound);
    }
    
    public static SoundNode playSound(GameState gameState, String path, Vector3f position) {
        return playSound(gameState, path, position, Options.getSFXVolume());
    }
    
    public static SoundNode playSound(GameState gameState, String path, Vector3f position,
            float volume) {
        SoundNode sound = new SoundNode(position, path, gameState.getApp().getAssetManager(),
                gameState.noCollideNode, volume);
        sounds.add(sound);
        
        return sound;
    }
    
    public static SoundNode playSound(GameState gameState, String path, Vector3f position,
            boolean stream, float volume) {
        SoundNode sound = new SoundNode(position, path, stream,
                gameState.getApp().getAssetManager(), gameState.noCollideNode, volume);
        sounds.add(sound);
        
        return sound;
    }
    
    public static SoundNode playBlackHoleSound(GameState gameState, Vector3f position) {
        SoundNode sound = new SoundNode(position, "Sound/BlackHole.wav", true,
                gameState.getApp().getAssetManager(), gameState.noCollideNode,
                Options.getSFXVolume() * 1.5f);
        blackHoleSounds.add(sound);
        
        return sound;
    }
    
    public static void playLaser(AssetManager am) {
        if (laserSound == null) {
            laserSound = new AudioNode(am, "Sound/Laser.wav", false, false);
            laserSound.setPositional(false);
            laserSound.setLooping(false);
            laserSound.setReverbEnabled(false);
        }
        
        laserSound.setVolume(Options.getSFXVolume() * 0.12f);
        laserSound.playInstance();
    }
    
    public static void playSonicBlast(AssetManager am) {
        if (sonicBlastSound == null) {
            sonicBlastSound = new AudioNode(am, "Sound/SonicBlast.wav", false, false);
            sonicBlastSound.setPositional(false);
            sonicBlastSound.setLooping(false);
            sonicBlastSound.setReverbEnabled(false);
        }
        
        sonicBlastSound.setVolume(Options.getSFXVolume() * 1.3f);
        sonicBlastSound.playInstance();
    }
}
