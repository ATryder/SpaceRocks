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
public final class TopGuns {
    public static final TopGun[] topGuns = new TopGun[7];
    
    public static void setTopGun(int position, String initials, long rocks) {
        if (position >= topGuns.length || position < 0)
            return;
        
        for (int i = topGuns.length - 1; i > position; i--) {
            topGuns[i] = topGuns[i - 1];
        }
        topGuns[position] = new TopGun(initials, rocks);
    }
    
    public static int isNewTopGun(long rocks) {
        for (int i = 0; i < topGuns.length; i++) {
            if (topGuns[i].rocks < rocks)
                return i;
        }
        
        return -1;
    }
}
