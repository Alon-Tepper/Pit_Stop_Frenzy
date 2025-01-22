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

//import com.google.firebase.auth.FirebaseAuth;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this); // Enables edge-to-edge display to give a more modern look by extending the app content behind system bars.
        setContentView(R.layout.activity_main); // Sets the layout for this activity using the activity_main.xml file.

        // Find the button for the front-left wheel and set an action to navigate to WheelGameActivity when clicked.
        Button button = findViewById(R.id.frontwheel1);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Creates an Intent to navigate from MainActivity to WheelGameActivity.
                Intent intent = new Intent(MainActivity.this, WheelGameActivity.class);
                startActivity(intent); // Starts the WheelGameActivity.
            }
        });

        // Find the button for the front-right wheel and set an action to navigate to WheelGameActivity when clicked.
        Button button1 = findViewById(R.id.frontwheel2);
        button1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Navigates to the WheelGameActivity.
                Intent intent = new Intent(MainActivity.this, WheelGameActivity.class);
                startActivity(intent); // Starts the WheelGameActivity.
            }
        });

        // Find the button for the rear-left wheel and set an action to navigate to WheelGameActivity when clicked.
        Button button2 = findViewById(R.id.rearwheel3);
        button2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Navigates to the WheelGameActivity.
                Intent intent = new Intent(MainActivity.this, WheelGameActivity.class);
                startActivity(intent); // Starts the WheelGameActivity.
            }
        });

        // Find the button for the rear-right wheel and set an action to navigate to WheelGameActivity when clicked.
        Button button3 = findViewById(R.id.rearwheel4);
        button3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Navigates to the WheelGameActivity.
                Intent intent = new Intent(MainActivity.this, WheelGameActivity.class);
                startActivity(intent); // Starts the WheelGameActivity.
            }
        });

        // Find the button for the rear wing adjustment and set an action to navigate to Rear_ViewActivity when clicked.
        Button button4 = findViewById(R.id.rearwing);
        button4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Creates an Intent to navigate from MainActivity to Rear_ViewActivity.
                Intent intent = new Intent(MainActivity.this, Rear_ViewActivity.class);
                startActivity(intent); // Starts the Rear_ViewActivity.
            }
        });

        // Applies padding to the main view to ensure proper layout adjustment when the system bars (like the status bar or navigation bar) are present.
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars()); // Gets the system bar dimensions.
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom); // Sets the padding to match the system bar insets.
            return insets; // Returns the insets for further processing if needed.
        });
    }
}
