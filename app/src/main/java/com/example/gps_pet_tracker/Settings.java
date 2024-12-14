package com.example.gps_pet_tracker;

import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.IBinder;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;

public class Settings extends Fragment implements ConnectionReceiver.ReceiverListener{
    Button btn_myprofile, btn_changePassword, btn_policy, btn_logout, btn_logout_confirm, btn_logout_cancel, btn_Register, btn_set_location;
    AlertDialog dialog;
    boolean haveInternet = false;
    FirebaseAuth firebaseAuth;
    AlertDialog showInternet;
    private myForeGroundService myService;
    private boolean isBound = false;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_settings, container, false);

        btn_myprofile = view.findViewById(R.id.btn_myprofile);
        btn_changePassword = view.findViewById(R.id.btn_changePassword);
        btn_policy = view.findViewById(R.id.btn_policy);
        btn_logout = view.findViewById(R.id.btn_logout);
        btn_Register = view.findViewById(R.id.btn_Register);
        firebaseAuth = FirebaseAuth.getInstance();
        btn_set_location = view.findViewById(R.id.btn_set_location);

        // alert dialog no internet connection
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setCancelable(false);
        // get layout inflater
        LayoutInflater layoutInflater = getLayoutInflater();
        View view1 = layoutInflater.inflate(R.layout.custom_no_internet,null,false);

        Button btn_ok = view1.findViewById(R.id.btn_ok);
        builder.setView(view1);
        showInternet = builder.create();

        btn_ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showInternet.dismiss();
            }
        });

        btn_Register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getContext(),Registerr.class);
                i.putExtra("previousFragment","4");
                startActivity(i);
            }
        });

        btn_myprofile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getContext(),MyInformation.class);
                i.putExtra("previousFragment","4");
                startActivity(i);
            }
        });

        btn_set_location.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(getActivity(),User_map_set_location.class);
                startActivity(i);
            }
        });

        btn_changePassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getActivity(),change_password.class);
                startActivity(i);
            }
        });

        btn_policy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Handle button click here
                new androidx.appcompat.app.AlertDialog.Builder(getContext())
                        .setMessage("Privacy Policy for GPS Pet Tracker\n" +
                                "\n" +
                                "At GPS Pet Tracker, we are committed to safeguarding your privacy and ensuring the security of your personal data. This Privacy Policy outlines how we collect, use, and protect information when you use our mobile application.\n" +
                                "\n" +
                                "Information Collection\n" +
                                "\n" +
                                "We collect various types of information to provide and improve our services. This includes location data, which is necessary for tracking your pet's movements. We may also collect personal information such as your name, email address, and phone number when you register or interact with our app. This information helps us to authenticate users, provide customer support, and enhance the overall user experience.\n" +
                                "\n" +
                                "Use of Information\n" +
                                "\n" +
                                "The information we collect is used to deliver our services effectively. Location data is used to track and display your pet's current position on the map. Personal information is used for account management, customer support, and communication purposes. We do not use your data for any other purpose without your explicit consent.\n" +
                                "\n" +
                                "Data Security\n" +
                                "\n" +
                                "We take appropriate measures to ensure the security of your information. We use encryption to protect sensitive data during transmission and storage. Access to your personal information is restricted to authorized personnel only, and we implement industry-standard practices to safeguard against unauthorized access, disclosure, or misuse of your data.\n" +
                                "\n" +
                                "Data Sharing\n" +
                                "\n" +
                                "We do not sell, trade, or otherwise transfer your personal information to outside parties. However, we may share your data with trusted service providers who assist us in operating our app and providing services to you, as long as those parties agree to keep your information confidential. We may also disclose your information if required by law or in response to legal processes.\n" +
                                "\n" +
                                "Your Rights\n" +
                                "\n" +
                                "You have the right to access, correct, or delete your personal information. If you wish to update your data or have any concerns about how your information is being used, please contact us through the app or our support channels. We will respond to your request in a timely manner.\n" +
                                "\n" +
                                "Changes to This Policy\n" +
                                "\n" +
                                "We may update this Privacy Policy from time to time to reflect changes in our practices or legal requirements. Any modifications will be posted on our app with an updated effective date. We encourage you to review this policy periodically to stay informed about how we are protecting your information.\n" +
                                "\n" +
                                "Contact Us\n" +
                                "\n" +
                                "If you have any questions or concerns about this Privacy Policy or our data practices, please contact us at jamerodexter13@gmail.com. We are committed to addressing your inquiries and ensuring your satisfaction with our services.\n" +
                                "\n" +
                                "By using GPS Pet Tracker, you acknowledge that you have read and understood this Privacy Policy and agree to the collection and use of your information as described herein.\n" +
                                "\n" +
                                "Feel free to tailor this policy to better fit your app's specific functionalities and legal obligations.")
                        .setCancelable(true)
                        .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {

                            }
                        })
                        .create()
                        .show();

            }
        });

        btn_logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder build = new AlertDialog.Builder(requireContext());
                LayoutInflater inflater_logout = getLayoutInflater();
                View layout = inflater_logout.inflate(R.layout.custom_logout,null);

                btn_logout_confirm = (Button) layout.findViewById(R.id.btn_logout_confirm);
                btn_logout_cancel = (Button) layout.findViewById(R.id.btn_logout_cancel);
                // Set the custom layout to the AlertDialog
                build.setView(layout);


                // Create and show the AlertDialog
                dialog = build.create();
                build.setCancelable(true);
                dialog.show();

                btn_logout_confirm.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        /*GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                                .requestEmail()
                                .build();
                         gsc = GoogleSignIn.getClient(this,gso);
                        GoogleSignInAccount acct = GoogleSignIn.getLastSignedInAccount(this);


                        if (acct != null) {
                            String namee = acct.getDisplayName();
                            String emaill = acct.getEmail();
                            name.setText(namee);
                            emai.setText(emaill);
                        }*/
                        checkInternet ();

                        if (haveInternet) {
                            signOut();
                            StopHandlerLocation();
                            dialog.dismiss();
                        }else {
                            showInternet.show();
                        }
                    }
                });

                btn_logout_cancel.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog.dismiss();
                    }
                });
            }
        });

        return view;
    }

    private void signOut() {
        if (getActivity() instanceof Home) {
             ((Home) getActivity()).signOut();
        }
    }


    // Method to bind to the service
    private void bindService() {
        Intent intent = new Intent(getActivity(), myForeGroundService.class);
        getActivity().bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);
    }

    // Service connection
    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder binder) {
            myForeGroundService.LocalBinder localBinder = (myForeGroundService.LocalBinder) binder;
            myService = localBinder.getService();
            isBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            isBound = false;
        }
    };

    private void StopHandlerLocation() {
        if (isBound && myService != null) {
            myService.StopHandlerLocation();
        }
    }

    private void checkInternet () {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("android.net.conn.CONNECTIVITY_CHANGE");
        getContext().registerReceiver(new ConnectionReceiver(),intentFilter);
        ConnectivityManager connectivityManager = (ConnectivityManager) getContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        boolean isConnected = networkInfo != null && networkInfo.isConnected();
        showResultsInternet(isConnected);
    }

    private void showResultsInternet(boolean isConnected) {
        if (isConnected) {
            haveInternet = true;
        }else {
            haveInternet = false;
        }
    }

    @Override
    public void onNetworkChange (boolean isConnected) {
        showResultsInternet(isConnected);
    }
    // If GoogleSignInClient is null, sign out directly from Firebase
         // Finish current activity to prevent returning to this screen
        // Navigate to login screen
       /* if (gsc != null) {
            gsc.signOut().addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()) {
                        // Redirect user to login screen or perform any other action
                        startActivity(new Intent(requireContext(), login.class));
                        requireActivity().finish();
                    } else {
                        // Handle sign out failure
                    }

                }
            });
        } else {
            // If GoogleSignInClient is null, sign out directly from Firebase
            firebaseAuth.signOut();
            navigateToLogin(); // Navigate to login screen
        }
    }
    private void navigateToLogin() {
        Intent intent = new Intent(getContext(), login.class);
       // intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish(); // Finish current activity to prevent returning to this screen
    }*/
    @Override
    public void onStart() {
        super.onStart();
        bindService(); // Bind to the service when the fragment starts
    }

    @Override
    public void onStop() {
        super.onStop();
        if (isBound) {
            getActivity().unbindService(serviceConnection);
            isBound = false;
        }
    }
}