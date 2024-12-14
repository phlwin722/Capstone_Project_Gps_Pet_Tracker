package com.example.gps_pet_tracker;

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
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class History extends Fragment implements ConnectionReceiver.ReceiverListener{
    Button Registerr;
    SearchView searchView;
    FrameLayout loading_dataView,no_data_View,no_internet_connection;
    RecyclerView recylerView;
    List<HistoryFetch> petListt;
    HistoryFetchAdapter historyFetchAdapter;
    ConnectionReceiver connectionReceiver;
    FirebaseAuth firebaseAuth;
    FirebaseFirestore firebaseFirestore;
    FirebaseDatabase firebaseDatabase;
    HistoryFetch historyFetch;
    String userID;
    boolean isInternetDialogVisible = false;
    boolean haveData = false;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_historyy, container, false);
        Registerr = view.findViewById(R.id.Registerr);
        searchView = view.findViewById(R.id.searchh);
        searchView.clearFocus();
        loading_dataView = view.findViewById(R.id.loading_dataView);
        no_internet_connection = view.findViewById(R.id.no_internet_connection);
        recylerView = view.findViewById(R.id.recylerView);
        no_data_View = view.findViewById(R.id.no_data_View);

        firebaseDatabase = FirebaseDatabase.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();
        firebaseAuth = FirebaseAuth.getInstance();
        userID = firebaseAuth.getCurrentUser().getUid();

        fetchDataFromFireBase();
        connectionReceiver = new ConnectionReceiver();

        // Initialize RecyclerView and set its layout manager
        recylerView.setLayoutManager(new LinearLayoutManager(getContext()));

        // Initialize pet list and adapter
        petListt = new ArrayList<>();
        // when user click each of recycler view
        historyFetchAdapter = new HistoryFetchAdapter(petListt, new HistoryFetchAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(HistoryFetch pet) {
                Intent i = new Intent(getActivity(),MapRoutePetHistory.class);
                // Put the selected 'pet' object into the Intent as extra data
                // Cast 'pet' to Serializable to make it transferable between activities
                i.putExtra("pet", (Serializable) pet);
                startActivity(i);
            }
        });

        // setAdapter to recylerview
        recylerView.setAdapter(historyFetchAdapter);

        // check if user focus on search view
        searchView.setOnQueryTextFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
                if (b){
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
                searchList(newText);
                return true;
            }
        });

        Registerr.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getContext(), Registerr.class);
                i.putExtra("previousFragment","2");
                startActivity(i);
            }
        });
        return view;
    }

    private void searchList (String text) {
        ArrayList<HistoryFetch> searchList = new ArrayList<>();

        for (HistoryFetch historyFetch1 : petListt) {
            Log.e("GEEEEeee",String.valueOf(historyFetch1));
            if (historyFetch1.getArduinoId().toLowerCase().contains(text.toLowerCase())){
                searchList.add(historyFetch1);
            }else if (historyFetch1.getPetName().toLowerCase().contains(text.toLowerCase())) {
                searchList.add(historyFetch1);
            }
        }
        historyFetchAdapter.searchHistory(searchList);
    }

    public void fetchDataFromFireBase () {
        DatabaseReference databaseReference = firebaseDatabase.getReference("Pet Information").child(userID);

        checkInterNet();

        loading_dataView.setVisibility(View.VISIBLE);
        no_data_View.setVisibility(View.GONE);
        recylerView.setVisibility(View.GONE);
        no_internet_connection.setVisibility(View.GONE);

        if (!isInternetDialogVisible){
            no_internet_connection.setVisibility(View.VISIBLE);
            loading_dataView.setVisibility(View.GONE);
            recylerView.setVisibility(View.GONE);
            no_data_View.setVisibility(View.GONE);
        }

        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                petListt.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()){
                    // Deserialize each snapshot to a Marker object add to the list
                     historyFetch = dataSnapshot.getValue(HistoryFetch.class);

                    if (historyFetch != null) {
                       // calculateDistance(dataSnapshot);
                        petListt.add(historyFetch);
                        historyFetchAdapter.notifyDataSetChanged();
                        haveData = true;
                    }
                }

                if (haveData) {
                    checkInterNet();
                    no_internet_connection.setVisibility(View.GONE);
                    recylerView.setVisibility(View.VISIBLE);
                    //calculateDistance(snapshot);
                    no_data_View.setVisibility(View.GONE);
                    loading_dataView.setVisibility(View.GONE);
                }else {
                    recylerView.setVisibility(View.GONE);
                    loading_dataView.setVisibility(View.GONE);
                    no_data_View.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    public void showTablayout () {
        if (getContext() instanceof Home) {
            ((Home) getContext()).showTabLayout();
        }
    }

    public void hideTablayout () {
        if (getContext() instanceof  Home) {
            ((Home) getContext()).hideTabLayout();
        }
    }

    public void checkInterNet() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("android.net.conn.CONNECTIVITY_CHANGE");

        // Make sure the context is valid
        Context context = getActivity();
        if (context != null) {
            connectionReceiver = new ConnectionReceiver();
            context.registerReceiver(connectionReceiver, intentFilter);

            ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            if (connectivityManager != null) {
                NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
                boolean isConnected = networkInfo != null && networkInfo.isConnected();
                showResultInternet(isConnected);
            }
        } else {
            // Handle null context scenario (optional logging)
        }
    }


    public void showResultInternet(boolean isConnected) {
        if (isConnected){
            isInternetDialogVisible = true;
        }else {
            isInternetDialogVisible = false;
        }
    }

    @Override
    public void onNetworkChange (boolean isConnected){
        showResultInternet(isConnected);
    }

    @Override
    public void onStop() {
        super.onStop();
        // Unregister the receiver when the fragment is no longer visible
        Context context = getContext();

    }

    @Override
    public void onDestroy() {
        super.onDestroy();

    }


}