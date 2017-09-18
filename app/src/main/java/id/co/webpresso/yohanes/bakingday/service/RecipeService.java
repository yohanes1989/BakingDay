package id.co.webpresso.yohanes.bakingday.service;

import android.app.IntentService;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.support.annotation.Nullable;

import id.co.webpresso.yohanes.bakingday.R;
import id.co.webpresso.yohanes.bakingday.model.Recipe;
import id.co.webpresso.yohanes.bakingday.provider.RecipeProvider;
import id.co.webpresso.yohanes.bakingday.util.RecipeUtil;
import id.co.webpresso.yohanes.bakingday.widget.RecipeIngredientsAppWidget;

public class RecipeService extends IntentService {
    public static final String ROOT_PACKAGE_NAME
            = RecipeService.class
            .getPackage()
            .getName();
    public static final String ACTION_FETCH_REPICES = ROOT_PACKAGE_NAME + ".FETCH_RECIPES";
    public static final String ACTION_FETCH_REPICE = ROOT_PACKAGE_NAME + ".FETCH_RECIPE";

    public static final String RECIPE_ID_KEY = "recipe_id";

    public RecipeService() {
        super(RecipeService.class.getName());
    }

    /**
     * Specifically used by Widget to load Recipe
     * @param context
     * @param recipeId
     */
    public static void startActionLoadRecipe(Context context, Long recipeId, int appWidgetId) {
        Intent intent = new Intent(context, RecipeService.class);
        intent.setAction(ACTION_FETCH_REPICE);
        intent.putExtra(RECIPE_ID_KEY, recipeId);
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);

        context.startService(intent);
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();

            if (action.matches(ACTION_FETCH_REPICES)) {
                RecipeUtil.fetchAndSaveRecipes(this);
            } else if (action.matches(ACTION_FETCH_REPICE)) {
                // Specifically for handling Recipe loading in Widget
                Long recipeId = intent.getLongExtra(RECIPE_ID_KEY, 0);

                // When Widget is first added, there is no recipeID because it's not yet configured
                if (recipeId != 0) {
                    int appWidgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);

                    Uri recipeUri = RecipeProvider.CONTENT_URI
                            .buildUpon()
                            .appendPath(String.valueOf(recipeId))
                            .build();

                    Cursor recipeCursor = getContentResolver().query(
                            recipeUri,
                            null,
                            null,
                            null,
                            null
                    );

                    recipeCursor.moveToFirst();
                    Recipe recipe = new Recipe(recipeCursor);

                    AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(this);

                    appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetId, R.id.recipeIngredientAppWidgetList);
                    RecipeIngredientsAppWidget.updateAppWidget(this, appWidgetManager, recipe, appWidgetId);
                }
            }
        }
    }
}
