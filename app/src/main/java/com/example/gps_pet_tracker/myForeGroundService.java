package com.example.gps_pet_tracker;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Vibrator;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.mapbox.android.core.location.LocationEngine;
import com.mapbox.android.core.location.LocationEngineCallback;
import com.mapbox.android.core.location.LocationEngineProvider;
import com.mapbox.android.core.location.LocationEngineRequest;
import com.mapbox.android.core.location.LocationEngineResult;

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
import java.util.Locale;
import java.util.Map;

public class myForeGroundService extends Service {
    private static myForeGroundService instance;
    private FirebaseAuth firebaseAuth; // Declares a variable for Firebase authentication.
    String insertDataBalanceIfLoadHasBeen7Days3Days0Days;
    private boolean isInserting = false; // flag to track if insert is already in progress\
    private boolean isInsertingg = false; // flag to track if insert is already in progress
    private FirebaseDatabase firebaseDatabase; // Declares a variable for Firebase Realtime Database.
    private static MediaPlayer mediaPlayer;
    String time = "";
    boolean onDistroy = false;
    private NotificationManager notificationManager;
    private static boolean isAlerting = false;
    private boolean isPetMissingNotificationShown = false;
    private boolean isServiceRunning = false; // Set to false when service is destroyed
    private static final String CHANNEL_ID = "pet_channel_id"; // Notification channel ID
    String timee = "";
    private static final String CHANNEL_IDd = "pet_channel_id"; // Notification channel ID
    private boolean insertedPetDays = false;
    private static final String CHANNEL_ID_BATTERY = "pet_channel_id"; // Notification channel ID
    private Vibrator vibrator;
    String userId = "", arduinoIDd = "";
    private Handler handler;
    public Runnable runnable;
    private long lastNotificationTime = 0; // Variable to track the last notification time
    private long showNotificationTime = 0;
    private boolean isNotificationStarted = false;
    private final IBinder binder = new LocalBinder();

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this; // Assign instance to static reference\

        isServiceRunning = true; // Set to false when service is destroyed
        // Initialize Firebase components.
        firebaseAuth = FirebaseAuth.getInstance(); // Gets the Firebase authentication instance.
        firebaseDatabase = FirebaseDatabase.getInstance(); // Gets the Firebase Realtime Database instance.
        notificationManager = (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);

        FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();

        if (firebaseUser != null) {
            userId = firebaseAuth.getCurrentUser().getUid();
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // Start the foreground service only once
        if (!isNotificationStarted) {
                // Get the user ID from Firebase Authentication.
                if (userId != null) { // Check if user ID is not null.
                    initializeService(userId); // Initialize service with user ID
                    startNotificationService();
                    isNotificationStarted = true; // Ensure it only starts once
                }
        }
        // Change the return value to prevent the service from restarting after it stops
        return START_STICKY; // restart the service if killed
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    private void initializeService(String userId) {
        checkPetDistance(userId); // Check distance between user and pet
        checkPetLocationAndDisplayOnHistoryOfPet(); // Display pet location history
        listenForAndroidDeviceChanges ();
        listenForAndroidDeviceChangess();
        checkBatteryPercentage ();
        checkIfExceedPet ();
        checkDataBalance();
        checkifdateis0();
        checkIf7days3days0days();

      /*  handler = new Handler();
        runnable = new Runnable() {
            @Override
            public void run() {
          //      LocationUserRealTimer (); //start update location of user
                handler.postDelayed(this,10000); // Adjust frequency if needed
            }
        };
        handler.post(runnable);*/
    }

    @SuppressLint("ForegroundServiceType")
    private void startNotificationService() {
        if (isServiceRunning) {
            if (userId != null) {

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    if (ActivityCompat.checkSelfPermission(this,Manifest.permission.FOREGROUND_SERVICE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
                            ActivityCompat.checkSelfPermission(this,Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
                            ActivityCompat.checkSelfPermission(this,Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
                            ActivityCompat.checkSelfPermission(this,Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
                        // Create a notification channel if API level >= 26
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            NotificationChannel notificationChannel = new NotificationChannel(
                                    CHANNEL_ID,
                                    "Pet Tracking Service",
                                    NotificationManager.IMPORTANCE_HIGH
                            );
                            notificationManager.createNotificationChannel(notificationChannel);
                        }

                        // Create an intent that will be triggered when the notification is clicked
                        Intent notificationIntent = new Intent(this, Home.class);
                        notificationIntent.putExtra("selectMapUserNotEmpty", "0");
                        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

                        // Create a pending intent that wraps the intent
                        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

                        // Build the notification
                        Notification notification = new Notification.Builder(this, CHANNEL_ID)
                                .setContentText("Tap to go this app")
                                .setContentTitle("GPS PET TRACKER is running")
                                .setContentIntent(pendingIntent)
                                .setSmallIcon(R.drawable.image_gps_trackerr)
                                .build();
                        // Start the service in the foreground
                        startForeground(1001, notification);
                    }
                }else {
                    // Create a notification channel if API level >= 26
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        NotificationChannel notificationChannel = new NotificationChannel(
                                CHANNEL_ID,
                                "Pet Tracking Service",
                                NotificationManager.IMPORTANCE_HIGH
                        );
                        notificationManager.createNotificationChannel(notificationChannel);
                    }

                    // Create an intent that will be triggered when the notification is clicked
                    Intent notificationIntent = new Intent(this, Home.class);
                    notificationIntent.putExtra("selectMapUserNotEmpty", "0");
                    notificationIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

                    // Create a pending intent that wraps the intent
                    PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

                    // Build the notification
                    Notification notification = new Notification.Builder(this, CHANNEL_ID)
                            .setContentText("Tap to go this app")
                            .setContentTitle("GPS PET TRACKER is running")
                            .setContentIntent(pendingIntent)
                            .setSmallIcon(R.drawable.image_gps_trackerr)
                            .build();
                    // Start the service in the foreground
                    startForeground(1001, notification);
                }
            }
        }
    }


    private void listenForAndroidDeviceChanges() {
        // Reference to the "ArduinoDeviceId" node in Firebase
        DatabaseReference androidDeviceRef = firebaseDatabase.getReference("ArduinoDeviceId");

        // Set a listener to monitor changes in the "ArduinoDeviceId" data
        androidDeviceRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                // Create a list to hold snapshots of each Arduino device
                List<DataSnapshot> petSnapshots = new ArrayList<>();

                // Loop through each child node (each Arduino ID) in the snapshot
                for (DataSnapshot checkPetDistance : snapshot.getChildren()) {
                    // Add the child snapshot to the list
                    petSnapshots.add(checkPetDistance);
                }
                // Process all Arduino devices in a single method call
                processArduinoDevices(petSnapshots);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle any errors that occur during the data retrieval
            }
        });
    }

    private void processArduinoDevices(List<DataSnapshot> pets) {
        // Loop through each pet snapshot received
        for (DataSnapshot snapshot1 : pets) {
            // Check if the necessary fields (latitude, longitude, battery) exist in the snapshot
            if (snapshot1.child("latitude").exists() &&
                    snapshot1.child("longitude").exists() && snapshot1.child("battery").exists()) {
                // Retrieve the Arduino ID, longitude, latitude, battery level, and status from the snapshot
                String arduinoId = snapshot1.child("arduinoId").getValue(String.class);
                String arduinoIdd = snapshot1.child("arduinoIdd").getValue(String.class);
                double longitude = snapshot1.child("longitude").getValue(Double.class);
                double latitude = snapshot1.child("latitude").getValue(Double.class);
                int battery = snapshot1.child("battery").getValue(Integer.class);
                String status = snapshot1.child("status").getValue(String.class);
                String time = snapshot1.child("time").getValue(String.class);
                String date = snapshot1.child("date").getValue(String.class);

                // Ensure latitude and longitude are valid (not equal to 0.0)
                if (longitude != 0.0 && latitude != 0.0) {
                    if (arduinoId != null) {
                        // Update the pet information with the retrieved values
                        updatePetInformation(arduinoId, latitude, longitude, status, String.valueOf(battery), time, date);
                    }else {
                        // Update the pet information with the retrieved values
                        updatePetInformation(arduinoIdd, latitude, longitude, status, String.valueOf(battery), time, date);
                    }

                }
            }
        }
    }

    private void updatePetInformation(String arduinoId, double latitude, double longitude, String status, String battery,String time,String date) {
        // Reference to the specific pet information node in Firebase based on the user ID and Arduino ID
        DatabaseReference petInfoRef = firebaseDatabase.getReference("DeviceID")
                .child(arduinoId);

        // Set a single listener to check if the pet information exists
        petInfoRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                // Check if the pet information snapshot exists
                if (snapshot.exists()) {
                    // Create a map to hold the updated pet information
                    Map<String, Object> petInfo = new HashMap<>();
                    petInfo.put("latitude", latitude);   // Update latitude
                    petInfo.put("longitude", longitude); // Update longitude
                    petInfo.put("battery", battery);     // Update battery level
                    petInfo.put("status", status);       // Update status
                    petInfo.put("time",time);
                    petInfo.put("date",date);

                    // Update the pet information in the Firebase database
                    petInfoRef.updateChildren(petInfo).addOnCompleteListener(updateTask -> {
                        // Check if the update was successful
                        if (updateTask.isSuccessful()) {
                            Log.d("UpdateSuccess", "Pet information updated successfully.");
                        } else {
                            // Log an error if the update failed
                            Log.e("UpdateError", "Failed to update pet information.", updateTask.getException());
                        }
                    });
                }else {
                    DatabaseReference databaseReference1 = firebaseDatabase.getReference("ArduinoDeviceId")
                            .child(arduinoId);

                    databaseReference1.removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void unused) {

                        }
                    });

                    DatabaseReference databaseReference3 = firebaseDatabase.getReference("Pet Information")
                            .child(userId)
                            .child(arduinoId);
                    databaseReference3.removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void unused) {

                        }
                    });

                    DatabaseReference databaseReference2 = firebaseDatabase.getReference("HistoryOfPet")
                            .child(arduinoId);

                    databaseReference2.removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void unused) {

                        }
                    });

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle any errors that occur while trying to retrieve the data
            }
        });
    }

    private void listenForAndroidDeviceChangess() {
        // Reference to the "DeviceID" node in Firebase
        DatabaseReference androidDeviceRef = firebaseDatabase.getReference("DeviceID");

        // Set a listener to monitor changes in the "DeviceID" data
        androidDeviceRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                // Create a list to hold snapshots of each Arduino device
                List<DataSnapshot> petSnapshots = new ArrayList<>();

                // Loop through each child node (each Arduino ID) in the snapshot
                for (DataSnapshot checkPetDistance : snapshot.getChildren()) {
                    // Add the child snapshot to the list
                    petSnapshots.add(checkPetDistance);
                }
                // Process all Arduino devices in a single method call
                processArduinoDevicess(petSnapshots);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle any errors that occur during the data retrieval
            }
        });
    }

    private void processArduinoDevicess(List<DataSnapshot> pets) {
        // Loop through each pet snapshot received
        for (DataSnapshot snapshot1 : pets) {
            // Check if the necessary fields (latitude, longitude, battery) exist in the snapshot
            if (snapshot1.child("latitude").exists() &&
                    snapshot1.child("longitude").exists()) {
                // Retrieve the Arduino ID, longitude, latitude, battery level, and status from the snapshot
                String arduinoId = snapshot1.child("arduinoId").getValue(String.class);
                double longitude = snapshot1.child("longitude").getValue(Double.class);
                double latitude = snapshot1.child("latitude").getValue(Double.class);
                String battery = snapshot1.child("battery").getValue(String.class);
                String status = snapshot1.child("status").getValue(String.class);

                // Ensure latitude and longitude are valid (not equal to 0.0)
                if (longitude != 0.0 && latitude != 0.0) {
                    if (arduinoId != null) {
                        updatePetInformationn(arduinoId,latitude,longitude,status, battery);
                    }
                }
            }
        }
    }

    private void updatePetInformationn(String arduinoId, double latitude, double longitude, String status, String battery) {
        // Reference to the specific pet information node in Firebase based on the user ID and Arduino ID
        DatabaseReference petInfoRef = firebaseDatabase.getReference("Pet Information")
                .child(userId)
                .child(arduinoId);

        // Set a single listener to check if the pet information exists
        petInfoRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                // Check if the pet information snapshot exists
                if (snapshot.exists()) {

                    // Create a map to hold the updated pet information
                    Map<String, Object> petInfo = new HashMap<>();
                    petInfo.put("latitude", latitude);   // Update latitude
                    petInfo.put("longitude", longitude); // Update longitude
                    petInfo.put("battery", battery);     // Update battery level
                    petInfo.put("status", status);       // Update status

                    // Update the pet information in the Firebase database
                    petInfoRef.updateChildren(petInfo).addOnCompleteListener(updateTask -> {
                        // Check if the update was successful
                        if (updateTask.isSuccessful()) {
                            Log.d("UpdateSuccess", "Pet information updated successfully.");
                        } else {
                            // Log an error if the update failed
                            Log.e("UpdateError", "Failed to update pet information.", updateTask.getException());
                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle any errors that occur while trying to retrieve the data
            }
        });
    }

    // Method to check the distance between user and pet.
    private void checkPetDistance(String userId) {
        DatabaseReference databaseReference = firebaseDatabase.getReference("Pet Information")
                .child(userId);
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    for (DataSnapshot snapshot1 : snapshot.getChildren()) {
                        String arduinoId = snapshot1.child("arduinoId").getValue(String.class);
                        if (arduinoId != null) {
                            if (snapshot1.child("longitude").exists() && snapshot1.child("latitude").exists()) {
                                double longitudePet =  snapshot1.child("longitude").getValue(double.class);
                                double latitudePet = snapshot1.child("latitude").getValue(double.class);

                                if (longitudePet != 0.0 && latitudePet != 0.0) {
                                    // Get the data of user
                                    DatabaseReference databaseReference1 = firebaseDatabase.getReference("User SafeLocation")
                                            .child(userId);
                                    databaseReference1.addValueEventListener(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot snapshot2) {
                                            if (snapshot2.exists()) {
                                                String latitudeStr = snapshot2.child("latitude").getValue(String.class);
                                                String longitudeStr = snapshot2.child("longitude").getValue(String.class);
                                                String distanceTextInsert;

                                                if (latitudeStr != null && longitudeStr != null && !latitudeStr.equals("0.0") && !longitudeStr.equals("0.0")) {

                                                    // Calculate distance between user and pet.
                                                    float[] results = new float[1]; // Array to store distance result.
                                                    Location.distanceBetween(Double.parseDouble(latitudeStr), Double.parseDouble(longitudeStr), latitudePet, longitudePet, results);
                                                    float distanceInMeters = results[0]; // Get distance in meters.

                                                    // Format distance text based on distance value.
                                                    if (distanceInMeters < 1000) {
                                                        // If the distance is less than 1000 meters, display it in meters without decimal places
                                                        distanceTextInsert = String.format("%.0f m", distanceInMeters);
                                                    } else {
                                                        // If the distance is 1000 meters or more, convert it to kilometers and display with two decimal places
                                                        float distanceInKm = distanceInMeters / 1000;

                                                        distanceTextInsert = String.format("%.2f km", distanceInKm); // Correctly format to 2 decimal places
                                                    }

                                                    // Update current distance in Firebase Realtime Database.
                                                    DatabaseReference databaseReferences = firebaseDatabase.getReference("Pet Information")
                                                            .child(userId)
                                                            .child(arduinoId);
                                                    databaseReferences.addListenerForSingleValueEvent(new ValueEventListener() {
                                                        @Override
                                                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                            if (snapshot.exists()) {
                                                                Map<String,Object> update = new HashMap<>();
                                                                update.put("currentDistance",distanceTextInsert);
                                                                update.put("Currentdistance", distanceInMeters);
                                                                databaseReferences.updateChildren(update).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                    @Override
                                                                    public void onSuccess(Void unused) {
                                                                        Log.e("update","haha");
                                                                    }
                                                                });
                                                            }
                                                        }

                                                        @Override
                                                        public void onCancelled(@NonNull DatabaseError error) {

                                                        }
                                                    });

                                                }  else {
                                                    handleNoLocation(userId,arduinoId);
                                                }
                                            }
                                            else {
                                                handleNoLocation(userId,arduinoId);
                                            }
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError error) {

                                        }
                                    });
                                } else {
                                    if (arduinoId != null) {
                                        handleNoLocation(userId,arduinoId);
                                    }
                                }

                            }else {
                                if (arduinoId != null) {
                                    handleNoLocation(userId,arduinoId);
                                }
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

    private void handleNoLocation (String userId, String arduinoId) {
        String distanceTextc = "Unconfigured ";
        String userAware = "Not Aware";
        // Get a reference to the database path where the distance will be stored
        DatabaseReference databaseReference = firebaseDatabase.getReference("Pet Information")
                .child(userId)
                .child(arduinoId);

        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    Map<String,Object> update = new HashMap<>();
                    update.put("currentDistance",distanceTextc);
                    update.put("userAware", userAware);

                    databaseReference.updateChildren(update).addOnSuccessListener(new OnSuccessListener<Void>() {
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

    }

    private void checkIfExceedPet () {
        DatabaseReference databaseReference = firebaseDatabase.getReference("Pet Information")
                .child(userId);

        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    List<DataSnapshot> insert = new ArrayList<>();
                    for (DataSnapshot checkPetDistance : snapshot.getChildren()) {
                        insert.add(checkPetDistance);
                    }
                    CheckNow(insert,0);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void CheckNow (List<DataSnapshot> pet,int Index) {
        if (Index >= pet.size()) {
       //     checkIfExceedPet ();
            return;
        }

        DatabaseReference databaseReferencees = firebaseDatabase.getReference("User SafeLocation")
                .child(userId);

        databaseReferencees.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                   int radius = Integer.parseInt(snapshot.child("radius").getValue(String.class));

                    DataSnapshot petSnapshot = pet.get(Index);

                    if (petSnapshot.child("Currentdistance").exists() && petSnapshot.child("currentDistance").exists()) {
                        String petName = petSnapshot.child("petName").getValue(String.class); // Pet's name.
                        String arduinoId = petSnapshot.child("arduinoId").getValue(String.class);
                        double Currentdistance = petSnapshot.child("Currentdistance").getValue(double.class); // Current distance.
                        String userAware = petSnapshot.child("userAware").getValue(String.class); // User awareness status.
                        String currentDistance = petSnapshot.child("currentDistance").getValue(String.class); // Displayable distance.

                        // Check if the current distance exceeds the allowed distance.
                        if (Currentdistance > radius) {
                            // Only show the alert if the user is not aware of the situation.
                            if (!userAware.equals("I aware")) {
                                // Create a notification message for the alert.
                                String notification = "Your pet " + petName + " has exceeded the set distance! It is over " + currentDistance + " away from your location.";
                                showPetMissingNotification (notification,Index,pet,arduinoId);// Show the alert dialog for this pet.
                                insertNotification(notification); // Handle insertion of the notification if needed
                                return; // Exit to wait for user awareness.
                            }
                        }else {
                            DatabaseReference databaseReference = firebaseDatabase.getReference("Pet Information")
                                    .child(userId)
                                    .child(arduinoId);

                            databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    if (snapshot.exists()) {
                                        Map<String,Object> update = new HashMap<>();
                                        update.put("userAware","I not aware");

                                        databaseReference.updateChildren(update).addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void unused) {
                                                // If no alert is shown, proceed to check the next pet.
                                                CheckNow (pet,Index + 1);
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
                    // If no alert is shown, proceed to check the next pet.
                    CheckNow (pet,Index + 1);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    // Show notification with custom action button "I'm aware"
    private void showPetMissingNotification(String message, int Index,List<DataSnapshot> pets,String arduinoId) {
        Log.d("bb", "Creating notification with message: " + message);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(CHANNEL_IDd, "Pet Tracker", NotificationManager.IMPORTANCE_HIGH);
            channel.setDescription("Pet distance alerts");
            notificationManager.createNotificationChannel(channel);
        }

        // Intent to launch Home.class
        Intent activityIntent = new Intent(this, Home.class);
        activityIntent.putExtra("HistoryOfPet", arduinoId);
        activityIntent.setAction("com.example.pettracker.ACTION_ACKNOWLEDGE_ALERT");
        activityIntent.putExtra("listIDArduino", arduinoId);
        activityIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        PendingIntent activityPendingIntent = PendingIntent.getActivity(this, 0, activityIntent, PendingIntent.FLAG_MUTABLE);

        //  Intent intent = new Intent(this, myBroadcastReciever.class);
        //  intent.setAction("com.example.pettracker.ACTION_ACKNOWLEDGE_ALERTt");

        ///     intent.putExtra("listIDArduino", Index);

        //   PendingIntent pendingIntent = PendingIntent.getBroadcast(getApplicationContext(), 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        Notification notification = new NotificationCompat.Builder(getApplicationContext(), CHANNEL_IDd)
                .setContentTitle("Notification Pet Alert")
                .setContentText(message)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(message))
                .setSmallIcon(R.drawable.image_gps_trackerr)
                .setAutoCancel(false) // Ensure this is set to false
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(activityPendingIntent) // Set the activity intent here
                //    .addAction(R.drawable.icons8_notification_50, "I'm aware", pendingIntent) // Add "I'm aware" button
                .build();

        // Create a unique notification ID using the pet's arduinoId (or Index)
        int notificationId = arduinoId.hashCode();

        notificationManager.notify(notificationId, notification); // Use unique notification ID

        Log.d("Notification", "Notification displayed.");
        startAlert();
    }

    private void insertNotification(String notification) {
        if (isInserting) {
            Log.d("InsertNotification", "Already inserting, skipping duplicate.");
            return;  // Skip if a previous insert is still in progress.
        }

        isInserting = true; // Set the flag to true when starting the insert.

        long currentTime = System.currentTimeMillis();

        DatabaseReference addNotifi = firebaseDatabase.getReference("User Notification")
                .child(userId);

        addNotifi.orderByChild("timestamp") // Order by timestamp for accurate last entry
                .limitToLast(1) // Get the last inserted notification based on timestamp
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            // Check if the last inserted notification matches the new one
                            for (DataSnapshot childSnapshot : snapshot.getChildren()) {
                                String existingNotification = childSnapshot.child("notification").getValue(String.class);
                                if (existingNotification != null && existingNotification.equals(notification)) {
                                    Log.d("InsertNotification", "Duplicate notification detected. Not inserting.");
                                    isInserting = false; // Reset the flag
                                    return; // Exit the method early since it's a duplicate
                                }
                            }
                        }

                        // If no duplicate found, insert the new notification
                        String notificationID = addNotifi.push().getKey();
                        SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd 'at' hh:mm a", Locale.getDefault());
                        String formattedDate = dateFormat.format(new Date());

                        // Prepare the data for insertion
                        Map<String, Object> insertNotification = new HashMap<>();
                        insertNotification.put("notification", notification);
                        insertNotification.put("date", formattedDate);
                        insertNotification.put("timestamp", currentTime);

                        // Insert the notification into the database
                        addNotifi.child(notificationID).setValue(insertNotification)
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void unused) {
                                        isInserting = false; // Reset flag after insert
                                    }
                                });
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.e("DatabaseError", "Error querying database: " + error.getMessage());
                        isInserting = false; // Reset flag if cancelled
                    }
                });

    }

    // Reset the flag when the alert is acknowledged
    public void onNotificationAcknowledged() {
        isPetMissingNotificationShown = false;
    }

    //Start alert with sound and notification
    public void startAlert() {
        if (!isAlerting) {
            isAlerting = true;

            // Recreate the MediaPlayer
            mediaPlayer = MediaPlayer.create(getApplicationContext(), R.raw.alert);
            mediaPlayer.start();

            // Initialize the vibrator service
            vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
            if (vibrator != null) {
                long[] pattern = {0, 1000, 500}; // Vibrate for 1 second, pause for 0.5 seconds
                vibrator.vibrate(pattern, 0); // 0 means to repeat the pattern indefinitely
            }

            mediaPlayer.setOnCompletionListener(mp -> {
                if (isAlerting) {
                    mediaPlayer.start();
                }
            });
        }
    }

    public static myForeGroundService getInstance() {
        return instance;
    }

    // Stop alert sound and vibration
    public void stopAlert() {
        isAlerting = false; // Set alerting to false to prevent further alerts
        if (mediaPlayer != null) {
            if (mediaPlayer.isPlaying()) {
                mediaPlayer.stop();
            }
            mediaPlayer.reset(); // Reset the MediaPlayer
            mediaPlayer.release(); // Release resources
            mediaPlayer = null; // Set to null to prepare for a new instance
        }
        // Stop vibration
        if (vibrator != null) {
            vibrator.cancel();
        }
    }


    private void checkPetLocationAndDisplayOnHistoryOfPet() {
        // Reference to the "DeviceID" node in Firebase
        DatabaseReference fetchArduinoID = firebaseDatabase.getReference("DeviceID");

        // Attach a listener to get updates when data changes in the "DeviceID" node
        fetchArduinoID.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                // Iterate over all the children of "DeviceID"
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    if (dataSnapshot.child("longitude").exists() && dataSnapshot.child("latitude").exists()) {
                        double latitudee = dataSnapshot.child("latitude").getValue(double.class);
                        double longitudee = dataSnapshot.child("longitude").getValue(double.class);
                        String ArduinoID = dataSnapshot.child("arduinoId").getValue(String.class);

                        if (latitudee != 0.0 && longitudee != 0.0 && ArduinoID != null) {
                            // Convert snapshot to HistoryDisplayOnFirebase object (which contains ArduinoId, location, etc.)
                            HistoryDisplayOnFirebase historyDisplayOnFirebase = dataSnapshot.getValue(HistoryDisplayOnFirebase.class);

                            if (historyDisplayOnFirebase != null) {
                                if (historyDisplayOnFirebase.getDate() != "00/00/00"){
                                    // Reference to the "HistoryOfPet" node in Firebase under the specific Arduino ID
                                    DatabaseReference historyOfPetRef = firebaseDatabase.getReference("HistoryOfPet")
                                            .child(historyDisplayOnFirebase.getArduinoId());

                                    // Fetch the last recorded entry in "HistoryOfPet" using orderByKey().limitToLast(1)
                                    historyOfPetRef.orderByKey().limitToLast(1).addValueEventListener(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot dataSnapshotHistoryPet) {
                                            if (dataSnapshotHistoryPet.exists()) {
                                                for (DataSnapshot dataSnapshot1 : dataSnapshotHistoryPet.getChildren()) {
                                                    // Default values for last recorded date and time
                                                    String lastTimeOnHistory = dataSnapshot1.child("time").getValue(String.class);
                                                    String lastDateOnHistory = dataSnapshot1.child("datee").getValue(String.class);
                                                    String arduibo = dataSnapshot1.child("arduinoId").getValue(String.class);

                                                    // Compare the date from DeviceID with the last recorded date
                                                    String arduinoDateOnArduinoID = historyDisplayOnFirebase.getDate();  // Get date from DeviceID
                                                    String arduinoTimeOnArduinoID = historyDisplayOnFirebase.getTime();  // Get time from DeviceID

                                                    if (lastDateOnHistory != null && arduinoDateOnArduinoID != null && lastTimeOnHistory != null & arduinoTimeOnArduinoID != null) {
                                                        if (arduibo.equals(ArduinoID)) {
                                                            // Check if the last recorded date matches the current date from Arduino
                                                            if (lastDateOnHistory.equals(arduinoDateOnArduinoID)) {
                                                                // If the time doesn't match, check if the time is over 15 minutes
                                                                if (isTimeOver15Minutes(lastTimeOnHistory)) {
                                                                    // If the date is the same, check the time
                                                                    if (lastTimeOnHistory.equals(arduinoTimeOnArduinoID)) {
                                                                        // if matches so we need to change offline the device
                                                                        DatabaseReference updateStatus = firebaseDatabase.getReference("DeviceID")
                                                                                .child(historyDisplayOnFirebase.getArduinoId());

                                                                        Map<String,Object> updateStat = new HashMap<>();
                                                                        updateStat.put("status","Offline");

                                                                        updateStatus.updateChildren(updateStat).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                            @Override
                                                                            public void onSuccess(Void unused) {
                                                                                DatabaseReference updateStatuss = firebaseDatabase.getReference("ArduinoDeviceId")
                                                                                        .child(historyDisplayOnFirebase.getArduinoId());

                                                                                updateStatuss.updateChildren(updateStat);
                                                                            }
                                                                        });
                                                                    }
                                                                    // If more than 15 minutes have passed, send the data to "HistoryOfPet"
                                                                    else {
                                                                        Log.e("tanginaNaginsert","baoboaddasdaob");
                                                                        //did not exist will be insert history of pet
                                                                        sendDataToHistoryOfPet(historyDisplayOnFirebase);
                                                                    }
                                                                } else {
                                                                    // Log a message if less than 15 minutes have passed
                                                                    Log.e("Time", "Less than 15 minutes have passed.");
                                                                }
                                                            } else {
                                                                Log.e("tanginaNaginsert","baoboadob");
                                                                // If the date doesn't match, check the time again
                                                                sendDataToHistoryOfPet(historyDisplayOnFirebase);
                                                            }
                                                        }
                                                    }
                                                }
                                            } else {
                                                DatabaseReference databaseReference = firebaseDatabase.getReference("Pet Information")
                                                        .child(userId)
                                                        .child(ArduinoID);

                                                databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
                                                    @Override
                                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                        if (snapshot.exists()) {
                                                            Log.e("tanginaNaginsert","baoboaob");
                                                            sendDataToHistoryOfPet(historyDisplayOnFirebase);
                                                        }
                                                    }

                                                    @Override
                                                    public void onCancelled(@NonNull DatabaseError error) {

                                                    }
                                                });
                                            }
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError error) {
                                            // Handle possible errors
                                            Log.e("FetchError", "Error fetching last entry", error.toException());
                                        }
                                    });
                                }
                            }
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle any errors that occur while fetching the DeviceID data
                Log.e("FetchError", "Error fetching DeviceID", error.toException());
            }
        });
    }

    // Method to check if more than 20 minutes have passed
    public boolean isTimeOver15Minutes(String lastTimeOnHistory) {
        // Ensure that the time is in the correct "HH:mm" format
        String time = lastTimeOnHistory;

        // Format the time to HH:mm if necessary (in case it's in H:mm format)
        if (time != null && time.length() == 4) {
            time = "0" + time;  // Add leading zero if hour is single-digit
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
            LocalTime startime = LocalTime.parse(time,timeFormat);
            LocalTime endTime = LocalTime.parse(formattedTime,timeFormat);

            // Calculate the minutes difference, considering the potential for crossing midnight
            long minutesBetween = ChronoUnit.MINUTES.between(startime, endTime);

        /* If the minutes difference is negative, it means the time crossed midnight
        if (minutesBetween < 0) {
            // Add 24 hours to the end time to handle midnight wraparound
            minutesBetween += 24 * 60; // 24 hours in minutes
        }*/

            // Check if the difference is greater than 20 minutes
            return minutesBetween > 15;
        } catch (DateTimeParseException e) {
            // If there is an error in parsing, log it and return false (or handle error appropriately)
            Log.e("TimeParseError", "Failed to parse time: " + lastTimeOnHistory, e);
            return false;
        }
    }

    // Method to send data to the "HistoryOfPet" node in Firebase
    private void sendDataToHistoryOfPet(HistoryDisplayOnFirebase historyDisplayOnFirebase) {

        if (isInsertingg) {
            Log.d("InsertNotification", "Already inserting, skipping duplicate.");
            return;  // Skip if a previous insert is still in progress.
        }

        isInsertingg = true; // Set the flag to true when starting the insert.

                String lastTimeOnHistory = historyDisplayOnFirebase.getTime();

                // Create a new entry with updated time and date
                String time_datee = "", timee = "", datee = "";
                long time_stamp = System.currentTimeMillis();

               //  Ensure the lastTimeOnHistory is in the correct format: "HH:mm" (with two digits for the hour)
                if (lastTimeOnHistory != null && lastTimeOnHistory.length() == 5) {
                    // If the hour is single digit (like "8:30"), we prepend a "0" to make it "08:30"
                    if (lastTimeOnHistory.charAt(0) != '0' && lastTimeOnHistory.charAt(1) != ':') {
                        lastTimeOnHistory = "0" + lastTimeOnHistory;
                    }
                }

                try {
                    // Construct the date_time string correctly
                    String time_date = historyDisplayOnFirebase.getDate() + " " + historyDisplayOnFirebase.getTime();
                    String date = historyDisplayOnFirebase.getDate();

                    // Define the input date format
                    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yy/MM/dd HH:mm");
                    SimpleDateFormat simpleTimeFormat = new SimpleDateFormat("HH:mm");
                    SimpleDateFormat simpleDateFormat1 = new SimpleDateFormat("yy/MM/dd");

                    // Parse the input string to a Date object
                    Date parseDate = simpleDateFormat.parse(time_date);
                    Date parseTime = simpleTimeFormat.parse(lastTimeOnHistory);
                    Date date1 = simpleDateFormat1.parse(date);

                    // Define the desired output format
                    SimpleDateFormat outputFormat = new SimpleDateFormat("MMM dd 'at' hh:mm a");
                    SimpleDateFormat outputTime = new SimpleDateFormat("hh:mm a");
                    SimpleDateFormat outputDate = new SimpleDateFormat("yyyy/MM/dd");

                    time_datee = outputFormat.format(parseDate);
                    timee = outputTime.format(parseTime);
                    datee = outputDate.format(date1);
                }  catch (Exception e) {
                    e.printStackTrace();;
                }

                Map<String,Object> insertHistory = new HashMap<>();
                insertHistory.put("arduinoId", historyDisplayOnFirebase.getArduinoId());
                insertHistory.put("petName",historyDisplayOnFirebase.getPetName());
                insertHistory.put("latitude",historyDisplayOnFirebase.getLatitude());
                insertHistory.put("longitude",historyDisplayOnFirebase.getLongitude());
                insertHistory.put("time",historyDisplayOnFirebase.getTime());
                insertHistory.put("time_formated",timee);
                insertHistory.put("timestamp",time_stamp);
                insertHistory.put("date",datee);
                insertHistory.put("datee",historyDisplayOnFirebase.getDate());
                // Insert the new entry into the "HistoryOfPet" node in Firebase
                String finalTime_datee = time_datee;

        // Reference to the "HistoryOfPet" node for the specific Arduino ID
        DatabaseReference historyOfPet = firebaseDatabase.getReference("HistoryOfPet")
                .child(historyDisplayOnFirebase.getArduinoId());

        // Generate a unique key for the new entry
        String ID = historyOfPet.push().getKey();

        historyOfPet.child(ID).setValue(insertHistory).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                // Log a success message when data is successfully inserted into Firebase
                Log.d("SuccessInsertHistory", "Data successfully inserted into HistoryOfPet");

                DatabaseReference databaseReferencee = firebaseDatabase.getReference("Pet Information")
                        .child(userId)
                        .child(historyDisplayOnFirebase.getArduinoId());

                Map<String,Object> update = new HashMap<>();
                update.put("date_time",finalTime_datee);
                databaseReferencee.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            databaseReferencee.updateChildren(update).addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void unused) {
                                    isInsertingg = false; // Reset the flag
                                }
                            });

                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                // Log an error message if there was an issue inserting the data
                Log.e("InsertError", "Error inserting data into HistoryOfPet", e);
            }
        });

    }

    private void checkifdateis0() {
        Handler handler1 = new Handler();
        Runnable runnable1 = new Runnable() {
            @Override
            public void run() {
                DatabaseReference databaseReference = firebaseDatabase.getReference("Pet Information")
                        .child(userId);

                databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            for (DataSnapshot snapshot1 : snapshot.getChildren()) {
                                String petDay = snapshot1.child("petDays").getValue(String.class);
                                String petDataTime = snapshot1.child("petDataTime").getValue(String.class);
                                String askuserifpetload = snapshot1.child("askuserifpetload").getValue(String.class);

                                String arduinoId = snapshot1.child("arduinoId").getValue(String.class);
                                Map<String, Object> update = new HashMap<>();

                                DatabaseReference databaseReference1 = firebaseDatabase.getReference("Pet Information")
                                        .child(userId)
                                        .child(arduinoId);

                                if ("0".equals(petDay)) {  // Check petDay as a String
                                    DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm");

                                    // Get current time
                                    LocalTime localTime = LocalTime.now();
                                    String time_now = localTime.format(dateTimeFormatter);

                                    if (time_now.equals(petDataTime)) {
                                        // Only update if it's necessary
                                        if (askuserifpetload == null) {
                                            update.put("askuserifpetload", "asking");
                                            databaseReference1.updateChildren(update);
                                        }else {
                                            update.put("askuserifpetload", "asking");
                                            databaseReference1.updateChildren(update);
                                        }
                                    }
                                }
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        // Handle potential errors (logging, retry logic, etc.)
                        Log.e("DatabaseError", error.getMessage());
                    }
                });

                // Schedule the next execution after 10 seconds
                handler1.postDelayed(this, 10000);
            }
        };

        // Start the initial execution of the Runnable
        handler1.post(runnable1);
    }


    private void checkDataBalance() {
        DatabaseReference databaseReferencep = firebaseDatabase.getReference("Pet Information")
                .child(userId);

        databaseReferencep.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String monthdayyear = "";
                    Map<String, Object> insert = new HashMap<>();

                    for (DataSnapshot snapshot1 : snapshot.getChildren()) {
                        String arduinoId = snapshot1.child("arduinoId").getValue(String.class);
                        String petDataDate = snapshot1.child("petDataDate").getValue(String.class);
                        String petDaysStr = snapshot1.child("petDays").getValue(String.class);

                        // Parse petDataDate into LocalDate (assuming format is "yyyy-MM-dd")
                        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
                        LocalDate petDate = LocalDate.parse(petDataDate, formatter);

                        // Get the current date
                        LocalDate currentDate = LocalDate.now();

                        // Calculate the difference in days
                        long daysBetween = ChronoUnit.DAYS.between(petDate, currentDate);

                        // Log the updated petDays for debugging
                        Log.d("daysBetween", "New Pet Days: " + daysBetween);

                        // Retrieve current petDays value (it should be a string)
                        int petDays =  Integer.parseInt(petDaysStr);

                        // If lastUpdatedDate is not null and different from today, we can update petDays
                        if (petDataDate != null) {
                            LocalDate lastUpdatedDate = LocalDate.parse(petDataDate, formatter);

                            // If it's a new day, subtract the number of days and update petDays
                            if (!lastUpdatedDate.isEqual(currentDate)) {

                                int newPetDays = petDays - (int) daysBetween;

                                if (Integer.parseInt(String.valueOf(newPetDays)) == -1) {
                                    insert.put("petDays","-1");
                                }
                                else if (Integer.parseInt(String.valueOf(newPetDays)) == 0) {
                                    monthdayyear = time;
                                    insert.put("petDataMonthorDay","Today");
                                    insert.put("petDays",String.valueOf(newPetDays));
                                    insert.put("petDataDate",String.valueOf(currentDate));
                                    insert.put("petDayORMONTH",monthdayyear);
                                }
                                else if (Integer.parseInt(String.valueOf(newPetDays)) <= 31) {
                                    monthdayyear = "Days";
                                    insert.put("petDataMonthorDay",String.valueOf(newPetDays));
                                    insert.put("petDays",String.valueOf(newPetDays));
                                    insert.put("petDataDate",String.valueOf(currentDate));
                                    insert.put("petDayORMONTH",monthdayyear);
                                }else if (Integer.parseInt(String.valueOf(newPetDays)) < 365) {
                                    int days = Integer.parseInt(String.valueOf(newPetDays));  // Parse timeInDays once

                                    String month = "";
                                    if (days > 31 && days <= 62) {
                                        month = "2";
                                    } else if (days <= 93) {
                                        month = "3";
                                    } else if (days <= 124) {
                                        month = "4";
                                    } else if (days <= 155) {
                                        month = "5";
                                    } else if (days <= 186) {
                                        month = "6";
                                    } else if (days <= 217) {
                                        month = "7";
                                    } else if (days <= 248) {
                                        month = "8";
                                    } else if (days <= 279) {
                                        month = "9";
                                    } else if (days <= 310) {
                                        month = "19";
                                    } else if (days <= 341) {
                                        month = "11";
                                    } else if (days <= 372) {
                                        month = "12";
                                    }

                                    monthdayyear = "Months";
                                    insert.put("petDataMonthorDay", month);
                                    insert.put("petDayORMONTH", monthdayyear);
                                    insert.put("petDays",String.valueOf(newPetDays));
                                    insert.put("petDataDate",String.valueOf(currentDate));

                                }else {
                                    monthdayyear = "Years";
                                    insert.put("petDays",String.valueOf(newPetDays));
                                    insert.put("petDataDate",String.valueOf(currentDate));
                                    insert.put("petDayORMONTH",monthdayyear);
                                }

                                DatabaseReference databaseReferencel = firebaseDatabase.getReference("Pet Information")
                                        .child(userId)
                                        .child(arduinoId);

                                databaseReferencel.updateChildren(insert).addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void unused) {
                                        if (newPetDays == 7 || newPetDays == 3 || newPetDays == 0) {
                                            //update
                                            DatabaseReference databaseReferenceo = firebaseDatabase.getReference("Pet Information")
                                                    .child(userId)
                                                    .child(arduinoId);
                                            Map<String,Object> update = new HashMap<>();
                                            update.put("alert","alertnow");

                                            databaseReferenceo.updateChildren(update).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void unused) {

                                                }
                                            });

                                        }
                                    }
                                });

                                // Log the updated petDays for debugging
                                Log.d("PetDaysUpdated", "New Pet Days: " + newPetDays);
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

    private void checkIf7days3days0days () {

        DatabaseReference databaseReference = firebaseDatabase.getReference("Pet Information")
                .child(userId);

        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    for (DataSnapshot snapshot1 : snapshot.getChildren()) {
                        String petName = snapshot1.child("petName").getValue(String.class);
                        String petDataTime = snapshot1.child("petDataTime").getValue(String.class);
                        String expDate = snapshot1.child("expDate").getValue(String.class);
                        String arduinoId = snapshot1.child("arduinoId").getValue(String.class);
                        int newPetDays = Integer.parseInt(snapshot1.child("petDays").getValue(String.class));
                        String alertnow = snapshot1.child("alert").getValue(String.class);

                        if (alertnow != null && alertnow.equals("alertnow")) {
                            if (newPetDays == 7 || newPetDays == 3 || newPetDays == 0) {
                                if (newPetDays == -1) {
                                    return;
                                }

                                if (newPetDays == 0) {

                                    try {
                                        SimpleDateFormat timeformat = new SimpleDateFormat("HH:mm");
                                        Date parseTime = timeformat.parse(petDataTime);
                                        SimpleDateFormat timeformatt = new SimpleDateFormat("hh:mm a");
                                        Log.e("hapoha",petDataTime);
                                        timee = timeformatt.format(parseTime);
                                    } catch (ParseException e) {
                                        throw new RuntimeException(e);
                                    }
                                    insertDataBalanceIfLoadHasBeen7Days3Days0Days = "Hi, Your pet " + petName + ", has the data plan you availed will expired today at " + timee + ".";

                                    showPetDataNotification(insertDataBalanceIfLoadHasBeen7Days3Days0Days, arduinoId, String.valueOf(newPetDays));

                                } else if (newPetDays == 3) {
                                    try {
                                        SimpleDateFormat timeformat = new SimpleDateFormat("HH:mm");
                                        Date parseTime = timeformat.parse(petDataTime);
                                        SimpleDateFormat timeformatt = new SimpleDateFormat("hh:mm a");
                                        timee = timeformatt.format(parseTime);
                                    } catch (ParseException e) {
                                        e.printStackTrace();
                                    }

                                    insertDataBalanceIfLoadHasBeen7Days3Days0Days = "Hi, Your pet "+ petName +" has "+ newPetDays + " days remaining on the data plan you have availed, which will expire on " + expDate + " at " + timee + ".";

                                    showPetDataNotification(insertDataBalanceIfLoadHasBeen7Days3Days0Days, arduinoId, String.valueOf(newPetDays));
                                } else if (newPetDays == 7) {
                                    try {
                                        SimpleDateFormat timeformat = new SimpleDateFormat("HH:mm");
                                        Date parseTime = timeformat.parse(petDataTime);
                                        SimpleDateFormat timeformatt = new SimpleDateFormat("hh:mm a");
                                        timee = timeformatt.format(parseTime);
                                    } catch (ParseException e) {
                                        e.printStackTrace();
                                    }

                                    insertDataBalanceIfLoadHasBeen7Days3Days0Days = "Hi, Your pet "+ petName +" has "+ newPetDays + " days remaining on the data plan you have availed, which will expire on " + expDate + " at " + timee + ".";

                                    showPetDataNotification(insertDataBalanceIfLoadHasBeen7Days3Days0Days, arduinoId, String.valueOf(newPetDays));
                                }

                                if (insertedPetDays) {
                                    return;
                                }
                                insertedPetDays = true;

                                long currentTime = System.currentTimeMillis();

                                DatabaseReference addNotifi = firebaseDatabase.getReference("User Notification")
                                        .child(userId);

                                addNotifi.orderByChild("timestamp")
                                        .limitToLast(1)
                                        .addListenerForSingleValueEvent(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                if (snapshot.exists()) {
                                                    // Check if the last inserted notification matches the new one
                                                    for (DataSnapshot childSnapshot : snapshot.getChildren()) {
                                                        String existingNotification = childSnapshot.child("notification").getValue(String.class);
                                                        if (existingNotification != null && existingNotification.equals(insertDataBalanceIfLoadHasBeen7Days3Days0Days)) {
                                                            Log.d("InsertNotification", "Duplicate notification detected. Not inserting.");
                                                            insertedPetDays = false; // Reset the flag
                                                            return; // Exit the method early since it's a duplicate
                                                        }
                                                    }
                                                }


                                                // Create a new notification ID
                                                String notificationID = addNotifi.push().getKey();
                                                // Format the date and time
                                                SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd 'at' hh:mm a", Locale.getDefault());
                                                String formattedDate = dateFormat.format(new Date());

                                                // Prepare the data for insertion
                                                Map<String, Object> insertNotification = new HashMap<>();
                                                insertNotification.put("notification", insertDataBalanceIfLoadHasBeen7Days3Days0Days);
                                                insertNotification.put("date", formattedDate);
                                                insertNotification.put("timestamp", currentTime);

                                                // Insert the notification into the database
                                                addNotifi.child(notificationID).setValue(insertNotification)
                                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                            @Override
                                                            public void onSuccess(Void unused) {
                                                                insertedPetDays = false; // Reset flag after insert
                                                            }
                                                        });
                                            }

                                            @Override
                                            public void onCancelled(@NonNull DatabaseError error) {
                                                Log.e("DatabaseError", "Error querying database: " + error.getMessage());
                                                isInserting = false; // Reset flag if cancelled
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

    // Show notification with custom action button "I'm aware"
    private void showPetDataNotification(String message, String arduinoId, String petDaysStr) {
        Log.d("bb", "Creating notification with message: " + message);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(CHANNEL_IDd, "Pet Tracker", NotificationManager.IMPORTANCE_HIGH);
            channel.setDescription("Pet distance alerts");
            notificationManager.createNotificationChannel(channel);
        }

        // Intent to launch Home.class
        Intent activityIntent = new Intent(this, Home.class);
        activityIntent.putExtra("myForeGroundListFragment", "showPetDataNotification");
        activityIntent.setAction("com.example.pettracker.ACTION_ACKNOWLEDGE_ALERTt");
        activityIntent.putExtra("listIDArduino", arduinoId);
        activityIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        PendingIntent activityPendingIntent = PendingIntent.getActivity(this, 0, activityIntent, PendingIntent.FLAG_MUTABLE);

        Notification notification = new NotificationCompat.Builder(getApplicationContext(), CHANNEL_IDd)
                .setContentTitle("Notification Pet Alert")
                .setContentText(message)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(message))
                .setSmallIcon(R.drawable.image_gps_trackerr)
                .setAutoCancel(false) // Ensure this is set to false
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(activityPendingIntent) // Set the activity intent here
                //    .addAction(R.drawable.icons8_notification_50, "I'm aware", pendingIntent) // Add "I'm aware" button
                .build();

        // Create a unique notification ID using the pet's arduinoId (or Index)
        int notificationId = arduinoId.hashCode();

        notificationManager.notify(notificationId, notification); // Use unique notification ID

        Log.d("Notification", "Notification displayed.");
        if (petDaysStr.equals("0")) {
            startAlert();
        }else if (petDaysStr.equals("3")) {
            startAlert();
        }if (petDaysStr.equals("7")) {
            startAlert();
        }
    }



    private void LocationUserRealTimer() {
        LocationEngine locationEngine = LocationEngineProvider.getBestLocationEngine(getApplicationContext());

        // Create a LocationEngineRequest for high accuracy
        LocationEngineRequest request = new LocationEngineRequest.Builder(60000) // Update interval in 20 seconds
                .setPriority(LocationEngineRequest.PRIORITY_HIGH_ACCURACY)
                .setDisplacement(0f)  // Minimum displacement of 10 meters before triggering an update
                .build();

        if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                || ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

            locationEngine.requestLocationUpdates(request, new LocationEngineCallback<LocationEngineResult>() {
                @Override
                public void onSuccess(LocationEngineResult result) {
                    Context context = getApplicationContext();
                    if (context != null) {
                        Location location = result.getLastLocation();
                        // insert their latest location when user click close navigation

                        if (location != null) {
                            double Longitude = location.getLongitude();
                            double Latitude = location.getLatitude();

                            Log.e("LOLO", String.valueOf(Latitude));
                            Log.e("LOLO", String.valueOf(Longitude));

                            DatabaseReference databaseReference = firebaseDatabase.getReference("User Information")
                                    .child(userId);
                            Map<String, Object> updateLongitudeAndLatitude = new HashMap<>();
                            updateLongitudeAndLatitude.put("longitude",String.valueOf(Longitude));
                            updateLongitudeAndLatitude.put("latitude", String.valueOf(Latitude));
                            databaseReference.updateChildren(updateLongitudeAndLatitude);


                        }
                    }
                }

                @Override
                public void onFailure(@NonNull Exception exception) {
                    Toast.makeText(getApplicationContext(),exception.getMessage(),Toast.LENGTH_SHORT).show();
                }
            }, getMainLooper());
        }
    }

    public void StopHandlerLocation () {
        //  handler.removeCallbacks(runnable);
    }

    public void checkBatteryPercentage () {

        DatabaseReference databaseReference = firebaseDatabase.getReference("Pet Information")
                .child(userId);

        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String notification = "";
                    for (DataSnapshot snapshot1 : snapshot.getChildren()) {
                        if (snapshot1.child("battery").exists()) {
                            String petBattery = snapshot1.child("battery").getValue(String.class);
                            String petName = snapshot1.child("petName").getValue(String.class);
                            String arduinoId = snapshot1.child( "arduinoId").getValue(String.class);
                            String petBat = snapshot1.child("petBat").getValue(String.class);

                            //get current date
                            Date currentDate = new Date();

                            //crete date format
                            SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd 'at' hh:mm a", Locale.getDefault());
                            String formattedDate = dateFormat.format(currentDate);
                            long timestamp = System.currentTimeMillis();

                            Map<String,Object> insertNotification = new HashMap<>();

                            if (petBat == "notc") {
                                if (Integer.parseInt(petBattery) == 20 ) {
                                    // insertion on notification
                                    notification = "Your pet "+ petName +" has a battery level of " + petBattery +"%. Please charge it.";
                                    insertNotification.put("notification",notification);
                                    insertNotification.put("date",formattedDate);
                                    insertNotification.put("timestamp",timestamp);

                                    DatabaseReference databaseReference1 = firebaseDatabase.getReference("User Notification")
                                            .child(userId);
                                    databaseReference1.addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                                            if (snapshot.exists()) {
                                                String notificationId = databaseReference1.push().getKey();
                                                databaseReference1.child(notificationId).setValue(insertNotification);
                                            }
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError error) {

                                        }
                                    });

                                    DatabaseReference petInfoRef = firebaseDatabase.getReference("Pet Information")
                                            .child(userId)
                                            .child(arduinoId);

                                    petInfoRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                                            if (snapshot.exists()) {
                                                Map<String, Object> petInfo = new HashMap<>();
                                                petInfo.put("petBat", "20");

                                                petInfoRef.updateChildren(petInfo).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task) {

                                                    }
                                                });
                                            }
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError error) {

                                        }
                                    });
                                }
                            } else if (petBat == "20") {
                                if (Integer.parseInt(petBattery) == 10) {
                                    // insertion on notification
                                    notification = "Your pet "+ petName +" has a battery level of " + petBattery +"%. Please charge it.";
                                    insertNotification.put("notification",notification);
                                    insertNotification.put("date",formattedDate);
                                    insertNotification.put("timestamp",timestamp);


                                    DatabaseReference databaseReference1 = firebaseDatabase.getReference("User Notification")
                                            .child(userId);
                                    databaseReference1.addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                                            if (snapshot.exists()) {
                                                String notificationId = databaseReference1.push().getKey();
                                                databaseReference1.child(notificationId).setValue(insertNotification);
                                            }
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError error) {

                                        }
                                    });

                                    DatabaseReference petInfoRef = firebaseDatabase.getReference("Pet Information")
                                            .child(userId)
                                            .child(arduinoId);

                                    petInfoRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                                            if (snapshot.exists()) {
                                                Map<String, Object> petInfo = new HashMap<>();
                                                petInfo.put("petBat", "10");

                                                petInfoRef.updateChildren(petInfo).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task) {

                                                    }
                                                });
                                            }
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError error) {

                                        }
                                    });
                                }
                            } else if (Integer.parseInt(petBattery) > 20) {
                                DatabaseReference petInfoRef = firebaseDatabase.getReference("Pet Information")
                                        .child(userId)
                                        .child(arduinoId);

                                petInfoRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                        if (snapshot.exists()) {
                                            Map<String, Object> petInfo = new HashMap<>();
                                            petInfo.put("petBat", "notc");

                                            petInfoRef.updateChildren(petInfo).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {

                                                }
                                            });
                                        }
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {

                                    }
                                });
                            }

                            if (notification.equals("")){

                            }else {
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                    NotificationChannel channel = new NotificationChannel(CHANNEL_ID_BATTERY,"Pet Tracker", NotificationManager.IMPORTANCE_HIGH);
                                    channel.setDescription("Pet distance alerts");
                                    notificationManager.createNotificationChannel(channel);
                                }

                                Intent acitivtyInten = new Intent(myForeGroundService.this, Home.class);
                                acitivtyInten.putExtra("myForeGroundListFragment","previousFragment");
                                acitivtyInten.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                PendingIntent activityPending = PendingIntent.getActivity(myForeGroundService.this,0,acitivtyInten,PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

                                Notification notification1 = new NotificationCompat.Builder(getApplicationContext(), CHANNEL_ID_BATTERY)
                                        .setContentText(notification)
                                        .setContentTitle("Notification Pet Alert")
                                        .setStyle(new NotificationCompat.BigTextStyle().bigText(notification))
                                        .setPriority(NotificationCompat.PRIORITY_HIGH)
                                        .setContentIntent(activityPending)
                                        .setSmallIcon(R.drawable.image_gps_trackerr)
                                        .build();
                                notificationManager.notify(3,notification1);
                                notification = "";
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


    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d("myForeGroundService", "onDestroy called");
        onDistroy = false;
        // Remove the notification and stop the service
        stopForeground(true); // Remove the notification
        stopSelf(); // Stop the service completely
        isNotificationStarted = false; // Reset the flag
        isServiceRunning = false; // Set to false when service is destroyed
    }


    public class LocalBinder extends Binder {
        myForeGroundService getService() {
            return myForeGroundService.this;
        }
    }
}


/* // Create a new LocationRequest object to specify location request settings

 */
