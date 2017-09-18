package id.co.webpresso.yohanes.bakingday;

import android.support.v7.widget.RecyclerView;
import android.view.View;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;

public class RecipeTestHelper {
    /**
     * Matcher for RecyclerView adapter item count
     */
    public static Matcher<View> recylerViewAdapterCount(final Matcher<Integer> countMatcher) {
        return new TypeSafeMatcher<View>() {
            @Override
            protected boolean matchesSafely(View item) {
                return countMatcher.matches(((RecyclerView) item).getAdapter().getItemCount());
            }

            @Override
            public void describeTo(Description description) {
                description.appendText("RecyclerView's adapter count doesn't match: ");
                countMatcher.describeTo(description);
            }
        };
    }
}
