package com.example.scanpal;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

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

        UserController userController = new UserController(FirebaseFirestore.getInstance());

        view.findViewById(R.id.createUserButton).setOnClickListener(userButton -> {

            userController.addUser(new User(username, firstName, lastName), new UserAddCallback() {
                @Override
                public void onSuccess() {
                    NavController navController = NavHostFragment.findNavController(RoleSelectFragment.this);
                    navController.navigate(R.id.createUserCompleted);
                }

                @Override
                public void onError(Exception e) {
                    Toast.makeText(userButton.getContext(), e.getMessage(), Toast.LENGTH_LONG).show();
                }
            });
        });
        return view;
    }
}
