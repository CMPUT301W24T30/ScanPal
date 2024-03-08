package com.example.scanpal;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.clearText;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.intent.Intents.init;
import static androidx.test.espresso.intent.Intents.release;
import static androidx.test.espresso.intent.matcher.IntentMatchers.hasAction;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

import android.app.Activity;
import android.app.Instrumentation;
import android.content.Intent;
import android.net.Uri;

import androidx.fragment.app.testing.FragmentScenario;
import androidx.lifecycle.Lifecycle;
import androidx.test.espresso.Espresso;
import androidx.test.espresso.assertion.ViewAssertions;
import androidx.test.espresso.intent.Intents;
import androidx.test.espresso.matcher.ViewMatchers;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.google.firebase.firestore.FirebaseFirestore;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class ProfileFragmentTest {
    @Rule
    public ActivityScenarioRule<MainActivity> scenario = new ActivityScenarioRule<>(MainActivity.class);

    private final User testUser = new User("test1", "Test1", "Testuser");

    @Before
    public void setUp() {
        init();

        onView(withId(R.id.addUsername)).perform(typeText(testUser.getUsername()), closeSoftKeyboard());
        onView(withId(R.id.addFirstName)).perform(typeText(testUser.getFirstName()), closeSoftKeyboard());
        onView(withId(R.id.addLastName)).perform(typeText(testUser.getLastName()), closeSoftKeyboard());
        onView(withId(R.id.addUserContinue)).perform(click());

        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
            return;
        }

        onView(withId(R.id.createUserButton)).perform(click());
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
            return;
        }
    }

    @After
    public void tearDown() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("Users").document(testUser.getUsername()).delete();
        release();
    }

    /**
     * Test whether current user's info is correctly displayed on ProfileFragment.
     */
    @Test
    public void testProfile() {
        onView(withId(R.id.button_profile)).perform(click());

        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        onView(withId(R.id.addUsername)).check(matches(withText(testUser.getUsername())));
        onView(withId(R.id.first_name)).check(matches(withText(testUser.getFirstName())));
        onView(withId(R.id.last_name)).check(matches(withText(testUser.getLastName())));
    }

}
