package id.co.webpresso.yohanes.bakingday.ui;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;

import id.co.webpresso.yohanes.bakingday.R;
import id.co.webpresso.yohanes.bakingday.model.Recipe;

public class RecipeDetailActivity
        extends AppCompatActivity
        implements RecipeDetailFragment.OnStepClickListener,
            RecipeDetailFragment.OnRecipeLoadedListener,
            RecipeStepDetailFragment.RecipeStepNavListener {
    public static final String BUNDLE_RECIPE_STEP_NUMBER = "recipe_step_number";

    public Uri recipeUri;
    public boolean twoPanes;

    private int recipeStepNumber;
    private RecipeDetailFragment recipeDetailFragment;
    private RecipeStepDetailFragment recipeStepDetailFragment;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recipe_detail);

        // Check for R.id.recipeStepDetailWrapper existance.
        // If it is, it means this is 2 panes layout
        twoPanes = findViewById(R.id.recipeStepDetailWrapper) != null;

        if (getIntent() != null) {
            recipeUri = getIntent().getData();

            recipeDetailFragment = new RecipeDetailFragment();
            Bundle recipeDetailBundle = new Bundle();
            recipeDetailBundle.putString(RecipeDetailFragment.ARG_RECIPE_CONTENT_PATH, recipeUri.toString());
            recipeDetailFragment.setArguments(recipeDetailBundle);

            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.recipeDetailWrapper, recipeDetailFragment)
                    .commit();

            if (twoPanes) {
                if (savedInstanceState != null) {
                    recipeStepNumber = savedInstanceState.getInt(BUNDLE_RECIPE_STEP_NUMBER);
                } else {
                    recipeStepNumber = 1;
                }

                loadRecipeStepFragment(recipeStepNumber, savedInstanceState);
            }
        }

        getSupportActionBar().setTitle(getString(R.string.recipe_title_loading));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public void onStepClick(int sortNumber) {
        recipeStepNumber = sortNumber;

        if (twoPanes) {
            loadRecipeStepFragment(sortNumber, null);
        } else {
            Intent stepDetailIntent = new Intent(this, RecipeStepDetailActivity.class);
            stepDetailIntent.setData(recipeUri);
            stepDetailIntent.putExtra(RecipeStepDetailActivity.BUNDLE_RECIPE_STEP_NUMBER, sortNumber);
            startActivity(stepDetailIntent);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        if (twoPanes) {
            outState.putInt(BUNDLE_RECIPE_STEP_NUMBER, recipeStepNumber);
            getSupportFragmentManager().putFragment(outState, RecipeStepDetailFragment.RECIPE_STEP_DETAIL_FRAGMENT, recipeStepDetailFragment);
        }
    }

    @Override
    public void onRecipeLoaded(Recipe recipe) {
        getSupportActionBar().setTitle(recipe.getName());
        setCurrentStepSelected();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }

        return super.onOptionsItemSelected(item);
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
    public void onRecipeStepNavClick(int step) {
        recipeStepNumber += step;

        loadRecipeStepFragment(recipeStepNumber, null);
    }

    protected void loadRecipeStepFragment(int recipeStepNumber, Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            recipeStepDetailFragment = (RecipeStepDetailFragment) getSupportFragmentManager().getFragment(savedInstanceState, RecipeStepDetailFragment.RECIPE_STEP_DETAIL_FRAGMENT);
        } else {
            recipeStepDetailFragment = new RecipeStepDetailFragment();
            Bundle recipeDetailBundle = new Bundle();

            recipeDetailBundle.putString(RecipeStepDetailFragment.ARG_RECIPE_CONTENT_PATH, recipeUri.toString());
            recipeDetailBundle.putInt(BUNDLE_RECIPE_STEP_NUMBER, recipeStepNumber);
            recipeStepDetailFragment.setArguments(recipeDetailBundle);
        }

        FragmentTransaction fragmentTransaction = getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.recipeStepDetailWrapper, recipeStepDetailFragment);

        fragmentTransaction.commit();

        setCurrentStepSelected();
    }

    protected void setCurrentStepSelected() {
        if (recipeDetailFragment.recipeStepList != null) {
            // Set other steps inactive and set current Step as active.
            // We increment child position because it contains "Steps" section title
            for (int i = 1; i <= recipeDetailFragment.recipeStepList.getChildCount() - 1; i += 1) {
                recipeDetailFragment.recipeStepList.getChildAt(i).setSelected(i == recipeStepNumber);
            }
        }
    }
}
