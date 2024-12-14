package com.example.gps_pet_tracker;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.util.Random;

public class forget_change_password extends AppCompatActivity implements ConnectionReceiver.ReceiverListener{
    Button btn_backPage, btn_nextPage, btn_ok;
    RadioButton checkEmail;
    TextView tvEmail,tvFullname;
    String VerificationCode;
    int code;
    ImageView custom_image_view_user;
    boolean dialogShowNoconnection = false;
    FirebaseAuth firebaseAuth;
    FirebaseUser user;
    FirebaseFirestore firebaseFirestore;
    FirebaseDatabase firebaseDatabase;
    String userID, Email;
    AlertDialog noConnectionDialog, loadingProcess;

    GoogleSignInAccount gsa;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_forget_change_password);

        AlertDialog.Builder showIntenetDiaglog = new AlertDialog.Builder(forget_change_password.this);
        LayoutInflater layoutInflater = getLayoutInflater();
        View view = layoutInflater.inflate(R.layout.custom_no_internet, (ViewGroup) findViewById(R.id.custom_no_internet));
        showIntenetDiaglog.setView(view);
        showIntenetDiaglog.setCancelable(false);
        noConnectionDialog = showIntenetDiaglog.create();

        AlertDialog.Builder loadprocess = new AlertDialog.Builder(forget_change_password.this);
        loadprocess.setCancelable(false);
        loadprocess.setView(R.layout.custom_no_internet);
        loadingProcess = loadprocess.create();

        btn_ok = view.findViewById(R.id.btn_ok);
        btn_backPage = findViewById(R.id.btn_backPage);
        custom_image_view_user = findViewById(R.id.custom_image_view_user);
        btn_nextPage = findViewById(R.id.btn_nextPage);
        checkEmail = findViewById(R.id.checkEmail);
        tvEmail = findViewById(R.id.tvEmail);
        tvFullname = findViewById(R.id.tvFullname);

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();
        firebaseDatabase = FirebaseDatabase.getInstance();
        userID = firebaseAuth.getCurrentUser().getUid();
        user = firebaseAuth.getCurrentUser();

        Intent intent = getIntent();
        Email = intent.getStringExtra("Email");

        checkEmail.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b) {
                    checkEmail.setButtonTintList(ColorStateList.valueOf(Color.rgb(38, 166, 254)));
                }
            }
        });


        if (gsa != null) {

        }else {

            StorageReference storageReference = FirebaseStorage.getInstance().getReference();
            StorageReference store = storageReference.child("users/"+firebaseAuth.getCurrentUser().getUid()+"/profile.jpg");

            store.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                @Override
                public void onSuccess(Uri uri) {
                    Picasso.get().load(uri).into(custom_image_view_user);
                }
            });

            DatabaseReference databaseReference = firebaseDatabase.getReference("User Information").child(userID);
            databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    Email = snapshot.child("email").getValue(String.class);
                    String fullname = snapshot.child("fullname").getValue(String.class);
                    char firstChar = Email .charAt(0);
                    String add = firstChar +"*****@gmail.com";
                    tvEmail.setText(add);
                    tvFullname.setText(fullname);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });

          /*  DocumentReference documentReference = firebaseFirestore.collection("Users Information").document(userID);
            documentReference.addSnapshotListener(forget_change_password.this, new EventListener<DocumentSnapshot>() {
                @Override
                public void onEvent(@Nullable DocumentSnapshot value, @Nullable FirebaseFirestoreException error) {
                    Email = value.getString("email");
                    char firstChar = Email .charAt(0);
                    String add = firstChar +"*****@gmail.com";
                    tvEmail.setText(add);
                }
            }); */
        }

btn_ok.setOnClickListener(new View.OnClickListener() {
    @Override
    public void onClick(View view) {
        noConnectionDialog.dismiss();
    }
});

btn_backPage.setOnClickListener(new View.OnClickListener() {
    @Override
    public void onClick(View view) {
        Intent i = new Intent(forget_change_password.this, change_password.class);
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP); // Just keep this flag
        startActivity(i);
        finish();
    }
});

btn_nextPage.setOnClickListener(new View.OnClickListener() {
    @Override
    public void onClick(View view) {
        if (!checkEmail.isChecked()){
            Toast.makeText(forget_change_password.this,"Please select",Toast.LENGTH_SHORT).show();
        }else {
           /* firebaseAuth.sendPasswordResetEmail(Email).addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void unused) {
                    startActivity(new Intent(getApplicationContext(),SuccessfulChangePassword.class));
                }
            })
                    // if the user fail sent the email
                    .addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {

                }
            }); */
            checkInternet();
            if (dialogShowNoconnection) {
                sendEmail();
            }
        }
    }
});

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private void sendEmail () {
        // generate random 6 degit code
        Random random = new Random();
        code = random.nextInt(900000) + 100000;
        VerificationCode = String.valueOf(code)+ " is your authentication code. For your protection, do not share this code with anyone";
        JavaMailAPI javaMailAPI = new JavaMailAPI(forget_change_password.this,Email,"Verification Code",VerificationCode);
        javaMailAPI.execute();

        Intent i = new Intent(forget_change_password.this, forget_password_change_password_verification.class);
        i.putExtra("Email",Email);
        i.putExtra("Verificationcode", String.valueOf(code));
        startActivity(i);

    }
    private void checkInternet () {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("android.net.conn.CONNECTIVITY_CHANGE");
        registerReceiver(new ConnectionReceiver(),intentFilter);
        ConnectivityManager  connectivityManager = (ConnectivityManager) getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        boolean isConnected = networkInfo != null && networkInfo.isConnectedOrConnecting();
        showResultsInternet (isConnected);
    }

    private void showResultsInternet (boolean isConnected) {
        if (noConnectionDialog != null) {
            if (isConnected) {
                dialogShowNoconnection = true;
                noConnectionDialog.dismiss();
            }else {
                dialogShowNoconnection = false;
                noConnectionDialog.show();
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
}