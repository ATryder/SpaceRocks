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
package com.atr.spacerocks.effects;

import com.atr.spacerocks.shape.CenterQuad;
import com.atr.spacerocks.state.GameState;
import com.jme3.math.ColorRGBA;
import com.jme3.scene.Geometry;

/**
 *
 * @author Adam T. Ryder
 * <a href="http://1337atr.weebly.com">http://1337atr.weebly.com</a>
 */
public class Flash extends Geometry {
    public Flash(GameState gameState, ColorRGBA col1, ColorRGBA col2, ColorRGBA col3) {
        super("Flash", new CenterQuad(4, 4));
        setCullHint(CullHint.Dynamic);
        setQueueBucket(queueBucket.Transparent);
        addControl(new FlashCont(gameState, col1, col2, col3));
    }
    
    public Flash(GameState gameState) {
        super("Flash", new CenterQuad(4, 4));
        setCullHint(CullHint.Dynamic);
        setQueueBucket(queueBucket.Transparent);
        addControl(new FlashCont(gameState));
    }
}
