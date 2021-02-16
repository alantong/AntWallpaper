package com.alan.antwallpaper;

import android.app.Activity;
import android.app.WallpaperManager;
import android.content.ComponentName;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceManager;

public class AntWallpaperActivity extends Activity {

    //SharedPreferences sharedPreferences;
    //SharedPreferences.OnSharedPreferenceChangeListener listener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
/*
        sharedPreferences =
                PreferenceManager.getDefaultSharedPreferences(this);

        listener = new SharedPreferences.OnSharedPreferenceChangeListener() {
                    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
                        if (key.equals("num_of_ants")) {
                            //Log.i("num_of_ants", "Preference value was updated to:" + sharedPreferences.getInt(key, 10));
                            num_of_ants = sharedPreferences.getInt(key, 10);
                        }
                    }
                };
        sharedPreferences.registerOnSharedPreferenceChangeListener(listener);
*/
        Intent i = new Intent();
        i.setAction(WallpaperManager.ACTION_CHANGE_LIVE_WALLPAPER);
        i.putExtra(WallpaperManager.EXTRA_LIVE_WALLPAPER_COMPONENT,
                new ComponentName(
                        AntWallpaperService.class.getPackage().getName(),
                        AntWallpaperService.class.getCanonicalName()
                )
        );

        startActivity(i);
        onBackPressed();

    }

}