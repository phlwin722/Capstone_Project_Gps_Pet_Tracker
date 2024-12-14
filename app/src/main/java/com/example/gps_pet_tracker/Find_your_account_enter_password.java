package com.example.gps_pet_tracker;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.bumptech.glide.Glide;
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
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.HashMap;
import java.util.Map;

public class Find_your_account_enter_password extends AppCompatActivity {

    String Email, password, fullname, userID;
    Button btn_backPage, btn_nextPage, btn_tryAgin, btn_getCode;
    EditText etPassword;
    TextInputLayout inputLayoutPassword;
    ImageView imageProfile;
    TextView tvFullname;
    AlertDialog showLoggedAnotherDevice;
    AlertDialog show;
    FirebaseDatabase firebaseDatabase;

    FirebaseAuth firebaseAuth;
    StorageReference storageReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_find_your_account_enter_password);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        firebaseAuth = FirebaseAuth.getInstance();
        storageReference = FirebaseStorage.getInstance().getReference();
        firebaseDatabase = FirebaseDatabase.getInstance();

        tvFullname = findViewById(R.id.tvFullname);
        btn_backPage = findViewById(R.id.btn_backPage);
        btn_nextPage = findViewById(R.id.btn_nextPage);
        etPassword = findViewById(R.id.etPassword);
        inputLayoutPassword = findViewById(R.id.inputLayoutPassword);
        imageProfile = findViewById(R.id.imageProfile);

        Intent i = getIntent();
        Email = i.getStringExtra("Email");
        fullname = i.getStringExtra("fullname");
        userID = i.getStringExtra("userID");

        AlertDialog.Builder showLoggedDevice = new AlertDialog.Builder(Find_your_account_enter_password.this);
        showLoggedDevice.setCancelable(false);
        LayoutInflater inflater2 = getLayoutInflater();
        View view1 = inflater2.inflate(R.layout.custom_logged_into_another_device,null);
        showLoggedDevice.setView(view1);
        showLoggedAnotherDevice = showLoggedDevice.create();

        Button btn_ok_logged_device = view1.findViewById(R.id.btn_okk);

        btn_ok_logged_device.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showLoggedAnotherDevice.dismiss();
            }
        });

        //process alertdialog
        AlertDialog.Builder builder = new AlertDialog.Builder(Find_your_account_enter_password.this);
        builder.setCancelable(false);
        builder.setView(R.layout.custom_loading_process);

        show = builder.create();
        show.show();

        // load the image of user
        StorageReference profileref = storageReference.child("users/" + userID + "/profile.jpg");
        profileref.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                if (uri != null) {
                    //Picasso.get().load(uri).into(imageProfile);
                    Glide.with(getApplicationContext()).load(uri).into(imageProfile);
                    show.dismiss();
                }else {
                    show.dismiss();
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                show.dismiss();
                //Toast.makeText(getApplicationContext(),e.getMessage(),Toast.LENGTH_SHORT).show();
            }
        });

        tvFullname.setText(fullname);

        btn_backPage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(Find_your_account_enter_password.this,Find_your_account_email_verify.class);
                i.putExtra("Email",Email);
                i.putExtra("fullname",fullname);
                i.putExtra("userID",userID);
                i.putExtra("setCheckedPass","haveValue");
                startActivity(i);
            }
        });

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
                    inputLayoutPassword.setHintTextColor(ColorStateList.valueOf(Color.GRAY));
                    inputLayoutPassword.setBoxStrokeColor(Color.GRAY);
                }
            }
        });

        btn_nextPage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                show.show();
                password = etPassword.getText().toString();

                if (password.isEmpty()) {
                    inputLayoutPassword.setError("The field cannot be blank");
                    show.dismiss();
                }else {
                    firebaseAuth.signInWithEmailAndPassword(Email,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
                                if (firebaseUser != null) {
                                    String userIdd= firebaseUser.getUid();
                                    DatabaseReference databaseReference = firebaseDatabase.getReference("User Information")
                                            .child(userIdd);

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
                                                    firebaseAuth.signOut();
                                                }else {
                                                    String updateSigned = "signed";
                                                    Map<String, Object> editInformation = new HashMap<>();
                                                    editInformation.put("signed",updateSigned);
                                                    editInformation.put("DeviceName",deviceName);

                                                    DatabaseReference databaseReference1 = firebaseDatabase.getReference("User Information")
                                                            .child(userIdd);
                                                    databaseReference1.updateChildren(editInformation).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                        @Override
                                                        public void onSuccess(Void unused) {
                                                            // User is already logged in, navigate to Home activity
                                                            Intent i = new Intent(Find_your_account_enter_password.this, Home.class);
                                                            i.putExtra("selectMapUserNotEmpty","0");
                                                            startActivity(i);
                                                            finish(); // Finish current activity to prevent returning to this screen
                                                            show.dismiss();
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

                            }else {
                                show.dismiss();
                                AlertDialog.Builder builder = new AlertDialog.Builder(Find_your_account_enter_password.this);
                                LayoutInflater inflater = getLayoutInflater();
                                View layout = inflater.inflate(R.layout.custom_incorrect_password, (ViewGroup) view.findViewById(R.id.custom_incorrect_password_interface) );
                                btn_tryAgin = (Button) layout.findViewById(R.id.btn_tryAgain);
                                btn_getCode = (Button) layout.findViewById(R.id.btn_getCode);

                                builder.setView(layout);

                                AlertDialog dialog = builder.create();
                                dialog.show();

                                btn_tryAgin.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        dialog.dismiss();
                                    }
                                });

                                btn_getCode.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        Intent i = new Intent(Find_your_account_enter_password.this,Find_your_account_email_verify.class);
                                        i.putExtra("Email",Email);
                                        i.putExtra("Email",Email);
                                        i.putExtra("fullname",fullname);
                                        i.putExtra("userID",userID);
                                        i.putExtra("setCheckedPass","haveValue");
                                        startActivity(i);
                                    }
                                });


                            }
                        }
                    });
                }
            }
        });
    }

    @Override
    public void onBackPressed () {
        super.onBackPressed();
        Intent intent = new Intent(Find_your_account_enter_password.this,login.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP); // Just keep this flag
        startActivity(intent);
        finish();
    }
}