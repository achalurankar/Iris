package com.koven.iris.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.koven.iris.R;
import com.koven.iris.modal.User;
import com.koven.iris.util.Constants;
import com.koven.iris.util.Session;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

public class Users extends AppCompatActivity {

    /**
     * member fields
     */
    private RecyclerView mRecyclerView;
    private List<User> mUsers = new ArrayList<>();
    private CustomAdapter mAdapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_users);
        mRecyclerView = findViewById(R.id.recycler_view);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        getUsers();
    }

    private void getUsers() {
        FirebaseFirestore.getInstance().collection(Constants.USERS_COLLECTION)
                .addSnapshotListener((value, error) -> {
                    mUsers.clear();
                    for (DocumentSnapshot documentSnapshot : value.getDocuments()) {
                        User user = new User();
                        if (documentSnapshot.get(Constants.USERNAME) != null)
                            user.setUsername(documentSnapshot.get(Constants.USERNAME).toString());
                        if (documentSnapshot.get(Constants.USER_ID) != null)
                            user.setUserId(documentSnapshot.get(Constants.USER_ID).toString());
                        if (documentSnapshot.get(Constants.PIC_URL) != null)
                            user.setPicUrl(documentSnapshot.get(Constants.PIC_URL).toString());

                        if (!user.getUserId().equals(Session.currentUser.getUserId()))
                            mUsers.add(user);
                    }
                    if (mUsers.size() != 0) {
                        mAdapter = new CustomAdapter(Users.this, mUsers);
                        mAdapter.setHasStableIds(true);
                        mRecyclerView.setAdapter(mAdapter);
                    } else
                        mRecyclerView.setAdapter(null);
                });
    }


    public class CustomAdapter extends RecyclerView.Adapter<CustomAdapter.CustomViewHolder> {

        /**
         * Adapter member fields
         */
        private List<User> mmList;
        private Context mmContext;

        public CustomAdapter(Context context, List<User> list) {
            mmList = list;
            mmContext = context;
        }

        @NonNull
        @Override
        public CustomViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(mmContext).inflate(R.layout.user_list_item, parent, false);
            return new CustomViewHolder(v);
        }

        @Override
        public void onBindViewHolder(@NonNull CustomViewHolder holder, int position) {

            User user = mmList.get(position);
            Picasso.with(mmContext)
                    .load(user.getPicUrl())
                    .placeholder(R.drawable.user_vector)
                    .into(holder.profilePic);
            holder.username.setText(user.getUsername());

            holder.item.setOnClickListener(v -> {
                Session.selectedUser = user;
                startActivity(new Intent(getApplicationContext(), Chat.class));
                overridePendingTransition(0, 0);
            });
        }

        @Override
        public int getItemCount() {
            return mmList.size();
        }

        public class CustomViewHolder extends RecyclerView.ViewHolder {

            /**
             * view holder member fields
             */
            RelativeLayout item;
            ImageView profilePic;
            TextView username;

            public CustomViewHolder(@NonNull View itemView) {
                super(itemView);
                item = itemView.findViewById(R.id.item);
                profilePic = itemView.findViewById(R.id.profile_iv);
                username = itemView.findViewById(R.id.username_tv);
            }
        }
    }
}