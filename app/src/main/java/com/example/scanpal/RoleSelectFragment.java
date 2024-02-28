package com.example.scanpal;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;

import com.google.firebase.firestore.FirebaseFirestore;

public class RoleSelectFragment extends Fragment {
    public RoleSelectFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.role_select, container, false);

        assert getArguments() != null;
        String username = getArguments().getString("username", "");
        String firstName = getArguments().getString("firstName", "");
        String lastName = getArguments().getString("lastName", "");

        view.findViewById(R.id.createUserButton).setOnClickListener(v -> {
            new UserController(FirebaseFirestore.getInstance()).addUser(new User(username, firstName, lastName));
        });

        view.findViewById(R.id.createAdminButton).setOnClickListener(v -> {

        });

        return view;
    }
}
