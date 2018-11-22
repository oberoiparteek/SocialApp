package com.mainpackage;

import java.util.ArrayList;

public class Group {
    private String groupCode;
    private String groupName;
    private String owner;
    private ArrayList<String> members;
    public Group(){

    }

    public Group(String groupCode, String groupName, String owner, ArrayList<String> members) {
        this.groupCode = groupCode;
        this.groupName = groupName;
        this.owner = owner;
        this.members = members;
    }

    public String getGroupCode() {
        return groupCode;
    }

    public void setGroupCode(String groupCode) {
        this.groupCode = groupCode;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public ArrayList<String> getMembers() {
        return members;
    }

    public void setMembers(ArrayList<String> members) {
        this.members = members;
    }

    @Override
    public String toString() {
        return groupName;
    }
}
