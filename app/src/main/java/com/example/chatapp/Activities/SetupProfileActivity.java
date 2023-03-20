package com.example.chatapp.Activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.example.chatapp.Models.User;
import com.example.chatapp.databinding.ActivitySetupProfileBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class SetupProfileActivity extends AppCompatActivity {

    FirebaseDatabase database;
    FirebaseStorage storage;
    FirebaseAuth auth;

    ActivitySetupProfileBinding binding;

    ActivityResultLauncher<String> selectPicture;
    Uri selectedImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySetupProfileBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        database = FirebaseDatabase.getInstance();
        storage = FirebaseStorage.getInstance();
        auth = FirebaseAuth.getInstance();

        selectPicture = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                result -> {
                    // Display selected picture
                    binding.profilePicture.setImageURI(result);
                    selectedImage = result;
                }
        );

        // User clicks profile picture avatar
        binding.profilePicture.setOnClickListener(v -> selectPicture.launch("image/*"));

        binding.continueBtn.setOnClickListener(v -> {
            String name = binding.nameInput.getText().toString();
            if(name.isEmpty()) {
                binding.nameInput.setError("Please Enter a Name");
                return;
            }

            binding.progressBar.setVisibility(View.VISIBLE);
            binding.continueBtn.setVisibility(View.INVISIBLE);

            if (selectedImage != null) {
                // Create a 'Profiles' section in storage and stores profile picture
                StorageReference reference = storage.getReference().child("Profiles").child(auth.getUid());
                reference.putFile(selectedImage).addOnCompleteListener(task -> {
                    if(task.isSuccessful()) {
                        // Url of image stored as attribute for user object
                        reference.getDownloadUrl().addOnSuccessListener(uri -> {
                            // Information to store as the attributes of user class
                            String imageUrl = uri.toString();
                            String uid = auth.getUid();
                            String phone = auth.getCurrentUser().getPhoneNumber();
                            String name1 = binding.nameInput.getText().toString();
                            String status = binding.status.getText().toString();

                            // User object
                            User user = new User(uid, name1, status, phone, imageUrl);

                            // Create a 'users' section and then a further unique section for user and stores user object
                            database.getReference()
                                    .child("users")
                                    .child(uid)
                                    .setValue(user)
                                    .addOnSuccessListener(aVoid -> {
                                        // Once data is stored, it launches main activity
                                        Intent intent = new Intent(SetupProfileActivity.this, MainActivity.class);
                                        startActivity(intent);
                                        finish();
                                    });
                        });
                    }
                });

            // If user does not select a profile picture
            }else {
                String uid = auth.getUid();
                String phone = auth.getCurrentUser().getPhoneNumber();
                String status = binding.status.getText().toString();

                // User object
                User user = new User(uid, name, status, phone, "No Profile Picture");

                // Create a 'users' section and then a further unique section for user and stores user object
                database.getReference()
                        .child("users")
                        .child(uid)
                        .setValue(user)
                        .addOnSuccessListener(aVoid -> {
                            // Requests to launch MainActivity
                            Intent intent = new Intent(SetupProfileActivity.this, MainActivity.class);
                            startActivity(intent);
                            finish();
                        });
            }
        });
    }
}
