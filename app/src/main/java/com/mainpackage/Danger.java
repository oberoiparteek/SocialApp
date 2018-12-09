package com.mainpackage;

public class Danger {

    public String host, reason, date, location;
    public double lat, lng;
    public double radius;

    public Danger() {
    }

    public Danger(String host, String reason, String date, String location, double lat, double lng, double radius) {
        this.host = host;
        this.reason = reason;
        this.date = date;
        this.location = location;
        this.lat = lat;
        this.lng = lng;
        this.radius = radius;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public double getLat() {
        return lat;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public double getLng() {
        return lng;
    }

    public void setLng(double lng) {
        this.lng = lng;
    }

    public double getRadius() {
        return radius;
    }

    public void setRadius(double radius) {
        this.radius = radius;
    }
}
