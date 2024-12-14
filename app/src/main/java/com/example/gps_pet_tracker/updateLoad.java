package com.example.gps_pet_tracker;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class updateLoad extends AppCompatActivity {
    TextInputLayout inputLayoutPetname, inputLayoutArduinoId,inputlayoutData;
    EditText petId, petname;
    Spinner categoryPetData;
    TextView DataInfoo;
    ProgressBar Progress;
    ImageView captureImage;
    FirebaseDatabase firebaseDatabase;
    StorageReference storageReference;
    FirebaseStorage firebaseStorage;
    FirebaseAuth firebaseAuth;
    int dataPlan = 0, positionn = 0;
    String expired, petDataPlan, petDataDate, petDataTime, petDays, petDataMonthorDay, petDayORMONTH,arduinoId,petName,userID, expDate;
    Button btn_edit_information, btn_backPage, btn_backPagee;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_update_load);

        Intent intent = getIntent();
         arduinoId = intent.getStringExtra("arduinoId");
         petName = intent.getStringExtra("petName");
         userID = intent.getStringExtra("userID");

        inputLayoutPetname = findViewById(R.id.inputlayoutPetname);
        inputLayoutArduinoId = findViewById(R.id.inputlayoutArduinoId);
        petId = findViewById(R.id.etArduinoID);
        petname = findViewById(R.id.etPetname);
        categoryPetData = findViewById(R.id.tilData);

        DataInfoo = findViewById(R.id.DataInfoo);
        Progress = findViewById(R.id.Progress);
        captureImage = findViewById(R.id.captureImage);
        inputlayoutData = findViewById(R.id.inputlayoutData);
        btn_edit_information = findViewById(R.id.btn_edit_information);
        btn_backPage = findViewById(R.id.btn_backPage);
        btn_backPagee = findViewById(R.id.btn_backPagee);


        // Initialize Firebase instances
        firebaseAuth = FirebaseAuth.getInstance();
        firebaseStorage = FirebaseStorage.getInstance();
        storageReference = firebaseStorage.getReference();
        firebaseDatabase = FirebaseDatabase.getInstance();

        petId.setText(arduinoId);
        petname.setText(petName);

        // Load the pet image using Picasso or Glide
        StorageReference profileref = storageReference.child("Pet Image").child(userID).child(arduinoId).child("/profile.jpg");
        profileref.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                Glide.with(getApplicationContext()).load(uri).into(captureImage);
                Progress.setVisibility(View.GONE);
                captureImage.setVisibility(View.VISIBLE);
            }
        });

        ArrayAdapter<CharSequence> adapterr = new ArrayAdapter<CharSequence>(
                this, R.layout.custom_spinner_item, getResources().getStringArray(R.array.sim_balance)) {
            @Override
            public View getDropDownView(int position, View convertView, ViewGroup parent) {
                View view = super.getDropDownView(position, convertView, parent);
                TextView textView = view.findViewById(R.id.spinner_item);

                // Make the placeholder item (position == 0) appear differently
                if (position == 0) {
                    textView.setTextColor(Color.GRAY);  // Set the placeholder color to gray
                    textView.setTypeface(null, Typeface.ITALIC);  // Make the placeholder italic
                } else {
                    textView.setTextColor(Color.BLACK);  // Normal color for valid items
                    textView.setTypeface(null, Typeface.NORMAL);  // Normal style for valid items
                }

                return view;
            }
        };
        // Set the adapter for the Spinner
        categoryPetData.setAdapter(adapterr);

        categoryPetData.setSelection(0, false);

        // Handle item selection
        categoryPetData.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {
                positionn = position;
                // If it's the first selection and the user selects "Select an animal...", do nothing
                if (position == 0) {
                    inputlayoutData.setError("Please select a data plan");
                    DataInfoo.setText("");
                    petDataPlan = "";
                } else {
                    double totalDataMB = 0;
                    if (position >= 1) {
                        int Conversion = 0;
                        if (position == 1){
                            Conversion = position + 1;
                        } else if (position == 2){
                            Conversion = position + 4;
                        }else if (position == 3){
                            Conversion = position * 8;
                        }else if (position == 4){
                            Conversion = position * 9;
                        }else if (position == 5){
                            Conversion = (position * 9) + 3;
                        }else if (position == 6){
                            Conversion = position * 10;
                        }

                        // Define the total data in MB (2GB = 2048MB)
                        totalDataMB = Conversion * 1024;
                    }

                    // Define the hourly data usage in MB
                    double hourlyUsageMB = 0.46;

                    // Calculate the total time in hours
                    double timeInHours = totalDataMB / hourlyUsageMB;

                    // Convert hours to days (1 day = 24 hours)
                    double timeInDays = timeInHours / 24;

                    // Convert days to months (1 month = 30.44 days on average)
                    double timeInMonths = timeInDays / 30.44;

                    double timeInYears = timeInDays / 365.25;

                    LocalDate localDate = LocalDate.now();

                    // Get the current time
                    LocalTime currentTime = LocalTime.now();

                    // Create a DateTimeFormatter object to parse time in HH:mm (24-hour format with two-digit hours)
                    DateTimeFormatter timeFormat = DateTimeFormatter.ofPattern("HH:mm");

                    // Format the current time to match the pattern HH:mm
                    String formattedTime = currentTime.format(timeFormat); // Example: "15:45"

                    // Define the starting date as today's date
                    LocalDate startDate = LocalDate.now();

                    // Add 185 days to the start date
                    LocalDate newDate = startDate.plusDays((int) timeInDays);
                    expired = "Hello, your pet " + petName + " data load promo, which you availed, is estimated to expire on " + (int) timeInDays + " Days (" + (int) timeInMonths + " Months) " + String.valueOf(newDate) + ".";
                    expDate = String.valueOf(newDate);

                    // Print the results
                    Log.d("Balance", "Time to use up 2GB of data at 0.46MB per hour:");
                    Log.d("Balance", "Time in hours: " + (int) timeInHours);
                    Log.d("Balance", "Time in days: " + (int) timeInDays);
                    Log.d("Balance", "Time in months: " +  timeInMonths);
                    Log.d("Balance", "Time in years: " +  (int) timeInYears);
                    Log.d("Balance", "Date now: " +  localDate);
                    Log.d("Balance", "Date time: " +  formattedTime);

                    petDataDate = String.valueOf(localDate);
                    petDataTime = formattedTime;
                    petDays = String.valueOf((int) timeInDays);

                    if ((int) timeInMonths < 1) {
                        DataInfoo.setText(expired);
                        petDataMonthorDay = String.valueOf((int) timeInDays);
                        petDayORMONTH = "day";
                    } else if ((int) timeInMonths >= 1 && timeInMonths <= 12) {
                        DataInfoo.setText(expired);
                        petDataMonthorDay = String.valueOf((int) timeInMonths);
                        petDayORMONTH = "months";
                    } else {
                        DataInfoo.setText(expired);
                        petDataMonthorDay = String.valueOf((int) timeInMonths);
                        petDayORMONTH = "Year";
                    }
                    // Handle valid selection (Dog or Cat)
                    inputlayoutData.setErrorEnabled(false);
                    dataPlan = position;
                    petDataPlan = adapterView.getItemAtPosition(position).toString();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                // Handle the case when nothing is selected (optional)
                petDataPlan = "";
                Toast.makeText(getApplicationContext(), "Please select a pet category", Toast.LENGTH_SHORT).show();
            }
        });

        btn_edit_information.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (positionn != 0) {
                    updateInformation ();
                }
            }
        });

        btn_backPage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                backPAge();
            }
        });

        btn_backPagee.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                backPAge();
            }
        });

    }

    private void updateInformation () {
        DatabaseReference databaseReference1 = firebaseDatabase.getReference("Pet Information")
                .child(userID)
                .child(arduinoId);

        Map<String,Object> update = new HashMap<>();
        update.put("asking","notAsking");
        update.put("askme5minutes","notAskME");
        update.put("askuserifpetload","notAsking");
        update.put("petDays",petDays);
        update.put("petDataPlan",petDataPlan);
        update.put(  "petDataTime",petDataTime);
        update.put("expired",expired);
        update.put("expDate",expDate);
        update.put("petDayORMONTH",petDayORMONTH);

        databaseReference1.updateChildren(update).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                DatabaseReference databaseReference = firebaseDatabase.getReference("User Notification")
                        .child(userID);

                // get current date
                Date currentDate = new Date();
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("MMM dd 'at' hh:mm a");
                String formattedtime = simpleDateFormat.format(currentDate);

                long timeStamp = System.currentTimeMillis();

                // Create a new notification ID
                String notificationID = databaseReference.push().getKey();

                Map<String,Object> insertNotification = new HashMap<>();
                insertNotification.put("notification",expired);
                insertNotification.put("date", formattedtime);
                insertNotification.put("timestamp", timeStamp);

                databaseReference.child(notificationID).setValue(insertNotification).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        Intent intent = new Intent(updateLoad.this, Sucessfull_update.class);
                        intent.putExtra("SafeZoneee","SafeZonee");
                        startActivity(intent);
                        finish();
                    }
                });
            }
        });
    }

    private void backPAge () {
        Intent intent = new Intent(updateLoad.this,Home.class);
        intent.putExtra("SafeZonee","SafeZonee");
        startActivity(intent);
        finish();
    }
}