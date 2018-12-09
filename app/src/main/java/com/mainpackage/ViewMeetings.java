package com.mainpackage;

import android.app.Dialog;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.LinearSmoothScroller;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.SpannableString;
import android.text.style.UnderlineSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;

public class ViewMeetings extends AppCompatActivity {

    RecyclerView rv_meetings;
    DatabaseReference dbRef;
    ArrayList<String> meetingCodeList;
    ArrayList<Meeting> meetingsList;
    MeetingAdapter meetingAdapter;
    ImageView no_data_view_my_meetings;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_meetings);
        no_data_view_my_meetings = findViewById(R.id.no_data_view_meetings);
        Toolbar toolbar = findViewById(R.id.toolbar_my_meetings);
        setSupportActionBar(toolbar);

        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        dbRef = FirebaseDatabase.getInstance().getReference();
        meetingCodeList = new ArrayList<>();
        meetingsList = new ArrayList<>();
        meetingAdapter = new MeetingAdapter();
        rv_meetings = findViewById(R.id.rv_meetings);
        rv_meetings.setLayoutManager(new LinearLayoutManager(this));
        rv_meetings.setAdapter(meetingAdapter);
        dbRef.child("users").child(GlobalApp.phone_number).child("meeting").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                meetingCodeList.clear();
                meetingAdapter.notifyDataSetChanged();
                if (dataSnapshot.exists()) {
                    no_data_view_my_meetings.setVisibility(View.GONE);
                    for (DataSnapshot d : dataSnapshot.getChildren()) {
                        meetingCodeList.add(d.getValue(String.class));
                    }
                    getMyMeetings();
                } else {
                    no_data_view_my_meetings.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


    }

    private void getMyMeetings() {
        for (String s : meetingCodeList) {
            dbRef.child("meeting").child(s).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    meetingsList.add(dataSnapshot.getValue(Meeting.class));
                    Log.d("MYMSG", "Item added " + meetingsList.size());
                    meetingAdapter.notifyDataSetChanged();
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }
        Log.d("MYMSG", "adapter notify");

    }

    class MeetingAdapter extends RecyclerView.Adapter<MeetingAdapter.MeetingViewHolder> {


        @NonNull
        @Override
        public MeetingViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
            return new MeetingViewHolder(LayoutInflater.from(ViewMeetings.this).inflate(R.layout.single_row_meeting, viewGroup, false));
        }

        @Override
        public void onBindViewHolder(@NonNull final MeetingViewHolder meetingViewHolder, final int i) {
            meetingViewHolder.tv_title.setText(meetingsList.get(i).getTitle().toUpperCase());
            meetingViewHolder.tv_date.setText(" " + meetingsList.get(i).getDate().substring(0, 10) + "\n" + " " + meetingsList.get(i).getDate().substring(11, 20));
            SpannableString content = new SpannableString(meetingsList.get(i).getLocation());
            content.setSpan(new UnderlineSpan(), 0, content.length(), 0);
            meetingViewHolder.tv_dialog_at.setText(content);
            meetingViewHolder.tv_dialog_at.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("geo:<lat>,<long>?q=" + meetingsList.get(i).lat + "," + meetingsList.get(i).lng + meetingsList.get(i).location));
                    startActivity(intent);
                }
            });
            meetingViewHolder.tv_dialog_created.setText(" " + GlobalApp.name_no_mapping.get(meetingsList.get(i).getHost()));
            meetingViewHolder.bt_chat.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(ViewMeetings.this, ChatActivity.class);
                    intent.putExtra("meeting", meetingsList.get(i).getId());
                    intent.putExtra("name", meetingsList.get(i).getTitle());
                    startActivity(intent);
                }
            });
            final ArrayList<User> alUser = new ArrayList<>();
            final HashMap<String, String> group_member = meetingsList.get(i).getGroup_member();
            Set<String> keySet = group_member.keySet();
            for (String s : keySet) {
                FirebaseDatabase.getInstance().getReference("users").child(group_member.get(s)).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        User user = dataSnapshot.getValue(User.class);
                        alUser.add(user);
                        if (alUser.size() == group_member.size()) {
                            final DialogAdapter dialogAdapter = new DialogAdapter(alUser);
                            LinearLayoutManager linearLayoutManager = new LinearLayoutManager(ViewMeetings.this, LinearLayoutManager.HORIZONTAL, false
                            );
                            meetingViewHolder.rv_dialog.setLayoutManager(linearLayoutManager);
                            meetingViewHolder.rv_dialog.addItemDecoration(new DividerItemDecoration(ViewMeetings.this, DividerItemDecoration.HORIZONTAL));
                            meetingViewHolder.rv_dialog.setAdapter(dialogAdapter);

                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
            }
        }

        @Override
        public int getItemCount() {
            return meetingsList.size();
        }

        public class MeetingViewHolder extends RecyclerView.ViewHolder {

            TextView tv_title, tv_date;
            TextView tv_dialog_created, tv_dialog_at;
            RecyclerView rv_dialog;
            Button bt_chat;


            public MeetingViewHolder(@NonNull View itemView) {
                super(itemView);
                tv_title = itemView.findViewById(R.id.tv_meeting_title);
                tv_date = itemView.findViewById(R.id.tv_meeting_time);
                tv_dialog_created = itemView.findViewById(R.id.tv_meeting_created);
                tv_dialog_at = itemView.findViewById(R.id.tv_meeting_at);
                rv_dialog = itemView.findViewById(R.id.rv_meeting_dialog);
                bt_chat = itemView.findViewById(R.id.dialog_chat);

            }
        }

        class DialogAdapter extends RecyclerView.Adapter<DialogAdapter.DialogViewHolder> {


            ArrayList<User> alUser;

            public DialogAdapter(ArrayList<User> alUser) {
                this.alUser = alUser;
            }

            @NonNull
            @Override
            public DialogViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
                return new DialogViewHolder(LayoutInflater.from(ViewMeetings.this).inflate(R.layout.single_row_dialog_user, viewGroup, false));
            }

            @Override
            public void onBindViewHolder(@NonNull DialogViewHolder dialogViewHolder, final int i) {
                dialogViewHolder.user_name.setText(alUser.get(i).username.toUpperCase());
                dialogViewHolder.user_phone.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent = new Intent(Intent.ACTION_DIAL);
                        intent.setData(Uri.parse("tel:" + alUser.get(i).phone));
                        startActivity(intent);
                    }
                });
                Glide.with(ViewMeetings.this).load(alUser.get(i).photo).into(dialogViewHolder.user_photo);
            }

            @Override
            public int getItemCount() {
                return alUser.size();
            }

            public class DialogViewHolder extends RecyclerView.ViewHolder {

                TextView user_name;
                ImageView user_photo;
                ImageButton user_phone;

                public DialogViewHolder(@NonNull View itemView) {
                    super(itemView);
                    user_name = itemView.findViewById(R.id.user_name);
                    user_phone = itemView.findViewById(R.id.user_phone);
                    user_photo = itemView.findViewById(R.id.user_photo);
                }
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
