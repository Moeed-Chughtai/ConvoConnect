package com.example.chatapp.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.chatapp.Adapters.BuilderAdapter;
import com.example.chatapp.Adapters.GroupChatListAdapter;
import com.example.chatapp.Models.GroupChat;
import com.example.chatapp.Models.User;
import com.example.chatapp.R;
import com.example.chatapp.databinding.ActivityGroupChatListBinding;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class GroupChatListActivity extends AppCompatActivity {

    ActivityGroupChatListBinding binding;
    FirebaseDatabase database;

    BuilderAdapter builderAdapter;
    ArrayList<User> users;

    GroupChatListAdapter groupChatListAdapter;
    ArrayList<GroupChat> chats;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityGroupChatListBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        database = FirebaseDatabase.getInstance();

        chats = new ArrayList<>();
        groupChatListAdapter = new GroupChatListAdapter(this, chats);
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerView.setAdapter(groupChatListAdapter);

        database.getReference()
                .child("group_chats")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        chats.clear();
                        for (DataSnapshot snapshot1 : snapshot.getChildren()) {
                            GroupChat groupChat = snapshot1.getValue(GroupChat.class);
                            database.getReference()
                                    .child("group_chat_users")
                                    .child(groupChat.getName())
                                    .addValueEventListener(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                                            for (DataSnapshot snapshot1 : snapshot.getChildren()) {
                                                User user = snapshot1.getValue(User.class);
                                                if (user.getUid().equals(FirebaseAuth.getInstance().getUid())) {
                                                    Toast.makeText(GroupChatListActivity.this, "Yes", Toast.LENGTH_SHORT).show();
                                                    chats.add(groupChat);
                                                    groupChatListAdapter.notifyDataSetChanged();
                                                }
                                            }
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError error) {
                                        }
                                    });
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                    }
                });


        BottomNavigationView bottomNavigationView = binding.bottomNavigationView;
        bottomNavigationView.setSelectedItemId(R.id.group_chats);

        binding.bottomNavigationView.setOnItemSelectedListener(item -> {
            switch(item.getItemId()) {
                case R.id.group_chats:
                    return true;
                case R.id.chats:
                    startActivity(new Intent(getApplicationContext(),MainActivity.class));
                    overridePendingTransition(0,0);
                    return true;
                case R.id.friends:
                    startActivity(new Intent(getApplicationContext(),UsersListActivity.class));
                    overridePendingTransition(0,0);
                    return true;
            }
            return false;
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.action_bar2, menu);
        return true;
    }

    // Menu items
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        // If group chat 'ImageView' clicked
        if (id == R.id.create_gc) {
            // Builder function
            createNewDialog();
        }
        return super.onOptionsItemSelected(item);
    }

    public void createNewDialog() {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        // Link the gc_popup to dialogBuilder
        final View gc_popup = getLayoutInflater().inflate(R.layout.gc_popup, null);
        dialogBuilder.setView(gc_popup);

        // Link the different views
        EditText name = gc_popup.findViewById(R.id.name);
        Button continueBtn = gc_popup.findViewById(R.id.continueBtn);
        RecyclerView recyclerView = gc_popup.findViewById(R.id.recyclerView);

        // Link the recyclerView to the adapter
        users = new ArrayList<>();
        builderAdapter = new BuilderAdapter(dialogBuilder.getContext(), users);
        recyclerView.setLayoutManager(new LinearLayoutManager(dialogBuilder.getContext()));
        recyclerView.setAdapter(builderAdapter);

        // Check for changes in the database {prototype version}
        database.getReference()
                .child("users")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        users.clear();
                        for (DataSnapshot snapshot1 : snapshot.getChildren()) {
                            User user = snapshot1.getValue(User.class);
                            if (!user.getUid().equals(FirebaseAuth.getInstance().getUid())) {
                                users.add(user);
                            }
                        }
                        builderAdapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                    }
                });

        AlertDialog dialog = dialogBuilder.create();
        dialog.show();

        continueBtn.setOnClickListener(view -> {
            String groupChatName = name.getText().toString();
            if(groupChatName.isEmpty()) {
                name.setError("Please Enter a Name");
                return;
            }
            GroupChat groupChat = new GroupChat(groupChatName);
            database.getReference()
                    .child("temp")
                    .child(FirebaseAuth.getInstance().getUid())
                    .addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            for (DataSnapshot snapshot1 : snapshot.getChildren()) {
                                User user = snapshot1.getValue(User.class);
                                database.getReference()
                                        .child("group_chats")
                                        .child(groupChatName)
                                        .setValue(groupChat)
                                        .addOnSuccessListener(unused -> {
                                        });
                                database.getReference()
                                        .child("temp")
                                        .child(FirebaseAuth.getInstance().getUid())
                                        .child(user.getUid())
                                        .removeValue();
                                database.getReference()
                                        .child("group_chat_users")
                                        .child(groupChatName)
                                        .child(user.getUid())
                                        .setValue(user)
                                        .addOnSuccessListener(unused -> {
                                        });
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                        }
                    });
            finish();
        });
    }
}
