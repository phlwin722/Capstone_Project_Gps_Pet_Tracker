package com.example.gps_pet_tracker;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.Random;

public class forget_password_change_password_verification extends AppCompatActivity {

    Button btn_backPage, btn_backPagee;
    String Email, code,Codee, bck_findyouremailVerify;
    EditText etVerify;
    FirebaseAuth firebaseAuth;
    FirebaseUser user;
    Button btn_proceed;
    TextView btn_getCode;
    int codee;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_forget_password_change_password_verification);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        btn_backPage = findViewById(R.id.btn_backPage);
        etVerify = findViewById(R.id.etVerify);
        btn_getCode = findViewById(R.id.btn_getCode);
        btn_proceed = findViewById(R.id.btn_proceed);
        btn_backPagee = findViewById(R.id.btn_backPagee);
        firebaseAuth = FirebaseAuth.getInstance();
        user = firebaseAuth.getCurrentUser();


        Intent intent = getIntent();
        Email = intent.getStringExtra("Email");
        code = intent.getStringExtra("Verificationcode");
        bck_findyouremailVerify = intent.getStringExtra("back");

        btn_proceed.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (code.equals(etVerify.getText().toString())) {
                    Intent i = new Intent(forget_password_change_password_verification.this, Forget_new_change_password_home.class);
                    i.putExtra("Email",Email);
                    i.putExtra("back",bck_findyouremailVerify);
                    i.putExtra("Verificationcode",code);
                    startActivity(i);
                }else {
                    Toast.makeText(forget_password_change_password_verification.this,"Please Enter correct code",Toast.LENGTH_SHORT).show();
                }
            }
        });

        btn_getCode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendEmail ();
            }
        });

        btn_backPage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                backPreviousPage ();
            }
        });

        btn_backPagee.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                backPreviousPage ();
            }
        });
    }

    private void sendEmail () {
        Random random = new Random();
        codee = random.nextInt (900000) + 100000;
        Codee = String.valueOf(codee) + " is your authentication code. For your protection, do not share this code with anyone";
        code = String.valueOf(codee);
        JavaMailAPI javaMailAPI = new JavaMailAPI(forget_password_change_password_verification.this,Email,"Verification Code",Codee);
        javaMailAPI.execute();
    }

    private void backPreviousPage () {
        if (bck_findyouremailVerify != null) {
            Intent i = new Intent(forget_password_change_password_verification.this,Find_your_account_email_verify.class);
            i.putExtra("Email",Email);
            i.putExtra("back",bck_findyouremailVerify);
            startActivity(i);
            finish();
        }else {
            Intent i = new Intent(forget_password_change_password_verification.this, forget_change_password.class);
            i.putExtra("Email", Email);
            startActivity(i);
            finish();
        }
    }
}