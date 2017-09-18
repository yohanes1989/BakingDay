package id.co.webpresso.yohanes.bakingday.widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.widget.RemoteViews;

import id.co.webpresso.yohanes.bakingday.R;
import id.co.webpresso.yohanes.bakingday.model.Recipe;
import id.co.webpresso.yohanes.bakingday.provider.RecipeProvider;
import id.co.webpresso.yohanes.bakingday.service.RecipeService;
import id.co.webpresso.yohanes.bakingday.ui.RecipeDetailActivity;

/**
 * Implementation of App Widget functionality.
 * App Widget Configuration implemented in {@link RecipeIngredientsAppWidgetConfigureActivity RecipeIngredientsAppWidgetConfigureActivity}
 */
public class RecipeIngredientsAppWidget extends AppWidgetProvider {

    public static void updateAppWidget(Context context, AppWidgetManager appWidgetManager,
                                Recipe recipe, int appWidgetId) {
        RemoteViews views = renderSingleRecipe(context, recipe);

        // Instruct the widget manager to update the widget
        appWidgetManager.updateAppWidget(appWidgetId, views);
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        // There may be multiple widgets active, so update all of them
        for (int appWidgetId : appWidgetIds) {
            Long recipeId = RecipeIngredientsAppWidgetConfigureActivity.loadRecipeIdPref(context, appWidgetId);

            RecipeService.startActionLoadRecipe(context, recipeId, appWidgetId);
        }
    }

    @Override
    public void onDeleted(Context context, int[] appWidgetIds) {
        // When the user deletes the widget, delete the preference associated with it.
        for (int appWidgetId : appWidgetIds) {
            RecipeIngredientsAppWidgetConfigureActivity.deleteRecipeIdPref(context, appWidgetId);
        }
    }

    @Override
    public void onEnabled(Context context) {
        // Enter relevant functionality for when the first widget is created
    }

    @Override
    public void onDisabled(Context context) {
        // Enter relevant functionality for when the last widget is disabled
    }

    protected static RemoteViews renderSingleRecipe(Context context, Recipe recipe) {
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.recipe_ingredients_app_widget);

        if (recipe != null) {
            views.setTextViewText(R.id.recipeIngredientAppWidgetRecipeTitle, recipe.getName());

            Uri recipeUri = RecipeProvider.CONTENT_URI
                    .buildUpon()
                    .appendPath(String.valueOf(recipe._id))
                    .build();

            Intent recipeIntent = new Intent(context, RecipeDetailActivity.class);
            recipeIntent.setData(recipeUri);
            PendingIntent recipePendingIntent = PendingIntent.getActivity(
                    context,
                    0,
                    recipeIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT
            );

            views.setOnClickPendingIntent(R.id.recipeIngredientAppWidgetRecipeTitle, recipePendingIntent);

            Intent listIntent = new Intent(context, RecipeIngredientListService.class);
            // RemoteViewsFactory doesn't consider different Extra as different
            // So we use setData just so RemoteViewsFactory considers intent as different between widget instance
            listIntent.setData(recipeUri);
            listIntent.putExtra(RecipeIngredientListService.BUNDLE_RECIPE_ID_KEY, recipe._id);
            views.setRemoteAdapter(R.id.recipeIngredientAppWidgetList, listIntent);
            views.setEmptyView(R.id.recipeIngredientAppWidgetList, R.id.recipeIngredientAppWidgetEmpty);
        }

        return views;
    }
}

