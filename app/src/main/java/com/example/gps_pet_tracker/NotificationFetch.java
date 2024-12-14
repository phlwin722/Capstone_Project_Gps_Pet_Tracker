package com.example.gps_pet_tracker;

public class NotificationFetch {
    private String notification;
    private long timestamp; // Add this field

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    private String date;

    // Default constructor required for calls to DataSnapshot.getValue(NotificationFetch.class)
    public NotificationFetch() {}

    public NotificationFetch(String notification) {
        this.notification = notification;
    }

    public long getTimestamp() {
        return timestamp; // Getter for timestamp
    }

    public String getNotification() {
        return notification;
    }

}

