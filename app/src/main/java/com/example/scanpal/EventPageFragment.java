package com.example.scanpal;

import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

/**
 * Fragment for displaying a list of events. Allows users to navigate to event details,
 * add new events, and scan QR codes for event-related actions.
 */
public class EventPageFragment extends Fragment {

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
     * Default constructor for EventPageFragment.
     */
    public EventPageFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.events_page, container, false);

        adapter = new EventGridAdapter(getContext(), new ArrayList<>());
        GridView gridView = view.findViewById(R.id.event_grid);
        gridView.setAdapter(adapter);

        // init eventController
        eventController = new EventController();

        Spinner spinner = view.findViewById(R.id.browser_select);
        // Create an ArrayAdapter using the string array and a default spinner layout.
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                requireContext(),
                R.array.browser_select_array,
                android.R.layout.simple_spinner_item
        );
        // Specify the layout to use when the list of choices appears.
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner.
        spinner.setAdapter(adapter);

        fetchAllEvents();

        // Set up button to add new events.
        FloatingActionButton addEventButton = view.findViewById(R.id.button_add_event);
        addEventButton.setOnClickListener(v -> {
            NavController navController = NavHostFragment.findNavController(EventPageFragment.this);
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
     * Sends a pop to the user asking for notifications permissions
     * only ask once, when the user first gets to this fragment
     */
    private void askNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(requireContext(), android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                requestPermissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS);
            }
        }
    }
}