package com.example.chatapp.Activities;

import android.app.ProgressDialog;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.bumptech.glide.Glide;
import com.example.chatapp.Adapters.MessagesAdapter;
import com.example.chatapp.Models.Message;
import com.example.chatapp.R;
import com.example.chatapp.databinding.ActivityChatBinding;
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

public class ChatActivity extends AppCompatActivity {

    FirebaseDatabase database;
    FirebaseStorage storage;

    ActivityChatBinding binding;
    MessagesAdapter adapter;
    ArrayList<Message> messages;

    String senderRoom, receiverRoom;
    String senderUid;
    String receiverUid;

    ProgressDialog dialog;
    ActivityResultLauncher<String> selectMedia;
    Uri selectedMedia;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityChatBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        database = FirebaseDatabase.getInstance();
        storage = FirebaseStorage.getInstance();

        receiverUid = getIntent().getStringExtra("uid");
        senderUid = FirebaseAuth.getInstance().getUid();

        // Create unique identifiers between the sender and receiver
        senderRoom = senderUid + receiverUid;
        receiverRoom = receiverUid + senderUid;

        messages = new ArrayList<>();
        adapter = new MessagesAdapter(this, messages, senderRoom, receiverRoom);
        // Link recyclerView to MessagesAdapter
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerView.setAdapter(adapter);

        // Information from previous activity
        String name = getIntent().getStringExtra("name");
        String profileImage = getIntent().getStringExtra("image");

        binding.name.setText(name);
        Glide.with(ChatActivity.this)
                .load(profileImage)
                .placeholder(R.drawable.avatar)
                .into(binding.profile);

        // Set custom action bar
        setSupportActionBar(binding.toolbar);
        // Remove app title
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        // Back arrow returns to chat menu
        binding.imageView2.setOnClickListener(view -> finish());

        dialog = new ProgressDialog(this);
        dialog.setMessage("Uploading media...");
        dialog.setCancelable(false);

        // Check if receiver has a presence
        database.getReference()
                .child("presence")
                .child(receiverUid)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            // Presence of message receiver
                            String status = snapshot.getValue(String.class);
                            // Set status under name but make invisible if offline
                            if (!status.isEmpty()) {
                                if (status.equals("Offline")) {
                                    binding.status.setVisibility(View.GONE);
                                } else {
                                    binding.status.setText(status);
                                    binding.status.setVisibility(View.VISIBLE);
                                }
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                    }
                });

        // Listener on senders section to wait for new messages
        database.getReference()
                .child("chats")
                .child(senderRoom)
                .child("messages")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        messages.clear();
                        // Add all messages to list
                        for(DataSnapshot snapshot1 : snapshot.getChildren()) {
                            Message message = snapshot1.getValue(Message.class);
                            messages.add(message);
                        }
                        // Update adapter with list
                        adapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                    }
                });

        // When a user clicks the send button
        binding.send.setOnClickListener(v -> {
            if (!binding.messageBox.getText().toString().isEmpty()) {
                // Retrieve text entered by user
                String messageTxt = binding.messageBox.getText().toString();
                Date date = new Date();
                // Create message object
                Message message = new Message(messageTxt, senderUid, date.getTime());
                binding.messageBox.setText("");

                // Store latest message and time of message to display in chat menu
                HashMap<String, Object> lastMsgObj = new HashMap<>();
                lastMsgObj.put("lastMsg", message.getMessage());
                lastMsgObj.put("lastMsgTime", date.getTime());

                // Store message in the senders section in the database
                database.getReference()
                        .child("chats")
                        .child(senderRoom)
                        .updateChildren(lastMsgObj);
                database.getReference().child("chats")
                        .child(receiverRoom)
                        .updateChildren(lastMsgObj);

                // Store message receivers section in the database
                database.getReference()
                        .child("chats")
                        .child(senderRoom)
                        .child("messages")
                        .child(String.valueOf(date.getTime()))
                        .setValue(message).addOnSuccessListener(aVoid -> database.getReference()
                                .child("chats")
                                .child(receiverRoom)
                                .child("messages")
                        .child(String.valueOf(date.getTime()))
                                .setValue(message).addOnSuccessListener(aVoid1 -> {
                                }));
            }
        });

        // User clicks media button
        selectMedia = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                result -> {
                    // Media selected by user
                    selectedMedia = result;

                    if (selectedMedia != null) {
                        // Time used for unique node to store media
                        Calendar calendar = Calendar.getInstance();
                        // Upload media to storage
                        StorageReference reference = storage.getReference()
                                .child("Chats")
                                .child(calendar.getTimeInMillis() + "");
                        // Popup to tell user media is uploading
                        dialog.show();
                        reference.putFile(selectedMedia).addOnCompleteListener(task -> {
                            dialog.dismiss();
                            if (task.isSuccessful()) {
                                reference.getDownloadUrl().addOnSuccessListener(uri -> {
                                    // Convert the path of the media in storage to a string
                                    String filePath = uri.toString();

                                    // Create message object
                                    String messageTxt = binding.messageBox.getText().toString();
                                    Date date = new Date();
                                    Message message = new Message(messageTxt, senderUid, date.getTime());
                                    message.setMessage("media");
                                    message.setMediaUrl(filePath);

                                    // Store latest message and time of message to display in chat menu
                                    HashMap<String, Object> lastMsgObj = new HashMap<>();
                                    lastMsgObj.put("lastMsg", message.getMessage());
                                    lastMsgObj.put("lastMsgTime", date.getTime());
                                    database.getReference().child("chats")
                                            .child(senderRoom)
                                            .updateChildren(lastMsgObj);
                                    database.getReference().child("chats")
                                            .child(receiverRoom)
                                            .updateChildren(lastMsgObj);

                                    // Store message in the senders and receivers section in the database
                                    database.getReference().child("chats")
                                            .child(senderRoom)
                                            .child("messages")
                                            .child(String.valueOf(date.getTime()))
                                            .setValue(message).addOnSuccessListener(aVoid -> database.getReference()
                                                    .child("chats")
                                                    .child(receiverRoom)
                                                    .child("messages")
                                            .child(String.valueOf(date.getTime()))
                                                    .setValue(message).addOnSuccessListener(aVoid1 -> {
                                                    }));
                                });
                            }
                        });
                    }
                }
        );

        // Open media on the device which displays all pictures and videos
        binding.attachment.setOnClickListener(v -> selectMedia.launch("*/*"));

        final Handler handler = new Handler();
        // Look for change in message box to change status to typing
        binding.messageBox.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            // Called if anything typed into message box
            @Override
            public void afterTextChanged(Editable editable) {
                database.getReference()
                        .child("presence")
                        .child(senderUid)
                        .setValue("typing...");
                handler.removeCallbacksAndMessages(null);
                // Change back to online if user doesn't type for 2 seconds
                handler.postDelayed(userStoppedTyping, 2000);
            }
            final Runnable userStoppedTyping = new Runnable() {
                @Override
                public void run() {
                    database.getReference().child("presence").child(senderUid).setValue("Online");
                }
            };
        });

        // If remove button clicked
        binding.remove.setOnClickListener(view -> {
            // Remove node of friend under list of friends of user
            database.getReference()
                    .child("user_friends")
                    .child(FirebaseAuth.getInstance().getUid())
                    .child("friends")
                    .child(receiverUid)
                    .removeValue();
            finish();
            Toast.makeText(ChatActivity.this, "Friend removed", Toast.LENGTH_SHORT).show();
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        String currentUser = FirebaseAuth.getInstance().getUid();
        // Current user presence set to online in database
        database.getReference().child("presence").child(currentUser).setValue("Online");
    }

    @Override
    protected void onPause() {
        super.onPause();
        String currentUser = FirebaseAuth.getInstance().getUid();
        // Current user presence set to offline in database
        database.getReference().child("presence").child(currentUser).setValue("Offline");
    }
}
