package com.example.chatapp.Adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.chatapp.Activities.GroupChatMessagesActivity;
import com.example.chatapp.Models.GroupChat;
import com.example.chatapp.R;
import com.example.chatapp.databinding.RowConversationBinding;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class GroupChatListAdapter extends RecyclerView.Adapter<GroupChatListAdapter.GroupChatListViewHolder> {

    Context context;
    ArrayList<GroupChat> chats;

    public GroupChatListAdapter(Context context, ArrayList<GroupChat> chats) {
        this.context = context;
        this.chats = chats;
    }

    @NonNull
    @Override
    public GroupChatListViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Links the sample design to the GroupChatListViewHolder
        View view = LayoutInflater.from(context).inflate(R.layout.row_conversation, parent, false);
        return new GroupChatListViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull GroupChatListViewHolder holder, int position) {
        GroupChat groupChat = chats.get(position);
        holder.binding.username.setText(groupChat.getName());

        FirebaseDatabase.getInstance().getReference()
                        .child("group_chat_latest_message")
                        .child(groupChat.getName())
                        // Retrieve latest message and time of message
                        .addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                if (snapshot.exists()) {
                                    String lastMsg = snapshot.child("lastMsg").getValue(String.class);
                                    long time = snapshot.child("lastMsgTime").getValue(Long.class);
                                    SimpleDateFormat dateFormat = new SimpleDateFormat("hh:mm a");
                                    // Replaces sample data with actual data
                                    holder.binding.msgTime.setText(dateFormat.format(new Date(time)));
                                    holder.binding.lastMsg.setText(lastMsg);

                                } else {
                                    // If no current messages exist
                                    holder.binding.msgTime.setText("");
                                    holder.binding.lastMsg.setText("Tap to chat");
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {
                            }
                        });

        // If a group chat is clicked, request to load the relevant chat
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, GroupChatMessagesActivity.class);
            intent.putExtra("name", groupChat.getName());
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return chats.size();
    }


    public static class GroupChatListViewHolder extends RecyclerView.ViewHolder {

        RowConversationBinding binding;

        // Binds the sample data to the RecyclerView
        public GroupChatListViewHolder(@NonNull View itemView) {
            super(itemView);
            binding = RowConversationBinding.bind(itemView);
        }
    }
}
