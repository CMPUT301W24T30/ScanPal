package com.example.scanpal;

import com.example.scanpal.EventController;

import android.content.Context;
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
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.FirebaseFirestore;
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

        gridView.setOnItemClickListener((parent, view1, position, id) -> {
            Event event = eventsList.get(position);
            Bundle bundle = new Bundle();
            bundle.putString("event_id", event.getId());
            NavHostFragment.findNavController(this).navigate(R.id.select_event, bundle);
        });

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
}
