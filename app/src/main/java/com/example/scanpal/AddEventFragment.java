package com.example.scanpal;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.FirebaseFirestore;

/**
 * A Fragment for adding new events to the Firestore database. It allows users to input
 * details about a new event and save it. This class manages the UI for creating an event
 * and interacts with Firestore through {@link EventController} to store the event data.
 */
public class AddEventFragment extends Fragment {
    Button saveButton;
    FloatingActionButton backButton;
    Button deleteButton;
    Button editImageButton;
    EditText attendeesForm;
    EditText eventNameForm;
    EditText eventLocationForm;
    EditText eventDescriptionForm;
    Event newEvent;
    EventController eventController;
    UserController userController;
    User Organizer;
    private Uri imageUri;
    private ImageView profileImageView;
    private ImageController imageController;

    private final ActivityResultLauncher<Intent> pickImageLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    imageUri = result.getData().getData();
                    profileImageView.setImageURI(imageUri);
                    uploadImageToFirebase(imageUri);
                }
            });


    public AddEventFragment() {
        // Required empty public constructor
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.add_edit_event, null, false);
        ((MainActivity) requireActivity()).setNavbarVisibility(false);

        TextView pageHeader = view.findViewById(R.id.add_edit_event_Header);
        pageHeader.setText("Create Event");

        this.deleteButton = view.findViewById(R.id.add_edit_deleteButton);
        this.deleteButton.setVisibility(View.GONE);


        this.saveButton = view.findViewById(R.id.add_edit_save_button);
        this.backButton = view.findViewById(R.id.add_edit_backButton);
        this.editImageButton = view.findViewById(R.id.add_edit_event_imageButton);
        this.attendeesForm = view.findViewById(R.id.add_edit_event_Attendees);
        this.eventNameForm = view.findViewById(R.id.add_edit_event_Name);
        this.eventLocationForm = view.findViewById(R.id.add_edit_event_Location);
        this.eventDescriptionForm = view.findViewById(R.id.add_edit_event_description);
        this.profileImageView = view.findViewById(R.id.add_edit_event_ImageView);

        imageController = new ImageController();
        userController = new UserController(FirebaseFirestore.getInstance(), view.getContext());
        eventController = new EventController();

        userController.getUser(userController.fetchStoredUsername(), new UserFetchCallback() {
            @Override
            public void onSuccess(User user) {
                Organizer = user;
            }

            @Override
            public void onError(Exception e) {
            }
        });

        this.newEvent = new Event(this.Organizer, "", "");

        saveButton.setOnClickListener(v -> {

            //checking for valid input
            if (eventNameForm.getText().toString().isEmpty() ||
                    eventLocationForm.getText().toString().isEmpty() ||
                    eventDescriptionForm.getText().toString().isEmpty() ||
                    attendeesForm.getText().toString().isEmpty() ||
                    null == imageUri) {

                Toast.makeText(view.getContext(), "Please input all Information", Toast.LENGTH_LONG).show();

            } else if (Integer.parseInt(attendeesForm.getText().toString()) < 1) {
                Toast.makeText(view.getContext(), "Please allow at least 1 Attendee", Toast.LENGTH_LONG).show();
            } else {
                newEvent.setName(eventNameForm.getText().toString());
                newEvent.setLocation(eventLocationForm.getText().toString());
                newEvent.setDescription(eventDescriptionForm.getText().toString());
                newEvent.setMaximumAttendees(Integer.parseInt(attendeesForm.getText().toString()));
                newEvent.setPosterURI(imageUri);
                newEvent.setAnnouncementCount(0L);

                eventController.addEvent(newEvent);

                NavController navController = NavHostFragment.findNavController(AddEventFragment.this);
                navController.navigate(R.id.addEditEventComplete);
                ((MainActivity) requireActivity()).setNavbarVisibility(true);
            }
        });

        backButton.setOnClickListener(v -> {
            NavController navController = NavHostFragment.findNavController(AddEventFragment.this);
            navController.navigate(R.id.addEditEventComplete);
            ((MainActivity) requireActivity()).setNavbarVisibility(true);
        });

        editImageButton.setOnClickListener(v -> openGallery());

        return view;
    }

    /**
     * Opens the device's gallery for the user to pick a new profile image.
     */
    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        pickImageLauncher.launch(intent);
    }

    /**
     * Uploads the selected image to Firebase Storage, specifying a folder and filename.
     *
     * @param imageUri The URI of the image selected by the user.
     */
    private void uploadImageToFirebase(Uri imageUri) {
        String folderPath = "events";
        String fileName = "event_" + newEvent.getId() + ".jpg";

        imageController.uploadImage(imageUri, folderPath, fileName,
                uri -> System.out.println("Success"),
                e -> Log.e("AddEventFragment", "Upload failed: " + e.getMessage()));
    }
}
