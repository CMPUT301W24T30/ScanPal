package com.example.scanpal.Fragments;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.GridView;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import com.example.scanpal.Adapters.EventGridAdapter;
import com.example.scanpal.Adapters.ImageGridAdapter;
import com.example.scanpal.Adapters.ProfileGridAdapter;
import com.example.scanpal.Callbacks.EventsFetchCallback;
import com.example.scanpal.Callbacks.ImagesDeleteCallback;
import com.example.scanpal.Callbacks.ImagesFetchCallback;
import com.example.scanpal.Callbacks.UserFetchCallback;
import com.example.scanpal.Callbacks.UserSignedUpCallback;
import com.example.scanpal.Callbacks.UsersFetchCallback;
import com.example.scanpal.Controllers.EventController;
import com.example.scanpal.Controllers.ImageController;
import com.example.scanpal.Controllers.UserController;
import com.example.scanpal.Models.Event;
import com.example.scanpal.Models.User;
import com.example.scanpal.R;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

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

    protected List<Event> allEvents = new ArrayList<>();
    protected List<User> allUsers = new ArrayList<>();
    protected List<String> allImages = new ArrayList<>();
    private EventGridAdapter eventGridAdapter;
    private ProfileGridAdapter profileGridAdapter;
    private ImageGridAdapter imageGridAdapter;
    private EventController eventController;
    private ImageController imageController;
    private UserController userController;

    private GridView gridView;
    private int selectedImage;

    //Specifically put here because its the first fragments the user goes to after creation

    private final BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String message = intent.getStringExtra("message");
            Log.d("msg", "broadcast received MAIN: ");

            showCustomPopup(context,message);
        }
    };

    /**
     * Default constructor. Initializes the fragment.
     */
    public BrowseEventFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.browse_events, container, false);
        askNotificationPermission();

        eventGridAdapter = new EventGridAdapter(getContext());
        profileGridAdapter = new ProfileGridAdapter(getContext());
        imageGridAdapter = new ImageGridAdapter(getContext());

        gridView = view.findViewById(R.id.event_grid);
        gridView.setAdapter(eventGridAdapter);

        // init eventController
        eventController = new EventController();
        userController = new UserController(this.getContext());
        imageController = new ImageController();

        AutoCompleteTextView dropdown = view.findViewById(R.id.browser_select_autocomplete);
        ((TextView) view.findViewById(R.id.event_page_title)).setText("Events Browser");
        dropdown.setText("Events Browser");
        fetchAllEvents();

        //receiver notif stuff
        IntentFilter filter = new IntentFilter("com.example.scanpal.MESSAGE_RECEIVED");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requireActivity().registerReceiver(receiver, filter,Context.RECEIVER_EXPORTED);
        }


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

        dropdown.setOnItemClickListener((parent, view12, position, id) -> {
            // Similar handling as above
            String selectedItem = (String) parent.getItemAtPosition(position);
            switch (selectedItem) {
                case "Events Browser":
                    fetchAllEvents();
                    ((TextView) view.findViewById(R.id.event_page_title)).setText("Events Browser");
                    break;
                case "Image Browser":
                    fetchAllImages();
                    ((TextView) view.findViewById(R.id.event_page_title)).setText("Image Browser");
                    break;
                case "Profile Browser":
                    fetchAllUsers();
                    ((TextView) view.findViewById(R.id.event_page_title)).setText("Profile Browser");
                    break;
            }
        });

        // Set up button to add new events.
        FloatingActionButton addEventButton = view.findViewById(R.id.button_add_event);
        addEventButton.setOnClickListener(v -> {
            NavController navController = NavHostFragment.findNavController(BrowseEventFragment.this);
            navController.navigate(R.id.addEditEvent);
        });

        gridView.setOnItemClickListener((parent, view1, position, id) -> {
            Event event = allEvents.get(position);
            Bundle bundle = new Bundle();
            bundle.putString("event_id", event.getId());
            NavHostFragment.findNavController(this).navigate(R.id.select_event, bundle);
        });

        askNotificationPermission();

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

                gridView.setAdapter(eventGridAdapter);

                gridView.setOnItemClickListener((parent, view1, position, id) -> {
                    Event event = allEvents.get(position);
                    Bundle bundle = new Bundle();
                    bundle.putString("event_id", event.getId());
                    NavHostFragment.findNavController(BrowseEventFragment.this).navigate(R.id.select_event, bundle);
                });

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


    /**
     * Fetches all users from the user controller and updates the UI accordingly.
     * This method fetches all users using the user controller and updates the UI with the retrieved users.
     * It sets the fetched users to the profile grid adapter and sets the adapter to the grid view.
     * Additionally, it handles item click events to navigate to the profile fragment when a user is clicked.
     *
     */

    private void fetchAllUsers() {
        userController.fetchAllUsers(new UsersFetchCallback() {
            @Override
            public void onSuccess(List<User> users) {
                // Do something with the user
                allUsers.clear();
                allUsers.addAll(users);
                profileGridAdapter.setUsers(allUsers);

                gridView.setAdapter(profileGridAdapter);

                gridView.setOnItemClickListener((parent, view1, position, id) -> {
                    User user = allUsers.get(position);
                    Bundle bundle = new Bundle();
                    bundle.putString("username", user.getUsername());
                    NavHostFragment.findNavController(BrowseEventFragment.this).navigate(R.id.profile_fragment, bundle);
                });
            }

            @Override
            public void onError(Exception e) {
                Toast.makeText(getContext(), "Error fetching all events.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Fetches all images from the image controller and updates the UI accordingly.
     * This method fetches all images using the image controller and updates the UI with the retrieved images.
     * It sets the fetched images to the image grid adapter and sets the adapter to the grid view.
     * Additionally, it handles item click events to show delete confirmation when an image is clicked.
     */
    private void fetchAllImages() {
        imageController.fetchAllImages(new ImagesFetchCallback() {
            @Override
            public void onSuccess(List<String> images) {
                // Do something with the user
                allImages.clear();
                allImages.addAll(images);
                imageGridAdapter.setImages(allImages);

                gridView.setAdapter(imageGridAdapter);

                gridView.setOnItemClickListener((parent, view1, position, id) -> {
                    selectedImage = position;
                    showDeleteConfirmation();
                });
            }

            @Override
            public void onError(Exception e) {
                Toast.makeText(getContext(), "Error fetching all events.", Toast.LENGTH_SHORT).show();
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

    /**
     * Shows a confirmation dialog to confirm user deletion.
     */
    private void showDeleteConfirmation() {
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Delete Image?")
                .setMessage("Are you sure you want to delete this image? This action cannot be undone.")
                .setPositiveButton("Delete", (dialog, which) -> deleteImage())
                .setNegativeButton("Cancel", null)
                .setIcon(R.drawable.danger_icon)
                .show();
    }

    /**
     * Deletes the selected image.
     * This method deletes the image selected by the user. It retrieves the image to be deleted from the list of all images.
     * After successful deletion, it displays a toast message indicating successful deletion and refreshes the list of images.
     * If an error occurs during deletion, it logs the error message and displays a toast message indicating the failure.
     *
     */

    private void deleteImage() {
        String image = allImages.get(selectedImage);
        imageController.deleteImage(image, new ImagesDeleteCallback() {
            @Override
            public void onSuccess() {
                Toast.makeText(getContext(), "Image deleted successfully.", Toast.LENGTH_SHORT).show();
                fetchAllImages();
            }

            @Override
            public void onError(Exception e) {
                System.out.println(e.toString());
                Toast.makeText(getContext(), "Failed to delete image." + e, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void showCustomPopup(Context context, String message) {

        View customView = LayoutInflater.from(context).inflate(R.layout.notif_popup, null);
        TextView textView = customView.findViewById(R.id.popup_text);
        textView.setText(message);


        // Create and show the pop-up window
        PopupWindow popupWindow = new PopupWindow(customView, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        popupWindow.showAtLocation(customView, android.view.Gravity.TOP | android.view.Gravity.CENTER_HORIZONTAL, 0, 100);
        //popupWindow.showAsDropDown(customView);

        // Dismiss the pop-up window after a certain duration
        customView.postDelayed(popupWindow::dismiss, 3000);

    }
}