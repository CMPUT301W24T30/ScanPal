package com.example.scanpal;

import static androidx.test.espresso.Espresso.onData;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.intent.Intents.init;
import static androidx.test.espresso.intent.Intents.release;
import static androidx.test.espresso.intent.matcher.IntentMatchers.hasAction;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.CoreMatchers.anything;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;

import android.app.Activity;
import android.app.Instrumentation;
import android.content.Intent;
import android.net.Uri;

import androidx.test.espresso.intent.Intents;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class EventDetailsFragmentTest {

    private final User testUser = new User("test1", "Test1", "Testuser", "token");
    @Rule
    public ActivityScenarioRule<MainActivity> scenario = new ActivityScenarioRule<>(MainActivity.class);

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
        }
    }

    @After
    public void tearDown() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("Users").document(testUser.getUsername()).delete();
        release();
    }

    /**
     * Test whether event detail fragment displays the content properly.
     */
    @Test
    public void testEventDetail() {
        onView(withId(R.id.button_add_event)).perform(click());

        String name = "Test Event 1";
        String loc = "Test Location";
        String desc = "Test Description";
        int maxAttendee = 100;
        addEvent(name, loc, desc, maxAttendee);
        onData(is(instanceOf(String.class))).inAdapterView(withId(R.id.event_grid)).atPosition(0).check(matches(withText("Test Event 1")));

        onData(anything()).inAdapterView(withId(R.id.event_grid)).atPosition(0).perform(click());
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        onView(withId(R.id.event_Title)).check(matches(withText(name)));
        onView(withId(R.id.event_Location)).check(matches(withText(loc)));
        onView(withId(R.id.event_orgName)).check(matches(withText(testUser.getFirstName() + " " + testUser.getLastName())));
        onView(withId(R.id.event_description)).check(matches(withText(desc)));

        cleanUp();
    }

    private void addEvent(String name, String loc, String desc, int maxAttendee) {
        Intent resultData = new Intent();
        resultData.setData(Uri.parse("android.resource://com.example.scanpal/" + R.raw.test_image));
        Instrumentation.ActivityResult result = new Instrumentation.ActivityResult(Activity.RESULT_OK, resultData);
        Intents.intending(hasAction(Intent.ACTION_PICK)).respondWith(result);

        onView(withId(R.id.add_edit_event_Name)).perform(typeText(name), closeSoftKeyboard());
        // TODO change the location data to valid one
        onView(withId(R.id.add_edit_event_Location)).perform(typeText(loc), closeSoftKeyboard());
        onView(withId(R.id.add_edit_event_description)).perform(typeText(desc), closeSoftKeyboard());
        onView(withId(R.id.add_edit_event_Attendees)).perform(typeText(String.valueOf(maxAttendee)), closeSoftKeyboard());
        onView(withId(R.id.add_edit_event_imageButton)).perform(click());

        onView(withId(R.id.add_edit_save_button)).perform(click());

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }


    /**
     * Delete all the events from db created by the test user.
     */
    private void cleanUp() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference userRef = db.collection("Users").document(testUser.getUsername());
        db.collection("Events")
                .whereEqualTo("organizer", userRef)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isComplete()) {
                        for (QueryDocumentSnapshot doc : task.getResult()) {
                            db.collection("Events").document(doc.getId()).delete();
                        }
                    }
                });
    }
}
