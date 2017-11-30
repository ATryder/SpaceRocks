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
package com.atr.spacerocks;

import com.jme3.app.DefaultAndroidProfiler;
import com.atr.spacerocks.util.JmeToHarness;
import com.atr.spacerocks.util.Options;
import com.atr.spacerocks.util.TopGuns;
import com.atr.spacerocks.util.TopGun;
import android.app.Activity;
import android.app.FragmentManager;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;
import android.content.SharedPreferences;
import android.content.Context;
import android.net.Uri;
import android.content.Intent;
import com.jme3.app.AndroidHarnessFragment;
import java.util.logging.Level;
import java.util.logging.LogManager;

public class MainActivity extends Activity {
    /*
     * Note that you can ignore the errors displayed in this file,
     * the android project will build regardless.
     * Install the 'Android' plugin under Tools->Plugins->Available Plugins
     * to get error checks and code completion for the Android project files.
     */

    public MainActivity(){
        // Set the default logging level (default=Level.INFO, Level.ALL=All Debug Info)
        LogManager.getLogManager().getLogger("").setLevel(Level.INFO);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Set window fullscreen and remove title bar
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_main);

        // find the fragment
        FragmentManager fm = getFragmentManager();
        AndroidHarnessFragment jmeFragment =
                (AndroidHarnessFragment) fm.findFragmentById(R.id.jmeFragment);
        ((JmeFragment)jmeFragment).ctx = this;

        // uncomment the next line to add the default android profiler to the project
        //jmeFragment.getJmeApplication().setAppProfiler(new DefaultAndroidProfiler());
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        //forcing process killing to avoid bad things to happen when restarting the application
        if(isFinishing()) {
            android.os.Process.killProcess(android.os.Process.myPid());
        }
    }

    public static class JmeFragment extends AndroidHarnessFragment implements JmeToHarness {
        protected Context ctx;

        public JmeFragment() {
            // Set main project class (fully qualified path)
            appClass = "com.atr.spacerocks.SpaceRocks";

            // Set the desired EGL configuration
            eglBitsPerPixel = 24;
            eglAlphaBits = 0;
            eglDepthBits = 16;
            eglSamples = 0;
            eglStencilBits = 0;

            // Set the maximum framerate
            // (default = -1 for unlimited)
            frameRate = 60;

            // Set the maximum resolution dimension
            // (the smaller side, height or width, is set automatically
            // to maintain the original device screen aspect ratio)
            // (default = -1 to match device screen resolution)
            maxResolutionDimension = -1;

            // Set input configuration settings
            joystickEventsEnabled = false;
            keyEventsEnabled = true;
            mouseEventsEnabled = true;

            // Set application exit settings
            finishOnAppStop = true;
            handleExitHook = true;
            exitDialogTitle = "Do you want to exit?";
            exitDialogMessage = "Use your home key to bring this app into the background or exit to terminate it.";

            // Set splash screen resource id, if used
            // (default = 0, no splash screen)
            // For example, if the image file name is "splash"...
            //     splashPicID = R.drawable.splash;
            splashPicID = 0;
        }

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            if (getJmeApplication() != null)
                ((SpaceRocks)getJmeApplication()).setJmeToHarness(this);
        }

        @Override
        public String getString(String name) {
            return ctx.getResources().getString(getResources()
                    .getIdentifier(name, "string", ctx.getPackageName()));
        }

        @Override
        public void loadPrefs() {
            SharedPreferences prefs = ctx.getSharedPreferences(ctx.getPackageName(),
                    MODE_PRIVATE);
            Options.setMusicVolume(prefs.getFloat("musicvol", 0.43f));
            Options.setSFXVolume(prefs.getFloat("sfxvol", 0.65f));
            Options.setParticleDetail(prefs.getInt("particle_detail", 2));
            Options.setUIDetail(prefs.getInt("ui_detail", 1));
            for (int i = 0; i < TopGuns.topGuns.length; i++) {
                String initials = prefs.getString("topgun_initials" + Integer.toString(i), "-");
                long rocks = prefs.getLong("topgun_rocks" + Integer.toString(i), 0);
                TopGuns.topGuns[i] = new TopGun(initials, rocks);
            }
        }

        @Override
        public void savePrefs() {
            SharedPreferences.Editor prefs = ctx.getSharedPreferences(ctx.getPackageName(),
                    MODE_PRIVATE).edit();
            prefs.putFloat("musicvol", Options.getMusicVolume());
            prefs.putFloat("sfxvol", Options.getSFXVolume());
            prefs.putInt("particle_detail", Options.getParticleDetailInt());
            prefs.putInt("ui_detail", Options.getUIDetailInt());
            for (int i = 0; i < TopGuns.topGuns.length; i++) {
                TopGun tg = TopGuns.topGuns[i];
                prefs.putString("topgun_initials" + Integer.toString(i), tg.initials);
                prefs.putLong("topgun_rocks" + Integer.toString(i), tg.rocks);
            }

            prefs.commit();
        }

        @Override
        public void launchDonate() {
            /*Intent launchBrowser = new Intent(Intent.ACTION_VIEW,
                Uri.parse("https://www.paypal.me/1337tryder"));*/
            Intent launchBrowser = new Intent(Intent.ACTION_VIEW,
                    Uri.parse("http://1337atr.weebly.com"));
            startActivity(launchBrowser);
        }
    }
}
