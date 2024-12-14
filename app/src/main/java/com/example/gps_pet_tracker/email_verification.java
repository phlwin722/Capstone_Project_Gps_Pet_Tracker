package com.example.gps_pet_tracker;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputLayout;

import java.util.Random;

public class email_verification extends AppCompatActivity {
    Button next_page;
    TextView btn_backpage, resendCode, haveAlreadyAnAccount;
    EditText etverify;
    TextInputLayout inputLayoutEmail;
    String verifycode,email,lastname,firstname,birthday;
    AlertDialog showLoadProcess;
    private EditText [] OtpEditext;
    int codee = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_email_verification);

        next_page = findViewById(R.id.nextPage);
        btn_backpage = findViewById(R.id.btn_backPage);
        etverify = findViewById(R.id.etverify);
        resendCode = findViewById(R.id.resendCode);
        inputLayoutEmail = findViewById(R.id.inputLayoutEmail);
        haveAlreadyAnAccount = findViewById(R.id.haveAlreadyAnAccount);

        // Initialize the EditText fields for OTP input
        OtpEditext = new EditText[] {
                findViewById(R.id.otp_edit_text1),
                findViewById(R.id.otp_edit_text2),
                findViewById(R.id.otp_edit_text3),
                findViewById(R.id.otp_edit_text4),
                findViewById(R.id.otp_edit_text5),
                findViewById(R.id.otp_edit_text6)
        };

        // Set up TextWatchers and KeyListeners for each EditText
        for (int s = 0; s < OtpEditext.length; s++) {
            final int index = s;

            // FocusChangeListener to handle field clearing
            OtpEditext[s].setOnFocusChangeListener(new View.OnFocusChangeListener() {
                @Override
                public void onFocusChange(View view, boolean hasFocus) {
                    if (hasFocus) {
                        // Redistribute input if the current field is focused
                        handleRedistribution(index);
                        // Show the keyboard explicitly (optional, usually not needed)
                        showKeyboard(view);
                    }
                }
            });

            // KeyListener to handle backspace key
            OtpEditext[s].setOnKeyListener(new View.OnKeyListener() {
                @Override
                public boolean onKey(View view, int i, KeyEvent keyEvent) {
                    if (i == KeyEvent.KEYCODE_DEL && keyEvent.getAction() == KeyEvent.ACTION_DOWN){
                        // If the user presses backspace and the current field is empty
                        if (index > 0 && OtpEditext[index].getText().toString().isEmpty()) {
                            OtpEditext[index - 1].requestFocus(); // Move focus to the previous field
                        }
                    }
                    return false; // Return false to allow the default backspace behavior
                }
            });

            // TextWatcher to move focus to the next field when a digit is entered
            OtpEditext[s].addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                }

                @Override
                public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                    // If the user has entered a digit and it is not the last field
                    if (charSequence.length() > 1) {
                        // If the length is more than 1, keep only the first character
                        OtpEditext[index].setText(charSequence.subSequence(0, 1));
                        OtpEditext[index].setSelection(1);
                    } else if (charSequence.length() == 1 && index < OtpEditext.length - 1) {
                        // Move focus to the next field
                        OtpEditext[index + 1].requestFocus();
                    }
                }

                @Override
                public void afterTextChanged(Editable editable) {

                }
            });

        }

        Intent i = getIntent();
        lastname = i.getStringExtra("lastname");
        firstname = i.getStringExtra("firstname");
 //       birthday = i.getStringExtra("birthday");
        email = i.getStringExtra("email");
        verifycode = i.getStringExtra("verificationCode");

        AlertDialog.Builder builder = new AlertDialog.Builder(email_verification.this);
        builder.setCancelable(false);
        builder.setView(R.layout.custom_loading_process);

        showLoadProcess = builder.create();

        resendCode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clearAllEditText();
                verifycode = generateCode();
                sendEmail(email,"Verification Code",verifycode);
            }
        });

        haveAlreadyAnAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(email_verification.this, login.class));
                finish();
            }
        });

        next_page.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showLoadProcess.show();
                validateOtp();
            }
        });

        btn_backpage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(email_verification.this, email.class);
                i.putExtra("lastname",lastname);
                i.putExtra("firstname",firstname);
    //            i.putExtra("birthday",birthday);
                i.putExtra("email",email);
                startActivity(i);
            }
        });
    }

    // Method to validate the OTP
    private void validateOtp() {
        // Create a StringBuilder to collect OTP input
        StringBuilder otp = new StringBuilder();

        // Collect OTP values from all EditText fields
        for (EditText editText : OtpEditext) {
            String text = editText.getText().toString().trim(); // Trim any whitespace
            if (text.isEmpty()) {
                showLoadProcess.dismiss();
                Toast.makeText(this, "Please enter the full OTP", Toast.LENGTH_SHORT).show();
                return; // Exit the method if any field is empty
            }
            otp.append(text); // Append each digit to the OTP string
        }

        // Check if the OTP length is exactly 6 digits
        if (otp.length() == 6) {
            // Assuming verifycode and codee are previously defined and accessible
            String otpString = otp.toString();
            if (otpString.equals(verifycode) || otpString.equals(String.valueOf(codee))) {
                // OTP is valid, proceed with the next step
                showLoadProcess.dismiss();
                Intent i = new Intent(email_verification.this, PasswordSetup.class);
                i.putExtra("lastname", lastname);
                i.putExtra("firstname", firstname);
   //             i.putExtra("birthday", birthday);
                i.putExtra("email", email);
                startActivity(i);
            } else {
                // OTP is invalid
                showLoadProcess.dismiss();
                Toast.makeText(this, "Invalid OTP. Please try again.", Toast.LENGTH_SHORT).show();
            }
        } else {
            // OTP is not exactly 6 digits
            // OTP is invalid
            showLoadProcess.dismiss();
            Toast.makeText(this, "OTP must be exactly 6 digits", Toast.LENGTH_SHORT).show();
        }
    }

    // Method to handle redistribution of input to earlier fields
    private void handleRedistribution(int currentIndex) {
        for (int i = 0; i < currentIndex; i++) {
            if (OtpEditext[i].getText().toString().isEmpty()) {
                // Move focus to the current index if any previous field is empty
                OtpEditext[i].requestFocus();
                break;
            }
        }
    }
    // Method to show the keyboard explicitly (optional)
    private void showKeyboard  (View v) {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null){
            imm.showSoftInput(v, InputMethodManager.SHOW_IMPLICIT);
        }
    }

    // Method to clear all EditText fields
    private void clearAllEditText() {
        for (EditText editText : OtpEditext){
            editText.setText("");
        }
    }

    // verfication code using email
    private String generateCode () {
        // generate random 6-degit number
        Random random = new Random();
        int code = random.nextInt(900000) + 100000; // ensure the cide us always 6 degits
        Log.d("error",String.valueOf(code));
        codee = code;
        verifycode = String.valueOf(codee);
        return  String.valueOf(code) + " is your authentication code. For your protection, do not share this code with anyone";
    }

    private void sendEmail (String recieptEmail, String subject, String message) {
        JavaMailAPI javaMailAPI= new JavaMailAPI(email_verification.this,recieptEmail,subject,message);
        javaMailAPI.execute();
    }

    @Override
    public void onBackPressed () {
        super.onBackPressed();
        Intent intent = new Intent(email_verification.this,login.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP); // Just keep this flag
        startActivity(intent);
        finish();
    }

    
}

/*Log.e("code",verifycode);

                if (etverify.getText().toString().equals(verifycode) || etverify.getText().toString().equals(String.valueOf(codee))) {
                    isValid = true;
                    inputLayoutEmail.setErrorEnabled(false);
                }else if (etverify.getText().toString().trim().isEmpty()) {
                    inputLayoutEmail.setError("Field cannot be blank");
                    showLoadProcess.dismiss();
                }else{
                    showLoadProcess.dismiss();
                    Toast.makeText(email_verification.this,"Please Correct Verification Code", Toast.LENGTH_LONG).show();
                }

                if (isValid) {
                    showLoadProcess.dismiss();
                    Intent i = new Intent(email_verification.this, PasswordSetup.class);
                    i.putExtra("lastname",lastname);
                    i.putExtra("firstname",firstname);
                    i.putExtra("birthday",birthday);
                    i.putExtra("email",email);
                    startActivity(i);
                }*/