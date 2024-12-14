package com.example.gps_pet_tracker;

import static android.content.ContentValues.TAG;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class Notification extends Fragment implements ConnectionReceiver.ReceiverListener{

    FirebaseAuth firebaseAuth;
    String userID;
    DatabaseReference databaseReference;
    private List<NotificationFetch> notificationFetchList;
    private NoticationFetchAdapter noticationFetchAdapter;
    private RecyclerView Recycler;
    ConnectionReceiver connectionReceiver;
    private View noDataView;
    private FrameLayout loadingView, view_no_internet;
    boolean isHaveInternet = false;

    Button Registerr;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        firebaseAuth = FirebaseAuth.getInstance();
        userID = firebaseAuth.getCurrentUser().getUid();

        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_notification, container, false);
        Registerr = view.findViewById(R.id.Registerr);
        noDataView = view.findViewById(R.id.noDataView);
        loadingView = view.findViewById(R.id.loadingView);
        view_no_internet = view.findViewById(R.id.view_no_internet);

        // set up recycler view
        Recycler = view.findViewById(R.id.recyclerNotification);
        Recycler.setLayoutManager(new LinearLayoutManager(getContext()));

        // Initialize data list
        notificationFetchList = new ArrayList<>();

        noticationFetchAdapter = new NoticationFetchAdapter( notificationFetchList);
        Recycler.setAdapter(noticationFetchAdapter);

        // Fetch data from Firebase Database
        fetchDataFromFirebase();

        Registerr.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getContext(),Registerr.class);
                i.putExtra("previousFragment","3");
                startActivity(i);
            }
        });

        return view;
    }
    public void fetchDataFromFirebase() {

        checkInternet();
        
        loadingView.setVisibility(View.VISIBLE);
        noDataView.setVisibility(View.GONE);
        Recycler.setVisibility(View.GONE);
        view_no_internet.setVisibility(View.GONE);

        if (userID == null) {
            Log.e(TAG, "UserID is null");
            return;
        }
         if (!isHaveInternet) {
             view_no_internet.setVisibility(View.VISIBLE);
             noDataView.setVisibility(View.GONE);
             loadingView.setVisibility(View.GONE);
             Recycler.setVisibility(View.GONE);
         }

        // Initialize Firebase Database reference
        databaseReference = FirebaseDatabase.getInstance().getReference("User Notification").child(userID);

        // Attach a listener to read the data at our reference
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                notificationFetchList.clear(); // Clear existing data (if any)
                Log.d(TAG, "Data snapshot received: " + snapshot.toString()); // Debug log
                boolean haveData = false;

                // Iterate through all data in the database
                for (DataSnapshot snapshot1 : snapshot.getChildren()) {
                    NotificationFetch data = snapshot1.getValue(NotificationFetch.class);
                    if (data != null) {
                        notificationFetchList.add(data);
                        haveData = true;
                        Log.d(TAG, "Data added: " + data.getNotification());
                    } else {
                        Log.d(TAG, "Data is null for snapshot: " + snapshot1.toString());
                    }
                }

                // Sort the list in descending order based on the timestamp
                Collections.sort(notificationFetchList, new Comparator<NotificationFetch>() {
                    @Override
                    public int compare(NotificationFetch n1, NotificationFetch n2) {
                        return Long.compare(n2.getTimestamp(), n1.getTimestamp()); // Assuming getTimestamp() returns a long
                    }
                });

                if (haveData) {
                    loadingView.setVisibility(View.GONE);
                    noDataView.setVisibility(View.GONE);
                    view_no_internet.setVisibility(View.GONE);
                    Recycler.setVisibility(View.VISIBLE);
                    noticationFetchAdapter.notifyDataSetChanged(); // Notify adapter of data changes
                }else {
                    loadingView.setVisibility(View.GONE);
                    noDataView.setVisibility(View.VISIBLE);
                    view_no_internet.setVisibility(View.GONE);
                    Recycler.setVisibility(View.GONE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle errors encountered while fetching data
                Log.e(TAG, "Failed to read value.", error.toException());
            }
        });
    }

    public void checkInternet () {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("android.net.conn.CONNECTIVITY_CHANGE");
        Context context = getActivity();
        if (context != null) {
            connectionReceiver = new ConnectionReceiver();
            context.registerReceiver(connectionReceiver, intentFilter);

            ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            if (connectivityManager != null) {
                NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
                boolean isConnected = networkInfo != null && networkInfo.isConnected();
                showResultsInternet(isConnected);
            }
        } else {
            // Handle null context scenario (optional logging)
        }
    }

    public void showResultsInternet (boolean isConnected) {
        if (isConnected) {
            isHaveInternet = true;
        }
        else {
            isHaveInternet = false;
        }
    }

    @Override
    public void onNetworkChange (boolean isConnected) {
        showResultsInternet(isConnected);
    }
    @Override
    public void onResume () {
        super.onResume();
        checkInternet();
    }

    @Override
    public void onPause () {
        super.onPause();
        checkInternet();
    }

    @Override
    public void onStop() {
        super.onStop();
        // Unregister the receiver when the fragment is no longer visible
        Context context = getContext();
        if (context != null && connectionReceiver != null) {
            context.unregisterReceiver(connectionReceiver);
        }
    }

}