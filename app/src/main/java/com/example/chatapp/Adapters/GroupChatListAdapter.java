package com.example.chatapp.Adapters;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.chatapp.Activities.ChatActivity;
import com.example.chatapp.Activities.GroupChatMessagesActivity;
import com.example.chatapp.Models.GroupChat;
import com.example.chatapp.Models.User;
import com.example.chatapp.R;
import com.example.chatapp.databinding.RowConversationBinding;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

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
        View view = LayoutInflater.from(context).inflate(R.layout.row_conversation, parent, false);
        return new GroupChatListViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull GroupChatListViewHolder holder, int position) {
        GroupChat groupChat = chats.get(position);
        holder.binding.username.setText(groupChat.getName());

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


    public class GroupChatListViewHolder extends RecyclerView.ViewHolder {

        RowConversationBinding binding;

        public GroupChatListViewHolder(@NonNull View itemView) {
            super(itemView);
            binding = RowConversationBinding.bind(itemView);
        }
    }
}
