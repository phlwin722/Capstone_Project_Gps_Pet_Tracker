package com.example.gps_pet_tracker;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Random;

public class email extends AppCompatActivity implements ConnectionReceiver.ReceiverListener{
    TextInputLayout inputLayoutEmail;
    EditText etEmail;
    Button btn_nextpage, btn_ok;
    TextView btn_backpage, haveAlreadyAnAccount;
    String generateCode, userEmail, lastname, firstname, birthday, email, inputlayoutEmail;
    FirebaseAuth firebaseAuth;
    FirebaseUser firebaseUser;
    FirebaseFirestore firebaseFirestore;
    int codee = 0;
    FirebaseDatabase firebaseDatabase;
    AlertDialog showInternet, showLoading;
    boolean isInternetDialogVisible = false; // Flag to track if showInternet is visible

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_email);

        inputLayoutEmail = findViewById(R.id.inputLayoutEmail);
        etEmail = findViewById(R.id.etEmail);
        btn_nextpage = findViewById(R.id.nextPage);
        btn_backpage = findViewById(R.id.btn_backPage);
        haveAlreadyAnAccount = findViewById(R.id.haveAlreadyAnAccount);

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseUser = firebaseAuth.getCurrentUser();
        firebaseFirestore = FirebaseFirestore.getInstance();
        firebaseDatabase = FirebaseDatabase.getInstance();


        Intent i = getIntent();
         lastname = i.getStringExtra("lastname");
         firstname = i.getStringExtra("firstname");
       //  birthday = i.getStringExtra("birthday");
         email = i.getStringExtra("email");
         inputlayoutEmail = i.getStringExtra("inputLayoutEmail");

        AlertDialog.Builder checkInternet = new AlertDialog.Builder(com.example.gps_pet_tracker.email.this);
        LayoutInflater layoutInflater = getLayoutInflater();
        View layoutcheck = layoutInflater.inflate(R.layout.custom_no_internet, (ViewGroup) findViewById(R.id.custom_no_internet));
        checkInternet.setView(layoutcheck);

        btn_ok = layoutcheck.findViewById(R.id.btn_ok);

        showInternet = checkInternet.create();

        AlertDialog.Builder showLoadInterface = new AlertDialog.Builder(email.this);
        showLoadInterface.setCancelable(false);
        showLoadInterface.setView(R.layout.custom_loading_process);

        showLoading = showLoadInterface.create();

        haveAlreadyAnAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(email.this, login.class));
            }
        });

        btn_ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showInternet.dismiss();
            }
        });


        if (email != null) {
            etEmail.setText(email);
        }
        if (inputlayoutEmail != null) {
            inputLayoutEmail.setError(inputlayoutEmail);
        }

        btn_backpage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(email.this, create_account.class);
                i.putExtra("lastname", lastname);
                i.putExtra("firstname", firstname);
                i.putExtra("birthday", birthday);
                startActivity(i);
            }
        });

        etEmail.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                inputLayoutEmail.setErrorEnabled(false);
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        etEmail.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean hasFocus) {
                if (hasFocus) {
                    inputLayoutEmail.setBoxStrokeColor(Color.rgb(112,112,112));
                    inputLayoutEmail.setHintTextColor(ColorStateList.valueOf(Color.BLACK));
                } else {
                    inputLayoutEmail.setBoxStrokeColor(Color.GRAY);
                    inputLayoutEmail.setHintTextColor(ColorStateList.valueOf(Color.GRAY));
                }
            }
        });

        btn_nextpage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                boolean isValid = true;
                showLoading.show();
                if (etEmail.getText().toString().isEmpty()) {
                    inputLayoutEmail.setError("Email cannot be blank");
                    isValid = false;
                    showLoading.dismiss();
                } else {
                    if (!isValidEmail((etEmail.getText().toString()))) {
                        inputLayoutEmail.setError("Invalid email format");
                        showLoading.dismiss();
                        isValid = false;
                    }else {
                        inputLayoutEmail.setErrorEnabled(false);
                        isValid = true;
                    }
                }

                if (isValid) {
                   checkInternet();
                       if (isInternetDialogVisible) {
                           showInternet.dismiss();
                           userEmail = etEmail.getText().toString().trim();// Replace with the actual email

                           DatabaseReference databaseReference = firebaseDatabase.getReference("User Information");
                           databaseReference.orderByChild("email").equalTo(userEmail).addListenerForSingleValueEvent(new ValueEventListener() {
                               @Override
                               public void onDataChange(@NonNull DataSnapshot snapshot) {
                                   if (snapshot.exists()) {
                                       inputLayoutEmail.setError("Email already existing");
                                       showLoading.dismiss();
                                   }else{
                                       inputLayoutEmail.setErrorEnabled(false);
                                       // Email does not exist in authentication
                                       showLoading.dismiss();
                                       proceedWithRegistration(lastname,firstname,birthday);
                                   }
                               }

                               @Override
                               public void onCancelled(@NonNull DatabaseError error) {

                               }
                           });

                           /* firebaseFirestore.collection("Users Information")
                                   .whereEqualTo("email",userEmail)
                                   .get()
                                   .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                       @Override
                                       public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                           if (task.isSuccessful()) {
                                               if(!task.getResult().isEmpty()){
                                                   inputLayoutEmail.setError("Email already existing");
                                                   showLoading.dismiss();
                                               }else{
                                                   inputLayoutEmail.setErrorEnabled(false);
                                                   // Email does not exist in authentication
                                                   showLoading.dismiss();
                                                   proceedWithRegistration(lastname,firstname,birthday);
                                               }

                                           }else {
                                               // Email does not exist in authentication
                                               showLoading.dismiss();
                                               // Email does not exist in authentication
                                               proceedWithRegistration(lastname,firstname,birthday);
                                           }
                                       }
                                   }).addOnFailureListener(new OnFailureListener() {
                                       @Override
                                       public void onFailure(@NonNull Exception e) {
                                           Toast.makeText(email.this,e.getMessage(),Toast.LENGTH_SHORT).show();
                                       }
                                   });*/
                    /* firebaseAuth.fetchSignInMethodsForEmail(userEmail)
                            .addOnCompleteListener(task -> {
                                if (task.isSuccessful()) {
                                    SignInMethodQueryResult result = task.getResult();
                                    List<String> signInMethods = result.getSignInMethods();
                                    if (signInMethods != null && !signInMethods.isEmpty()) {
                                        // Email exists in authentication
                                        Log.d("TAG", "Email exists in authentication.");
                                    } else {
                                        // Email does not exist in authentication
                                        proceedWithRegistration(lastname,firstname,birthday);
                                    }
                                } else {
                                    // Error occurred while checking email existence
                                    Log.e("TAG", "Error checking email existence: " + task.getException());
                                }
                            }); */
                       }
                }
            }
        });
    }

    // Function to validate email using regex
    private boolean isValidEmail(CharSequence target) {
        return !TextUtils.isEmpty(target) && android.util.Patterns.EMAIL_ADDRESS.matcher(target).matches();
    }


    // Proceed with sending verification code and moving to the next activity
    private void proceedWithRegistration(String lastname, String firstname, String birthday) {
        if (showInternet != null) {
            // Generate verification code
            generateCode = generateCode();

            // Send verification code via email
            sendEmail(etEmail.getText().toString().trim(), "Verification Code", generateCode);

            // Move to the next activity
            Intent i = new Intent(email.this, email_verification.class);
            i.putExtra("lastname", lastname);
            i.putExtra("firstname", firstname);
         //   i.putExtra("birthday", birthday);
            i.putExtra("email", etEmail.getText().toString());
            i.putExtra("verificationCode", String.valueOf(codee));
            startActivity(i);
        }
    }

    // Generate verification code
    private String generateCode() {
        // generate random 6-digit code
        Random random = new Random();
        int code = random.nextInt(900000) + 100000; // Ensure the code is always 6 digits
        codee = code;
        return String.valueOf(code) + " is your authentication code. For your protection, do not share this code with anyone";
    }

    // Send email
    private void sendEmail(String recipientEmail, String subject, String message) {
        final String username = "gpspetracker08@gmail.com";
        final String password = "Gleepogi@1";

        JavaMailAPI javaMailAPI = new JavaMailAPI(email.this, recipientEmail, subject, message);
        javaMailAPI.execute();
    }
    // checking internet
    private void checkInternet () {
        // initialize intent filter
        IntentFilter intentFilter = new IntentFilter();
        // add action
        intentFilter.addAction("android.net.conn.CONNECTIVITY_CHANGE");
        // register reciever
        registerReceiver(new ConnectionReceiver(),intentFilter);
        // initialize listener
        ConnectivityManager connectivityManager = (ConnectivityManager) getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        // initialize network info
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        // get connection status
        boolean isConnected = networkInfo != null && networkInfo.isConnectedOrConnecting();
        // display the message if no internet
        showResultInternet(isConnected);
    }

    private void showResultInternet(boolean isConnected) {
        if (showInternet != null) {
            if (isConnected) {
                showInternet.dismiss();
                isInternetDialogVisible = true;
            }else {
                showInternet.show();
                isInternetDialogVisible = false;
            }
        }
    }
    @Override
    public void onNetworkChange (boolean isConnected) {
        showResultInternet(isConnected);
    }

    @Override
    protected void onResume () {
        super.onResume();
        checkInternet();
    }

    @Override
    protected void onPause () {
        super.onPause();
        checkInternet();
    }

    @Override
    public void onBackPressed () {
        super.onBackPressed();
        Intent intent = new Intent(email.this,login.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP); // Just keep this flag
        startActivity(intent);
        finish();
    }
}
