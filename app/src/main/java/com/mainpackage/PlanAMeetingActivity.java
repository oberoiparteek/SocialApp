package com.mainpackage;

import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocompleteFragment;
import com.google.android.gms.location.places.ui.PlaceSelectionListener;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class PlanAMeetingActivity extends AppCompatActivity implements OnMapReadyCallback {

    Spinner spinner_group_meeting;
    ArrayList<Group> group_codesArrayList = new ArrayList<>();
    ArrayAdapter<Group> spinner_adapter;
    Marker m;
    String locationName, meetingId, owner;
    double locationLat, locationLon;
    private GoogleMap mMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_plan_a_meeting);
        spinner_group_meeting = findViewById(R.id.spinner_group_meeting);
        spinner_adapter =
                new ArrayAdapter<Group>(getApplicationContext(), android.R.layout.simple_spinner_dropdown_item, group_codesArrayList);
        spinner_adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner_group_meeting.setAdapter(spinner_adapter);

        PlaceAutocompleteFragment autocompleteFragment = (PlaceAutocompleteFragment)
                getFragmentManager().findFragmentById(R.id.fragment_place_autocomplete);

        autocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {
                if (m != null) {
                    locationName = place.getName().toString();
                    LatLng latLng = place.getLatLng();
                    locationLat = latLng.latitude;
                    locationLon = latLng.longitude;
                    m = mMap.addMarker(new MarkerOptions().position(place.getLatLng()).title(locationName));
                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(place.getLatLng(), 12.0f));

                }
            }

            @Override
            public void onError(Status status) {
                Log.i("MYMSG", "An error occurred: " + status);
            }
        });

        load_groups();
    }

    private void load_groups() {
        FirebaseDatabase
                .getInstance()
                .getReference("users")
                .child(GlobalApp.phone_number)
                .child("groupcodes").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshotGroupCodes) {
                if (dataSnapshotGroupCodes != null) {
                    group_codesArrayList.clear();
                    spinner_adapter.notifyDataSetChanged();

                    for (DataSnapshot snapshot : dataSnapshotGroupCodes.getChildren()) {
                        String group_code = snapshot.getValue(String.class);
                        FirebaseDatabase.getInstance().getReference("groups").child(group_code).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshotGroup) {
                                if (dataSnapshotGroup != null) {
                                    Group curr_group = dataSnapshotGroup.getValue(Group.class);
                                    group_codesArrayList.add(curr_group);
                                    spinner_adapter.notifyDataSetChanged();
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {
                                Toast.makeText(PlanAMeetingActivity.this, databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });
                    }

                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(PlanAMeetingActivity.this, databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {

        this.mMap=googleMap;
    }
}
