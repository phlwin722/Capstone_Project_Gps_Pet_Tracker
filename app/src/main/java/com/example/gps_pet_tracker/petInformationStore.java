package com.example.gps_pet_tracker;

import java.io.Serializable;

public class petInformationStore implements Serializable {

    // Properties of the pet information
     String arduinoId;
     String petName;
     String petCategory;
     String email;
     String key;

    public String getPetDataTime() {
        return petDataTime;
    }

    public void setPetDataTime(String petDataTime) {
        this.petDataTime = petDataTime;
    }

    String petDataTime;

    public String getPetDays() {
        return petDays;
    }

    public void setPetDays(String petDays) {
        this.petDays = petDays;
    }

    String petDays;

    public String getPetDataPlan() {
        return petDataPlan;
    }

    public void setPetDataPlan(String petDataPlan) {
        this.petDataPlan = petDataPlan;
    }

    String petDataPlan;
     String petDataDate;

    public String getPetDayORMONTH() {
        return petDayORMONTH;
    }

    public void setPetDayORMONTH(String petDayORMONTH) {
        this.petDayORMONTH = petDayORMONTH;
    }

    String petDayORMONTH;

    public String getPetDataMonthorDay() {
        return petDataMonthorDay;
    }

    public void setPetDataMonthorDay(String petDataMonthorDay) {
        this.petDataMonthorDay = petDataMonthorDay;
    }

    public String getPetDataDate() {
        return petDataDate;
    }

    public void setPetDataDate(String petDataDate) {
        this.petDataDate = petDataDate;
    }

    String petDataMonthorDay;
    String status;
    String petBat;
    String battery;

    public String getExpDate() {
        return expDate;
    }

    public void setExpDate(String expDate) {
        this.expDate = expDate;
    }

    String expDate;

    public String getExpired() {
        return expired;
    }

    public void setExpired(String expired) {
        this.expired = expired;
    }

    String expired;
    double longitude;
    double latitude;

    public String getPetBat() {
        return petBat;
    }

    public void setPetBat(String petBat) {
        this.petBat = petBat;
    }
    public String getCurrentDistance() {
        return currentDistance;
    }

    public void setCurrentDistance(String currentDistance) {
        this.currentDistance = currentDistance;
    }

    String currentDistance;
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

    public String getBattery() {
        return battery;
    }

    public void setBattery(String battery) {
        this.battery = battery;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    // Default constructor (required by Firebase)
    public petInformationStore() {
        // Default constructor required for calls to DataSnapshot.getValue(Pet.class)
    }

    // Constructor with parameters
    public petInformationStore(String arduinoId, String petName, String petCategory,String petDataPlan, String petDataDate,String petDataMonthorDay,String petDayORMONTH, String petDays, String petDataTime,String expired, String expDate, String email,String battery, String status, String petBat) {
        this.arduinoId = arduinoId;
        this.petName = petName;
        this.petCategory = petCategory;
        this.petDataPlan = petDataPlan;
        this.petDataDate = petDataDate;
        this.petDataMonthorDay = petDataMonthorDay;
        this.petDayORMONTH = petDayORMONTH;
        this.petDays = petDays;
        this.petDataTime = petDataTime;
        this.expired = expired;
        this.expDate = expDate;
        this.email = email;
        this.battery = battery;
        this.status = status;
        this.petBat = petBat;
    }

    // Getters and setters for all fields

    public String getArduinoId() {
        return arduinoId;
    }

    public void setArduinoId(String arduinoId) {
        this.arduinoId = arduinoId;
    }

    public String getPetName() {
        return petName;
    }

    public void setPetName(String petName) {
        this.petName = petName;
    }

    public String getPetCategory() {
        return petCategory;
    }

    public void setPetCategory(String petCategory) {
        this.petCategory = petCategory;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
