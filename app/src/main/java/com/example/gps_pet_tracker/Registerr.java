package com.example.gps_pet_tracker;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.github.dhaval2404.imagepicker.ImagePicker;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class Registerr extends AppCompatActivity implements ConnectionReceiver.ReceiverListener{
    Button btn_backPage,btn_backPagee , regiterrPet, btn_ok, btn_confirm;// Corrected button name
    boolean save = true;
    EditText petId, petname;
    Spinner categoryPetSpinner ,categoryPetData;
    TextView questionMark;
    AlertDialog show;
    TextInputLayout inputLayoutPetname, inputLayoutArduinoId, inputlayoutPetcategory,inputlayoutData;
    boolean isInternetDialogVisible = false;
    ImageView petImage;
    FirebaseFirestore firebaseFirestore;
    FirebaseAuth firebaseAuth;
    DatabaseReference databaseReference;
    FirebaseDatabase firebaseDatabase;
   // TextView level;d
    int petCategoryy = 0;
    int dataPlan = 0;
    String expired, expiredd;
    private boolean isFirstSelection = true; // Track if it's the first selection
    StorageReference storageReference;
    String userId,Email , arduinoID, petName, petNamee, petCategory =" ", petDataPlan="",petDataDate,petDayORMONTH, petDataMonthorDay,petDays,petDataTime, petImagee, previousFragment,expDate;
    Uri uri;
    AlertDialog showInternet;
    TextView DataInfoo;
    private static int PERMISSION_REQUEST_CODE = 1;
    private static final int IMAGE_PICKER_REQUEST_CODE = 200;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registerr);

        // Initialize views
        categoryPetSpinner  = findViewById(R.id.tilCategory);
        regiterrPet = findViewById(R.id.registerrPet);
        btn_backPage = findViewById(R.id.btn_backPage  ); // Corrected button reference0
        petImage = findViewById(R.id.petImage);
        petId = findViewById(R.id.petId);
        petname = findViewById(R.id.petname);
        questionMark = findViewById(R.id.questionMark);
        DataInfoo = findViewById(R.id.DataInfoo);
     //   level = findViewById(R.id.level);
        inputLayoutArduinoId = findViewById(R.id.inputlayoutArduinoId);
        inputlayoutPetcategory = findViewById(R.id.inputlayoutPetcategory);
        inputlayoutData = findViewById(R.id.inputlayoutData);
        categoryPetData = findViewById(R.id.tilData);
        inputLayoutPetname = findViewById(R.id.inputlayoutPetname);
        btn_backPagee = findViewById(R.id.btn_backPagee);

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();
        userId = firebaseAuth.getCurrentUser().getUid();
        firebaseDatabase = FirebaseDatabase.getInstance();
        storageReference = FirebaseStorage.getInstance().getReference();

        // Display alert sim_loaded
        AlertDialog.Builder sim_loaded = new AlertDialog.Builder(Registerr.this);
        View vieww = getLayoutInflater().inflate(R.layout.custom_user_notify_sim_loaded,null);
        sim_loaded.setView(vieww);
        sim_loaded.setCancelable(false);
        AlertDialog show_sim_loaded = sim_loaded.create();
        show_sim_loaded.show();

        btn_confirm = vieww.findViewById(R.id.btn_confirm);

        btn_confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                show_sim_loaded.dismiss();
            }
        });

        petname.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                petNamee = charSequence.toString();
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });


        // alert dialog on connection no internet
        AlertDialog.Builder builderInternet = new AlertDialog.Builder(Registerr.this);
        LayoutInflater layoutInflater = getLayoutInflater();
        View view = layoutInflater.inflate(R.layout.custom_no_internet, (ViewGroup) findViewById(R.id.custom_no_internet));
        btn_ok = view.findViewById(R.id.btn_ok);
        builderInternet.setView(view);
        showInternet = builderInternet.create();

        // Handle back press event
        previousFragment = getIntent().getStringExtra("previousFragment");

        btn_ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showInternet.dismiss();
            }
        });

        //get information on firebase firestore
        DocumentReference documentReference = firebaseFirestore.collection("Users Information").document(userId);
        documentReference.addSnapshotListener(Registerr.this, new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot value, @Nullable FirebaseFirestoreException error) {
                Email = value.getString("email");
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
                    expired = "Your data load promo, which you availed, is estimated to expire on " + (int) timeInDays + " Days (" + (int) timeInMonths + " Months) " + String.valueOf(newDate) + ".";
                    expiredd = "Hello, your pet " + petNamee + " data load promo, which you availed, is estimated to expire on " + (int) timeInDays + " Days (" + (int) timeInMonths + " Months) " + String.valueOf(newDate) + ".";
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

        // Initialize the spinner with the array from strings.xml
        ArrayAdapter<CharSequence> adapter = new ArrayAdapter<CharSequence>(
                this, R.layout.custom_spinner_item, getResources().getStringArray(R.array.animal_options)) {

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
        categoryPetSpinner.setAdapter(adapter);

        // Set the initial selection to "Select an animal..." (position 0), but don't trigger any action
        categoryPetSpinner.setSelection(0, false);

        // Handle item selection
        categoryPetSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {
                // If it's the first selection and the user selects "Select an animal...", do nothing
                if (position == 0 && isFirstSelection) {
                    return;  // Ignore "Select an animal..." on first selection
                }
                petCategoryy = position;

                // If position is 0, it means "Select an animal..." is still selected
                if (position == 0) {
                    // Reset category if placeholder is selected
                    petCategory = "";
                    //inputlayoutPetcategory.setError("Please select a valid pet category.");
                } else {
                    // Handle valid selection (Dog or Cat)
                    petCategory = adapterView.getItemAtPosition(position).toString();
                    inputlayoutPetcategory.setErrorEnabled(false);  // Clear the error if valid selection
                }

                // After the first valid selection, prevent returning to "Select an animal..."
                if (isFirstSelection) {
                    isFirstSelection = false;
                    // Set the spinner to show the first valid item (Dog or Cat)
                    categoryPetSpinner.setSelection(1); // Force the selection to the first valid item
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                // Handle the case when nothing is selected (optional)
                petCategory = "";
                Toast.makeText(getApplicationContext(), "Please select a pet category", Toast.LENGTH_SHORT).show();
            }
        });

        questionMark.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder alert = new AlertDialog.Builder(Registerr.this);
                View view1 = getLayoutInflater().inflate(R.layout.custom_note_question,null);
                alert.setView(view1);

                AlertDialog alertt = alert.create();

                alertt.show();

                Button custom_logout_button = view1.findViewById(R.id.btn_logout_confirm);

                custom_logout_button.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        alertt.dismiss();
                    }
                });
            }
        });

        petImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Define the required permissions for camera and external storage

                if (Build.VERSION.SDK_INT >= 33) {
                    String[] permissions = {
                            Manifest.permission.CAMERA,
                            Manifest.permission.READ_MEDIA_IMAGES // Required for API 30 and above
                    };
                    // Check if the required permissions are already granted
                    if (arePermissionsGranted(permissions)) {
                        // Permissions are granted, proceed with image picking
                        openImagePicker();
                    } else {
                        // Request permissions
                        ActivityCompat.requestPermissions(Registerr.this, permissions, PERMISSION_REQUEST_CODE);
                    }
                }else {
                    String[] permissions = {
                            Manifest.permission.CAMERA,
                            Manifest.permission.READ_EXTERNAL_STORAGE // Required for API 30 and above
                    };
                    // Check if the required permissions are already granted
                    if (arePermissionsGranted(permissions)) {
                        // Permissions are granted, proceed with image picking
                        openImagePicker();
                    } else {
                        // Request permissions
                        ActivityCompat.requestPermissions(Registerr.this, permissions, PERMISSION_REQUEST_CODE);
                    }
                }
            }
        });


        // Listener for notification button click
        btn_backPage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Handle back press event
                onBackPresse();
            }
        });

        btn_backPagee.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPresse();
            }
        });

        petId.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                inputLayoutArduinoId.setErrorEnabled(false);
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        petname.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                inputLayoutPetname.setErrorEnabled(false);
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        petId.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
                if (b) {
                    inputLayoutArduinoId.setBoxStrokeColor(Color.rgb(112,112,112));
                    inputLayoutArduinoId.setHintTextColor(ColorStateList.valueOf(Color.BLACK));
                } else {
                    inputLayoutArduinoId.setHintTextColor(ColorStateList.valueOf(Color.BLACK));
                    inputLayoutArduinoId.setBoxStrokeColor(Color.GRAY);
                }
            }
        });

        petname.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean hasFocus) {
                if (hasFocus) {
                    inputLayoutPetname.setBoxStrokeColor(Color.rgb(112,112,112));
                    inputLayoutPetname.setHintTextColor(ColorStateList.valueOf(Color.BLACK));
                } else {
                    inputLayoutPetname.setBoxStrokeColor(Color.GRAY);
                    inputLayoutPetname.setHintTextColor(ColorStateList.valueOf(Color.GRAY));
                }
            }
        });

        regiterrPet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                arduinoID = petId.getText().toString();
                petName = petname.getText().toString();
                   if (uri != null) {
                       if (!arduinoID.isEmpty()) {
                           inputLayoutArduinoId.setErrorEnabled(false);

                           if (arduinoID.length() != 11) {
                               inputLayoutArduinoId.setError("Device ID must be 11 characters");
                               save = false;
                           }else {
                               inputLayoutArduinoId.setErrorEnabled(false);
                               if (!petName.isEmpty()) {
                                   inputLayoutPetname.setErrorEnabled(false);

                                   if (petCategoryy == 0) {
                                       save = false;
                                       inputlayoutPetcategory.setError("Please select pet category");
                                   } else {
                                       if (petDataPlan != null) {
                                           inputlayoutPetcategory.setErrorEnabled(false); // Ensure to clear error if category is selected
                                           save = true;
                                           checkingInternet();
                                       }else {
                                           inputlayoutData.setError("Please select data plan.");
                                           save = false;
                                       }
                                   }

                               } else {
                                   save = false;
                                   inputLayoutPetname.setError("Field cannot be blank");
                               }
                           }
                       }else {
                           save = false;
                           inputLayoutArduinoId.setError("Field cannot be blank");
                       }
                   }else {
                       save = false;
                       Toast.makeText(Registerr.this,"Please select an image",Toast.LENGTH_SHORT).show();
                   }

                if (save) {

                    DatabaseReference databaseReference = firebaseDatabase.getReference("User SafeLocation")
                            .child(userId);
                    databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if (snapshot.exists()) {
                                if (snapshot.child("longitude").exists() && snapshot.child("latitude").exists()) {
                                    checkingInternet();

                                    AlertDialog.Builder builder = new AlertDialog.Builder(Registerr.this);
                                    builder.setCancelable(false);
                                    builder.setView(R.layout.custom_loading_process);

                                    show = builder.create();
                                    show.show();
                                    if (isInternetDialogVisible) {
                                        showInternet.dismiss();
                                        storePetInformation();
                                    }else {
                                        show.dismiss();
                                        showInternet.show();
                                    }
                                }
                            }else{
                                android.app.AlertDialog.Builder alert = new android.app.AlertDialog.Builder(Registerr.this);
                                alert.setCancelable(false);

                                LayoutInflater layoutInflater = getLayoutInflater();
                                View view = layoutInflater.inflate(R.layout.custom_safezon_alert,null);
                                alert.setView(view);
                                android.app.AlertDialog alertt = alert.create();

                                alertt.show();

                                Button set = view.findViewById(R.id.btn_set_now);
                                Button cancel = view.findViewById(R.id.btn_cancel);

                                set.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        Intent i = new Intent(Registerr.this, User_map_set_location.class);
                                        i.putExtra("register_class","register");
                                        i.putExtra("previousFragment",previousFragment);
                                        startActivity(i);
                                        alertt.dismiss();
                                    }
                                });

                                cancel.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        alertt.dismiss();
                                    }
                                });
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
                }
            }
        });

    }

    private void onBackPresse() {
        if (previousFragment != null && previousFragment.equals("0")) {
            // Navigate back to the Map fragment
            Intent i = new Intent(Registerr.this,Home.class);
            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP); // Just keep this flag
            i.putExtra("back_to_home_register",previousFragment);
            startActivity(i);
            finish();
        }
        else if (previousFragment != null && previousFragment.equals("1")) {
            // Navigate back to the List fragment
            // Navigate back to the Map fragment
            Intent i = new Intent(Registerr.this,Home.class);
            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP); // Just keep this flag
            i.putExtra("back_to_home_register",previousFragment);
            startActivity(i);
            finish();
        }
        else if (previousFragment != null && previousFragment.equals("2")) {
            // Navigate back to the History fragment
            // Navigate back to the Map fragment
            Intent i = new Intent(Registerr.this,Home.class);
            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP); // Just keep this flag
            i.putExtra("back_to_home_register",previousFragment);
            startActivity(i);
            finish();
        }
        else if (previousFragment != null && previousFragment.equals("3")) {
            // Navigate back to the Notification fragment
            // Navigate back to the Map fragment
            Intent i = new Intent(Registerr.this,Home.class);
            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP); // Just keep this flag
            i.putExtra("back_to_home_register",previousFragment);
            startActivity(i);
            finish();
        }
        else if (previousFragment != null && previousFragment.equals("4")) {
            // Navigate back to the Settings fragment
            // Navigate back to the Map fragment
            Intent i = new Intent(Registerr.this,Home.class);
            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP); // Just keep this flag
            i.putExtra("back_to_home_register",previousFragment);
            startActivity(i);
            finish();
        }else {
            Toast.makeText(getApplicationContext(),"Error",Toast.LENGTH_LONG).show();
        }

    }
    // Method to check if all specified permissions are granted
    private boolean arePermissionsGranted(String[] permissions) {
        // Iterate through each permission in the array
        for (String permission : permissions) {
            // Check if the current permission is not granted
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                // Return false if any permission is not granted
                return false;
            }
        }
        // Return true if all permissions are granted
        return true;
    }

    private void openImagePicker() {
        // Use the ImagePicker library to open the image picker
        ImagePicker.with(Registerr.this)
                .crop()                   // Optional: Enable image cropping
                .compress(1024)           // Optional: Compress image to be less than 1 MB
                .maxResultSize(1080, 1080) // Optional: Limit image resolution to 1080x1080
                .start(IMAGE_PICKER_REQUEST_CODE); // Start the image picker activity with a request code
    }
    // Handle the result of the permission request
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE) {
            // Iterate through the results to check if all permissions are granted
            boolean allPermissionsGranted = true;
            for (int result : grantResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    allPermissionsGranted = false;
                    break;
                }
            }
            // If all permissions are granted, proceed with image picking
            if (allPermissionsGranted) {
                // Permissions are granted, proceed with image picking
                openImagePicker();
            } else {
                // Handle the case where permissions are not granted
                showPermissionRationaleDialog(permissions);
            }
        }
    }

    // Show a dialog explaining why permissions are needed and request them again
    private void showPermissionRationaleDialog(final String[] permissions) {
        boolean showRationale = false;
        // Check if we should show a rationale for each permission
        for (String permission : permissions) {
            showRationale = showRationale || ActivityCompat.shouldShowRequestPermissionRationale(this, permission);
        }
        // If we should show a rationale
        if (showRationale) {
            new android.app.AlertDialog.Builder(Registerr.this)
                    .setTitle("Permissions Required")
                    .setMessage("This app needs camera and storage permissions to function properly. Please grant the necessary permissions.")
                    .setPositiveButton("OK", (dialog, which) -> {
                        // Request permissions again
                        ActivityCompat.requestPermissions(Registerr.this, permissions, PERMISSION_REQUEST_CODE);
                    })
                    .setNegativeButton("Cancel", (dialog, which) -> {
                        // Inform the user that permissions are necessary
                        Toast.makeText(Registerr.this, "Permission denied. Please enable them in app settings.", Toast.LENGTH_SHORT).show();
                    })
                    .create()
                    .show();
        } else {
            // User has permanently denied permission, direct them to app settings
            new android.app.AlertDialog.Builder(Registerr.this)
                    .setTitle("Permissions Needed")
                    .setMessage("Camera and storage permissions are required. Please enable them in the app settings.")
                    .setPositiveButton("Open Settings", (dialog, which) -> {
                        // Open the app settings screen to allow the user to enable permissions
                        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                        Uri uri = Uri.fromParts("package", getPackageName(), null);
                        intent.setData(uri);
                        startActivity(intent);
                    })
                    .setNegativeButton("Cancel", (dialog, which) -> {
                        Toast.makeText(Registerr.this, "Permission denied. Some features may not work.", Toast.LENGTH_SHORT).show();
                    })
                    .create()
                    .show();
            // Inform the user that they need to manually enable permissions in settings
            //Toast.makeText(Registerr.this, "Permissions denied. Please enable them in app settings.", Toast.LENGTH_SHORT).show();
        }
    }

    // Handle the result from the image picker
    @Override
    protected void onActivityResult (int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode,resultCode,data);
        // Check if the result is from the image picker
        if (requestCode == IMAGE_PICKER_REQUEST_CODE) {
            if (data != null) {
                // Get the URI of the selected image
                uri = data.getData();
                if (uri != null) {
                    // Set the selected image URI to the ImageView
                    petImage.setImageURI(uri);
                } else {
                    // Handle the case where getData() returns null
                    Log.e("onActivityResult", "Data URI is null");
                }
            } else {
                // Handle the case where the Intent data is null
                Log.e("onActivityResult", "Intent data is null");
            }
        }
    }


    private void uploadImage() {
        if (uri != null) {
            // Create a reference to 'Pet Image/{userId}/{arduinoID}/{fileName}'
            StorageReference fileRef = storageReference.child("Pet Image/" + userId + "/" + arduinoID + "/" + "/profile.jpg");

            fileRef.putFile(uri)
                    .addOnSuccessListener(taskSnapshot -> {
                        // Image uploaded successfully, get its download URL
                        fileRef.getDownloadUrl().addOnSuccessListener(uri -> {
                            petImagee = uri.toString(); // Store the download URL
                            // Now proceed to store pet information in Firebase Realtime Database
                        }).addOnFailureListener(e -> {
                            Toast.makeText(getApplicationContext(), "Failed to upload image: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        });
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(getApplicationContext(), "Failed to upload image: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        } else {
            Toast.makeText(this, "Please select an image", Toast.LENGTH_SHORT).show();
        }
    }

    public void storePetInformation () {
        // change this from uri to string to upload the file name
        petImagee = uri.toString();
        // on user id get then insert the user id with specific arduino id

        DatabaseReference databaseReference1 = firebaseDatabase.getReference("DeviceID")
                .child(arduinoID);

        databaseReference1.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(Registerr.this);
                    LayoutInflater inflater = getLayoutInflater();
                    View layout = inflater.inflate(R.layout.custom_already_exist_arduino_id,(ViewGroup)findViewById(R.id.custom_already_exist_arduino_idd));

                    Button btn_close = (Button) layout.findViewById(R.id.btn_Close);

                    builder.setView(layout);

                    AlertDialog dialog = builder.create();
                    dialog.setCancelable(false);
                    dialog.show();

                    btn_close.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            show.dismiss();
                            dialog.dismiss();
                        }
                    });
                }else {

                    DatabaseReference databaseReference2 = firebaseDatabase.getReference("HistoryOfPet")
                            .child(arduinoID);

                    databaseReference2.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if (snapshot.exists()) {
                                databaseReference2.removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void unused) {

                                    }
                                });
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });

                    DatabaseReference databaseReference3 = firebaseDatabase.getReference("Pet Information")
                            .child(userId)
                            .child(arduinoID);

                    databaseReference3.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if (snapshot.exists()){
                                databaseReference3.removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void unused) {

                                    }
                                });
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });

                    databaseReference = firebaseDatabase.getReference("Pet Information").child(userId);

                    String stat = "Unconfigured";
                    String battery = "101";
                    String petBat = "notc";
                    petInformationStore petInformationStore = new petInformationStore(arduinoID,petName,petCategory,petDataPlan,petDataDate,petDataMonthorDay,petDayORMONTH,petDays ,petDataTime,expired ,expDate,Email, battery ,stat, petBat);
                    PetInformationAndLongitudeLatitude petInformationAndLongitudeLatitude = new PetInformationAndLongitudeLatitude(arduinoID,petName,stat);
                    databaseReference.child(arduinoID).setValue(petInformationStore).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void unused) {

                            uploadImage();
                            // Get a reference to the "AndroidDeviceID" node and specify the child with the Arduino ID
                            DatabaseReference forAndroidDeviceId = firebaseDatabase.getReference("ArduinoDeviceId")
                                    .child(arduinoID);

                            // Set the "Pet name" value under the specific Arduino ID
                            forAndroidDeviceId.setValue(petInformationAndLongitudeLatitude).addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void unused) {
                                    DatabaseReference databaseReference2 = firebaseDatabase.getReference("DeviceID")
                                            .child(arduinoID);
                                    databaseReference2.setValue(petInformationAndLongitudeLatitude).addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void unused) {
                                            sentNotification ();
                                            show.dismiss();
                                            Intent i = new Intent(getApplicationContext(),Sucessfull_register_pet.class);
                                            i.putExtra("previousFragment","1");
                                            startActivity(i);
                                        }
                                    });
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Toast.makeText(Registerr.this,e.getMessage(),Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(getApplicationContext(),e.getMessage(),Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void sentNotification () {
        DatabaseReference databaseReference1 = firebaseDatabase.getReference("User Notification")
                .child(userId);

        //get current date
        Date currentDate = new Date();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("MMM dd 'at' hh:mm a");
        String formattedDate = simpleDateFormat.format(currentDate);
        long timeStamp = System.currentTimeMillis();

        // Create a new notification ID
        String notificationID = databaseReference1.push().getKey();

        Map<String,Object> insertNotification = new HashMap<>();
        insertNotification.put("notification",expiredd);
        insertNotification.put("date", formattedDate);
        insertNotification.put("timestamp", timeStamp);

        databaseReference1.child(notificationID).setValue(insertNotification).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {

            }
        });
    }

    private void checkingInternet () {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("android.net.conn.CONNECTIVITY_CHANGE");
        registerReceiver(new ConnectionReceiver(), intentFilter);
        ConnectivityManager connectivityManager = (ConnectivityManager) getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        // initialize network info
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        // get connection status
        boolean isConnected = networkInfo != null && networkInfo.isConnectedOrConnecting();
        // display the message if no internet
        showResultsInternet (isConnected);
    }
    private void showResultsInternet (boolean isConnected) {
            if (isConnected) {
                showInternet.dismiss();
                isInternetDialogVisible = true;
            }else {
                isInternetDialogVisible = false;
            }
    }

    @Override
    public void onNetworkChange (boolean isConnected) {
      //  showResultsInternet(isConnected);
    }
    @Override
    protected void onResume () {
        super.onResume();
     //   checkingInternet();
    }
    @Override
    protected void onPause () {
        super.onPause();
      //  checkingInternet();
    }

    @Override
    public void onBackPressed() {
        if (previousFragment != null && previousFragment.equals("0")) {
            // Navigate back to the Map fragment
            Intent i = new Intent(Registerr.this,Home.class);
            i.putExtra("back_to_home_register",previousFragment);
            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP); // Just keep this flag
            startActivity(i);
            finish();
        }
        else if (previousFragment != null && previousFragment.equals("1")) {
            // Navigate back to the List fragment
            // Navigate back to the Map fragment

            Intent i = new Intent(Registerr.this,Home.class);
            i.putExtra("back_to_home_register",previousFragment);
            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP); // Just keep this flag
            startActivity(i);
            finish();
        }
        else if (previousFragment != null && previousFragment.equals("2")) {
            // Navigate back to the History fragment
            // Navigate back to the Map fragment
            Intent i = new Intent(Registerr.this,Home.class);
            i.putExtra("back_to_home_register",previousFragment);
            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP); // Just keep this flag
            startActivity(i);
            finish();
        }
        else if (previousFragment != null && previousFragment.equals("3")) {
            // Navigate back to the Notification fragment
            // Navigate back to the Map fragment

            Intent i = new Intent(Registerr.this,Home.class);
            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP); // Just keep this flag
            i.putExtra("back_to_home_register",previousFragment);
            startActivity(i);
            finish();
        }
        else if (previousFragment != null && previousFragment.equals("4")) {
            // Navigate back to the Settings fragment
            // Navigate back to the Map fragment

            Intent i = new Intent(Registerr.this,Home.class);
            i.putExtra("back_to_home_register",previousFragment);
            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP); // Just keep this flag
            startActivity(i);
            finish();
        }else {
            Toast.makeText(getApplicationContext(),"Error",Toast.LENGTH_LONG).show();
        }

    }

}