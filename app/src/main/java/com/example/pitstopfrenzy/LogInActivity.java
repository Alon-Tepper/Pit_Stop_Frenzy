package com.example.pitstopfrenzy;

import android.content.Intent;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.Log;
import android.view.WindowManager;
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
    FirebaseAuth auth;
    GoogleSignInClient googleSignInClient;
    ShapeableImageView imageView;
    TextView name, mail;

    private SensorManager sensorManager;
    private Sensor lightSensor;
    private SensorEventListener lightSensorListener;

    private final ActivityResultLauncher<Intent> activityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode() == RESULT_OK) {
                        Task<GoogleSignInAccount> accountTask = GoogleSignIn.getSignedInAccountFromIntent(result.getData());
                        try {
                            GoogleSignInAccount signInAccount = accountTask.getResult(ApiException.class);
                            AuthCredential authCredential = GoogleAuthProvider.getCredential(signInAccount.getIdToken(), null);

                            auth.signInWithCredential(authCredential).addOnCompleteListener(task -> {
                                if (task.isSuccessful()) {
                                    auth = FirebaseAuth.getInstance();
                                    FirebaseDatabase database = FirebaseDatabase.getInstance();
                                    String userId = Objects.requireNonNull(auth.getCurrentUser()).getUid();

                                    SharedPreferences prefs = getSharedPreferences("game", MODE_PRIVATE);
                                    int finalTime = prefs.getInt("finalTime", 0);

                                    User user = new User(
                                            userId,
                                            auth.getCurrentUser().getDisplayName(),
                                            auth.getCurrentUser().getEmail(),
                                            auth.getCurrentUser().getPhotoUrl() != null ? auth.getCurrentUser().getPhotoUrl().toString() : "",
                                            finalTime
                                    );

                                    database.getReference("Users").child(userId).setValue(user);

                                    Glide.with(LogInActivity.this)
                                            .load(Objects.requireNonNull(auth.getCurrentUser()).getPhotoUrl())
                                            .into(imageView);

                                    name.setText(auth.getCurrentUser().getDisplayName());
                                    mail.setText(auth.getCurrentUser().getEmail());

                                    Toast.makeText(LogInActivity.this, "Sign in successful!", Toast.LENGTH_SHORT).show();

                                    Intent intent = new Intent(LogInActivity.this, MainActivity.class);
                                    intent.putExtra("USERNAME", auth.getCurrentUser().getDisplayName());
                                    startActivity(intent);
                                } else {
                                    Toast.makeText(LogInActivity.this, "Failed to sign in: " + task.getException(), Toast.LENGTH_SHORT).show();
                                }
                            });
                        } catch (ApiException e) {
                            Log.e("LOGIN_ERROR", "Google sign-in failed", e);
                        }
                    }
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_log_in);

        FirebaseApp.initializeApp(this);

        imageView = findViewById(R.id.profileImage);
        name = findViewById(R.id.nameTV);
        mail = findViewById(R.id.mailTV);

        GoogleSignInOptions options = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.client_id))
                .requestEmail()
                .build();

        googleSignInClient = GoogleSignIn.getClient(LogInActivity.this, options);
        auth = FirebaseAuth.getInstance();

        SignInButton signInButton = findViewById(R.id.signIn);
        signInButton.setOnClickListener(view -> {
            Intent intent = googleSignInClient.getSignInIntent();
            activityResultLauncher.launch(intent);
        });

        // SENSOR SETUP - Light sensor for screen brightness
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        lightSensor = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);

        lightSensorListener = new SensorEventListener() {
            @Override
            public void onSensorChanged(SensorEvent event) {
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
}
