package com.example.gps_pet_tracker;

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

// Adapter class for binding pet data to the RecyclerView
public class PetAdapter extends RecyclerView.Adapter<PetAdapter.PetViewHolder> {

    private List<petInformationStore> petList;
    private OnItemClickListener listener;

    private FirebaseAuth firebaseAuth;
    private FirebaseStorage firebaseStorage;
    private StorageReference storageReference;

    // Interface for handling item clicks
    public interface OnItemClickListener {
        void onItemClick(petInformationStore pet);
    }

    // Constructor for PetAdapter
    public PetAdapter(List<petInformationStore> petList, OnItemClickListener listener) {
        this.petList = petList;
        this.listener = listener;

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseStorage = FirebaseStorage.getInstance();
        storageReference = firebaseStorage.getReference();
    }

    @NonNull
    @Override
    public PetViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Inflate the item layout for each pet
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.custom_list_of_pet, parent, false);
        return new PetViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PetViewHolder holder, int position) {
        // Bind pet data to the view holder
        petInformationStore pet = petList.get(position);
        holder.bind(pet, listener, firebaseAuth, storageReference);
    }

    @Override
    public int getItemCount() {
        // Return the total number of items
        return petList.size();
    }

    public void searchPetAdapter(ArrayList<petInformationStore> searchList) {
        petList = searchList;
        notifyDataSetChanged();
    }

    // ViewHolder class for RecyclerView items
    public static class PetViewHolder extends RecyclerView.ViewHolder {
        TextView petName;
        TextView petCategory;
        TextView recStatus;
        TextView recDistance;
        TextView recBattery;
        ImageView petImage,petCategoryIcon,battery_icon;
        ProgressBar loadingImage;

        public PetViewHolder(@NonNull View itemView) {
            super(itemView);
            // Initialize the TextViews for pet information
            petName = itemView.findViewById(R.id.recPetname);
            petImage = itemView.findViewById(R.id.recImage);
            loadingImage = itemView.findViewById(R.id.loadingImage);
            recDistance = itemView.findViewById(R.id.recDistance);
        }

        public void bind(final petInformationStore pet, final OnItemClickListener listener, FirebaseAuth firebaseAuth, StorageReference storageReference) {
            // Set the pet information and handle item clicks

            if (pet != null) {
                petName.setText(pet.getPetName());
                recDistance.setText(pet.getCurrentDistance());

                // Load the pet image using Picasso or Glide
                StorageReference profileRef = storageReference.child("Pet Image/" + firebaseAuth.getCurrentUser().getUid() + "/" + pet.getArduinoId() + "/profile.jpg");
                profileRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        Glide.with(itemView.getContext())
                                .load(uri)
                                //    .placeholder(R.drawable.placeholder_image) // Optional: add a placeholder image
                                //  .error(R.drawable.error_image) // Optional: add an error image
                                .into(petImage);
                        if (uri != null) {
                            petImage.setVisibility(View.VISIBLE);
                            loadingImage.setVisibility(View.GONE);
                        }else {
                            petImage.setVisibility(View.GONE);
                            loadingImage.setVisibility(View.VISIBLE);
                        }
                    }
                }).addOnFailureListener(e -> {
                    // Handle any errors here
                });


                itemView.setOnClickListener(v -> listener.onItemClick(pet));
            }


        }
    }
}
