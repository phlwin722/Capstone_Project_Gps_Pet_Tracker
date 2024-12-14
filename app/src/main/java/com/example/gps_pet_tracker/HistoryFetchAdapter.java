package com.example.gps_pet_tracker;

// Import statements for necessary libraries and components

import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.List;

public class HistoryFetchAdapter extends RecyclerView.Adapter<HistoryFetchAdapter.ViewHolder> {

    private List<HistoryFetch> petListt; // List to hold Marker objects for display
    private OnItemClickListener listener; // Listener for item click events
    private FirebaseAuth firebaseAuth; // Firebase Authentication instance
    private FirebaseStorage firebaseStorage; // Firebase Storage instance
    private StorageReference storageReference; // Reference to Firebase Storage

    // Interface to handle item click events
    public interface OnItemClickListener {
        void onItemClick(HistoryFetch pet);
    }

    // Constructor for the adapter
    public HistoryFetchAdapter(List<HistoryFetch> petlist, OnItemClickListener listener) {
        this.petListt = petlist; // Initialize pet list
        this.listener = listener; // Initialize item click listener

        // Initialize Firebase instances
        firebaseAuth = FirebaseAuth.getInstance();
        firebaseStorage = FirebaseStorage.getInstance();
        storageReference = firebaseStorage.getReference(); // Reference to Firebase Storage root
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Inflate the layout for individual items in the RecyclerView
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.custom_history_listview, parent, false);
        return new ViewHolder(view); // Return a new ViewHolder instance
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        // Get the Marker object at the current position
        HistoryFetch pet = petListt.get(position);
        // Bind the Marker object data to the ViewHolder
        holder.bind(pet, listener, firebaseAuth, storageReference);
    }

    @Override
    public int getItemCount() {
        // Return the number of items in the pet list
        return petListt.size();
    }

    // Method to update the pet list and notify the adapter
    public void searchHistory(ArrayList<HistoryFetch> searchList) {
        petListt = searchList; // Update pet list
        notifyDataSetChanged(); // Notify the adapter of data changes
    }

    // ViewHolder class to hold and manage views for each item
    public static class ViewHolder extends RecyclerView.ViewHolder {

        ImageView recImage; // ImageView for pet image
        TextView recPetname; // TextView for pet name
        TextView recLastOnline; // TextView for last online time (not used in bind method here)
        TextView recDistance; // TextView for distance (not used in bind method here)
        ProgressBar loadImageHistory;

        // Constructor for ViewHolder
        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            // Initialize view references
            recImage = itemView.findViewById(R.id.recImage);
            recPetname = itemView.findViewById(R.id.recPetname);
            recLastOnline = itemView.findViewById(R.id.recLastOnline);
         //   recDistance = itemView.findViewById(R.id.recDistance);
            loadImageHistory = itemView.findViewById(R.id.loadImageHistory);
        }

        // Bind method to set data for each item
        public void bind(HistoryFetch pet, OnItemClickListener listener, FirebaseAuth firebaseAuth, StorageReference storageReference) {
            if (pet != null) {
                // Set pet name
                recPetname.setText(pet.getPetName());
                //   recDistance.setText(pet.getCurrentDistance());
                if (pet.getDate_time() != null) {
                    recLastOnline.setText(pet.getDate_time());
                }

                // Create reference to the pet image in Firebase Storage
                StorageReference petImageRef = storageReference.child("Pet Image")
                        .child(firebaseAuth.getCurrentUser().getUid())
                        .child(pet.getArduinoId())
                        .child("profile.jpg");

                // Get the download URL of the image and load it using Glide
                petImageRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        // Load the image into the ImageView using Glide
                        Glide.with(itemView.getContext())
                                .load(uri)
                                .into(recImage);

                        if (uri != null) {
                            loadImageHistory.setVisibility(View.GONE);
                            recImage.setVisibility(View.VISIBLE);
                        }else {
                            loadImageHistory.setVisibility(View.VISIBLE);
                            recImage.setVisibility(View.GONE);
                        }
                    }
                });

                // Set up click listener for the item view
                itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        // Notify the listener when an item is clicked
                        listener.onItemClick(pet);
                    }
                });
            }
        }
    }
}
