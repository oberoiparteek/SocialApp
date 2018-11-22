package com.mainpackage;

import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        SharedPreferences sharedPreferences = getSharedPreferences("myprefs", MODE_PRIVATE);

        String session_phone = sharedPreferences.getString("session_phone", "");
        String session_username = sharedPreferences.getString("session_username", "");
        String session_img_url = sharedPreferences.getString("session_img_url", "");
        String session_email = sharedPreferences.getString("session_email", "");


        if(!session_phone.equals("")){
            GlobalApp.phone_number=session_phone;
            GlobalApp.name=session_username;
            GlobalApp.img_url=session_img_url;
            GlobalApp.email=session_email;
            Log.d("DUPLICATE", "onCreate: "+GlobalApp.phone_number+"kahani");
            Intent i=new Intent(this,UserHomeActivity.class);
            i.putExtra("phone",session_phone);
            startActivity(i);
            this.finish();
        }

    }

    public void bt_event_login(View view) {
        Intent intent = new Intent(this, OTPLoginActivity.class);
        startActivity(intent);

    }

    public void bt_event_signup(View view) {
        Intent intent = new Intent(this, OTPSignupActivity.class);
        startActivity(intent);
    }
}
