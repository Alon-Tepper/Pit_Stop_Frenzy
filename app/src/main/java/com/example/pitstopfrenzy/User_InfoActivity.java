package com.example.pitstopfrenzy;

import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.Log;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import com.bumptech.glide.Glide;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class User_InfoActivity extends AppCompatActivity {

    private ShapeableImageView profileImage;
    private TextView userName, userEmail, userBestTime;
    private User user;

    // Light sensor component
    private SensorManager sensorManager;
    private Sensor lightSensor;
    private SensorEventListener lightSensorListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_info);

        // Link UI element
        profileImage = findViewById(R.id.profile_image);
        userName = findViewById(R.id.user_name);
        userEmail = findViewById(R.id.user_email);
        userBestTime = findViewById(R.id.user_best_time);

        loadUserData(); // Load and display user data from Firebase

        // Handle back button to return to main activity
        Button backButton = findViewById(R.id.buttonBackToMain);
        backButton.setOnClickListener(v -> {
            Intent intent = new Intent(User_InfoActivity.this, MainActivity.class);
            startActivity(intent);
        });

        // Sensor setup - Light sensor for screen brightness
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        lightSensor = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);

        lightSensorListener = new SensorEventListener() {
            @Override
            public void onSensorChanged(android.hardware.SensorEvent event) {
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

    // Load user info from Firebase Realtime Database
    private void loadUserData() {
        FirebaseAuth auth = FirebaseAuth.getInstance();
        if (auth.getCurrentUser() == null) return;

        String userId = auth.getCurrentUser().getUid();
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("Users").child(userId);

        userRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult().exists()) {
                User user = task.getResult().getValue(User.class);
                if (user != null) {
                    userName.setText(user.getUserName() != null ? user.getUserName() : "No Name");
                    userEmail.setText(user.getEmail() != null ? user.getEmail() : "No Email");

                    if (user.getTime() > 0) {
                        int minutes = user.getTime() / 60;
                        int seconds = user.getTime() % 60;
                        userBestTime.setText(String.format("Best Time: %02d:%02d", minutes, seconds));
                    } else {
                        userBestTime.setText("Best Time: N/A");
                    }

                    if (user.getProfileImageUrl() != null && !user.getProfileImageUrl().isEmpty()) {
                        Glide.with(this)
                                .load(user.getProfileImageUrl())
                                .circleCrop()
                                .into(profileImage);
                    } else {
                        profileImage.setImageResource(R.drawable.h_tire);
                    }
                }
            } else {
                Toast.makeText(this, "Unable to load user data.", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
