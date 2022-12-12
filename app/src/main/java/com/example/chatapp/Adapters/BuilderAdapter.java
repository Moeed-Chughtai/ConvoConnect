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
        View view = LayoutInflater.from(context).inflate(R.layout.users_list, parent, false);
        return new BuilderViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BuilderViewHolder holder, int position) {
        User user = users.get(position);

        holder.binding.username.setText(user.getName());
        holder.binding.status.setText(user.getStatus());
        Glide.with(context).load(user.getProfilePicture())
                .placeholder(R.drawable.avatar)
                .into(holder.binding.profile);

        holder.binding.addUser.setOnClickListener(view -> {
            database = FirebaseDatabase.getInstance().getReference();
            database.child("temp")
                    .child(FirebaseAuth.getInstance().getUid())
                    .child(user.getUid())
                    .setValue(user)
                    .addOnSuccessListener(unused -> {
                        Toast.makeText(context, "User added", Toast.LENGTH_LONG).show();
                    });
        });
    }

    @Override
    public int getItemCount() {
        return users.size();
    }


    public class BuilderViewHolder extends RecyclerView.ViewHolder {

        UsersListBinding binding;

        public BuilderViewHolder(@NonNull View itemView) {
            super(itemView);
            binding = UsersListBinding.bind(itemView);
        }
    }
}
