package com.example.pitstopfrenzy;

import android.content.Intent;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.Random;

public class WheelGameActivity extends AppCompatActivity {

    private ImageView tireImage, progressBar;
    private View gameContainer; // FrameLayout that holds the tire
    private int clickCounter = 0;

    // Light sensor components
    private SensorManager sensorManager;
    private Sensor lightSensor;
    private SensorEventListener lightSensorListener;

    private final int[] tireImages = {
            R.drawable.w_tire,
            R.drawable.h_tire,
            R.drawable.i_tire,
            R.drawable.m_tire
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_wheel_game);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            v.setPadding(insets.getInsets(WindowInsetsCompat.Type.systemBars()).left,
                    insets.getInsets(WindowInsetsCompat.Type.systemBars()).top,
                    insets.getInsets(WindowInsetsCompat.Type.systemBars()).right,
                    insets.getInsets(WindowInsetsCompat.Type.systemBars()).bottom);
            return insets;
        });

        // Bind views
        tireImage = findViewById(R.id.imageview_b_tire);
        progressBar = findViewById(R.id.imageView_progBar);
        gameContainer = findViewById(R.id.gameContainer);

        // Set initial images
        tireImage.setImageResource(tireImages[0]);
        progressBar.setImageResource(R.drawable.progress_bar_0);

        // Set click listener
        tireImage.setOnClickListener(v -> handleTireClick());

        // First random position when view is ready
        gameContainer.post(this::moveTireRandomly);

        // Light Sensor setup
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        lightSensor = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);

        lightSensorListener = new SensorEventListener() {
            @Override
            public void onSensorChanged(SensorEvent event) {
                float lightLevel = event.values[0];
                Log.d("LIGHT_SENSOR", "Light level: " + lightLevel);

                float brightness = lightLevel / 1000f;
                brightness = Math.max(0.05f, Math.min(brightness, 1f));

                WindowManager.LayoutParams layout = getWindow().getAttributes();
                layout.screenBrightness = brightness;
                getWindow().setAttributes(layout);
            }

            @Override
            public void onAccuracyChanged(Sensor sensor, int accuracy) {
                // Not used
            }
        };

        if (lightSensor != null) {
            sensorManager.registerListener(lightSensorListener, lightSensor, SensorManager.SENSOR_DELAY_NORMAL);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (sensorManager != null && lightSensorListener != null) {
            sensorManager.unregisterListener(lightSensorListener);
        }
    }

    private void handleTireClick() {
        if (clickCounter < 10) {
            clickCounter++;

            int tireIndex = clickCounter / 2;
            if (tireIndex >= tireImages.length) {
                tireIndex = tireImages.length - 1;
            }

            tireImage.setImageResource(tireImages[tireIndex]);
            updateProgressBar(clickCounter * 10);

            moveTireRandomly(); // Move tire after every click

            if (clickCounter == 10) {
                showFinishDialog();
            }
        }
    }

    private void updateProgressBar(int percent) {
        int progressDrawable;
        switch (percent) {
            case 10:
                progressDrawable = R.drawable.progress_bar_10; break;
            case 20: case 30:
                progressDrawable = R.drawable.progress_bar_30; break;
            case 40:
                progressDrawable = R.drawable.progress_bar_40; break;
            case 50: case 60:
                progressDrawable = R.drawable.progress_bar_60; break;
            case 70: case 80:
                progressDrawable = R.drawable.progress_bar_80; break;
            case 90: case 100:
                progressDrawable = R.drawable.progress_bar_100; break;
            default:
                progressDrawable = R.drawable.progress_bar_0; break;
        }
        progressBar.setImageResource(progressDrawable);
    }

    private void moveTireRandomly() {
        int rootWidth = gameContainer.getWidth();
        int rootHeight = gameContainer.getHeight();

        int tireWidth = tireImage.getWidth();
        int tireHeight = tireImage.getHeight();

        int minY = 50;
        int maxX = rootWidth - tireWidth;
        int maxY = rootHeight - tireHeight;

        if (maxX <= 0 || maxY <= minY) return;

        Random rand = new Random();
        int newX = rand.nextInt(maxX);
        int newY = rand.nextInt(maxY - minY) + minY;

        tireImage.setX(newX);
        tireImage.setY(newY);
    }

    private void showFinishDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Finished!")
                .setMessage("Great job! You finished the game.")
                .setCancelable(false)
                .setPositiveButton("Back to Main", (dialog, which) -> {
                    SharedPreferences prefs = getSharedPreferences("game", MODE_PRIVATE);
                    String key = getIntent().getStringExtra("buttonKey");
                    if (key != null) {
                        prefs.edit().putBoolean(key, true).apply();
                    }

                    if (allGamesCompleted()) {
                        GameTimer.getInstance().stopTimer();
                    }

                    startActivity(new Intent(WheelGameActivity.this, MainActivity.class));
                    finish();
                })
                .show();
    }

    private boolean allGamesCompleted() {
        SharedPreferences prefs = getSharedPreferences("game", MODE_PRIVATE);
        return prefs.getBoolean("frontLeftDone", false) &&
                prefs.getBoolean("frontRightDone", false) &&
                prefs.getBoolean("rearLeftDone", false) &&
                prefs.getBoolean("rearRightDone", false);
    }
}
