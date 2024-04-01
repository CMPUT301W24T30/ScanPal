package com.example.scanpal.Fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.scanpal.Models.Announcement;
import com.example.scanpal.Controllers.AnnouncementController;
import com.example.scanpal.Callbacks.AnnouncementsFetchCallback;
import com.example.scanpal.Models.Attendee;
import com.example.scanpal.Controllers.AttendeeController;
import com.example.scanpal.Callbacks.AttendeeFetchCallback;
import com.example.scanpal.Controllers.EventController;
import com.example.scanpal.Callbacks.EventIDsFetchCallback;
import com.example.scanpal.Adapters.NotificationListAdapter;
import com.example.scanpal.R;
import com.example.scanpal.Controllers.UserController;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class NotificationsFragment extends Fragment {
    private final List<Announcement> notificationsList = new ArrayList<>();
    private NotificationListAdapter adapter;
    private EventController eventController;
    private List<String> allEventIDs;

    public NotificationsFragment() {

    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.notifications_screen, null, false);

        eventController = new EventController();

        adapter = new NotificationListAdapter(getContext(), new ArrayList<>());
        ListView listView = view.findViewById(R.id.notification_list);
        listView.setAdapter(adapter);

        fetchAllNotifications();

        return view;
    }


    /**
     * Gets all notifications relevant to the current user
     */
    private void fetchAllNotifications() {

        //got to get all attendee objects that have your username + eventIDMatch
        //need a list of all eventIDs
        notificationsList.clear();
        AnnouncementController announcementController = new AnnouncementController();
        AttendeeController attendeeController = new AttendeeController(FirebaseFirestore.getInstance());
        UserController userController = new UserController( this.getContext());
        String userName = userController.fetchStoredUsername();


        eventController.getAllEventIds(new EventIDsFetchCallback() {
            @Override
            public void onSuccess(List<String> EventIDs) {
                allEventIDs = EventIDs;

                for (String ID : allEventIDs) {
                    Log.d("NOTIFICATIONS", "In for loop");

                    announcementController.getAnnouncementsByEventId(ID, new AnnouncementsFetchCallback() {
                        @Override
                        public void onSuccess(List<Announcement> notifications) {
                            Log.d("NOTIFICATIONS", "Successful announcement retrieval");
                            //add all the announcements for this event if checked in

                            attendeeController.fetchAttendee(userName + ID, new AttendeeFetchCallback() {
                                @Override
                                public void onSuccess(Attendee attendee) {
                                    if (attendee.isRsvp()) {
                                        notificationsList.addAll(notifications);
                                        adapter.setNotifications(notificationsList);
                                    }
                                }

                                @Override
                                public void onError(Exception e) {
                                    Log.d("Notifications", "Attendee Object Does not exist for: " + userName + ID);
                                }
                            });
                        }

                        @Override
                        public void onError(Exception e) {
                            Toast.makeText(getContext(), "Error fetching all event IDs.", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }

            @Override
            public void onError(Exception e) {

            }
        });
    }
}
