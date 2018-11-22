package com.mainpackage;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.NavigationView;
import android.os.Bundle;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
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
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.Serializable;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;

public class UserHomeActivity extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private DrawerLayout mDrawerLayout;
    private RecyclerView recycler_view_group_members;
    private RecyclerAdapterGroupMembers adapter_selected_group;
    ArrayList<Group> groupsArrayList = new ArrayList<>();
    ArrayList<User> selected_group_membersArrayList = new ArrayList<>();
    Spinner spinner_choose_group;
    ArrayAdapter<Group> spinner_adapter;
    final String TAG = "MYMSG";


    private HashMap<Marker, User> markersHashMap=new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_home);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_user_home);
        setSupportActionBar(toolbar);

        // Create Navigation drawer and inflate layout
        final NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view_user_home);
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout_user_home);
        adapter_selected_group = new RecyclerAdapterGroupMembers();
        recycler_view_group_members = findViewById(R.id.recycler_view_group_members);


        recycler_view_group_members.setAdapter(adapter_selected_group);

        GridLayoutManager layoutManager = new GridLayoutManager(this, 1);
        layoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        recycler_view_group_members.setLayoutManager(layoutManager);


        spinner_choose_group = findViewById(R.id.spinner_choose_group);
        spinner_adapter =
                new ArrayAdapter<Group>(getApplicationContext(), android.R.layout.simple_spinner_dropdown_item, groupsArrayList);
        spinner_adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner_choose_group.setAdapter(spinner_adapter);


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
                // set item as selected to persist highlight
                // close drawer when item is tapped
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
                        i=new Intent(UserHomeActivity.this,PlanAMeetingActivity.class);
                        startActivity(i);
                        return  true;
                }
                return true;
            }
        });
        View headerView = navigationView.getHeaderView(0);
        TextView tv_username = headerView.findViewById(R.id.nav_header_username);
        ImageView im_photo = headerView.findViewById(R.id.nav_header_userphoto);
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

    void getGroups() {
        FirebaseDatabase
                .getInstance()
                .getReference("users")
                .child(GlobalApp.phone_number)
                .child("groupcodes")
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
            case R.id.menu_item_change_photo:
                Toast.makeText(this, "Change Photo", Toast.LENGTH_SHORT).show();
                return true;
            case R.id.menu_item_logout:
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
                Intent i = new Intent(this, MainActivity.class);
                startActivity(i);
                return true;
            case R.id.menu_stop_service:
                Intent service_intent = new Intent(this, FetchContinousLocationService.class);
                service_intent.setAction("STOP SIGNAL");
                startService(service_intent);

        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_user_home, menu);
        return true;
    }

    void bt_event_fetch_group_details(View v) {

        if (spinner_choose_group.getSelectedItem() != null) {

            Group selected_group = (Group) spinner_choose_group.getSelectedItem();
            mMap.clear();
            selected_group_membersArrayList.clear();
            markersHashMap.clear();
            for (String member_phone : selected_group.getMembers()) {
                FirebaseDatabase
                        .getInstance()
                        .getReference("users")
                        .child(member_phone).addListenerForSingleValueEvent(new ValueEventListener() {
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


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
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
            tv_group_member_name.setText(curr_user.getUsername());
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



}

