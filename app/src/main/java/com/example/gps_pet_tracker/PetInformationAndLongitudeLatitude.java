package com.example.gps_pet_tracker;

public class PetInformationAndLongitudeLatitude {


    public String getPetName() {
        return petName;
    }

    public void setPetName(String petName) {
        this.petName = petName;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getArduinoId() {
        return arduinoId;
    }

    public void setArduinoId(String arduinoId) {
        this.arduinoId = arduinoId;
    }

    public PetInformationAndLongitudeLatitude(String arduinoId, String petName, String status) {
        this.arduinoId = arduinoId;
        this.petName = petName;
        this.status = status;
    }

    String arduinoId;
    String petName;
    String status;




}
