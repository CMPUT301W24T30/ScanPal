package com.example.scanpal;

import android.os.Bundle;

import androidx.fragment.app.testing.FragmentScenario;
import androidx.lifecycle.Lifecycle;
import androidx.test.espresso.Espresso;
import androidx.test.espresso.assertion.ViewAssertions;
import androidx.test.espresso.matcher.ViewMatchers;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class RoleSelectFragmentTest {

    @Before
    public void setUp() {
        Bundle args = new Bundle();
        args.putString("test", "test");
        FragmentScenario<RoleSelectFragment> scenario = FragmentScenario.launchInContainer(RoleSelectFragment.class, args);
        scenario.moveToState(Lifecycle.State.STARTED);
    }

    /**
     * Tests the UI components' visibility in the RoleSelectFragment.
     */
    @Test
    public void testUIVisibility() {
        Espresso.onView(ViewMatchers.withId(R.id.admin_imageview)).check(ViewAssertions.matches(ViewMatchers.isDisplayed()));
        Espresso.onView(ViewMatchers.withId(R.id.user_imageview)).check(ViewAssertions.matches(ViewMatchers.isDisplayed()));
        Espresso.onView(ViewMatchers.withId(R.id.adminDefinition)).check(ViewAssertions.matches(ViewMatchers.isDisplayed()));
        Espresso.onView(ViewMatchers.withId(R.id.userDefinition)).check(ViewAssertions.matches(ViewMatchers.isDisplayed()));
        Espresso.onView(ViewMatchers.withId(R.id.userText)).check(ViewAssertions.matches(ViewMatchers.isDisplayed()));
        Espresso.onView(ViewMatchers.withId(R.id.adminText)).check(ViewAssertions.matches(ViewMatchers.isDisplayed()));
        Espresso.onView(ViewMatchers.withId(R.id.createAdminButton)).check(ViewAssertions.matches(ViewMatchers.isDisplayed()));
        Espresso.onView(ViewMatchers.withId(R.id.createUserButton)).check(ViewAssertions.matches(ViewMatchers.isDisplayed()));
    }
}
