package com.mainpackage;

import android.location.Location;

import java.util.ArrayList;
import java.util.HashMap;

public class User {
    String email;
    double last_longitude;
    double last_latitude;
    String phone;
    String photo;
    String username;
    private HashMap<String, LocationDAO> locations;
    private HashMap<String, String> meeting;

    HashMap<String, String> invitations;
    HashMap<String, String> groups;

    public User() {
    }

    public User(String email, double last_longitude, double last_latitude, String phone, String photo, String username, HashMap<String, LocationDAO> locations, HashMap<String, String> meeting, HashMap<String, String> invitations, HashMap<String, String> groups) {
        this.email = email;
        this.last_longitude = last_longitude;
        this.last_latitude = last_latitude;
        this.phone = phone;
        this.photo = photo;
        this.username = username;
        this.locations = locations;
        this.meeting = meeting;
        this.invitations = invitations;
        this.groups = groups;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public double getLast_longitude() {
        return last_longitude;
    }

    public void setLast_longitude(double last_longitude) {
        this.last_longitude = last_longitude;
    }

    public double getLast_latitude() {
        return last_latitude;
    }

    public void setLast_latitude(double last_latitude) {
        this.last_latitude = last_latitude;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getPhoto() {
        return photo;
    }

    public void setPhoto(String photo) {
        this.photo = photo;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public HashMap<String, LocationDAO> getLocations() {
        return locations;
    }

    public void setLocations(HashMap<String, LocationDAO> locations) {
        this.locations = locations;
    }

    public HashMap<String, String> getMeeting() {
        return meeting;
    }

    public void setMeeting(HashMap<String, String> meeting) {
        this.meeting = meeting;
    }

    public HashMap<String, String> getInvitations() {
        return invitations;
    }

    public void setInvitations(HashMap<String, String> invitations) {
        this.invitations = invitations;
    }

    public HashMap<String, String> getGroups() {
        return groups;
    }

    public void setGroups(HashMap<String, String> groups) {
        this.groups = groups;
    }
}