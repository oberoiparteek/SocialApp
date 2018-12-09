package com.mainpackage;

import android.app.Activity;
import android.content.Context;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import java.text.SimpleDateFormat;
import java.util.HashMap;

public class GlobalApp {
    public static String phone_number = "";
    public static String name = "";
    public static String email = "";
    public static String img_url = "";
    public static SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd-MM-yyyy");
    public static HashMap<String, String> name_no_mapping = new HashMap<>();

    static void clearAll() {
        phone_number = "";
        name = "";
        email = "";
        img_url = "";
        name_no_mapping.clear();

    }

    public static void hideKeyboard(Activity activity) {
        View view = activity.findViewById(android.R.id.content);
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }
}
