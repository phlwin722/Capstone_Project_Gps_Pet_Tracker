package com.example.gps_pet_tracker;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class SuccessfulChangePassword extends AppCompatActivity {
LinearLayout bck_to_login;
Button loginn;
TextView login;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_successful_change_password);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        bck_to_login = findViewById(R.id.bck_to_login);
        loginn = findViewById(R.id.loginn);
        login = findViewById(R.id.login);

        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(getApplicationContext(), login.class);
                i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP); // Just keep this flag
                startActivity(i);
                finish();
            }
        });

        loginn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(getApplicationContext(), login.class);
                i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP); // Just keep this flag
                startActivity(i);
                finish();
            }
        });

        bck_to_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(getApplicationContext(), login.class);
                i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP); // Just keep this flag
                startActivity(i);
                finish();
            }
        });
    }
}