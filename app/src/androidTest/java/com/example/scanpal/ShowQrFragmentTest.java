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
public class ShowQrFragmentTest {

    @Before
    public void setUp() {
        Bundle args = new Bundle();
        args.putString("event_id", "515e4f51-1728-495e-8437-0b6862263b23");
        FragmentScenario<ShowQrFragment> scenario = FragmentScenario.launchInContainer(ShowQrFragment.class, args);
        scenario.moveToState(Lifecycle.State.STARTED);
    }

    /**
     * Tests the UI components' visibility in the ShowQrFragment.
     */
    @Test
    public void testUIVisibility() {
        Espresso.onView(ViewMatchers.withId(R.id.button_go_back)).check(ViewAssertions.matches(ViewMatchers.isDisplayed()));
        Espresso.onView(ViewMatchers.withId(R.id.show_qr_title)).check(ViewAssertions.matches(ViewMatchers.isDisplayed()));
        Espresso.onView(ViewMatchers.withId(R.id.qr_code)).check(ViewAssertions.matches(ViewMatchers.isDisplayed()));
        Espresso.onView(ViewMatchers.withId(R.id.event_name_qrcode)).check(ViewAssertions.matches(ViewMatchers.isDisplayed()));
    }
}
