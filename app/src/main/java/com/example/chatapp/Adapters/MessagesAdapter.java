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
import com.example.chatapp.R;
import com.example.chatapp.databinding.ItemReceiveBinding;
import com.example.chatapp.databinding.ItemSendBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.Objects;

public class MessagesAdapter extends RecyclerView.Adapter {

    FirebaseDatabase database;

    Context context;
    ArrayList<Message> messages;
    String senderRoom, receiverRoom;

    final int SEND = 1;
    final int RECEIVE = 2;

    private String editedMessage;

    public MessagesAdapter(Context context, ArrayList<Message> messages, String senderRoom, String receiverRoom) {
        this.context = context;
        this.messages = messages;
        this.senderRoom = senderRoom;
        this.receiverRoom = receiverRoom;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Differentiate the viewType as there is the send and receive views
        if(viewType == SEND) {
            // Links the send sample design to SendViewHolder
            View view = LayoutInflater.from(context).inflate(R.layout.item_send, parent, false);
            return new SendViewHolder(view);

        }else {
            // Links the receive sample design to ReceiverViewHolder
            View view = LayoutInflater.from(context).inflate(R.layout.item_receive, parent, false);
            return new ReceiverViewHolder(view);
        }
    }

    // There are 2 different views so this determine which is required
    @Override
    public int getItemViewType(int position) {
        Message message = messages.get(position);
        // Compares uid of current user and to uid of the sender of message
        if(Objects.equals(FirebaseAuth.getInstance().getUid(), message.getSenderId())) {
            // If same, it must have been sent by the current user
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
            SendViewHolder viewHolder = (SendViewHolder)holder;

            // Set imageView visible if media sent
            if (message.getMessage().equals("media")) {
                // Make ImageView visible and TextView gone
                viewHolder.binding.media.setVisibility(View.VISIBLE);
                viewHolder.binding.message.setVisibility(View.GONE);
                // Set edit and delete TextViews gone
                viewHolder.binding.linearLayout2.setVisibility(View.GONE);
                Glide.with(context)
                        .load(message.getMediaUrl())
                        // Placeholder whilst media loads
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

            if (message.getMessage().equals("deleted")) {
                // Replace TextView if msg is deleted
                viewHolder.binding.message.setVisibility(View.GONE);
                viewHolder.binding.deleted.setVisibility(View.VISIBLE);
                // Remove edit and delete TextViews if msg deleted
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
                AlertDialog.Builder builder = new AlertDialog.Builder(view.getContext());
                builder.setTitle("Enter edited message: ");
                // EditText displayed within the builder
                final EditText input = new EditText(view.getContext());
                input.setInputType(InputType.TYPE_CLASS_TEXT);
                builder.setView(input);
                // OK button once user is done
                builder.setPositiveButton("OK", (dialogInterface, i) -> {
                    // Get text from input
                    editedMessage = input.getText().toString();

                    if (!editedMessage.equals("")) {
                        // Update message object
                        message.setEdited(Boolean.TRUE);
                        message.setMessage(editedMessage);
                        // Update message object in database
                        database.getReference()
                                .child("chats")
                                .child(senderRoom)
                                .child("messages")
                                .child(String.valueOf(message.getTimestamp()))
                                .setValue(message).addOnSuccessListener(aVoid -> database.getReference()
                                        .child("chats")
                                        .child(receiverRoom)
                                        .child("messages")
                                        .child(String.valueOf(message.getTimestamp()))
                                        .setValue(message).addOnSuccessListener(aVoid1 -> {
                                        }));
                    }
                });
                builder.show();
            });

            viewHolder.binding.delete.setOnClickListener(view -> {
                message.setMessage("deleted");
                // Identify deleted messages and remove them from database
                database.getReference()
                        .child("chats")
                        .child(senderRoom)
                        .child("messages")
                        .child(String.valueOf(message.getTimestamp()))
                        .setValue(message).addOnSuccessListener(aVoid -> database.getReference()
                                .child("chats")
                                .child(receiverRoom)
                                .child("messages")
                                .child(String.valueOf(message.getTimestamp()))
                                .setValue(message).addOnSuccessListener(aVoid1 -> {
                                }));
            });

        }else {
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
        }
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }


    public static class SendViewHolder extends RecyclerView.ViewHolder {

        ItemSendBinding binding;

        // Binds the sample data to the RecyclerView
        public SendViewHolder(@NonNull View itemView) {
            super(itemView);
            binding = ItemSendBinding.bind(itemView);
        }
    }

    public static class ReceiverViewHolder extends RecyclerView.ViewHolder {

        ItemReceiveBinding binding;

        // Binds the sample data to the RecyclerView
        public ReceiverViewHolder(@NonNull View itemView) {
            super(itemView);
            binding = ItemReceiveBinding.bind(itemView);
        }
    }
}
