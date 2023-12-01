package com.couchpotatoes

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isClickable
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@LargeTest
class RequestFormActivityTest {

    @get:Rule
    val activityRule = ActivityScenarioRule(RequestFormActivity::class.java)

    @Test
    fun testUserSelectionScreen() {
        onView(withId(R.id.button)).check(matches(isDisplayed()))
        onView(withId(R.id.what)).check(matches(isClickable()))
        onView(withId(R.id.where)).check(matches(isClickable()))
        onView(withId(R.id.cost)).check(matches(isClickable()))
        onView(withId(R.id.address)).check(matches(isClickable()))
    }
}