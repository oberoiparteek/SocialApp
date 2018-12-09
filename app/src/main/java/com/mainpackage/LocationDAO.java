package com.mainpackage;

public class LocationDAO {
    String date;
    double latitude;
    double longitude;

    public LocationDAO() {
    }

    public LocationDAO(String date, double latitude, double longitude) {
        this.date = date;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }
}
