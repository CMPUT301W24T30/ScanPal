package com.example.scanpal;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;
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

        //Toast.makeText( getApplicationContext(), "You've Received a new Notification: " + messageBody, Toast.LENGTH_SHORT).show();
        //View customView = LayoutInflater.from(getBaseContext()).inflate(R.layout.notif_popup, null);

        //Snackbar.make(getApplicationContext(), customView , messageBody,Snackbar.LENGTH_SHORT).show();

        //Snackbar mySnackbar = Snackbar.make(customView, "You've Received a new Notification: " + messageBody, BaseTransientBottomBar.LENGTH_SHORT);

        //mySnackbar.show();
        showCustomPopup(getApplicationContext(), messageBody);

    }

    private void showCustomPopup(Context context, String message) {

        new Handler(Looper.getMainLooper()).post(() -> {
            View customView = LayoutInflater.from(context).inflate(R.layout.notif_popup, null);
            TextView textView = customView.findViewById(R.id.popup_text);
            textView.setText(message);


            // Create and show the pop-up window
            PopupWindow popupWindow = new PopupWindow(customView, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            popupWindow.showAtLocation(customView, android.view.Gravity.TOP | android.view.Gravity.CENTER_HORIZONTAL, 0, 100);
            //popupWindow.showAsDropDown(customView);

            // Dismiss the pop-up window after a certain duration
            customView.postDelayed(popupWindow::dismiss, 3000);

        });
    }

}
