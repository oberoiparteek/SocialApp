package com.mainpackage;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.NavigationView;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.Target;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.ib.custom.toast.CustomToast;
import com.toptoche.searchablespinnerlibrary.SearchableSpinner;

import java.io.Serializable;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;
import java.util.Set;

public class UserHomeActivity extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private DrawerLayout mDrawerLayout;
    private RecyclerView recycler_view_group_members;
    private RecyclerAdapterGroupMembers adapter_selected_group;
    ArrayList<Group> groupsArrayList = new ArrayList<>();
    ArrayList<User> selected_group_membersArrayList = new ArrayList<>();
    SearchableSpinner spinner_choose_group;
    ArrayAdapter<Group> spinner_adapter;
    final String TAG = "MYMSG";
    ImageView im_photo;
    Switch switch_location;
    private HashMap<Marker, User> markersHashMap = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_home);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION}, 103);
        }
        switch_location = findViewById(R.id.switch_location);
        if (FetchContinousLocationService.isServiceStarted) {
            switch_location.setChecked(true);
        }
        Intent i = new Intent(this, CheckDangerLocationService.class);
        i.setAction("START SERVICE");
        startForegroundService(i);
        switch_location.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b) {
                    Intent service_intent = new Intent(UserHomeActivity.this, FetchContinousLocationService.class);
                    service_intent.setAction("START SIGNAL");
                    if (ActivityCompat.checkSelfPermission(UserHomeActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(UserHomeActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        switch_location.setChecked(false);
                        ActivityCompat.requestPermissions(UserHomeActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION}, 103);
                    } else {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            startForegroundService(service_intent);
                        } else {
                            startService(service_intent);
                        }
                    }

                } else {
                    stopService();
                }
            }
        });
        mapNumbers();
        final SharedPreferences myprefs = getSharedPreferences("myprefs", Context.MODE_PRIVATE);
        String token_generated = myprefs.getString("token_generated", "");
        if (token_generated.isEmpty()) {
            FirebaseInstanceId.getInstance().getInstanceId().addOnCompleteListener(new OnCompleteListener<InstanceIdResult>() {
                @Override
                public void onComplete(@NonNull Task<InstanceIdResult> task) {
                    Log.d("MYMSG Token On Home", task.getResult().getToken());
                    FirebaseDatabase.getInstance().getReference("tokens").child(GlobalApp.phone_number).setValue(task.getResult().getToken()).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            CustomToast.makeSuccessToast(UserHomeActivity.this, "Token Saved", View.VISIBLE).show();
                            SharedPreferences.Editor edit = myprefs.edit();
                            edit.putString("token_generated", "true");
                            edit.commit();
                        }
                    });
                }
            });
        } else {
            CustomToast.makeInfoToast(UserHomeActivity.this, "Token Already Saved", View.VISIBLE).show();
        }

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_user_home);
        setSupportActionBar(toolbar);

        // Create Navigation drawer and inflate layout
        final NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view_user_home);
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout_user_home);
        adapter_selected_group = new RecyclerAdapterGroupMembers();
        recycler_view_group_members = findViewById(R.id.recycler_view_group_members);
        recycler_view_group_members.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        recycler_view_group_members.setAdapter(adapter_selected_group);


        spinner_choose_group = findViewById(R.id.spinner_choose_group);
        spinner_choose_group.setTitle("Select A Group");
        spinner_adapter =
                new ArrayAdapter<Group>(getApplicationContext(), android.R.layout.simple_spinner_dropdown_item, groupsArrayList);
        spinner_adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner_choose_group.setAdapter(spinner_adapter);
        spinner_choose_group.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                fetchGroupLocation();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });


        // Adding menu icon to Toolbar
        ActionBar supportActionBar = getSupportActionBar();
        if (supportActionBar != null) {
            supportActionBar.setHomeAsUpIndicator(R.drawable.ic_ham);
            supportActionBar.setDisplayHomeAsUpEnabled(true);
            supportActionBar.setHomeButtonEnabled(true);
        }


        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                mDrawerLayout.closeDrawers();

                Intent i;
                switch (menuItem.getItemId()) {

                    case R.id.menu_item_make_group:
                        i = new Intent(UserHomeActivity.this, MakeGroupActivity.class);
                        startActivity(i);
                        return true;
                    case R.id.menu_item_view_invites:
                        i = new Intent(UserHomeActivity.this, ViewInvitesActivity.class);
                        startActivity(i);
                        return true;
                    case R.id.menu_item_my_groups:
                        i = new Intent(UserHomeActivity.this, MyGroupsActivity.class);
                        startActivity(i);
                        return true;
                    case R.id.menu_item_plan_a_meeting:
                        i = new Intent(UserHomeActivity.this, PlanAMeetingActivity.class);
                        startActivity(i);
                        return true;
                    case R.id.nav_manage:
                        i = new Intent(UserHomeActivity.this, ViewMeetings.class);
                        startActivity(i);
                        return true;
                    case R.id.mark_danger:
                        i = new Intent(UserHomeActivity.this, MarkDangerLocation.class);
                        startActivity(i);
                        return true;
                    case R.id.view_danger_location:
                        i = new Intent(UserHomeActivity.this, ViewDangerLocation.class);
                        startActivity(i);
                        return true;
                    case R.id.logout:
                        logout();
                        return true;
                }
                return false;
            }
        });
        View headerView = navigationView.getHeaderView(0);
        TextView tv_username = headerView.findViewById(R.id.nav_header_username);
        im_photo = headerView.findViewById(R.id.nav_header_userphoto);
        ImageView edit_photo = headerView.findViewById(R.id.edit_photo);
        edit_photo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (ActivityCompat.checkSelfPermission(UserHomeActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(UserHomeActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 110);
                } else {
                    Intent intent = new Intent();
                    intent.setType("image/*");
                    intent.setAction(Intent.ACTION_GET_CONTENT);
                    startActivityForResult(Intent.createChooser(intent, "Select Picture"), 106);
                }
            }
        });
        SharedPreferences sharedPreferences = getSharedPreferences("myprefs", MODE_PRIVATE);
        String session_username = sharedPreferences.getString("session_username", "");
        String session_img_url = sharedPreferences.getString("session_img_url", "");
        tv_username.setText(session_username);
        Glide.with(this).load(session_img_url).apply(RequestOptions.circleCropTransform()).into(im_photo);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        getGroups();
    }

    private void mapNumbers() {

        FirebaseDatabase.getInstance().getReference("users").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot d : dataSnapshot.getChildren()) {
                    GlobalApp.name_no_mapping.put(d.getKey(), d.child("username").getValue(String.class));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    void getGroups() {
        FirebaseDatabase
                .getInstance()
                .getReference("users")
                .child(GlobalApp.phone_number)
                .child("groups")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull final DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {
                            groupsArrayList.clear();
                            spinner_adapter.notifyDataSetChanged();

                            for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                final String group_code = snapshot.getValue(String.class);
                                FirebaseDatabase
                                        .getInstance()
                                        .getReference("groups")
                                        .child(group_code)
                                        .addListenerForSingleValueEvent(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(@NonNull DataSnapshot dataSnapshotMembers) {
                                                if (dataSnapshotMembers.exists()) {
                                                    Group group = dataSnapshotMembers.getValue(Group.class);
                                                    groupsArrayList.add(group);
                                                    spinner_adapter.notifyDataSetChanged();
                                                } else {
                                                    Toast.makeText(UserHomeActivity.this, "Data Doesn't Exist", Toast.LENGTH_SHORT).show();
                                                }
                                            }

                                            @Override
                                            public void onCancelled(@NonNull DatabaseError databaseError) {
                                                Toast.makeText(UserHomeActivity.this, databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                                            }
                                        });
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        Toast.makeText(UserHomeActivity.this, databaseError.getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                mDrawerLayout.openDrawer(GravityCompat.START);
                return true;

        }

        return super.onOptionsItemSelected(item);
    }

    void fetchGroupLocation() {
        if (spinner_choose_group.getSelectedItem() != null) {

            Group selected_group = (Group) spinner_choose_group.getSelectedItem();
            mMap.clear();
            selected_group_membersArrayList.clear();
            markersHashMap.clear();
            Set<String> keySet = selected_group.getMembers().keySet();
            for (String member_phone : keySet) {
                FirebaseDatabase
                        .getInstance()
                        .getReference("users")
                        .child(selected_group.getMembers().get(member_phone)).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {
                            final User user_temp = dataSnapshot.getValue(User.class);

                            selected_group_membersArrayList.add(user_temp);
                            adapter_selected_group.notifyDataSetChanged();

                            double last_latitude = user_temp.getLast_latitude();
                            double last_longitude = user_temp.getLast_longitude();

                            final LatLng temp_user_location = new LatLng(last_latitude, last_longitude);

                            Glide.with(UserHomeActivity.this).load(user_temp.photo).listener(new RequestListener<Drawable>() {
                                @Override
                                public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                                    return false;
                                }

                                @Override
                                public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
//                                    getMarkerBitmapFromView(resource);
                                    Log.d(TAG, "onResourceReady: hello");
                                    Marker marker = mMap.addMarker(new MarkerOptions()
                                            .position(temp_user_location).icon(BitmapDescriptorFactory.fromBitmap(getMarkerBitmapFromView(resource, user_temp.username))).title(user_temp.getUsername()));
                                    marker.showInfoWindow();
                                    markersHashMap.put(marker, user_temp);
                                    return false;

                                }
                            }).apply(RequestOptions.circleCropTransform()).into(100, 100);

                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        Toast.makeText(UserHomeActivity.this, "Internet Issue, Try again", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        } else {
            Toast.makeText(this, "Invalid Selection", Toast.LENGTH_SHORT).show();
        }
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;


        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                BottomSheetFragment bottomSheetFragment = new BottomSheetFragment();
                bottomSheetFragment.setCurr_user(markersHashMap.get(marker));
                bottomSheetFragment.show(getSupportFragmentManager(), bottomSheetFragment.getTag());
                return false;
            }
        });

    }

    class RecyclerAdapterGroupMembers extends RecyclerView.Adapter<RecyclerAdapterGroupMembers.MyViewHolder> {

        // Define ur own View Holder (Refers to Single Row)
        class MyViewHolder extends RecyclerView.ViewHolder {
            CardView singlecardview;

            // We have Changed View (which represent single row) to CardView in whole code
            public MyViewHolder(CardView itemView) {
                super(itemView);
                singlecardview = (itemView);
            }
        }


        // Inflate ur Single Row / CardView from XML here
        @Override
        public RecyclerAdapterGroupMembers.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater inflater = LayoutInflater.from(parent.getContext());

            View viewthatcontainscardview = inflater.inflate(R.layout.recycler_item_user_home_group_members, parent, false);

            CardView cardView = (CardView) (viewthatcontainscardview.findViewById(R.id.cardview_user_home_group_member));

            // This will call Constructor of MyViewHolder, which will further copy its reference
            // to customview (instance variable name) to make its usable in all other methods of class
            Log.d("MYMESSAGE", "On CreateView Holder Done");
            return new MyViewHolder(cardView);
        }

        @Override
        public void onBindViewHolder(RecyclerAdapterGroupMembers.MyViewHolder holder, final int position) {

            final User curr_user = selected_group_membersArrayList.get(position);

            CardView localcardview = holder.singlecardview;

            localcardview.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(curr_user.getLast_latitude(), curr_user.getLast_longitude()), 13));

                    CameraPosition cameraPosition = new CameraPosition.Builder()
                            .target(new LatLng(curr_user.getLast_latitude(), curr_user.getLast_longitude()))      // Sets the center of the map to location user
                            .zoom(17)                   // Sets the zoom
                            .bearing(90)                // Sets the orientation of the camera to east
                            .tilt(40)                   // Sets the tilt of the camera to 30 degrees
                            .build();                   // Creates a CameraPosition from the builder
                    mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));

                    // Toast.makeText(getApplicationContext(),position+" clicked",Toast.LENGTH_LONG).show();
                }
            });


            TextView tv_group_member_name;
            final ImageView im_group_member_photo;

            tv_group_member_name = (TextView) (localcardview.findViewById(R.id.tv_group_member_name));
            im_group_member_photo = (ImageView) (localcardview.findViewById(R.id.im_group_member_photo));
            tv_group_member_name.setText(curr_user.getUsername().toUpperCase());
            Glide.with(UserHomeActivity.this).load(curr_user.photo).apply(RequestOptions.circleCropTransform()).into(im_group_member_photo);
        }

        @Override
        public int getItemCount() {
            return selected_group_membersArrayList.size();
        }
    }

    private Bitmap getMarkerBitmapFromView(Drawable res, String name) {

        View customMarkerView = ((LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.custom_marker, null);
        ImageView markerImageView = (ImageView) customMarkerView.findViewById(R.id.profile_image);

        markerImageView.setImageDrawable(res);
        //  Glide.with(this).load(photo_url).into(markerImageView);
        customMarkerView.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
        customMarkerView.layout(0, 0, customMarkerView.getMeasuredWidth(), customMarkerView.getMeasuredHeight());
        customMarkerView.buildDrawingCache();
        Bitmap returnedBitmap = Bitmap.createBitmap(customMarkerView.getMeasuredWidth(), customMarkerView.getMeasuredHeight(),
                Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(returnedBitmap);
        canvas.drawColor(Color.WHITE, PorterDuff.Mode.SRC_IN);
        Drawable drawable = customMarkerView.getBackground();
        if (drawable != null)
            drawable.draw(canvas);
        customMarkerView.draw(canvas);
        return returnedBitmap;
    }

    private void logout() {
        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setCancelable(false);
        progressDialog.setTitle("Logging Out !!");
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.show();
        GlobalApp.clearAll();
        SharedPreferences sp = getSharedPreferences("myprefs", MODE_PRIVATE);
        SharedPreferences.Editor sp_editor = sp.edit();
        sp_editor.clear();
        sp_editor.commit();
        progressDialog.dismiss();
        stopService();
        Intent i = new Intent(this, MainActivity.class);
        startActivity(i);
    }

    private void stopService() {
        if (FetchContinousLocationService.isServiceStarted) {
            Intent service_intent = new Intent(this, FetchContinousLocationService.class);
            service_intent.setAction("STOP SIGNAL");
            startService(service_intent);
        }
        if (CheckDangerLocationService.isStarted) {
            Intent service_intent = new Intent(this, CheckDangerLocationService.class);
            service_intent.setAction("STOP SIGNAL");
            startService(service_intent);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == 106) {
            if (resultCode == RESULT_OK) {
                if (data.getData() != null) {
                    CustomToast.makeDefaultToast(UserHomeActivity.this, "Uploading Photo").show();
                    Uri uri = data.getData();
                    final StorageReference reference = FirebaseStorage.getInstance().getReference("images");
                    UploadTask uploadTask = reference.putFile(uri);
                    uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                        @Override
                        public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                            if (!task.isSuccessful()) {
                                throw Objects.requireNonNull(task.getException());
                            }

                            return reference.getDownloadUrl();
                        }
                    }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                        @Override
                        public void onComplete(@NonNull Task<Uri> task) {
                            if (task.isSuccessful()) {
                                CustomToast.makeSuccessToast(UserHomeActivity.this, "Successfully Uploaded Photo", View.VISIBLE).show();
                                Uri downloadUri = task.getResult();
                                FirebaseDatabase.getInstance().getReference("users").child(GlobalApp.phone_number).child("photo").setValue(downloadUri.toString());
                                SharedPreferences sharedPreferences = getSharedPreferences("myprefs", MODE_PRIVATE);
                                SharedPreferences.Editor editor = sharedPreferences.edit();
                                editor.putString("session_img_url", downloadUri.toString());
                                editor.apply();
                                Glide.with(UserHomeActivity.this).load(downloadUri).apply(RequestOptions.circleCropTransform()).into(im_photo);
                            } else {
                                CustomToast.makeErrorToast(UserHomeActivity.this, "Failed To Upload Photo", View.VISIBLE).show();

                            }
                        }
                    });

                }
            } else {
                CustomToast.makeInfoToast(this, "No Photo Selected", View.VISIBLE).show();
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == 110) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent, "Select Picture"), 106);
            } else {
                CustomToast.makeInfoToast(this, "Permission Denied! Can't Select Photo", View.VISIBLE).show();
            }
        }
    }
}

