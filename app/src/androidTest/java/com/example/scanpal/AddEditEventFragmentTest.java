package com.example.scanpal;

import static androidx.test.espresso.Espresso.onData;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.intent.Intents.init;
import static androidx.test.espresso.intent.matcher.IntentMatchers.hasAction;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;

import android.app.Activity;
import android.app.Instrumentation;
import android.content.Intent;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;

import androidx.test.espresso.assertion.ViewAssertions;
import androidx.test.espresso.intent.Intents;
import androidx.test.espresso.matcher.ViewMatchers;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
@LargeTest
public class AddEditEventFragmentTest {

    @Rule
    public ActivityScenarioRule<MainActivity> scenario = new ActivityScenarioRule<>(MainActivity.class);

    private final User testUser = new User("test1", "Test1", "Testuser");

    @Before
    public void setUp() {
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


    }

    @Test
    public void testAddEvent() {
        init();
        Intent resultData = new Intent();
        resultData.setData(Uri.parse("android.resource://com.example.scanpal/" + R.raw.test_image));
        Instrumentation.ActivityResult result = new Instrumentation.ActivityResult(Activity.RESULT_OK, resultData);
        Intents.intending(hasAction(Intent.ACTION_PICK)).respondWith(result);

        onView(withId(R.id.button_add_event)).perform(click());
        onView(withId(R.id.add_edit_event_Name)).perform(typeText("Test Event 1"), closeSoftKeyboard());
        // TODO change the location data to valid one
        onView(withId(R.id.add_edit_event_Location)).perform(typeText("Test Location"), closeSoftKeyboard());
        onView(withId(R.id.add_edit_event_description)).perform(typeText("Test Description"), closeSoftKeyboard());
        onView(withId(R.id.add_edit_event_Attendees)).perform(typeText("100"), closeSoftKeyboard());
        onView(withId(R.id.add_edit_event_imageButton)).perform(click());

        onView(withId(R.id.add_edit_save_button)).perform(click());

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        onData(is(instanceOf(String.class))).inAdapterView(withId(R.id.event_List)).atPosition(0).check(matches(withText("Test Event 1")));
    }

    /**
     * Tests the UI components' visibility in the AddEditEventFragment.
     */
    @Test
    public void testUIVisibility() {
        onView(withId(R.id.add_edit_event_ImageView)).check(matches(ViewMatchers.isDisplayed()));
        onView(withId(R.id.add_edit_backButton)).check(matches(ViewMatchers.isDisplayed()));
        onView(withId(R.id.add_edit_event_Header)).check(matches(ViewMatchers.isDisplayed()));
        onView(withId(R.id.add_edit_save_button)).check(matches(ViewMatchers.isDisplayed()));
        onView(withId(R.id.add_edit_event_imageButton)).check(matches(ViewMatchers.isDisplayed()));
        onView(withId(R.id.max_attendees)).check(matches(ViewMatchers.isDisplayed()));
        onView(withId(R.id.add_edit_event_Attendees)).check(matches(ViewMatchers.isDisplayed()));
        onView(withId(R.id.event_name_title)).check(matches(ViewMatchers.isDisplayed()));
        onView(withId(R.id.event_location_title)).check(matches(ViewMatchers.isDisplayed()));
        onView(withId(R.id.event_description_title)).check(matches(ViewMatchers.isDisplayed()));
        onView(withId(R.id.add_edit_deleteButton)).check(matches(ViewMatchers.isDisplayed()));
    }
}