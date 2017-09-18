package id.co.webpresso.yohanes.bakingday;

import android.content.Intent;
import android.support.test.espresso.IdlingRegistry;
import android.support.test.espresso.IdlingResource;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static android.support.test.espresso.matcher.ViewMatchers.hasChildCount;
import static android.support.test.espresso.matcher.ViewMatchers.hasDescendant;
import static android.support.test.espresso.assertion.ViewAssertions.matches;


import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import id.co.webpresso.yohanes.bakingday.provider.RecipeProvider;
import id.co.webpresso.yohanes.bakingday.ui.RecipeDetailActivity;
import id.co.webpresso.yohanes.bakingday.util.Util;

@RunWith(AndroidJUnit4.class)
public class RecipeDetailActivityTest {
    private IdlingResource idlingResource;

    @Rule
    public ActivityTestRule<RecipeDetailActivity> activityTestRule
            = new ActivityTestRule<>(RecipeDetailActivity.class, false, false);

    @Before
    public void registerIdlingResource() {
        idlingResource = Util.getIdlingResource();
        IdlingRegistry.getInstance().register(idlingResource);

        // Manually launch activity to load 2nd Recipe (ID: 2).
        Intent recipeDetailIntent = new Intent();
        recipeDetailIntent.setData(
                RecipeProvider.CONTENT_URI
                        .buildUpon()
                        .appendPath("2")
                        .build()
        );

        activityTestRule.launchActivity(recipeDetailIntent);
    }

    @Test
    public void correctlyShowsRecipe() {
        onView(withId(R.id.recipeDetailName)).check(matches(withText("Brownies")));
    }

    /**
     * It should have 10 ingredients, but because there is section title, we should add 1 child count
     */
    @Test
    public void rendersCorrectIngredientsCount() {
        onView(withId(R.id.recipeDetailIngredientList)).check(matches(hasChildCount(11)));
    }

    @Test
    public void rendersCorrectIngredients() {
        onView(withId(R.id.recipeDetailIngredientList)).check(matches(hasDescendant(withText("Bittersweet chocolate (60-70% cacao)"))));
        onView(withId(R.id.recipeDetailIngredientList)).check(matches(hasDescendant(withText("semisweet chocolate chips"))));
    }

    /**
     * It should have 10 steps, but because there is section title, we should add 1 child count
     */
    @Test
    public void rendersCorrectStepsCount() {
        onView(withId(R.id.recipeDetailStepList)).check(matches(hasChildCount(11)));
    }

    @Test
    public void rendersCorrectSteps() {
        onView(withId(R.id.recipeDetailStepList)).check(matches(hasDescendant(withText("Add sugars to wet mixture."))));
        onView(withId(R.id.recipeDetailStepList)).check(matches(hasDescendant(withText("Remove pan from oven."))));
    }

    @After
    public void unregisterIdlingResource() {
        if (idlingResource != null) {
            IdlingRegistry.getInstance().unregister(idlingResource);
        }
    }
}
