package com.example.pitstopfrenzy;

//import static com.google.firebase.FirebaseApp.initializeApp;

import android.content.Intent;
import android.os.Bundle;
//import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
//import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
//import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.AuthCredential;
//import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.GoogleAuthProvider;

import java.util.Objects;
/** @noinspection CallToPrintStackTrace*/
public class LogInActivity extends AppCompatActivity {
    FirebaseAuth auth; // Firebase Authentication instance to handle user authentication
    GoogleSignInClient googleSignInClient; // Client for managing Google Sign-In interactions
    ShapeableImageView imageView; // Customizable ImageView for displaying user profile picture
    TextView name, mail; // TextViews to display the user's name and email

    // Launcher for starting the Google Sign-In intent and handling its result
    private final ActivityResultLauncher<Intent> activityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode() == RESULT_OK) { // Check if the result indicates a successful operation
                        // Get the GoogleSignInAccount object from the intent data
                        Task<GoogleSignInAccount> accountTask = GoogleSignIn.getSignedInAccountFromIntent(result.getData());
                        try {
                            // Retrieve the GoogleSignInAccount if successful
                            GoogleSignInAccount signInAccount = accountTask.getResult(ApiException.class);

                            // Create a credential using the Google ID token
                            AuthCredential authCredential = GoogleAuthProvider.getCredential(signInAccount.getIdToken(), null);

                            // Authenticate with Firebase using the credential
                            auth.signInWithCredential(authCredential).addOnCompleteListener(task -> {
                                if (task.isSuccessful()) { // If Firebase sign-in succeeds
                                    auth = FirebaseAuth.getInstance(); // Refresh the auth instance to ensure it's updated

                                    // Load the user's profile picture into the ImageView using Glide
                                    Glide.with(LogInActivity.this).load(Objects.requireNonNull(auth.getCurrentUser()).getPhotoUrl()).into(imageView);

                                    // Set the user's name and email in the corresponding TextViews
                                    name.setText(auth.getCurrentUser().getDisplayName());
                                    mail.setText(auth.getCurrentUser().getEmail());

                                    // Display a success message to the user
                                    Toast.makeText(LogInActivity.this, "Sign in successfully!", Toast.LENGTH_SHORT).show();

                                    // Start the main activity and pass the username as an extra
                                    Intent intent = new Intent(LogInActivity.this, MainActivity.class);
                                    intent.putExtra("USERNAME", auth.getCurrentUser().getDisplayName());
                                    startActivity(intent);
                                } else { // If Firebase sign-in fails
                                    Toast.makeText(LogInActivity.this, "Failed to sign in: " + task.getException(), Toast.LENGTH_SHORT).show();
                                }
                            });
                        } catch (ApiException e) { // Handle any exceptions related to Google Sign-In
                            e.printStackTrace();
                        }
                    }
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this); // Enable edge-to-edge display for the activity
        setContentView(R.layout.activity_log_in); // Set the layout for this activity

        FirebaseApp.initializeApp(this); // Initialize Firebase SDK for the app
        imageView = findViewById(R.id.profileImage); // Find the ImageView for displaying the profile picture
        name = findViewById(R.id.nameTV); // Find the TextView for displaying the user's name
        mail = findViewById(R.id.mailTV); // Find the TextView for displaying the user's email

        // Configure Google Sign-In options with the client ID and email request
        GoogleSignInOptions options = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.client_id)) // Request an ID token for authentication
                .requestEmail() // Request access to the user's email
                .build();

        googleSignInClient = GoogleSignIn.getClient(LogInActivity.this, options); // Initialize the GoogleSignInClient
        auth = FirebaseAuth.getInstance(); // Get the Firebase Authentication instance

        SignInButton signInButton = findViewById(R.id.signIn); // Find the Google Sign-In button
        signInButton.setOnClickListener(view -> {
            // Start the Google Sign-In intent when the button is clicked
            Intent intent = googleSignInClient.getSignInIntent();
            activityResultLauncher.launch(intent); // Launch the intent using the ActivityResultLauncher
        });
    }
}