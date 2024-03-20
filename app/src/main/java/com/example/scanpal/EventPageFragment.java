package com.example.scanpal;

import com.example.scanpal.EventController;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.Toast;
import android.widget.Button;


import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.messaging.FirebaseMessaging;
import com.journeyapps.barcodescanner.ScanContract;
import com.journeyapps.barcodescanner.ScanOptions;

import java.util.ArrayList;
import java.util.List;

/**
 * Fragment for displaying a list of events. Allows users to navigate to event details,
 * add new events, and scan QR codes for event-related actions.
 */
public class EventPageFragment extends Fragment {

    private ArrayList<String> testList;
    private ArrayList<String> EventIDs;
    private ActivityResultLauncher<ScanOptions> qrCodeScanner;
    private QrScannerController qrScannerController;

    private GridView gridView;
    private EventGridAdapter adapter;
    private List<Event> eventsList = new ArrayList<>();

    private Button buttonAllEvents;
    private Button buttonYourEvents;
    private List<Event> allEvents = new ArrayList<>();
    private List<Event> userEvents = new ArrayList<>();
    private EventController eventController;

    //necessary to request user for notification perms
    private final ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    // FCM SDK (and your app) can post notifications.
                } else {
                    // TODO: Inform user that that your app will not show notifications.
                }
            });


    /**
     * Default constructor for EventPageFragment.
     */
    public EventPageFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.events_page, container, false);

        adapter = new EventGridAdapter(getContext(), new ArrayList<>());
        gridView = view.findViewById(R.id.event_grid);
        gridView.setAdapter(adapter);

        // init eventController
        eventController = new EventController();

        // buttons init
        buttonAllEvents = view.findViewById(R.id.button_all_events);
        buttonYourEvents = view.findViewById(R.id.button_user_events);

        // button click listeners
        buttonAllEvents.setOnClickListener(v -> showAllEvents());
        buttonYourEvents.setOnClickListener(v -> showYourEvents());

        fetchAllEvents();
        fetchYourEvents();

        AttendeeController attendeeController = new AttendeeController(FirebaseFirestore.getInstance(), getContext());
        qrScannerController = new QrScannerController(attendeeController);

        // Initialize QR Code Scanner and set up scan button.
        qrCodeScanner = registerForActivityResult(new ScanContract(), result -> {
            if (result.getContents() != null) {
                UserController userController = new UserController(FirebaseFirestore.getInstance(), getContext());
                String username = userController.fetchStoredUsername();
                qrScannerController.handleResult(result.getContents(), username);
            } else {
                Toast.makeText(getContext(), "Invalid QR Code", Toast.LENGTH_SHORT).show();
            }
        });

        FloatingActionButton scan = view.findViewById(R.id.button_scan);
        scan.setOnClickListener(v -> qrCodeScanner.launch(QrScannerController.getOptions()));

        // Set up button to add new events.
        FloatingActionButton addEventButton = view.findViewById(R.id.button_add_event);
        addEventButton.setOnClickListener(v -> {
            NavController navController = NavHostFragment.findNavController(EventPageFragment.this);
            navController.navigate(R.id.addEvent);
        });

        // Set up button to navigate to user profile.
        FloatingActionButton profileButton = view.findViewById(R.id.button_profile);
        profileButton.setOnClickListener(v -> {
            NavController navController = NavHostFragment.findNavController(EventPageFragment.this);
            navController.navigate(R.id.events_to_profile);
        });

        // Set up button to navigate to Notifications/Announcements.
        FloatingActionButton notificationsButton = view.findViewById(R.id.button_notifications);
        notificationsButton.setOnClickListener(v -> {
            NavController navController = NavHostFragment.findNavController(EventPageFragment.this);
            navController.navigate(R.id.eventsPage_to_notifications);
        });

        gridView.setOnItemClickListener((parent, view1, position, id) -> {
            Event event = eventsList.get(position);
            Bundle bundle = new Bundle();
            bundle.putString("event_id", event.getId());
            NavHostFragment.findNavController(this).navigate(R.id.select_event, bundle);
        });


        askNotificationPermission();//ask the user for perms
        // Fetch events from Firebase and update the grid
        fetchEventsAndUpdateGrid();




        return view;
    }


    private void fetchAllEvents() {
        eventController.fetchAllEvents(new EventsFetchCallback() {
            @Override
            public void onSuccess(List<Event> events) {
                allEvents.clear();
                allEvents.addAll(events);
                // Check if the "All Events" view is active before updating the adapter
                if (buttonAllEvents.isEnabled()) {
                    adapter.setEvents(allEvents);
                }
            }

            @Override
            public void onError(Exception e) {
                // Handle the error, possibly by showing a toast message
                Toast.makeText(getContext(), "Error fetching all events.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void fetchYourEvents() {
        UserController userController = new UserController(FirebaseFirestore.getInstance(), getContext());
        String username = userController.fetchStoredUsername();
        if (username != null) {
            // Use EventController to fetch user-specific events
            eventController.getEventsByUser(new View(getContext()), new EventFetchByUserCallback() {
                @Override
                public void onSuccess(List<Event> events) {
                    userEvents.clear();
                    userEvents.addAll(events);
                    // Check if the "Your Events" view is active before updating the adapter
                    if (!buttonAllEvents.isEnabled()) {
                        adapter.setEvents(userEvents);
                    }
                }

                @Override
                public void onError(Exception e) {
                    // Handle the error, possibly by showing a toast message
                    Toast.makeText(getContext(), "Error fetching your events.", Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            Toast.makeText(getContext(), "Username not found", Toast.LENGTH_SHORT).show();
        }
    }


    private void showAllEvents() {
        adapter.setEvents(allEvents);
        buttonAllEvents.setEnabled(false);
        buttonYourEvents.setEnabled(true);
    }

    private void showYourEvents() {
        adapter.setEvents(userEvents);
        buttonAllEvents.setEnabled(true);
        buttonYourEvents.setEnabled(false);
    }

    private void fetchEventsAndUpdateGrid() {
        EventController eventController = new EventController();
        eventController.fetchAllEvents(new EventsFetchCallback() {
            @Override
            public void onSuccess(List<Event> events) {
                eventsList.clear();
                eventsList.addAll(events);
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onError(Exception e) {
                Toast.makeText(getContext(), "Error fetching events: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }


    private void askNotificationPermission() {
        // This is only necessary for API level >= 33 (TIRAMISU)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this.getContext(), android.Manifest.permission.POST_NOTIFICATIONS) ==
                    PackageManager.PERMISSION_GRANTED) {
                // FCM SDK (and your app) can post notifications.
            } else if (shouldShowRequestPermissionRationale(android.Manifest.permission.POST_NOTIFICATIONS)) {
                // TODO: display an educational UI explaining to the user the features that will be enabled
                //       by them granting the POST_NOTIFICATION permission. This UI should provide the user
                //       "OK" and "No thanks" buttons. If the user selects "OK," directly request the permission.
                //       If the user selects "No thanks," allow the user to continue without notifications.
            } else {
                // Directly ask for the permission
                requestPermissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS);
            }
        }

    }
}
