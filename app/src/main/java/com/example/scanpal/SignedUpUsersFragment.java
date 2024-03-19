package com.example.scanpal;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
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
    private RecyclerView usersList;
    private UsersAdapter usersAdapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.attendees_list, null, false);
        Bundle bundle = getArguments();
        if (bundle != null) {
            eventID = bundle.getString("eventID");
            Log.d("SignedUpUsersFragment", eventID);
        }
        attendeeController = new AttendeeController(FirebaseFirestore.getInstance(), getContext());
        navController = NavHostFragment.findNavController(this);

        backButton = view.findViewById(R.id.button_go_back);
        usersList = view.findViewById(R.id.user_list);
        usersList.setLayoutManager(new LinearLayoutManager(getContext()));
        usersAdapter = new UsersAdapter(getContext(), new ArrayList<>());
        usersList.setAdapter(usersAdapter);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        attendeeController.fetchSignedUpUsers(eventID, new AttendeeSignedUpFetchCallback() {
            @Override
            public void onSuccess(ArrayList<Attendee> attendees) {
                // upcast to User
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
    }
}
