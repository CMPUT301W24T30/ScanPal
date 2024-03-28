package com.example.scanpal;

import androidx.fragment.app.testing.FragmentScenario;
import androidx.lifecycle.Lifecycle;
import androidx.test.espresso.Espresso;
import androidx.test.espresso.assertion.ViewAssertions;
import androidx.test.espresso.matcher.ViewMatchers;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.example.scanpal.Fragments.ProfileFragment;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class ProfileFragmentTest {

    @Before
    public void setUp() {
        FragmentScenario<ProfileFragment> scenario = FragmentScenario.launchInContainer(ProfileFragment.class);
        scenario.moveToState(Lifecycle.State.STARTED);
    }

    /**
     * Tests the UI components' visibility in the ProfileFragment.
     */
    @Test
    public void testUIVisibility() {
        Espresso.onView(ViewMatchers.withId(R.id.button_go_back)).check(ViewAssertions.matches(ViewMatchers.isDisplayed()));
        Espresso.onView(ViewMatchers.withId(R.id.button_edit_profile)).check(ViewAssertions.matches(ViewMatchers.isDisplayed()));
        Espresso.onView(ViewMatchers.withId(R.id.addUsername)).check(ViewAssertions.matches(ViewMatchers.isDisplayed()));
        Espresso.onView(ViewMatchers.withId(R.id.first_name)).check(ViewAssertions.matches(ViewMatchers.isDisplayed()));
        Espresso.onView(ViewMatchers.withId(R.id.last_name)).check(ViewAssertions.matches(ViewMatchers.isDisplayed()));
        Espresso.onView(ViewMatchers.withId(R.id.homepage)).check(ViewAssertions.matches(ViewMatchers.isDisplayed()));
        Espresso.onView(ViewMatchers.withId(R.id.profile_page_image)).check(ViewAssertions.matches(ViewMatchers.isDisplayed()));
    }
}
