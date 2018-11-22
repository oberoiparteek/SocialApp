package com.mainpackage;

import android.app.ProgressDialog;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class ViewInvitesActivity extends AppCompatActivity {
    private ArrayList<Invite> inviteArrayList = new ArrayList<>();
    private RecyclerView recycler_view_invites;
    private ViewInvitesActivity.InvitesRecyclerAdapter invitesRecyclerAdapter;
    private ProgressDialog progressDialog;
    private final String TAG = "MYMSG";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_invites);
        recycler_view_invites = findViewById(R.id.recycler_view_invites);
        progressDialog = new ProgressDialog(this);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.setTitle("Loading Invites");
        invitesRecyclerAdapter = new InvitesRecyclerAdapter();

        recycler_view_invites.setAdapter(invitesRecyclerAdapter);
        LinearLayoutManager simpleverticallayout = new LinearLayoutManager(this);
        recycler_view_invites.setLayoutManager(simpleverticallayout);

        loadInvites();

    }


    void loadInvites() {
        progressDialog.show();
        DatabaseReference invitesReference = FirebaseDatabase.getInstance().getReference("users/" + GlobalApp.phone_number + "/invitations");
        invitesReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull final DataSnapshot dataSnapshot) {
                inviteArrayList.clear();
//                invitesRecyclerAdapter.notifyDataSetChanged();
                if (dataSnapshot.exists()) {
                    for (final DataSnapshot snapshot : dataSnapshot.getChildren()) {

                        final String group_code = (String) snapshot.getValue();
                        Log.d(TAG, group_code + " load invites");
                        DatabaseReference groupsReference = FirebaseDatabase.getInstance().getReference("groups").child(group_code);
                        groupsReference.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshotGroups) {
                                final Group group = dataSnapshotGroups.getValue(Group.class);
                                String owner_phone = group.getOwner();

                                FirebaseDatabase.getInstance().getReference("users/" + owner_phone).addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                        User owner_details = (User) dataSnapshot.getValue(User.class);
                                        Invite invite = new Invite();
                                        invite.group_code = group_code;
                                        invite.group_name = group.getGroupName();
                                        invite.group_owner_name = owner_details.getUsername();
                                        invite.group_owner_photo = owner_details.getPhoto();
                                        invite.group_owner_phone = owner_details.getPhone();
                                        inviteArrayList.add(invite);
                                        invitesRecyclerAdapter.notifyDataSetChanged();
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError databaseError) {

                                    }
                                });

                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });


                    }

                }
                progressDialog.dismiss();


            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    class InvitesRecyclerAdapter extends RecyclerView.Adapter<ViewInvitesActivity.InvitesRecyclerAdapter.MyViewHolder> {

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
        public ViewInvitesActivity.InvitesRecyclerAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater inflater = LayoutInflater.from(parent.getContext());

            View viewthatcontainscardview = inflater.inflate(R.layout.recycler_item_invites, parent, false);

            CardView cardView = (CardView) (viewthatcontainscardview.findViewById(R.id.cardview_invites));
            return new ViewInvitesActivity.InvitesRecyclerAdapter.MyViewHolder(cardView);
        }

        @Override
        public void onBindViewHolder(ViewInvitesActivity.InvitesRecyclerAdapter.MyViewHolder holder, final int position) {

            CardView localcardview = holder.singlecardview;
            TextView tv_group_name;

            ImageView im_inviter_photo;
            TextView tv_group_owner_name;
            TextView tv_group_owner_phone_number;

            final Button bt_accept_invite;
            Button bt_reject_invite;

            tv_group_name = localcardview.findViewById(R.id.tv_group_name);
            im_inviter_photo = localcardview.findViewById(R.id.im_inviter_photo);
            tv_group_owner_name = localcardview.findViewById(R.id.tv_group_owner_name);
            tv_group_owner_phone_number = localcardview.findViewById(R.id.tv_group_owner_phone_number);
            bt_accept_invite = localcardview.findViewById(R.id.bt_accept_invite);
            bt_reject_invite = localcardview.findViewById(R.id.bt_reject_invite);
            final Invite invite = inviteArrayList.get(position);
            tv_group_name.setText("Group Name: " + invite.group_name);
            tv_group_owner_name.setText(invite.group_owner_name);
            tv_group_owner_phone_number.setText(invite.group_owner_phone);
            bt_accept_invite.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    acceptInvite(invite);
                }
            });
            bt_reject_invite.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    rejectInvite(invite);
                }
            });

            Glide.with(ViewInvitesActivity.this).load(invite.group_owner_photo).apply(RequestOptions.circleCropTransform()).into(im_inviter_photo);
        }

        @Override
        public int getItemCount() {
            return inviteArrayList.size();
        }
    }

    void acceptInvite(final Invite invite) {

        final DatabaseReference mainRef = FirebaseDatabase.getInstance()
                .getReference("users")
                .child(GlobalApp.phone_number)
                .child("invitations");
        mainRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshotInvitations) {

                ArrayList<String> al_invitations = (ArrayList<String>) dataSnapshotInvitations.getValue();
                Log.d(TAG, "Size" + al_invitations.size() + "");
                int index = -1;
                for (int i = 0; i < al_invitations.size(); i++) {
                    Log.d(TAG, al_invitations.get(i) + "  :   " + invite.group_code);
                    if (al_invitations.get(i).equals(invite.group_code)) {
                        Log.d(TAG, "Equal");
                        index = i;
                        break;
                    }
                }
                al_invitations.remove(index);


                Log.d("MYMSG",al_invitations.size()+"   0000 "+GlobalApp.phone_number);
                FirebaseDatabase.getInstance().getReference("users").child(GlobalApp.phone_number).child("invitations").setValue(al_invitations).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isComplete()){
                            addToGroup(invite);
                        }
                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(ViewInvitesActivity.this, "UnKNOWN ERROR OCCURRED", Toast.LENGTH_SHORT).show();
            }
        });

    }

    void addToGroup(final Invite invite) {
        final DatabaseReference user_ref = FirebaseDatabase.
                getInstance()
                .getReference("users")
                .child(GlobalApp.phone_number);

        user_ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                final User curr_user = dataSnapshot.getValue(User.class);
                ArrayList<String> groupCodes = new ArrayList<>();
                if (curr_user.groupcodes != null) {
                    groupCodes = curr_user.groupcodes;
                }
                groupCodes.add(invite.group_code);

                curr_user.groupcodes = groupCodes;

                ArrayList<String> groupNames = new ArrayList<>();
                if (curr_user.groupnames != null) {
                    groupNames = curr_user.groupnames;
                }
                groupNames.add(invite.group_name);

                curr_user.groupnames = groupNames;

                dataSnapshot.getRef().setValue(curr_user).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isComplete()){

                            FirebaseDatabase.getInstance().getReference("groups/"+invite.group_code+"/members").addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshotMembers) {
                                    ArrayList<String> members=new ArrayList<>();
                                    if(dataSnapshotMembers.getValue()!=null){
                                        members=(ArrayList<String>)dataSnapshotMembers.getValue();
                                    }
                                    members.add(GlobalApp.phone_number);
                                    dataSnapshotMembers.getRef().setValue(members).addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            Toast.makeText(ViewInvitesActivity.this, "INVITE ACCEPTED", Toast.LENGTH_SHORT).show();
                                        }
                                    });
                                    

                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {
                                    Toast.makeText(ViewInvitesActivity.this, "Task couldnt be completed", Toast.LENGTH_SHORT).show();
                                }
                            });
                        }else{
                            Toast.makeText(ViewInvitesActivity.this, "Task Couldnt be completed", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(ViewInvitesActivity.this, databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    void rejectInvite(final Invite invite) {
        final DatabaseReference mainRef = FirebaseDatabase.getInstance()
                .getReference("users")
                .child(GlobalApp.phone_number)
                .child("invitations");
        mainRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshotInvitations) {

                ArrayList<String> al_invitations = (ArrayList<String>) dataSnapshotInvitations.getValue();

                if (al_invitations == null) {
                    al_invitations = new ArrayList<>();
                }

                int index = -1;
                for (int i = 0; i < al_invitations.size(); i++) {
                    if (al_invitations.get(i).equals(invite.group_code)) {
                        index = i;
                        break;
                    }
                }
                al_invitations.remove(index);
                Log.d(TAG, "onDataChange: " + al_invitations.size());
                dataSnapshotInvitations.getRef().setValue(al_invitations).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        Toast.makeText(ViewInvitesActivity.this, "REJECTED", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(ViewInvitesActivity.this, "UnKNOWN ERROR OCCURRED", Toast.LENGTH_SHORT).show();
            }
        });

    }

}
