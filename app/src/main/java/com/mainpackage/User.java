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
    private ArrayList<LocationDAO> locations;

    ArrayList<String> invitations;
    ArrayList<String> groupcodes;
    ArrayList<String> groupnames;
    User() {
    }

    public User(String email, double last_longitude, double last_latitude, String phone, String photo, String username, ArrayList<LocationDAO> locations, ArrayList<String> invitaions, ArrayList<String> groupcodes, ArrayList<String> groupnames) {
        this.email = email;
        this.last_longitude = last_longitude;
        this.last_latitude = last_latitude;
        this.phone = phone;
        this.photo = photo;
        this.username = username;
        this.locations = locations;
        this.invitations = invitaions;
        this.groupcodes = groupcodes;
        this.groupnames = groupnames;
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

    public ArrayList<LocationDAO> getLocations() {
        return locations;
    }

    public void setLocations(ArrayList<LocationDAO> locations) {
        this.locations = locations;
    }

    public ArrayList<String> getInvitations() {
        return invitations;
    }

    public void setInvitations(ArrayList<String> invitations) {
        this.invitations = invitations;
    }

    public ArrayList<String> getGroupcodes() {
        return groupcodes;
    }

    public void setGroupcodes(ArrayList<String> groupcodes) {
        this.groupcodes = groupcodes;
    }

    public ArrayList<String> getGroupnames() {
        return groupnames;
    }

    public void setGroupnames(ArrayList<String> groupnames) {
        this.groupnames = groupnames;
    }
}