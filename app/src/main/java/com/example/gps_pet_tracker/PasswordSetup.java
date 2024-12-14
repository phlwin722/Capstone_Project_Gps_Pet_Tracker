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

public class PasswordSetup extends AppCompatActivity {
    Button nextPage;
    TextInputLayout inputLayoutPassword, inputLayoutRetypePassword;
    EditText etPassword, etRetypePassword;
    TextView btn_backpage, haveAlreadyAnAccount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_password_setup);

        nextPage = findViewById(R.id.nextPage);
        inputLayoutPassword = findViewById(R.id.inputLayoutPassword);
        inputLayoutRetypePassword = findViewById(R.id.inputLayoutRetypePassword);
        etPassword = findViewById(R.id.etPassword);
        etRetypePassword = findViewById(R.id.etRetypepassword);
        btn_backpage = findViewById(R.id.btn_backPage);
        haveAlreadyAnAccount = findViewById(R.id.haveAlreadyAnAccount);

        Intent i = getIntent();
        String lastname = i.getStringExtra("lastname");
        String firstname = i.getStringExtra("firstname");
      //  String birthday = i.getStringExtra("birthday");
        String email = i.getStringExtra("email");

        haveAlreadyAnAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(PasswordSetup.this, login.class));
                finish();
            }
        });

        btn_backpage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(PasswordSetup.this, email_verification.class);
                i.putExtra("lastname", lastname);
                i.putExtra("firstname", firstname);
    //            i.putExtra("birthday", birthday);
                i.putExtra("email", email);
                startActivity(i);
                finish();
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
                    inputLayoutPassword.setBoxStrokeColor(Color.GRAY);
                    inputLayoutPassword.setHintTextColor(ColorStateList.valueOf(Color.GRAY));
                }
            }
        });

        etRetypePassword.addTextChangedListener(new TextWatcher() {
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

        etRetypePassword.setOnFocusChangeListener(new View.OnFocusChangeListener() {
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

        nextPage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String password = etPassword.getText().toString().trim();
                String retypePassword = etRetypePassword.getText().toString().trim();

                boolean isValid = true; // Assume the passwords are valid by default

                if (password.isEmpty()) {
                    inputLayoutPassword.setError("Please fill up the blank");
                    isValid = false;
                }else {
                    if (password.length() < 8) {
                        inputLayoutPassword.setError("Password must be at least 8 characters");
                        isValid = false;
                    }else {
                        if (!password.equals(retypePassword)) {
                            inputLayoutPassword.setError("Retype password does not match password");
                        } else {
                            if (containsDisallowedSymbol(password)) {
                                inputLayoutPassword.setError("Password must contain the symbols @#$!&*^%$");
                                isValid = false;
                            } else {
                                inputLayoutPassword.setErrorEnabled(false);
                            }
                        }
                    }
                }

                if (retypePassword.isEmpty()) {
                    inputLayoutRetypePassword.setError("Please fill up the blank");
                    isValid = false;
                }else {
                    if (retypePassword.length() < 8) {
                        inputLayoutRetypePassword.setError("Password must be at least 8 characters");
                        isValid = false;
                    }else {
                        if (!retypePassword.equals(password)) {
                            inputLayoutRetypePassword.setError("Password does not match retype password");
                            isValid = false;
                        }else {
                            if (containsDisallowedSymbol(retypePassword)) {
                                inputLayoutRetypePassword.setError("Password must contain the symbols @#$!&*^%$");
                                isValid = false;
                            }else {
                                inputLayoutRetypePassword.setErrorEnabled(false);
                            }
                        }
                    }
                }

                // If everything is valid, proceed
                if (isValid) {
                    Intent i = new Intent(PasswordSetup.this, create_user_loading_process.class);
                    i.putExtra("lastname", lastname);
                    i.putExtra("firstname", firstname);
       //             i.putExtra("birthday", birthday);
                    i.putExtra("email", email);
                    i.putExtra("password", password);
                    startActivity(i);
                    finish();
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

    @Override
    public void onBackPressed () {
        super.onBackPressed();
        Intent intent = new Intent(PasswordSetup.this,login.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP); // Just keep this flag
        startActivity(intent);
        finish();
    }
}
