package com.example.gps_pet_tracker;

public class HistoryDisplayOnFirebase {
    public HistoryDisplayOnFirebase(String arduinoId, String petName, double latitude, double longitude, String time, String date) {
    }


    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getPetName() {
        return petName;
    }

    public void setPetName(String petName) {
        this.petName = petName;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getArduinoId() {
        return arduinoId;
    }

    public void setArduinoId(String arduinoId) {
        this.arduinoId = arduinoId;
    }

    public HistoryDisplayOnFirebase () {

    }

    double longitude, latitude;
    String arduinoId;
    String date;
    String petName;
    String time;

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    long timestamp;

    public String getTime_formated() {
        return time_formated;
    }

    public void setTime_formated(String time_formated) {
        this.time_formated = time_formated;
    }

    String time_formated;

}
