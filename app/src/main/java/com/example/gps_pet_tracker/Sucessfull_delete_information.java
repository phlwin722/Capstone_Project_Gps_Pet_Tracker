 package com.example.gps_pet_tracker;

 import android.content.Intent;
 import android.os.Bundle;
 import android.os.Handler;
 import android.widget.TextView;

 import androidx.activity.EdgeToEdge;
 import androidx.appcompat.app.AppCompatActivity;

 import java.io.Serializable;

 public class Sucessfull_delete_information extends AppCompatActivity {

     String previousFragment ;
     HistoryFetch pet;
     String myInformationDelete;
     TextView textTV;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_sucessfull_delete_information);

        // Handle back press event
        previousFragment = getIntent().getStringExtra("previousFragment");
        // Retrieve the 'pet' object passed from the previous activity
        pet = (HistoryFetch) getIntent().getSerializableExtra("pet");
        myInformationDelete = getIntent().getStringExtra("myInformationDelete");

        textTV = findViewById(R.id.textTV);

        if (myInformationDelete != null && !myInformationDelete.isEmpty()) {
            textTV.setText("Your account has been successfully deleted.");
        }


        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
               if (previousFragment != null && previousFragment.equals("1")) {
                   Intent intent = new Intent(Sucessfull_delete_information.this, Home.class);
                   intent.putExtra("previousFragment",previousFragment);
                   intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP); // Just keep this flag
                   startActivity(intent);
                   finish();
               } else if (pet != null) {
                   Intent i = new Intent(Sucessfull_delete_information.this,MapRoutePetHistory.class);
                   i.putExtra("pet", (Serializable) pet);
                   i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP); // Just keep this flag
                   startActivity(i);
                   finish();
               } else if (myInformationDelete != null && !myInformationDelete.isEmpty()) {
                   Intent intent = new Intent(Sucessfull_delete_information.this,Home.class);
                   intent.putExtra("myInformationDelete",myInformationDelete);
                   intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP); // Just keep this flag
                   startActivity(intent);
                   finish();
               }
            }
        },3000);
    }
}