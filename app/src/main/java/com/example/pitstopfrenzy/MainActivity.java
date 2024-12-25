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

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        Button button = findViewById(R.id.frontwheel1);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Navigate to WheelGameActivity
                Intent intent = new Intent(MainActivity.this, WheelGameActivity.class);
                startActivity(intent);
            }
        });

        Button button1 = findViewById(R.id.frontwheel2);
        button1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Navigate to WheelGameActivity
                Intent intent = new Intent(MainActivity.this, WheelGameActivity.class);
                startActivity(intent);
            }
        });

        Button button2 = findViewById(R.id.rearwheel3);
        button2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Navigate to WheelGameActivity
                Intent intent = new Intent(MainActivity.this, WheelGameActivity.class);
                startActivity(intent);
            }
        });

        Button button3 = findViewById(R.id.rearwheel4);
        button3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Navigate to WheelGameActivity
                Intent intent = new Intent(MainActivity.this, WheelGameActivity.class);
                startActivity(intent);
            }
        });

        Button button4 = findViewById(R.id.rearwing);
        button4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Navigate to rearWingGameActivity
                Intent intent = new Intent(MainActivity.this, Rear_ViewActivity.class);
                startActivity(intent);
            }
        });


        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }
}