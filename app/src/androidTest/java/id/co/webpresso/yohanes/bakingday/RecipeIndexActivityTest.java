package id.co.webpresso.yohanes.bakingday;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.contrib.RecyclerViewActions.actionOnItemAtPosition;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static android.support.test.espresso.action.ViewActions.click;
import static org.hamcrest.Matchers.is;

import android.support.test.espresso.Espresso;
import android.support.test.espresso.IdlingRegistry;
import android.support.test.espresso.IdlingResource;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import id.co.webpresso.yohanes.bakingday.ui.RecipeIndexActivity;
import id.co.webpresso.yohanes.bakingday.util.Util;

@RunWith(AndroidJUnit4.class)
public class RecipeIndexActivityTest {
    private IdlingResource idlingResource;

    @Rule
    public ActivityTestRule<RecipeIndexActivity> activityTestRule
            = new ActivityTestRule<>(RecipeIndexActivity.class);

    @Before
    public void registerIdlingResource() {
        idlingResource = Util.getIdlingResource();
        IdlingRegistry.getInstance().register(idlingResource);
    }

    @Test
    public void showsAllRecipes() {
        onView(withId(R.id.recipeListRecyclerView)).check(matches(
             RecipeTestHelper.recylerViewAdapterCount(is(4))
        ));
    }

    @Test
    public void clickRecipeCard_OpensRecipeDetail() {
        onView(withId(R.id.recipeListRecyclerView))
                .perform(actionOnItemAtPosition(1, click()));

        onView(withId(R.id.recipeDetailName)).check(matches(withText("Brownies")));
    }

    @After
    public void unregisterIdlingResource() {
        if (idlingResource != null) {
            IdlingRegistry.getInstance().unregister(idlingResource);
        }
    }
}
