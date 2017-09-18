package id.co.webpresso.yohanes.bakingday.util;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.provider.BaseColumns;
import android.util.Log;

import java.util.List;

import id.co.webpresso.yohanes.bakingday.model.Recipe;
import id.co.webpresso.yohanes.bakingday.provider.RecipeProvider;
import id.co.webpresso.yohanes.bakingday.service.RecipeService;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.GET;

public class RecipeUtil {
    public static final String ACTION_SYNC_FINISHED = RecipeUtil.class
            .getPackage()
            .getName()
            .replace(".util", "") + ".ACTION_SYNC_FINISHED";
    public static final String BASE_URL = "http://go.udacity.com/";
    public static RecipeRequestInterface request;

    public static void fetchAndSaveRecipes(final Context context) {
        RecipeRequestInterface recipeRequest = getRequest();
        Call<List<Recipe>> recipeCall = recipeRequest.getRecipes();

        recipeCall.enqueue(new Callback<List<Recipe>>() {

            @Override
            public void onResponse(Call<List<Recipe>> call, Response<List<Recipe>> response) {
                ContentResolver contentResolver = context.getContentResolver();
                List<Recipe> recipes = response.body();

                for (Recipe recipe : recipes) {
                    ContentValues contentValues = new ContentValues();
                    contentValues.put(BaseColumns._ID, recipe._id);
                    contentValues.put(Recipe.COLUMN_NAME, recipe.getName());
                    contentValues.put(Recipe.COLUMN_SERVINGS, recipe.getServings());
                    contentValues.put(Recipe.COLUMN_IMAGE, recipe.getImage());

                    Uri recipeUri = contentResolver.insert(RecipeProvider.CONTENT_URI, contentValues);

                    // Delete old ingredients and save new ones
                    Uri recipeIngredientsUri = recipeUri.buildUpon().appendPath(Recipe.Ingredient.TABLE_NAME).build();
                    contentResolver.delete(recipeIngredientsUri, null, null);

                    int sortOrder = 0;
                    for (Recipe.Ingredient ingredient : recipe.getIngredients()) {
                        ContentValues ingredientContentValues = new ContentValues();
                        ingredientContentValues.put(Recipe.Ingredient.COLUMN_INGREDIENT, ingredient.getIngredient());
                        ingredientContentValues.put(Recipe.Ingredient.COLUMN_MEASURE, ingredient.getMeasure());
                        ingredientContentValues.put(Recipe.Ingredient.COLUMN_QUANTITY, ingredient.getQuantity());
                        ingredientContentValues.put(Recipe.Ingredient.COLUMN_SORT_ORDER, sortOrder);

                        contentResolver.insert(recipeIngredientsUri, ingredientContentValues);

                        sortOrder++;
                    }

                    // Delete old steps and save new ones
                    Uri recipeStepsUri = recipeUri.buildUpon().appendPath(Recipe.Step.TABLE_NAME).build();
                    contentResolver.delete(recipeStepsUri, null, null);

                    for (Recipe.Step step : recipe.getSteps()) {
                        ContentValues stepContentValues = new ContentValues();
                        stepContentValues.put(Recipe.Step.COLUMN_SHORT_DESCRIPTION, step.getShortDescription());
                        stepContentValues.put(Recipe.Step.COLUMN_DESCRIPTION, step.getDescription());
                        stepContentValues.put(Recipe.Step.COLUMN_THUMBNAIL_URL, step.getThumbnailURL());
                        stepContentValues.put(Recipe.Step.COLUMN_VIDEO_URL, step.getVideoURL());
                        stepContentValues.put(Recipe.Step.COLUMN_SORT_ORDER, step.getSortOrder());

                        contentResolver.insert(recipeStepsUri, stepContentValues);
                    }
                }

                // Send broadcast when Sync is finished
                context.sendBroadcast(new Intent(ACTION_SYNC_FINISHED));
            }

            @Override
            public void onFailure(Call<List<Recipe>> call, Throwable t) {

            }
        });
    }

    public static void syncRecipes(Context context) {
        Intent intent = new Intent(context, RecipeService.class);
        intent.setAction(RecipeService.ACTION_FETCH_REPICES);
        context.startService(intent);
    }

    public static RecipeRequestInterface getRequest() {
        if (request == null) {
            Retrofit client = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();

            request = client.create(RecipeRequestInterface.class);
        }

        return request;
    }

    public interface RecipeRequestInterface {
        @GET("android-baking-app-json")
        Call<List<Recipe>> getRecipes();
    }
}
