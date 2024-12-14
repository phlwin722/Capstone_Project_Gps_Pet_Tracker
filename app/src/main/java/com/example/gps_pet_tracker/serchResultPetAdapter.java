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
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.List;

public class serchResultPetAdapter extends RecyclerView.Adapter<serchResultPetAdapter.PetListHolder> {

    private List<Marker> petList;
    private OnItemClickListener listenerr;

    private FirebaseAuth firebaseAuth;
    private FirebaseStorage firebaseStorage;
    private StorageReference storageReference;

    //interface for handling items click
    public interface OnItemClickListener {
        void onItemClick(Marker petInfor);
    }

    // handling for search items
    public void searchAdapter (List<Marker> searchList) {
        petList = searchList;
        notifyDataSetChanged();
    }

    public serchResultPetAdapter(List<Marker> petList, OnItemClickListener listenerr) {
        this.petList = petList;
        this.listenerr = listenerr;

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseStorage = FirebaseStorage.getInstance();
        storageReference = firebaseStorage.getReference();
    }

    @NonNull
    @Override
    public PetListHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Inflate the item layout for each pet
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.custom_search_result_pet, parent, false);
        return new PetListHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PetListHolder holder, int position) {
        // Bind pet data to the view holder
        //  this code will get and display on public void final
        Marker petInfor = petList.get(position);
        holder.bind(petInfor,listenerr,firebaseAuth,storageReference);
    }

    @Override
    public int getItemCount() {
        return petList.size();
    }

    public static class PetListHolder extends RecyclerView.ViewHolder{

        private TextView petName, ArduinoId;
        private ImageView petImage;
        ProgressBar prog;

        public PetListHolder(@NonNull View itemView) {
            super(itemView);

            // Initialize the textViews for pet Information
            petName = (TextView) itemView.findViewById(R.id.petNamee);
            petImage = (ImageView) itemView.findViewById(R.id.petImagee);
            ArduinoId = (TextView) itemView.findViewById(R.id.arduinoId);
            prog = itemView.findViewById(R.id.prog);
        }
        public void bind(final Marker petInfor, final OnItemClickListener listenerr, FirebaseAuth firebaseAuth, StorageReference storageReference){
            // Set the pet information and handle item clicks
            petName.setText(petInfor.getPetName());
            ArduinoId.setText(petInfor.getArduinoId());

            // Load the pet image using Picasso or Glide
            StorageReference profilePet = storageReference.child("Pet Image")
                    .child(firebaseAuth.getCurrentUser().getUid())
                    .child(petInfor.getArduinoId())
                    .child("profile.jpg");
            profilePet.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                @Override
                public void onSuccess(Uri uri) {
                    Glide.with(itemView.getContext())
                            .load(uri)
                            .into(petImage);
                    prog.setVisibility(View.GONE);
                    petImage.setVisibility(View.VISIBLE);
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {

                }
            });
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    listenerr.onItemClick(petInfor);
                }
            });
        }

    }
}
