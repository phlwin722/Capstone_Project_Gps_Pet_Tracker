package com.example.gps_pet_tracker;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Home extends AppCompatActivity {

    private FirebaseAuth firebaseAuth;
    FirebaseDatabase firebaseDatabase;
    FirebaseFirestore firebaseFirestore;
    FirebaseUser currentUser;
    String userId;
    boolean isServiceRunning = false;
    Button btn_ok;
    private static final int PERMISSION_REQUEST_CODE = 1;
    private TabLayout tabLayout;
    AlertDialog alertDialog1;
    public boolean isPetMissingNotificationShown = false;
    private boolean logout = false;
    AlertDialog alertDialogPet, alertDialogLogged;
    private long time = 0;
    StringBuilder messageBuilder = new StringBuilder();
    ArrayList<String> ListID = new ArrayList<>();
    String arduinoID = "";
    String myInformationDelete = "";
    boolean alertISClose = true;
    long backPressed;
    int petDistance;

    // Defining constants for SharedPreferences and the key to store the boolean value
    private static final String PREFS_NAME = "MyPrefsFile"; // Name of the SharedPreferences file
    private static final String LAST_BOOLEAN_KEY = "last_boolean"; // Key for storing the boolean value
    private boolean lastBooleanValue = true; // Initialize the boolean value, for example, true initially


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home); // Set the content view to the activity_home layout

        isServiceRunning = true;

        firebaseAuth = FirebaseAuth.getInstance(); // Initialize FirebaseAuth instance
        firebaseDatabase = FirebaseDatabase.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();
        currentUser = firebaseAuth.getCurrentUser();
        userId = currentUser.getUid(); // Safe to call now

        // Retrieving the last saved boolean value from SharedPreferences when the activity is created
        SharedPreferences preferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE); // Open SharedPreferences in private mode
        lastBooleanValue = preferences.getBoolean(LAST_BOOLEAN_KEY, true); // Load the saved boolean value, default is true if not found

        Log.d("LastBoolean", "OnCreate called, loaded last boolean value: " + lastBooleanValue); // Log the loaded boolean value

        AlertDialog.Builder showLogged = new AlertDialog.Builder(Home.this);
        showLogged.setCancelable(false);
        LayoutInflater layoutInflater= getLayoutInflater();
        View view = layoutInflater.inflate(R.layout.custom_attention,null);
        showLogged.setView(view);
        alertDialogLogged = showLogged.create();

        btn_ok = view.findViewById(R.id.btn_okk);

        DatabaseReference databaseReferences = firebaseDatabase.getReference("User SafeLocation")
                        .child(userId);

        databaseReferences.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    petDistance = Integer.parseInt(snapshot.child("radius").getValue(String.class));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        btn_ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                logout = true;
                Intent service = new Intent(Home.this, myForeGroundService.class);
                stopService(service);
                firebaseAuth.signOut();

                alertDialogLogged.dismiss();
                Intent i = new Intent(Home.this, login.class); // Create an Intent for Login activity
                startActivity(i); // Start the Login activity
                finish(); // Finish the current activity
            }
        });

        if (currentUser == null) {
            // User is not logged in, handle this case
            Intent intent = new Intent(Home.this, login.class); // Redirect to login
            startActivity(intent);
            finish(); // Close current activity
            return; // Exit early
        }else{
            DatabaseReference databaseReference = firebaseDatabase.getReference("User Information")
                    .child(userId);

            databaseReference.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        String signed = snapshot.child("signed").getValue(String.class);
                        String devicename = snapshot.child("DeviceName").getValue(String.class);

                        String deviceNAme = Build.MODEL;

                        if (signed != null) {
                            if (!logout) {
                                if (signed.equals("signed")) {
                                    if (devicename.equals(deviceNAme)) {
                                        //  check ();
                                        checkPetDistanceAlertDialog();
                                        checkDataBalance();
                                        requestedPermission();
                                        askyourinsertload ();
                                    }else {
                                        // Ensure that the activity is in a valid state
                                        if (isFinishing() || isDestroyed()) {
                                            return; // Avoid showing the dialog if the activity is not active
                                        }

                                        // // If the activity is valid, show the dialog
                                        runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                // Show your dialog here
                                                alertDialogLogged.show();
                                            }
                                        });
                                    }

                                }else {
                                        // Ensure that the activity is in a valid state
                                        if (isFinishing() || isDestroyed()) {
                                            return; // Avoid showing the dialog if the activity is not active
                                        }

                                        // // If the activity is valid, show the dialog
                                        runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                // Show your dialog here
                                                alertDialogLogged.show();
                                            }
                                        });
                                }
                            }
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }

        tabLayout = findViewById(R.id.tablayout); // Find the TabLayout in the layout

        // Check if the TabLayout was found
        if (tabLayout == null) {
            Log.e("HomeActivity", "TabLayout is not initialized."); // Log an error if TabLayout is not found
            return;
        }

        Intent ii = getIntent(); // Get the Intent that started this activity
        handleIntentExtras(ii); // Handle the data passed via the Intent

        setupTabIcons(); // Set up the tab icons and their sizes
        setupTabLayoutListener(); // Set up listener for tab selection events
    }

    private void requestedPermission () {
        Handler handlerr = new Handler();
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                if (Build.VERSION.SDK_INT >= 34) {
                    if (ActivityCompat.checkSelfPermission(Home.this,Manifest.permission.FOREGROUND_SERVICE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                            ActivityCompat.checkSelfPermission(Home.this,Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                            ActivityCompat.checkSelfPermission(Home.this,Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                            ActivityCompat.checkSelfPermission(Home.this,Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
                        //Intent serviceIntent = new Intent(Home.this, myForeGroundService.class);
                       // ContextCompat.startForegroundService(Home.this, serviceIntent);
                        handlerr.removeCallbacks(this);
                        if (userId != null) {
                            if (isServiceRunning) {
                                startForeService();
                                isServiceRunning = false;
                            }
                        }
                    }
                }else {
                    if (ActivityCompat.checkSelfPermission(Home.this,Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                            ActivityCompat.checkSelfPermission(Home.this,Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                            ActivityCompat.checkSelfPermission(Home.this,Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
                        //Intent serviceIntent = new Intent(Home.this, myForeGroundService.class);
                        // ContextCompat.startForegroundService(Home.this, serviceIntent);
                        handlerr.removeCallbacks(this);
                       // Toast.makeText(getApplicationContext(),"androud12",Toast.LENGTH_SHORT).show();
                        if (userId != null) {
                            if (isServiceRunning) {
                                startForeService();
                                isServiceRunning = false;
                            }
                        }
                    }
                }
                    handlerr.postDelayed(this,5000);

            }
        };
        handlerr.post(runnable);
    }

    private void check () {
            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    String [] permissions = null;
                    // check if android 14 or below api
                    if (Build.VERSION.SDK_INT >= 34) {
                        permissions = new String[] {
                                Manifest.permission.FOREGROUND_SERVICE_LOCATION,
                                Manifest.permission.ACCESS_COARSE_LOCATION,
                                Manifest.permission.ACCESS_COARSE_LOCATION,
                                Manifest.permission.CAMERA,
                                Manifest.permission.POST_NOTIFICATIONS,
                                Manifest.permission.READ_MEDIA_IMAGES
                        };
                    } else if (Build.VERSION.SDK_INT == 33) {
                        permissions = new String[] {
                                Manifest.permission.ACCESS_COARSE_LOCATION,
                                Manifest.permission.ACCESS_COARSE_LOCATION,
                                Manifest.permission.POST_NOTIFICATIONS,
                                Manifest.permission.CAMERA,
                                Manifest.permission.READ_MEDIA_IMAGES
                        };
                    }
                    else{
                        permissions = new String[]{
                                Manifest.permission.ACCESS_FINE_LOCATION,
                                Manifest.permission.ACCESS_COARSE_LOCATION,
                                Manifest.permission.READ_EXTERNAL_STORAGE,
                                Manifest.permission.CAMERA
                        };
                    }

                    // Directly request permissions
                    ActivityCompat.requestPermissions(Home.this, permissions, PERMISSION_REQUEST_CODE);
                }
            },3000);


     /*   Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                DatabaseReference databaseReference = firebaseDatabase.getReference("User Information")
                        .child(userId);

                databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            String permission = snapshot.child("permission").getValue(String.class);

                            if (!permission.equals("not")) {
                                String [] permissions = null;
                                // check if android 14 or below api
                                if (Build.VERSION.SDK_INT == 34) {
                                    permissions = new String[] {
                                            Manifest.permission.ACCESS_COARSE_LOCATION,
                                            Manifest.permission.ACCESS_COARSE_LOCATION,
                                            Manifest.permission.CAMERA,
                                            Manifest.permission.READ_MEDIA_IMAGES,
                                            Manifest.permission.FOREGROUND_SERVICE_LOCATION
                                    };
                                } else if (Build.VERSION.SDK_INT == 33) {
                                    permissions = new String[] {
                                            Manifest.permission.ACCESS_FINE_LOCATION,
                                            Manifest.permission.ACCESS_COARSE_LOCATION,
                                            Manifest.permission.CAMERA,
                                            Manifest.permission.READ_MEDIA_IMAGES
                                    };
                                } else{
                                    permissions = new String[]{
                                            android.Manifest.permission.ACCESS_FINE_LOCATION,
                                            android.Manifest.permission.ACCESS_COARSE_LOCATION,
                                            android.Manifest.permission.READ_EXTERNAL_STORAGE,
                                            Manifest.permission.CAMERA
                                    };
                                }

                                // Directly request permissions
                                ActivityCompat.requestPermissions(Home.this, permissions, PERMISSION_REQUEST_CODE);

                                Map<String,Object> update = new HashMap<>();
                                update.put("permission","requested");

                                databaseReference.updateChildren(update).addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void unused) {
                                        Log.d("FirebaseUpdate", "Permission status updated successfully.");
                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Log.e("FirebaseUpdate", "Failed to update permission status: " + e.getMessage());
                                    }
                                });
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
            }
        },2000);*/
    }

    // Handle the data passed via the Intent
    private void handleIntentExtras(Intent intent) {
        // Retrieve optional data from the Intent
        myInformationDelete = intent.getStringExtra("myInformationDelete");
        String PasswordUpdate = intent.getStringExtra("PasswordUpdate");
        String petInformationDetails = intent.getStringExtra("select_list_fragment");
        String changePassword = intent.getStringExtra("change_password");
        String myInformation = intent.getStringExtra("My_information");
        String backToHomeRegister = intent.getStringExtra("back_to_home_register");
        String fromRegister = intent.getStringExtra("previousFragment");
        String SafeZone = intent.getStringExtra("SafeZone");
        String SafeZonee = intent.getStringExtra("SafeZonee");
        String selectMapUserNotEmpty = intent.getStringExtra("selectMapUserNotEmpty");
        String activityPendingIntentMyForeGround = intent.getStringExtra("HistoryOfPet");
        String activityPendingIntentmyForeGroundListFragment = intent.getStringExtra("myForeGroundListFragment");
        String maproute = intent.getStringExtra("maproute");

        // Handle each case based on the Intent data
        if (petInformationDetails != null) {
            selectTab(1, new ListFragment()); // Select tab 1 and show ListFragment
        } else if (PasswordUpdate != null && !PasswordUpdate.isEmpty()) {
            signOut();
        }else if (myInformationDelete != null && !myInformationDelete.isEmpty()) {
            signOut();
        }else if (changePassword != null) {
            selectTab(4, new Settings()); // Select tab 4 and show Settings fragment
        } else if (myInformation != null) {
            selectTab(4, new Settings()); // Select tab 4 and show Settings fragment
        } else if (backToHomeRegister != null) {
            int position = Integer.parseInt(backToHomeRegister); // Parse position from Intent data
            if (position >= 0 && position < tabLayout.getTabCount()) {
                selectTab(position, getFragmentForPosition(position)); // Select the tab and show the corresponding fragment
            }
        } else if (selectMapUserNotEmpty != null && selectMapUserNotEmpty.equals("0")) {
            selectTab(0, new MapFragment()); // Select tab 0 and show MapFragment
        } else if (fromRegister != null) {
            selectTab(1, new ListFragment()); // Select tab 1 and show ListFragment
        } else if (activityPendingIntentmyForeGroundListFragment != null) {
            if(activityPendingIntentmyForeGroundListFragment.equals("showPetDataNotification")) {
                // Stop the alert in the foreground service
               /* myForeGroundService serviceInstance = myForeGroundService.getInstance();
                if (serviceInstance != null) {
                    serviceInstance.stopAlert();
                }*/
            }
            selectTab(1,new ListFragment());
        }else if (activityPendingIntentMyForeGround != null) {
            selectTab(1, new ListFragment());

            // Stop the alert in the foreground service
            myForeGroundService serviceInstance = myForeGroundService.getInstance();
            if (serviceInstance != null) {
                serviceInstance.stopAlert();
            }
            /*DatabaseReference databaseReference = firebaseDatabase.getReference("Pet Information")
                            .child(userId)
                            .child(activityPendingIntentMyForeGround);

            databaseReference.child("userAware").setValue("I aware").addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {

                }
            });*/
        }else if (SafeZone != null && !SafeZone.isEmpty()) {
            selectTab(4,new Settings());
        }else if (SafeZonee != null && !SafeZonee.isEmpty()) {
            selectTab(0, new MapFragment());
        }
        else if (maproute != null && !maproute.isEmpty()) {
            selectTab(2,new History());
        }
    }

    // Return the corresponding fragment based on the tab position
    private Fragment getFragmentForPosition(int position) {
        switch (position) {
            case 0: return new MapFragment(); // Fragment for tab 0
            case 1: return new ListFragment(); // Fragment for tab 1
            case 2: return new History(); // Fragment for tab 2
            case 3: return new Notification(); // Fragment for tab 3
            case 4: return new Settings(); // Fragment for tab 4
            default: return new Fragment(); // Default or empty fragment
        }
    }

    // Select a tab and replace the fragment based on the given position
    private void selectTab(int position, Fragment fragment) {
        TabLayout.Tab tab = tabLayout.getTabAt(position); // Get the tab at the specified position
        if (tab != null) {
            changeIconColor(); // Change the color of the icon for the currently selected tab
            tab.select(); // Select the tab programmatically
            if (tab.getIcon() != null) {
                tab.getIcon().setColorFilter(Color.rgb(38, 166, 254), PorterDuff.Mode.SRC_IN); // Set icon color
            }
            replaceFragment(fragment); // Replace the fragment in the container view
        }
    }

    // Set up the icons and their sizes for all tabs
    private void setupTabIcons() {
        if (tabLayout == null) return; // Return if TabLayout is null

        // Define a ColorStateList for the selected state (from resources)
        ColorStateList selectedIconColor = ContextCompat.getColorStateList(this, R.color.buttn);
        if (selectedIconColor != null) {
            for (int i = 0; i < tabLayout.getTabCount(); i++) {
                TabLayout.Tab tab = tabLayout.getTabAt(i); // Get each tab
                if (tab != null && tab.getIcon() != null) {
                    Drawable icon = tab.getIcon(); // Get the icon drawable
                    icon.setBounds(0, 0, dpToPx(50), dpToPx(50)); // Set the icon size
                    tab.setIcon(icon); // Update the tab icon
                }
            }
        }
    }

    // Convert dp to pixels
    private int dpToPx(int dp) {
        final float scale = getResources().getDisplayMetrics().density; // Get screen density
        return (int) (dp * scale + 0.5f); // Convert dp to pixels
    }

    // Set up the listener for tab selection events
    private void setupTabLayoutListener() {
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                if (tab != null && tab.getIcon() != null) {
                    tab.getIcon().setColorFilter(Color.rgb(38, 166, 254), PorterDuff.Mode.SRC_IN);
                    replaceFragment(getFragmentForPosition(tab.getPosition()));
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
                if (tab != null && tab.getIcon() != null) {
                    // Change tab icon color when unselected
                    tab.getIcon().setColorFilter(Color.parseColor("#000000"), PorterDuff.Mode.SRC_IN); // Set color for unselected tab
                }
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
                // Handle tab reselection if needed (currently does nothing)
            }
        });
    }

    // Change the icon color of the first tab to black
    private void changeIconColor() {
        TabLayout.Tab tab = tabLayout.getTabAt(0); // Get the first tab
        if (tab != null && tab.getIcon() != null) {
            tab.getIcon().setColorFilter(Color.parseColor("#000000"), PorterDuff.Mode.SRC_IN); // Set icon color to black
        }
    }

    // Replace the fragment in the container view
    private void replaceFragment(Fragment fragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.replace(R.id.fragmentContainerView2, fragment); // Replace current fragment
        transaction.setReorderingAllowed(true);
        transaction.addToBackStack(fragment.getClass().getSimpleName()); // Add to back stack
        transaction.commit(); // Commit the transaction
    }


        // Show the TabLayout view
    public void showTabLayout() {
        if (tabLayout != null) {
            tabLayout.setVisibility(View.VISIBLE); // Set visibility to visible
        }
    }

    // Hide the TabLayout view
    public void hideTabLayout() {
        if (tabLayout != null) {
            tabLayout.setVisibility(View.GONE); // Set visibility to gone
        }
    }

    public void signOut() {
        // Stop alert sound and vibration after user acknowledgment.
        myForeGroundService serviceInstance = myForeGroundService.getInstance();
        if (serviceInstance != null) {
            serviceInstance.stopAlert();
        }
        Intent serviceIntent = new Intent(this, myForeGroundService.class);
        stopService(serviceIntent);  // Call stopService to stop the service and its notification
        isServiceRunning = false;
        Intent i = new Intent(Home.this, login.class);
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP); // Just keep this flag
        startActivity(i);
        finish(); // Finish the current activity

        logout = true;
        alertISClose = false;

        // Update user status in the database
        DatabaseReference databaseReference = firebaseDatabase.getReference("User Information").child(userId);
        String signed = "Logout";
        Map<String, Object> editInformation = new HashMap<>();
        editInformation.put("signed", signed);

        // Update the database and handle success or failure
        databaseReference.updateChildren(editInformation).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                // On success, start the login activity
                // Sign out from Firebase Auth
                firebaseAuth.signOut();
                userId = "";

                if (myInformationDelete != null && !myInformationDelete.isEmpty()) {
                    // get time now
                    Date date = new Date();
                    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yy/MM/dd");
                    String formattedDate = simpleDateFormat.format(date);

                    // get time now
                    LocalTime localTime = LocalTime.now();
                    DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm");
                    String formattedTime = localTime.format(dateTimeFormatter);

                    DatabaseReference databaseReferences = firebaseDatabase.getReference("User Information").child(myInformationDelete);

                    databaseReferences.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if (snapshot.exists()) {
                                String DeviceName = snapshot.child("DeviceName").getValue(String.class);
                                String birthday = snapshot.child("birthday").getValue(String.class);
                                String email = snapshot.child("email").getValue(String.class);
                                String fullname = snapshot.child("fullname").getValue(String.class);
                                String latitude = snapshot.child("latitude").getValue(String.class);
                                String longitude = snapshot.child("longitude").getValue(String.class);
                                String password = snapshot.child("password").getValue(String.class);
                                String permission = snapshot.child("permission").getValue(String.class);
                                String signed = snapshot.child("signed").getValue(String.class);
                                String userIDd = snapshot.child("userID").getValue(String.class);

                                // insert on the archieve

                                Map<String, Object> insertArchive = new HashMap<>();
                                insertArchive.put("DeviceName",DeviceName);
                                insertArchive.put("birthday",birthday);
                                insertArchive.put("email",email);
                                insertArchive.put("fullname",fullname);
                                insertArchive.put("latitude",latitude);
                                insertArchive.put("longitude",longitude);
                                insertArchive.put("password",password);
                                insertArchive.put("permission",permission);
                                insertArchive.put("signed",signed);
                                insertArchive.put("userID",userIDd);
                                insertArchive.put("timeDelete",formattedTime);
                                insertArchive.put("dateDelete",formattedDate);

                                DatabaseReference databaseReference1 = firebaseDatabase.getReference("User Archieve")
                                        .child(myInformationDelete);

                                databaseReference1.updateChildren(insertArchive).addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void unused) {

                                        //delete the user info on firebase
                                        databaseReferences.removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if (task.isSuccessful()) {

                                                }
                                            }
                                        });
                                    }
                                });
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
                }
            } else {
                // Handle failure (e.g., show a Toast or log the error)
                Log.e("SignOut", "Failed to update user status", task.getException());
                Toast.makeText(Home.this, "Failed to sign out. Please try again.", Toast.LENGTH_SHORT).show();
            }
        });
    }


    private void startForeService () {
        Intent serviceIntent = new Intent(this,myForeGroundService.class);
            startForegroundService(serviceIntent);
    }

    private void checkPetDistanceAlertDialog() {
        // Get a reference to the "Pet Information" in the Firebase database for the current user.
        DatabaseReference databaseReference = firebaseDatabase.getReference("Pet Information")
                .child(userId);

        // Add a listener to read the data once from the database.
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                // Check if there is data in the snapshot.
                if (snapshot.exists()) {
                    List<DataSnapshot> dataSnapshotList = new ArrayList<>();

                    // Loop through each pet's data in the snapshot.
                    for (DataSnapshot snapshot1 : snapshot.getChildren()) {
                        dataSnapshotList.add(snapshot1); // Add each pet's data to a list.
                    }

                    // Start checking the distance for the first pet in the list.
                    checkPetDistance(dataSnapshotList, 0);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // This method is called if there is an error reading the data.
                // Handle the error appropriately (e.g., log it).
            }
        });
    }


    private void checkPetDistance(List<DataSnapshot> pets, int index) {
        // If the index is greater than or equal to the number of pets, all pets have been checked.
        if (index >= pets.size()) {
            checkPetDistanceAlertDialog(); // Restart the alert checking process.
            return; // Exit this method.
        }


        // Get the data for the current pet being checked.
        DataSnapshot petSnapshot = pets.get(index);

        // Check if the "Currentdistance" field exists for this pet.
        if (petSnapshot.child("Currentdistance").exists()) {
            // Retrieve the necessary details from the pet's data.
            String arduinoID = petSnapshot.child("arduinoId").getValue(String.class); // Pet's ID.
            String petName = petSnapshot.child("petName").getValue(String.class); // Pet's name.
            double Currentdistance = petSnapshot.child("Currentdistance").getValue(double.class); // Current distance.
            String userAware = petSnapshot.child("userAware").getValue(String.class); // User awareness status.
            String currentDistance = petSnapshot.child("currentDistance").getValue(String.class); // Displayable distance.

            // Check if the current distance exceeds the allowed distance.
            if (Currentdistance > petDistance) {
                // Only show the alert if the user is not aware of the situation.
                if (!userAware.equals("I aware")) {
                    // Create a notification message for the alert.
                    String notification = "Your pet " + petName + " has exceeded the set distance! It is over " + currentDistance + " away from your location.";
                    // Show the alert dialog for this pet.
                    showPetMissingAlertDialog(notification, arduinoID, index, pets);
                    return; // Exit to wait for user awareness.
                }
            }
        }

        // If no alert is shown, proceed to check the next pet.
        checkPetDistance(pets, index + 1);
    }


    public void showPetMissingAlertDialog(String notification, String arduinoId, int index, List<DataSnapshot> pets) {
        // Build the alert dialog for the missing pet.
        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert.setCancelable(false); // Prevent the dialog from being canceled by touching outside.

        // Inflate the custom layout for the alert dialog.
        LayoutInflater layoutInflater = getLayoutInflater();
        View view = layoutInflater.inflate(R.layout.custom_alert_diaglog_alert_distance, null);
        alert.setView(view); // Set the custom view in the alert dialog.
        alertDialogPet = alert.create(); // Create the dialog instance.

        // Show the alert dialog on the main UI thread.
        runOnUiThread(() -> {
            if (!isFinishing()) { // Check if the activity is still valid.
                alertDialogPet.show(); // Show the alert dialog.
                // Start sound and vibration for alert.
                myForeGroundService serviceInstance = myForeGroundService.getInstance();
                if (serviceInstance != null) {
                    serviceInstance.startAlert(); // Start alert sound and vibration.
                }
            }
        });

        // Set the notification message in the dialog.
        TextView alertText = view.findViewById(R.id.alertText);
        alertText.setText(notification);

        // Set up the close button for the alert dialog.
        Button alertClose = view.findViewById(R.id.CloseAlert);
        alertClose.setOnClickListener(v -> {
            alertDialogPet.dismiss(); // Dismiss the alert dialog when the button is clicked.
            // Update the user’s awareness in the database.
            updateUserAwareness(arduinoId, index, pets);
        });
    }


    private void updateUserAwareness(String arduinoId, int index, List<DataSnapshot> pets) {
        String userAware = "I aware"; // Set the user’s awareness status.

        // Get a reference to the specific pet's "userAware" field in the database.
        DatabaseReference databaseReference = firebaseDatabase.getReference("Pet Information")
                .child(userId)
                .child(arduinoId)
                .child("userAware");

        // Update the database with the user’s awareness status.
        databaseReference.setValue(userAware).addOnCompleteListener(task -> {
            // Wait for 5 seconds before checking the next pet.
            new Handler().postDelayed(() -> checkPetDistance(pets, index + 1), 5000);

            // Stop alert sound and vibration after user acknowledgment.
            myForeGroundService serviceInstance = myForeGroundService.getInstance();
            if (serviceInstance != null) {
                serviceInstance.stopAlert();
            }
        });
    }

    private void askyourinsertload () {
        Handler handler = new Handler();
        Runnable runnable = new Runnable() {
            @Override
            public void run() {

                DatabaseReference databaseReference = firebaseDatabase.getReference("Pet Information")
                        .child(userId);

                databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            for  (DataSnapshot snapshot1 : snapshot.getChildren()) {
                                String askuserifpetload = snapshot1.child("askuserifpetload").getValue(String.class);
                                String petName = snapshot1.child("petName").getValue(String.class);
                                String arduinoId = snapshot1.child("arduinoId").getValue(String.class);
                                String timeAsking = snapshot1.child("timeAsking").getValue(String.class);
                                String askme5minutes = snapshot1.child("askme5minutes").getValue(String.class);

                                AlertDialog.Builder alertDialog = new AlertDialog.Builder(Home.this);
                                View view = getLayoutInflater().inflate(R.layout.custom_askuserifpetload,null);
                                alertDialog.setCancelable(false);
                                alertDialog.setView(view);
                                alertDialog1 = alertDialog.create();

                                TextView tv = view.findViewById(R.id.askTv);
                                Button btn_update = view.findViewById(R.id.btn_logout_confirm);
                                Button btn_logout_cancel = view.findViewById(R.id.btn_logout_cancel);
                                tv.setText("Hi, your pet, " + petName + ", has the data plan you availed, but it has already expired. Please make sure that your SIM card is loaded first so you won't encounter any problems.");

                                if (askme5minutes != null && askme5minutes.equals("askme5minutes")) {
                                    if (timeaskingis5minutes(timeAsking)) {
                                        if (askuserifpetload.equals("asking")) {

                                            if (alertDialog1.isShowing()) {
                                                return;
                                            }

                                            if (!isFinishing() && !isDestroyed()) {
                                                alertDialog1.show();
                                            }


                                            btn_update.setOnClickListener(new View.OnClickListener() {
                                                @Override
                                                public void onClick(View view) {
                                                    alertDialog1.dismiss();
                                                    updateBalance (arduinoId, petName);
                                                }
                                            });

                                            btn_logout_cancel.setOnClickListener(new View.OnClickListener() {
                                                @Override
                                                public void onClick(View view) {
                                                    alertDialog1.dismiss();
                                                    // Get the current time
                                                    LocalTime localTime = LocalTime.now();
                                                    // Define the time format (HH:mm)
                                                    DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm");
                                                    String format = localTime.format(dateTimeFormatter);


                                                    // Get the current time


                                                    // Define the time format (HH:mm)

                                                    // Add 5 minutes to the current time
                                                    LocalTime newTime = localTime.plusMinutes(5);

                                                    // Format the new time as a string
                                                    String formattedTime = newTime.format(dateTimeFormatter);

                                                    Map<String, Object> insert = new HashMap<>();

                                                    insert.put("petDataTime",formattedTime);
                                                    insert.put("timeAsking",format);
                                                    insert.put("askme5minutes","askme5minutes");

                                                    DatabaseReference databaseReference1 = firebaseDatabase.getReference("Pet Information")
                                                            .child(userId)
                                                            .child(arduinoId);

                                                    databaseReference1.updateChildren(insert).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                        @Override
                                                        public void onSuccess(Void unused) {

                                                        }
                                                    });
                                                }
                                            });

                                        }
                                    }
                                }else {
                                    if (askuserifpetload != null && askuserifpetload.equals("asking")) {

                                        if (alertDialog1.isShowing()) {
                                            return;
                                        }

                                        if (!isFinishing() && !isDestroyed()) {
                                            alertDialog1.show();
                                        }


                                        btn_update.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View view) {
                                                alertDialog1.dismiss();

                                                updateBalance (arduinoId, petName);

                                            }
                                        });

                                        btn_logout_cancel.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View view) {
                                                alertDialog1.dismiss();
                                                // Get the current time
                                                LocalTime localTime = LocalTime.now();
                                                // Define the time format (HH:mm)
                                                DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm");
                                                String format = localTime.format(dateTimeFormatter);


                                                // Get the current time


                                                // Define the time format (HH:mm)

                                                // Add 5 minutes to the current time
                                                LocalTime newTime = localTime.plusMinutes(5);

                                                // Format the new time as a string
                                                String formattedTime = newTime.format(dateTimeFormatter);

                                                Map<String, Object> insert = new HashMap<>();

                                                insert.put("petDataTime",formattedTime);
                                                insert.put("timeAsking",format);
                                                insert.put("askme5minutes","askme5minutes");

                                                DatabaseReference databaseReference1 = firebaseDatabase.getReference("Pet Information")
                                                        .child(userId)
                                                        .child(arduinoId);

                                                databaseReference1.updateChildren(insert).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                    @Override
                                                    public void onSuccess(Void unused) {

                                                    }
                                                });
                                            }
                                        });

                                    }
                                }
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
                handler.postDelayed(this, 25000);
            }
        };
        handler.post(runnable);
    }

    private boolean timeaskingis5minutes (String timeAsking) {
        // Format the time to HH:mm if necessary (in case it's in H:mm format)
        if (timeAsking != null && timeAsking.length() == 4) {
            timeAsking = "0" + timeAsking;  // Add leading zero if hour is single-digit
        }
        // Get the current time
        LocalTime currentTime = LocalTime.now();

        // Create a DateTimeFormatter object to parse time in HH:mm (24-hour format with two-digit hours)
        DateTimeFormatter timeFormat = DateTimeFormatter.ofPattern("HH:mm");

        // Format the current time to match the pattern HH:mm
        String formattedTime = currentTime.format(timeFormat); // Example: "15:45"

        /* Ensure the lastTimeOnHistory is in the correct format: "HH:mm" (with two digits for the hour)
        if (lastTimeOnHistory != null && lastTimeOnHistory.length() == 5) {
            // If the hour is single digit (like "8:30"), we prepend a "0" to make it "08:30"
            if (lastTimeOnHistory.charAt(0) != '0' && lastTimeOnHistory.charAt(1) != ':') {
                lastTimeOnHistory = "0" + lastTimeOnHistory;
            }
        }*/

        try {
            // Parse the times
            LocalTime startime = LocalTime.parse(timeAsking,timeFormat);
            LocalTime endTime = LocalTime.parse(formattedTime,timeFormat);

            // Calculate the minutes difference, considering the potential for crossing midnight
            long minutesBetween = ChronoUnit.MINUTES.between(startime, endTime);

        /* If the minutes difference is negative, it means the time crossed midnight
        if (minutesBetween < 0) {
            // Add 24 hours to the end time to handle midnight wraparound
            minutesBetween += 24 * 60; // 24 hours in minutes
        }*/

            // Check if the difference is greater than 5 minutes
            return minutesBetween >= 5;
        } catch (DateTimeParseException e) {
            // If there is an error in parsing, log it and return false (or handle error appropriately)
            Log.e("TimeParseError", "Failed to parse time: " + timeAsking, e);
            return false;
        }
    }

    private void updateBalance (String arduinoId, String petName) {
        Intent intent = new Intent(Home.this,updateLoad.class);
        intent.putExtra("arduinoId",arduinoId);
        intent.putExtra("petName",petName);
        intent.putExtra("userID",userId);
        startActivity(intent);
    }

    private void checkDataBalance() {
        DatabaseReference databaseReferencep = firebaseDatabase.getReference("Pet Information")
                .child(userId);

        databaseReferencep.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    for (DataSnapshot snapshot1 : snapshot.getChildren()) {
                        String notification = "";
                        String time = "";
                        String arduinoId = snapshot1.child("arduinoId").getValue(String.class);
                        String petDataDate = snapshot1.child("petDataDate").getValue(String.class);
                        String petDaysStr = snapshot1.child("petDays").getValue(String.class);
                        String petName = snapshot1.child("petName").getValue(String.class);
                        String petDataTime = snapshot1.child("petDataTime").getValue(String.class);
                        String alertnow = snapshot1.child("alert").getValue(String.class);
                        String expDate = snapshot1.child("expDate").getValue(String.class);

                        // Parse petDataDate into LocalDate (assuming format is "yyyy-MM-dd")
                        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
                        LocalDate petDate = LocalDate.parse(petDataDate, formatter);

                        // Get the current date
                        LocalDate currentDate = LocalDate.now();

                        // Calculate the difference in days
                        long daysBetween = ChronoUnit.DAYS.between(petDate, currentDate);

                        // Log the updated petDays for debugging
                        Log.d("daysBetween", "New Pet Days: " + daysBetween);

                        // If petDataDate is not null, we can update petDays
                        if (petDataDate != null && alertnow != null) {
                            // If it's a new day, subtract the number of days and update petDays
                            if (alertnow.equals("alertnow")) {
                                // Retrieve current petDays value (it should be a string)
                                int petDays = Integer.parseInt(petDaysStr);
                                int newPetDays = petDays - (int) daysBetween;

                                // Build the alert dialog for the missing pet.
                                AlertDialog.Builder alert = new AlertDialog.Builder(Home.this);
                                alert.setCancelable(false); // Prevent the dialog from being canceled by touching outside.

                                // Inflate the custom layout for the alert dialog.
                                LayoutInflater layoutInflater = getLayoutInflater();
                                View view = layoutInflater.inflate(R.layout.custom_alert_diaglog_alert_distance, null);
                                alert.setView(view); // Set the custom view in the alert dialog.
                                alertDialogPet = alert.create(); // Create the dialog instance.

                                // Show the dialog and handle the notification message
                                if (newPetDays == 0) {
                                    try {
                                        SimpleDateFormat timeformat = new SimpleDateFormat("HH:mm");
                                        Date parseTime = timeformat.parse(petDataTime);
                                        SimpleDateFormat timeformatt = new SimpleDateFormat("hh:mm a");
                                        time = timeformatt.format(parseTime);
                                    } catch (ParseException e) {
                                        e.printStackTrace();
                                    }

                                    notification = "Hi, Your pet " + petName + ", has the data plan you availed will expired today at " + time + ".";

                                } else if (newPetDays == 3 || newPetDays == 7) {

                                    try {
                                        SimpleDateFormat timeformat = new SimpleDateFormat("HH:mm");
                                        Date parseTime = timeformat.parse(petDataTime);
                                        SimpleDateFormat timeformatt = new SimpleDateFormat("hh:mm a");
                                        time = timeformatt.format(parseTime);
                                    } catch (ParseException e) {
                                        e.printStackTrace();
                                    }

                                    notification = "Hi, Your pet "+ petName +" has "+ newPetDays + " days remaining on the data plan you have availed, which will expire on " + expDate + " at " + time + ".";
                                }

                                // Show the alert dialog on the main UI thread
                                runOnUiThread(() -> {
                                    if (!isFinishing() && alertDialogPet != null && !alertDialogPet.isShowing()) {
                                        alertDialogPet.show(); // Show the alert dialog.
                                        // Start sound and vibration for alert.
                                        myForeGroundService serviceInstance = myForeGroundService.getInstance();
                                        if (serviceInstance != null) {
                                            serviceInstance.startAlert(); // Start alert sound and vibration.
                                        }
                                    }
                                });

                                // Set the notification message in the dialog.
                                TextView alertText = view.findViewById(R.id.alertText);
                                alertText.setText(notification);

                                // Set up the close button for the alert dialog
                                Button alertClose = view.findViewById(R.id.CloseAlert);
                                alertClose.setOnClickListener(v -> {
                                    Log.d("AlertDialog", "Close button clicked.");

                                    // Dismiss the dialog safely on the UI thread
                                    runOnUiThread(() -> {
                                        if (alertDialogPet != null && alertDialogPet.isShowing()) {
                                            Log.d("AlertDialog", "Dismissing the dialog.");
                                            alertDialogPet.dismiss(); // Dismiss the alert dialog
                                        } else {
                                            Log.d("AlertDialog", "Dialog is not showing or is already dismissed.");
                                        }
                                    });

                                    // Perform the database update after dismissal
                                    DatabaseReference databaseReferenceqq = firebaseDatabase.getReference("Pet Information")
                                            .child(userId)
                                            .child(arduinoId);

                                    Map<String, Object> update = new HashMap<>();
                                    update.put("alert", "StopAlert");

                                    databaseReferenceqq.updateChildren(update).addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void unused) {
                                            Log.d("Firebase", "Alert status updated successfully.");
                                        }
                                    });

                                    // Stop alert sound and vibration after user acknowledgment
                                    myForeGroundService serviceInstance = myForeGroundService.getInstance();
                                    if (serviceInstance != null) {
                                        serviceInstance.stopAlert();
                                    }
                                });
                            }
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle any database read errors here
                Log.e("FirebaseError", "Error: " + error.getMessage());
            }
        });
    }


    @Override
    public void onResume () {
        super.onResume(); // Calling the superclass method (Activity's onResume)

        // Retrieving the last saved boolean value from SharedPreferences when the activity resumes
        SharedPreferences preferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE); // Open SharedPreferences in private mode
        lastBooleanValue = preferences.getBoolean(LAST_BOOLEAN_KEY, true); // Load the saved boolean value, default is true if not found

        Log.d("LastBoolean", "OnResume called, last boolean value: " + lastBooleanValue); // Log the loaded boolean value
        if (lastBooleanValue) {
            check(); //check permission
        }
    }

    @Override
    protected void onPause() {
        super.onPause(); // Calling the superclass method (Activity's onPause)

        // Saving the current boolean value to SharedPreferences before the app goes to the background
        SharedPreferences preferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE); // Open SharedPreferences in private mode
        SharedPreferences.Editor editor = preferences.edit(); // Begin editing SharedPreferences
        editor.putBoolean(LAST_BOOLEAN_KEY, lastBooleanValue); // Save the current boolean value to SharedPreferences with the key
        editor.apply(); // Apply the changes asynchronously (save the data)

        Log.d("LastBoolean", "OnPause called, saved boolean value: " + lastBooleanValue); // Log the saved boolean value
    }

    public void onBackPressed(){
        // Get the currently visible fragment
        Fragment currentFragment = getSupportFragmentManager().findFragmentById(R.id.fragmentContainerView2);

        if (currentFragment instanceof MapFragment) {
            // If we're in MapFragment, handle the back press here
           // if (backPressed + 2000 > System.currentTimeMillis()) {
            super.onBackPressed();  // This will pop the fragment off the stack or exit the app if no fragments are left
                finish();  // Close the activity (exit the app)  // Exit the activity
            // Handle other fragments if necessary (typically you don't need this block)
          //  } else {
               // Toast.makeText(this, "Press again to exit", Toast.LENGTH_SHORT).show();
           // }
           // backPressed = System.currentTimeMillis();
        } else if (currentFragment instanceof ListFragment) {
            // If we're in MapFragment, handle the back press here
            selectTab(0, new MapFragment()); // Select tab 0 and show MapFragment
        }

        else if (currentFragment instanceof History) {
            // If we're in MapFragment, handle the back press here
            selectTab(0, new MapFragment()); // Select tab 0 and show MapFragment
        }

        else if (currentFragment instanceof Notification) {
            selectTab(0, new MapFragment()); // Select tab 0 and show MapFragment
        }

        else if (currentFragment instanceof Settings) {
            // If we're in MapFragment, handle the back press here
            selectTab(0, new MapFragment()); // Select tab 0 and show MapFragment
        }
    }
}



