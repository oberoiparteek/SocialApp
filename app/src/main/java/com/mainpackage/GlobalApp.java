package com.mainpackage;

import android.app.Activity;
import android.content.Context;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

public class GlobalApp {
    public static String phone_number = "";
    public static String name = "";
    public static String email = "";
    public static String img_url = "";

    static void clearAll() {
        phone_number = "";
        name = "";
        email = "";
        img_url = "";

    }

    public static void hideKeyboard(Activity activity) {
        View view = activity.findViewById(android.R.id.content);
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }
}
