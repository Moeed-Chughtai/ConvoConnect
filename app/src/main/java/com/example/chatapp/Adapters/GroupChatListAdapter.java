package com.example.chatapp.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.chatapp.Models.User;
import com.example.chatapp.R;
import com.example.chatapp.databinding.RowConversationBinding;

import java.util.ArrayList;

public class GroupChatListAdapter extends RecyclerView.Adapter<GroupChatListAdapter.GroupChatListViewHolder> {

    Context context;
    ArrayList<User> chats;

    public GroupChatListAdapter(Context context, ArrayList<User> chats) {
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
