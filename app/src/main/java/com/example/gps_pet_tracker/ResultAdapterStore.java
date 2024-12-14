package com.example.gps_pet_tracker;

public class ResultAdapterStore {
    String arduinoId, petName ,petCategory, petDistance, status;
    double longitude, latitude;

    public ResultAdapterStore() {
        // Default constructor required for calls to DataSnapshot.getValue(Pet.class)
    }

    // Constructor with parameters
    public ResultAdapterStore(String arduinoId, String petName, String petCategory, String status, String petDistance, double longitude, double latitude) {
        this.arduinoId = arduinoId;
        this.petName = petName;
        this.petCategory = petCategory;
        this.status = status;
        this.petDistance = petDistance;
        this.longitude = longitude;
        this.latitude = latitude;
    }

    public String getArduinoId() {
        return arduinoId;
    }

    public String getPetName() {
        return petName;
    }

    public String getPetCategory() {
        return petCategory;
    }

    public String getPetDistance() {
        return petDistance;
    }

    public String getStatus() {
        return status;
    }

    public double getLongitude() {
        return longitude;
    }

    public double getLatitude() {
        return latitude;
    }
}
