package com.mainpackage;

import java.util.ArrayList;
import java.util.HashMap;

public class Meeting {

    public String group_code;
    HashMap<String, String> group_member;
    public String title, date, host, id, location;
    public double lat, lng;

    public Meeting() {
    }

    public Meeting(String group_code, HashMap<String, String> group_member, String title, String date, String host, String id, String location, double lat, double lng) {
        this.group_code = group_code;
        this.group_member = group_member;
        this.title = title;
        this.date = date;
        this.host = host;
        this.id = id;
        this.location = location;
        this.lat = lat;
        this.lng = lng;
    }

    public String getGroup_code() {
        return group_code;
    }

    public void setGroup_code(String group_code) {
        this.group_code = group_code;
    }

    public HashMap<String, String> getGroup_member() {
        return group_member;
    }

    public void setGroup_member(HashMap<String, String> group_member) {
        this.group_member = group_member;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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
}
