package com.example.scanpal;

import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Toolbar;

import androidx.annotation.NonNull;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.mifmif.common.regex.Main;

public class MyFirebaseMessagingService extends FirebaseMessagingService {


    /**
     * Required empty public constructor
     */
    public MyFirebaseMessagingService() {

    }

    /**
     * Handles what to do when message is received
     *
     * @param message Remote message that has been received.
     */
    @Override
    public void onMessageReceived(@NonNull RemoteMessage message) {
        super.onMessageReceived(message);

        String messageBody = message.getNotification().getBody();

        Log.d("msg", "onMessageReceived: " + message.getNotification().getBody());

        Intent intent = new Intent("com.example.scanpal.MESSAGE_RECEIVED");
        intent.putExtra("message", messageBody);
        sendBroadcast(intent);


    }

}
