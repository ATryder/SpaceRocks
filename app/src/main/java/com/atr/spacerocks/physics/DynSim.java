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
package com.atr.spacerocks.physics;

import com.atr.spacerocks.SpaceRocks;
import com.atr.spacerocks.state.GameState;
import java.util.concurrent.atomic.AtomicBoolean;
import org.dyn4j.dynamics.World;

/**
 *
 * @author Adam T. Ryder
 * <a href="http://1337atr.weebly.com">http://1337atr.weebly.com</a>
 */
public class DynSim implements Runnable {
    private final World world;
    private final GameState gameState;
    
    public final AtomicBoolean active = new AtomicBoolean(true);
    private final AtomicBoolean phySync = new AtomicBoolean(true);
    
    private float tpf = 0.25f;
    
    public DynSim(World world, GameState gameState) {
        this.world = world;
        this.gameState = gameState;
    }
    
    public World getWorld() {
        while(phySync.get())
            continue;
        return world;
    }
    
    public boolean updating() {
        return phySync.get();
    }
    
    public void setUpdating(float tpf) {
        this.tpf = tpf;
        phySync.set(true);
    }
    
    public void setTpf(float tpf) {
        this.tpf = tpf;
    }
    
    @Override
    public void run() {
        while(active.get())
            update();
        
        phySync.set(false);
    }
    
    private void update() {
        if (!SpaceRocks.PAUSED.get()) {
            world.update(tpf);
            phySync.set(false);

            while(!phySync.get() && active.get())
                continue;
        }
    }
}
