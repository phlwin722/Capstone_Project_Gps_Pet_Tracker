package com.example.gps_pet_tracker;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
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
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

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

public class change_password extends AppCompatActivity implements ConnectionReceiver.ReceiverListener {
    Button forgetPassword, savePassword, btn_ok ,btn_backPagee,btn_backPage;
    EditText newPassword, currentPassword, rePassword;
    String userId, Email, currentpass,newpass,repass;
    AlertDialog show, InternetDialog,ChangePAss;
    FirebaseAuth firebaseAuth;
    FirebaseUser user ;
    FirebaseDatabase firebaseDatabase;
    String changePassword = "";
    boolean isValid = true;
    boolean isInternetDialogVisible = false;
    TextInputLayout inputlayoutCurrentpass, inputlayoutNewpass, inputlayoutRepass;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_change_password);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        savePassword = findViewById(R.id.savePassword);
        forgetPassword = findViewById(R.id.forgetPassword);
        newPassword = findViewById(R.id.newPassword);
        rePassword = findViewById(R.id.rePassword);
        currentPassword = findViewById(R.id.currentPassword);
        inputlayoutRepass = findViewById(R.id.inputlayoutRepass);
        inputlayoutNewpass = findViewById(R.id.inputlayoutNewpass);
        inputlayoutCurrentpass = findViewById(R.id.inputlayoutCurrentpass);
        btn_backPage = findViewById(R.id.btn_backPage);
        btn_backPagee = findViewById(R.id.btn_backPagee);

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseDatabase = FirebaseDatabase.getInstance();
        userId = firebaseAuth.getCurrentUser().getUid();
        user = firebaseAuth.getCurrentUser();

        // process bar
        AlertDialog.Builder builder = new AlertDialog.Builder(change_password.this);
        builder.setCancelable(false);
        builder.setView(R.layout.custom_loading_process);
        show = builder.create();

        // show no internet connection
        AlertDialog.Builder showInternetDialog = new AlertDialog.Builder(change_password.this);
        LayoutInflater inflater = getLayoutInflater();
        View view = inflater.inflate(R.layout.custom_no_internet, (ViewGroup) findViewById(R.id.custom_no_internet));
        showInternetDialog.setView(view);
        InternetDialog = showInternetDialog.create();

        AlertDialog.Builder builder1 = new AlertDialog.Builder(change_password.this);
        View view1 = getLayoutInflater().inflate(R.layout.passwordchangesuccess,null);
        builder1.setCancelable(false);
        builder1.setView(view1);
        ChangePAss = builder1.create();

        Button logOut = view1.findViewById(R.id.btn_logout_confirm);
        Button staySign = view1.findViewById(R.id.btn_stay_in);

        Intent i = getIntent();
        changePassword = i.getStringExtra("changePassword");
        Email = i.getStringExtra("Email");

        if (changePassword != null) {
            ChangePAss.show();
            logOut.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    ChangePAss.dismiss();
                    Intent intent = new Intent(change_password.this,Home.class);
                    intent.putExtra("PasswordUpdate","PasswordUpdate");
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP); // Just keep this flag
                    startActivity(intent);
                    finish();
                }
            });
            staySign.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    ChangePAss.dismiss();
                    changePassword = "";
                }
            });
        }

        DatabaseReference databaseReference = firebaseDatabase.getReference("User Information").child(userId);

        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    Email = snapshot.child("email").getValue(String.class);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        btn_ok = view.findViewById(R.id.btn_ok);

        newPassword.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                inputlayoutNewpass.setErrorEnabled(false);
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        currentPassword.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                inputlayoutCurrentpass.setErrorEnabled(false);
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        rePassword.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                inputlayoutRepass.setErrorEnabled(false);
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        btn_ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                InternetDialog.dismiss();
            }
        });

        forgetPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(change_password.this, forget_change_password.class);
                startActivity(i);
            }
        });

        btn_backPagee.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                backtosetting();
            }
        });

        btn_backPage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                backtosetting();
            }
        });
        savePassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                currentpass = currentPassword.getText().toString();
                newpass = newPassword.getText().toString();
                repass = rePassword.getText().toString();

                if (TextUtils.isEmpty(currentpass)) {
                    inputlayoutCurrentpass.setError("Field cannot be blank");
                    isValid = false;
                }else {
                    inputlayoutCurrentpass.setErrorEnabled(false);
                    isValid = true;
                }

                if (TextUtils.isEmpty(newpass)) {
                    inputlayoutNewpass.setError("Field cannot be blank");
                    isValid = false;
                }else {
                    if (newpass.length() < 8) {
                        inputlayoutNewpass.setError("Password must be at least 8 characters");
                        isValid = false;
                    }else {
                        inputlayoutNewpass.setErrorEnabled(false);
                        if (containsDisallowedSymbol(newpass)) {
                            inputlayoutNewpass.setError("Password must contain the symbols @#$!&*^%$");
                            isValid = false;
                        }else {
                            inputlayoutNewpass.setErrorEnabled(false);
                            isValid = true;
                        }
                    }
                }

                if (TextUtils.isEmpty(repass)) {
                    inputlayoutRepass.setError("Field cannot be blank");
                    isValid = false;
                }else {
                    if (repass.length() < 8) {
                        inputlayoutRepass.setError("Password must be at least 8 characters");
                        isValid = false;
                    }else {
                        inputlayoutRepass.setErrorEnabled(false);
                        if (containsDisallowedSymbol(repass)) {
                            inputlayoutRepass.setError("Password must contain the symbols @#$!&*^%$");
                            isValid = false;
                        }else {
                            inputlayoutRepass.setErrorEnabled(false);
                        }
                    }
                }

                if (isValid) {
                    checkInternet();
                    if (isInternetDialogVisible) {
                        firebaseAuth.signInWithEmailAndPassword(Email, currentpass).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()) {
                                  if (newpass.equals(repass)) {
                                      show.show();
                                      updatePassword(newpass);
                                  }else {
                                      inputlayoutNewpass.setError("The new password does not match the repeated password.");
                                      inputlayoutRepass.setError("The repeated password does not match the new password.");
                                  }
                                } else {
                                    inputlayoutCurrentpass.setError("Enter correct password");

                                }
                            }
                        });
                    }
                } else {
                    // Handle validation errors if necessary
                }
            }
        });

    }
    public void updatePassword (String password){
            user.updatePassword(password).addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void unused) {
                    // Password updated successfully

                    Map<String, Object> userr = new HashMap<>();
                    userr.put("password",password);
                    DatabaseReference databaseReference = firebaseDatabase.getReference("User Information")
                            .child(firebaseAuth.getCurrentUser().getUid());

                    databaseReference.updateChildren(userr).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void unused) {
                            Intent intent = new Intent(change_password.this,Sucessfull_update.class);
                            intent.putExtra("changePassword","changePassword");
                            intent.putExtra("Email",Email);
                            show.dismiss();
                            startActivity(intent);
                        }
                    });

                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    // Handle failure to update password
                    Toast.makeText(getApplicationContext(), "Failed to update password: " + e.getMessage(), Toast.LENGTH_SHORT).show();

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

    // back to the settings
    private void backtosetting () {
        Intent i = new Intent(change_password.this,Home.class);
        i.putExtra("change_password","change_password");
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP); // Just keep this flagi
        startActivity(i);
        finish();
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
                InternetDialog.show();
                isInternetDialogVisible = false;
            }
        }
    }
    @Override
    public void onNetworkChange (boolean isConnected) {
        showResultsInternet(isConnected);
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
        Intent intent = new Intent(change_password.this,Home.class);
        intent.putExtra("SafeZone","SafeZone");
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP); // Just keep this flag
        startActivity(intent);
        finish();
    }
}