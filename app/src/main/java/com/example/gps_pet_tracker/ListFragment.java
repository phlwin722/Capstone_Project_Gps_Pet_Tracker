package com.example.gps_pet_tracker;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class ListFragment extends Fragment implements ConnectionReceiver.ReceiverListener{
    Button btn_notification;
    private RecyclerView recyclerView;
    View noDataView;
    FrameLayout loadingDataView, no_internet_connection;
    PetAdapter petAdapter;
    private List<petInformationStore> petList;
    String userID;
    FirebaseAuth firebaseAuth;
    private ConnectionReceiver connectionReceiver;
    SearchView searchView;
    boolean haveData = false;
    boolean isInternetDialogVisible = false;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_list, container, false);

        btn_notification = view.findViewById(R.id.btn_notification);
        recyclerView = view.findViewById(R.id.recylerView);
        searchView = view.findViewById(R.id.search);
        noDataView = view.findViewById(R.id.no_data_View);
        loadingDataView = view.findViewById(R.id.loading_dataView);
        no_internet_connection = view.findViewById(R.id.no_internet_connection);
        searchView.clearFocus();

        firebaseAuth = FirebaseAuth.getInstance();
        userID = firebaseAuth.getCurrentUser().getUid();

        // Initialize RecyclerView and set its layout manager
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        // Initialize pet list and adapter
        petList = new ArrayList<>();
        petAdapter = new PetAdapter(petList, new PetAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(petInformationStore pet) {
                // Handle item click: navigate to PetDetailActivity and pass the clicked Pet object
                Intent intent = new Intent(getContext(), Pet_info_details.class);
                intent.putExtra("pet", (Serializable) pet);
                startActivity(intent);
            }
        });

        connectionReceiver = new ConnectionReceiver();

        // Set adapter to RecyclerView
        recyclerView.setAdapter(petAdapter);

        fetchDataFromFirebase ();

        searchView.setOnQueryTextFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean hasFocus) {
                if (hasFocus) {
                    hideTablayout();
                }else {
                    showTablayout();
                }
            }
        });

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                // pending
                searchList(newText);
                return true;
            }
        });

        btn_notification.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getContext(), Registerr.class);
                i.putExtra("previousFragment", "1");
                startActivity(i);
            }
        });
        return view;
    }
    public void searchList (String text) {
        ArrayList<petInformationStore> searchList = new ArrayList<>();

        for (petInformationStore petInformationStore : petList) {
            if (petInformationStore.getArduinoId().toLowerCase().contains(text.toLowerCase())) {
                searchList.add(petInformationStore);
            } else if (petInformationStore.getPetName().toLowerCase().contains(text.toLowerCase())) {
                searchList.add(petInformationStore);
            }
        }
        petAdapter.searchPetAdapter(searchList);
    }

    public void fetchDataFromFirebase () {
        loadingDataView.setVisibility(View.VISIBLE);
        noDataView.setVisibility(View.GONE);
        recyclerView.setVisibility(View.GONE);
        no_internet_connection.setVisibility(View.GONE);

        checkInternet();

        if (!isInternetDialogVisible) {
            no_internet_connection.setVisibility(View.VISIBLE);
            loadingDataView.setVisibility(View.GONE);
            noDataView.setVisibility(View.GONE);
            recyclerView.setVisibility(View.GONE);
        }

        // Reference to the Firebase Realtime Database
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Pet Information").child(userID);

        // Retrieve data from Firebase Realtime Database
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                petList.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    // Deserialize each snapshot to a petInformationStore object and add to the list
                    petInformationStore pet = snapshot.getValue(petInformationStore.class);
                    if (pet != null) {
                        pet.setKey(snapshot.getKey());
                        petList.add(pet);
                        haveData = true;
                    }
                }
                if (haveData) {
                    // Notify adapter about data changes
                    checkInternet();
                    no_internet_connection.setVisibility(View.GONE);
                    recyclerView.setVisibility(View.VISIBLE);
                    loadingDataView.setVisibility(View.GONE);
                    noDataView.setVisibility(View.GONE);
                    petAdapter.notifyDataSetChanged();
                }else {
                    noDataView.setVisibility(View.VISIBLE);
                    loadingDataView.setVisibility(View.GONE);
                    recyclerView.setVisibility(View.GONE);
                    no_internet_connection.setVisibility(View.GONE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle possible errors
            }
        });
    }

    //hide tablayout from home activity
    public void hideTablayout () {
        if (getContext() instanceof Home){
             ((Home) getContext()).hideTabLayout();
        }
    }

    // show tablayout from home activity.
    public void showTablayout (){
        if (getContext() instanceof Home) {
            ((Home) getContext()).showTabLayout();
        }
    }

    public void checkInternet () {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("android.net.conn.CONNECTIVITY_CHANGE");
        Context context = getActivity();
        if (context != null) {
            ConnectionReceiver connectionReceiver = new ConnectionReceiver();
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
            isInternetDialogVisible = true;
        }else {
            isInternetDialogVisible = false;
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
    public void onDestroy() {
        super.onDestroy();
        //    getContext().unregisterReceiver(connectionReceiver);
    }
}
