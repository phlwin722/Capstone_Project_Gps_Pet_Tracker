package com.example.gps_pet_tracker;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Calendar;

public class birth_day extends AppCompatActivity {
    AutoCompleteTextView autoCompletemonth, autoCompleteday, autoCompleteyear;
    ArrayAdapter<String> adapterMonth, adapterDay, adapterYear;
    Button nextPage, dateButton;
    TextView btn_backPage,haveAlreadyAnAccount;
    DatePickerDialog datePickerDialog;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_birth_day);
        // initialize date picker
        initDatePicker();

        nextPage = findViewById(R.id.nextPage);
        dateButton = findViewById(R.id.btn_dataPicker);
        btn_backPage = findViewById(R.id.btn_backPage);
        haveAlreadyAnAccount = findViewById(R.id.haveAlreadyAnAccount);

        dateButton.setText(getTodaysDate());

        // get the putextra on the previos page
        Intent intent = getIntent();
        String firstname =  intent.getStringExtra("firstname");
        String lastname = intent.getStringExtra("lastname");
        String birthday = intent.getStringExtra("birthday");

        if (birthday != null) {
            dateButton.setText(birthday);
        }

        haveAlreadyAnAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(birth_day.this, login.class));
            }
        });
        nextPage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(birth_day.this, email.class);
                i.putExtra("birthday", dateButton.getText().toString());
                i.putExtra("firstname", firstname);
                i.putExtra("lastname",lastname);
                startActivity(i);
            }
        });

        btn_backPage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(birth_day.this,create_account.class);
                i.putExtra("birthday", dateButton.getText().toString());
                i.putExtra("firstname", firstname);
                i.putExtra("lastname",lastname);
                startActivity(i);
            }
        });

        dateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openDatePicker(v);
            }
        });
    }

    private String getTodaysDate() {
        Calendar cal = Calendar.getInstance();
        int year = cal.get(Calendar.YEAR);
        int month = cal.get(Calendar.MONTH);
        month += 1;
        int day = cal.get(Calendar.DAY_OF_MONTH);
        return makeDateString(day,month,year);
    }

    public void initDatePicker () {
        DatePickerDialog.OnDateSetListener dateSetListener = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                month = month + 1;
                String date = makeDateString(dayOfMonth,month,year);
                dateButton.setText(date);
            }
        };

        Calendar cal = Calendar.getInstance();
        int year = cal.get(Calendar.YEAR);
        int month = cal.get(Calendar.MONTH);
        int day = cal.get(Calendar.DAY_OF_MONTH);

        datePickerDialog = new DatePickerDialog(this,dateSetListener,year,month,day);
    }

    private String makeDateString(int dayOfMonth, int month, int year) {
        return  getMonthFormat (month) + " " + dayOfMonth + " " + year;
    }

    private String getMonthFormat(int month) {
        if (month == 1) {
            return "January";
        } else if (month == 2) {
            return "February";
        } else if (month == 3) {
            return "March";
        } else if (month == 4) {
            return "April";
        } else if (month == 5) {
            return "May";
        } else if (month == 6) {
            return "June";
        } else if (month == 7) {
            return "July";
        } else if (month == 8) {
            return "August";
        } else if (month == 9) {
            return "September";
        } else if (month == 10) {
            return "October";
        } else if (month == 11) {
            return "November";
        } else if (month == 12) {
            return "December";
        }
        return "January";
    }

    public void openDatePicker (View view) {
        datePickerDialog.show();
    }

    @Override
    public void onBackPressed () {
        super.onBackPressed();
        Intent intent = new Intent(birth_day.this,login.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP); // Just keep this flag
        startActivity(intent);
        finish();
    }
}