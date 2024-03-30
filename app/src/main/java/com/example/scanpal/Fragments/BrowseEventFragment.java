package com.example.scanpal.Fragments;

import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.GridView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import com.example.scanpal.Adapters.EventGridAdapter;
import com.example.scanpal.Callbacks.EventsFetchCallback;
import com.example.scanpal.Callbacks.UserFetchCallback;
import com.example.scanpal.Callbacks.UserSignedUpCallback;
import com.example.scanpal.Controllers.EventController;
import com.example.scanpal.Controllers.UserController;
import com.example.scanpal.Models.Event;
import com.example.scanpal.Models.User;
import com.example.scanpal.R;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

/**
 * Fragment for displaying a list of events. Allows users to navigate to event details,
 * add new events, and scan QR codes for event-related actions.
 */
public class BrowseEventFragment extends Fragment {

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
    public BrowseEventFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.browse_events, container, false);

        adapter = new EventGridAdapter(getContext(), new ArrayList<>());
        GridView gridView = view.findViewById(R.id.event_grid);
        gridView.setAdapter(adapter);

        // init eventController
        eventController = new EventController();

        AutoCompleteTextView dropdown = view.findViewById(R.id.browser_select_autocomplete);

        dropdown.setText("Events Browser");

        // Create an ArrayAdapter using the string array and a default dropdown layout.
        ArrayAdapter<CharSequence> adapter = new ArrayAdapter<>(
                requireContext(),
                R.layout.browser_list_item,
                new String[]{"Events Browser", "Image Browser", "Profile Browser"} // Directly input your strings here
        );
        // Apply the adapter to the dropdown.
        dropdown.setAdapter(adapter);

        UserController userController = new UserController(FirebaseFirestore.getInstance(), this.getContext());
        if (userController.fetchStoredUsername() != null) {
            userController.getUser(userController.fetchStoredUsername(), new UserFetchCallback() {
                @Override
                public void onSuccess(User user) {
                    if (user.isAdministrator()) {
                        // If user is administrator, browser is visible
                        view.findViewById(R.id.browser_select).setVisibility(View.VISIBLE);
                    }
                }

                @Override
                public void onError(Exception e) {
                    Toast.makeText(view.getContext(), "Failed to load user details", Toast.LENGTH_LONG).show();
                }
            });
        }

        dropdown.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // Similar handling as above
                String selectedItem = (String) parent.getItemAtPosition(position);
                switch (selectedItem) {
                    case "Events Browser":
                        NavHostFragment.findNavController(BrowseEventFragment.this).navigate(R.id.eventsPage);
                        break;
                    case "Image Browser":
                        NavHostFragment.findNavController(BrowseEventFragment.this).navigate(R.id.browseImageFragment);
                        break;
                    case "Profile Browser":
                        NavHostFragment.findNavController(BrowseEventFragment.this).navigate(R.id.browseProfileFragment);
                        break;
                }
            }
        });

        fetchAllEvents();

        // Set up button to add new events.
        FloatingActionButton addEventButton = view.findViewById(R.id.button_add_event);
        addEventButton.setOnClickListener(v -> {
            NavController navController = NavHostFragment.findNavController(BrowseEventFragment.this);
            navController.navigate(R.id.addEvent);
        });

        gridView.setOnItemClickListener((parent, view1, position, id) -> {
            Event event = allEvents.get(position);
            Bundle bundle = new Bundle();
            bundle.putString("event_id", event.getId());
            NavHostFragment.findNavController(this).navigate(R.id.select_event, bundle);
        });

        askNotificationPermission();
        fetchEventsAndUpdateGrid();

        return view;
    }

    /**
     * Fetches all events from the data source and updates the grid view accordingly.
     * Applies filters based on user sign-up status and sorts events.
     */
    private void fetchAllEvents() {
        UserController userController = new UserController(FirebaseFirestore.getInstance(), this.getContext());
        eventController.fetchAllEvents(new EventsFetchCallback() {
            @Override
            public void onSuccess(List<Event> events) {
                allEvents.clear();
                allEvents.addAll(events);

                CountDownLatch latch = new CountDownLatch(allEvents.size());

                for (Event event : allEvents) {
                    userController.isUserSignedUp(userController.fetchStoredUsername(), event.getId(), new UserSignedUpCallback() {
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
    }

    /**
     * Fetches events from the data source and updates the grid view.
     * This method differs from fetchAllEvents by focusing on updating the existing list.
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