package id.co.webpresso.yohanes.bakingday.provider;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.provider.BaseColumns;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import id.co.webpresso.yohanes.bakingday.database.RecipeSQLLiteHelper;
import id.co.webpresso.yohanes.bakingday.model.Recipe;

public class RecipeProvider extends ContentProvider {
    public static final String CONTENT_AUTHORITY
            = RecipeProvider.class
            .getPackage()
            .getName()
            .replace(".provider", "");
    public static final Uri CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY)
            .buildUpon()
            .appendPath(Recipe.TABLE_NAME)
            .build();

    public static final int CODE_RECIPE_INDEX = 100;
    public static final int CODE_RECIPE_DETAIL = 101;
    public static final int CODE_RECIPE_INGREDIENTS = 102;
    public static final int CODE_RECIPE_STEPS = 103;
    public static final int CODE_RECIPE_STEP_DETAIL = 104;

    private static UriMatcher uriMatcher = buildUriMatcher();

    private static UriMatcher buildUriMatcher() {
        UriMatcher uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

        uriMatcher.addURI(CONTENT_AUTHORITY, Recipe.TABLE_NAME, CODE_RECIPE_INDEX);
        uriMatcher.addURI(CONTENT_AUTHORITY, Recipe.TABLE_NAME + "/#", CODE_RECIPE_DETAIL);
        uriMatcher.addURI(CONTENT_AUTHORITY, Recipe.TABLE_NAME + "/#/" + Recipe.Ingredient.TABLE_NAME, CODE_RECIPE_INGREDIENTS);
        uriMatcher.addURI(CONTENT_AUTHORITY, Recipe.TABLE_NAME + "/#/" + Recipe.Step.TABLE_NAME, CODE_RECIPE_STEPS);
        uriMatcher.addURI(CONTENT_AUTHORITY, Recipe.TABLE_NAME + "/#/" + Recipe.Step.TABLE_NAME + "/#/", CODE_RECIPE_STEP_DETAIL);

        return uriMatcher;
    }

    private RecipeSQLLiteHelper sqlHelper;

    @Override
    public boolean onCreate() {
        sqlHelper = new RecipeSQLLiteHelper(getContext());

        return true;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] strings, @Nullable String s, @Nullable String[] strings1, @Nullable String s1) {
        int match = uriMatcher.match(uri);

        final SQLiteDatabase db = sqlHelper.getReadableDatabase();
        final Cursor cursor;

        switch (match) {
            case CODE_RECIPE_INDEX:
                cursor = db.query(Recipe.TABLE_NAME, strings, s, strings1, null, null, s1);
                break;
            case CODE_RECIPE_DETAIL:
                String _id = uri.getPathSegments().get(1);
                cursor = db.query(Recipe.TABLE_NAME, strings, BaseColumns._ID + " = ?", new String[]{_id}, null, null, s1);
                break;
            case CODE_RECIPE_INGREDIENTS:
                cursor = db.query(
                        Recipe.Ingredient.TABLE_NAME,
                        strings,
                        Recipe.Ingredient.COLUMN_RECIPE_ID + " = ?",
                        new String[]{uri.getPathSegments().get(1)},
                        null,
                        null,
                        Recipe.Ingredient.COLUMN_SORT_ORDER + " ASC"
                );
                break;
            case CODE_RECIPE_STEPS:
                cursor = db.query(
                        Recipe.Step.TABLE_NAME,
                        strings,
                        Recipe.Step.COLUMN_RECIPE_ID + " = ?",
                        new String[]{uri.getPathSegments().get(1)},
                        null,
                        null,
                        Recipe.Step.COLUMN_SORT_ORDER + " ASC"
                );
                break;
            case CODE_RECIPE_STEP_DETAIL:
                cursor = db.query(
                        Recipe.Step.TABLE_NAME,
                        strings,
                        Recipe.Step.COLUMN_RECIPE_ID + " = ?",
                        new String[]{uri.getPathSegments().get(1)},
                        null,
                        null,
                        Recipe.Step.COLUMN_SORT_ORDER + " ASC",
                        (Integer.valueOf(uri.getPathSegments().get(3)) - 1) + ",1"
                );
                break;
            default:
                throw new IllegalArgumentException("Unsupported URI: " + uri.toString());
        }

        cursor.setNotificationUri(getContext().getContentResolver(), uri);

        return cursor;
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        return null;
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues contentValues) {
        int match = uriMatcher.match(uri);

        final SQLiteDatabase db = sqlHelper.getWritableDatabase();

        switch (match) {
            case CODE_RECIPE_INDEX:
                long insertedId = db.insertWithOnConflict(Recipe.TABLE_NAME, null, contentValues, SQLiteDatabase.CONFLICT_REPLACE);

                getContext().getContentResolver().notifyChange(uri, null);

                return ContentUris.withAppendedId(uri, insertedId);
            case CODE_RECIPE_INGREDIENTS:
                contentValues.put(Recipe.Step.COLUMN_RECIPE_ID, uri.getPathSegments().get(1));

                long insertedIngredientId = db.insert(Recipe.Ingredient.TABLE_NAME, null, contentValues);

                if (insertedIngredientId != -1) {
                    getContext().getContentResolver().notifyChange(uri, null);
                }

                return uri;
            case CODE_RECIPE_STEPS:
                contentValues.put(Recipe.Step.COLUMN_RECIPE_ID, uri.getPathSegments().get(1));

                long insertedStepId = db.insert(Recipe.Step.TABLE_NAME, null, contentValues);

                if (insertedStepId != -1) {
                    getContext().getContentResolver().notifyChange(uri, null);
                }

                return uri;
            default:
                throw new IllegalArgumentException("Unsupported URI: " + uri.toString());
        }
    }

    @Override
    public int bulkInsert(@NonNull Uri uri, @NonNull ContentValues[] values) {
        int match = uriMatcher.match(uri);
        int insertedLength = 0;

        final SQLiteDatabase db = sqlHelper.getWritableDatabase();

        switch (match) {
            case CODE_RECIPE_INDEX:
                db.beginTransaction();

                try {
                    for (ContentValues contentValues : values) {
                        long _id = db.insertWithOnConflict(Recipe.TABLE_NAME, null, contentValues, SQLiteDatabase.CONFLICT_REPLACE);

                        if (_id != -1) {
                            insertedLength++;
                        }
                    }

                    db.setTransactionSuccessful();
                } finally {
                    db.endTransaction();
                }

                getContext().getContentResolver().notifyChange(uri, null);

                return insertedLength;
        }

        return super.bulkInsert(uri, values);
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String s, @Nullable String[] strings) {
        int match = uriMatcher.match(uri);
        int deletedRows = 0;

        SQLiteDatabase db = sqlHelper.getWritableDatabase();

        switch (match) {
            case CODE_RECIPE_INGREDIENTS:
                deletedRows = db.delete(
                        Recipe.Ingredient.TABLE_NAME,
                        Recipe.Ingredient.COLUMN_RECIPE_ID + " = ?",
                        new String[]{uri.getPathSegments().get(1)}
                );
                break;
            case CODE_RECIPE_STEPS:
                deletedRows = db.delete(
                        Recipe.Step.TABLE_NAME,
                        Recipe.Step.COLUMN_RECIPE_ID + " = ?",
                        new String[]{uri.getPathSegments().get(1)}
                );
                break;
        }

        getContext().getContentResolver().notifyChange(uri, null);

        return deletedRows;
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues contentValues, @Nullable String s, @Nullable String[] strings) {
        int match = uriMatcher.match(uri);

        int updatedRows = 0;

        switch (match) {
            case CODE_RECIPE_DETAIL:
                String _id = uri.getPathSegments().get(1);

                SQLiteDatabase db = sqlHelper.getWritableDatabase();

                updatedRows = db.update(Recipe.TABLE_NAME, contentValues, BaseColumns._ID + " = ?", new String[]{_id});

                if (updatedRows < 1) {
                    throw new SQLException("Failed to update into URI: " + uri);
                }

                break;
            default:
                throw new UnsupportedOperationException("Uri: " + uri +" not found");
        }

        getContext().getContentResolver().notifyChange(uri, null);

        return updatedRows;
    }
}
