package com.mainpackage;

import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class ViewDangerLocation extends AppCompatActivity {


    ArrayList<Danger> alDanger;
    public static Danger danger;
    RecyclerView rv_danger;
    ImageView no_data_view_danger;
    MapDialog newFragment;
    DangerAdapter dangerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_danger_location);
        no_data_view_danger = findViewById(R.id.no_data_view_danger);

        Toolbar toolbar = findViewById(R.id.toolbar_view_danger);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);


        alDanger = new ArrayList<>();
        dangerAdapter = new DangerAdapter();
        rv_danger = findViewById(R.id.rv_view_danger_location);
        rv_danger.setLayoutManager(new LinearLayoutManager(this));
        rv_danger.setAdapter(dangerAdapter);
        FirebaseDatabase.getInstance().getReference("danger").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                alDanger.clear();
                dangerAdapter.notifyDataSetChanged();
                if (dataSnapshot.exists()) {
                    no_data_view_danger.setVisibility(View.GONE);
                    for (DataSnapshot d : dataSnapshot.getChildren()) {
                        alDanger.add(d.getValue(Danger.class));
                        dangerAdapter.notifyDataSetChanged();
                    }
                } else {
                    no_data_view_danger.setVisibility(View.VISIBLE);
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    class DangerAdapter extends RecyclerView.Adapter<DangerAdapter.DangerHolder> {
        @NonNull
        @Override
        public DangerHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
            return new DangerHolder(LayoutInflater.from(ViewDangerLocation.this).inflate(R.layout.single_row_danger_location, viewGroup, false));
        }

        @Override
        public void onBindViewHolder(@NonNull DangerHolder viewHolder, final int i) {
            viewHolder.tv_location.setText(" " + alDanger.get(i).getLocation());
            viewHolder.tv_reason.setText(" " + alDanger.get(i).getReason());
            viewHolder.tv_date.setText(" " + alDanger.get(i).getDate());
            viewHolder.tv_created.setText(" " + GlobalApp.name_no_mapping.get(alDanger.get(i).getHost()).toUpperCase() + " (" + alDanger.get(i).getHost() + ")");
            viewHolder.tv_view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    danger = alDanger.get(i);
                    android.support.v4.app.FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
                    android.support.v4.app.Fragment prev = getSupportFragmentManager().findFragmentByTag("dialog");
                    if (prev != null) {
                        ft.remove(prev);
                    }
                    ft.addToBackStack(null);

                    // Create and show the dialog.
                    MapDialog mapDialog = new MapDialog();
                    mapDialog.show(ft, "dialog");

                }
            });
        }

        @Override
        public int getItemCount() {
            return alDanger.size();
        }

        public class DangerHolder extends RecyclerView.ViewHolder {

            TextView tv_created, tv_location, tv_date, tv_reason, tv_view;

            public DangerHolder(@NonNull View itemView) {
                super(itemView);
                tv_location = itemView.findViewById(R.id.tv_location);
                tv_created = itemView.findViewById(R.id.tv_created);
                tv_reason = itemView.findViewById(R.id.tv_reason);
                tv_date = itemView.findViewById(R.id.tv_date);
                tv_view = itemView.findViewById(R.id.tv_view);
            }
        }

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        }
        return true;
    }

}
