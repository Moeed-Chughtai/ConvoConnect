package com.example.chatapp.Activities;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import com.example.chatapp.databinding.ActivityGroupChatMessagesBinding;

public class GroupChatMessagesActivity extends AppCompatActivity {

    ActivityGroupChatMessagesBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityGroupChatMessagesBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
    }
}
