package com.mainpackage;

import android.app.ProgressDialog;
import android.content.Context;
import android.database.Cursor;
import android.graphics.LinearGradient;
import android.net.Uri;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
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

public class MakeGroupActivity extends AppCompatActivity {

    private String TAG = "MYMSG";
    private ArrayList<ContactItem> contactItemArrayList = new ArrayList<>();
    private ArrayList<ContactItem> contactItemFirebaseArrayList = new ArrayList<>();
    private ArrayList<ContactItem> contactSelectedArrayList = new ArrayList<>();
    private RecyclerView recycler_view_contacts;
    private AdapterRecyclerContacts adapterRecyclerContacts;
    private ProgressDialog progressDialog;
    private RecyclerView recycler_view_contacts_selected;
    private SelectedRecyclerAdapter selectedRecyclerAdapter;
    private TextView tv_selected_contacts_count;
    private FloatingActionButton fab_make_group;
    private EditText et_new_group_name;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_make_group);


        fab_make_group = findViewById(R.id.fab_make_group);
        recycler_view_contacts = findViewById(R.id.recycler_view_contacts);
        recycler_view_contacts_selected = findViewById(R.id.recycler_view_contacts_selected);
        tv_selected_contacts_count = findViewById(R.id.tv_selected_contacts_count);
        et_new_group_name = findViewById(R.id.et_new_group_name);
        //Making floating action Button Invisible
        fab_make_group.hide();

        //Toolbar
        Toolbar myToolbar = (Toolbar) findViewById(R.id.toolbar_make_group);
        setSupportActionBar(myToolbar);

        // Setting Home up enabled
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Please wait !!");
        progressDialog.setCancelable(false);
        fetch_firebase_contacts();

        // Starting actions


        // List of All
        adapterRecyclerContacts = new AdapterRecyclerContacts(contactItemFirebaseArrayList, this);
        recycler_view_contacts.setAdapter(adapterRecyclerContacts);
        LinearLayoutManager simpleverticallayout = new LinearLayoutManager(this);
        recycler_view_contacts.setLayoutManager(simpleverticallayout);

        // Selected Recycler
        selectedRecyclerAdapter = new SelectedRecyclerAdapter();
        recycler_view_contacts_selected.setAdapter(selectedRecyclerAdapter);
        GridLayoutManager layoutManager = new GridLayoutManager(this, 1);
        layoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        recycler_view_contacts_selected.setLayoutManager(layoutManager);


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

    void fetch_contacts() {
        Uri uri = ContactsContract.Contacts.CONTENT_URI;
        String colnames[] = {
                ContactsContract.Contacts.DISPLAY_NAME,
                ContactsContract.Contacts.HAS_PHONE_NUMBER,
                ContactsContract.Contacts._ID
        };
        Cursor cursor = getContentResolver().query(uri, colnames, null,
                null, null, null);
        StringBuffer tvdata = new StringBuffer();

        int i = 0;
        while (cursor.moveToNext()) {
            String dname = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
            int hascontact = cursor.getInt(cursor.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER));
            int id = cursor.getInt(cursor.getColumnIndex(ContactsContract.Contacts._ID));
            if (hascontact == 1) {
                Uri contentUri = ContactsContract.CommonDataKinds.Phone.CONTENT_URI;
                Cursor cnew = getContentResolver().query(contentUri, null,
                        ContactsContract.CommonDataKinds.Phone.CONTACT_ID + "=" + id,
                        null, null);
                String phonenumbers = "";
                if (cnew.moveToNext()) {
                    String phonenumber = cnew.getString(cnew.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));

                    phonenumbers = phonenumbers + phonenumber;
                }

                if (tvdata.toString().contains("--->" + phonenumbers + "\n\n")) {
                    continue;
                }
                tvdata.append(dname + "---> " + phonenumbers + "\n\n");
                contactItemArrayList.add(new ContactItem(dname, phonenumbers));
                i++;
                cnew.close();
            } else {
            }
        }
        cursor.close();
        retainAllUseful();

    }

    void fetch_firebase_contacts() {
        contactItemFirebaseArrayList.clear();
        progressDialog.show();
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference usersRef = database.getReference("users/");
        usersRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Iterable<DataSnapshot> children = dataSnapshot.getChildren();
                for (DataSnapshot snap : dataSnapshot.getChildren()) {
                    String phone = snap.child("phone").getValue(String.class);
                    String username = snap.child("username").getValue(String.class);
                    ContactItem contactItem = new ContactItem(username, phone);
                    contactItem.photo = snap.child("photo").getValue(String.class);
                    contactItemFirebaseArrayList.add(contactItem);
                }
                if (contactItemFirebaseArrayList.size() == dataSnapshot.getChildrenCount()) {
                    Log.d("Make Group","firebase over");
                    fetch_contacts();
                    progressDialog.dismiss();
                    GlobalApp.hideKeyboard(MakeGroupActivity.this);
                }


            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                progressDialog.dismiss();
                Toast.makeText(MakeGroupActivity.this, "Internet Error !", Toast.LENGTH_SHORT).show();
            }
        });
    }

    void retainAllUseful() {


        contactItemFirebaseArrayList.retainAll(contactItemArrayList);
        for (ContactItem contactItem : contactItemFirebaseArrayList) {
            Log.d(TAG, "retainAllUseful: " + contactItem.photo);
        }
        adapterRecyclerContacts.notifyDataSetChanged();
    }

    public void bt_event_make_group(View view) {
        progressDialog.show();

        final String group_name = et_new_group_name.getText().toString();
        if (group_name.equals("")) {
            Toast.makeText(this, "Invalid Group Name", Toast.LENGTH_SHORT).show();
            progressDialog.dismiss();
        } else {
            DatabaseReference groups_reference = FirebaseDatabase.getInstance().getReference("groups");
            final String push_key_groups = groups_reference.push().getKey();
            Group group =new Group();
            group.setGroupCode(push_key_groups);
            group.setGroupName(group_name);
            group.setOwner(GlobalApp.phone_number);

            ArrayList<String> al_members = new ArrayList<>();
            al_members.add(GlobalApp.phone_number);

            group.setMembers(al_members);

            groups_reference.child(push_key_groups).setValue(group).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    sendInvites(push_key_groups,group_name);
                }
            });

        }
    }


    void sendInvites(final String group_code, final String group_name){
        final FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
        for(ContactItem c: contactSelectedArrayList){
            final DatabaseReference reference = firebaseDatabase.getReference("users/"+c.phone_number+"/invitations");
            reference.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    ArrayList<String> invites=(ArrayList<String>) dataSnapshot.getValue();
                    if(invites==null){
                        invites=new ArrayList<>();
                    }
                    invites.add(group_code);
                    reference.setValue(invites);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Toast.makeText(MakeGroupActivity.this,"Could not send Invites , Unknown Error ",Toast.LENGTH_SHORT).show();
                }
            });


        }
            final DatabaseReference reference_group_codes = firebaseDatabase.getReference("users/" + GlobalApp.phone_number + "/groupcodes");
            reference_group_codes.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    ArrayList<String> all_codes = (ArrayList<String>) dataSnapshot.getValue();
                    if (all_codes == null) {
                        all_codes = new ArrayList<>();
                    }
                    all_codes.add(group_code);
                    reference_group_codes.setValue(all_codes);

                    ///-------------------------------------------------------------------------
                    final DatabaseReference reference_group_names = firebaseDatabase.getReference("users/" + GlobalApp.phone_number + "/groupnames");
                    reference_group_names.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            ArrayList<String> all_names = (ArrayList<String>) dataSnapshot.getValue();
                            if (all_names == null) {
                                all_names = new ArrayList<>();
                            }

                            all_names.add(group_name);
                            reference_group_names.setValue(all_names);
                            Toast.makeText(MakeGroupActivity.this, "Invites has been sent", Toast.LENGTH_SHORT).show();
                            progressDialog.dismiss();
                            MakeGroupActivity.this.finish();
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {
                            Toast.makeText(MakeGroupActivity.this, "Unknown Error", Toast.LENGTH_LONG).show();
                        }
                    });
                    ///-------------------------------------------------------------------------------


                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Toast.makeText(MakeGroupActivity.this, "Unknown Error", Toast.LENGTH_LONG).show();
                }
            });


    }


    class AdapterRecyclerContacts extends RecyclerView.Adapter<AdapterRecyclerContacts.MyViewHolder> {

        private ArrayList<ContactItem> adapterItemArrayList;
        private Context context;
        String TAG = "MYMSG";

        class MyViewHolder extends RecyclerView.ViewHolder {
            CardView singlecardview;

            public MyViewHolder(CardView itemView) {
                super(itemView);
                singlecardview = (itemView);
            }
        }

        AdapterRecyclerContacts(ArrayList<ContactItem> contactItemArrayList, Context context) {
            super();
            this.adapterItemArrayList = contactItemArrayList;
            this.context = context;
        }

        @Override
        public AdapterRecyclerContacts.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater inflater = LayoutInflater.from(parent.getContext());

            View viewthatcontainscardview = inflater.inflate(R.layout.recycler_item_contacts, parent, false);

            CardView cardView = (CardView) (viewthatcontainscardview.findViewById(R.id.cardview1));

            return new MyViewHolder(cardView);
        }

        @Override
        public void onBindViewHolder(AdapterRecyclerContacts.MyViewHolder holder, final int position) {
            Log.d(TAG, "onBindViewHolder: ");
            CardView localcardview = holder.singlecardview;


            final TextView tv_contacts_name, tv_contacts_phone;
            final CheckBox cb_contact;
            final ImageView im_contact_photo;

            tv_contacts_name = (TextView) (localcardview.findViewById(R.id.tv_contacts_name));
            tv_contacts_phone = (TextView) (localcardview.findViewById(R.id.tv_contacts_phone));
            cb_contact = (localcardview.findViewById(R.id.cb_contacts));
            im_contact_photo = (localcardview.findViewById(R.id.im_contact_item_photo));

            ContactItem i = adapterItemArrayList.get(position);
            localcardview.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (cb_contact.isChecked()) {
                        cb_contact.setChecked(false);
                    } else {
                        cb_contact.setChecked(true);
                    }
                }
            });

            cb_contact.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                    if (isChecked) {
                        contactSelectedArrayList.add(adapterItemArrayList.get(position));
                        refreshView();
                    } else {
                        contactSelectedArrayList.remove(adapterItemArrayList.get(position));
                        refreshView();
                    }
                }
            });
            tv_contacts_name.setText(i.name);
            tv_contacts_phone.setText(i.phone_number);
            Glide.with(context).load(i.photo).apply(RequestOptions.circleCropTransform()).into(im_contact_photo);
        }

        @Override
        public int getItemCount() {
            return adapterItemArrayList.size();
        }


    }


    void refreshView() {

        tv_selected_contacts_count.setText("Selected Contacts: \t" + contactSelectedArrayList.size());
        if (contactSelectedArrayList.size() >= 2) {
            fab_make_group.show();
        } else {
            fab_make_group.hide();
        }
        selectedRecyclerAdapter.notifyDataSetChanged();

    }


    class SelectedRecyclerAdapter extends RecyclerView.Adapter<SelectedRecyclerAdapter.MyViewHolder> {

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
        public SelectedRecyclerAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater inflater = LayoutInflater.from(parent.getContext());

            View viewthatcontainscardview = inflater.inflate(R.layout.recycler_item_selected_for_group, parent, false);

            CardView cardView = (CardView) (viewthatcontainscardview.findViewById(R.id.cardview_selected));

            // This will call Constructor of MyViewHolder, which will further copy its reference
            // to customview (instance variable name) to make its usable in all other methods of class
            Log.d("MYMESSAGE", "On CreateView Holder Done");
            return new MyViewHolder(cardView);
        }

        @Override
        public void onBindViewHolder(SelectedRecyclerAdapter.MyViewHolder holder, final int position) {

            CardView localcardview = holder.singlecardview;

            localcardview.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    // Toast.makeText(getApplicationContext(),position+" clicked",Toast.LENGTH_LONG).show();
                }
            });


            TextView tv_contacts_name_selected;
            final ImageView im_contact_item_photo_selected;

            tv_contacts_name_selected = (TextView) (localcardview.findViewById(R.id.tv_contacts_name_selected));
            im_contact_item_photo_selected = (ImageView) (localcardview.findViewById(R.id.im_contact_item_photo_selected));
            ContactItem i = contactSelectedArrayList.get(position);
            tv_contacts_name_selected.setText(i.name);
            Glide.with(MakeGroupActivity.this).load(i.photo).apply(RequestOptions.circleCropTransform()).into(im_contact_item_photo_selected);
        }

        @Override
        public int getItemCount() {
            return contactSelectedArrayList.size();
        }
    }


}
