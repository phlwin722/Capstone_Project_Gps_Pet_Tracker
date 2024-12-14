package com.example.gps_pet_tracker;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class MapRoutePetAdapter extends RecyclerView.Adapter<MapRoutePetAdapter.ViewHolder> {

    private List<HistoryDisplayOnFirebase> petHistory;
    private OnItemClickListener listener;

    // Interface to handle item click events
    public interface OnItemClickListener {
        void onItemClickListener(HistoryDisplayOnFirebase historyOfPet);
    }

    // Constructor for the adapter

    public MapRoutePetAdapter(List<HistoryDisplayOnFirebase> petHistory, OnItemClickListener listener) {
        this.petHistory = petHistory; // Initialize pet list
        this.listener = listener; // Initialize item click listener
    }

    @NonNull
    @Override
    public MapRoutePetAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.custom_map_route_history_recycler_view,parent,false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MapRoutePetAdapter.ViewHolder holder, int position) {
        // Get the Marker object at the current position
        HistoryDisplayOnFirebase historyDisplayOnFirebase = petHistory.get(position);
        // Bind the Marker object data to the ViewHolder
        holder.bind (historyDisplayOnFirebase, listener);
    }

    @Override
    public int getItemCount() {
        return petHistory.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView textTime, textDate;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            textDate = itemView.findViewById(R.id.textDate);
            textTime = itemView.findViewById(R.id.textTime);
        }

        public void bind (HistoryDisplayOnFirebase historyDisplayOnFirebase, OnItemClickListener listener ){
            textTime.setText(historyDisplayOnFirebase.getTime_formated());
            textDate.setText(historyDisplayOnFirebase.getDate());

            Log.e("timerr",historyDisplayOnFirebase.getDate());
            Log.e("timerr",historyDisplayOnFirebase.getTime_formated());
            // Set up click listener for the item view
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    listener.onItemClickListener(historyDisplayOnFirebase);
                }
            });
        }
    }
}
