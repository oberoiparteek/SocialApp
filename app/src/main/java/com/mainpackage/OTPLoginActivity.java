package com.mainpackage;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.ib.custom.toast.CustomToast;

import java.util.concurrent.TimeUnit;

public class OTPLoginActivity extends AppCompatActivity {


    private EditText et_login_otp;
    private FirebaseAuth mAuth;
    private String VerificationId;
    private PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks;
    private boolean mVerificationInProgress = false;
    private String mVerificationId;
    private PhoneAuthProvider.ForceResendingToken mResendToken;
    private EditText et_login_phone_number;
    private TextView tv_login_status;
    private Button bt_login_send_otp;
    private Button login_with_Otp;
    private String phoneNumber;
    private ProgressBar progressBar_login_otp;
    private User value;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_otplogin);
//        FirebaseApp.initializeApp(this);
        mAuth = FirebaseAuth.getInstance();
        et_login_otp = findViewById(R.id.et_login_otp);
        et_login_phone_number = findViewById(R.id.et_login_phone_number);
        tv_login_status = findViewById(R.id.tv_login_status);
        login_with_Otp = findViewById(R.id.login_with_Otp);
        bt_login_send_otp = findViewById(R.id.bt_login_send_otp);
        progressBar_login_otp = findViewById(R.id.progressBar_login_otp);
        mCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

            @Override
            public void onVerificationCompleted(PhoneAuthCredential credential) {
                et_login_otp.setText(credential.getSmsCode());
                et_login_phone_number.setVisibility(View.INVISIBLE);
                login_with_Otp.setVisibility(View.INVISIBLE);
                tv_login_status.setText("Auto Detection Complete");
                tv_login_status.setTextColor(Color.GREEN);
                bt_login_send_otp.setEnabled(false);
                gotoNextActivity();
            }

            @Override
            public void onVerificationFailed(FirebaseException e) {
                // This callback is invoked in an invalid request for verification is made,
                // for instance if the the phone number format is not valid.
                tv_login_status.setText("Verification Failed " + e.getMessage());
            }

            @Override
            public void onCodeSent(String verificationId,
                                   PhoneAuthProvider.ForceResendingToken token) {
                VerificationId = verificationId;
                tv_login_status.setText("Detecting Otp");
                et_login_otp.setVisibility(View.VISIBLE);
                tv_login_status.setVisibility(View.VISIBLE);
                login_with_Otp.setVisibility(View.VISIBLE);
            }
        };

    }

    public void gotoNextActivity() {

        if (value != null) {
            GlobalApp.phone_number = value.phone;
            GlobalApp.email = value.email;
            GlobalApp.img_url = value.photo;
            GlobalApp.name = value.username;

            SharedPreferences myprefs = getSharedPreferences("myprefs", Context.MODE_PRIVATE);
            SharedPreferences.Editor sp_editor = myprefs.edit();
            sp_editor.putString("session_phone", GlobalApp.phone_number);
            sp_editor.putString("session_username", GlobalApp.name);
            sp_editor.putString("session_img_url", GlobalApp.img_url);
            sp_editor.putString("session_email", GlobalApp.email);
            sp_editor.apply();


            Intent i = new Intent(this, UserHomeActivity.class);
            startActivity(i);
            this.finish();
        }
    }


    public void send_otp(View view) {


        if (et_login_phone_number.getText().toString().equals("") || et_login_phone_number.getText().length() < 10) {
            Toast.makeText(this, "All Fields Are Compulsory", Toast.LENGTH_LONG).show();
        } else {
            phoneNumber = et_login_phone_number.getText().toString();
            if (!phoneNumber.contains("+")) {
                phoneNumber = "+91" + phoneNumber;
            }

            final FirebaseDatabase database = FirebaseDatabase.getInstance();
            DatabaseReference usersRef = database.getReference("users/" + phoneNumber);

            usersRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (!dataSnapshot.exists()) {
                        Toast.makeText(OTPLoginActivity.this, "User doesn't Exist Please Check the number", Toast.LENGTH_LONG).show();
                    } else {
                        value = dataSnapshot.getValue(User.class);
                        hideKeyboard(OTPLoginActivity.this);
                        bt_login_send_otp.setEnabled(false);
                        progressBar_login_otp.setVisibility(View.VISIBLE);
                        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                                phoneNumber,        // Phone number to verify
                                120,                 // Timeout duration
                                TimeUnit.SECONDS,   // Unit of timeout
                                OTPLoginActivity.this,               // Activity (for callback binding)
                                mCallbacks);        // OnVerificationStateChangedCallback
                        et_login_otp.setVisibility(View.VISIBLE);
                        tv_login_status.setVisibility(View.VISIBLE);
                        login_with_Otp.setVisibility(View.VISIBLE);
                        et_login_otp.setVisibility(View.VISIBLE);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Log.d("MYMSG", databaseError.getMessage());
                }
            });


        }
    }

    public void login(View view) {
        String code = et_login_otp.getText().toString();
        if (code.equalsIgnoreCase("")) {
            Toast.makeText(this, "Invalid Code", Toast.LENGTH_LONG).show();
        } else {
            PhoneAuthCredential credential = PhoneAuthProvider.getCredential(VerificationId, code);
            signInWithPhoneAuthCredential(credential);
        }
    }

    private void signInWithPhoneAuthCredential(final PhoneAuthCredential credential) {
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            et_login_otp.setText(credential.getSmsCode());
                            et_login_phone_number.setVisibility(View.INVISIBLE);
                            tv_login_status.setText("Manual Verification Complete");
                            tv_login_status.setTextColor(Color.BLUE);
                            gotoNextActivity();

                        } else {
                            if (task.getException() instanceof
                                    FirebaseAuthInvalidCredentialsException) {
                                // The verification code entered was invalid
                                CustomToast.makeText(OTPLoginActivity.this, "Invalid code", Toast.LENGTH_SHORT).show();


                            }
                        }
                    }
                });


    }


    public static void hideKeyboard(Activity activity) {
        View view = activity.findViewById(android.R.id.content);
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

}



