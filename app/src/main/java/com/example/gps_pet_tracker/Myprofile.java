package com.example.gps_pet_tracker;

import static android.app.Activity.RESULT_OK;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.github.dhaval2404.imagepicker.ImagePicker;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.Calendar;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link Myprofile#newInstance} factory method to
 * create an instance of this fragment.
 */
public class Myprofile extends Fragment {

    AutoCompleteTextView gender;
    Button btn_backPage, btn_backPagee, btn_DatePicker, saveInfomration, editProfile;
    EditText etFirstname, etLastname, etEmail;
    GoogleSignInOptions gso;
    GoogleSignInClient gsc;
    GoogleSignInAccount gsa;
    DatePickerDialog datePickerDialog;

    ImageView imageView;

    FirebaseAuth firebaseAuth;
    FirebaseFirestore firebaseFirestore;
    StorageReference storageReference;
    Uri uri;

    String userID;
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public Myprofile() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment Myprofile.
     */
    // TODO: Rename and change types and number of parameters
    public static Myprofile newInstance(String param1, String param2) {
        Myprofile fragment = new Myprofile();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        initializeDatePicker ();

        View view = inflater.inflate(R.layout.fragment_myprofile, container, false);

        //gender = view.findViewById (R.id.gender);
        btn_backPage = view.findViewById(R.id.btn_backPage);
        btn_backPagee = view.findViewById(R.id.btn_backPagee);
        btn_DatePicker = view.findViewById(R.id.btn_dataPicker);
        etLastname = view.findViewById(R.id.etLastname);
        etEmail = view.findViewById(R.id.etEmail);
        saveInfomration = view.findViewById(R.id.saveInfomration);
        editProfile = view.findViewById(R.id.editProfile);
        imageView = view.findViewById(R.id.imageView);

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();
        userID = firebaseAuth.getCurrentUser().getUid();
        storageReference = FirebaseStorage.getInstance().getReference();


      /*  String Gender [] = {"Male", "Female", "Other"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(),R.layout.list_gender,Gender);
        gender.setAdapter(adapter);
*/
        gsa = GoogleSignIn.getLastSignedInAccount(requireContext());

        // Set up Firestore listener
        DocumentReference documentReference = firebaseFirestore.collection("User information").document(userID);
        documentReference.addSnapshotListener(getActivity(), new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot value, @Nullable FirebaseFirestoreException error) {
                if (value != null) {
                    etEmail.setText(value.getString("email"));
                    etLastname.setText(value.getString("lastname") + " " + value.getString("firstname"));
                    btn_DatePicker.setText(value.getString("birthday"));
                } else if (gsa != null) {
                    etEmail.setText(gsa.getGivenName());
                    etLastname.setText(gsa.getDisplayName());
                    btn_DatePicker.setText("Set now");
                }
            }
        });

        editProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ImagePicker.with(Myprofile.this)
                        .crop()
                        .compress(1024)
                        .maxResultSize(1080, 1080)
                        .start();
            }
        });

        btn_backPagee.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                navigateToSettingsFragment();
            }
        });

        btn_backPage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                navigateToSettingsFragment();
            }
        });

        btn_DatePicker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openDatePicker(v);
            }


        });

        saveInfomration.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                uploadProfileImage ();
            }
        });

        // btn_DatePicker.setText(getTodaysDate());
        return view;
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
                btn_DatePicker.setText(date);
            }
        };

        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        datePickerDialog = new DatePickerDialog(requireContext(),dateSetListener,year,month,day);
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
    private void navigateToSettingsFragment() {
        FragmentManager fragmentManager = getParentFragmentManager();
        Fragment currentFragment = fragmentManager.findFragmentById(R.id.fragmentContainerView2);

        // Ensure we do not perform the transaction if the same fragment is already in place
        if (!(currentFragment instanceof Settings)) {
            fragmentManager.beginTransaction()
                    .replace(R.id.fragmentContainerView2, Settings.class, null)
                    .addToBackStack("back")
                    .setReorderingAllowed(true)
                    .commitAllowingStateLoss();
        }
    }

    @Override
    public void onActivityResult(int resultCode, int requestCode, Intent data) {
        super.onActivityResult(requestCode,resultCode,data);

        if (requestCode == ImagePicker.REQUEST_CODE && resultCode == RESULT_OK && data != null) {
            uri = data.getData();
            imageView.setImageURI(uri);
        } else {
            Toast.makeText(getActivity(), "Failed to pick image", Toast.LENGTH_SHORT).show();
        }
    }

    private void uploadProfileImage () {

    }

}