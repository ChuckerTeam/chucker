import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.chuckerteam.chucker.sample.MainActivity
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MainActivityTest {
    @Test
    fun testUIComponentsAreDisplayed() {
        ActivityScenario.launch(MainActivity::class.java)

        // Interceptor type label
        onView(withText("Interceptor type:")).check(matches(isDisplayed()))

        // Application radio button
        onView(withText("Application")).check(matches(isDisplayed()))
        onView(withText("Application")).perform(click())

        // Network radio button
        onView(withText("Network")).check(matches(isDisplayed()))
        onView(withText("Network")).perform(click())

        // Buttons
        onView(withText("DO HTTP ACTIVITY")).check(matches(isDisplayed()))
        onView(withText("DO GRAPHQL ACTIVITY")).check(matches(isDisplayed()))
        onView(withText("LAUNCH CHUCKER DIRECTLY")).check(matches(isDisplayed()))
        onView(withText("EXPORT TO LOG FILE")).check(matches(isDisplayed()))
        onView(withText("EXPORT TO HAR FILE")).check(matches(isDisplayed()))
    }

    @Test
    fun testButtonInteractions() {
        ActivityScenario.launch(MainActivity::class.java)
        onView(withText("DO HTTP ACTIVITY")).perform(click())
    }
}
