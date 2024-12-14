package com.example.gps_pet_tracker;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
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

import com.bumptech.glide.Glide;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.Random;

public class  Find_your_account_email_verify extends AppCompatActivity {

    Button btn_nextPage, btn_backPage;
    TextView tvEmail, tvFullname;
    RadioButton checkEmail, checkPassword;
    String Email, codee, codeee, fullname, userID;
    ImageView imageUser;
    int code;
    FirebaseDatabase firebaseDatabase;
    FirebaseAuth firebaseAuth;
    AlertDialog show;
    StorageReference storageReference;
    GoogleSignInClient gsc;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_find_your_account_email_verify);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        firebaseAuth = FirebaseAuth.getInstance();
        storageReference = FirebaseStorage.getInstance().getReference();
        firebaseDatabase = FirebaseDatabase.getInstance();

        tvFullname = findViewById(R.id.tvFullname);
        imageUser = findViewById(R.id.imageUser);
        btn_backPage = findViewById(R.id.btn_backPage);
        btn_nextPage = findViewById(R.id.btn_nextPage);
        tvEmail = findViewById(R.id.tvEmail);
        checkEmail = findViewById(R.id.checkEmail);
        checkPassword = findViewById(R.id.checkPassword);

        Intent i = getIntent();
        Email = i.getStringExtra("Email");
        fullname = i.getStringExtra("fullname");
        userID = i.getStringExtra("userID");
        String setCheckedPassword = i.getStringExtra("setCheckedPass");

        if (setCheckedPassword != null) {
            checkPassword.setChecked(true);
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(Find_your_account_email_verify.this);
        builder.setCancelable(false);
        builder.setView(R.layout.custom_loading_process);

        show = builder.create();
        show.show();

        // load the image of user
        StorageReference profileref = storageReference.child("users").child(userID).child("profile.jpg");
        profileref.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                if (uri != null) {
                    Glide.with(getApplicationContext()).load(uri).into(imageUser);
                    show.dismiss();
                }else {
                    show.dismiss();
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                show.dismiss();
               // Toast.makeText(getApplicationContext(),e.getMessage(),Toast.LENGTH_SHORT).show();
            }
        });

        // get the first character then add *****
        char firstEmail = Email.charAt(0);
        String addfirstEmail = String.valueOf(firstEmail) + "*****@gmail.com";

        tvFullname.setText(fullname);
        tvEmail.setText(addfirstEmail);

        checkEmail.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b) {
                    checkEmail.setButtonTintList(ColorStateList.valueOf(Color.rgb(38, 166, 254)));
                }else{
                    checkEmail.setButtonTintList(ColorStateList.valueOf(Color.GRAY));
                }
            }
        });

        checkPassword.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b) {
                    checkPassword.setButtonTintList(ColorStateList.valueOf(Color.rgb(38, 166, 254)));
                }else {
                    checkPassword.setButtonTintList(ColorStateList.valueOf(Color.GRAY));
                }
            }
        });


        btn_nextPage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                show.show();
                if (checkEmail.isChecked()){
                    checkPassword.setChecked(   false);
                    firebaseAuth.sendPasswordResetEmail(Email).addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void unused) {
                                    DatabaseReference databaseReference = firebaseDatabase.getReference("User Information")
                                                    .child(userID);

                                    databaseReference.child("signed").setValue("not Signed");

                                    show.dismiss();
                                    signout();
                                    startActivity(new Intent(getApplicationContext(), SuccessfulChangePassword.class));
                                }
                            })
                            // if failure changes pssword
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {

                                }
                            });
                }else if (checkPassword.isChecked()) {
                    show.dismiss();
                    checkEmail.setChecked(false);
                    Intent i = new Intent(Find_your_account_email_verify.this, Find_your_account_enter_password.class);
                    i.putExtra("Email",Email);
                    i.putExtra("userID",userID);
                    i.putExtra("fullname",fullname);
                    startActivity(i);
                }else {
                    Toast.makeText(Find_your_account_email_verify.this,"Please select",Toast.LENGTH_SHORT).show();
                }
            }
        });

        btn_backPage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(Find_your_account_email_verify.this, Find_your_account.class);
                i.putExtra("Email", Email);
                startActivity(i);
            }
        });
    }

    private void sendEmail () {
        Random random = new Random();
        code = random.nextInt(900000) + 100000;
        codee = String.valueOf(code)+ " is your authentication code. For your protection, do not share this code with anyone";
        codeee = String.valueOf(code);

        JavaMailAPI javaMailAPI = new JavaMailAPI(Find_your_account_email_verify.this,Email,"Verification Code",codee);
        javaMailAPI.execute();

        Intent i = new Intent(Find_your_account_email_verify.this, forget_password_change_password_verification.class);
        i.putExtra("Email",Email);
        i.putExtra("Verificationcode", codeee);
        i.putExtra("back","Find_your_account_email_verify");
        startActivity(i);
    }


    private void signout () {
        if (gsc != null) {
            gsc.signOut().addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()) {
                        // Redirect user to login screen or perform any other action
                        startActivity(new Intent(getApplicationContext(), login.class));
                    } else {
                        // Handle sign out failure
                    }

                }
            });
        }else if (firebaseAuth != null) {
            firebaseAuth.signOut();
        }else {
            // gsc is null, handle this case
            Log.e("SettingsFragment", "GoogleSignInClient is null");
        }
    }
    @Override
    public void onBackPressed () {
        super.onBackPressed();
        Intent intent = new Intent(Find_your_account_email_verify.this,login.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP); // Just keep this flag
        startActivity(intent);
        finish();
    }
}