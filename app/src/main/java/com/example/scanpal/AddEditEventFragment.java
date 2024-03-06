package com.example.scanpal;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.firestore.FirebaseFirestore;

/**
 * Fragment for adding/editing an event
 */
public class AddEditEventFragment extends Fragment {

    TextInputEditText eventNameEditText;
    TextInputEditText eventDescriptionEditText;
    Button saveButton;

    UserController userController;
    EventController eventController;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.add_edit_event, container, false);
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        userController = new UserController(db, view.getContext());
        eventController = new EventController();
        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {/*
        eventNameEditText = view.findViewById(R.id.event_name_edittext);
        eventDescriptionEditText = view.findViewById(R.id.event_description_edittext);
        saveButton = view.findViewById(R.id.save_button);

        saveButton.setOnClickListener(v -> {

            String eventName = eventNameEditText.getText().toString();
            String eventDescription = eventDescriptionEditText.getText().toString();

            if (eventName.isEmpty()) {
                Toast.makeText(view.getContext(), "Event name cannot be empty", Toast.LENGTH_LONG)
                        .show();
                return;
            }

            if (eventDescription.isEmpty()) {
                Toast.makeText(view.getContext(), "Event description cannot be empty", Toast.LENGTH_LONG)
                        .show();
                return;
            }
            Log.d("AddEditEventFragment", userController.fetchStoredUsername());
            userController.getUser(userController.fetchStoredUsername(), new UserFetchCallback() {
                @Override
                public void onSuccess(User user) {
                    Event event = new Event(user, eventName, eventDescription);
                    eventController.addEvent(event);
                    Log.d("AddEditEventFragment", "User successfully fetched");
                }

                @Override
                public void onError(Exception e) {
                    Toast.makeText(view.getContext(), "Failed to add event", Toast.LENGTH_LONG).show();
                    Log.e("AddEditEventFragment", "Failed to fetch user " + e.getMessage());
                }
            });
        });

*/
    }
}
