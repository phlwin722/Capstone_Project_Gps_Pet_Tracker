package com.example.gps_pet_tracker;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputLayout;

public class create_account extends AppCompatActivity {
    Button nextPage;
    TextView btn_backpage, haveAlreadyAnAccount;
    EditText etLastname, etFirstname;
    TextInputLayout inputLayoutFirstname, inputLayoutLastname;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_account);

        nextPage = findViewById(R.id.nextPage);
        etFirstname = findViewById(R.id.etFirstname);
        etLastname = findViewById(R.id.etLastname);
        inputLayoutFirstname = findViewById(R.id.inputLayoutFirstname);
        inputLayoutLastname = findViewById(R.id.inputLayoutLastname);
        haveAlreadyAnAccount = findViewById(R.id.haveAlreadyAnAccount);
        btn_backpage = findViewById(R.id.btn_backpage);


        Intent i = getIntent();
        String lastname = i.getStringExtra("firstname");
        String firstname = i.getStringExtra("lastname");

        if (firstname != null || lastname != null) {
            etLastname.setText(lastname);
            etFirstname.setText(firstname);
        }

        haveAlreadyAnAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(create_account.this, login.class));
                finish();
            }
        });

        etFirstname.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                inputLayoutFirstname.setErrorEnabled(false);
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        etFirstname.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean hasFocus) {
                if (hasFocus) {
                    inputLayoutFirstname.setBoxStrokeColor(Color.rgb(112,112,112));
                    inputLayoutFirstname.setHintTextColor(ColorStateList.valueOf(Color.BLACK));
                } else {
                    inputLayoutFirstname.setBoxStrokeColor(Color.GRAY);
                    inputLayoutFirstname.setHintTextColor(ColorStateList.valueOf(Color.GRAY));
                }
            }
        });

        etLastname.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                inputLayoutLastname.setErrorEnabled(false);
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        etLastname.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean hasFocus) {
                if (hasFocus) {
                    inputLayoutLastname.setBoxStrokeColor(Color.rgb(112,112,112));
                    inputLayoutLastname.setHintTextColor(ColorStateList.valueOf(Color.BLACK));
                } else {
                    inputLayoutLastname.setBoxStrokeColor(Color.GRAY);
                    inputLayoutLastname.setHintTextColor(ColorStateList.valueOf(Color.GRAY));
                }
            }
        });

        nextPage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean isValid = true;

                if (etFirstname.getText().toString().isEmpty()) {
                    inputLayoutFirstname.setError("First name cannot be blank");
                    isValid = false ;
                } else {
                    inputLayoutFirstname.setErrorEnabled(false);
                }

                if (etLastname.getText().toString().isEmpty()) {
                    inputLayoutLastname.setError("Last name cannot be blank");
                    isValid = false;
                } else {
                    inputLayoutLastname.setErrorEnabled(false);
                }

                // to check if is valid

                if (isValid){
                    Intent i = new Intent(create_account.this, email.class);
                    i.putExtra("firstname",etFirstname.getText().toString());
                    i.putExtra("lastname",etLastname.getText().toString());
                    startActivity(i);
                }
            }
        });

        btn_backpage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(create_account.this, login.class);;
                startActivity(i);
            }
        });
    }

    @Override
    public void onBackPressed () {
        super.onBackPressed();
        Intent intent = new Intent(create_account.this,login.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP); // Just keep this flag
        startActivity(intent);
        finish();
    }
}