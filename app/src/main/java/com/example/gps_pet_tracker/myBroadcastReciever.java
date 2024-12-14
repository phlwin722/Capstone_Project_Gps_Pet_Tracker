package com.example.gps_pet_tracker;

import android.app.ActivityManager;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class myBroadcastReciever extends BroadcastReceiver {
    private static final String TAG = "myBroadcastReceiver"; // Added a tag for logging
    private FirebaseDatabase firebaseDatabase;
    private FirebaseAuth firebaseAuth;
    private String userId;

    @Override
    public void onReceive(Context context, Intent intent) {
        firebaseAuth = FirebaseAuth.getInstance();
        firebaseDatabase = FirebaseDatabase.getInstance();

        // Ensure the user is authenticated
        if (firebaseAuth.getCurrentUser() != null) {
            userId = firebaseAuth.getCurrentUser().getUid();

            // Start the foreground service if needed
            startForegroundService(context);
        } else {
            Log.w(TAG, "No authenticated user found.");
        }

        // Check if the service is running before launching the Home activity
        if (isServiceRunning(context, myForeGroundService.class)) {
            // my foregroundService
            // Check the action from the incoming intent to determine which action to handle
            if ("com.example.pettracker.ACTION_ACKNOWLEDGE_ALERT".equals(intent.getAction())) {
                handleAcknowledgeAlertt(context, intent);
            }else if ("com.example.pettracker.ACTION_ACKNOWLEDGE_ALERTt".equals(intent.getAction())) {
                handleAcknowledgeAlert(context, intent);
            }
        }
    }

    private void handleAcknowledgeAlertt(Context context, Intent intent) {
        Log.e(TAG, "User clicked 'I'm aware'");

        // Retrieve the list of IDs from the intent
        String arduinoId = intent.getStringExtra("arduinoId");  // Getting single ID
        if (arduinoId != null) {
                String userAware = "I aware";
                DatabaseReference databaseReference = firebaseDatabase.getReference("Pet Information")
                        .child(userId)
                        .child(arduinoId)
                        .child("userAware");

                // Update the database
                databaseReference.setValue(userAware).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            //sb.append(id).append(" "); // Append the acknowledged ID
                        } else {
                            Log.e(TAG, "Failed to update userAware for ID: " + arduinoId);
                        }
                    }
                }); // Launch Home activity
            // Launch the Home activity to display the pet's history
            launchHomeActivity(context, arduinoId);

        }

        // Stop the alert in the foreground service
        myForeGroundService serviceInstance = myForeGroundService.getInstance();
        if (serviceInstance != null) {
            serviceInstance.stopAlert();
            serviceInstance.onNotificationAcknowledged();
        }

        // Cancel the notification shown for this pet
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (notificationManager != null) {
            notificationManager.cancel(1); // Dismiss the notification with ID 1
            Log.d(TAG, "Notification canceled");
        }
    }

    // Launches the Home activity, passing the pet's Arduino ID to the intent
    private void launchHomeActivity(Context context, String arduinoId) {
        Intent activityIntent = new Intent(context, Home.class); // Create an Intent to launch Home activity
        activityIntent.putExtra("HistoryOfPet", arduinoId); // Pass the pet's Arduino ID to the Home activity
        activityIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK); // Clear existing activities from the stack
        context.startActivity(activityIntent); // Start the Home activity
    }

    // Checks if the specified service is currently running
    private boolean isServiceRunning(Context context, Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE); // Get the ActivityManager to check running services
        // Iterate through the list of running services
        for (ActivityManager.RunningServiceInfo serviceInfo : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(serviceInfo.service.getClassName())) { // Check if the target service is running
                return true; // Return true if the service is found
            }
        }
        return false; // Return false if the service is not running
    }

    // Starts the foreground service if it's not running
    private void startForegroundService(Context context) {
        Intent serviceIntent = new Intent(context, myForeGroundService.class); // Create an Intent to start the service
        context.startForegroundService(serviceIntent); // Start the service in the foreground (required for API 26+)
    }

    // this code is not use
    private void handleAcknowledgeAlert(Context context, Intent intent) {

        // Stop the alert in the foreground service
        myForeGroundService serviceInstance = myForeGroundService.getInstance();
        if (serviceInstance != null) {
            serviceInstance.stopAlert();
        }

        // Retrieve the list of IDs from the intent
        String arduinoId = intent.getStringExtra("arduinoId");  // Getting single ID

        // Cancel the ongoing notification
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (notificationManager != null) {
            notificationManager.cancel(1); // Dismiss the notification with ID 1
            Log.d(TAG, "Notification canceled");
        }
    }    // this code is not use
}