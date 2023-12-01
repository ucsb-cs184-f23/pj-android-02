package com.couchpotatoes

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isClickable
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.rules.ActivityScenarioRule
import org.junit.Rule
import org.junit.Test

class UserSelectionActivityTest {
    @get:Rule
    val activityRule = ActivityScenarioRule(UserSelectionActivity::class.java)

    @Test
    fun testUserSelectionScreen() {
        onView(withId(R.id.requesterButton)).check(matches(isDisplayed()))
        onView(withId(R.id.HustlerButton)).check(matches(isDisplayed()))
        onView(withId(R.id.RequesterText)).check(matches(withText("Requester")))
        onView(withId(R.id.HustlerText)).check(matches(withText("Hustler")))
    }

}