package com.example.scanpal.Fragments;

import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import com.example.scanpal.Adapters.EventGridAdapter;
import com.example.scanpal.Callbacks.EventFetchByUserCallback;
import com.example.scanpal.Callbacks.EventsFetchCallback;
import com.example.scanpal.Callbacks.UserSignedUpCallback;
import com.example.scanpal.Controllers.EventController;
import com.example.scanpal.Controllers.UserController;
import com.example.scanpal.Models.Event;
import com.example.scanpal.R;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

/**
 * Fragment for displaying a list of events. Allows users to navigate to event details,
 * add new events, and scan QR codes for event-related actions.
 */
public class BrowseYourEventFragment extends Fragment {

    //necessary to request user for notification perms
    private final ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (!isGranted) {
                    Toast.makeText(getContext(), "Notifications cannot be sent since the permission is disabled.", Toast.LENGTH_LONG).show();
                }
            });
    protected List<Event> eventsList = new ArrayList<>();
    protected List<Event> allEvents = new ArrayList<>();

    private EventGridAdapter adapter;
    private EventController eventController;


    /**
     * Default constructor. Initializes the fragment.
     */
    public BrowseYourEventFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.browse_events, container, false);
        ((TextView) view.findViewById(R.id.event_page_title)).setText("Your Events");

        adapter = new EventGridAdapter(getContext());
        GridView gridView = view.findViewById(R.id.event_grid);
        gridView.setAdapter(adapter);

        // init eventController
        eventController = new EventController();

        fetchYourEvents();

        // Set up button to add new events.
        FloatingActionButton addEventButton = view.findViewById(R.id.button_add_event);
        addEventButton.setOnClickListener(v -> {
            NavController navController = NavHostFragment.findNavController(BrowseYourEventFragment.this);
            navController.navigate(R.id.addEditEvent);
        });

        gridView.setOnItemClickListener((parent, view1, position, id) -> {
            Event event = allEvents.get(position);
            Bundle bundle = new Bundle();
            bundle.putString("event_id", event.getId());
            NavHostFragment.findNavController(this).navigate(R.id.eventDetailsPage, bundle);
        });

        askNotificationPermission();
        fetchEventsAndUpdateGrid();

        return view;
    }

    /**
     * Fetches all events organized by the user from the data source and updates the grid view accordingly.
     * Applies filters based on user sign-up status and sorts events.
     */
    private void fetchYourEvents() {
        UserController userController = new UserController(getContext());
        String username = userController.fetchStoredUsername();
        if (username != null) {
            // Use EventController to fetch user-specific events
            eventController.getEventsByUser(new View(getContext()), new EventFetchByUserCallback() {
                @Override
                public void onSuccess(List<Event> events) {
                    allEvents.clear();
                    allEvents.addAll(events);

                    CountDownLatch latch = new CountDownLatch(allEvents.size());

                    for (Event event : allEvents) {
                        userController.isUserSignedUp(username, event.getId(), new UserSignedUpCallback() {
                            @Override
                            public void onResult(boolean isSignedUp) {
                                event.setUserSignedUp(isSignedUp);
                                latch.countDown();
                            }

                            @Override
                            public void onError(Exception e) {
                                latch.countDown();
                            }
                        });
                    }

                    new Thread(() -> {
                        try {
                            latch.await();
                            requireActivity().runOnUiThread(() -> {
                                allEvents.sort((o1, o2) -> Boolean.compare(o2.isUserSignedUp(), o1.isUserSignedUp()));
                                adapter.setEvents(allEvents);
                            });
                        } catch (InterruptedException ignored) {
                        }
                    }).start();
                }

                @Override
                public void onError(Exception e) {
                    Toast.makeText(getContext(), "Error fetching all events.", Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            Toast.makeText(getContext(), "Username not found", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Fetches events from the data source and updates the grid view.
     * This method differs from fetchYourEvents by focusing on updating the existing list.
     */
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

    /**
     * Requests the POST_NOTIFICATIONS permission from the user at runtime.
     * This method is only relevant for devices running on Android Tiramisu (API level 33) or above.
     */
    private void askNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(requireContext(), android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                requestPermissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS);
            }
        }
    }
}
