package com.mainpackage;

import android.app.DatePickerDialog;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;
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

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class ViewLocationHistoryActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    String phone_number_to_track;
     Calendar myCalendar;
    EditText et_view_history_date_picker;
    DatePickerDialog.OnDateSetListener date;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_location_history);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        phone_number_to_track = getIntent().getStringExtra("phone_number");
        Toast.makeText(this, "Select a date", Toast.LENGTH_SHORT).show();
        et_view_history_date_picker = findViewById(R.id.et_view_history_date_picker);

        date = new DatePickerDialog.OnDateSetListener() {

            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear,
                                  int dayOfMonth) {
                myCalendar.set(Calendar.YEAR, year);
                myCalendar.set(Calendar.MONTH, monthOfYear);
                myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                updateMap();
            }

        };
        et_view_history_date_picker.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                new DatePickerDialog(ViewLocationHistoryActivity.this, date, myCalendar
                        .get(Calendar.YEAR), myCalendar.get(Calendar.MONTH),
                        myCalendar.get(Calendar.DAY_OF_MONTH)).show();
            }
        });
        myCalendar = Calendar.getInstance();
    }
    void updateMap(){
        String myFormat = "yy-dd-mm";
        SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.US);
        String date_to_filter = sdf.format(myCalendar.getTime());
        et_view_history_date_picker.setText(date_to_filter);
        Log.d("MYMSG", "updateMap: "+phone_number_to_track);
        FirebaseDatabase
                .getInstance()
                .getReference("users")
                .child(phone_number_to_track)
                .child("locations")
                .orderByChild("date")
                .equalTo(date_to_filter).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshotLocations) {

                LatLng[] latLngs = new LatLng[(int) (dataSnapshotLocations.getChildrenCount())];
                int count = 0;
                Log.d("MYMSG", "onDataChange: "+dataSnapshotLocations);

                if (dataSnapshotLocations.exists()) {
                    for (DataSnapshot snapshot : dataSnapshotLocations.getChildren()) {
                        LocationDAO locationDAO_temp = snapshot.getValue(LocationDAO.class);
                        latLngs[count++] = new LatLng(locationDAO_temp.latitude, locationDAO_temp.longitude);
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

                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(ViewLocationHistoryActivity.this, databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });



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

    }
}
