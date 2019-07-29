package com.biz.aceras.ordertracking

import android.app.Activity
import android.app.Instrumentation
import android.content.Context
import android.preference.PreferenceManager
import android.support.test.InstrumentationRegistry
import android.support.test.InstrumentationRegistry.getInstrumentation
import android.support.test.espresso.Espresso.closeSoftKeyboard
import android.support.test.espresso.Espresso.onView
import android.support.test.espresso.action.ViewActions.click
import android.support.test.espresso.action.ViewActions.typeText
import android.support.test.espresso.assertion.ViewAssertions.matches
import android.support.test.espresso.matcher.RootMatchers.isDialog
import android.support.test.espresso.matcher.ViewMatchers.*
import android.support.test.rule.ActivityTestRule
import android.support.test.rule.GrantPermissionRule
import android.support.test.runner.AndroidJUnit4
import android.util.Log
import com.biz.aceras.ordertracking.activities.ActivateActivity
import com.biz.aceras.ordertracking.activities.RegisterActivity
import com.biz.aceras.ordertracking.activities.SplashActivity
import com.biz.aceras.ordertracking.activities.WelcomeActivity

import org.junit.Test
import org.junit.runner.RunWith

import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule

/**
 * Instrumented test, which will execute on an Android device.
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
@RunWith(AndroidJUnit4::class)
class ExampleInstrumentedTest {

    @Rule
    @JvmField
    // This basically means it will lunch the activity to start the app
    val activityTestRule: ActivityTestRule<SplashActivity> = ActivityTestRule(SplashActivity::class.java)

    @Rule
    @JvmField
    // Since espresso cannot control the System UI, this is basically used to accept the necessary permission to proceed further in testing
    val grantPermissionRule: GrantPermissionRule = GrantPermissionRule.grant(android.Manifest.permission.READ_PHONE_STATE)
    lateinit var registerID: String
    val monitorSplashActivity: Instrumentation.ActivityMonitor = getInstrumentation().addMonitor(SplashActivity::class.java.name, null, false)
    val monitorRegisterActivity: Instrumentation.ActivityMonitor = getInstrumentation().addMonitor(RegisterActivity::class.java.name, null, false)
    val monitorActivateActivity: Instrumentation.ActivityMonitor = getInstrumentation().addMonitor(ActivateActivity::class.java.name, null, false)
    val monitorWelcomeActivity: Instrumentation.ActivityMonitor = getInstrumentation().addMonitor(WelcomeActivity::class.java.name, null, false)

    //    @Test
//    fun endToEndPositiveTest() {
//        if (isRegistered()) {
//            startFromRegisterScreen()
//        } else {
//            startFromWelcomeScreen()
//        }
//    }
//
//    private fun isRegistered(): Boolean {
//        val activity: Activity = activityTestRule.activity
//        val registrationID = SharedPreference(activity).getValueString(activity.getString(R.string.pref_registration_id))
//        return !registrationID.isNullOrEmpty()
//    }
    @Before
    fun setup() {
        val targetContext: Context = getInstrumentation().getTargetContext()
        registerID = SharedPreference(targetContext).getValueString(targetContext.getString(R.string.pref_registration_id)).toString()
    }

    @Test
    fun startFromWelcomeScreen() {
//        // Wait for Welcome Activity to load first
//        val welcomeActivity: Activity = getInstrumentation().waitForMonitorWithTimeout(monitorWelcomeActivity,8000)
//        assertNotNull(welcomeActivity)

        if (registerID.isEmpty()) {
            // Wait for Register Activity to load first
            val registerActivity: Activity = getInstrumentation().waitForMonitorWithTimeout(monitorRegisterActivity, 8000)
            assertNotNull(registerActivity)

            // webview
            onView(withText("Terms of Service & Privacy Policy")).check(matches(isDisplayed()))
            onView(withText("Agree")).inRoot(isDialog()).check(matches(isDisplayed())).perform(click())

            // Register
            onView(withId(R.id.etEmail)).perform(typeText("Hello"))
            closeSoftKeyboard()
            onView(withId(R.id.etPhoneNo)).perform(typeText("123"))
            closeSoftKeyboard()
            onView(withId(R.id.btnRegister)).perform(click())

            // Wait for Activate Activity to load first
            val activateActivity: Activity = getInstrumentation().waitForMonitorWithTimeout(monitorActivateActivity, 8000)
            assertNotNull(activateActivity)
            onView(withId(R.id.etActivationCode)).perform(typeText("123"))
            closeSoftKeyboard()
            onView(withId(R.id.btnActivate)).perform(click())

            // Wait for Welcome Activity to load first
            val welcomeActivity: Activity = getInstrumentation().waitForMonitorWithTimeout(monitorWelcomeActivity, 8000)
            assertNotNull(welcomeActivity)
            onView(withId(R.id.btnContinue)).perform(click())
        } else {
            // Wait for Welcome Activity to load first
            val welcomeActivity: Activity = getInstrumentation().waitForMonitorWithTimeout(monitorWelcomeActivity, 8000)
            assertNotNull(welcomeActivity)
            onView(withId(R.id.btnContinue)).perform(click())
        }
    }

//    @Test
//    fun startFromRegisterScreen() {
//        // Wait for Register Activity to load first
//        val registerActivity: Activity = getInstrumentation().waitForMonitorWithTimeout(monitorRegisterActivity,8000)
//        assertNotNull(registerActivity)
//
//        // webview
//        onView(withText("Terms of Service & Privacy Policy")).check(matches(isDisplayed()))
//        onView(withText("Agree")).inRoot(isDialog()).check(matches(isDisplayed())).perform(click())
//
//        // Register
//        onView(withId(R.id.etEmail)).perform(typeText("Hello"))
//        closeSoftKeyboard()
//        onView(withId(R.id.etPhoneNo)).perform(typeText("123"))
//        closeSoftKeyboard()
//        onView(withId(R.id.btnRegister)).perform(click())
//
//        // Wait for Activate Activity to load first
//        val activateActivity: Activity = getInstrumentation().waitForMonitorWithTimeout(monitorActivateActivity,8000)
//        assertNotNull(activateActivity)
//        onView(withId(R.id.etActivationCode)).perform(typeText("123"))
//        closeSoftKeyboard()
//        onView(withId(R.id.btnActivate)).perform(click())
//
//        // Wait for Welcome Activity to load first
//        val welcomeActivity: Activity = getInstrumentation().waitForMonitorWithTimeout(monitorWelcomeActivity,8000)
//        assertNotNull(welcomeActivity)
//        onView(withId(R.id.btnContinue)).perform(click())
//    }

// @After
//    fun teardown(){}
}
