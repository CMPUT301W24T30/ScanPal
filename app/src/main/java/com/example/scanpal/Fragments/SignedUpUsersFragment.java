package com.example.scanpal.Fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.scanpal.Adapters.UsersAdapter;
import com.example.scanpal.Callbacks.AttendeeSignedUpFetchCallback;
import com.example.scanpal.Controllers.AttendeeController;
import com.example.scanpal.Models.Attendee;
import com.example.scanpal.R;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.materialswitch.MaterialSwitch;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;

/**
 * This fragment displays signed up users for the selected event.
 */
public class SignedUpUsersFragment extends Fragment {

    private AttendeeController attendeeController;
    private NavController navController;
    private String eventID;
    private FloatingActionButton backButton;
    private MaterialSwitch listSwitch;
    private RecyclerView usersList;
    private TextView title;
    private UsersAdapter usersAdapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.attendees_list, container, false);
        Bundle bundle = getArguments();
        if (bundle != null) {
            eventID = bundle.getString("event_id");
            Log.d("SignedUpUsersFragment", eventID);
        }
        attendeeController = new AttendeeController(FirebaseFirestore.getInstance());
        navController = NavHostFragment.findNavController(this);

        backButton = view.findViewById(R.id.button_go_back);
        usersList = view.findViewById(R.id.user_list);
        usersList.setLayoutManager(new LinearLayoutManager(getContext()));
        usersAdapter = new UsersAdapter(getContext(), new ArrayList<>());
        usersList.setAdapter(usersAdapter);
        listSwitch = view.findViewById(R.id.listSwitch1);
        title = view.findViewById(R.id.attendeesList_title);

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);


        attendeeController.fetchSignedUpUsers(eventID, new AttendeeSignedUpFetchCallback() {
            @Override
            public void onSuccess(ArrayList<Attendee> attendees) {
                // upcast to User
                usersAdapter = new UsersAdapter(getContext(), new ArrayList<>());//to empty it(bless the garbage collector)
                usersList.setAdapter(usersAdapter);

                title.setText("Signed Up");

                for (Attendee attendee : attendees) {
                    usersAdapter.addUser(attendee.getUser());
                }
            }

            @Override
            public void onFailure(Exception e) {
                Log.e("EventDetailsFragment", e.getMessage());
            }
        });

        backButton.setOnClickListener(v -> navController.navigateUp());

        listSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    //listSwitch.setChecked(false);


                    attendeeController.fetchCheckedInUsers(eventID, new AttendeeSignedUpFetchCallback() {
                        @Override
                        public void onSuccess(ArrayList<Attendee> attendees) {
                            usersAdapter = new UsersAdapter(getContext(), new ArrayList<>());//to empty it(bless the garbage collector)
                            usersList.setAdapter(usersAdapter);

                            title.setText("Checked-In");

                            for (Attendee attendee : attendees) {
                                usersAdapter.addUser(attendee.getUser());
                                usersAdapter.notifyDataSetChanged();
                            }
                        }

                        @Override
                        public void onFailure(Exception e) {

                        }
                    });

                } else {
                    //listSwitch.setChecked(true);

                    attendeeController.fetchSignedUpUsers(eventID, new AttendeeSignedUpFetchCallback() {
                        @Override
                        public void onSuccess(ArrayList<Attendee> attendees) {
                            // upcast to User
                            usersAdapter = new UsersAdapter(getContext(), new ArrayList<>());//to empty it(bless the garbage collector)
                            usersList.setAdapter(usersAdapter);

                            title.setText("Signed Up");

                            for (Attendee attendee : attendees) {
                                usersAdapter.addUser(attendee.getUser());
                                usersAdapter.notifyDataSetChanged();
                            }
                        }

                        @Override
                        public void onFailure(Exception e) {
                            Log.e("EventDetailsFragment", e.getMessage());
                        }
                    });

                }
            }
        });


    }
}
