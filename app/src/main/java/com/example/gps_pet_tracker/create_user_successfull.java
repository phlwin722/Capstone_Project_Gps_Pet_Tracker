package com.example.gps_pet_tracker;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class create_user_successfull extends AppCompatActivity {
    Button logiin;
    LinearLayout login;
    TextView loginn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_user_successfull);

        login = findViewById(R.id.login);
        loginn = findViewById(R.id.loginn);
        logiin = findViewById(R.id.logiin);

        logiin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(getApplicationContext(),com.example.gps_pet_tracker.login.class);
                i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP); // Just keep this flag
                startActivity(i);
                finish();
            }
        });

        loginn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(getApplicationContext(),com.example.gps_pet_tracker.login.class);
                i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP); // Just keep this flag
                startActivity(i);
                finish();
            }
        });

        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getApplicationContext(),com.example.gps_pet_tracker.login.class);
                startActivity(i);
                finish();
            }
        });
    }
}
