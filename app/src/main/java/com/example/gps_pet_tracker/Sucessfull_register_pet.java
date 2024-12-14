package com.example.gps_pet_tracker;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

public class Sucessfull_register_pet extends AppCompatActivity {
    Button logiin;
    LinearLayout login;
    TextView loginn;
    String previousFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_sucessfull_register_pet);

        login = findViewById(R.id.loginn);
        loginn = findViewById(R.id.loginnn);
        logiin = findViewById(R.id.logiinn);

        // Handle back press event
        previousFragment = getIntent().getStringExtra("previousFragment");


        logiin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (previousFragment.equals("1")) {
                    Intent i = new Intent(getApplicationContext(),com.example.gps_pet_tracker.Home.class);
                    i.putExtra("previousFragment",previousFragment);
                    i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP); // Just keep this flag
                    startActivity(i);
                    finish();
                } else {
                    Intent i = new Intent(getApplicationContext(),com.example.gps_pet_tracker.Registerr.class);
                    i.putExtra("previousFragment",previousFragment);
                    i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP); // Just keep this flag
                    startActivity(i);
                    finish();
                }
            }
        });

        loginn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (previousFragment.equals("1")) {
                    Intent i = new Intent(getApplicationContext(),com.example.gps_pet_tracker.Home.class);
                    i.putExtra("previousFragment",previousFragment);
                    i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP); // Just keep this flag
                    startActivity(i);
                    finish();
                } else {
                    Intent i = new Intent(getApplicationContext(),com.example.gps_pet_tracker.Registerr.class);
                    i.putExtra("previousFragment",previousFragment);
                    i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP); // Just keep this flag
                    startActivity(i);
                    finish();
                }
            }
        });

        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (previousFragment.equals("1")) {
                    Intent i = new Intent(getApplicationContext(),com.example.gps_pet_tracker.Home.class);
                    i.putExtra("previousFragment",previousFragment);
                    i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP); // Just keep this flag
                    startActivity(i);
                    finish();
                } else {
                    Intent i = new Intent(getApplicationContext(),com.example.gps_pet_tracker.Registerr.class);
                    i.putExtra("previousFragment",previousFragment);
                    i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP); // Just keep this flag
                    startActivity(i);
                    finish();
                }
            }
        });
    }
}