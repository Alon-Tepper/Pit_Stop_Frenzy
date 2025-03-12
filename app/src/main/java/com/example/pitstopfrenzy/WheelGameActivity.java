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

    private ImageView tireImage, progressBar;
    private int clickCounter = 0;

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

        tireImage = findViewById(R.id.imageview_b_tire);
        progressBar = findViewById(R.id.imageView_progBar);

        tireImage.setImageResource(tireImages[0]);
        progressBar.setImageResource(R.drawable.progress_bar_0);

        tireImage.setOnClickListener(v -> handleTireClick());
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
                        GameTimer.getInstance().stopTimer(); // עצירת הטיימר כשמסיימים הכול
                    }

                    startActivity(new Intent(WheelGameActivity.this, MainActivity.class));
                    finish();
                })
                .show();
    }

    // בדיקה אם כל 4 המשחקים הושלמו
    private boolean allGamesCompleted() {
        SharedPreferences prefs = getSharedPreferences("game", MODE_PRIVATE);
        return prefs.getBoolean("frontLeftDone", false) &&
                prefs.getBoolean("frontRightDone", false) &&
                prefs.getBoolean("rearLeftDone", false) &&
                prefs.getBoolean("rearRightDone", false);
    }


}


