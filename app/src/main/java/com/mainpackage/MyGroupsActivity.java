package com.mainpackage;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

public class MyGroupsActivity extends AppCompatActivity {

    private String TAG = "MYMSG";
    private ProgressDialog progressDialog;
    RecyclerView recycler_view_my_groups;
    ArrayList<Group> myGroupsArrayList = new ArrayList<>();
    MyGroupsRecyclerAdapter myGroupsRecyclerAdapter;
    ImageView no_data_my_group;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_groups);
        no_data_my_group = findViewById(R.id.no_data_my_group);
        recycler_view_my_groups = findViewById(R.id.recycler_view_my_groups);
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Loading Groups.. !!");
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        Toolbar toolbar = findViewById(R.id.toolbar_my_groups);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        myGroupsRecyclerAdapter = new MyGroupsRecyclerAdapter();
        recycler_view_my_groups.setAdapter(myGroupsRecyclerAdapter);

        LinearLayoutManager simpleverticallayout = new LinearLayoutManager(this);
        recycler_view_my_groups.setLayoutManager(simpleverticallayout);
        getMyGroups();

    }

    public void getMyGroups() {
        progressDialog.show();
        FirebaseDatabase
                .getInstance()
                .getReference("users")
                .child(GlobalApp.phone_number).child("groups")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {
                            for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                String group_code = snapshot.getValue(String.class);
                                getGroupInfo(group_code);
                            }
                            no_data_my_group.setVisibility(View.GONE);
                        } else {
                            no_data_my_group.setVisibility(View.VISIBLE);
                        }
                        progressDialog.dismiss();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        Toast.makeText(MyGroupsActivity.this, "Request Cancelled", Toast.LENGTH_SHORT).show();
                        progressDialog.dismiss();
                    }
                });

    }

    void getGroupInfo(String group_code) {
        FirebaseDatabase
                .getInstance()
                .getReference("groups")
                .child(group_code).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Group group = dataSnapshot.getValue(Group.class);
                myGroupsArrayList.add(group);
                myGroupsRecyclerAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(MyGroupsActivity.this, "Request Cancelled", Toast.LENGTH_SHORT).show();
            }

        });

    }


    class MyGroupsRecyclerAdapter extends RecyclerView.Adapter<MyGroupsActivity.MyGroupsRecyclerAdapter.MyViewHolder> {

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
        public MyGroupsActivity.MyGroupsRecyclerAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater inflater = LayoutInflater.from(parent.getContext());

            View viewthatcontainscardview = inflater.inflate(R.layout.recycler_item_my_groups, parent, false);

            CardView cardView = (CardView) (viewthatcontainscardview.findViewById(R.id.cardview_item_my_groups));
            return new MyGroupsActivity.MyGroupsRecyclerAdapter.MyViewHolder(cardView);
        }

        @Override
        public void onBindViewHolder(MyGroupsActivity.MyGroupsRecyclerAdapter.MyViewHolder holder, final int position) {
            CardView localcardview = holder.singlecardview;
            final Group groupItem = myGroupsArrayList.get(position);
            TextView tv_item_group_name;
            final TextView tv_item_group_owner_name;
            final TextView tv_item_owner_phone_number;
            ImageButton bt_item_view_group_info;

            tv_item_group_name = localcardview.findViewById(R.id.tv_item_group_name);
            tv_item_group_owner_name = localcardview.findViewById(R.id.tv_item_group_owner_name);
            tv_item_owner_phone_number = localcardview.findViewById(R.id.tv_item_owner_phone_number);
            bt_item_view_group_info = localcardview.findViewById(R.id.bt_item_view_group_info);

            tv_item_owner_phone_number.setText(groupItem.getOwner());
            tv_item_group_owner_name.setText(GlobalApp.name_no_mapping.get(groupItem.getOwner()).toUpperCase());
            tv_item_group_name.setText(groupItem.getGroupName());


            bt_item_view_group_info.setOnClickListener(new View.OnClickListener() {
                DialogRecyclerAdapter dialogRecyclerAdapter;
                TextView tv_dialog_owner_phone;
                TextView tv_dialog_owner_name;
                ArrayList<User> membersArrayList = new ArrayList<>();
                Dialog dialog = null;
                User ownerObj = null;


                @Override
                public void onClick(View view) {

                    dialog = new Dialog(MyGroupsActivity.this, android.R.style.Theme_Light);
                    dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                    dialog.setContentView(R.layout.alert_dialog_group_info);

                    RecyclerView recycler_dialog_members = dialog.findViewById(R.id.recycler_dialog_members);
                    tv_dialog_owner_name = dialog.findViewById(R.id.tv_dialog_owner_name);
                    tv_dialog_owner_phone = dialog.findViewById(R.id.tv_dialog_owner_phone);
                    TextView tv_dialog_group_name = dialog.findViewById(R.id.tv_dialog_group_name);


                    tv_dialog_group_name.setText("" + groupItem.getGroupName());

                    dialogRecyclerAdapter = new DialogRecyclerAdapter(membersArrayList);
                    recycler_dialog_members.setAdapter(dialogRecyclerAdapter);

                    LinearLayoutManager simpleverticallayout = new LinearLayoutManager(getApplicationContext());
                    recycler_dialog_members.setLayoutManager(simpleverticallayout);

                    dialog.show();
                    loadMembers();
                }

                void loadMembers() {
                    membersArrayList.clear();

                    FirebaseDatabase.getInstance().getReference("users").child(groupItem.getOwner()).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            ownerObj = dataSnapshot.getValue(User.class);
                            tv_dialog_owner_name.setText(ownerObj.getUsername());
                            tv_dialog_owner_phone.setText(ownerObj.getPhone());
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });
                    HashMap<String, String> members = groupItem.getMembers();

                    Set<String> keySet = members.keySet();
                    for (String s : keySet) {
                        FirebaseDatabase.getInstance().getReference("users").child(members.get(s)).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                User value = dataSnapshot.getValue(User.class);
                                membersArrayList.add(value);
                                dialogRecyclerAdapter.notifyDataSetChanged();
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {
                                Toast.makeText(getApplicationContext(), "Couldn't Load Members", Toast.LENGTH_SHORT).show();
                            }

                        });
                    }
                }
            });


        }

        @Override
        public int getItemCount() {
            return myGroupsArrayList.size();
        }
    }


    class DialogRecyclerAdapter extends RecyclerView.Adapter<MyGroupsActivity.DialogRecyclerAdapter.MyViewHolder> {
        private ArrayList<User> membersArrayList;

        DialogRecyclerAdapter(ArrayList<User> members) {
            this.membersArrayList = members;
        }

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
        public MyGroupsActivity.DialogRecyclerAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater inflater = LayoutInflater.from(parent.getContext());

            View viewthatcontainscardview = inflater.inflate(R.layout.recycler_item_dialog_group_info, parent, false);

            CardView cardView = (CardView) (viewthatcontainscardview.findViewById(R.id.cardview_members));
            return new MyGroupsActivity.DialogRecyclerAdapter.MyViewHolder(cardView);
        }

        @Override
        public void onBindViewHolder(MyGroupsActivity.DialogRecyclerAdapter.MyViewHolder holder, final int position) {
            CardView localcardview = holder.singlecardview;

            User userItem = membersArrayList.get(position);

            TextView tv_dialog_member_name;
            TextView tv_dialog_member_phone_number;
            ImageView im_dialog_member_photo;

            tv_dialog_member_name = localcardview.findViewById(R.id.tv_dialog_member_name);
            tv_dialog_member_phone_number = localcardview.findViewById(R.id.tv_dialog_member_phone_number);
            im_dialog_member_photo = localcardview.findViewById(R.id.im_dialog_member_photo);

            tv_dialog_member_name.setText(userItem.getUsername());
            tv_dialog_member_phone_number.setText(userItem.getPhone());

            Glide.with(getApplicationContext()).load(userItem.photo).apply(RequestOptions.circleCropTransform()).into(im_dialog_member_photo);
        }

        @Override
        public int getItemCount() {
            return membersArrayList.size();
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
