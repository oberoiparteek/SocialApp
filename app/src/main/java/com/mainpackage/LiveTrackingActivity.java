package com.mainpackage;

import android.app.ProgressDialog;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

public class LiveTrackingActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private String phone_number_to_track;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_live_tracking);
        phone_number_to_track = getIntent().getStringExtra("phone_number");
        if (phone_number_to_track == null || phone_number_to_track.isEmpty()) {
            this.finish();
        }
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Loading Data");
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

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
        progressDialog.show();
        getTodayHistory();
    }

    void getTodayHistory() {

        final long time = System.currentTimeMillis();
        java.sql.Date d = new java.sql.Date(time);
        final String date_today = d.toString();
        FirebaseDatabase
                .getInstance()
                .getReference("users")
                .child(phone_number_to_track)
                .child("locations")
                .orderByChild("date")
                .equalTo(date_today).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshotLocations) {

                LatLng[] latLngs = new LatLng[(int) (dataSnapshotLocations.getChildrenCount())];
                int count = 0;

                if (dataSnapshotLocations.exists()) {
                    for (DataSnapshot snapshot : dataSnapshotLocations.getChildren()) {
                        LocationDAO locationDAO_temp = snapshot.getValue(LocationDAO.class);
                        latLngs[count++] = new LatLng(locationDAO_temp.latitude, locationDAO_temp.longitude);

                    }
                }

                Polyline polyline1 = mMap.addPolyline(new PolylineOptions()
                        .clickable(true)
                        .add(latLngs)
                );
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLngs[0], 13));

                CameraPosition cameraPosition = new CameraPosition.Builder()
                        .target(latLngs[0])      // Sets the center of the map to location user
                        .zoom(17)                   // Sets the zoom
                        .bearing(90)                // Sets the orientation of the camera to east
                        .tilt(40)                   // Sets the tilt of the camera to 30 degrees
                        .build();                   // Creates a CameraPosition from the builder
                mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
                progressDialog.dismiss();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(LiveTrackingActivity.this, databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

    }


}
