package com.example.pitstopfrenzy;

import android.os.Handler;
import android.os.Looper;

public class GameTimer {
    private static GameTimer instance;
    private int seconds = 0;
    private boolean running = false;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private Runnable runnable;

    public static synchronized GameTimer getInstance() {
        if (instance == null) {
            instance = new GameTimer();
        }
        return instance;
    }

    private GameTimer() {
        runnable = new Runnable() {
            @Override
            public void run() {
                if (running) {
                    seconds++;
                    handler.postDelayed(this, 1000);
                }
            }
        };
    }

    public void startTimer() {
        if (!running) {
            running = true;
            handler.post(runnable);
        }
    }

    public void stopTimer() {
        running = false;
    }

    public void resetTimer() {
        running = false;
        seconds = 0;
    }

    public int getSeconds() {
        return seconds;
    }

    public String getFormattedTime() {
        int mins = seconds / 60;
        int secs = seconds % 60;
        return String.format("%02d:%02d", mins, secs);
    }
}
