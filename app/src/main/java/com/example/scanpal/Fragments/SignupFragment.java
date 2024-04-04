package com.example.scanpal.Fragments;

import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import com.example.scanpal.Callbacks.UsernameCheckCallback;
import com.example.scanpal.Controllers.UserController;
import com.example.scanpal.R;
import com.github.javafaker.Faker;
import com.google.android.material.textfield.TextInputEditText;

import java.util.Objects;

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

        ConstraintLayout layout = view.findViewById(R.id.constraint_layout);
        AnimationDrawable animationDrawable = (AnimationDrawable) layout.getBackground();
        animationDrawable.setEnterFadeDuration(10);
        animationDrawable.setExitFadeDuration(5000);
        animationDrawable.start();

        ImageView logoImageView = view.findViewById(R.id.logoImageView);
        Animation rotation = AnimationUtils.loadAnimation(getContext(), R.anim.rotate);
        logoImageView.startAnimation(rotation);

        view.findViewById(R.id.addUserContinue).setOnClickListener(v -> {
            TextInputEditText username = view.findViewById(R.id.addUsername);
            TextInputEditText firstName = view.findViewById(R.id.addFirstName);
            TextInputEditText lastName = view.findViewById(R.id.addLastName);

            // Ensure all fields are not null
            if (username != null && firstName != null && lastName != null) {
                String usernameStr = Objects.requireNonNull(username.getText()).toString();
                String firstNameStr = Objects.requireNonNull(firstName.getText()).toString();
                String lastNameStr = Objects.requireNonNull(lastName.getText()).toString();

                // Proceed if none of the fields are empty
                if (!usernameStr.isEmpty() && !firstNameStr.isEmpty() && !lastNameStr.isEmpty()) {
                    Bundle bundle = new Bundle();
                    bundle.putString("username", usernameStr);
                    bundle.putString("firstName", firstNameStr);
                    bundle.putString("lastName", lastNameStr);

                    new UserController(getContext()).isUsernameTaken(usernameStr,
                            new UsernameCheckCallback() {
                                @Override
                                public void onUsernameTaken(boolean isTaken) {
                                    if (isTaken) {
                                        Toast.makeText(view.getContext(), "Username is already taken", Toast.LENGTH_LONG).show();
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
                } else {
                    Toast.makeText(view.getContext(), "Please fill in all fields", Toast.LENGTH_LONG).show();
                }
            }
        });

        generateGuestUser(view);

        return view;
    }

    private void generateGuestUser(View view) {
        view.findViewById(R.id.addUserGuest).setOnClickListener(v -> {

            Faker faker = new Faker();


            String usernameStr = "G_" + faker.name().username();
            String firstNameStr = faker.name().firstName();
            String lastNameStr = faker.name().lastName();

            Bundle bundle = new Bundle();
            bundle.putString("username", usernameStr);
            bundle.putString("firstName", firstNameStr);
            bundle.putString("lastName", lastNameStr);

            new UserController(getContext()).isUsernameTaken(usernameStr,
                    new UsernameCheckCallback() {
                        @Override
                        public void onUsernameTaken(boolean isTaken) {
                            if (isTaken) {
                                generateGuestUser(view);
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
    }
}