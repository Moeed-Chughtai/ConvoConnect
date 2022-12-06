package com.example.chatapp.Activities;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.chatapp.Adapters.MessagesAdapter;
import com.example.chatapp.Models.Message;
import com.example.chatapp.databinding.ActivityChatBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

public class ChatActivity extends AppCompatActivity {

    ActivityChatBinding binding;
    MessagesAdapter adapter;
    ArrayList<Message> messages;
    String senderRoom, receiverRoom;
    FirebaseDatabase database;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityChatBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        database = FirebaseDatabase.getInstance();
        messages = new ArrayList<>();
        adapter = new MessagesAdapter(this, messages);
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerView.setAdapter(adapter);

        // Information from previous activity
        String name = getIntent().getStringExtra("name");
        String receiverUid = getIntent().getStringExtra("uid");
        String senderUid = FirebaseAuth.getInstance().getUid();

        // Create unique identifiers between the senders and receivers
        senderRoom = senderUid + receiverUid;
        receiverRoom = receiverUid + senderUid;

        // Listener on senders msg location
        database.getReference().child("chats")
                .child(senderRoom)
                .child("messages")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        messages.clear();
                        // Add all msgs including new ones
                        for(DataSnapshot snapshot1 : snapshot.getChildren()) {
                            Message message = snapshot1.getValue(Message.class);
                            messages.add(message);
                        }
                        adapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                    }
                });

        // When a user sends a message
        binding.send.setOnClickListener(v -> {
            String messageTxt = binding.messageBox.getText().toString();
            Date date = new Date();
            Message message = new Message(messageTxt, senderUid, date.getTime());
            // clears the message box
            binding.messageBox.setText("");

            // Store last message and time of message to display on MainActivity
            HashMap<String, Object> lastMsgObj = new HashMap<>();
            lastMsgObj.put("lastMsg", message.getMessage());
            lastMsgObj.put("lastMsgTime", date.getTime());
            database.getReference().child("chats")
                    .child(senderRoom)
                    .updateChildren(lastMsgObj);
            database.getReference().child("chats")
                    .child(receiverRoom)
                    .updateChildren(lastMsgObj);

            // Storing message in the sender and receivers sections in the database
            database.getReference().child("chats")
                    .child(senderRoom)
                    .child("messages")
                    .push()
                    .setValue(message).addOnSuccessListener(aVoid -> database.getReference()
                            .child("chats")
                            .child(receiverRoom)
                            .child("messages")
                            .push()
                            .setValue(message).addOnSuccessListener(aVoid1 -> {
                            }));
        });

        // Users name displayed in action bar
        getSupportActionBar().setTitle(name);
        // Back arrow
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    // Return to mainActivity when back arrow pressed
    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return super.onSupportNavigateUp();
    }
}
