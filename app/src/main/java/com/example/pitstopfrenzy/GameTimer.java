package com.example.pitstopfrenzy;

import android.os.Handler;
import android.os.Looper;

public class GameTimer {
    private static GameTimer instance; // Singleton instance
    private int seconds = 0; // Tracks elapsed seconds
    private boolean running = false; // Indicates if timer is running
    private final Handler handler = new Handler(Looper.getMainLooper()); // Handler for posting to main thread
    private Runnable runnable;

    // Singleton getter to ensure only one instance exists
    public static synchronized GameTimer getInstance() {
        if (instance == null) {
            instance = new GameTimer(); // Create instance if it doesn't exist
        }
        return instance;
    }

    // Private constructor initializes the timer logic
    private GameTimer() {
        runnable = new Runnable() {
            @Override
            public void run() {
                if (running) {
                    seconds++; // Increment the timer by one second
                    handler.postDelayed(this, 1000); // Schedule next run after 1 second
                }
            }
        };
    }

    // Starts the timer if it's not already running
    public void startTimer() {
        if (!running) {
            running = true; // Mark the timer as running
            handler.post(runnable); // Start the runnable
        }
    }

    // Stops the timer
    public void stopTimer() {
        running = false; // Just stops the loop; doesn't remove callbacks
    }

    // Resets the timer to 0 and stops it
    public void resetTimer() {
        running = false; // Stop the timer
        seconds = 0; // Reset elapsed time
    }

    // Returns the number of seconds elapsed
    public int getSeconds() {
        return seconds;
    }

    // Returns the formatted time in MM:SS format
    public String getFormattedTime() {
        int mins = seconds / 60; // Calculate minutes
        int secs = seconds % 60; // Calculate remaining seconds
        return String.format("%02d:%02d", mins, secs); // Format as 2-digit minutes:seconds
    }
}
