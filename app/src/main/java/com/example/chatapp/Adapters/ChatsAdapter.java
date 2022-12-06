package com.example.chatapp.Adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.chatapp.Activities.ChatActivity;
import com.example.chatapp.R;
import com.example.chatapp.Models.User;
import com.example.chatapp.databinding.RowConversationBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class ChatsAdapter extends RecyclerView.Adapter<ChatsAdapter.ChatsViewHolder> {

    Context context;
    ArrayList<User> users;

    // Constructor
    public ChatsAdapter(Context context, ArrayList<User> users) {
        this.context = context;
        this.users = users;
    }

    @NonNull
    @Override
    public ChatsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.row_conversation, parent, false);
        return new ChatsViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ChatsViewHolder holder, int position) {
        User user = users.get(position);

        String senderId = FirebaseAuth.getInstance().getUid();
        // Unique string with sender and receiver id combined
        String senderRoom = senderId + user.getUid();

        // If there is a new message, update latest message and time of that message in MainActivity
        FirebaseDatabase.getInstance().getReference()
                        .child("chats")
                        .child(senderRoom)
                        // Listens for any changes
                        .addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                if (snapshot.exists()) {
                                    // Gets value of msg and time
                                    String lastMsg = snapshot.child("lastMsg").getValue(String.class);
                                    long time = snapshot.child("lastMsgTime").getValue(Long.class);
                                    SimpleDateFormat dateFormat = new SimpleDateFormat("hh:mm a");
                                    // Replaces sample data with actual data
                                    holder.binding.msgTime.setText(dateFormat.format(new Date(time)));
                                    holder.binding.lastMsg.setText(lastMsg);

                                // If no current messages exist
                                } else {
                                    holder.binding.msgTime.setText("");
                                    holder.binding.lastMsg.setText("Tap to chat");
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {
                            }
                        });

        // Sets the name of user
        holder.binding.username.setText(user.getName());

        // Loads users profile picture
        Glide.with(context).load(user.getProfilePicture())
                // If user does not have a profile picture
                .placeholder(R.drawable.avatar)
                .into(holder.binding.profile);

        // If a current chat is clicked, redirects to chat activity
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, ChatActivity.class);
                // Information sent to next activity
                intent.putExtra("name", user.getName());
                intent.putExtra("uid", user.getUid());
                context.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return users.size();
    }


    public class ChatsViewHolder extends RecyclerView.ViewHolder {

        RowConversationBinding binding;

        public ChatsViewHolder(@NonNull View itemView) {
            super(itemView);
            // Reference all views by binding with xml file
            binding = RowConversationBinding.bind(itemView);
        }
    }
}
