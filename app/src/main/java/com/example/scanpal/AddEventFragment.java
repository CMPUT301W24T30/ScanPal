package com.example.scanpal;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import com.google.firebase.firestore.FirebaseFirestore;

public class AddEventFragment extends Fragment {
    Button saveButton;
    EditText attendeesForm;
    EditText eventNameForm;
    EditText eventLocationForm;
    EditText eventDescriptionForm;
    Event newEvent;
    EventController eventController;
    UserController userController;

    User Organizer;//change later to probably doc reference


    public AddEventFragment() {

    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.add_edit_event, null, false);

        TextView pageHeader = view.findViewById(R.id.add_edit_event_Header);
        pageHeader.setText("Create Event");


         this.saveButton = view.findViewById(R.id.add_edit_save_button);
         this.attendeesForm = view.findViewById(R.id.add_edit_event_Attendees);
         this.eventNameForm = view.findViewById(R.id.add_edit_event_Name);
         this.eventLocationForm = view.findViewById(R.id.add_edit_event_Location);
         this.eventDescriptionForm = view.findViewById(R.id.add_edit_event_description);


         userController = new UserController(FirebaseFirestore.getInstance(), view.getContext());
         eventController = new EventController();

        //first somehow get an instance of the user

        userController.getUser(userController.fetchStoredUsername(), new UserFetchCallback() {
            @Override
            public void onSuccess(User user) {
                Organizer = user;
                //log
            }

            @Override
            public void onError(Exception e) {
                Toast.makeText(view.getContext(), "Failed to fetch User", Toast.LENGTH_LONG).show();
            }
        });

        this.newEvent = new Event(this.Organizer,"","");//blank event for now until user clicks 'save'



        //log test
        Log.d("STORAGE", userController.fetchStoredUsername());

        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
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

                    //add: stuff with photos and QR

                    //now add the event to the database
                    eventController.addEvent(newEvent);

                    NavController navController = NavHostFragment.findNavController(AddEventFragment.this);
                    navController.navigate(R.id.addEditEventComplete);
                }

            }
        });

        return view;
    }

}
