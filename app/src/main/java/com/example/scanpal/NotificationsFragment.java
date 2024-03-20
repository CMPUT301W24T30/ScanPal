package com.example.scanpal;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class NotificationsFragment extends Fragment {
    private ListView listView;
    private NotificationListAdapter adapter;
    private List<Announcement> notificationsList = new ArrayList<>();
    private EventController eventController;
    private List<String> allEventIDs;

    public NotificationsFragment() {

    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.notifications_screen, null, false);

        eventController = new EventController();

        // Set up button to navigate to user profile.
        FloatingActionButton profileButton = view.findViewById(R.id.button_profile);
        profileButton.setOnClickListener(v -> {
            NavController navController = NavHostFragment.findNavController(NotificationsFragment.this);
            navController.navigate(R.id.notifications_to_profile_fragment);
        });

        // Set up button to navigate to Homepage
        FloatingActionButton homeButton = view.findViewById(R.id.button_homepage);
        homeButton.setOnClickListener(v -> {
            NavController navController = NavHostFragment.findNavController(NotificationsFragment.this);
            navController.navigate(R.id.notifications_to_eventsPage);
        });

        adapter = new NotificationListAdapter(getContext(), new ArrayList<>());
        listView = view.findViewById(R.id.notification_list);
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
        AttendeeController attendeeController = new AttendeeController(FirebaseFirestore.getInstance(),this.getContext());
        UserController userController = new UserController(FirebaseFirestore.getInstance(),this.getContext());
        String userName = userController.fetchStoredUsername();


        eventController.getAllEventIds(new EventIDsFetchCallback() {
            @Override
            public void onSuccess(List<String> EventIDs) {
                allEventIDs = EventIDs;

                for(String ID : allEventIDs) {
                    Log.d("NOTIFICATIONS", "In for loop");

                    announcementController.getAnnouncementsByEventId(ID, new AnnouncementsFetchCallback() {
                        @Override
                        public void onSuccess(List<Announcement> notifications) {
                            Log.d("NOTIFICATIONS", "Successful announcement retrieval");
                            //add all the announcements for this event if checked in

                            attendeeController.fetchAttendee(userName + ID, new AttendeeFetchCallback() {
                                @Override
                                public void onSuccess(Attendee attendee) {
                                    if(attendee.isRsvp()) {//if theyre rvsp'd then add notifs
                                        Log.d("Testing Attendee is Rvsp'd", userName+ ID);


                                        notificationsList.addAll(notifications);
                                        adapter.setNotifications(notificationsList);

                                    }
                                }

                                @Override
                                public void onError(Exception e) {
                                    //Toast.makeText(getContext(), "Error fetching attendee", Toast.LENGTH_SHORT).show();
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