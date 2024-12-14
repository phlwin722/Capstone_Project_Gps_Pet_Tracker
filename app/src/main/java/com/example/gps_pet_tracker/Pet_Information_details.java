package com.example.gps_pet_tracker;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.github.dhaval2404.imagepicker.ImagePicker;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class Pet_Information_details extends AppCompatActivity {
    ImageView captureImage;
    EditText etPetname, etArduinoID;
    Spinner tilCategory;
    TextInputLayout inputlayoutArduinoId, inputlayoutPetname, inputlayoutPetcategoryy;
    boolean save = true;
    Button btn_edit_Profile, btn_backPage, btn_edit_information, btn_backPagee, btn_yes_to_edit, btn_cancel_edit;
    String arduinoID, userID, petCategory = "", petName, categ;
    petInformationStore pet;
    FirebaseAuth firebaseAuth;
    DatabaseReference databaseReference;
    FirebaseStorage firebaseStorage;
  //  TextView level;
    StorageReference storageReference;
    ProgressBar Progress;
    FirebaseFirestore firebaseFirestore;
    FirebaseDatabase firebaseDatabase;
    AlertDialog alertLoad, Internet;
    Uri urii;
    boolean haveInternet = false;
    int petCategoryy = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pet_information_details);

        // Enable edge-to-edge display
        EdgeToEdge.enable(this);

        // Initialize Firebase instances
        firebaseAuth = FirebaseAuth.getInstance();
        firebaseStorage = FirebaseStorage.getInstance();
        userID = firebaseAuth.getCurrentUser().getUid();
        storageReference = firebaseStorage.getReference();

        AlertDialog.Builder aler = new AlertDialog.Builder(Pet_Information_details.this);
        aler.setCancelable(false);
        View view = getLayoutInflater().inflate(R.layout.custom_loading_process,null);
        aler.setView(view);

        alertLoad = aler.create();

        // Get the Pet object passed from ListFragment
        pet = (petInformationStore) getIntent().getSerializableExtra("pet");

        // Initialize views
        btn_edit_Profile = findViewById(R.id.btn_edit_Profile);
        btn_backPagee = findViewById(R.id.btn_backPagee);
        Progress = findViewById(R.id.Progress);
        btn_backPage = findViewById(R.id.btn_backPage);
        btn_edit_information = findViewById(R.id.btn_edit_information);
        captureImage = findViewById(R.id.captureImage);
        etPetname = findViewById(R.id.etPetname);
        etArduinoID = findViewById(R.id.etArduinoID);
        tilCategory = findViewById(R.id.tilCategory);
        inputlayoutPetcategoryy = findViewById(R.id.inputlayoutPetcategoryyy);
        inputlayoutPetname = findViewById(R.id.inputlayoutPetname);
      //  level = findViewById(R.id.level);
        inputlayoutArduinoId = findViewById(R.id.inputlayoutArduinoId);
        firebaseFirestore = FirebaseFirestore.getInstance();
        firebaseDatabase = FirebaseDatabase.getInstance();

        AlertDialog.Builder netWork = new AlertDialog.Builder(this);
        View view1 = getLayoutInflater().inflate(R.layout.custom_no_internet,null);
        netWork.setView(view1);

        Button btn_ok = view1.findViewById(R.id.btn_ok);

        btn_ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Internet.dismiss();
            }
        });

        Internet = netWork.create();

        // Set values from 'pet' object to views
        if (pet != null) {
            arduinoID = pet.getArduinoId();
            etArduinoID.setText(pet.getArduinoId());
            etPetname.setText(pet.getPetName());

       /*     if (Integer.parseInt(pet.petDistance) > 20 && Integer.parseInt(pet.petDistance) < 45) {
                level.setText("Standard");
            }else if (Integer.parseInt(pet.petDistance) > 50 && Integer.parseInt(pet.petDistance) < 80) {
                level.setText("Hi-Risk");
            }else if (Integer.parseInt(pet.petDistance) > 85 && Integer.parseInt(pet.petDistance) < 115) {
                level.setText("Very Dangerous");
            }else if (Integer.parseInt(pet.petDistance) > 120 ) {
                level.setText("Extremely dangerous");
            }*/
            // Load the pet image using Picasso or Glide
            StorageReference profileref = storageReference.child("Pet Image").child(firebaseAuth.getCurrentUser().getUid()).child(pet.getArduinoId()).child("/profile.jpg");
            profileref.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                @Override
                public void onSuccess(Uri uri) {
                    Glide.with(getApplicationContext()).load(uri).into(captureImage);
                    Progress.setVisibility(View.GONE);
                    captureImage.setVisibility(View.VISIBLE);
                }
            });
        }

        // Initialize the spinner with the array from strings.xml
        ArrayAdapter<CharSequence> adapterr = new ArrayAdapter<CharSequence>(
                this, R.layout.custom_spinner_item, getResources().getStringArray(R.array.animal_options)) {

            @Override
            public View getDropDownView(int position, View convertView, ViewGroup parent) {
                View view = super.getDropDownView(position, convertView, parent);
                TextView textView = view.findViewById(R.id.spinner_item);

                // Make the placeholder item (position == 0) appear differently
                if (position == 0) {
                    textView.setTextColor(Color.GRAY);  // Set the placeholder color to gray
                    textView.setTypeface(null, Typeface.ITALIC);  // Make the placeholder italic
                } else {
                    textView.setTextColor(Color.BLACK);  // Normal color for valid items
                    textView.setTypeface(null, Typeface.NORMAL);  // Normal style for valid items
                }

                return view;
            }
        };
        tilCategory.setAdapter(adapterr);

        if (pet.petCategory.equals("Dog")) {
            tilCategory.setSelection(1);
        } else {
            tilCategory.setSelection(2);
        }

        // Get selected spinner item
        tilCategory.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long l) {
                petCategoryy = position;
                    petCategory = adapterView.getItemAtPosition(position).toString();

            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                petCategory = "";
            }
        });

        btn_backPage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                back_to_List_Fragment ();
            }
        });

        btn_backPagee.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                back_to_List_Fragment ();
            }
        });

        btn_edit_Profile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ImagePicker.with(Pet_Information_details.this)
                        .crop()
                        .start();
            }
        });

        btn_edit_information.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Retrieve values from EditText fields
                arduinoID = etArduinoID.getText().toString().trim();
                petName = etPetname.getText().toString().trim();
                categ = petCategory;

                    if (!arduinoID.isEmpty()) {
                        inputlayoutArduinoId.setErrorEnabled(false);

                        if (arduinoID.length() !=11)  {
                            inputlayoutArduinoId.setError("Device ID must be 11 characters");
                            save = false;
                        }else {
                            inputlayoutArduinoId.setErrorEnabled(false);
                            if (!petName.isEmpty()) {
                                inputlayoutPetname.setErrorEnabled(false);

                                if (petCategoryy == 0 || petCategory.isEmpty()) {
                                    save = false;
                                    Toast.makeText(Pet_Information_details.this,"Please select pet category",Toast.LENGTH_SHORT).show();
                                    inputlayoutPetcategoryy.setError("Please select pet category");
                                } else {
                                    inputlayoutPetcategoryy.setErrorEnabled(false); // Ensure to clear error if category is selected
                                    save = true;
                                }

                            } else {
                                save = false;
                                inputlayoutPetname.setError("Field cannot be blank");
                            }
                        }
                    }else {
                        save = false;
                        inputlayoutArduinoId.setError("Field cannot be blank");
                    }

                // If all validations passed, proceed with upload and update
                if (save) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(Pet_Information_details.this);
                    builder.setCancelable(false);
                    LayoutInflater layoutInflater = getLayoutInflater();
                    View view2 = layoutInflater.inflate(R.layout.custom_update_alert, (ViewGroup) view.findViewById(R.id.custom_verification_edit));
                    builder.setView(view2);
                    btn_yes_to_edit =  view2.findViewById(R.id.btn_yes_confirm);
                    btn_cancel_edit = view2.findViewById(R.id.btn_cancel);

                    AlertDialog showEditCustom = builder.create();
                    showEditCustom.show();

                    btn_yes_to_edit.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            checkInternet();

                            if (haveInternet) {
                                alertLoad.show();
                                showEditCustom.dismiss();
                                uploadImage();
                                updatePetInfoInRealtimeDatabase();
                            }else {
                                showEditCustom.dismiss();
                            }
                        }
                    });

                    btn_cancel_edit.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            showEditCustom.dismiss();
                        }
                    });
                }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == ImagePicker.REQUEST_CODE && resultCode == RESULT_OK && data != null) {
            urii = data.getData();
            if (urii != null) {
                captureImage.setImageURI(urii);
            }
        }
    }

    private void uploadImage() {
        if (urii != null) {
                    try {
                        arduinoID = pet.getArduinoId();
                        storageReference = FirebaseStorage.getInstance().getReference("Pet Image/" + userID + "/" + arduinoID + "/profile.jpg");
                // Check if the file exists
                storageReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        storageReference.delete().addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    uploadNewImage (urii, userID, arduinoID);
                                }
                            }
                        });
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // File does not exist
                        Toast.makeText(Pet_Information_details.this, "Pet image does not exist", Toast.LENGTH_SHORT).show();
                    }
                });
            } catch (Exception e) {
                Toast.makeText(Pet_Information_details.this, "Error creating storage reference", Toast.LENGTH_SHORT).show();
                return;
            }
        }
    }

    private void uploadNewImage (Uri uri, String userID, String arduinoID) {
        if (uri != null) {
            StorageReference fileRef = FirebaseStorage.getInstance().getReference("Pet Image/" + userID + "/" + arduinoID + "/profile.jpg");

            fileRef.putFile(uri)
                    .addOnSuccessListener(taskSnapshot -> {
                        // Image uploaded successfully, get its download URL
                        fileRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                // Now proceed to store pet information in Firebase Realtime Database
                            }
                        }).addOnFailureListener(e -> {
                            Toast.makeText(getApplicationContext(), "Failed to upload image: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        });
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(getApplicationContext(), "Failed to upload image: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        }else {
            updatePetInfoInRealtimeDatabase () ;
        }
    }

    private void updatePetInfoInRealtimeDatabase() {

        DatabaseReference databaseReference = firebaseDatabase.getReference("Pet Information").child(userID).child(arduinoID);

        Map<String, Object> petInfonew = new HashMap<>();
        petInfonew.put("petName", petName);
        petInfonew.put("petCategory", petCategory);

        databaseReference.updateChildren(petInfonew).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                Intent i = new Intent(Pet_Information_details.this,Sucessfull_update.class);
                i.putExtra("pet_information_details","Pet information updated successfully");
                startActivity(i);
                alertLoad.dismiss();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(getApplicationContext(), "Failed to update pet information: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                Log.e("UpdateData", "Update failed: " + e.getMessage());
            }
        });
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

    private void showResultsInternet (boolean isConnected) {
        if (isConnected) {
            haveInternet = true;
        }else {
            Internet.show();
            haveInternet = false;
        }
    }

    // back to the lis fragment
    public void back_to_List_Fragment () {
        Intent i = new Intent(getApplicationContext(),Pet_info_details.class);
        i.putExtra("pet",(Serializable) pet);

        startActivity(i);
    }

    @Override
    public void onBackPressed () {
        super.onBackPressed();
        Intent intent = new Intent(Pet_Information_details.this,Pet_info_details.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP); // Just keep this flag
        intent.putExtra("pet",(Serializable) pet);
        startActivity(intent);
        finish();
    }
}
