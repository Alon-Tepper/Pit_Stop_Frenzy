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
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class MainActivity extends AppCompatActivity {

    Button frontLeft, frontRight, rearLeft, rearRight, resetButton;
    SharedPreferences prefs;
    private TextView timerTextView;

    // Handler and Runnable for updating the timer text every second
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
        EdgeToEdge.enable(this); // Enables edge-to-edge layout
        setContentView(R.layout.activity_main);

        // Apply system window insets (e.g. for notch or status bar)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            v.setPadding(insets.getInsets(WindowInsetsCompat.Type.systemBars()).left,
                    insets.getInsets(WindowInsetsCompat.Type.systemBars()).top,
                    insets.getInsets(WindowInsetsCompat.Type.systemBars()).right,
                    insets.getInsets(WindowInsetsCompat.Type.systemBars()).bottom);
            return insets;
        });

        prefs = getSharedPreferences("game", MODE_PRIVATE); // Initialize SharedPreferences

        // Initialize buttons and timer text
        frontLeft = findViewById(R.id.frontwheel1);
        frontRight = findViewById(R.id.frontwheel2);
        rearLeft = findViewById(R.id.rearwheel1);
        rearRight = findViewById(R.id.rearwheel2);
        resetButton = findViewById(R.id.resetButton);
        timerTextView = findViewById(R.id.timerTextView);

        // Reset button clears game progress but preserves best time
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

        // Button to view user info
        Button userInfoButton = findViewById(R.id.buttonUserInfo);
        userInfoButton.setOnClickListener(v -> {
            GameTimer.getInstance().stopTimer();
            timerHandler.removeCallbacks(timerRunnable);

            Intent intent = new Intent(MainActivity.this, User_InfoActivity.class);
            startActivity(intent);
        });

        updateButtons();

        // Start game if not started already
        boolean isGameStarted = prefs.getBoolean("isGameStarted", false);
        if (!isGameStarted || GameTimer.getInstance().getSeconds() == 0) {
            prefs.edit().putBoolean("isGameStarted", false).apply();
            showStartDialog();
        } else {
            timerHandler.post(timerRunnable);
        }
    }

    // Set up button behavior depending on whether that part is already done
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

    // Show alert if user clicks a completed wheel
    private void showCompletedDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Game Completed")
                .setMessage("Finished the game already, try a different one.")
                .setPositiveButton("OK", null)
                .show();
    }

    // Show a dialog at the start of the game
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

        // Resume timer if the game is ongoing
        if (isGameStarted && !gameFinished) {
            GameTimer.getInstance().startTimer();
            timerHandler.post(timerRunnable);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        timerHandler.removeCallbacks(timerRunnable); // Stop timer updates when activity is paused
    }

    // Update button states depending on completion
    private void updateButtons() {
        setupButton(frontLeft, "frontLeftDone");
        setupButton(frontRight, "frontRightDone");
        setupButton(rearLeft, "rearLeftDone");
        setupButton(rearRight, "rearRightDone");
    }

    // Check if all wheels are completed
    private void checkIfGameFinished() {
        boolean frontLeftDone = prefs.getBoolean("frontLeftDone", false);
        boolean frontRightDone = prefs.getBoolean("frontRightDone", false);
        boolean rearLeftDone = prefs.getBoolean("rearLeftDone", false);
        boolean rearRightDone = prefs.getBoolean("rearRightDone", false);
        boolean isGameStarted = prefs.getBoolean("isGameStarted", false);

        if (isGameStarted && frontLeftDone && frontRightDone && rearLeftDone && rearRightDone) {
            GameTimer.getInstance().stopTimer();
            timerHandler.removeCallbacks(timerRunnable);

            int finalTime = GameTimer.getInstance().getSeconds();
            prefs.edit().putInt("finalTime", finalTime).apply();

            FirebaseAuth auth = FirebaseAuth.getInstance();
            if (auth.getCurrentUser() != null && finalTime > 0) {
                String userId = auth.getCurrentUser().getUid();
                DatabaseReference userTimeRef = FirebaseDatabase.getInstance()
                        .getReference("Users").child(userId).child("time");

                // Update best time in Firebase if it's better than previous one
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

            // Show game finished dialog with final time
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