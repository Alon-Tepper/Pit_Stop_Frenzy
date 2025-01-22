package com.example.pitstopfrenzy;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class WheelGameActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState); // Initializes the activity.
        EdgeToEdge.enable(this); // Enables edge-to-edge UI for modern device screens.
        setContentView(R.layout.activity_wheel_game); // Sets the XML layout for this activity.

        // Adjusts layout padding to avoid overlap with system bars.
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars()); // Gets system bar insets.
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom); // Applies padding dynamically.
            return insets; // Returns the insets for processing.
        });

        // Configures the button to return to the main menu when clicked.
        Button buttonM1 = findViewById(R.id.backtomainbutton1); // Finds the button in the layout.
        buttonM1.setOnClickListener(v -> { // Sets a click listener.
            Intent intent = new Intent(WheelGameActivity.this, MainActivity.class); // Prepares navigation to MainActivity.
            startActivity(intent); // Starts the MainActivity.
        });
    }
}