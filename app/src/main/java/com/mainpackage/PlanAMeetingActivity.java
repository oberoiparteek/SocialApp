package com.mainpackage;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocompleteFragment;
import com.google.android.gms.location.places.ui.PlaceSelectionListener;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.ib.custom.toast.CustomToast;
import com.toptoche.searchablespinnerlibrary.SearchableSpinner;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Set;

public class PlanAMeetingActivity extends AppCompatActivity implements OnMapReadyCallback {

    SearchableSpinner spinner_group_meeting;
    ArrayList<Group> group_codesArrayList = new ArrayList<>();
    ArrayAdapter<Group> spinner_adapter;
    String locationName;
    private GoogleMap mMap;
    TextView tv_schedule_place;
    EditText et_date, et_time, et_title;
    Calendar selectedTime;
    LatLng latLng;
    int count = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_plan_a_meeting);

        Toolbar toolbar = findViewById(R.id.toolbar_plan_a_meeting);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        selectedTime = Calendar.getInstance();
        tv_schedule_place = findViewById(R.id.tv_schedule_place);
        et_date = findViewById(R.id.et_date);
        et_time = findViewById(R.id.et_time);
        et_title = findViewById(R.id.et_title);
        spinner_group_meeting = findViewById(R.id.spinner_group_meeting);
        spinner_adapter =
                new ArrayAdapter<Group>(getApplicationContext(), android.R.layout.simple_spinner_dropdown_item, group_codesArrayList);
        spinner_adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner_group_meeting.setAdapter(spinner_adapter);

        PlaceAutocompleteFragment autocompleteFragment = (PlaceAutocompleteFragment)
                getFragmentManager().findFragmentById(R.id.fragment_place_autocomplete);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        autocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {
                mMap.clear();
                locationName = place.getName().toString();
                latLng = place.getLatLng();
                mMap.addMarker(new MarkerOptions().position(place.getLatLng()).title(locationName));
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(place.getLatLng(), 12.0f));
                tv_schedule_place.setText(place.getAddress());
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
                .child("groups").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshotGroupCodes) {
                if (dataSnapshotGroupCodes.exists()) {
                    group_codesArrayList.clear();
                    spinner_adapter.notifyDataSetChanged();

                    for (DataSnapshot snapshot : dataSnapshotGroupCodes.getChildren()) {
                        String group_code = snapshot.getValue(String.class);
                        FirebaseDatabase.getInstance().getReference("groups").child(group_code).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshotGroup) {
                                if (dataSnapshotGroup.exists()) {
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

                } else {
                    CustomToast.makeInfoToast(PlanAMeetingActivity.this, "No Group Present", View.VISIBLE).show();
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
        this.mMap = googleMap;

    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case android.R.id.home:
                this.finish();
                return true;
        }
        return false;
    }


    public void schedule(View view) {

        Calendar currentTime = Calendar.getInstance();
        if (et_title.getText().toString().trim().isEmpty() || latLng == null) {
            Toast.makeText(this, "All Fields Are Compulsory", Toast.LENGTH_SHORT).show();
            return;
        }

        if (selectedTime.compareTo(currentTime) < 0) {
            Toast.makeText(this, "Time Already Passed", Toast.LENGTH_SHORT).show();
        } else {
            count = 0;
            int position = spinner_group_meeting.getSelectedItemPosition();
            final Group group = group_codesArrayList.get(position);
            final DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference("meeting");
            final String key = dbRef.push().getKey();
            Meeting meeting = new Meeting(group.getGroupCode(), group.getMembers(),
                    et_title.getText().toString().trim(), selectedTime.getTime().toString(), GlobalApp.phone_number,
                    key, locationName, latLng.latitude, latLng.longitude);
            dbRef.child(key)
                    .setValue(meeting)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                Set<String> keySet = group.getMembers().keySet();
                                for (String members : keySet) {
                                    FirebaseDatabase.getInstance().getReference("users")
                                            .child(group.getMembers().get(members))
                                            .child("meeting")
                                            .push()
                                            .setValue(key)
                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {
                                                    count++;
                                                    if (count == group.getMembers().size() - 1) {
                                                        Toast.makeText(PlanAMeetingActivity.this, "Meeting Successfully Scheduled", Toast.LENGTH_SHORT).show();
                                                        finish();
                                                    }
                                                }
                                            });
                                }
                            }
                        }
                    });
        }


    }

    public void timeSelect(View view) {
        Calendar mcurrentTime = Calendar.getInstance();
        final int hour = mcurrentTime.get(Calendar.HOUR_OF_DAY);
        int minute = mcurrentTime.get(Calendar.MINUTE);
        TimePickerDialog mTimePicker;
        mTimePicker = new TimePickerDialog(PlanAMeetingActivity.this, new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker timePicker, int selectedHour, int selectedMinute) {
                selectedTime.set(Calendar.HOUR_OF_DAY, selectedHour);
                selectedTime.set(Calendar.MINUTE, selectedMinute);
                et_time.setText(selectedHour + ":" + selectedMinute);
            }
        }, hour, minute, true);//Yes 24 hour time
        mTimePicker.setTitle("Select Time");
        mTimePicker.show();
    }

    public void dateSelect(View view) {
        Calendar mcurrentTime = Calendar.getInstance();
        int mYear = mcurrentTime.get(Calendar.YEAR);
        int mMonth = mcurrentTime.get(Calendar.MONTH);
        int mDay = mcurrentTime.get(Calendar.DAY_OF_MONTH);
        DatePickerDialog mDatePicker;
        mDatePicker = new DatePickerDialog(PlanAMeetingActivity.this, new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker datePicker, int i, int i1, int i2) {
                et_date.setText(i + "-" + i1 + "-" + i2);
                selectedTime.set(Calendar.YEAR, i);
                selectedTime.set(Calendar.MONTH, i1);
                selectedTime.set(Calendar.DAY_OF_MONTH, i2);

            }
        }, mYear, mMonth, mDay);
        mDatePicker.setTitle("Select Date");
        mDatePicker.show();
    }
}
