package com.example.gps_pet_tracker;

import android.Manifest;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.github.dhaval2404.imagepicker.ImagePicker;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnFailureListener;
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
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class MyInformation extends AppCompatActivity implements ConnectionReceiver.ReceiverListener{


    // https://github.com/Dhaval2404/ImagePicker
    AutoCompleteTextView gender;
    Button btn_backPage, btn_backPagee, btn_DatePicker, btn_ok ,btn_edit_profile, btn_edit_information, btn_yes_edit, btn_cancel_edit;
    EditText etFirstname, etLastname, etEmail;
    GoogleSignInOptions gso;
    GoogleSignInClient gsc;
    GoogleSignInAccount gsa;
    DatePickerDialog datePickerDialog;
    ImageView captureImage;
    AlertDialog show, showInternetConnection;
    boolean isInternetDialogVisible = false;
    private static int PERMISSION_REQUEST_CODE = 1;
    ImageButton delete_user;
    private static final int IMAGE_REQUEST_CODE = 2;

    Uri uri;
    FirebaseAuth firebaseAuth;
    StorageReference storageReference;
    FirebaseDatabase firebaseDatabase;
    FirebaseFirestore firebaseFirestore;
    String userID;
    FirebaseUser firebaseUser;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_information);

        //gender = view.findViewById (R.id.gender);
        btn_backPage = findViewById(R.id.btn_backPage);
        btn_backPagee = findViewById(R.id.btn_backPagee);
     //   btn_DatePicker = findViewById(R.id.btn_dataPicker);
        delete_user = findViewById(R.id.delete_user);
        etLastname = findViewById(R.id.etLastname);
        etEmail = findViewById(R.id.etEmail);
        btn_edit_profile = findViewById(R.id.btn_edit_Profile);
        captureImage = findViewById(R.id.captureImage);
        btn_edit_information = findViewById(R.id.btn_edit_information);

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();
        firebaseDatabase = FirebaseDatabase.getInstance();
        firebaseUser = firebaseAuth.getCurrentUser();
        userID = firebaseAuth.getCurrentUser().getUid();

        AlertDialog.Builder builder = new AlertDialog.Builder(MyInformation.this);
        builder.setCancelable(false);
        builder.setView(R.layout.custom_loading_process);

        show = builder.create();

        AlertDialog.Builder noInternetConnection = new AlertDialog.Builder(MyInformation.this);
        LayoutInflater inflater = getLayoutInflater();
        View view = inflater.inflate(R.layout.custom_no_internet, (ViewGroup) findViewById(R.id.custom_no_internet));
        noInternetConnection.setView(view);

        showInternetConnection = noInternetConnection.create();

        btn_ok = view.findViewById(R.id.btn_ok);

        btn_ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showInternetConnection.dismiss();
            }
        });

        storageReference = FirebaseStorage.getInstance().getReference();
        StorageReference profileref = storageReference.child("users/"+firebaseAuth.getCurrentUser().getUid()+"/profile.jpg");

        show.show();

        profileref.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                Picasso.get().load(uri).into(captureImage);
                show.dismiss();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
              //  showInternetConnection.show();
            }
        });

        initializeDatePicker();

      /*  String Gender [] = {"Male", "Female", "Other"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(),R.layout.list_gender,Gender);
        gender.setAdapter(adapter);
*/
        gsa = GoogleSignIn.getLastSignedInAccount(MyInformation.this);
        if (gsa != null) {
            etEmail.setText(gsa.getEmail());
            etLastname.setText(gsa.getDisplayName());
     //       btn_DatePicker.setText("Set now");
        }else {
            // Set up Firestore listener
            DatabaseReference databaseReference = firebaseDatabase.getReference("User Information").child(userID);
            databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        show.dismiss();
                        etEmail.setText(snapshot.child("email").getValue(String.class));
                        etLastname.setText(snapshot.child("fullname").getValue(String.class));
              //          btn_DatePicker.setText(snapshot.child("birthday").getValue(String.class));
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }

        delete_user.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder1 = new AlertDialog.Builder(MyInformation.this);
                View view1 = getLayoutInflater().inflate(R.layout.custom_delete_alert,null);
                builder1.setView(view1);
                AlertDialog alertDialog = builder1.create();

                alertDialog.show();

                Button delete = view1.findViewById(R.id.btn_logout_confirm);
                Button cancel = view1.findViewById(R.id.btn_logout_cancel);

                delete.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        alertDialog.dismiss();

                        Intent intent = new Intent(MyInformation.this,Sucessfull_delete_information.class);
                        intent.putExtra("myInformationDelete",userID);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP); // Just keep this flag
                        startActivity(intent);
                        finish();
                    }
                });

                cancel.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        alertDialog.dismiss();
                    }
                });
            }
        });

        btn_edit_profile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Build.VERSION.SDK_INT >= 33) {
                    String[] permissions = {
                            Manifest.permission.CAMERA,
                            Manifest.permission.READ_MEDIA_IMAGES // Required for API 30 and above
                    };
                    // Check if the required permissions are already granted
                    if (arePermissionGranted(permissions)) {
                        // Permissions are granted, proceed with image picking
                        openImagePicker();
                    } else {
                        // Request permissions
                        ActivityCompat.requestPermissions(MyInformation.this, permissions, PERMISSION_REQUEST_CODE);
                    }
                }else {
                    String[] permissions = {
                            Manifest.permission.CAMERA,
                            Manifest.permission.READ_EXTERNAL_STORAGE // Required for API 30 and above
                    };
                    // Check if the required permissions are already granted
                    if (arePermissionGranted(permissions)) {
                        // Permissions are granted, proceed with image picking
                        openImagePicker();
                    } else {
                        // Request permissions
                        ActivityCompat.requestPermissions(MyInformation.this, permissions, PERMISSION_REQUEST_CODE);
                    }
                }
            }
        });

        btn_backPagee.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Handle back press event
                BackPressed();
            }
        });

        btn_backPage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Handle back press event
                BackPressed();
            }
        });

    /*    btn_DatePicker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openDatePicker(v);
            }
        });*/

        btn_edit_information.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder1 = new AlertDialog.Builder(MyInformation.this);
                builder1.setCancelable(false);
                LayoutInflater inflater1 = getLayoutInflater();
                View view1 = inflater1.inflate(R.layout.custom_update_alert, (ViewGroup) view.findViewById(R.id.custom_verification_edit));
                builder1.setView(view1);

                btn_cancel_edit = view1.findViewById(R.id.btn_cancel);
                btn_yes_edit = view1.findViewById(R.id.btn_yes_confirm);

                AlertDialog showEdit = builder1.create();
                showEdit.show();

                btn_yes_edit.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        checkingInternet();
                        showEdit.dismiss();
                        show.show();
                        if (isInternetDialogVisible) {
                            if (uri != null) {
                                uploadImageProfile(uri);

                                updateInformation ();
                            }else {
                                updateInformation ();
                            }
                        }else {
                            show.dismiss();
                        }
                    }
                });

                btn_cancel_edit.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        showEdit.dismiss();
                    }
                });
            }
        });

 //       btn_DatePicker.setText(getTodaysDate());
    }

    /// get date and set date to button
    private  String getTodaysDate() {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        month += 1;
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        return  makeDateString (day,month,year);
    }
    private void openDatePicker(View view) {
        datePickerDialog.show();
    }
    private void initializeDatePicker() {
        DatePickerDialog.OnDateSetListener dateSetListener = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                month += 1;
                String date = makeDateString (dayOfMonth,month,year);
         //       btn_DatePicker.setText(date);
            }
        };

        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        datePickerDialog = new DatePickerDialog(MyInformation.this,dateSetListener,year,month,day);
    }

    private String makeDateString (int day, int month ,int year) {
        return getMonthFormat (month) + " " + day + " " + year;
    }

    private String getMonthFormat (int month) {
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

    private void BackPressed() {
        // Handle back press event
        Intent i = new Intent(MyInformation.this,Home.class);
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP); // Just keep this flag
        i.putExtra("My_information", "my_info");
        startActivity(i);
        finish();
    }

    // Method to check if all specified permissions are granted
    private boolean arePermissionGranted(String [] permission) {
        // Iterate through each permission in the array
        for (String permissions : permission) {
            // Check if the current permission is not granted
            if (ContextCompat.checkSelfPermission(this,permissions) != PackageManager.PERMISSION_GRANTED){
                // Return false if any permission is not granted
                return false;
            }
        }
        // Return true if all permissions are granted
        return true;
    }

    private void openImagePicker () {
        // Use the ImagePicker library to open the image picker
        ImagePicker.with(MyInformation.this)
                .crop()                   // Optional: Enable image cropping
                .compress(1024)           // Optional: Compress image to be less than 1 MB
                .maxResultSize(1080, 1080) // Optional: Limit image resolution to 1080x1080
                .start(IMAGE_REQUEST_CODE); // Start the image picker activity with a request code
    }

    @Override
    // Handle the result of the permission request
    public void onRequestPermissionsResult (int requestCode, @NonNull String [] permission, @NonNull int [] grantResults){
        super.onRequestPermissionsResult(requestCode,permission,grantResults);
        // Check if the result is for the expected request code
        if (requestCode == PERMISSION_REQUEST_CODE){
            // Iterate through the results to check if all permissions are granted
            boolean AllPermissionGranted = true;
            for (int result : grantResults) {
                if (result != PackageManager.PERMISSION_GRANTED){
                    AllPermissionGranted = false;
                    break;
                }
            }
            // If all permissions are granted, proceed with image picking
            if (AllPermissionGranted) {
                // Permission are granted, proceed with image picking
                openImagePicker();
            }else {
                // handle the case where permissions are not granted
                showPermissionRationaleDialog(permission);
            }
        }
    }

    // Show a dialog explaining why permissions are needed and request them again
    private void showPermissionRationaleDialog(String[] permission){
        boolean showRationale = false;
        // Check if we should show a rationale for each permission
        for (String permissions : permission){
            showRationale = showRationale || ActivityCompat.shouldShowRequestPermissionRationale(this,permissions);
        }
        // If we should show a rationale
        if (showRationale) {
            new AlertDialog.Builder(MyInformation.this)
                    .setTitle("Permission Required")
                    .setMessage("This app needs Camera and Storage permission to function properly. Please grant the necessary permission")
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            // Request Permission again
                            ActivityCompat.requestPermissions(MyInformation.this,permission,PERMISSION_REQUEST_CODE);
                        }
                    })
                    .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            // Inform the user that permission are necessary
                            Toast.makeText(MyInformation.this, "Permission denied. Please Enable them in App setting.",Toast.LENGTH_SHORT).show();
                        }
                    })
                    .create()
                    .show();
        }else {
            // User has permanently denied permission, direct them to app settings
            new AlertDialog.Builder(MyInformation.this)
                    .setTitle("Permission Needed")
                    .setMessage("Camera and Storage permission are require. Please enabled them in the app settings.")
                    .setPositiveButton("Open setting", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            // Open the app settings screen to allow the user to enable permissions
                            Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                            Uri uri1 = Uri.fromParts("package",getPackageName(),null);
                            intent.setData(uri1);
                            startActivity(intent);
                        }
                    })
                    .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            Toast.makeText(MyInformation.this, "Permission denied. Some features may not work.", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .create()
                    .show();
        }
    }

    // Handle the result from the image picker
    @Override
    protected void onActivityResult (int requestCode, int resultCode, Intent data){
        super.onActivityResult(requestCode,resultCode,data);
        // Check if the result is from the image picker
        if (requestCode == IMAGE_REQUEST_CODE){
            // Get the URI of the selected image
            uri = data.getData();
            if (uri != null){
                // Set the selected image URI to the ImageView
                captureImage.setImageURI(uri);
            }
        }
    }

    public void uploadImageProfile (Uri imgeuri) {
        // upload image to cloud storage
        StorageReference fileref = storageReference.child("users/"+firebaseAuth.getCurrentUser().getUid()+"/profile.jpg");
        fileref.putFile(imgeuri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                fileref.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        Picasso.get().load(uri).into(captureImage);
                    }
                });
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(MyInformation.this,e.getMessage(),Toast.LENGTH_SHORT).show();
                Log.e("boga",String.valueOf(e.getMessage()));
            }
        });

    }

    private void updateInformation () {
        // update information
        firebaseUser.updateEmail(etEmail.getText().toString()).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                    show.dismiss();
                    showInternetConnection.dismiss();
                    DatabaseReference databaseReference = firebaseDatabase.getReference("User Information").child(firebaseUser.getUid());
                    Map<String,Object> editInformation = new HashMap<>();
                    editInformation.put("fullname",etLastname.getText().toString());
                 //   editInformation.put("birthday",btn_DatePicker.getText().toString());
                    databaseReference.updateChildren(editInformation).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void unused) {
                            show.dismiss();
                            Intent i = new Intent(MyInformation.this,Sucessfull_update.class);
                            i.putExtra("My_information","My_info");
                            startActivity(i);
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(MyInformation.this,e.getMessage(),Toast.LENGTH_SHORT).show();
                        }
                    });
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(getApplicationContext(),e.getMessage(),Toast.LENGTH_SHORT).show();
            }
        });
    }

    @RequiresApi(api = Build.VERSION_CODES.TIRAMISU)
    private void checkingInternet () {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("android.net.conn.CONNECTIVITY_CHANCE");
        this.registerReceiver(new ConnectionReceiver(), intentFilter, Context.RECEIVER_EXPORTED);
        ConnectivityManager connectivityManager = (ConnectivityManager) getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        boolean isConnected = networkInfo != null && networkInfo.isConnectedOrConnecting();
        showResultsInternet (isConnected);
    }

    private void showResultsInternet (boolean isConnected) {
            if (isConnected) {
                showInternetConnection.dismiss();
                isInternetDialogVisible = true;
            }else {
                showInternetConnection.show();
                isInternetDialogVisible = false;
            }
    }

    @Override
    public void onNetworkChange (boolean isConnected) {
       // showResultsInternet(isConnected);
    }

    @Override
    protected void onResume () {
        super.onResume();
  //      checkingInternet();
    }

    @Override
    protected void onPause () {
        super.onPause();
    //    checkingInternet();
    }

    @Override
    public void onBackPressed () {
        Intent intent = new Intent(MyInformation.this,Home.class);
        intent.putExtra("SafeZone","SafeZone");
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP); // Just keep this flag
        startActivity(intent);
        finish();
    }
}