package com.koven.iris.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import com.koven.iris.R;
import com.koven.iris.modal.User;
import com.koven.iris.util.Session;

public class Chat extends AppCompatActivity {

    private User mReceiver;
    private User mSender;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        mReceiver = Session.selectedUser;
        mSender = Session.currentUser;
        if(mReceiver == null || mSender == null){
            finish();
            return;
        }
    }
}