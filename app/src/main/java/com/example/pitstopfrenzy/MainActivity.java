package com.example.pitstopfrenzy;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.Button;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
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
            prefs.edit().clear().apply();
            GameTimer.getInstance().resetTimer();
            updateButtons();
            prefs.edit().putBoolean("isGameStarted", false).apply();
            showStartDialog();
        });

        updateButtons();

        boolean isGameStarted = prefs.getBoolean("isGameStarted", false);
        if (!isGameStarted || GameTimer.getInstance().getSeconds() == 0) {
            prefs.edit().putBoolean("isGameStarted", false).apply();
            showStartDialog();
        } else {
            timerHandler.post(timerRunnable);
        }
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
                    prefs.edit().clear()
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


    @Override
    protected void onResume() {
        super.onResume();
        updateButtons();
        checkIfGameFinished();
    }

    @Override
    protected void onPause() {
        super.onPause();
        timerHandler.removeCallbacks(timerRunnable);
    }

    private void updateButtons() {
        setupButton(frontLeft, "frontLeftDone");
        setupButton(frontRight, "frontRightDone");
        setupButton(rearLeft, "rearLeftDone");
        setupButton(rearRight, "rearRightDone");
    }

    private void checkIfGameFinished() {
        boolean frontLeftDone = prefs.getBoolean("frontLeftDone", false);
        boolean frontRightDone = prefs.getBoolean("frontRightDone", false);
        boolean rearLeftDone = prefs.getBoolean("rearLeftDone", false);
        boolean rearRightDone = prefs.getBoolean("rearRightDone", false);

        if (frontLeftDone && frontRightDone && rearLeftDone && rearRightDone) {
            GameTimer.getInstance().stopTimer();
            timerHandler.removeCallbacks(timerRunnable);

            int finalTime = GameTimer.getInstance().getSeconds();
            prefs.edit().putInt("finalTime", finalTime).apply();

            // עדכון זמן המשחק ב-Firebase למשתמש הנוכחי
            FirebaseAuth auth = FirebaseAuth.getInstance();
            if (auth.getCurrentUser() != null) {
                String userId = auth.getCurrentUser().getUid();
                FirebaseDatabase database = FirebaseDatabase.getInstance();

                // עדכון הזמן של המשתמש ישירות
                database.getReference("Users")
                        .child(userId)
                        .child("time")
                        .setValue(finalTime);
            }

            new AlertDialog.Builder(this)
                    .setTitle("Game Over")
                    .setMessage("You completed the game in " + GameTimer.getInstance().getFormattedTime() + "!")
                    .setCancelable(false)
                    .setPositiveButton("New Game", (dialog, which) -> {
                        prefs.edit().clear().apply();
                        GameTimer.getInstance().resetTimer();
                        prefs.edit().putBoolean("isGameStarted", false).apply();
                        updateButtons();
                        showStartDialog();
                    })
                    .show();
        }
    }
}