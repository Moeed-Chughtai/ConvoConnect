package com.example.chatapp.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.chatapp.Adapters.ChatsAdapter;
import com.example.chatapp.Models.User;
import com.example.chatapp.R;
import com.example.chatapp.databinding.ActivityMainBinding;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

// TODO: Fix Status Bar (colour of 3 dots) and Themes
// TODO: Fix colour of action bar to green
// TODO: Send msg by pressing enter
public class MainActivity extends AppCompatActivity {

    ActivityMainBinding binding;
    FirebaseDatabase database;
    ArrayList<User> users;
    ChatsAdapter chatsAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        database = FirebaseDatabase.getInstance();
        users = new ArrayList<>();

        chatsAdapter = new ChatsAdapter(this, users);
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerView.setAdapter(chatsAdapter);

        // Check for real-time changes
        database.getReference()
                .child("user_friends")
                .child(FirebaseAuth.getInstance().getUid())
                .child("friends")
                .addValueEventListener(new ValueEventListener() {
                                           @Override
                                           public void onDataChange(@NonNull DataSnapshot snapshot) {
                                               users.clear();
                                               for (DataSnapshot snapshot1 : snapshot.getChildren()) {
                                                   User user = snapshot1.getValue(User.class);
                                                   users.add(user);
                                                   }
                                               chatsAdapter.notifyDataSetChanged();
                                               }

                                           @Override
                                           public void onCancelled(@NonNull DatabaseError error) {
                                           }
                                       });

        BottomNavigationView bottomNavigationView = binding.bottomNavigationView;
        bottomNavigationView.setSelectedItemId(R.id.chats);

        binding.bottomNavigationView.setOnItemSelectedListener(item -> {
            switch(item.getItemId()) {
                case R.id.chats:
                    return true;
                case R.id.find_users:
                    startActivity(new Intent(getApplicationContext(), UsersListActivity.class));
                    overridePendingTransition(0,0);
                    return true;
                case R.id.group_chats:
                    startActivity(new Intent(getApplicationContext(), GroupChatListActivity.class));
                    overridePendingTransition(0,0);
                    return true;
            }
            return false;
        });
    }

    // Add a menu on the action bar
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.action_bar, menu);
        return super.onCreateOptionsMenu(menu);
    }
}
