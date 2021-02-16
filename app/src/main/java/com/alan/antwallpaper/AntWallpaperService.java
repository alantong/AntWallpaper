package com.alan.antwallpaper;

import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.os.Handler;
import android.service.wallpaper.WallpaperService;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;

import androidx.preference.PreferenceManager;

public class AntWallpaperService extends WallpaperService {

    @Override
    public WallpaperService.Engine onCreateEngine() {
        return new AntWallpaperEngine();
    }

    // Here drawing will happen.
    private class AntWallpaperEngine extends Engine {

        SharedPreferences sharedPreferences;
        SharedPreferences.OnSharedPreferenceChangeListener listener;

        private final Handler handler = new Handler();
        private final Runnable drawRunner = new Runnable() {
            @Override
            public void run() {
                draw();
            }
        };
        private int num_of_ants = 8;
        private final float touchAreaSize = 50;
        private int width;
        private int height;

        private int ant_width = 30;
        private int ant_height = 40;
        private int deadAntWidth = 50;
        private int deadAntHeight = 40;
        private int ant_recover_time = 30;
        private float ant_step = 5;
        private Ant ants[];

        private Bitmap antBmp = Bitmap.createScaledBitmap(
                BitmapFactory.decodeResource(getResources(), R.drawable.ant),
                ant_width, ant_height, false);

        private Bitmap deadAntBmp = Bitmap.createScaledBitmap(
                BitmapFactory.decodeResource(getResources(), R.drawable.ant_dead),
                deadAntWidth, deadAntHeight, false);

        private boolean visible = true;

        public AntWallpaperEngine() {
            handler.post(drawRunner);
            height = getResources().getDisplayMetrics().heightPixels;
            width = getResources().getDisplayMetrics().widthPixels;

            sharedPreferences =
                    PreferenceManager.getDefaultSharedPreferences(getApplication().getApplicationContext());

            listener = new SharedPreferences.OnSharedPreferenceChangeListener() {
                public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
                    if (key.equals("num_of_ants")) {
                        //Log.i("num_of_ants", "Preference value was updated to:" + sharedPreferences.getInt(key, 10));
                        num_of_ants = sharedPreferences.getInt(key, 10);
                        init_ants();
                    }
                }
            };
            sharedPreferences.registerOnSharedPreferenceChangeListener(listener);
            init_ants();
        }

        @Override
        public void onVisibilityChanged(boolean visible) {
            this.visible = visible;
            if (visible) {
                handler.post(drawRunner);
            } else {
                handler.removeCallbacks(drawRunner);
            }
        }

        @Override
        public void onSurfaceDestroyed(SurfaceHolder holder) {
            super.onSurfaceDestroyed(holder);
            this.visible = false;
            handler.removeCallbacks(drawRunner);
        }

        @Override
        public void onSurfaceChanged(SurfaceHolder holder, int format, int width, int height) {
            this.width = width;
            this.height = height;
            super.onSurfaceChanged(holder, format, width, height);

        }

        @Override
        public void onTouchEvent(MotionEvent event) {
            float touchX = event.getX();
            float touchY = event.getY();
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    //Log.i("touch", touchX + " " + touchY);
                    hitAnts(touchX, touchY);
                    break;
            }
            super.onTouchEvent(event);
        }

        private void init_ants() {
            //Log.i("init_ants", "init_ants called");
            if (ants != null) ants = null;
            ants = new Ant[num_of_ants];
            for (int i = 0; i < ants.length; i++) {
                ants[i] = new Ant();
                ants[i].position_x = (float) Math.random() * width;
                ants[i].position_y = (float) Math.random() * height;
                ants[i].heading = (float) Math.random() * 360;
                ants[i].isAlive = true;
                ants[i].recoverTime = ant_recover_time;
            }
        }

        private void move_ants() {
            for (int i = 0; i < ants.length; i++) {
                if (!ants[i].isAlive) {
                    //ant is dead
                    ants[i].recoverTime -= 1;
                    if (ants[i].recoverTime <= 0) {
                        ants[i].isAlive = true;
                        ants[i].recoverTime = 30;

                        ants[i].position_x = (float) Math.random() * width;
                        ants[i].position_y = (float) 0;
                        ants[i].heading = (float) Math.random() * 360;
                    }
                    continue;
                }

                // move the ant one step by random
                float step = ant_step;
                float heading_change = (float) Math.random() * 40 - 20;
                float x_change = 0;
                float y_change = 0;

                if (Math.random() * 10 < 3) {
                    ants[i].heading += heading_change;
                }
                if (ants[i].heading >= 360) ants[i].heading -= 360;

                if (ants[i].heading < 0) ants[i].heading += 360;


                float a = ants[i].heading;
                if (a >= 0 && a < 90) {
                    x_change = (float) (step * Math.sin(Math.toRadians(a)));
                    y_change = -(float) (step * Math.cos(Math.toRadians(a)));
                } else if (a >= 90 && a < 180) {
                    x_change = (float) (step * Math.cos(Math.toRadians(a - 90)));
                    y_change = (float) (step * Math.sin(Math.toRadians(a - 90)));
                } else if (a >= 180 && a < 270) {
                    x_change = -(float) (step * Math.sin(Math.toRadians(a - 180)));
                    y_change = (float) (step * Math.cos(Math.toRadians(a - 180)));
                } else if (a >= 270 && a < 360) {
                    x_change = -(float) (step * Math.cos(Math.toRadians(a - 270)));
                    y_change = -(float) (step * Math.sin(Math.toRadians(a - 270)));
                }
                ants[i].position_x += x_change;
                ants[i].position_y += y_change;

                // move the ant to opposite side of when reach the edge
                if (ants[i].position_x > width) ants[i].position_x = 0;
                if (ants[i].position_y > height) ants[i].position_y = 0;
                if (ants[i].position_x < -5) ants[i].position_x = width;
                if (ants[i].position_y < -5) ants[i].position_y = height;
            }
        }

        private void hitAnts(float touchX, float touchY) {
            for (int i = 0; i < ants.length; i++) {
                float antX = ants[i].position_x + (ant_width/2);
                float antY = ants[i].position_y + (ant_height/2);
                if ((antX >= touchX - touchAreaSize && antX <= touchX + touchAreaSize) &&
                        (antY >= touchY - touchAreaSize && antY <= touchY + touchAreaSize)) {
                    ants[i].isAlive = false;
                }
            }
        }

        private void draw() {
            SurfaceHolder holder = getSurfaceHolder();
            Canvas canvas = null;
            try {
                canvas = holder.lockCanvas();
                if (canvas != null) {

                    Paint paint = new Paint();
                    canvas.drawColor(Color.parseColor("#a1a1ab"));


                    for (int i = 0; i < ants.length; i++) {
                        Matrix m = new Matrix();
                        float antCenterX = ants[i].position_x + (ant_width / 2);
                        float antCenterY = ants[i].position_y + (ant_height / 2);
                        m.setRotate(ants[i].heading, ant_width / 2, ant_height / 2);
                        m.postTranslate(ants[i].position_x, ants[i].position_y);

                        if (ants[i].isAlive) {
                            canvas.drawBitmap(antBmp, m, paint);
                        } else {
                            canvas.drawBitmap(deadAntBmp, m, paint);
                        }
                    }
                    move_ants();
                }
            } finally {
                if (canvas != null) holder.unlockCanvasAndPost(canvas);
            }
            handler.removeCallbacks(drawRunner);
            if (visible) handler.postDelayed(drawRunner, 50);
        }


    }

}
