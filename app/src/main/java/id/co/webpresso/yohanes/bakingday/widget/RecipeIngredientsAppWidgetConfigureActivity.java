package id.co.webpresso.yohanes.bakingday.widget;

import android.appwidget.AppWidgetManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import java.util.ArrayList;
import java.util.HashMap;

import id.co.webpresso.yohanes.bakingday.R;
import id.co.webpresso.yohanes.bakingday.model.Recipe;
import id.co.webpresso.yohanes.bakingday.provider.RecipeProvider;
import id.co.webpresso.yohanes.bakingday.util.RecipeUtil;

/**
 * The configuration screen for the {@link RecipeIngredientsAppWidget RecipeIngredientsAppWidget} AppWidget.
 */
public class RecipeIngredientsAppWidgetConfigureActivity extends AppCompatActivity {
    public static final int LOADER_RECIPES = 1;

    private static final String PREFS_NAME = "id.co.webpresso.yohanes.bakingday.widget.RecipeIngredientsAppWidget";
    private static final String PREF_PREFIX_KEY = "recipe_ingredients_appwidget_";
    int appWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID;

    SyncBroadcastReceiver syncBroadcastReceiver;
    Spinner recipeSpinner;

    /**
     * Used as Spinner selections
     */
    Recipe[] recipes;

    /**
     * Use to map Recipe Id to Recipe object
     */
    HashMap<Long, Recipe> recipesStores;

    View.OnClickListener mOnClickListener = new View.OnClickListener() {
        public void onClick(View v) {
            final Context context = RecipeIngredientsAppWidgetConfigureActivity.this;

            // When the button is clicked, store Recipe ID locally
            Recipe selectedRecipe = (Recipe) recipeSpinner.getSelectedItem();
            saveRecipeIdPref(context, appWidgetId, selectedRecipe._id);

            // It is the responsibility of the configuration activity to update the app widget
            AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
            RecipeIngredientsAppWidget.updateAppWidget(context, appWidgetManager, selectedRecipe, appWidgetId);

            // Make sure we pass back the original appWidgetId
            Intent resultValue = new Intent();
            resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
            setResult(RESULT_OK, resultValue);
            finish();
        }
    };

    public RecipeIngredientsAppWidgetConfigureActivity() {
        super();
    }

    // Write the prefix to the SharedPreferences object for this widget
    static void saveRecipeIdPref(Context context, int appWidgetId, Long recipeId) {
        SharedPreferences.Editor prefs = context.getSharedPreferences(PREFS_NAME, 0).edit();
        prefs.putLong(PREF_PREFIX_KEY + appWidgetId, recipeId);
        prefs.apply();
    }

    // Read the prefix from the SharedPreferences object for this widget.
    // If there is no preference saved, get the default from a resource
    static Long loadRecipeIdPref(Context context, int appWidgetId) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, 0);
        Long recipeId = prefs.getLong(PREF_PREFIX_KEY + appWidgetId, 0);

        return recipeId;
    }

    static void deleteRecipeIdPref(Context context, int appWidgetId) {
        SharedPreferences.Editor prefs = context.getSharedPreferences(PREFS_NAME, 0).edit();
        prefs.remove(PREF_PREFIX_KEY + appWidgetId);
        prefs.apply();
    }

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);

        // Set the result to CANCELED.  This will cause the widget host to cancel
        // out of the widget placement if the user presses the back button.
        setResult(RESULT_CANCELED);

        setContentView(R.layout.recipe_ingredients_app_widget_configure);
        recipeSpinner = (Spinner) findViewById(R.id.recipeIngredientAppWidgetConfigurationRecipeSelect);

        findViewById(R.id.recipeIngredientAppWidgetConfigurationAddButton).setOnClickListener(mOnClickListener);

        // Find the widget id from the intent.
        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        if (extras != null) {
            appWidgetId = extras.getInt(
                    AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
        }

        // If this activity was started with an intent without an app widget ID, finish with an error.
        if (appWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            finish();
            return;
        }

        // Load Recipes
        getSupportLoaderManager().initLoader(LOADER_RECIPES, null, loaderCallbacks);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (syncBroadcastReceiver != null) {
            unregisterReceiver(syncBroadcastReceiver);
        }
    }

    LoaderManager.LoaderCallbacks<Cursor> loaderCallbacks = new LoaderManager.LoaderCallbacks<Cursor>() {
        @Override
        public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
            return new CursorLoader(RecipeIngredientsAppWidgetConfigureActivity.this, RecipeProvider.CONTENT_URI, null, null, null, null);
        }

        @Override
        public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
            if (cursor.getCount() > 0) {
                // Build Spinner values
                recipes = new Recipe[cursor.getCount()];
                recipesStores = new HashMap<>();

                int index = 0;
                for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
                    recipes[index] = new Recipe(cursor);
                    recipesStores.put(recipes[index]._id, recipes[index]);

                    index += 1;
                }

                ArrayAdapter<Recipe> spinnerAdapter = new ArrayAdapter<>(RecipeIngredientsAppWidgetConfigureActivity.this, android.R.layout.simple_spinner_item, recipes);
                recipeSpinner.setAdapter(spinnerAdapter);

                Long savedRecipeId = loadRecipeIdPref(RecipeIngredientsAppWidgetConfigureActivity.this, appWidgetId);

                recipeSpinner.setSelection(spinnerAdapter.getPosition(recipesStores.get(savedRecipeId)));
            } else {
                // If no Recipe, sync from server and listen for after sync broadcast receiver
                IntentFilter syncIntentFilter = new IntentFilter();
                syncIntentFilter.addAction(RecipeUtil.ACTION_SYNC_FINISHED);

                syncBroadcastReceiver = new SyncBroadcastReceiver();
                registerReceiver(syncBroadcastReceiver, syncIntentFilter);

                RecipeUtil.syncRecipes(RecipeIngredientsAppWidgetConfigureActivity.this);
            }
        }

        @Override
        public void onLoaderReset(Loader<Cursor> loader) {

        }
    };

    private class SyncBroadcastReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            getSupportLoaderManager().restartLoader(LOADER_RECIPES, null, loaderCallbacks);
        }
    }
}

