package com.example.gps_pet_tracker;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity {

    private FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        firebaseAuth = FirebaseAuth.getInstance();

        // Using a handler to delay the action for 3 seconds (loading screen simulation)
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                // Check if the user is already logged in
                start();
            }
        }, 3000); // 3000 milliseconds = 3 seconds
    }

    private void start() {
        // Check if the user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        if (currentUser != null) {
            // User is already logged in, navigate to Home activity
            Intent i = new Intent(getApplicationContext(), Home.class);
            i.putExtra("selectMapUserNotEmpty","0");
            startActivity(i);
            finish(); // Finish current activity to prevent returning to this screen
        } else {
            // User is not logged in, navigate to login activity
            Intent intent = new Intent(MainActivity.this, login.class);
            startActivity(intent);
            finish(); // Finish current activity after navigating to login screen
        }
    }}
