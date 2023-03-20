package com.example.chatapp.Activities;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import com.example.chatapp.Models.User;
import com.example.chatapp.databinding.ActivitySettingsBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class SettingsActivity extends AppCompatActivity {

    FirebaseDatabase database;

    ActivitySettingsBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySettingsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        database = FirebaseDatabase.getInstance();

        setSupportActionBar(binding.toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        binding.arrow.setOnClickListener(view -> finish());

        // Change name
        binding.nameChangeBtn.setOnClickListener(view -> {
            String nameChange = binding.nameChange.getText().toString();
            if(nameChange.isEmpty()) {
                binding.nameChangeBtn.setError("Please Enter a Name");
                return;
            }
            // Find current user
            database.getReference()
                    .child("users")
                    .addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            for (DataSnapshot snapshot1 : snapshot.getChildren()) {
                                User user = snapshot1.getValue(User.class);
                                // If the user id equals the current user id
                                if (user.getUid().equals(FirebaseAuth.getInstance().getUid())) {
                                    // Update user object with new name
                                    user.setName(nameChange);
                                    // Update database with updated user object
                                    database.getReference()
                                            .child("users")
                                            .child(FirebaseAuth.getInstance().getUid())
                                            .setValue(user);
                                }
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                        }
                    });
        });

        /// Change status
        binding.statusChangeBtn.setOnClickListener(view -> {
            String statusChange = binding.statusChange.getText().toString();
            if(statusChange.isEmpty()) {
                binding.statusChangeBtn.setError("Please Enter a Status");
                return;
            }
            // Find current user
            database.getReference()
                    .child("users")
                    .addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            for (DataSnapshot snapshot1 : snapshot.getChildren()) {
                                User user = snapshot1.getValue(User.class);
                                if (user.getUid().equals(FirebaseAuth.getInstance().getUid())) {
                                    // Update user object with new status
                                    user.setStatus(statusChange);
                                    // Update database with updated user object
                                    database.getReference()
                                            .child("users")
                                            .child(FirebaseAuth.getInstance().getUid())
                                            .setValue(user);
                                }
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                        }
                    });
        });

        // Switch between light and dark theme files
        binding.darkMode.setOnCheckedChangeListener((compoundButton, isChecked) -> {
            if (isChecked) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
            }
        });
    }

    // Stop flickers
    @Override
    public void recreate() {
        finish();
        // Smooth transitions
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);

        startActivity(getIntent());
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }
}
