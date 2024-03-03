package com.example.scanpal;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import com.google.firebase.firestore.FirebaseFirestore;

/**
 * Fragment controlling user sign up screen
 */
public class SignupFragment extends Fragment {
    public SignupFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.signup_page, container, false);

        view.findViewById(R.id.addUserContinue).setOnClickListener(v -> {

            TextView username = view.findViewById(R.id.addUsername);
            TextView firstName = view.findViewById(R.id.addFirstName);
            TextView lastName = view.findViewById(R.id.addLastName);

            Bundle bundle = new Bundle();
            bundle.putString("username", username.getText().toString());
            bundle.putString("firstName", firstName.getText().toString());
            bundle.putString("lastName", lastName.getText().toString());

            new UserController(FirebaseFirestore.getInstance(), getContext()).isUsernameTaken(username.getText().toString(),
                    new UsernameCheckCallback() {

                        @Override
                        public void onUsernameTaken(boolean isTaken) {
                            if (isTaken) {
                                Toast.makeText(view.getContext(), "Username is already taken", Toast.LENGTH_LONG)
                                        .show();
                            } else {
                                NavController navController = NavHostFragment.findNavController(SignupFragment.this);
                                navController.navigate(R.id.addUserContinueAction, bundle);
                            }

                        }

                        @Override
                        public void onError(Exception e) {
                            Toast.makeText(view.getContext(), e.getMessage(), Toast.LENGTH_LONG).show();
                        }

                    });

        });

        return view;
    }
}
