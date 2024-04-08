package com.example.scanpal;

import android.content.Intent;

import android.util.Log;

import androidx.annotation.NonNull;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

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
