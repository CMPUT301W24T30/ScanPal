package com.example.scanpal;

import android.os.Looper;
import android.widget.Toast;

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
        Looper.prepare();//causes error otherwise

        String messageBody = message.getNotification().getBody();

        Toast.makeText( getApplicationContext(), "You've Received a new Notification: " + messageBody, Toast.LENGTH_SHORT).show();

    }

}
