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

        messages = new ArrayList<>();
        adapter = new GroupChatMessagesAdapter(this, messages);
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerView.setAdapter(adapter);

        String name = getIntent().getStringExtra("name");
        binding.name.setText(name);

        setSupportActionBar(binding.toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        binding.imageView2.setOnClickListener(view -> finish());

        dialog = new ProgressDialog(this);
        dialog.setMessage("Uploading media...");
        dialog.setCancelable(false);

        database.getReference().child("group_chat_messages")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        messages.clear();
                        for(DataSnapshot snapshot1 : snapshot.getChildren()) {
                            Message message = snapshot1.getValue(Message.class);
                            message.setMessageId(snapshot1.getKey());
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

                /*HashMap<String, Object> lastMsgObj = new HashMap<>();
                lastMsgObj.put("lastMsg", message.getMessage());
                lastMsgObj.put("lastMsgTime", date.getTime());
                database.getReference()
                        .child("group_chat_messages")
                        .updateChildren(lastMsgObj);*/

                database.getReference()
                        .child("group_chat_messages")
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
                        // Time used for unique node
                        Calendar calendar = Calendar.getInstance();
                        // Upload media to storage
                        StorageReference reference = storage.getReference().child("Group_Chats").child(calendar.getTimeInMillis() + "");
                        // Give user information
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

                                    /*HashMap<String, Object> lastMsgObj = new HashMap<>();
                                    lastMsgObj.put("lastMsg", message.getMessage());
                                    lastMsgObj.put("lastMsgTime", date.getTime());
                                    database.getReference()
                                            .child("group_chat_messages")
                                            .updateChildren(lastMsgObj);*/

                                    database.getReference()
                                            .child("group_chat_messages")
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

        // Open file - includes all types (images and videos)
        binding.attachment.setOnClickListener(v -> selectMedia.launch("*/*"));
    }
}
