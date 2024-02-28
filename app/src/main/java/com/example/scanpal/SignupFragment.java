package com.example.scanpal;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

public class SignupFragment extends Fragment {
    public SignupFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.signup_page, container, false);

        TextView username = view.findViewById(R.id.addUsername);
        TextView firstName = view.findViewById(R.id.addFirstName);
        TextView lastName = view.findViewById(R.id.addLastName);

        Bundle bundle = new Bundle();
        bundle.putString("username", username.getText().toString());
        bundle.putString("firstName", firstName.getText().toString());
        bundle.putString("lastName", lastName.getText().toString());

        view.findViewById(R.id.addUserContinue).setOnClickListener(v -> {
            NavController navController = NavHostFragment.findNavController(this);
            navController.navigate(R.id.addUserContinueAction, bundle);
        });

        return view;
    }
}
