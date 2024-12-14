package com.example.gps_pet_tracker;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class create_user_loading_process extends AppCompatActivity implements ConnectionReceiver.ReceiverListener{
    FirebaseAuth firebaseAuth;
    FirebaseFirestore firebaseFirestore;
    FirebaseDatabase firebaseDatabase;
    String userID;
    Button btn_ok;
    AlertDialog showInternet;
    DocumentReference documentReference;
    DatabaseReference databaseReference;
    boolean haveInternet = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_user_loading_process);

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();
        firebaseDatabase = FirebaseDatabase.getInstance();

        Intent i = getIntent();
        String lastname = i.getStringExtra("lastname");
        String firstname = i.getStringExtra("firstname");
 //       String birthday = i.getStringExtra("birthday");
        String email = i.getStringExtra("email");
        String password = i.getStringExtra("password");

        AlertDialog.Builder builder = new AlertDialog.Builder(create_user_loading_process.this);
        builder.setCancelable(false);
        LayoutInflater layoutInflater = getLayoutInflater();
        View view = layoutInflater.inflate(R.layout.custom_no_internet, (ViewGroup) findViewById(R.id.custom_no_internet));
        showInternet = builder.create();

        builder.setView(view);
        btn_ok = view.findViewById(R.id.btn_ok);

        btn_ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showInternet.dismiss();
            }
        });


        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                checkingInternet();
                if (haveInternet) {
                    firebaseAuth.createUserWithEmailAndPassword(email,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()){
                                userID = firebaseAuth.getCurrentUser().getUid();
                                String longitude = "0.0";
                                String latitude = "0.0";
                                String signed = "notSinged";

                                String fullname = firstname +" "+ lastname;

                                Map<String,Object> user = new HashMap<>();
                                user.put("userID",userID);
                                user.put("fullname",fullname);
          //                      user.put("birthday",birthday);
                                user.put("email",email);
                                user.put("longitude",longitude);
                                user.put("latitude",latitude);
                                user.put("signed",signed);
                                user.put("permission","not");
                                user.put("password",password);

                                databaseReference = firebaseDatabase.getReference("User Information").child(userID);
                                databaseReference.setValue(user).addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void unused) {
                                        // Create a notification message
                                        String notification = "Welcome, " + fullname + "! You have successfully created an account. Our app is happy to serve you.";

                                        // date today
                                        Date current_Date = new Date();
                                        // date formate
                                        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("MMM dd 'at' hh:mm a", Locale.getDefault());
                                        String dateFormat = simpleDateFormat.format(current_Date);
                                        long timestamp = System.currentTimeMillis(); // Get the current timestamp

                                        Map<String, Object> insertNotification = new HashMap<>();
                                        insertNotification.put("notification", notification);
                                        insertNotification.put("date",dateFormat);
                                        insertNotification.put("timestamp",timestamp);

                                        // Save notification to Realtime Database
                                        databaseReference = firebaseDatabase.getReference("User Notification").child(userID);
                                        // Generate a new notification ID
                                        String notificationId = databaseReference.push().getKey();
                                        databaseReference.child(notificationId).setValue(insertNotification).addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void unused) {
                                                firebaseAuth.signOut();
                                                Intent i = new Intent(create_user_loading_process.this,create_user_successfull.class);
                                                i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP); // Just keep this flag
                                                finish();
                                                startActivity(i);
                                            }
                                        });
                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Toast.makeText(create_user_loading_process.this,e.getMessage(),Toast.LENGTH_SHORT).show();
                                    }
                                });

                                /* this is firebase firestore insert
                                documentReference = firebaseFirestore.collection("Users Information").document(userID);
                                documentReference.set(user).addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void unused) {

                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Toast.makeText(create_user_loading_process.this,e.getMessage(),Toast.LENGTH_SHORT).show();
                                    }
                                });*/
                            }
                        }
                    });
                }
            }
        },3000);
    }

    private void checkingInternet () {
        // initilize intent filter
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("android.net.conn.CONNECTIVITY_CHANGE");
        registerReceiver(new ConnectionReceiver(), intentFilter);
        ConnectivityManager connectivityManager = (ConnectivityManager) getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        boolean isConnected = networkInfo != null && networkInfo.isConnectedOrConnecting();
        showInternetResult (isConnected);
    }
    private void showInternetResult (boolean isConnected) {
        if (showInternet != null) {
            if (isConnected) {
                showInternet.dismiss();
                haveInternet = true;
            }else {
                showInternet.show();
                haveInternet = false;
            }
        }
    }
    @Override
    public void onNetworkChange (boolean isConnected) {
      //  showInternetResult(isConnected);
    }
    @Override
    protected void onResume () {
        super.onResume();
      //  checkingInternet();
    }
    @Override
    protected void onPause () {
        super.onPause();
       // checkingInternet();
    }
}