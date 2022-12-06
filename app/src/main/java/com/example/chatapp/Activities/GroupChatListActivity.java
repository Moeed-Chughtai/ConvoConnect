package com.example.chatapp.Activities;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.example.chatapp.R;
import com.example.chatapp.databinding.ActivityGroupListChatBinding;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class GroupChatListActivity extends AppCompatActivity {

    ActivityGroupListChatBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityGroupListChatBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        BottomNavigationView bottomNavigationView = binding.bottomNavigationView;
        bottomNavigationView.setSelectedItemId(R.id.friends);

        binding.bottomNavigationView.setOnItemSelectedListener(item -> {
            switch(item.getItemId()) {
                case R.id.friends:
                    return true;
                case R.id.chats:
                    startActivity(new Intent(getApplicationContext(),MainActivity.class));
                    overridePendingTransition(0,0);
                    return true;
                case R.id.group_chats:
                    startActivity(new Intent(getApplicationContext(),UsersListActivity.class));
                    overridePendingTransition(0,0);
                    return true;
            }
            return false;
        });

    }
}
