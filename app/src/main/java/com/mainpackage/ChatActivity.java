package com.mainpackage;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.Calendar;

public class ChatActivity extends AppCompatActivity {

    RecyclerView rv_chat;
    Button bt_send;
    EditText et_message;
    ArrayList<Message> alChat;
    String id;
    ChatAdapter chatAdapter;
    TextView tv_chat_name;
    String name;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        rv_chat = findViewById(R.id.rv_chat);
        bt_send = findViewById(R.id.bt_send_msg);
        et_message = findViewById(R.id.et_message);
        tv_chat_name = findViewById(R.id.tv_chat_name);
        Intent intent = getIntent();
        id = intent.getStringExtra("meeting");
        name = intent.getStringExtra("name");
        tv_chat_name.setText(name);
        alChat = new ArrayList<>();
        chatAdapter = new ChatAdapter();
        rv_chat.setLayoutManager(new LinearLayoutManager(this));
        rv_chat.setAdapter(chatAdapter);
        FirebaseDatabase.getInstance().getReference("chat").child(id).addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                alChat.add(dataSnapshot.getValue(Message.class));
                chatAdapter.notifyItemInserted(alChat.size());
                rv_chat.smoothScrollToPosition(alChat.size());
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

    public void send(View view) {
        if (et_message.getText().toString().trim().isEmpty()) {
            return;
        }
        String date = Calendar.getInstance().getTime().toString();
        FirebaseDatabase.getInstance().getReference("chat").child(id).push().setValue(new Message(GlobalApp.phone_number, et_message.getText().toString(), date));
        et_message.setText("");
        rv_chat.smoothScrollToPosition(alChat.size());
    }

    class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.ChatViewHolder> {
        @NonNull
        @Override
        public ChatViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
            return new ChatViewHolder(LayoutInflater.from(ChatActivity.this).inflate(R.layout.single_row_chat, viewGroup, false));
        }

        @Override
        public void onBindViewHolder(@NonNull ChatViewHolder chatViewHolder, int i) {
            if (alChat.get(i).getFrom().equalsIgnoreCase(GlobalApp.phone_number)) {
                chatViewHolder.tv_chat.setText("Me" + ":" + alChat.get(i).getMessage() + "\n" + alChat.get(i).getDate().substring(0, 10));
                chatViewHolder.tv_chat.setBackgroundResource(R.drawable.marker_mask2);
                chatViewHolder.ll_chat.setHorizontalGravity(Gravity.RIGHT);
                chatViewHolder.ll_chat.setGravity(Gravity.RIGHT);
            } else {
                chatViewHolder.tv_chat.setText(GlobalApp.name_no_mapping.get(alChat.get(i).getFrom()) + ":" + alChat.get(i).getMessage() + "\n" + alChat.get(i).getDate().substring(0, 20));
                chatViewHolder.tv_chat.setBackgroundResource(R.drawable.marker_mask);
                chatViewHolder.ll_chat.setGravity(Gravity.LEFT);
                chatViewHolder.ll_chat.setHorizontalGravity(Gravity.LEFT);
            }


        }

        @Override
        public int getItemCount() {
            return alChat.size();
        }

        public class ChatViewHolder extends RecyclerView.ViewHolder {

            LinearLayout ll_chat;
            TextView tv_chat;

            public ChatViewHolder(@NonNull View itemView) {
                super(itemView);
                tv_chat = itemView.findViewById(R.id.chat_content);
                ll_chat = itemView.findViewById(R.id.ll_chat);
            }
        }

    }
}
