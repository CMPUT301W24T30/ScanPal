package com.example.scanpal.Fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.example.scanpal.Callbacks.UserFetchCallback;
import com.example.scanpal.Controllers.UserController;
import com.example.scanpal.Models.User;
import com.example.scanpal.R;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.FirebaseFirestore;

public class BrowseImageFragment extends Fragment {

    /**
     * Default constructor. Initializes the fragment.
     */
    public BrowseImageFragment() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.browse_events, container, false);

        AutoCompleteTextView dropdown = view.findViewById(R.id.browser_select_autocomplete);

        dropdown.setText("Image Browser");

        // Create an ArrayAdapter using the string array and a default dropdown layout.
        ArrayAdapter<CharSequence> adapter = new ArrayAdapter<>(
                requireContext(),
                R.layout.browser_list_item,
                new String[]{"Events Browser", "Image Browser", "Profile Browser"} // Directly input your strings here
        );
        // Apply the adapter to the dropdown.
        dropdown.setAdapter(adapter);

        UserController userController = new UserController(FirebaseFirestore.getInstance(), this.getContext());
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
                        NavHostFragment.findNavController(BrowseImageFragment.this).navigate(R.id.eventsPage);
                        break;
                    case "Image Browser":
                        NavHostFragment.findNavController(BrowseImageFragment.this).navigate(R.id.browseImageFragment);
                        break;
                    case "Profile Browser":
                        NavHostFragment.findNavController(BrowseImageFragment.this).navigate(R.id.browseProfileFragment);
                        break;
                }
            }
        });

        FloatingActionButton addEventButton = view.findViewById(R.id.button_add_event);
        addEventButton.setVisibility(View.INVISIBLE);

        return view;
    }
}
