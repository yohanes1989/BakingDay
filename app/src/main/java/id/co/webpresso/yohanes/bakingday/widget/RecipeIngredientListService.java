package id.co.webpresso.yohanes.bakingday.widget;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import java.text.DecimalFormat;

import id.co.webpresso.yohanes.bakingday.R;
import id.co.webpresso.yohanes.bakingday.model.Recipe;
import id.co.webpresso.yohanes.bakingday.provider.RecipeProvider;

public class RecipeIngredientListService extends RemoteViewsService {
    public static final String BUNDLE_RECIPE_ID_KEY = "recipe_id";

    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        return new ListRemoteViewsFactory(this.getApplicationContext(), intent.getLongExtra(BUNDLE_RECIPE_ID_KEY, 0));
    }
}

class ListRemoteViewsFactory implements RemoteViewsService.RemoteViewsFactory {
    Context context;
    Recipe recipe;
    Uri recipeUri;
    Uri recipeIngredientsUri;
    Cursor recipeCursor;
    Cursor ingredientsCursor;

    public ListRemoteViewsFactory(Context context, Long recipeId) {
        this.context = context;

        this.recipeUri = RecipeProvider.CONTENT_URI
                .buildUpon()
                .appendPath(String.valueOf(recipeId))
                .build();

        this.recipeIngredientsUri = RecipeProvider.CONTENT_URI
                .buildUpon()
                .appendPath(String.valueOf(recipeId))
                .appendPath(Recipe.Ingredient.TABLE_NAME)
                .build();
    }

    @Override
    public void onCreate() {

    }

    @Override
    public void onDataSetChanged() {
        recipeCursor = context.getContentResolver().query(
                recipeUri,
                null,
                null,
                null,
                null
        );

        recipeCursor.moveToFirst();

        recipe = new Recipe(recipeCursor);
        recipeCursor.close();

        if (ingredientsCursor != null) {
            ingredientsCursor.close();
        }

        ingredientsCursor = context.getContentResolver().query(
                recipeIngredientsUri,
                null,
                null,
                null,
                null
        );

        recipe.setIngredients(ingredientsCursor);
        ingredientsCursor.close();
    }

    @Override
    public void onDestroy() {
        if (ingredientsCursor != null) {
            ingredientsCursor.close();
        }
    }

    @Override
    public int getCount() {
        return recipe.getIngredients() != null ? recipe.getIngredients().size() : 0;
    }

    @Override
    public RemoteViews getViewAt(int position) {
        Recipe.Ingredient ingredient = recipe.getIngredients().get(position);

        RemoteViews ingredientRemoteView = new RemoteViews(context.getPackageName(), R.layout.recipe_ingredients_app_widget_ingredient);

        DecimalFormat decimalFormat = new DecimalFormat("#");
        ingredientRemoteView.setTextViewText(R.id.recipeIngredientAppWidgetQuantity, decimalFormat.format(ingredient.getQuantity()) + " " + ingredient.getMeasure());
        ingredientRemoteView.setTextViewText(R.id.recipeIngredientAppWidgetName, ingredient.getIngredient());

        return ingredientRemoteView;
    }

    @Override
    public RemoteViews getLoadingView() {
        return null;
    }

    @Override
    public int getViewTypeCount() {
        return 1;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }
}
