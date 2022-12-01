package com.example.chatapp.Activities;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;

import com.example.chatapp.Models.User;
import com.example.chatapp.databinding.ActivitySetupProfileBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

public class SetupProfileActivity extends AppCompatActivity {

    FirebaseAuth auth;
    FirebaseDatabase database;
    FirebaseStorage storage;

    ActivitySetupProfileBinding binding;
    ActivityResultLauncher<String> selectPicture;

    Uri selectedImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySetupProfileBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        auth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();
        storage = FirebaseStorage.getInstance();

        selectPicture = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                new ActivityResultCallback<Uri>() {
                    @Override
                    // Method called when user clicks profile picture avatar
                    public void onActivityResult(Uri result) {
                        // Set and display the new profile picture
                        binding.profilePicture.setImageURI(result);
                        selectedImage = result;
                    }
                }
        );

        binding.profilePicture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectPicture.launch("image/*");
            }
        });

        binding.continueBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
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
                    reference.putFile(selectedImage).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                            if(task.isSuccessful()) {
                                // Download Url of image to store as user attribute
                                reference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                    @Override
                                    public void onSuccess(Uri uri) {
                                        // Information to store as the attributes of user class
                                        String imageUrl = uri.toString();
                                        String uid = auth.getUid();
                                        String phone = auth.getCurrentUser().getPhoneNumber();
                                        String name = binding.nameInput.getText().toString();
                                        // Creates a user object
                                        User user = new User(uid, name, phone, imageUrl);

                                        // Create a 'users' section and then a further unique section for user in the database to store user data
                                        database.getReference()
                                                .child("users")
                                                .child(uid)
                                                .setValue(user)
                                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                    @Override
                                                    public void onSuccess(Void aVoid) {
                                                        // Once data is stored, it launches main activity
                                                        Intent intent = new Intent(SetupProfileActivity.this, MainActivity.class);
                                                        startActivity(intent);
                                                        finish();
                                                    }
                                                });
                                    }
                                });
                            }
                        }
                    });

                // If user does not select a profile picture
                }else {
                    String uid = auth.getUid();
                    String phone = auth.getCurrentUser().getPhoneNumber();
                    User user = new User(uid, name, phone, "No Profile Picture");

                    // Create a 'users' section and then a further unique section for user in the database to store user data
                    database.getReference()
                            .child("users")
                            .child(uid)
                            .setValue(user)
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    // Once profile is complete and data is stored, it redirects to main activity
                                    Intent intent = new Intent(SetupProfileActivity.this, MainActivity.class);
                                    startActivity(intent);
                                    finish();
                                }
                            });
                }
            }
        });
    }
}
