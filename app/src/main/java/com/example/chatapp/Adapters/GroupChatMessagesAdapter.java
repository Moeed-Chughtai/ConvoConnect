package com.example.chatapp.Adapters;

import android.app.AlertDialog;
import android.content.Context;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.chatapp.Models.Message;
import com.example.chatapp.Models.User;
import com.example.chatapp.R;
import com.example.chatapp.databinding.GroupReceiveBinding;
import com.example.chatapp.databinding.GroupSendBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Objects;

// Similar to MessagesAdapter
public class GroupChatMessagesAdapter extends RecyclerView.Adapter {

    FirebaseDatabase database;

    Context context;
    ArrayList<Message> messages;
    String name;

    final int SEND = 1;
    final int RECEIVE = 2;

    private String editedMessage;

    public GroupChatMessagesAdapter(Context context, ArrayList<Message> messages, String name) {
        this.context = context;
        this.messages = messages;
        this.name = name;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Differentiate the viewType as there is the send and receive views
        if(viewType == SEND) {
            // Links the send sample design to SendViewHolder
            View view = LayoutInflater.from(context).inflate(R.layout.group_send, parent, false);
            return new SendViewHolder(view);

        }else {
            // Links the receive sample design to ReceiverViewHolder
            View view = LayoutInflater.from(context).inflate(R.layout.group_receive, parent, false);
            return new ReceiverViewHolder(view);
        }
    }

    // There are 2 different views so this determine which is required
    @Override
    public int getItemViewType(int position) {
        Message message = messages.get(position);
        // Compares uid of current user and to uid of the sender of message
        if(Objects.equals(FirebaseAuth.getInstance().getUid(), message.getSenderId())) {
            return SEND;
        } else {
            return RECEIVE;
        }
    }

    // Not known which viewHolder is passed through
    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        database = FirebaseDatabase.getInstance();
        Message message = messages.get(position);
        if(holder.getClass() == SendViewHolder.class) {
            SendViewHolder viewHolder = (SendViewHolder) holder;

            // Set imageView visible if media sent
            if (message.getMessage().equals("media")) {
                // Make ImageView visible and TextView gone
                viewHolder.binding.media.setVisibility(View.VISIBLE);
                viewHolder.binding.message.setVisibility(View.GONE);
                // Set edit and delete TextViews gone
                viewHolder.binding.linearLayout2.setVisibility(View.GONE);
                Glide.with(context)
                        .load(message.getMediaUrl())
                        .placeholder(R.drawable.placeholder)
                        .into(viewHolder.binding.media);
            }

            // Replace edit with edited if edit has been done
            if (message.getEdited().equals(Boolean.TRUE)) {
                viewHolder.binding.edit.setVisibility(View.GONE);
                viewHolder.binding.edited.setVisibility(View.VISIBLE);
            } else {
                viewHolder.binding.edit.setVisibility(View.VISIBLE);
                viewHolder.binding.edited.setVisibility(View.GONE);
            }

            // Replace TextView if msg is deleted
            if (message.getMessage().equals("deleted")) {
                viewHolder.binding.message.setVisibility(View.GONE);
                viewHolder.binding.deleted.setVisibility(View.VISIBLE);
                viewHolder.binding.edit.setVisibility(View.GONE);
                viewHolder.binding.edited.setVisibility(View.GONE);
                viewHolder.binding.delete.setVisibility(View.GONE);
            } else {
                if (!message.getMessage().equals("media")) {
                    viewHolder.binding.message.setVisibility(View.VISIBLE);
                    viewHolder.binding.deleted.setVisibility(View.GONE);
                    // Only show text if not deleted
                    viewHolder.binding.message.setText(message.getMessage());
                }
            }

            viewHolder.binding.edit.setOnClickListener(view -> {
                // Display popup
                AlertDialog.Builder builder = new AlertDialog.Builder(view.getContext());
                builder.setTitle("Enter edited message: ");
                // EditText displayed within the builder
                final EditText input = new EditText(view.getContext());
                input.setInputType(InputType.TYPE_CLASS_TEXT);
                builder.setView(input);
                builder.setPositiveButton("OK", (dialogInterface, i) -> {
                    // Get text from input
                    editedMessage = input.getText().toString();

                    if (!editedMessage.equals("")) {
                        // Update message object
                        message.setEdited(Boolean.TRUE);
                        message.setMessage(editedMessage);
                        // Update message object in database
                        database.getReference()
                                .child("group_chat_messages")
                                .child(name)
                                .child(String.valueOf(message.getTimestamp()))
                                .setValue(message)
                                .addOnSuccessListener(unused -> {
                                });
                    }
                });
                builder.show();
            });

            viewHolder.binding.delete.setOnClickListener(view -> {
                message.setMessage("deleted");
                // Identify deleted messages and remove them from database
                database.getReference()
                        .child("group_chat_messages")
                        .child(name)
                        .child(String.valueOf(message.getTimestamp()))
                        .setValue(message)
                        .addOnSuccessListener(unused -> {
                        });
            });

        } else {
            // Must be ReceiveViewHolder
            ReceiverViewHolder viewHolder = (ReceiverViewHolder)holder;
            if (message.getMessage().equals("media")) {
                viewHolder.binding.media.setVisibility(View.VISIBLE);
                viewHolder.binding.message.setVisibility(View.GONE);
                Glide.with(context)
                        .load(message.getMediaUrl())
                        .placeholder(R.drawable.placeholder)
                        .into(viewHolder.binding.media);
            }

            // Replace TextView with edited if message is edited
            if (message.getEdited().equals(Boolean.TRUE)) {
                viewHolder.binding.edited.setVisibility(View.VISIBLE);
            } else {
                viewHolder.binding.edited.setVisibility(View.GONE);
            }

            if (message.getMessage().equals("deleted")) {
                // Replace TextView with deleted message if deleted
                viewHolder.binding.message.setVisibility(View.GONE);
                viewHolder.binding.deleted.setVisibility(View.VISIBLE);
                viewHolder.binding.edited.setVisibility(View.GONE);
            } else {
                if (!message.getMessage().equals("media")) {
                    viewHolder.binding.message.setVisibility(View.VISIBLE);
                    viewHolder.binding.deleted.setVisibility(View.GONE);
                    // Only show text if not deleted
                    viewHolder.binding.message.setText(message.getMessage());
                }
            }

            database.getReference()
                    .child("users")
                    .child(message.getSenderId())
                    .addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if (snapshot.exists()) {
                                User user = snapshot.getValue(User.class);
                                viewHolder.binding.name.setText(user.getName());
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                        }
                    });
        }
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }


    public static class SendViewHolder extends RecyclerView.ViewHolder {

        GroupSendBinding binding;

        // Binds the sample data to the RecyclerView
        public SendViewHolder(@NonNull View itemView) {
            super(itemView);
            binding = GroupSendBinding.bind(itemView);
        }
    }

    public static class ReceiverViewHolder extends RecyclerView.ViewHolder {

        GroupReceiveBinding binding;

        // Binds the sample data to the RecyclerView
        public ReceiverViewHolder(@NonNull View itemView) {
            super(itemView);
            binding = GroupReceiveBinding.bind(itemView);
        }
    }
}
