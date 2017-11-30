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
import com.atr.spacerocks.util.Callback;
import com.atr.spacerocks.util.Options;
import com.atr.spacerocks.util.Tools;
import com.jme3.asset.AssetManager;
import com.jme3.audio.AudioNode;
import com.jme3.audio.AudioSource.Status;
import com.jme3.math.FastMath;
import java.util.LinkedList;

/**
 *
 * @author Adam T. Ryder
 * <a href="http://1337atr.weebly.com">http://1337atr.weebly.com</a>
 */
public final class Music {
    private static final LinkedList<String> queue = new LinkedList<String>();
    private static final LinkedList<String> finishedQueue = new LinkedList<String>();
    
    private static AudioNode nowPlaying;
    
    private static final String menuTrack = "raw/sci_fi_industries_nativity_in_glass.ogg";
    private static final String[] gameTracks = {"raw/sci_fi_industries_ribeira_grande.ogg",
                                                "raw/sci_fi_industries_te_motive.ogg",
                                                "raw/sci_fi_industries_azimutez.ogg"};
    
    private static AssetManager am;
    
    private static float fadeLength = 1f;
    private static Callback fadeCB;
    private static float fadeTme = 0;
    
    public static void initialize(AssetManager assetManager) {
        am = assetManager;
    }
    
    public static void playMenuTrack() {
        stop();
        queue.clear();
        finishedQueue.clear();
        queue.add(menuTrack);
        play();
    }
    
    public static void playGameTracks() {
        stop();
        queue.clear();
        finishedQueue.clear();
        for (String s : gameTracks)
            queue.add(s);
        
        play();
    }
    
    public static void play() {
        stop();
        String track;
        if (queue.size() > 1) {
            track = queue.remove(FastMath.nextRandomInt(0, queue.size() - 1));
            finishedQueue.add(track);
        } else if (!finishedQueue.isEmpty()) {
            track = queue.remove(0);
            queue.addAll(finishedQueue);
            finishedQueue.clear();
            finishedQueue.add(track);
        } else
            track = queue.get(0);
        
        nowPlaying = new AudioNode(am, track, true, false);
        nowPlaying.setVolume(Options.getMusicVolume());
        nowPlaying.setPositional(false);
        nowPlaying.setReverbEnabled(false);
        nowPlaying.setLooping(false);
        nowPlaying.play();
    }
    
    public static void stop() {
        if (nowPlaying != null)
            nowPlaying.stop();
    }
    
    public static void fadeOut(float length, Callback cb) {
        fadeTme = 0;
        fadeLength = length;
        fadeCB = cb;
    }
    
    public static void setVolume(float volume) {
        if (nowPlaying == null || nowPlaying.getStatus() == Status.Stopped)
            return;
        
        if (fadeCB == null) {
            nowPlaying.setVolume(volume);
            return;
        }
        
        nowPlaying.setVolume(GMath.smoothFloat(Math.min(fadeTme / fadeLength, 1),
                volume, 0));
    }
    
    public static void update(float tpf) {
        if (nowPlaying != null && nowPlaying.getStatus() == Status.Stopped)
            play();
        
        updateFade(tpf);
    }
    
    private static void updateFade(float tpf) {
        if (fadeCB == null)
            return;
        
        if (nowPlaying == null || nowPlaying.getStatus() == Status.Stopped) {
            fadeTme = 0;
            fadeCB.call();
            fadeCB = null;
            return;
        }
        
        fadeTme += tpf;
        nowPlaying.setVolume(GMath.smoothFloat(Math.min(fadeTme / fadeLength, 1),
                Options.getMusicVolume(), 0));
        
        if (nowPlaying.getVolume() > Tools.EPSILON)
            return;
        
        stop();
        fadeCB.call();
        fadeCB = null;
        fadeTme = 0;
    }
}
