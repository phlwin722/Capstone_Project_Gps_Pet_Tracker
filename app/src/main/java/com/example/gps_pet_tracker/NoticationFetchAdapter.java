package com.example.gps_pet_tracker;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class NoticationFetchAdapter extends RecyclerView.Adapter <NoticationFetchAdapter.ViewHolder> {
    List<NotificationFetch> datalist; // list of store data items

    public NoticationFetchAdapter(List<NotificationFetch> datalist) {
        this.datalist = datalist;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.custom_notification, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        NotificationFetch notificationFetch = datalist.get(position);
        holder.notificationList.setText(notificationFetch.getNotification());
        holder.Date.setText(notificationFetch.getDate());
    }

    @Override
    public int getItemCount() {
        return datalist.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView notificationList, Date;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            // Initilaize the textView for pet notification
            notificationList = itemView.findViewById(R.id.notificationText);
            Date = itemView.findViewById(R.id.Datee);
        }
    }
}
