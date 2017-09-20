package id.co.webpresso.yohanes.bakingday.ui;

import android.net.Uri;
import android.os.PersistableBundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.MenuItem;

import id.co.webpresso.yohanes.bakingday.R;
import id.co.webpresso.yohanes.bakingday.model.Recipe;

public class RecipeStepDetailActivity extends AppCompatActivity
        implements RecipeStepDetailFragment.RecipeStepNavListener, RecipeStepDetailFragment.OnRecipeStepLoadedListener {
    public static final String BUNDLE_RECIPE_STEP_NUMBER = "recipe_step_number";

    public RecipeStepDetailFragment recipeStepDetailFragment;

    private Uri recipeUri;
    private int recipeStepNumber;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recipe_step_detail);

        recipeUri = getIntent().getData();

        if (savedInstanceState != null) {
            recipeStepNumber = savedInstanceState.getInt(BUNDLE_RECIPE_STEP_NUMBER);

            recipeStepDetailFragment = (RecipeStepDetailFragment) getSupportFragmentManager().getFragment(savedInstanceState, RecipeStepDetailFragment.RECIPE_STEP_DETAIL_FRAGMENT);
        } else {
            recipeStepNumber = getIntent().getIntExtra(BUNDLE_RECIPE_STEP_NUMBER, 1);

            recipeStepDetailFragment = new RecipeStepDetailFragment();
            Bundle recipeStepDetailBundle = new Bundle();
            recipeStepDetailBundle.putString(RecipeStepDetailFragment.ARG_RECIPE_CONTENT_PATH, recipeUri.toString());
            recipeStepDetailBundle.putInt(BUNDLE_RECIPE_STEP_NUMBER, recipeStepNumber);
            recipeStepDetailFragment.setArguments(recipeStepDetailBundle);
        }

        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.recipeStepDetailWrapper, recipeStepDetailFragment)
                .commit();

        getSupportActionBar().setTitle(getString(R.string.recipe_title_loading));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putInt(BUNDLE_RECIPE_STEP_NUMBER, recipeStepNumber);
        getSupportFragmentManager().putFragment(outState, RecipeStepDetailFragment.RECIPE_STEP_DETAIL_FRAGMENT, recipeStepDetailFragment);
    }

    @Override
    public void onRecipeStepNavClick(int step) {
        recipeStepNumber += step;

        RecipeStepDetailFragment newRecipeStepDetailFragment = new RecipeStepDetailFragment();
        Bundle recipeDetailBundle = new Bundle();
        recipeDetailBundle.putString(RecipeStepDetailFragment.ARG_RECIPE_CONTENT_PATH, recipeUri.toString());
        recipeDetailBundle.putInt(BUNDLE_RECIPE_STEP_NUMBER, recipeStepNumber);
        newRecipeStepDetailFragment.setArguments(recipeDetailBundle);

        FragmentTransaction fragmentTransaction = getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.recipeStepDetailWrapper, newRecipeStepDetailFragment);

        if (step > 0) {
            fragmentTransaction.addToBackStack("RecipeStep" + String.valueOf(recipeStepNumber));
        } else {
            getSupportFragmentManager().popBackStack();
        }

        fragmentTransaction.commit();

        recipeStepDetailFragment = newRecipeStepDetailFragment;
    }

    @Override
    public void onRecipeStepLoaded(Recipe recipe, int position) {
        getSupportActionBar().setTitle(String.format(
                getString(R.string.recipe_step_title),
                position,
                recipe.getSteps().get(position - 1).getShortDescription()
        ));
    }

    @Override
    public void onBackPressed() {
        if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
            onRecipeStepNavClick(-1);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
