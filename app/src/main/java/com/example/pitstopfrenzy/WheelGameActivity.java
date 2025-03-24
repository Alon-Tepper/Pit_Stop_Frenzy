package com.example.pitstopfrenzy;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.ImageView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class WheelGameActivity extends AppCompatActivity {

    private ImageView tireImage, progressBar; // Views for tire and progress bar
    private int clickCounter = 0; // Counter to track user taps

    // Array of tire images to display as stages progress
    private final int[] tireImages = {
            R.drawable.w_tire,
            R.drawable.h_tire,
            R.drawable.i_tire,
            R.drawable.m_tire
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this); // Enables fullscreen layout
        setContentView(R.layout.activity_wheel_game);

        // Handle safe area/padding for devices with notches or system bars
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            v.setPadding(insets.getInsets(WindowInsetsCompat.Type.systemBars()).left,
                    insets.getInsets(WindowInsetsCompat.Type.systemBars()).top,
                    insets.getInsets(WindowInsetsCompat.Type.systemBars()).right,
                    insets.getInsets(WindowInsetsCompat.Type.systemBars()).bottom);
            return insets;
        });

        // Initialize views
        tireImage = findViewById(R.id.imageview_b_tire);
        progressBar = findViewById(R.id.imageView_progBar);

        // Set initial images
        tireImage.setImageResource(tireImages[0]);
        progressBar.setImageResource(R.drawable.progress_bar_0);

        // Set tap listener on tire
        tireImage.setOnClickListener(v -> handleTireClick());
    }

    // Called when user taps the tire
    private void handleTireClick() {
        if (clickCounter < 10) {
            clickCounter++;

            // Each 2 taps changes tire stage
            int tireIndex = clickCounter / 2;
            if (tireIndex >= tireImages.length) {
                tireIndex = tireImages.length - 1;
            }

            tireImage.setImageResource(tireImages[tireIndex]); // Update tire image
            updateProgressBar(clickCounter * 10); // Update progress bar

            // If completed 10 taps, show finish dialog
            if (clickCounter == 10) {
                showFinishDialog();
            }
        }
    }

    // Update progress bar image based on percentage
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

    // Show dialog when the tire minigame is completed
    private void showFinishDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Finished!")
                .setMessage("Great job! You finished the game.")
                .setCancelable(false)
                .setPositiveButton("Back to Main", (dialog, which) -> {
                    SharedPreferences prefs = getSharedPreferences("game", MODE_PRIVATE);
                    String key = getIntent().getStringExtra("buttonKey"); // Get which tire was clicked
                    if (key != null) {
                        prefs.edit().putBoolean(key, true).apply(); // Mark that tire as done
                    }

                    // Stop timer if all tire minigames are completed
                    if (allGamesCompleted()) {
                        GameTimer.getInstance().stopTimer();
                    }

                    // Go back to main screen
                    startActivity(new Intent(WheelGameActivity.this, MainActivity.class));
                    finish();
                })
                .show();
    }

    // Check if all 4 tires have been completed
    private boolean allGamesCompleted() {
        SharedPreferences prefs = getSharedPreferences("game", MODE_PRIVATE);
        return prefs.getBoolean("frontLeftDone", false) &&
                prefs.getBoolean("frontRightDone", false) &&
                prefs.getBoolean("rearLeftDone", false) &&
                prefs.getBoolean("rearRightDone", false);
    }
}
