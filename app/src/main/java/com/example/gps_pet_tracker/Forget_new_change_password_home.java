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
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

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

import java.util.HashMap;
import java.util.Map;

public class Forget_new_change_password_home extends AppCompatActivity {

    TextView btn_backPage;
    String Email, bck_findyouremailVerify;
    Button nextPage, btn_Backpagee;
    FirebaseDatabase firebaseDatabase;
    TextInputLayout inputLayoutPassword, inputLayoutRetypePassword;
    EditText etPassword, etRetypepassword;
    String password, retypepass, Verificationcode;

    FirebaseAuth firebaseAuth;
    FirebaseUser user;
    AlertDialog show, InternetDialog;
    boolean isInternetDialogVisible = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forget_new_change_password_home);

        btn_Backpagee = findViewById(R.id.btn_backPagee);
        btn_backPage = findViewById(R.id.btn_backPage);
        nextPage = findViewById(R.id.nextPage);
        etRetypepassword = findViewById(R.id.etRetypepassword);
        etPassword = findViewById(R.id.etPassword);
        inputLayoutRetypePassword = findViewById(R.id.inputLayoutRetypePassword);
        inputLayoutPassword = findViewById(R.id.inputLayoutPassword);

        // show no internet connection
        AlertDialog.Builder showInternetDialog = new AlertDialog.Builder(Forget_new_change_password_home.this);
        LayoutInflater inflater = getLayoutInflater();
        View view = inflater.inflate(R.layout.custom_no_internet, (ViewGroup) findViewById(R.id.custom_no_internet));
        showInternetDialog.setView(view);
        InternetDialog = showInternetDialog.create();

        Button btn_ok = view.findViewById(R.id.btn_ok);

        btn_ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                InternetDialog.dismiss();
            }
        });

        // process bar
        AlertDialog.Builder builder = new AlertDialog.Builder(Forget_new_change_password_home.this);
        builder.setCancelable(false);
        builder.setView(R.layout.custom_loading_process);
        show = builder.create();

        Intent i = getIntent();
        Email = i.getStringExtra("Email");
        bck_findyouremailVerify = i.getStringExtra("back");
        Verificationcode = i.getStringExtra("Verificationcode");

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseDatabase = FirebaseDatabase.getInstance();
        user = firebaseAuth.getCurrentUser();

        etPassword.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                inputLayoutPassword.setErrorEnabled(false);
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        etPassword.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean hasFocus) {
                if (hasFocus) {
                    inputLayoutPassword.setBoxStrokeColor(Color.rgb(112,112,112));
                    inputLayoutPassword.setHintTextColor(ColorStateList.valueOf(Color.BLACK));
                } else {
                    inputLayoutPassword.setBoxStrokeColor(Color.GRAY);
                    inputLayoutPassword.setHintTextColor(ColorStateList.valueOf(Color.GRAY));
                }
            }
        });

        etRetypepassword.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                inputLayoutRetypePassword.setErrorEnabled(false);
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        etRetypepassword.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean hasFocus) {
                if (hasFocus) {
                    inputLayoutRetypePassword.setBoxStrokeColor(Color.rgb(112,112,112));
                    inputLayoutRetypePassword.setHintTextColor(ColorStateList.valueOf(Color.BLACK));
                } else {
                    inputLayoutRetypePassword.setBoxStrokeColor(Color.GRAY);
                    inputLayoutRetypePassword.setHintTextColor(ColorStateList.valueOf(Color.GRAY));
                }
            }
        });

        btn_Backpagee.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(Forget_new_change_password_home.this, forget_password_change_password_verification.class);
                i.putExtra("Email", Email);
                i.putExtra("Verificationcode",Verificationcode);
                i.putExtra("back", bck_findyouremailVerify);
                startActivity(i);
                finish();
            }
        });

        btn_backPage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(Forget_new_change_password_home.this, forget_password_change_password_verification.class);
                i.putExtra("Email", Email);
                i.putExtra("back", bck_findyouremailVerify);
                i.putExtra("Verificationcode",Verificationcode);
                startActivity(i);
                finish();
            }
        });

        nextPage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                boolean change = true;

                retypepass = etRetypepassword.getText().toString();
                password = etPassword.getText().toString();

                if (password.isEmpty()) {
                    inputLayoutPassword.setError("Field cannot be blank");
                    change = false;
                } else {
                    inputLayoutPassword.setErrorEnabled(false);
                    if(password.length() < 8) {
                        inputLayoutPassword.setError("Password must be at least 8 characters");
                    }else {
                        if (!password.equals(retypepass)) {
                            inputLayoutPassword.setError("Password does not match retype password");
                            change = false;
                        }else {
                            inputLayoutPassword.setErrorEnabled(false);
                            if (containsDisallowedSymbol(password)) {
                                inputLayoutRetypePassword.setError("Password must contain the symbols @#$!&*^%$");
                                change = false;
                            }else {
                                inputLayoutRetypePassword.setErrorEnabled(false);
                                change = true;
                            }
                        }
                    }
                }

                if (retypepass.isEmpty()) {
                    inputLayoutRetypePassword.setError("Field cannot be blank");
                    change = false;
                } else {
                    inputLayoutRetypePassword.setErrorEnabled(false);
                    if(retypepass.length() < 8) {
                        inputLayoutRetypePassword.setError("Password must be at least 8 characters");
                        change = false;
                    }else {
                        if (!retypepass.equals(password)) {
                            inputLayoutRetypePassword.setError("Password does not match password");
                        }else {
                            inputLayoutRetypePassword.setErrorEnabled(false);
                            if (containsDisallowedSymbol(retypepass)) {
                                inputLayoutRetypePassword.setError("Password must contain the symbols @#$!&*^%$");
                                change = false;
                            }else {
                                inputLayoutRetypePassword.setErrorEnabled(false);
                            }
                        }
                    }
                }

                if (change) {
                       show.show();
                       checkInternet();
                       if (isInternetDialogVisible) {
                           updatePassword(password);
                       }
                }
            }
        });
    }

    private boolean containsDisallowedSymbol(String password) {
        // Define the disallowed symbols
        String disallowedSymbols = "@#$!&*^%$";

        // Iterate through each character in the password
        for (int i = 0; i < password.length(); i++) {
            char ch = password.charAt(i);

            // Check if the character is a disallowed symbol
            if (disallowedSymbols.indexOf(ch)!= -1) {
                return false; // Disallowed symbol found
            }
        }

        return true; // No disallowed symbol found
    }

    private void updatePassword(String newPassword) {

        DatabaseReference databaseReference = firebaseDatabase.getReference("User Information")
                .child(firebaseAuth.getCurrentUser().getUid());

        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String password = snapshot.child("password").getValue(String.class);
                    if (password != null ){
                        firebaseAuth.signInWithEmailAndPassword(Email,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()) {
                                    FirebaseUser user = firebaseAuth.getCurrentUser(); // Get current authenticated user
                                    if (user != null) {
                                        // User is logged in, proceed to update password
                                        Log.e("UserEmail", "Current user: " + user.getEmail()); // Log the user's email

                                        user.updatePassword(newPassword)
                                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                    @Override
                                                    public void onSuccess(Void unused) {
                                                        Map<String, Object> userr = new HashMap<>();
                                                        userr.put("password",newPassword);

                                                        databaseReference.updateChildren(userr).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                            @Override
                                                            public void onSuccess(Void unused) {
                                                                // Successfully updated the password
                                                                Log.e("PasswordUpdate", "Password updated successfully.");
                                                                Intent intent = new Intent(Forget_new_change_password_home.this, Sucessfull_update.class);
                                                                intent.putExtra("changePassword","changePassword");
                                                               show.dismiss();
                                                                startActivity(intent);
                                                                finish();
                                                            }
                                                        });
                                                    }
                                                })
                                                .addOnFailureListener(new OnFailureListener() {
                                                    @Override
                                                    public void onFailure(@NonNull Exception e) {
                                                        // Handle failure to update password
                                                        Log.e("PasswordUpdate", "Failed to update password: " + e.getMessage());
                                                        Toast.makeText(getApplicationContext(), "Failed to update password: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                                    }
                                                });
                                    } else {
                                        // User is not logged in
                                        Log.e("PasswordUpdate", "User is not logged in.");
                                        Toast.makeText(getApplicationContext(), "User is not logged in.", Toast.LENGTH_SHORT).show();
                                    }
                                }
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

    private void checkInternet () {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("android.net.conn.CONNECTIVITY_CHANGE");
        registerReceiver(new ConnectionReceiver(),intentFilter);
        ConnectivityManager connectivityManager = (ConnectivityManager) getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        boolean isConnected = networkInfo != null && networkInfo.isConnectedOrConnecting();
        showResultsInternet (isConnected);
    }
    private void showResultsInternet(boolean isConnected) {
        if (InternetDialog != null) {
            if (isConnected) {
                isInternetDialogVisible = true;
                InternetDialog.dismiss();
            }else {
                show.dismiss();
                InternetDialog.show();
                isInternetDialogVisible = false;
            }
        }
    }

}
