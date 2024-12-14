package com.example.gps_pet_tracker;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.FirebaseFirestore;

public class Find_your_account extends AppCompatActivity {

    TextView btn_backPage, btn_tryAgain, btn_createAccount;
    Button btn_nextPage;
    String email,fullname, userID;
    TextInputLayout inputLayoutEmail;
    EditText etEmail;
    FirebaseFirestore firebaseFirestore;
    FirebaseDatabase firebaseDatabase;
    FirebaseAuth firebaseAuth;
    AlertDialog showLoading;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_find_your_account);

        firebaseFirestore = FirebaseFirestore.getInstance();
        firebaseAuth = FirebaseAuth.getInstance();
        firebaseDatabase = FirebaseDatabase.getInstance();

        btn_backPage = findViewById(R.id.btn_backpage);
        etEmail = findViewById(R.id.etEmail);
        btn_nextPage = findViewById(R.id.nextPage);
        inputLayoutEmail = findViewById(R.id.inputLayoutEmail);

        email = etEmail.getText().toString();

        Intent i = getIntent();
        email = i.getStringExtra("Email");
        etEmail.setText(email);

        AlertDialog.Builder builder = new AlertDialog.Builder(Find_your_account.this);
        builder.setCancelable(false);
        builder.setView(R.layout.custom_loading_process);

        showLoading = builder.create();

        btn_backPage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(Find_your_account.this, login.class);
                startActivity(i);
                firebaseAuth.signOut();
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

        btn_nextPage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showLoading.show();
                boolean next = false;

                if (etEmail.getText().toString().isEmpty()){
                    inputLayoutEmail.setError("Field cannot blank");
                    showLoading.dismiss();
                }else {
                    next = true;
                }


                if (next) {
                    // Check if the email exists in Firestore
                    email = etEmail.getText().toString();
                    DatabaseReference databaseReference = firebaseDatabase.getReference("User Information");
                    // Query the database
                    databaseReference.orderByChild("email").equalTo(email).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if (snapshot.exists()) {
                               for (DataSnapshot snapshot1 : snapshot.getChildren()) {
                                   fullname = snapshot1.child("fullname").getValue(String.class);
                                   userID = snapshot1.child("userID").getValue(String.class);
                                   Intent i = new Intent(Find_your_account.this,Find_your_account_email_verify.class);
                                   i.putExtra("Email",email);
                                   i.putExtra("fullname",fullname);
                                   i.putExtra("userID", userID);
                                   showLoading.dismiss();
                                   startActivity(i);
                               }
                            }else {
                                showLoading.dismiss();
                                AlertDialog.Builder builder = new AlertDialog.Builder(Find_your_account.this);
                                LayoutInflater inflater = getLayoutInflater();
                                View layout = inflater.inflate(R.layout.could_find_account,(ViewGroup) view.findViewById(R.id.custom_couldnt_find_interface));

                                btn_tryAgain = (Button) layout.findViewById(R.id.btn_tryAgain);
                                btn_createAccount = (Button) layout.findViewById(R.id.btn_create);
                                TextView didfind = layout.findViewById(R.id.didfind);

                                didfind.setText("It looks like " + email + " isn't connected to an account. You can create a new account with this email or try again.");

                                builder.setView(layout);

                                AlertDialog dialog = builder.create();
                                dialog.show();

                                btn_tryAgain.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        dialog.dismiss();
                                    }
                                });

                                btn_createAccount.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        Intent i = new Intent(Find_your_account.this, create_account.class);
                                        startActivity(i);
                                    }
                                });
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });

                    /*firebaseFirestore.collection("Users Information")
                            .whereEqualTo("email",etEmail.getText().toString())
                            .get()
                            .addOnCompleteListener(task -> {
                                if (task.isSuccessful()) {
                                    // Check if there are any documents that match the query

                                    if (!task.getResult().isEmpty()) {
                                        // to get user id on firebase cloud storage
                                     //   firebaseUser = firebaseAuth.getCurrentUser(); // Refresh firebaseUser
                                       // userID = firebaseUser.getUid();

                                        for (QueryDocumentSnapshot documentSnapshot : task.getResult()) {
                                            fullname = documentSnapshot.getString("fullname");
                                            userID = documentSnapshot.getString("userID");
                                            Intent i = new Intent(Find_your_account.this,Find_your_account_email_verify.class);
                                            i.putExtra("Email",email);
                                            i.putExtra("fullname",fullname);
                                            i.putExtra("userID", userID);
                                            showLoading.dismiss();
                                            startActivity(i);
                                        }

                                    }else {
                                        showLoading.dismiss();
                                        AlertDialog.Builder builder = new AlertDialog.Builder(Find_your_account.this);
                                        LayoutInflater inflater = getLayoutInflater();
                                        View layout = inflater.inflate(R.layout.could_find_account,(ViewGroup) view.findViewById(R.id.custom_couldnt_find_interface));

                                        btn_tryAgain = (Button) layout.findViewById(R.id.btn_tryAgain);
                                        btn_createAccount = (Button) layout.findViewById(R.id.btn_create);

                                        builder.setView(layout);

                                        AlertDialog dialog = builder.create();
                                        dialog.show();

                                        btn_tryAgain.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View view) {
                                                dialog.dismiss();
                                            }
                                        });

                                        btn_createAccount.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View view) {
                                                Intent i = new Intent(Find_your_account.this, create_account.class);
                                                startActivity(i);
                                            }
                                        });
                                    }
                                }else {
                                    // Handle errors
                                    Toast.makeText(Find_your_account.this, "Error checking email existence" + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            });*/

                }
            }
        });

    }

    @Override
    public void onBackPressed () {
        super.onBackPressed();
        Intent intent = new Intent(Find_your_account.this,login.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP); // Just keep this flag
        startActivity(intent);
        finish();
    }
}