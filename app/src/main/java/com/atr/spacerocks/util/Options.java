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

/**
 *
 * @author Adam T. Ryder
 * <a href="http://1337atr.weebly.com">http://1337atr.weebly.com</a>
 */
public final class Options {
    public enum Detail {
        Low,
        Medium,
        High
    }
    
    private static float musicVol = 0.43f;
    private static float sfxVol = 0.65f;
    private static Detail particleDetail = Detail.High;
    private static Detail uiDetail = Detail.High;
    
    public static boolean supportDerivatives = false;
    
    public static float getMusicVolume() {
        return musicVol;
    }
    
    public static float getSFXVolume() {
        return sfxVol;
    }
    
    public static Detail getParticleDetail() {
        return particleDetail;
    }
    
    public static void setParticleDetail(Detail detail) {
        particleDetail = detail;
    }
    
    public static void setParticleDetail(int detail) {
        particleDetail = detail <= 0 ? Detail.Low
                : detail >= 2 ? Detail.High
                : Detail.Medium;
    }
    
    public static int getParticleDetailInt() {
        return particleDetail == Detail.Low ? 0
                : particleDetail == Detail.High ? 2
                : 1;
    }
    
    public static Detail getUIDetail() {
        return uiDetail;
    }
    
    public static int getUIDetailInt() {
        return uiDetail == Detail.Low ? 0 : 1;
    }
    
    public static void setUIDetail(Detail detail) {
            uiDetail = detail;
    }
    
    public static void setUIDetail(int detail) {
        uiDetail = detail <= 0 ? Detail.Low : Detail.High;
    }
    
    public static void setMusicVolume(float volume) {
        musicVol = (volume > 1) ? 1 : (volume < 0) ? 0 : volume;
    }
    
    public static void setSFXVolume(float volume) {
        sfxVol = (volume > 1) ? 1 : (volume < 0) ? 0 : volume;
    }
}
