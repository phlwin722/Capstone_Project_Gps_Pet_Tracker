package com.example.gps_pet_tracker;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;

public class Sucessfull_update extends AppCompatActivity {

    TextView info;
    FirebaseAuth firebaseAuth;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_sucessfull_update);

        firebaseAuth = FirebaseAuth.getInstance();
        info = findViewById(R.id.info);

        Intent i = getIntent();
        String pet_information_details = i.getStringExtra("pet_information_details");
        String My_information = i.getStringExtra("My_information");
        String changePassword = i.getStringExtra("changePassword");
        String Email = i.getStringExtra("Email");
        String SafeZone = i.getStringExtra("SafeZonee");
        String SafeZoneee = i.getStringExtra("SafeZoneee");
        String PasswordUpdate = i.getStringExtra("PasswordUpdate");
        String register_class = i.getStringExtra("register_class");
        String previousFragment_from_register = i.getStringExtra("previousFragment");


        if (pet_information_details != null && !pet_information_details.isEmpty()) {
            info.setText("Your Pet information updated successfully. ");
        }

        if (My_information != null && !My_information.isEmpty()) {
            info.setText("Your information updated successfully.");
        }

        if (SafeZone != null && !SafeZone.isEmpty()) {
            info.setText("Your location updated successfully.");
        }

        if (register_class != null && !register_class.isEmpty()) {
            info.setText("Your location updated successfully.");
        }

        if (SafeZoneee != null && !SafeZoneee.isEmpty()) {
            info.setText("Your pet load data plan has been updated successfully");
        }

        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (pet_information_details != null) {
                    Intent i = new Intent(Sucessfull_update.this,Home.class);
                    i.putExtra("select_list_fragment","select_list_fragment");
                    i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP); // Just keep this flag
                    startActivity(i);
                    finish();
                }else if (My_information != null) {
                    Intent i = new Intent(Sucessfull_update.this,MyInformation.class);
                    i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP); // Just keep this flag
                    startActivity(i);
                    finish();

                }
                else if (changePassword != null) {
                  //  Intent service = new Intent(Sucessfull_update.this,myForeGroundService.class);
                  //  stopService(service);

                    //firebaseAuth.signOut();

                    Intent intent = new Intent(Sucessfull_update.this, change_password.class);
                    intent.putExtra("changePassword",changePassword);
                    intent.putExtra("Email",Email);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP); // Just keep this flag
                    startActivity(intent);
                    finish(); // Optional: Finish the current activity
                } else if (SafeZone != null && !SafeZone.isEmpty()){
                    Intent i = new Intent(Sucessfull_update.this,Home.class);
                    i.putExtra("SafeZonee","select_list_fragment");
                    i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP); // Just keep this flag
                    startActivity(i);
                    finish();
                } else if (SafeZoneee != null && !SafeZoneee.isEmpty()) {
                    Intent i = new Intent(Sucessfull_update.this,Home.class);
                    i.putExtra("SafeZonee","select_list_fragment");
                    i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP); // Just keep this flag
                    startActivity(i);
                    finish();
                }
                else if (register_class != null && !register_class.isEmpty()) {
                    Intent i = new Intent(Sucessfull_update.this, Registerr.class);
                    i.putExtra("previousFragment",previousFragment_from_register);
                    i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP); // Just keep this flag
                    startActivity(i);
                    finish();
                } else if (PasswordUpdate != null && !PasswordUpdate.isEmpty()) {
                    Intent intent = new Intent(Sucessfull_update.this, Home.class);
                    intent.putExtra("PasswordUpdate","PasswordUpdate");
                    startActivity(intent);
                    finish();
                }
            }
        },3000);
    }
}