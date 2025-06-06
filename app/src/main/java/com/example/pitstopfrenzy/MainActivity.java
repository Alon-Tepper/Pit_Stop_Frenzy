package com.example.pitstopfrenzy;

import android.content.Intent;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class MainActivity extends AppCompatActivity {

    Button frontLeft, frontRight, rearLeft, rearRight, resetButton;
    SharedPreferences prefs;
    private TextView timerTextView;

    private final Handler timerHandler = new Handler(Looper.getMainLooper());
    private final Runnable timerRunnable = new Runnable() {
        @Override
        public void run() {
            timerTextView.setText(GameTimer.getInstance().getFormattedTime());
            timerHandler.postDelayed(this, 1000);
        }
    };

    // Sensor-related variables
    private SensorManager sensorManager;
    private Sensor lightSensor;
    private SensorEventListener lightSensorListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            v.setPadding(insets.getInsets(WindowInsetsCompat.Type.systemBars()).left,
                    insets.getInsets(WindowInsetsCompat.Type.systemBars()).top,
                    insets.getInsets(WindowInsetsCompat.Type.systemBars()).right,
                    insets.getInsets(WindowInsetsCompat.Type.systemBars()).bottom);
            return insets;
        });

        prefs = getSharedPreferences("game", MODE_PRIVATE);

        frontLeft = findViewById(R.id.frontwheel1);
        frontRight = findViewById(R.id.frontwheel2);
        rearLeft = findViewById(R.id.rearwheel1);
        rearRight = findViewById(R.id.rearwheel2);
        resetButton = findViewById(R.id.resetButton);
        timerTextView = findViewById(R.id.timerTextView);

        resetButton.setOnClickListener(v -> {
            int bestTime = prefs.getInt("finalTime", -1);
            prefs.edit()
                    .remove("frontLeftDone")
                    .remove("frontRightDone")
                    .remove("rearLeftDone")
                    .remove("rearRightDone")
                    .putBoolean("isGameStarted", false)
                    .putInt("finalTime", bestTime)
                    .apply();

            GameTimer.getInstance().resetTimer();
            updateButtons();
            showStartDialog();
        });

        Button userInfoButton = findViewById(R.id.buttonUserInfo);
        userInfoButton.setOnClickListener(v -> {
            GameTimer.getInstance().stopTimer();
            timerHandler.removeCallbacks(timerRunnable);

            Intent intent = new Intent(MainActivity.this, User_InfoActivity.class);
            startActivity(intent);
        });

        updateButtons();

        boolean isGameStarted = prefs.getBoolean("isGameStarted", false);
        if (!isGameStarted || GameTimer.getInstance().getSeconds() == 0) {
            prefs.edit().putBoolean("isGameStarted", false).apply();
            showStartDialog();
        } else {
            timerHandler.post(timerRunnable);
        }

        // SENSOR SETUP - Light sensor for screen brightness
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        lightSensor = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);

        lightSensorListener = new SensorEventListener() {
            @Override
            public void onSensorChanged(SensorEvent event) {
                float lightLevel = event.values[0];

                Log.d("LIGHT_SENSOR", "Light level: " + lightLevel); //Log Activity, checks if the light changes

                float brightness = lightLevel / 1000f;
                brightness = Math.max(0.05f, Math.min(brightness, 1f));

                WindowManager.LayoutParams layout = getWindow().getAttributes();
                layout.screenBrightness = brightness;
                getWindow().setAttributes(layout);
            }

            @Override
            public void onAccuracyChanged(Sensor sensor, int accuracy) {}
        };

        if (lightSensor != null) {
            sensorManager.registerListener(lightSensorListener, lightSensor, SensorManager.SENSOR_DELAY_NORMAL);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateButtons();
        checkIfGameFinished();

        boolean isGameStarted = prefs.getBoolean("isGameStarted", false);
        boolean gameFinished = prefs.getBoolean("frontLeftDone", false)
                && prefs.getBoolean("frontRightDone", false)
                && prefs.getBoolean("rearLeftDone", false)
                && prefs.getBoolean("rearRightDone", false);

        if (isGameStarted && !gameFinished) {
            GameTimer.getInstance().startTimer();
            timerHandler.post(timerRunnable);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        timerHandler.removeCallbacks(timerRunnable);
        if (lightSensor != null) {
            sensorManager.unregisterListener(lightSensorListener);
        }
    }

    private void updateButtons() {
        setupButton(frontLeft, "frontLeftDone");
        setupButton(frontRight, "frontRightDone");
        setupButton(rearLeft, "rearLeftDone");
        setupButton(rearRight, "rearRightDone");
    }

    private void setupButton(Button button, String key) {
        if (prefs.getBoolean(key, false)) {
            button.setOnClickListener(v -> showCompletedDialog());
        } else {
            button.setOnClickListener(v -> {
                Intent intent = new Intent(MainActivity.this, WheelGameActivity.class);
                intent.putExtra("buttonKey", key);
                startActivity(intent);
            });
        }
    }

    private void showCompletedDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Game Completed")
                .setMessage("Finished the game already, try a different one.")
                .setPositiveButton("OK", null)
                .show();
    }

    private void showStartDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Start Game")
                .setMessage("Click to start the game")
                .setCancelable(false)
                .setPositiveButton("Start", (dialog, which) -> {
                    int bestTime = prefs.getInt("finalTime", -1);
                    prefs.edit().clear()
                            .putInt("finalTime", bestTime)
                            .putBoolean("frontLeftDone", false)
                            .putBoolean("frontRightDone", false)
                            .putBoolean("rearLeftDone", false)
                            .putBoolean("rearRightDone", false)
                            .putBoolean("isGameStarted", true)
                            .apply();

                    GameTimer.getInstance().resetTimer();
                    updateButtons();
                    GameTimer.getInstance().startTimer();
                    timerHandler.post(timerRunnable);
                })
                .show();
    }

    private void checkIfGameFinished() {
        boolean frontLeftDone = prefs.getBoolean("frontLeftDone", false);
        boolean frontRightDone = prefs.getBoolean("frontRightDone", false);
        boolean rearLeftDone = prefs.getBoolean("rearLeftDone", false);
        boolean rearRightDone = prefs.getBoolean("rearRightDone", false);
        boolean isGameStarted = prefs.getBoolean("isGameStarted", false);

        if (isGameStarted && frontLeftDone && frontRightDone && rearLeftDone && rearRightDone) {
            GameTimer.getInstance().stopTimer();
            timerHandler.removeCallbacks(timerRunnable);

            //sound of winning the game
            MediaPlayer mediaPlayer = MediaPlayer.create(this, R.raw.game_win);
            mediaPlayer.start();
            mediaPlayer.setOnCompletionListener(mp -> mp.release());


            int finalTime = GameTimer.getInstance().getSeconds();
            prefs.edit().putInt("finalTime", finalTime).apply();

            FirebaseAuth auth = FirebaseAuth.getInstance();
            if (auth.getCurrentUser() != null && finalTime > 0) {
                String userId = auth.getCurrentUser().getUid();
                DatabaseReference userTimeRef = FirebaseDatabase.getInstance()
                        .getReference("Users").child(userId).child("time");

                userTimeRef.get().addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Integer bestTime = task.getResult().getValue(Integer.class);
                        if (bestTime == null || bestTime <= 0 || finalTime < bestTime) {
                            userTimeRef.setValue(finalTime);
                        }
                    } else {
                        userTimeRef.setValue(finalTime);
                    }
                });
            }

            new AlertDialog.Builder(this)
                    .setTitle("Game Over")
                    .setMessage("You completed the game in " + GameTimer.getInstance().getFormattedTime() + "!")
                    .setCancelable(false)
                    .setPositiveButton("New Game", (dialog, which) -> {
                        prefs.edit()
                                .remove("frontLeftDone")
                                .remove("frontRightDone")
                                .remove("rearLeftDone")
                                .remove("rearRightDone")
                                .putBoolean("isGameStarted", false)
                                .apply();

                        GameTimer.getInstance().resetTimer();
                        updateButtons();
                        showStartDialog();
                    })
                    .show();
        }
    }
}
