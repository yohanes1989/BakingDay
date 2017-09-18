package id.co.webpresso.yohanes.bakingday.ui;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.graphics.Rect;
import android.support.annotation.Nullable;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import id.co.webpresso.yohanes.bakingday.R;
import id.co.webpresso.yohanes.bakingday.adapter.RecipeAdapter;
import id.co.webpresso.yohanes.bakingday.idling_resource.RecipeIdlingResource;
import id.co.webpresso.yohanes.bakingday.provider.RecipeProvider;
import id.co.webpresso.yohanes.bakingday.util.RecipeUtil;
import id.co.webpresso.yohanes.bakingday.util.Util;

public class RecipeIndexActivity extends AppCompatActivity {
    public static final int LOADER_RECIPE = 1;

    public RecyclerView recipeListRecyclerView;
    public GridLayoutManager gridLayoutManager;
    public RecipeAdapter recipeAdapter;
    public IntentFilter syncFinishedIntentFilter;
    public BroadcastReceiver syncReceiver;

    @Nullable
    RecipeIdlingResource idlingResource;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initializeUI();

        syncFinishedIntentFilter = new IntentFilter();
        syncFinishedIntentFilter.addAction(RecipeUtil.ACTION_SYNC_FINISHED);

        syncReceiver = new SyncBroadcastReceiver();

        idlingResource = Util.getIdlingResource();

        getSupportLoaderManager().restartLoader(LOADER_RECIPE, null, loaderCallbacks);
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(syncReceiver, syncFinishedIntentFilter);
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(syncReceiver);
    }

    protected void initializeUI() {
        recipeAdapter = new RecipeAdapter();
        gridLayoutManager = new GridLayoutManager(this, getResources().getInteger(R.integer.recipe_list_columns));

        recipeListRecyclerView = (RecyclerView) findViewById(R.id.recipeListRecyclerView);
        recipeListRecyclerView.setAdapter(recipeAdapter);
        recipeListRecyclerView.setLayoutManager(gridLayoutManager);

        // Set spacing between items
        recipeListRecyclerView.addItemDecoration(new RecyclerView.ItemDecoration() {
            @Override
            public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
                int space = getResources().getInteger(R.integer.recipe_card_spacing) / 2;

                parent.setPadding(space, space, space, space);
                parent.setClipToPadding(false);

                outRect.top = space;
                outRect.bottom = space;
                outRect.left = space;
                outRect.right = space;
            }
        });
    }

    private LoaderManager.LoaderCallbacks<Cursor> loaderCallbacks = new LoaderManager.LoaderCallbacks<Cursor>() {
        @Override
        public Loader<Cursor> onCreateLoader(int id, Bundle args) {
            if (id == LOADER_RECIPE) {
                if (idlingResource != null) {
                    idlingResource.setIdleState(false);
                }

                return new CursorLoader(RecipeIndexActivity.this, RecipeProvider.CONTENT_URI, null, null, null, null);
            }

            throw new IllegalArgumentException("Unsupported Loader with ID: " + id);
        }

        @Override
        public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
            if (loader.getId() == LOADER_RECIPE) {
                recipeAdapter.setCursor(data);

                // Re-fetch if there is no Recipe
                if (data.getCount() < 1) {
                    // Fetch recipes. This activity loads fetched Recipe when receiving RecipeUtil.ACTION_SYNC_FINISHED
                    RecipeUtil.syncRecipes(RecipeIndexActivity.this);
                } else {
                    if (idlingResource != null) {
                        idlingResource.setIdleState(true);
                    }
                }
            }
        }

        @Override
        public void onLoaderReset(Loader<Cursor> loader) {
            recipeAdapter.setCursor(null);
        }
    };

    private class SyncBroadcastReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            getSupportLoaderManager().restartLoader(LOADER_RECIPE, null, loaderCallbacks);
        }
    }
}
