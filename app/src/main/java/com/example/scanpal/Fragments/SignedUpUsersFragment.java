package com.example.scanpal.Fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
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
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.materialswitch.MaterialSwitch;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;

/**
 * This fragment displays signed up users for the selected event.
 */
public class SignedUpUsersFragment extends Fragment {

    private RecyclerView.OnItemTouchListener currentTouchListener;
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
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.attendees_list, container, false);
        eventID = requireArguments().getString("event_id");

        attendeeController = new AttendeeController(FirebaseFirestore.getInstance());
        navController = NavHostFragment.findNavController(this);
        backButton = view.findViewById(R.id.button_go_back);
        usersList = view.findViewById(R.id.user_list);
        listSwitch = view.findViewById(R.id.listSwitch1);
        title = view.findViewById(R.id.attendeesList_title);

        usersList.setLayoutManager(new LinearLayoutManager(getContext()));
        usersAdapter = new UsersAdapter(getContext(), new ArrayList<>());
        usersList.setAdapter(usersAdapter);

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setupButtonListeners();
        fetchUsers(false);
    }

    private void setupButtonListeners() {
        backButton.setOnClickListener(v -> navController.navigateUp());
        listSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> fetchUsers(isChecked));
    }

    private void fetchUsers(boolean isChecked) {
        AttendeeSignedUpFetchCallback callback = new AttendeeSignedUpFetchCallback() {
            @Override
            public void onSuccess(ArrayList<Attendee> attendees) {
                usersAdapter.clearUsers();
                for (Attendee attendee : attendees) {
                    usersAdapter.addUser(attendee.getUser());
                    usersAdapter.notifyDataSetChanged();
                }
                title.setText(isChecked ? "Checked-In" : "Signed Up");
                setupTouchListener(attendees);
            }

            @Override
            public void onFailure(Exception e) {
                Log.e("SignedUpUsersFragment", "Error fetching users: " + e.getMessage());
            }
        };

        if (isChecked) {
            attendeeController.fetchCheckedInUsers(eventID, callback);
        } else {
            attendeeController.fetchSignedUpUsers(eventID, callback);
        }
    }

    private void setupTouchListener(ArrayList<Attendee> attendees) {
        if (currentTouchListener != null) {
            usersList.removeOnItemTouchListener(currentTouchListener);
        }
        MaterialAlertDialogBuilder organizerOptions = new MaterialAlertDialogBuilder(requireContext());
        currentTouchListener = new RecyclerView.SimpleOnItemTouchListener() {
            @Override
            public boolean onInterceptTouchEvent(@NonNull RecyclerView rv, @NonNull MotionEvent e) {
                View child = rv.findChildViewUnder(e.getX(), e.getY());
                if (child != null && e.getAction() == MotionEvent.ACTION_UP) {
                    int position = rv.getChildAdapterPosition(child);
                    Attendee attendee = attendees.get(position);
                    organizerOptions.setIcon(R.drawable.onphone);
                    organizerOptions.setTitle(attendee.getUser().getFirstName() + " " + attendee.getUser().getLastName());
                    organizerOptions.setMessage("🔥 Check In Count: " + attendee.getCheckinCount());
                    organizerOptions.show();
                    return true;
                }
                return false;
            }
        };
        usersList.addOnItemTouchListener(currentTouchListener);
    }
}
