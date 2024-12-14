package com.example.gps_pet_tracker; // Defines the package for this class.

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.media.MediaPlayer;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class PetTrackerWorkerManager extends Worker { // Defines a Worker class for performing background work.

    private FirebaseAuth firebaseAuth; // Declares a variable for Firebase authentication.
    private FirebaseDatabase firebaseDatabase; // Declares a variable for Firebase Realtime Database.
    private static MediaPlayer mediaPlayer;
    private NotificationManager notificationManager;
    private static boolean isAlerting = false;
    private static final String CHANNEL_ID = "pet_channel_id"; // Notification channel ID

    // Constructor to initialize Firebase components.
    public PetTrackerWorkerManager(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams); // Calls the parent constructor with context and worker parameters.

        // Initialize Firebase components.
        firebaseAuth = FirebaseAuth.getInstance(); // Gets the Firebase authentication instance.
        firebaseDatabase = FirebaseDatabase.getInstance(); // Gets the Firebase Realtime Database instance.
        notificationManager = (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
        mediaPlayer = MediaPlayer.create(getApplicationContext(),R.raw.alert);
    }

    // Override the doWork method to define the work to be done in the background.
    @NonNull
    @Override
    public Result doWork() {
        // Get the user ID from Firebase Authentication.
        String userId = firebaseAuth.getCurrentUser().getUid();
        if (userId != null) { // Check if user ID is not null.
            checkPetDistance(userId); // Call the method to check the distance between user and pet.
            checkPetLocationAndDisplayOnHistoryOfPet ();
        }
        // Return success result after performing work.
        return Result.success();
    }

    // Method to check the distance between user and pet.
    private void checkPetDistance(String userId) {
        DatabaseReference databaseReference = firebaseDatabase.getReference("User Information")
                .child(userId);
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot1) {
                if (snapshot1.exists()) { // Check if the snapshot exists and has data.

                    DatabaseReference petInformation = firebaseDatabase.getReference("Pet Information").child(userId);
                    // Add listener to fetch pet data.
                    petInformation.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if (snapshot.exists()) {
                                StringBuilder messageBuilder = new StringBuilder();
                                for (DataSnapshot snapshot11 : snapshot.getChildren()) {
                                    // loop all content of pet
                                    String longitudeUserStr = snapshot1.child("longitude").getValue(String.class); // Initialize user's longitude.
                                    String latitudeUserStr = snapshot1.child("latitude").getValue(String.class); // Initialize user's latitude.

                                    // arduino id on Pet Information
                                    String arduinoId = snapshot11.child("arduinoId").getValue(String.class);

                                    // check if longitude  and latitude are existing on firebase on petInformation
                                    if (longitudeUserStr != "0.0" && latitudeUserStr != "0.0") {
                                        // Check if longitude and latitude are existing on pet snapshot11
                                        if (snapshot11.child("longitude").exists() && snapshot11.child("latitude").exists()) {
                                            // Get pet's longitude and latitude from snapshot.

                                            double longitudeUser = Double.parseDouble(longitudeUserStr); // Initialize user's longitude.
                                            double latitudeUser = Double.parseDouble(latitudeUserStr); // Initialize user's latitude.

                                            double longitudePet = snapshot11.child("longitude").getValue(Double.class);
                                            double latitudePet = snapshot11.child("latitude").getValue(Double.class);

                                            String petDistance = snapshot11.child("petDistance").getValue(String.class);
                                            String petName = snapshot11.child("petName").getValue(String.class);
                                            String distanceText, distanceTextInsert;
                                            // Get user's current location.

                                            // Calculate distance between user and pet.
                                            float[] results = new float[1]; // Array to store distance result.
                                            Location.distanceBetween(latitudeUser, longitudeUser, latitudePet, longitudePet, results);
                                            float distanceInMeters = results[0]; // Get distance in meters.

                                            // Format distance text based on distance value.
                                            if (distanceInMeters < 1000) {
                                                // If the distance is less than 1000 meters, display it in meters without decimal places
                                                distanceTextInsert = String.format("%.0f m", distanceInMeters);
                                                distanceText = String.format("%.0f m", distanceInMeters);
                                            } else {
                                                // If the distance is 1000 meters or more, convert it to kilometers and display with two decimal places
                                                float distanceInKm = distanceInMeters / 1000;
                                                distanceTextInsert = String.format("%.2f km", distanceInKm);
                                                distanceText = String.format("%.2f km", distanceInKm);
                                            }

                                            // Check if pet distance is greater than the specified distance.
                                            if (Integer.parseInt(petDistance) < distanceInMeters) {
                                                messageBuilder.append("Your pet ").append(petName)
                                                        .append(" has exceeded the set distance! Is over ").append(distanceText).append(" away from your location.\n");
                                                // Show notification if pet is missing.
                                               // showPetMissingNotification(petName, distanceText);
                                            }

                                            // Update current distance in Firebase Realtime Database.
                                            DatabaseReference databaseReference = firebaseDatabase.getReference("Pet Information")
                                                    .child(userId)
                                                    .child(arduinoId)
                                                    .child("currentDistance");
                                            databaseReference.setValue(distanceTextInsert); // Set the updated distance value.
                                        }else {
                                            // if pet longitude at latitude are not existing on firebase
                                            String distanceText = "Not Config";
                                            // Get a reference to the database path where the distance will be stored
                                            DatabaseReference databaseReference = firebaseDatabase.getReference("Pet Information")
                                                    .child(userId)
                                                    .child(arduinoId)
                                                    .child("currentDistance");
                                            // Set the calculated distance value at the specified path
                                            databaseReference.setValue(distanceText);
                                        }
                                    }else {
                                        // if user are 0.0 longitude at latitude
                                        String distanceText = "Not Config";
                                        // Get a reference to the database path where the distance will be stored
                                        DatabaseReference databaseReference = firebaseDatabase.getReference("Pet Information")
                                                .child(userId)
                                                .child(arduinoId)
                                                .child("currentDistance");
                                        // Set the calculated distance value at the specified path
                                        databaseReference.setValue(distanceText);
                                    }

                                }
                                if (messageBuilder.length() > 0) {
                                    showPetMissingNotification(messageBuilder.toString());
                                    startAlert();
                                }
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

            }
        });
    }

    // Start alert with sound and notification
    private void startAlert() {
        if (!isAlerting) {
            isAlerting = true;
            mediaPlayer.start();

            mediaPlayer.setOnCompletionListener(mp -> {
                if (isAlerting) {
                    mediaPlayer.start();
                }
            });
        }
    }

    // Stop the alert sound
    public static void stopAlert() {
        isAlerting = false;
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            mediaPlayer.stop();
            mediaPlayer.prepareAsync(); // Reset mediaPlayer to reuse it
        }
    }

    // Show notification with custom action button "I'm aware"
    private void showPetMissingNotification(String message) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, "Pet Tracker", NotificationManager.IMPORTANCE_HIGH);
            channel.setDescription("Pet distance alerts");
            notificationManager.createNotificationChannel(channel);
        }

        Intent intent = new Intent(getApplicationContext(), AcknowledgeReceiver.class); // Handle button click via BroadcastReceiver
        PendingIntent pendingIntent = PendingIntent.getBroadcast(getApplicationContext(), 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        Notification notification = new NotificationCompat.Builder(getApplicationContext(), CHANNEL_ID)
                .setContentTitle("Pet Alert")
                .setContentText(message)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(message)) // Allow multiline text for multiple pets
                .setSmallIcon(R.drawable.image_gps_trackerr) // Custom notification icon
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .addAction(R.drawable.circle_icon, "I'm aware", pendingIntent) // Add "I'm aware" button
                .build();

        notificationManager.notify(1, notification);
    }

    // BroadcastReceiver to handle button click and stop alert
    public static class AcknowledgeReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            PetTrackerWorkerManager.stopAlert();
        }
    }

    private void checkPetLocationAndDisplayOnHistoryOfPet() {
        // Reference to the "ArduinoDeviceId" node in Firebase
        DatabaseReference fetchArduinoID = firebaseDatabase.getReference("ArduinoDeviceId");

        // Attach a listener to get updates when data changes in the "ArduinoDeviceId" node
        fetchArduinoID.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                // Iterate over all the children of "ArduinoDeviceId"
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    // Convert snapshot to HistoryDisplayOnFirebase object (which contains ArduinoId, location, etc.)
                    HistoryDisplayOnFirebase historyDisplayOnFirebase = dataSnapshot.getValue(HistoryDisplayOnFirebase.class);

                    if (historyDisplayOnFirebase != null) {
                        double longitude = historyDisplayOnFirebase.getLongitude();  // Get the longitude
                        double latitude = historyDisplayOnFirebase.getLatitude();    // Get the latitude

                        String longitudeStr = String.valueOf(longitude);  // Convert longitude to String
                        String latitudeStr = String.valueOf(latitude);    // Convert latitude to String

                        // Check if the coordinates are valid (not zero)
                        if (!longitudeStr.equals("0.0") && !latitudeStr.equals("0.0")) {

                            // Reference to the "HistoryOfPet" node in Firebase under the specific Arduino ID
                            DatabaseReference historyOfPetRef = firebaseDatabase.getReference("HistoryOfPet")
                                    .child(historyDisplayOnFirebase.getArduinoId());

                            // Fetch the last recorded entry in "HistoryOfPet" using orderByKey().limitToLast(1)
                            historyOfPetRef.orderByKey().limitToLast(1).addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    // Get the current date and time from the system
                                    String currentDate = new SimpleDateFormat("dd/M/yyyy", Locale.getDefault()).format(new Date());
                                    String currentTime = new SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(new Date());

                                    // Default values for last recorded date and time
                                    String lastDateOnHistory = "";
                                    String lastTimeOnHistory = "";

                                    // If there's data in "HistoryOfPet", retrieve the last date and time
                                    if (dataSnapshot.exists()) {
                                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                            // Get the last recorded entry
                                            HistoryDisplayOnFirebase lastEntry = snapshot.getValue(HistoryDisplayOnFirebase.class);
                                            if (lastEntry != null) {
                                                lastDateOnHistory = lastEntry.getDate();  // Get the last recorded date
                                                lastTimeOnHistory = lastEntry.getTime();  // Get the last recorded time

                                                // Compare the date from ArduinoDeviceId with the last recorded date
                                                String arduinoDateOnArduinoID = historyDisplayOnFirebase.getDate();  // Get date from ArduinoDeviceId
                                                String arduinoTimeOnArduinoID = historyDisplayOnFirebase.getTime();  // Get time from ArduinoDeviceId

                                                Log.e ("taeba",lastDateOnHistory);
                                                Log.e("taeba",lastTimeOnHistory);

                                                Log.e ("taena",arduinoDateOnArduinoID);
                                                Log.e("taena",arduinoTimeOnArduinoID);

                                                // Check if the last recorded date matches the current date from Arduino
                                                if (lastDateOnHistory.equals(arduinoDateOnArduinoID)) {
                                                    // If the date is the same, check the time
                                                    if (lastTimeOnHistory.equals(arduinoTimeOnArduinoID)) {
                                                        // If the time matches, no need to update Firebase
                                                    } else {
                                                        // If the time doesn't match, check if the time is over 15 minutes
                                                        if (isTimeOver15Minutes(lastTimeOnHistory)) {
                                                            // If more than 15 minutes have passed, send the data to "HistoryOfPet"
                                                            sendDataToHistoryOfPet(historyDisplayOnFirebase);
                                                        } else {
                                                            // Log a message if less than 15 minutes have passed
                                                            Log.e("Time", "Less than 15 minutes have passed.");
                                                        }
                                                    }
                                                } else {
                                                    // If the date doesn't match, check the time again
                                                    if (lastTimeOnHistory.equals(arduinoTimeOnArduinoID)) {
                                                        // If the time matches, no need to update Firebase
                                                    } else {
                                                        // If the time doesn't match, check if the time is over 15 minutes
                                                        if (isTimeOver15Minutes(lastTimeOnHistory)) {
                                                            // If more than 15 minutes have passed, send the data to "HistoryOfPet"
                                                            sendDataToHistoryOfPet(historyDisplayOnFirebase);
                                                        } else {
                                                            // Log a message if less than 15 minutes have passed
                                                            Log.e("Time", "Less than 15 minutes have passed.");
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }else {
                                        // no record so we need record now
                                        // send the data to "HistoryOfPet"
                                        sendDataToHistoryOfPet(historyDisplayOnFirebase);
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

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle any errors that occur while fetching the ArduinoDeviceId data
                Log.e("FetchError", "Error fetching ArduinoDeviceId", error.toException());
            }
        });
    }

    // Method to check if more than 15 minutes have passed
    public boolean isTimeOver15Minutes(String lastTimeOnHistory) {
        // Create a SimpleDateFormat object to parse time in HH:mm:ss
        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());

        try {
            // Get the current time in the same format ("HH:mm:ss")
            String currentTime = timeFormat.format(new Date());

            // Parse both the last recorded time and the current time to Date objects
            Date arduinoTimeDate = timeFormat.parse(lastTimeOnHistory);
            Date currentTimeDate = timeFormat.parse(currentTime);

            // Calculate the difference between current time and Arduino time in milliseconds
            long timeDifferenceMillis = currentTimeDate.getTime() - arduinoTimeDate.getTime();

            // Convert the difference to minutes
            long timeDifferenceMinutes = TimeUnit.MILLISECONDS.toMinutes(timeDifferenceMillis);

            // Check if the time difference is more than 15 minutes
            return timeDifferenceMinutes > 15;
        } catch (ParseException e) {
            // Handle any parsing exceptions
            e.printStackTrace();
        }
        return false;
    }

    // Method to send data to the "HistoryOfPet" node in Firebase
    private void sendDataToHistoryOfPet(HistoryDisplayOnFirebase historyDisplayOnFirebase) {
        // Create a new entry with updated time and date
        HistoryDisplayOnFirebase historyOfPetInsert = new HistoryDisplayOnFirebase(
                historyDisplayOnFirebase.getArduinoId(),
                historyDisplayOnFirebase.getPetName(),
                historyDisplayOnFirebase.getLatitude(),
                historyDisplayOnFirebase.getLongitude(),
                historyDisplayOnFirebase.getTime(),
                historyDisplayOnFirebase.getDate()
        );

        // Reference to the "HistoryOfPet" node for the specific Arduino ID
        DatabaseReference historyOfPet = firebaseDatabase.getReference("HistoryOfPet").child(historyDisplayOnFirebase.getArduinoId());

        // Generate a unique key for the new entry
        String ID = historyOfPet.push().getKey();

        // Insert the new entry into the "HistoryOfPet" node in Firebase
        historyOfPet.child(ID).setValue(historyOfPetInsert).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                // Log a success message when data is successfully inserted into Firebase
                Log.d("SuccessInsertHistory", "Data successfully inserted into HistoryOfPet");
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                // Log an error message if there was an issue inserting the data
                Log.e("InsertError", "Error inserting data into HistoryOfPet", e);
            }
        });
    }


}
