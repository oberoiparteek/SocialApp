package com.mainpackage;

public class Invite {
    String group_name;
    String group_code;
    String group_owner_name;
    String group_owner_photo;
    String group_owner_phone;

    public Invite() {
    }

    public Invite(String group_name, String group_code, String group_owner_name, String group_owner_photo, String group_owner_phone) {
        this.group_name = group_name;
        this.group_code = group_code;
        this.group_owner_name = group_owner_name;
        this.group_owner_photo = group_owner_photo;
        this.group_owner_phone = group_owner_phone;
    }

    public String getGroup_name() {
        return group_name;
    }

    public void setGroup_name(String group_name) {
        this.group_name = group_name;
    }

    public String getGroup_code() {
        return group_code;
    }

    public void setGroup_code(String group_code) {
        this.group_code = group_code;
    }

    public String getGroup_owner_name() {
        return group_owner_name;
    }

    public void setGroup_owner_name(String group_owner_name) {
        this.group_owner_name = group_owner_name;
    }

    public String getGroup_owner_photo() {
        return group_owner_photo;
    }

    public void setGroup_owner_photo(String group_owner_photo) {
        this.group_owner_photo = group_owner_photo;
    }

    public String getGroup_owner_phone() {
        return group_owner_phone;
    }

    public void setGroup_owner_phone(String group_owner_phone) {
        this.group_owner_phone = group_owner_phone;
    }
}
