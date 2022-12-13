package com.example.chatapp.Activities;

import android.app.ProgressDialog;
import android.net.Uri;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.chatapp.Adapters.GroupChatMessagesAdapter;
import com.example.chatapp.Models.Message;
import com.example.chatapp.databinding.ActivityGroupChatMessagesBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

public class GroupChatMessagesActivity extends AppCompatActivity {

    FirebaseDatabase database;
    FirebaseStorage storage;

    ActivityGroupChatMessagesBinding binding;
    GroupChatMessagesAdapter adapter;
    ArrayList<Message> messages;

    ProgressDialog dialog;
    ActivityResultLauncher<String> selectMedia;
    Uri selectedMedia;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityGroupChatMessagesBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        database = FirebaseDatabase.getInstance();
        storage = FirebaseStorage.getInstance();

        String name = getIntent().getStringExtra("name");

        messages = new ArrayList<>();
        adapter = new GroupChatMessagesAdapter(this, messages, name);
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerView.setAdapter(adapter);

        binding.name.setText(name);

        setSupportActionBar(binding.toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        binding.imageView2.setOnClickListener(view -> finish());

        dialog = new ProgressDialog(this);
        dialog.setMessage("Uploading media...");
        dialog.setCancelable(false);

        // Event listener at messages location
        database.getReference()
                .child("group_chat_messages")
                .child(name)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        messages.clear();
                        for(DataSnapshot snapshot1 : snapshot.getChildren()) {
                            Message message = snapshot1.getValue(Message.class);
                            message.setMessageId(snapshot1.getKey());
                            // Add all messages
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
            if (!binding.messageBox.getText().toString().isEmpty()) {
                String messageTxt = binding.messageBox.getText().toString();
                Date date = new Date();
                Message message = new Message(messageTxt, FirebaseAuth.getInstance().getUid(), date.getTime());
                binding.messageBox.setText("");

                // Dictionary with latest message
                HashMap<String, Object> lastMsgObj = new HashMap<>();
                lastMsgObj.put("lastMsg", message.getMessage());
                lastMsgObj.put("lastMsgTime", date.getTime());
                // Update latest message in database
                database.getReference()
                        .child("group_chat_latest_message")
                        .child(name)
                        .updateChildren(lastMsgObj);

                database.getReference()
                        .child("group_chat_messages")
                        .child(name)
                        .child(String.valueOf(date.getTime()))
                        .setValue(message)
                        .addOnSuccessListener(unused -> {
                        });
            }
        });

        selectMedia = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                result -> {
                    selectedMedia = result;

                    if (selectedMedia != null) {
                        Calendar calendar = Calendar.getInstance();
                        StorageReference reference = storage.getReference().child("Group_Chats").child(calendar.getTimeInMillis() + "");
                        dialog.show();
                        reference.putFile(selectedMedia).addOnCompleteListener(task -> {
                            dialog.dismiss();
                            if (task.isSuccessful()) {
                                reference.getDownloadUrl().addOnSuccessListener(uri -> {
                                    String filePath = uri.toString();

                                    // Store media in database
                                    String messageTxt = binding.messageBox.getText().toString();
                                    Date date = new Date();
                                    Message message = new Message(messageTxt, FirebaseAuth.getInstance().getUid(), date.getTime());
                                    message.setMessage("media");
                                    message.setMediaUrl(filePath);

                                    HashMap<String, Object> lastMsgObj = new HashMap<>();
                                    lastMsgObj.put("lastMsg", message.getMessage());
                                    lastMsgObj.put("lastMsgTime", date.getTime());
                                    database.getReference()
                                            .child("group_chat_latest_message")
                                            .child(name)
                                            .updateChildren(lastMsgObj);

                                    database.getReference()
                                            .child("group_chat_messages")
                                            .child(name)
                                            .child(String.valueOf(date.getTime()))
                                            .setValue(message)
                                            .addOnSuccessListener(unused -> {
                                            });
                                });
                            }
                        });
                    }
                }
        );

        binding.attachment.setOnClickListener(v -> selectMedia.launch("*/*"));
    }
}
