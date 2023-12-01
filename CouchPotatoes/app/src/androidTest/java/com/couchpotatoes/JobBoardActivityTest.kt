package com.couchpotatoes

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import com.couchpotatoes.jobBoard.JobBoardActivity
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@LargeTest
class JobBoardActivityTest {

    @get:Rule
    val activityRule = ActivityScenarioRule(JobBoardActivity::class.java)

    @Test
    fun testUserSelectionScreen() {
        onView(withId(R.id.job_board_title)).check(matches(withText("Job Board")))
    }
}