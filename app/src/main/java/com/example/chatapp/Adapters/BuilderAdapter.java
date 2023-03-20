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

public class BuilderAdapter extends RecyclerView.Adapter<BuilderAdapter.BuilderViewHolder> {

    Context context;
    ArrayList<User> users;
    DatabaseReference database;

    public BuilderAdapter(Context context, ArrayList<User> users) {
        this.context = context;
        this.users = users;
    }

    @NonNull
    @Override
    public BuilderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Links the sample design to the BuilderViewHolder
        View view = LayoutInflater.from(context).inflate(R.layout.users_list, parent, false);
        return new BuilderViewHolder(view);
    }

    // Sets the actual data for the RecyclerView
    @Override
    public void onBindViewHolder(@NonNull BuilderViewHolder holder, int position) {
        User user = users.get(position);

        // Replaces sample data with actual data
        holder.binding.username.setText(user.getName());
        holder.binding.status.setText(user.getStatus());
        Glide.with(context).load(user.getProfilePicture())
                .placeholder(R.drawable.avatar)
                .into(holder.binding.profile);

        // Add user to temp until group chat created
        holder.binding.addUser.setOnClickListener(view -> {
            database = FirebaseDatabase.getInstance().getReference();
            database.child("temp")
                    .child(FirebaseAuth.getInstance().getUid())
                    .child(user.getUid())
                    .setValue(user)
                    .addOnSuccessListener(unused -> Toast.makeText(context, "User added", Toast.LENGTH_LONG).show());
        });
    }

    @Override
    public int getItemCount() {
        return users.size();
    }


    public static class BuilderViewHolder extends RecyclerView.ViewHolder {

        UsersListBinding binding;

        // Binds the sample data to the RecyclerView
        public BuilderViewHolder(@NonNull View itemView) {
            super(itemView);
            binding = UsersListBinding.bind(itemView);
        }
    }
}
