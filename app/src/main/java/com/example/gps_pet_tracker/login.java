package com.example.gps_pet_tracker;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthResult;
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
import java.time.LocalTime;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class login extends AppCompatActivity {
/*
* "You have 30 days remaining before your account is permanently deleted. Do you wish to proceed with the deletion, or would you like to keep your account active? Please let us know."
*
* */
    Button create_account, googleAuthentication, login, btn_ok, btn_tryAgain, btn_create;
    FirebaseAuth authentication;
    FirebaseFirestore firebaseFirestore;
    FirebaseDatabase database;
 //   GoogleSignInClient googleSignInClient;
   // int rc_sign_in = 20;
    TextView forgetPassword;
    AlertDialog show, showInternet, showCouldnotFindAccount,showLoggedAnotherDevice;
    boolean isInternetDialogVisible = false; // Flag to track if showInternet is visible
    long backPressed;
    EditText etEmail, etPassword;
    TextInputLayout emailContainer, passwordContainer;
    private static final int PERMISSION_REQUEST_CODE = 1;
    private boolean permissionss = true;
    boolean isValid = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // initialize widget
        create_account = findViewById(R.id.create_account);
       // googleAuthentication = findViewById(R.id.googleAuthentication);

        emailContainer = findViewById(R.id.emailContainer);
        passwordContainer = findViewById(R.id.passwordContainer);

        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);

        login = findViewById(R.id.login);
        forgetPassword = findViewById(R.id.forgetPassword);

        // firebase and google code
        authentication = FirebaseAuth.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();
        database = FirebaseDatabase.getInstance();

        AlertDialog.Builder builder = new AlertDialog.Builder(login.this);
        builder.setCancelable(false);
        builder.setView(R.layout.custom_loading_process);
        show = builder.create();

        AlertDialog.Builder internetCheck = new AlertDialog.Builder(login.this);
        LayoutInflater inflater = getLayoutInflater();
        View layoutcheck = inflater.inflate(R.layout.custom_no_internet, null);
        btn_ok = layoutcheck.findViewById(R.id.btn_ok);
        internetCheck.setView(layoutcheck);
        showInternet = internetCheck.create();

        AlertDialog.Builder showCouldFind = new AlertDialog.Builder(login.this);
        LayoutInflater inflater1 = getLayoutInflater();
        View view = inflater1.inflate(R.layout.could_find_account_login, null);
        showCouldFind.setView(view);
        showCouldnotFindAccount = showCouldFind.create();

        AlertDialog.Builder showLoggedDevice = new AlertDialog.Builder(login.this);
        showLoggedDevice.setCancelable(false);
        LayoutInflater inflater2 = getLayoutInflater();
        View view1 = inflater2.inflate(R.layout.custom_logged_into_another_device,null);
        showLoggedDevice.setView(view1);
        showLoggedAnotherDevice = showLoggedDevice.create();

        Button btn_ok_logged_device = view1.findViewById(R.id.btn_okk);
        btn_create = view.findViewById(R.id.btn_create);
        btn_tryAgain = view.findViewById(R.id.btn_tryAgain);

       if (permissionss) {
          // checkPermission();
       }

       btn_ok_logged_device.setOnClickListener(new View.OnClickListener() {
           @Override
           public void onClick(View view) {
               showLoggedAnotherDevice.dismiss();
           }
       });

        btn_create.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getApplicationContext(), com.example.gps_pet_tracker.create_account.class));
            }
        });

        btn_tryAgain.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showCouldnotFindAccount.dismiss();
            }
        });

        btn_ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (showInternet != null) {
                    showInternet.dismiss();
                }
                checkingInternet();
            }
        });

      /*  GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        googleSignInClient = GoogleSignIn.getClient(login.this, gso);

        googleAuthentication.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                googleSignIn();
            }
        });*/

        forgetPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(login.this, Find_your_account.class);
                startActivity(i);
            }
        });

        create_account.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (showInternet != null) {
                    showInternet.dismiss();
                }
                Intent intent = new Intent(login.this, create_account.class);
                startActivity(intent);
            }
        });

        etEmail.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                // This method is called to notify you that the characters within 'start' and 'start + before' are about to be replaced with new text with a length of 'after'.
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                // This method is called to notify you that somewhere within 'start' to 'start + before', the text has been replaced with new text that has a length of 'after'.
                emailContainer.setErrorEnabled(false);
            }

            @Override
            public void afterTextChanged(Editable editable) {
                // This method is called to notify you that somewhere within 'start' to 'start + before', the text has been replaced with new text that has a length of 'after'.
            }
        });

        etEmail.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean hasFocus) {
                if (hasFocus) {
                    emailContainer.setBoxStrokeColor(Color.rgb(112,112,112));
                    emailContainer.setHintTextColor(ColorStateList.valueOf(Color.BLACK));

                } else {
                    emailContainer.setBoxStrokeColor(Color.GRAY);
                    emailContainer.setHintTextColor(ColorStateList.valueOf(Color.GRAY));
                }
            }
        });

        etPassword.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                // This method is called to notify you that the characters within 'start' and 'start + before' are about to be replaced with new text with a length of 'after'.
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                // This method is called to notify you that somewhere within 'start' to 'start + before', the text has been replaced with new text that has a length of 'after'.
                passwordContainer.setErrorEnabled(false);
            }

            @Override
            public void afterTextChanged(Editable editable) {
                // This method is called to notify you that somewhere within 'start' to 'start + before', the text has been replaced with new text that has a length of 'after'.
            }
        });

        etPassword.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean hasFocus) {
                if (hasFocus) {
                    passwordContainer.setBoxStrokeColor(Color.rgb(112,112,112));
                    passwordContainer.setHintTextColor(ColorStateList.valueOf(Color.BLACK));
                } else {
                    passwordContainer.setBoxStrokeColor(Color.GRAY);
                    passwordContainer.setHintTextColor(ColorStateList.valueOf(Color.GRAY));
                }
            }
        });

        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = etEmail.getText().toString();
                String password = etPassword.getText().toString();

                if (TextUtils.isEmpty(email)) {
                    emailContainer.setError("Email cannot be blank");
                    isValid = false;
                } else {
                    if (!isValidEmail(email)) {
                        emailContainer.setError("Invalid email format");
                        isValid = false;
                    } else {
                        emailContainer.setErrorEnabled(false);
                        isValid = true;
                    }
                }
                if (TextUtils.isEmpty(password)) {
                    passwordContainer.setError("Password cannot be blank");
                    isValid = false;
                } else {
                    if (password.trim().length() < 8) {
                        passwordContainer.setError("Minimum 8 characters required");
                        isValid = false;
                    } else {
                        passwordContainer.setErrorEnabled(false);
                    }
                }

                if (isValid) {
                    if (show != null) {
                        show.show();
                    checkingInternet();
                    if (isInternetDialogVisible) {
                                authentication.signInWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                    @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()) {

                                    FirebaseUser firebaseUser = authentication.getCurrentUser();
                                    if (firebaseUser != null) {
                                        String userIdd= firebaseUser.getUid();
                                        // check if user already sign in other device
                                        DatabaseReference databaseReference = database.getReference("User Information").
                                                child(userIdd);
                                        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                if (snapshot.exists()) {

                                                    // Get the device name
                                                    String deviceName = Build.MODEL;

                                                    String signed = snapshot.child("signed").getValue(String.class);
                                                    if (signed.equals("signed")) {
                                                        show.dismiss();
                                                        //Toast.makeText(login.this,"Already Sign inm",Toast.LENGTH_SHORT).show();
                                                        showLoggedAnotherDevice.show();
                                                        authentication.signOut();
                                                    }else {
                                                        String updateSigned = "signed";
                                                        Map<String, Object> editInformation = new HashMap<>();
                                                        editInformation.put("signed",updateSigned);
                                                        editInformation.put("DeviceName",deviceName);

                                                        DatabaseReference databaseReference1 = database.getReference("User Information").child(userIdd);
                                                        databaseReference1.updateChildren(editInformation).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                            @Override
                                                            public void onSuccess(Void unused) {
                                                                // User is already logged in, navigate to Home activity
                                                                Intent i = new Intent(login.this, Home.class);
                                                                i.putExtra("selectMapUserNotEmpty","0");
                                                                startActivity(i);
                                                                finish(); // Finish current activity to prevent returning to this screen
                                                                show.dismiss();
                                                            }
                                                        });
                                                    }
                                                }else {
                                                    DatabaseReference databaseReference1 = database.getReference("User Archieve")
                                                            .child(userIdd);

                                                    databaseReference1.addListenerForSingleValueEvent(new ValueEventListener() {
                                                        @Override
                                                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                            if (snapshot.exists()) {
                                                                show.dismiss();
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
                                                                String timeDelete = snapshot.child("timeDelete").getValue(String.class);
                                                                String dateDelete = snapshot.child("dateDelete").getValue(String.class);

                                                                // Show a Toast message with the remaining days
                                                                AlertDialog.Builder builder1 = new AlertDialog.Builder(login.this);
                                                                View view3 = getLayoutInflater().inflate(R.layout.custom_delete_user_account,null);
                                                                builder1.setView(view3);

                                                                Button delete_continue = view3.findViewById(R.id.btn_delete_confirm);
                                                                Button delete_cancel = view3.findViewById(R.id.btn_delete_cancel);
                                                                TextView deleteTv = view3.findViewById(R.id.deleteTv);

                                                                AlertDialog delete = builder1.create();

                                                                try {
                                                                    // Parse the deletionDate string to a Date object
                                                                    SimpleDateFormat sdf = new SimpleDateFormat("yy/MM/dd");
                                                                    Date deletedDate = sdf.parse(dateDelete); // e.g., "24/10/24"

                                                                    // Get the current date
                                                                    Date currentDate = new Date(); // Get the current system date

                                                                    // Calculate the difference in milliseconds between the current date and the deletion date
                                                                    long diffInMillis = currentDate.getTime() - deletedDate.getTime();

                                                                    // Convert the difference to days (TimeUnit handles this in a human-readable format)
                                                                    long diffInDays = TimeUnit.MILLISECONDS.toDays(diffInMillis);

                                                                    // Calculate the remaining days from a 30-day countdown
                                                                    int remainingDays = 30 - (int) diffInDays;

                                                                    // If remaining days are 0 or less, the account has already been deleted or expired
                                                                    if (remainingDays > 0) {
                                                                        String display = "You have "+ remainingDays +" days remaining before your account is permanently deleted. Do you wish to proceed with the deletion, or would you like to keep your account active?";
                                                                        deleteTv.setText(display);
                                                                        delete.show();
                                                                    } else if (remainingDays == 0){
                                                                        // Get Time now
                                                                        LocalTime currentTime = LocalTime.now();
                                                                        // Format the time for military time (HH:mm)
                                                                        SimpleDateFormat militaryTimeFormatter  = new SimpleDateFormat("HH:mm");
                                                                        // Format the time for AM/PM
                                                                        SimpleDateFormat amPmTimeFormatter  = new SimpleDateFormat("hh:mm a");

                                                                        String time = "", timeNowFormatted = "";
                                                                        try {
                                                                            // Parse the military time (HH:mm) into a Date object
                                                                            Date parseTime = militaryTimeFormatter .parse(timeDelete);
                                                                            // Parse the time now
                                                                            timeNowFormatted = militaryTimeFormatter.format(currentTime); // Format current time into "HH:mm"
                                                                            // Convert the Date object into the 12-hour AM/PM format
                                                                            time = amPmTimeFormatter .format(parseTime);
                                                                        } catch (ParseException e) {
                                                                            e.printStackTrace(); // Log the error or show a user-friendly message
                                                                            time = "Unknown time"; // Fallback in case of error
                                                                        }

                                                                        if (timeNowFormatted.equals(timeDelete)) {
                                                                            firebaseUser.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                                @Override
                                                                                public void onSuccess(Void unused) {
                                                                                    DatabaseReference databaseReference8  = database.getReference("Pet Information")
                                                                                            .child(userIDd);

                                                                                    databaseReference8.addListenerForSingleValueEvent(new ValueEventListener() {
                                                                                        @Override
                                                                                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                                                            if (snapshot.exists()) {
                                                                                                databaseReference8.removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                                                    @Override
                                                                                                    public void onSuccess(Void unused) {

                                                                                                    }
                                                                                                });
                                                                                            } else {
                                                                                                DatabaseReference databaseReference9 = database.getReference("User Archieve")
                                                                                                        .child(userIDd);

                                                                                                databaseReference9.addListenerForSingleValueEvent(new ValueEventListener() {
                                                                                                    @Override
                                                                                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                                                                        if (snapshot.exists()) {
                                                                                                            databaseReference9.removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                                                                @Override
                                                                                                                public void onSuccess(Void unused) {
                                                                                                                    DatabaseReference databaseReference2 = database.getReference("User Notification")
                                                                                                                            .child(userIDd);

                                                                                                                    databaseReference2.removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                                                                        @Override
                                                                                                                        public void onSuccess(Void unused) {

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
                                                                                        }

                                                                                        @Override
                                                                                        public void onCancelled(@NonNull DatabaseError error) {

                                                                                        }
                                                                                    });
                                                                                }
                                                                            });
                                                                        } else {
                                                                            String message = "Your account will be deleted today at " + time + ". Do you wish to proceed with the deletion, or would you like to keep your account active?";
                                                                            deleteTv.setText(message);
                                                                            delete.show();
                                                                        }

                                                                    }

                                                                } catch (Exception e) {
                                                                    e.printStackTrace(); // Handle parsing exceptions
                                                                    System.out.println("Error parsing deletion date.");
                                                                }

                                                                delete_continue.setOnClickListener(new View.OnClickListener() {
                                                                    @Override
                                                                    public void onClick(View view) {
                                                                        delete.dismiss();
                                                                        authentication.signOut();
                                                                    }
                                                                });

                                                                delete_cancel.setOnClickListener(new View.OnClickListener() {
                                                                    @Override
                                                                    public void onClick(View view) {
                                                                        delete.dismiss();

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

                                                                        DatabaseReference databaseReference71 = database.getReference("User Information")
                                                                                .child(userIdd);

                                                                        databaseReference71.updateChildren(insertArchive).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                            @Override
                                                                            public void onSuccess(Void unused) {

                                                                                //delete the user info on firebase
                                                                                databaseReference1.removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                    @Override
                                                                                    public void onComplete(@NonNull Task<Void> task) {
                                                                                        if (task.isSuccessful()) {
                                                                                            String deviceName = Build.MODEL;
                                                                                            String updateSigned = "signed";
                                                                                            Map<String, Object> editInformation = new HashMap<>();
                                                                                            editInformation.put("signed",updateSigned);
                                                                                            editInformation.put("DeviceName",deviceName);

                                                                                            DatabaseReference databaseReference12 = database.getReference("User Information").child(userIdd);
                                                                                            databaseReference12.updateChildren(editInformation).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                                                @Override
                                                                                                public void onSuccess(Void unused) {
                                                                                                    // User is already logged in, navigate to Home activity
                                                                                                    Intent i = new Intent(login.this, Home.class);
                                                                                                    i.putExtra("selectMapUserNotEmpty","0");
                                                                                                    startActivity(i);
                                                                                                    finish(); // Finish current activity to prevent returning to this screen
                                                                                                    show.dismiss();
                                                                                                }
                                                                                            });
                                                                                        }
                                                                                    }
                                                                                });
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
                                            }

                                            @Override
                                            public void onCancelled(@NonNull DatabaseError error) {

                                            }
                                        });
                                    }
                                } else {
                                        show.dismiss();
                                }
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                show.dismiss();
                                Log.e("Errror", e.getMessage());
                                showCouldnotFindAccount.show();
                            }
                        });
                    }
                }}
            }
        });

    }

// this method is use for multiple user permission
    private void checkPermission() {
        if (permissionss) {
            if (ActivityCompat.checkSelfPermission(this,Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED ||
                    ActivityCompat.checkSelfPermission(this,Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                PermissionMessage ();
            }else if (ActivityCompat.checkSelfPermission(this,Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED){
                PermissionMessage();
            }else if (ActivityCompat.checkSelfPermission(this,Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                PermissionMessage();
            }
        }

        permissionss = false;
    }

    private boolean hasPermission(String[] permissions) {
        for (String permission : permissions) {
            if (ContextCompat.checkSelfPermission(login.this, permission) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    private void PermissionMessage (){
        // Show explanation dialog if needed
        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert.setCancelable(true);
        View view = getLayoutInflater().inflate(R.layout.custom_permission_alert,null);
        alert.setView(view);

        AlertDialog alertt = alert.create();

        alertt.show();

        Button ok = view.findViewById(R.id.btn_set_now);
        ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                alertt.dismiss();
                String[] permissions;
                permissions = new String[]{
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION,
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.CAMERA
                };

                if (!hasPermission(permissions)) {
                    // Directly request permissions.
                    ActivityCompat.requestPermissions(login.this, permissions, PERMISSION_REQUEST_CODE);
                }
            }
        });
    }
    // this method is use for multiple user permission

    // Function to validate email using regex
    private boolean isValidEmail(CharSequence target) {
        return !TextUtils.isEmpty(target) && Patterns.EMAIL_ADDRESS.matcher(target).matches();
    }

    /*get google account
    public void googleSignIn() {
        Intent intent = googleSignInClient.getSignInIntent();
        startActivityForResult(intent, rc_sign_in);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == rc_sign_in) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);

            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                firebaseAuth(account.getIdToken());
            } catch (Exception e) {
                String errorMessage = task.getException().getMessage(); // Get the error message
                Log.e("FirebaseAuth", "Firebase authentication failed: " + errorMessage);

                Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
            }
        }
    }

    private void firebaseAuth(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        authentication.signInWithCredential(credential)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            FirebaseUser user = authentication.getCurrentUser();
                            HashMap<String, Object> map = new HashMap<>();
                            map.put("id", user.getUid());
                            map.put("name", user.getDisplayName());
                            map.put("profile", user.getPhotoUrl().toString());

                            database.getReference().child("users").child(user.getUid()).setValue(map);

                            Intent intent = new Intent(login.this, Home.class);
                            startActivity(intent);
                        } else {
                            Toast.makeText(login.this, "Try to connect internet", Toast.LENGTH_LONG).show();
                            String errorMessage = task.getException().getMessage(); // Get the error message
                            Log.e("FirebaseAuth", "Firebase authentication failed: " + errorMessage);
                        }
                    }
                });
    }*/

    private void checkingInternet() {
        // initialize intent filter
        IntentFilter intentFilter = new IntentFilter();
        // add action
        intentFilter.addAction("android.net.conn.CONNECTIVITY_CHANGE");
        // register receiver
        this.registerReceiver(new ConnectionReceiver(), intentFilter);
        // initialize listener
        ConnectivityManager connectivityManager = (ConnectivityManager) this.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        boolean isConnected = networkInfo != null && networkInfo.isConnectedOrConnecting();
        showResultsInternet(isConnected);
    }

    private void showResultsInternet(boolean isConnected) {
        if (showInternet != null) {
            if (isConnected) {
                showInternet.dismiss();
                isInternetDialogVisible = true;
            } else {
                showInternet.show();
                isInternetDialogVisible = false;
            }
        } else {
            Log.e("AlertDialog", "showInternet AlertDialog is null");
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        checkingInternet();
        if (!permissionss) {
            permissionss  = false;
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (!permissionss) {
            permissionss = false;
        }
    }
    @Override
    protected void onDestroy () {
        super.onDestroy();
    }

    @Override
    public void onBackPressed(){
     //   if (backPressed + 2000 > System.currentTimeMillis()){
            super.onBackPressed();
       // }else {
       ///     Toast.makeText(this,"Press agan to exit",Toast.LENGTH_SHORT).show();
      //  }
      //  backPressed = System.currentTimeMillis();
    }
}

/*



 */
