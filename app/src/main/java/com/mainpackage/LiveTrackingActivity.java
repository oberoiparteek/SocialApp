package com.mainpackage;

import android.app.ProgressDialog;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.JointType;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.maps.model.RoundCap;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.ib.custom.toast.CustomToast;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class LiveTrackingActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private String phone_number_to_track;
    private ProgressDialog progressDialog;
    Polyline polyline;
    int count = 0;
    Marker end = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_live_tracking);
        phone_number_to_track = getIntent().getStringExtra("phone_number");
        if (phone_number_to_track == null || phone_number_to_track.isEmpty()) {
            Toast.makeText(this, "Unable To Find History", Toast.LENGTH_SHORT).show();
            return;
        }
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Loading Data");
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        String today = GlobalApp.simpleDateFormat.format(Calendar.getInstance().getTime());
        FirebaseDatabase
                .getInstance()
                .getReference("users")
                .child(phone_number_to_track)
                .child("locations")
                .orderByChild("date")
                .equalTo(today).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    getTodayHistory();
                } else {
                    CustomToast.makeInfoToast(LiveTrackingActivity.this, "No Data Found", View.VISIBLE).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    void getTodayHistory() {


        String today = GlobalApp.simpleDateFormat.format(Calendar.getInstance().getTime());
        FirebaseDatabase
                .getInstance()
                .getReference("users")
                .child(phone_number_to_track)
                .child("locations")
                .orderByChild("date")
                .equalTo(today).addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                if (dataSnapshot.exists()) {

                    LocationDAO locationDAO = dataSnapshot.getValue(LocationDAO.class);
                    LatLng latLng = new LatLng(locationDAO.getLatitude(), locationDAO.getLongitude());
                    updatePolyline(latLng);

                    if (count == 0) {
                        mMap.addMarker(new MarkerOptions().position(latLng).title("Start").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW)));
                    } else {
                        if (end != null) {
                            end.setVisible(false);
                        }
                        end = mMap.addMarker(new MarkerOptions().position(latLng).title("End").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));

                    }
                    count++;
                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    void updatePolyline(LatLng latLng) {
        if (polyline == null) {
            polyline = mMap.addPolyline(new PolylineOptions()
                    .clickable(true)
                    .add(latLng)
                    .jointType(JointType.ROUND)
                    .startCap(new RoundCap())
                    .width(12)
                    .color(0xff000000)
                    .geodesic(true)

            );
        } else {
            List<LatLng> points = polyline.getPoints();
            points.add(latLng);
            polyline.setPoints(points);
        }
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 13));

        CameraPosition cameraPosition = new CameraPosition.Builder()
                .target(latLng)      // Sets the center of the map to location user
                .zoom(17)                   // Sets the zoom
                .bearing(90)                // Sets the orientation of the camera to east
                .tilt(40)                   // Sets the tilt of the camera to 30 degrees
                .build();                   // Creates a CameraPosition from the builder
        mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));


    }


}
