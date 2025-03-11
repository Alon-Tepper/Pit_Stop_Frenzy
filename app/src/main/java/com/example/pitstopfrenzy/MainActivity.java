package com.example.pitstopfrenzy;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class MainActivity extends AppCompatActivity {

    Button frontLeft, frontRight, rearLeft, rearRight, resetButton;
    SharedPreferences prefs;

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

        resetButton.setOnClickListener(v -> {
            prefs.edit().clear().apply();
            updateButtons();
        });

        updateButtons();
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

    @Override
    protected void onResume() {
        super.onResume();
        updateButtons();
    }

    private void updateButtons() {
        setupButton(frontLeft, "frontLeftDone");
        setupButton(frontRight, "frontRightDone");
        setupButton(rearLeft, "rearLeftDone");
        setupButton(rearRight, "rearRightDone");
    }
}
