package com.example.gps_pet_tracker;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.Serializable;

public class Pet_info_details extends AppCompatActivity {

    Button btn_backPage, btn_edit, btn_delete;
    TextView petName, Category, Status, Distance, Battery, arduinoID,loadd, expired;
    AlertDialog Internet, Delete,loading;
    petInformationStore pet;
    ImageView captureImage;
    FirebaseDatabase firebaseDatabase;
    FirebaseAuth firebaseAuth;
    boolean haveInternet;
    ProgressBar Progress;
    String arduinoId, userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pet_info_details);

        btn_edit = findViewById(R.id.btn_edit);
        btn_delete = findViewById(R.id.btn_delete);
        btn_backPage = findViewById(R.id.btn_backPage);
        petName = findViewById(R.id.petName);
        Category = findViewById(R.id.Category);
        Status = findViewById(R.id.Status);
        Distance = findViewById(R.id.Distance);
        arduinoID = findViewById(R.id.arduinoID);
        captureImage = findViewById(R.id.captureImage);
        Battery = findViewById(R.id.Battery);
        Progress = findViewById(R.id.Progress);
        loadd = findViewById(R.id.load);
        expired = findViewById(R.id.expired);

        firebaseAuth = FirebaseAuth.getInstance();
        userId = firebaseAuth.getUid();
        firebaseDatabase = FirebaseDatabase.getInstance();

        // Get the Pet object passed from ListFragment
        pet = (petInformationStore) getIntent().getSerializableExtra("pet");

        AlertDialog.Builder deleter = new AlertDialog.Builder(this);
        View view = getLayoutInflater().inflate(R.layout.custom_delete_alert,null);
        deleter.setView(view);

        Delete = deleter.create();

        AlertDialog.Builder internt = new AlertDialog.Builder(this);
        View view2 = getLayoutInflater().inflate(R.layout.custom_no_internet,null);
        internt.setView(view2);

        Internet = internt.create();

        AlertDialog.Builder load = new AlertDialog.Builder(this);
        View view1 = getLayoutInflater().inflate(R.layout.custom_loading_process,null);
        load.setView(view1);

        loading = load.create();;
        loading.show();

        Button delete_info = view.findViewById(R.id.btn_logout_confirm);
        Button Cancel = view.findViewById(R.id.btn_logout_cancel);

        // Set values from 'pet' object to views
        if (pet != null) {
            StorageReference profileref = FirebaseStorage.getInstance().getReference("Pet Image").child(firebaseAuth.getCurrentUser().getUid()).child(pet.getArduinoId()).child("/profile.jpg");
            profileref.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                @Override
                public void onSuccess(Uri uri) {
                    Glide.with(getApplicationContext()).load(uri).into(captureImage);
                    Progress.setVisibility(View.GONE);
                    captureImage.setVisibility(View.VISIBLE);
                }
            });
            arduinoId = pet.getArduinoId();
            arduinoID.setText(pet.getArduinoId());
            petName.setText(pet.getPetName());
            Category.setText(pet.getPetCategory());
            Distance.setText(pet.getCurrentDistance());
            loadd.setText(pet.getPetDataPlan());
         //   expired.setText(pet.getPetDataMonthorDay() + " " + pet.getPetDayORMONTH());
            expired.setText(pet.getExpired());
            if (pet.getStatus() != null){
                Status.setText(pet.getStatus());
            }else{
                Status.setText("Unconfigured");
            }

            if (pet.getBattery().equals("101")) {
                Battery.setText("Unconfigured");
            }else {
                Battery.setText(pet.getBattery());
            }
            // Load the pet image using Picasso or Glide
            loading.dismiss();
        }

        Button btn_ok = view2.findViewById(R.id.btn_ok);
        btn_ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Internet.dismiss();
            }
        });

        delete_info.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                loading.show();
                checkInternet();
                if (haveInternet){
                    Delete.dismiss();
                    DatabaseReference databaseReference = firebaseDatabase.getReference("Pet Information")
                            .child(userId)
                            .child(arduinoId);

                    databaseReference.removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                DatabaseReference databaseReference1 = firebaseDatabase.getReference("HistoryOfPet")
                                        .child(arduinoId);

                                databaseReference1.removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful()) {
                                            DatabaseReference databaseReference2 = firebaseDatabase.getReference("DeviceID")
                                                    .child(arduinoId);

                                            databaseReference2.removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {
                                                    if (task.isSuccessful()) {
                                                        DatabaseReference databaseReference3 = firebaseDatabase.getReference("ArduinoDeviceId")
                                                                .child(arduinoId);

                                                        databaseReference3.removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<Void> task) {
                                                                if (task.isSuccessful()) {
                                                                    StorageReference storageReference = FirebaseStorage.getInstance().getReference("Pet Image")
                                                                            .child(userId)
                                                                            .child(arduinoId)
                                                                            .child("profile.jpg");

                                                                    storageReference.delete().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                        @Override
                                                                        public void onComplete(@NonNull Task<Void> task) {
                                                                            if (task.isSuccessful()) {
                                                                                loading.dismiss();
                                                                                Intent i = new Intent(Pet_info_details.this,Sucessfull_delete_information.class);
                                                                                i.putExtra("previousFragment","1");
                                                                                startActivity(i);
                                                                            }
                                                                        }
                                                                    });
                                                                }
                                                            }
                                                        });
                                                    }
                                                }
                                            });
                                        }
                                    }
                                });
                            }
                        }
                    });
                }
            }
        });

        Cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Delete.dismiss();
            }
        });

        btn_edit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Pet_info_details.this, Pet_Information_details.class);
                intent.putExtra("pet", (Serializable) pet);
                startActivity(intent);
            }
        });

        btn_backPage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                back_to_List_Fragment();
            }
        });

        btn_delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Delete.show();
            }
        });

    }

    // back to the lis fragment
    public void back_to_List_Fragment () {
        Intent i = new Intent(getApplicationContext(),Home.class);
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP); // Just keep this flag
        i.putExtra("previousFragment","1");
        startActivity(i);
        finish();
    }

    private void checkInternet () {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("android.net.conn.CONNECTIVITY_CHANGE");
        this.registerReceiver(new ConnectionReceiver(), intentFilter);
        ConnectivityManager connectivityManager = (ConnectivityManager) this.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        boolean isConnected = networkInfo != null && networkInfo.isConnected();
        showResultsInternet (isConnected);
    }

    private void showResultsInternet (boolean isConnetec) {
        if (isConnetec) {
            haveInternet = true;
        }else {
            Delete.dismiss();
            Internet.show();
            loading.dismiss();
            haveInternet = false;
        }
    }

    @Override
    public void onBackPressed () {
        Intent intent = new Intent(Pet_info_details.this,Home.class);
        intent.putExtra("select_list_fragment","select_list_fragment");
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP); // Just keep this flag
        startActivity(intent);
        finish();
    }
}