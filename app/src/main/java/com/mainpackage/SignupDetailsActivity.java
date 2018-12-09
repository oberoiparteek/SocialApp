package com.mainpackage;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;
import com.ib.custom.toast.CustomToast;

import java.io.ByteArrayOutputStream;
import java.io.File;

import android.net.Uri;

public class SignupDetailsActivity extends AppCompatActivity {

    ProgressDialog progressDialog;
    EditText et_signup_email;
    EditText et_signup_name;
    ImageView img_signup;
    private int imageSelectedType = 0;
    private final int TYPE_CAMERA = 1;
    private final int TYPE_GALLERY = 2;
    private Uri myimageURI;
    String ImageURL = "";

    private Bitmap myimageBitmap;
    private String name;
    private String email;
    private String phone_number;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup_details);
        phone_number = getIntent().getStringExtra("phone");
        et_signup_email = findViewById(R.id.et_signup_email);
        et_signup_name = findViewById(R.id.et_signup_name);
        img_signup = findViewById(R.id.img_signup);
        progressDialog = new ProgressDialog(this);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.setMessage("Please Wait");

    }

    public void bt_event_next(View view) {
        name = et_signup_name.getText().toString().trim();
        email = et_signup_email.getText().toString().trim();
        //Empty Check
        if (name.equalsIgnoreCase("") || email.equalsIgnoreCase("")) {
            Toast.makeText(this, "Invalid details", Toast.LENGTH_LONG).show();
        }//email check
        else if ((!email.contains("@")) || (!email.contains("."))) {
            Toast.makeText(this, "Invalid Email", Toast.LENGTH_LONG).show();
        } else if (imageSelectedType == 0) {
            Toast.makeText(this, "Invalid Image", Toast.LENGTH_LONG).show();
        } else {
            progressDialog.show();
            StorageReference mStorageRef;
            mStorageRef = FirebaseStorage.getInstance().getReference("/");
            final long l = System.currentTimeMillis();
            StorageReference imageRef = mStorageRef.child("images/" + l + ".jpg");

            if (imageSelectedType == TYPE_GALLERY) {
                imageRef.putFile(myimageURI)
                        .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                FirebaseStorage
                                        .getInstance()
                                        .getReference("images/" + l + ".jpg")
                                        .getDownloadUrl()
                                        .addOnSuccessListener(new OnSuccessListener<Uri>() {
                                            @Override
                                            public void onSuccess(Uri uri) {
                                                Log.d("upload_success", uri + "");
                                                uploadSignupData(uri);
                                            }
                                        });
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception exception) {
                                // Handle unsuccessful uploads
                                Toast.makeText(SignupDetailsActivity.this, "Upload Failed, Check Internet", Toast.LENGTH_SHORT).show();
                                exception.printStackTrace();
                            }
                        });

            } else if (imageSelectedType == TYPE_CAMERA) {

                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                myimageBitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                byte[] data = baos.toByteArray();

                final UploadTask uploadTask = imageRef.putBytes(data);
                uploadTask
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception exception) {
                                Toast.makeText(SignupDetailsActivity.this, "Upload unsuccessful", Toast.LENGTH_LONG).show();
                            }
                        })
                        .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                FirebaseStorage
                                        .getInstance()
                                        .getReference("images/" + l + ".jpg")
                                        .getDownloadUrl()
                                        .addOnSuccessListener(new OnSuccessListener<Uri>() {
                                            @Override
                                            public void onSuccess(Uri uri) {
                                                Log.d("upload_success", uri + "");
                                                uploadSignupData(uri);
                                            }
                                        });
                            }
                        });


            }


        }
    }

    private void uploadSignupData(Uri uri) {
        Log.d("MYMSG", "in upload");
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference usersRef = database.getReference("users/" + phone_number);
        User user = new User();
        user.email = email;
        user.username = name;
        user.phone = phone_number;
        user.last_latitude = 0;
        user.last_longitude = 0;
        user.photo = uri.toString();
        usersRef.setValue(user).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    CustomToast.makeText(SignupDetailsActivity.this, "Signup Successful! Please Login Again", Toast.LENGTH_LONG).show();
                    progressDialog.dismiss();
                    Intent i = new Intent(SignupDetailsActivity.this, OTPLoginActivity.class);
                    startActivity(i);
                    SignupDetailsActivity.this.finish();
                } else {
                    progressDialog.dismiss();
                    Toast.makeText(SignupDetailsActivity.this, "Failed", Toast.LENGTH_SHORT).show();
                }
            }
        });


    }


    //    Picking Image from Gallery or camera
    public void bt_event_choose_from_camera(View view) {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(intent, 42);
    }


    public void bt_event_choose_from_gallery(View view) {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, 43);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (data != null) {
            if (requestCode == 42) {
                Bitmap bitmap = (Bitmap) data.getExtras().get("data");
                img_signup.setImageBitmap(bitmap);
                myimageBitmap = bitmap;
                imageSelectedType = TYPE_CAMERA;

            } else if (requestCode == 43) {
                Uri data1 = data.getData();
                img_signup.setImageURI(data1);
                myimageURI = data1;
                imageSelectedType = TYPE_GALLERY;

            }
        }
    }


/////End of section select photo//////////////////////////////////////
}
