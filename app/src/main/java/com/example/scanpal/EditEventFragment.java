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

import com.bumptech.glide.Glide;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.FirebaseFirestore;

public class EditEventFragment extends Fragment {

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
                    Log.d("IMAGEURI", imageUri.toString());
                    uploadImageToFirebase(imageUri);
                }
            });

    public EditEventFragment() {


    }



    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.add_edit_event, null, false);

        //Fragment UI
        TextView pageHeader = view.findViewById(R.id.add_edit_event_Header);
        pageHeader.setText("Edit Event");
        Log.d("Editing Existing Event", "Edit Details");

        //getting an instance of the currentUser
        userController = new UserController(FirebaseFirestore.getInstance(), view.getContext());
        eventController = new EventController();

        //TODO: Remove the delete button from page when creating event
        //this.deleteButton = view.findViewById(R.id.add_edit_deleteButton);
        //this.deleteButton.setVisibility(View.GONE); // no need for delete button when creating an event


        this.saveButton = view.findViewById(R.id.add_edit_save_button);
        this.backButton = view.findViewById(R.id.add_edit_backButton);
        this.editImageButton = view.findViewById(R.id.add_edit_event_imageButton);
        this.attendeesForm = view.findViewById(R.id.add_edit_event_Attendees);
        this.eventNameForm = view.findViewById(R.id.add_edit_event_Name);
        this.eventLocationForm = view.findViewById(R.id.add_edit_event_Location);
        this.eventDescriptionForm = view.findViewById(R.id.add_edit_event_description);
        this.profileImageView = view.findViewById(R.id.add_edit_event_ImageView);

        imageController = new ImageController();

        String eventID =  getArguments().getString("0");


        /*
        bundle.putString("0", eventID);
        bundle.putString("1", eventName);
        bundle.putString("2", eventLocation);
        bundle.putString("3", eventDescription);
        bundle.putString("4", ImageURI);
        */

        //prefill forms with existing data
        this.eventNameForm.setText(getArguments().getString("1"));
        this.eventLocationForm.setText(getArguments().getString("2"));
        this.eventDescriptionForm.setText(getArguments().getString("3"));

        //profileImageView.setImageURI();



        userController.getUser(userController.fetchStoredUsername(), new UserFetchCallback() {
            @Override
            public void onSuccess(User user) {
                Organizer = user;
            }

            @Override
            public void onError(Exception e) {
                Toast.makeText(view.getContext(), "Failed to fetch User Data", Toast.LENGTH_LONG).show();

                //TODO: probably should navigate back to the events page then

            }
        });

        eventController.getEventById(eventID, Organizer, new EventFetchCallback() {
            @Override
            public void onSuccess(Event event) {
                if (event.getPosterURI() != null) {
                    Glide.with(EditEventFragment.this).load(event.getPosterURI()).into(profileImageView);
                }
            }

            @Override
            public void onError(Exception e) {

            }
        });

        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //checking for valid input
                if(eventNameForm.getText().toString().equals("") ||
                        eventLocationForm.getText().toString().equals("") ||
                        eventDescriptionForm.getText().toString().equals("") ||
                        attendeesForm.getText().toString().equals("") ) {

                    Toast.makeText(view.getContext(), "Please input all Information", Toast.LENGTH_LONG).show();

                }
                else if( Integer.parseInt( attendeesForm.getText().toString()) < 1 )  {
                    Toast.makeText(view.getContext(), "Please allow at least 1 Attendee", Toast.LENGTH_LONG).show();
                }
                else {
                    newEvent.setName(eventNameForm.getText().toString());
                    newEvent.setLocation(eventLocationForm.getText().toString());
                    newEvent.setDescription(eventDescriptionForm.getText().toString());
                    newEvent.setMaximumAttendees( Integer.parseInt(attendeesForm.getText().toString()));
                    newEvent.setPosterURI(imageUri);

                    //TODO: stuff with adding/uploading photos and QR

                    //now add the new event to the database
                    //TODO: Some type of edit event method
                    //eventController.addEvent(newEvent);
                    eventController.editEventById(eventID,newEvent);

                    Bundle bundle = new Bundle();
                    bundle.putString("0",eventID);

                    NavController navController = NavHostFragment.findNavController(EditEventFragment.this);
                    navController.navigate(R.id.done_editingEvent, bundle);
                }

            }
        });

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //just go back to the previous screen without doing anything
                Bundle bundle = new Bundle();
                bundle.putString("0",eventID);
                NavController navController = NavHostFragment.findNavController(EditEventFragment.this);
                navController.navigate(R.id.done_editingEvent,bundle);
            }
        });

        editImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // what to do when trying to edit image
                openGallery();
            }
        });

        this.newEvent = new Event(this.Organizer,"","");//blank event for now until user clicks 'save'

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
     * Uploads the selected image to Firebase Storage and updates the user's profile image URL.
     *
     * @param imageUri The URI of the image selected by the user.
     */
    private void uploadImageToFirebase(Uri imageUri) {
        imageController.uploadImage(imageUri,
                taskSnapshot -> Toast.makeText(getContext(), "Image Uploaded Successfully", Toast.LENGTH_SHORT).show(),
                e -> Toast.makeText(getContext(), "Upload failed: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }
}
