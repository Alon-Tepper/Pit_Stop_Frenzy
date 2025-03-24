package com.example.pitstopfrenzy;

import android.content.Intent;
import android.os.Bundle;
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

    private ShapeableImageView profileImage; // Image view for user profile picture
    private TextView userName, userEmail, userBestTime; // UI elements to display user info
    private User user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_info); // Set the layout for this screen

        // Link UI elements
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
    }

    // Load user info from Firebase Realtime Database
    private void loadUserData() {
        FirebaseAuth auth = FirebaseAuth.getInstance();

        // Protect against null user
        if(auth.getCurrentUser() == null) return;

        String userId = auth.getCurrentUser().getUid();
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("Users").child(userId);

        // Fetch user data from Firebase
        userRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult().exists()) {
                User user = task.getResult().getValue(User.class);
                if (user != null) {
                    // Display user name and email, or default if missing
                    userName.setText(user.getUserName() != null ? user.getUserName() : "No Name");
                    userEmail.setText(user.getEmail() != null ? user.getEmail() : "No Email");

                    // Format and display best time if available
                    if (user.getTime() > 0) {
                        int minutes = user.getTime() / 60;
                        int seconds = user.getTime() % 60;
                        userBestTime.setText(String.format("Best Time: %02d:%02d", minutes, seconds));
                    } else {
                        userBestTime.setText("Best Time: N/A");
                    }

                    // Load profile image from URL using Glide
                    if (user.getProfileImageUrl() != null && !user.getProfileImageUrl().isEmpty()) {
                        Glide.with(this)
                                .load(user.getProfileImageUrl())
                                .circleCrop() // Make it circular
                                .into(profileImage);
                    } else {
                        // Show default image if no profile picture
                        profileImage.setImageResource(R.drawable.h_tire);
                    }
                }
            } else {
                // Show error toast if data could not be loaded
                Toast.makeText(this, "Unable to load user data.", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
