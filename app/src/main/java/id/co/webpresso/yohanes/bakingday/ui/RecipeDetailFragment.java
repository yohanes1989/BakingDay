package id.co.webpresso.yohanes.bakingday.ui;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.text.DecimalFormat;

import id.co.webpresso.yohanes.bakingday.R;
import id.co.webpresso.yohanes.bakingday.idling_resource.RecipeIdlingResource;
import id.co.webpresso.yohanes.bakingday.model.Recipe;
import id.co.webpresso.yohanes.bakingday.util.Util;

public class RecipeDetailFragment extends Fragment {
    public static final String ARG_RECIPE_CONTENT_PATH = "recipe_path";
    public static final int LOADER_RECIPE_DETAIL = 2;
    public static final int LOADER_RECIPE_DETAIL_INGREDIENTS = 3;
    public static final int LOADER_RECIPE_DETAIL_STEPS = 4;

    public Recipe recipe;
    public Uri recipeUri;

    public ImageView recipeImageView;
    public TextView recipeNameTextView;
    public TextView recipeServingsTextView;

    public LinearLayout recipeIngredientList;
    public LinearLayout recipeStepList;

    private LayoutInflater layoutInflater;
    private OnStepClickListener stepClickListener;
    private OnRecipeLoadedListener onRecipeLoadedListener;

    @Nullable private RecipeIdlingResource idlingResource;

    public interface OnStepClickListener {
        void onStepClick(int sortNumber);
    }

    public interface OnRecipeLoadedListener {
        void onRecipeLoaded(Recipe recipe);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        // Only assign if parent Activity implements OnRecipeLoadedListener
        if (context instanceof OnRecipeLoadedListener) {
            onRecipeLoadedListener = (OnRecipeLoadedListener) context;
        }

        try {
            stepClickListener = (OnStepClickListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + " must implement OnStepClickListener");
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        if (!getArguments().containsKey(ARG_RECIPE_CONTENT_PATH)) {
            throw new IllegalArgumentException("Argument ARG_RECIPE_CONTENT_PATH is required.");
        }

        setRecipeUri(Uri.parse(getArguments().getString(ARG_RECIPE_CONTENT_PATH)));

        final View rootView = inflater.inflate(R.layout.fragment_recipe_detail, container, false);

        layoutInflater = inflater;

        recipeImageView = (ImageView) rootView.findViewById(R.id.recipeDetailImage);
        recipeNameTextView = (TextView) rootView.findViewById(R.id.recipeDetailName);
        recipeServingsTextView = (TextView) rootView.findViewById(R.id.recipeDetailServings);

        recipeStepList = (LinearLayout) rootView.findViewById(R.id.recipeDetailStepList);
        recipeIngredientList = (LinearLayout) rootView.findViewById(R.id.recipeDetailIngredientList);

        idlingResource = Util.getIdlingResource();

        if (idlingResource != null) {
            idlingResource.setIdleState(false);
        }

        return rootView;
    }

    /**
     * Method to tell fragment to start fetching Recipe from db and render elements
     * @param recipeUri
     */
    public void setRecipeUri(Uri recipeUri) {
        this.recipeUri = recipeUri;
        getLoaderManager().restartLoader(LOADER_RECIPE_DETAIL, null, recipeDetailLoaderCallbacks);
    }

    private void renderIngredient(Recipe.Ingredient ingredient) {
        View ingredientView = layoutInflater.inflate(R.layout.recipe_ingredient_card, recipeIngredientList, false);

        TextView ingredientNameTextView = (TextView) ingredientView.findViewById(R.id.recipeDetailIngredientName);
        TextView ingredientQuantityTextView = (TextView) ingredientView.findViewById(R.id.recipeDetailIngredientQuantity);
        TextView ingredientMeasureTextView = (TextView) ingredientView.findViewById(R.id.recipeDetailIngredientMeasure);

        ingredientNameTextView.setText(ingredient.getIngredient());
        ingredientMeasureTextView.setText(ingredient.getMeasure());

        DecimalFormat decimalFormat = new DecimalFormat("#");
        ingredientQuantityTextView.setText(decimalFormat.format(ingredient.getQuantity()));

        recipeIngredientList.addView(ingredientView);
    }

    private void renderStep(Recipe.Step step, final int sortNumber) {
        View stepView = layoutInflater.inflate(R.layout.recipe_step_card, recipeStepList, false);

        TextView stepNumberTextView = (TextView) stepView.findViewById(R.id.recipeDetailStepNumber);
        TextView stepShortDescriptionView = (TextView) stepView.findViewById(R.id.recipeDetailStepShortDescription);

        stepNumberTextView.setText(String.valueOf(sortNumber));
        stepShortDescriptionView.setText(step.getShortDescription());

        stepView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                stepClickListener.onStepClick(sortNumber);
            }
        });

        recipeStepList.addView(stepView);
    }

    private LoaderManager.LoaderCallbacks<Cursor> recipeDetailLoaderCallbacks = new LoaderManager.LoaderCallbacks<Cursor>() {
        @Override
        public Loader<Cursor> onCreateLoader(int id, Bundle args) {
            switch (id) {
                case LOADER_RECIPE_DETAIL:
                    return new CursorLoader(getContext(), recipeUri, null, null, null, null);
                case LOADER_RECIPE_DETAIL_INGREDIENTS:
                    Uri recipeIngredientsUri = recipeUri.buildUpon().appendPath(Recipe.Ingredient.TABLE_NAME).build();

                    return new CursorLoader(getContext(), recipeIngredientsUri, null, null, null, null);
                case LOADER_RECIPE_DETAIL_STEPS:
                    Uri recipeStepsUri = recipeUri.buildUpon().appendPath(Recipe.Step.TABLE_NAME).build();

                    return new CursorLoader(getContext(), recipeStepsUri, null, null, null, null);
            }

            throw new UnsupportedOperationException("Unsupported Loader ID: " + id);
        }

        @Override
        public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
            switch (loader.getId()) {
                case LOADER_RECIPE_DETAIL:
                    if (data.getCount() > 0) {
                        data.moveToFirst();
                        recipe = new Recipe(data);

                        getLoaderManager().restartLoader(LOADER_RECIPE_DETAIL_INGREDIENTS, null, recipeDetailLoaderCallbacks);
                        getLoaderManager().restartLoader(LOADER_RECIPE_DETAIL_STEPS, null, recipeDetailLoaderCallbacks);

                        if (!recipe.getImage().isEmpty()) {
                            recipeImageView.setVisibility(View.VISIBLE);

                            Picasso.with(getContext())
                                    .load(recipe.getImage())
                                    .fit()
                                    .into(recipeImageView);
                        }

                        recipeNameTextView.setText(recipe.getName());

                        DecimalFormat decimalFormat = new DecimalFormat("#");
                        recipeServingsTextView.setText(
                                getResources().getQuantityString(
                                        R.plurals.serving_size,
                                        recipe.getServings().intValue(),
                                        decimalFormat.format(recipe.getServings())
                                )
                        );
                    }
                    break;
                case LOADER_RECIPE_DETAIL_INGREDIENTS:
                    recipe.setIngredients(data);

                    for (Recipe.Ingredient ingredient : recipe.getIngredients()) {
                        renderIngredient(ingredient);
                    }

                    break;
                case LOADER_RECIPE_DETAIL_STEPS:
                    recipe.setSteps(data);

                    for (int i = 1; i <= recipe.getSteps().size(); i++) {
                        renderStep(recipe.getSteps().get(i - 1), i);
                    }

                    if (idlingResource != null) {
                        idlingResource.setIdleState(true);
                    }

                    if (onRecipeLoadedListener != null) {
                        onRecipeLoadedListener.onRecipeLoaded(recipe);
                    }

                    break;
                default:
                    throw new UnsupportedOperationException("Recipe this URI not found: " + recipeUri.toString());
            }
        }

        @Override
        public void onLoaderReset(Loader<Cursor> loader) {

        }
    };
}
