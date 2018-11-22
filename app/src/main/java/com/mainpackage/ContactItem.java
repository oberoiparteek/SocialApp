package com.mainpackage;

import android.support.annotation.NonNull;
import android.util.Log;

public class ContactItem {
    String name;
    String phone_number;
    String photo;

    public ContactItem(String name, String phone_number) {
        this.name = name;
        this.phone_number = phone_number;
    }

    @Override
    public boolean equals(Object obj) {

        ContactItem contactItem = (ContactItem) obj;
        if (!this.phone_number.contains("+")) {
            this.phone_number = "+91"+this.phone_number;
        }

        if (!contactItem.phone_number.contains("+")) {
            contactItem.phone_number = "+91"+contactItem.phone_number;
        }
        this.phone_number =  this.phone_number.replace(" ","");
        this.phone_number =  this.phone_number.replace("-","");
        contactItem.phone_number = contactItem.phone_number.replace(" ","");
        contactItem.phone_number = contactItem.phone_number.replace("-","");
        if(contactItem.phone_number.equals(GlobalApp.phone_number)){
            return false;
        }
        if(this.phone_number.equals(GlobalApp.phone_number)){
            return false;
        }

        if (contactItem.phone_number.equals(this.phone_number)) {
            return true;
        }

        return false;
    }
}
