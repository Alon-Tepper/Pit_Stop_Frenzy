package com.example.pitstopfrenzy;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.FirebaseDatabase;
import com.google.android.material.imageview.ShapeableImageView;

import java.util.Objects;

public class LogInActivity extends AppCompatActivity {
    FirebaseAuth auth; // Firebase authentication instance
    GoogleSignInClient googleSignInClient; // Google sign-in client
    ShapeableImageView imageView; // Profile image view
    TextView name, mail; // TextViews for displaying name and email

    // Launcher for Google Sign-In result
    private final ActivityResultLauncher<Intent> activityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode() == RESULT_OK) {
                        // Get sign-in account from result
                        Task<GoogleSignInAccount> accountTask = GoogleSignIn.getSignedInAccountFromIntent(result.getData());
                        try {
                            GoogleSignInAccount signInAccount = accountTask.getResult(ApiException.class);
                            AuthCredential authCredential = GoogleAuthProvider.getCredential(signInAccount.getIdToken(), null);

                            // Sign in to Firebase with Google credentials
                            auth.signInWithCredential(authCredential).addOnCompleteListener(task -> {
                                if (task.isSuccessful()) {
                                    auth = FirebaseAuth.getInstance();
                                    FirebaseDatabase database = FirebaseDatabase.getInstance();
                                    String userId = Objects.requireNonNull(auth.getCurrentUser()).getUid();

                                    // Load game time from SharedPreferences
                                    SharedPreferences prefs = getSharedPreferences("game", MODE_PRIVATE);
                                    int finalTime = prefs.getInt("finalTime", 0);

                                    // Create a custom User object
                                    User user = new User(
                                            userId,
                                            auth.getCurrentUser().getDisplayName(),
                                            auth.getCurrentUser().getEmail(),
                                            auth.getCurrentUser().getPhotoUrl() != null ? auth.getCurrentUser().getPhotoUrl().toString() : "",
                                            finalTime
                                    );

                                    // Save user data to Firebase Realtime Database
                                    database.getReference("Users").child(userId).setValue(user);

                                    // Load profile picture using Glide
                                    Glide.with(LogInActivity.this)
                                            .load(Objects.requireNonNull(auth.getCurrentUser()).getPhotoUrl())
                                            .into(imageView);

                                    // Display user name and email
                                    name.setText(auth.getCurrentUser().getDisplayName());
                                    mail.setText(auth.getCurrentUser().getEmail());

                                    Toast.makeText(LogInActivity.this, "Sign in successful!", Toast.LENGTH_SHORT).show();

                                    // Go to MainActivity
                                    Intent intent = new Intent(LogInActivity.this, MainActivity.class);
                                    intent.putExtra("USERNAME", auth.getCurrentUser().getDisplayName());
                                    startActivity(intent);
                                } else {
                                    Toast.makeText(LogInActivity.this, "Failed to sign in: " + task.getException(), Toast.LENGTH_SHORT).show();
                                }
                            });
                        } catch (ApiException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this); // Enable full screen layout
        setContentView(R.layout.activity_log_in); // Set the layout

        FirebaseApp.initializeApp(this); // Initialize Firebase

        // Link UI elements
        imageView = findViewById(R.id.profileImage);
        name = findViewById(R.id.nameTV);
        mail = findViewById(R.id.mailTV);

        // Configure Google Sign-In options
        GoogleSignInOptions options = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.client_id)) // Request ID token
                .requestEmail() // Request user email
                .build();

        googleSignInClient = GoogleSignIn.getClient(LogInActivity.this, options); // Create client
        auth = FirebaseAuth.getInstance(); // Get FirebaseAuth instance

        // Handle sign-in button click
        SignInButton signInButton = findViewById(R.id.signIn);
        signInButton.setOnClickListener(view -> {
            Intent intent = googleSignInClient.getSignInIntent(); // Launch Google sign-in
            activityResultLauncher.launch(intent); // Handle result
        });
    }
}
