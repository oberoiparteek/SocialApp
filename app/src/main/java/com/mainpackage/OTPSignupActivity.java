package com.mainpackage;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
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
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.ib.custom.toast.CustomToast;

import java.util.concurrent.TimeUnit;

public class OTPSignupActivity extends AppCompatActivity {


    private EditText et_otp;
    private FirebaseAuth mAuth;
    private String VerificationId;
    private PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks;
    private boolean mVerificationInProgress = false;
    private String mVerificationId;
    private PhoneAuthProvider.ForceResendingToken mResendToken;
    private EditText et_phone_number;
    private TextView tv_status;
    private Button send_otp;
    private Button signup_with_Otp;
    private String phoneNumber;
    private ProgressBar progressBar_signup_otp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup_otp);

        mAuth = FirebaseAuth.getInstance();
        et_otp = findViewById(R.id.et_otp);
        et_phone_number = findViewById(R.id.et_phone_number);
        tv_status = findViewById(R.id.tv_status);
        signup_with_Otp = findViewById(R.id.signup_with_Otp);
        send_otp = findViewById(R.id.bt_send_otp);
        progressBar_signup_otp = findViewById(R.id.progressBar_signup_otp);
        mCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {


            @Override
            public void onVerificationCompleted(PhoneAuthCredential credential) {
                et_otp.setText(credential.getSmsCode());
                et_phone_number.setVisibility(View.INVISIBLE);
                signup_with_Otp.setVisibility(View.INVISIBLE);

                progressBar_signup_otp.setVisibility(View.GONE);

                tv_status.setText("Auto Detection Complete");
                tv_status.setTextColor(Color.GREEN);
                send_otp.setEnabled(false);
                gotoNextActivity();

            }

            @Override
            public void onVerificationFailed(FirebaseException e) {
                // This callback is invoked in an invalid request for verification is made,
                // for instance if the the phone number format is not valid.
                CustomToast.makeText(OTPSignupActivity.this, "Verification Failed, Try Again ", Toast.LENGTH_LONG).show();
                progressBar_signup_otp.setVisibility(View.GONE);
            }

            @Override
            public void onCodeAutoRetrievalTimeOut(String s) {

                progressBar_signup_otp.setVisibility(View.GONE);
                tv_status.setText("Enter Otp Manually");
            }

            @Override
            public void onCodeSent(String verificationId,
                                   PhoneAuthProvider.ForceResendingToken token) {
                Log.d("MYMSG", "code sent " + verificationId);
                VerificationId = verificationId;
                et_otp.setVisibility(View.VISIBLE);
                tv_status.setVisibility(View.VISIBLE);
                tv_status.setTextColor(Color.BLACK);
                tv_status.setText("Detecting OTP");
                signup_with_Otp.setVisibility(View.VISIBLE);
            }
        };

    }

    public void gotoNextActivity() {
        Intent i = new Intent(this, SignupDetailsActivity.class);
        i.putExtra("phone", phoneNumber);
        startActivity(i);
        this.finish();
    }

    public void send_otp(View view) {


        if (et_phone_number.getText().toString().equals("") || et_phone_number.getText().length() < 10) {
            Toast.makeText(this, "All Fields Are Compulsory", Toast.LENGTH_LONG).show();
        } else {
            phoneNumber = et_phone_number.getText().toString();
            if (!phoneNumber.contains("+")) {
                phoneNumber = "+91" + phoneNumber;
            }
            tv_status.setVisibility(View.VISIBLE);
            tv_status.setText("Please Wait!");
            FirebaseDatabase.getInstance().getReference("users").child(phoneNumber).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        tv_status.setText("Account Already Exist ! Try Sign In");
                        tv_status.setTextColor(getResources().getColor(R.color.colorButtonRed));
                        return;
                    } else {

                        hideKeyboard(OTPSignupActivity.this);
                        send_otp.setEnabled(false);
                        progressBar_signup_otp.setVisibility(View.VISIBLE);

                        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                                phoneNumber,        // Phone number to verify
                                120,                 // Timeout duration
                                TimeUnit.SECONDS,   // Unit of timeout
                                OTPSignupActivity.this,               // Activity (for callback binding)
                                mCallbacks);        // OnVerificationStateChangedCallbacks

                        et_otp.setVisibility(View.VISIBLE);
                        signup_with_Otp.setVisibility(View.VISIBLE);
                        et_otp.setVisibility(View.VISIBLE);

                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }
    }

    public void signup(View view) {
        String code = et_otp.getText().toString();

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
                            et_otp.setText(credential.getSmsCode());
                            et_phone_number.setVisibility(View.INVISIBLE);
                            tv_status.setText("Manual Verification Complete");
                            tv_status.setTextColor(Color.BLUE);
                            gotoNextActivity();
                        } else {
                            if (task.getException() instanceof
                                    FirebaseAuthInvalidCredentialsException) {
                                // The verification code entered was invalid
                                CustomToast.makeText(OTPSignupActivity.this, "Invalid code", Toast.LENGTH_SHORT).show();


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



