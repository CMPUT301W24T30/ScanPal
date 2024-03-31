package com.example.scanpal.Fragments;

import android.content.pm.PackageManager;
import android.media.Image;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
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
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import com.example.scanpal.Adapters.EventGridAdapter;
import com.example.scanpal.Adapters.ImageGridAdapter;
import com.example.scanpal.Adapters.ProfileGridAdapter;
import com.example.scanpal.Callbacks.EventsFetchCallback;
import com.example.scanpal.Callbacks.UserFetchCallback;
import com.example.scanpal.Callbacks.UserSignedUpCallback;
import com.example.scanpal.Callbacks.UsersFetchCallback;
import com.example.scanpal.Callbacks.UserUpdateCallback;
import com.example.scanpal.Controllers.EventController;
import com.example.scanpal.Controllers.ImageController;
import com.example.scanpal.Controllers.UserController;
import com.example.scanpal.Models.Event;
import com.example.scanpal.Models.User;
import com.example.scanpal.R;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import android.Manifest;

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

    private final ActivityResultLauncher<String> locationPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    logUserLocation();
                    askNotificationPermission();
                } else {
                    Toast.makeText(getContext(), "Location access is required to use this feature.", Toast.LENGTH_LONG).show();
                }
            });
    protected List<Event> eventsList = new ArrayList<>();
    protected List<Event> allEvents = new ArrayList<>();
    protected List<User> allUsers = new ArrayList<>();
    protected List<Image> allImages = new ArrayList<>();
    private EventGridAdapter eventGridAdapter;
    private ProfileGridAdapter profileGridAdapter;
    private ImageGridAdapter imageGridAdapter;
    private EventController eventController;
    private FusedLocationProviderClient fusedLocationClient;
    private ImageController imageController;
    private UserController userController;

    /**
     * Default constructor. Initializes the fragment.
     */
    public BrowseEventFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(getActivity());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(getActivity());
        View view = inflater.inflate(R.layout.browse_events, container, false);

        eventGridAdapter = new EventGridAdapter(getContext());
        GridView gridView = view.findViewById(R.id.event_grid);
        gridView.setAdapter(eventGridAdapter);

        // init eventController
        eventController = new EventController();
        userController = new UserController(this.getContext());
        imageController = new ImageController();

        AutoCompleteTextView dropdown = view.findViewById(R.id.browser_select_autocomplete);

        dropdown.setText("Events Browser");
        fetchAllEvents();


        // Create an ArrayAdapter using the string array and a default dropdown layout.
        ArrayAdapter<CharSequence> adapter = new ArrayAdapter<>(
                requireContext(),
                R.layout.browser_list_item,
                new String[]{"Events Browser", "Image Browser", "Profile Browser"} // Directly input your strings here
        );
        // Apply the adapter to the dropdown.
        dropdown.setAdapter(adapter);

        UserController userController = new UserController(this.getContext());
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
                        fetchAllEvents();
                        break;
                    case "Image Browser":
                        //setImages();
                        break;
                    case "Profile Browser":
                        //setProfiles();
                        break;
                }
            }
        });



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

        askLocationPermissionAndLogLocation();

        return view;
    }

    /**
     * Fetches all events from the data source and updates the grid view accordingly.
     * Applies filters based on user sign-up status and sorts events.
     */
    private void fetchAllEvents() {
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
                            eventGridAdapter.setEvents(allEvents);
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

    private void askLocationPermissionAndLogLocation() {
        if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION);
        } else {
            logUserLocation();
        }
    }

    private void logUserLocation() {
        FusedLocationProviderClient fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity());

        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.getLastLocation().addOnSuccessListener(requireActivity(), location -> {
                if (location != null) {
                    String locationStr = location.getLatitude() + "," + location.getLongitude();

                    // Fetch current user's username
                    UserController userController = new UserController(FirebaseFirestore.getInstance(), getContext());
                    String currentUsername = userController.fetchStoredUsername();

                    // Update user location
                    if (currentUsername != null) {
                        userController.updateUserLocation(currentUsername, locationStr, new UserUpdateCallback() {
                            @Override
                            public void onSuccess() {
                                Log.d("EventPageFragment", "User location updated successfully");
                            }

                            @Override
                            public void onError(Exception e) {
                                Log.e("EventPageFragment", "Failed to update user location", e);
                            }
                        });
                    }
                } else {
                    Log.d("EventPageFragment", "No location detected.");
                }
            });
        }
    }



    private void askLocationPermissionAndLogLocation() {
        if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION);
        } else {
            logUserLocation();
        }
    }

    private void logUserLocation() {
        FusedLocationProviderClient fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity());

        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.getLastLocation().addOnSuccessListener(requireActivity(), location -> {
                if (location != null) {
                    String locationStr = location.getLatitude() + "," + location.getLongitude();

                    // Fetch current user's username
                    UserController userController = new UserController(FirebaseFirestore.getInstance(), getContext());
                    String currentUsername = userController.fetchStoredUsername();

                    // Update user location
                    if (currentUsername != null) {
                        userController.updateUserLocation(currentUsername, locationStr, new UserUpdateCallback() {
                            @Override
                            public void onSuccess() {
                                Log.d("EventPageFragment", "User location updated successfully");
                            }

                            @Override
                            public void onError(Exception e) {
                                Log.e("EventPageFragment", "Failed to update user location", e);
                            }
                        });
                    }
                } else {
                    Log.d("EventPageFragment", "No location detected.");
                }
            });
        }
    }



    private void fetchAllUsers() {
        userController.fetchAllUsers(new UsersFetchCallback() {
            @Override
            public void onSuccess(List<User> user) {
                // Do something with the user
                allUsers.clear();
                allUsers.addAll(user);
                profileGridAdapter.setUsers(allUsers);
            }

            @Override
            public void onError(Exception e) {
                Toast.makeText(getContext(), "Error fetching all events.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void fetchAllImages() {

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