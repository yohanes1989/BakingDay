package id.co.webpresso.yohanes.bakingday;

import android.content.Intent;
import android.support.test.espresso.IdlingRegistry;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;

import com.google.android.exoplayer2.ui.SimpleExoPlayerView;

import static android.support.test.espresso.action.ViewActions.scrollTo;
import static android.support.test.espresso.assertion.ViewAssertions.doesNotExist;
import static android.support.test.espresso.matcher.ViewMatchers.isSelected;
import static android.support.test.espresso.matcher.ViewMatchers.withClassName;
import static android.support.test.espresso.matcher.ViewMatchers.withContentDescription;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withParent;
import static android.support.test.espresso.matcher.ViewMatchers.withChild;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.Espresso.pressBack;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.contrib.RecyclerViewActions.actionOnItemAtPosition;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.containsString;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import id.co.webpresso.yohanes.bakingday.idling_resource.RecipeIdlingResource;
import id.co.webpresso.yohanes.bakingday.provider.RecipeProvider;
import id.co.webpresso.yohanes.bakingday.ui.RecipeDetailActivity;
import id.co.webpresso.yohanes.bakingday.util.Util;

@RunWith(AndroidJUnit4.class)
public class RecipeStepDetailTest {
    private RecipeIdlingResource idlingResource;

    @Rule
    public ActivityTestRule<RecipeDetailActivity> activityTestRule
            = new ActivityTestRule<>(RecipeDetailActivity.class, false, false);

    @Before
    public void registerIdlingResource() {
        idlingResource = Util.getIdlingResource();
        IdlingRegistry.getInstance().register(idlingResource);

        // Manually launch activity to load 2nd Recipe (ID: 4).
        Intent recipeDetailIntent = new Intent();
        recipeDetailIntent.setData(
                RecipeProvider.CONTENT_URI
                        .buildUpon()
                        .appendPath("4")
                        .build()
        );

        activityTestRule.launchActivity(recipeDetailIntent);
    }

    @Test
    public void onStepClick_ShowCorrectStep() {
        onView(withId(R.id.recipeDetailStepList)).perform(actionOnItemAtPosition(0, click()));

        // Check if video is displayed
        onView(allOf(withId(R.id.recipeStepDetailVideoPlayerView), withClassName(is(SimpleExoPlayerView.class.getName())))).check(matches(isDisplayed()));

        onView(withId(R.id.recipeStepDetailTitle)).check(matches(withText(containsString("Recipe Introduction"))));
    }

    @Test
    public void onNextClick_ShowNextStep() {
        onView(withId(R.id.recipeDetailStepList)).perform(actionOnItemAtPosition(0, click()));

        onView(withId(R.id.recipeStepDetailNextButton)).perform(click());

        // Check if video is not displayed
        onView(allOf(withId(R.id.recipeStepDetailVideoPlayerView), withClassName(is(SimpleExoPlayerView.class.getName())))).check(matches(not(isDisplayed())));

        onView(withId(R.id.recipeStepDetailDescription)).check(matches(withText("1. Preheat the oven to 350°F. Grease the bottom of a 9-inch round springform pan with butter. ")));

        // If twoPanes, check if current Step is selected
        if (activityTestRule.getActivity().twoPanes) {
            onView(allOf(withText("Starting prep."), withId(R.id.recipeDetailStepShortDescription))).check(matches(isSelected()));
        }
    }

    @Test
    public void onPrevClick_ShowPreviousStep() {
        onView(withId(R.id.recipeDetailStepList)).perform(actionOnItemAtPosition(0, click()));

        // Move 2 steps
        onView(withId(R.id.recipeStepDetailNextButton)).perform(click());
        onView(withId(R.id.recipeStepDetailNextButton)).perform(click());

        onView(withId(R.id.recipeStepDetailPrevButton)).perform(click());
        onView(withId(R.id.recipeStepDetailTitle)).check(matches(withText(containsString("Starting prep."))));

        onView(withId(R.id.recipeStepDetailPrevButton)).perform(click());

        // Should not show Previous step button
        onView(withId(R.id.recipeStepDetailPrevButton)).check(matches(not(isDisplayed())));

        if (!activityTestRule.getActivity().twoPanes) {
            // If not twoPanes, Back press should show RecipeDetailActivity
            pressBack();

            onView(withId(R.id.recipeStepDetailTitle)).check(doesNotExist());
            onView(withId(R.id.recipeDetailName)).check(matches(withText("Cheesecake")));
        }
    }

    @Test
    public void onBackPressed_ShowPreviousStep() {
        onView(withId(R.id.recipeDetailStepList)).perform(actionOnItemAtPosition(0, click()));

        // Move 2 steps
        onView(withId(R.id.recipeStepDetailNextButton)).perform(click());
        onView(withId(R.id.recipeStepDetailNextButton)).perform(click());

        if (!activityTestRule.getActivity().twoPanes) {
            // If not twoPanes, Back press should open previous fragment
            pressBack();
            onView(withId(R.id.recipeStepDetailTitle)).check(matches(withText(containsString("Starting prep."))));

            // Should show Recipe detail
            pressBack();
            pressBack();
            onView(withId(R.id.recipeStepDetailTitle)).check(doesNotExist());
            onView(withId(R.id.recipeDetailName)).check(matches(withText("Cheesecake")));
        }
    }

    @After
    public void unregisterIdlingResource() {
        IdlingRegistry.getInstance().unregister(idlingResource);
    }
}
