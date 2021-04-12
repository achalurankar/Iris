package com.koven.iris.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.koven.iris.R;
import com.koven.iris.modal.Message;
import com.koven.iris.modal.User;
import com.koven.iris.util.Constants;
import com.koven.iris.util.Session;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

public class Chat extends AppCompatActivity {

    /**
     * activity variables
     */
    private TextView recipientName;
    private ImageView recipientImage;
    private EditText messageEditor;
    private ImageView sendBtn;
    private RecyclerView mRecyclerView;

    /**
     * Sender and receiver info
     */
    private User mReceiver;
    private User mSender;

    /**
     * message list
     */
    private List<Message> mMessages = new ArrayList<>();

    /**
     * Recycler view adapter
     */
    private CustomAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        mReceiver = Session.selectedUser;
        mSender = Session.currentUser;
        if (mReceiver == null || mSender == null) {
            finish();
            return;
        }
        recipientName = findViewById(R.id.username_tv);
        recipientImage = findViewById(R.id.profile_iv);
        messageEditor = findViewById(R.id.message_et);
        sendBtn = findViewById(R.id.send_btn);
        sendBtn.setOnClickListener(v -> sendMessage());
        mRecyclerView = findViewById(R.id.message_recycler_view);
        mRecyclerView.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setReverseLayout(true);
        mRecyclerView.setLayoutManager(linearLayoutManager);
        setRecipientInfo();
        getMessages();
    }

    private void sendMessage() {
        String messageText = messageEditor.getText().toString();
        if (messageText.length() == 0)
            return;
        String messageId = System.currentTimeMillis() + "";
        String senderId = mSender.getUserId();
        String receiverId = mReceiver.getUserId();
        Message message = new Message(
                "" + messageId,
                "" + senderId,
                "" + receiverId,
                "" + messageText);

        //update message to sender's log
        FirebaseFirestore.getInstance().collection(Constants.CHATS_COLLECTION + "/" + mSender.getUserId() + "/" + mReceiver.getUserId())
                .document(messageId)
                .set(message);

        //update message to receiver's log
        FirebaseFirestore.getInstance().collection(Constants.CHATS_COLLECTION + "/" + mReceiver.getUserId() + "/" + mSender.getUserId())
                .document(messageId)
                .set(message);

        //reset editor to blank for new message
        messageEditor.setText("");
    }

    private void setRecipientInfo() {
        recipientName.setText(mReceiver.getUsername());
        Picasso.with(this)
                .load(mReceiver.getPicUrl())
                .placeholder(R.drawable.user_vector)
                .into(recipientImage);
    }

    private void getMessages() {
        FirebaseFirestore.getInstance().collection(Constants.CHATS_COLLECTION + "/" + mSender.getUserId() + "/" + mReceiver.getUserId())
                .orderBy(Constants.MESSAGE_ID, Query.Direction.DESCENDING)
                .addSnapshotListener((value, error) -> {
                    if (value == null)
                        return;
                    mMessages.clear();
                    for (DocumentSnapshot documentSnapshot : value.getDocuments()) {
                        Message message = new Message();
                        if (documentSnapshot.get(Constants.MESSAGE_ID) != null)
                            message.setMessageId(documentSnapshot.get(Constants.MESSAGE_ID).toString());
                        if (documentSnapshot.get(Constants.MESSAGE_TEXT) != null)
                            message.setMessageText(documentSnapshot.get(Constants.MESSAGE_TEXT).toString());
                        if (documentSnapshot.get(Constants.MESSAGE_SENDER_ID) != null)
                            message.setSenderId(documentSnapshot.get(Constants.MESSAGE_SENDER_ID).toString());
                        if (documentSnapshot.get(Constants.MESSAGE_RECEIVER_ID) != null)
                            message.setReceiverId(documentSnapshot.get(Constants.MESSAGE_RECEIVER_ID).toString());
                        mMessages.add(message);
                    }
                    if (mMessages.size() == 0) {
                        mRecyclerView.setAdapter(null);
                        return;
                    }
                    mAdapter = new CustomAdapter(Chat.this, mMessages);
                    mAdapter.setHasStableIds(true);
                    mRecyclerView.setAdapter(mAdapter);
                });
    }

    public class CustomAdapter extends RecyclerView.Adapter<CustomAdapter.CustomViewHolder> {

        /**
         * Adapter member fields
         */
        private List<Message> mmList;
        private Context mmContext;

        public CustomAdapter(Context context, List<Message> list) {
            mmList = list;
            mmContext = context;
        }

        @NonNull
        @Override
        public CustomAdapter.CustomViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(mmContext).inflate(R.layout.message_item, parent, false);
            return new CustomAdapter.CustomViewHolder(v);
        }

        @Override
        public void onBindViewHolder(@NonNull CustomAdapter.CustomViewHolder holder, int position) {

            Message message = mmList.get(position);
            if (message.getSenderId().equals(mSender.getUserId())) {
                //message was sent by current user
                holder.receiverText.setVisibility(View.GONE);
                holder.senderText.setText(message.getMessageText());
            } else {
                //message was received by current user
                holder.senderText.setVisibility(View.GONE);
                holder.receiverText.setText(message.getMessageText());
            }
        }

        @Override
        public int getItemCount() {
            return mmList.size();
        }

        public class CustomViewHolder extends RecyclerView.ViewHolder {

            /**
             * view holder member fields
             */
            TextView senderText;
            TextView receiverText;

            public CustomViewHolder(@NonNull View itemView) {
                super(itemView);
                senderText = itemView.findViewById(R.id.sender_text);
                receiverText = itemView.findViewById(R.id.receiver_text);
            }
        }
    }
}