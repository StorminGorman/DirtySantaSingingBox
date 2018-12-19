package com.gorbotics.dirtysanta.dirtysanta;

import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.hardware.TriggerEvent;
import android.hardware.TriggerEventListener;
import android.media.MediaPlayer;
import android.os.Build;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.AttributeSet;
import android.util.FloatMath;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity implements SensorEventListener {

    // Start with some variables
    private SensorManager sensorMan;
    private Sensor accelerometer;

    private float[] mGravity;
    private float mAccel;
    private float mAccelCurrent;
    private float mAccelLast;

    private MediaPlayer player;

    private long lastMotionTime;

    //Content resolver used as a handle to the system's settings
    private ContentResolver cResolver;
    //Window object, that will store a reference to the current window
    private Window window;

    private ConstraintLayout layout;

    private Timer timer = new Timer();

    private int[] songs = {
            R.raw.deckthehalls,
            R.raw.godrestyemerry,
            R.raw.jinglebells,
            R.raw.joy,
            R.raw.mountain
    };
    int currentSong = 0;
    boolean isGreen = false;
    boolean delay = false;
    long startTime;

    private void playSomething() {
        if(delay) {
            if(System.currentTimeMillis() - 300000 < startTime) {
                return;
            }
        }

        if(System.currentTimeMillis() - lastMotionTime > 5000) {
            lastMotionTime = System.currentTimeMillis();

            if(player.isPlaying()) {
                Log.d("music", "stopping");
                timer.cancel();
                player.stop();
                layout.setBackgroundColor(Color.DKGRAY);
            } else {
                player = MediaPlayer.create(this, songs[currentSong]);
                currentSong += 1;
                if(currentSong == songs.length) {
                    currentSong = 0;
                }
                Log.d("music", "starting");
                player.start();

                player.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                    @Override
                    public void onCompletion(MediaPlayer mediaPlayer) {
                        MainActivity.this.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                timer.cancel();
                                layout.setBackgroundColor(Color.DKGRAY);
                            }
                        });
                    }
                });

                timer.cancel();
                timer = new Timer();
                timer.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        MainActivity.this.runOnUiThread(new Runnable() {
                            public void run() {
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                                    if(isGreen) {
                                        layout.setBackgroundColor(Color.RED);
                                        isGreen = false;
                                    } else {
                                        layout.setBackgroundColor(Color.GREEN);
                                        isGreen = true;
                                    }
                                }
                            }
                        });

                    }
                }, 0, 2000);
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        sensorMan.registerListener(this, accelerometer,
                SensorManager.SENSOR_DELAY_UI);
    }

    @Override
    protected void onPause() {
        super.onPause();
        sensorMan.unregisterListener(this);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER){
            mGravity = event.values.clone();
            // Shake detection
            float x = mGravity[0];
            float y = mGravity[1];
            float z = mGravity[2];
            mAccelLast = mAccelCurrent;
            mAccelCurrent = (float)Math.sqrt(x*x + y*y + z*z);
            float delta = mAccelCurrent - mAccelLast;
            mAccel = mAccel * 0.9f + delta;
            // Make this higher or lower according to how much
            // motion you want to detect
            if(mAccel > 6){
                Log.d("motion!", "detecting motion!");
                playSomething();
            }
        }

    }


    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // required method
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sensorMan = (SensorManager)getSystemService(SENSOR_SERVICE);
        accelerometer = sensorMan.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mAccel = 0.00f;
        mAccelCurrent = SensorManager.GRAVITY_EARTH;
        mAccelLast = SensorManager.GRAVITY_EARTH;

        player  = MediaPlayer.create(this, R.raw.deck_the_halls);
        layout = findViewById(R.id.screen);

//        float brightness = 100 / (float)255;
//        WindowManager.LayoutParams lp = getWindow().getAttributes();
//        lp.screenBrightness = brightness;
//        getWindow().setAttributes(lp);
        startTime = System.currentTimeMillis();

        AlertDialog alertDialog = new AlertDialog.Builder(this)
                //set icon
                .setIcon(android.R.drawable.ic_dialog_alert)
                //set title
                .setTitle("Delay ?")
                //set message
                .setMessage("Should I delay 5 minutes to keep it secret?")
                //set positive button
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        delay = true;
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        delay = false;
                    }
                })
                .show();
    }
}
