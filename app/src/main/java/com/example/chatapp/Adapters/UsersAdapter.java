package com.example.chatapp.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.chatapp.Models.User;
import com.example.chatapp.R;
import com.example.chatapp.databinding.UsersListBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;

public class UsersAdapter extends RecyclerView.Adapter<UsersAdapter.UsersViewHolder> {

    Context context;
    ArrayList<User> users;
    DatabaseReference database;

    public UsersAdapter(Context context, ArrayList<User> users) {
        this.context = context;
        this.users = users;
    }

    @NonNull
    @Override
    public UsersViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // links the users_list.xml to the UsersViewHolder
        View view = LayoutInflater.from(context).inflate(R.layout.users_list, parent, false);
        return new UsersViewHolder(view);
    }

    // Sets the actual data
    @Override
    public void onBindViewHolder(@NonNull UsersViewHolder holder, int position) {
        User user = users.get(position);

        // Replaces sample data with actual data using binding
        holder.binding.username.setText(user.getName());
        holder.binding.status.setText(user.getStatus());
        Glide.with(context).load(user.getProfilePicture())
                .placeholder(R.drawable.avatar)
                .into(holder.binding.profile);

        holder.binding.addUser.setOnClickListener(view -> {
            String uid = user.getUid();
            // Database reference
            database = FirebaseDatabase.getInstance().getReference();
            database.child("user_friends")
                    .child(FirebaseAuth.getInstance().getUid())
                    .child("friends")
                    .child(uid)
                    .setValue(user)
                    .addOnSuccessListener(unused -> {
                        // Notify user if successful
                        Toast.makeText(context, "User added", Toast.LENGTH_LONG).show();
                    });
            });
        }

    @Override
    public int getItemCount() {
        return users.size();
    }


    public class UsersViewHolder extends RecyclerView.ViewHolder {

        UsersListBinding binding;

        public UsersViewHolder(@NonNull View itemView) {
            super(itemView);
            binding = UsersListBinding.bind(itemView);
        }
    }
}
