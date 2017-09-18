package id.co.webpresso.yohanes.bakingday.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;

import id.co.webpresso.yohanes.bakingday.model.Recipe;

public class RecipeSQLLiteHelper extends SQLiteOpenHelper {
    private final static String DATABASE_NAME = "bakingday.db";
    private final static int DATABASE_VERSION = 1;

    public RecipeSQLLiteHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        String createRecipeTableQuery = "CREATE TABLE " + Recipe.TABLE_NAME + "(" +
                BaseColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                Recipe.COLUMN_NAME + " TEXT, " +
                Recipe.COLUMN_SERVINGS + " REAL, " +
                Recipe.COLUMN_IMAGE + " TEXT" +
                ");";

        String createRecipeIngredientTableQuery = "CREATE TABLE " + Recipe.Ingredient.TABLE_NAME + "(" +
                BaseColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                Recipe.Ingredient.COLUMN_RECIPE_ID + " INTEGER REFERENCES " + Recipe.TABLE_NAME + "(" + BaseColumns._ID + ") ON DELETE CASCADE, " +
                Recipe.Ingredient.COLUMN_INGREDIENT + " TEXT, " +
                Recipe.Ingredient.COLUMN_MEASURE + " TEXT, " +
                Recipe.Ingredient.COLUMN_QUANTITY + " REAL, " +
                Recipe.Ingredient.COLUMN_SORT_ORDER + " INTEGER" +
                ");";

        String createRecipeStepTableQuery = "CREATE TABLE " + Recipe.Step.TABLE_NAME + "(" +
                BaseColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                Recipe.Step.COLUMN_RECIPE_ID + " INTEGER REFERENCES " + Recipe.TABLE_NAME + "(" + BaseColumns._ID + ") ON DELETE CASCADE, " +
                Recipe.Step.COLUMN_SHORT_DESCRIPTION + " TEXT, " +
                Recipe.Step.COLUMN_DESCRIPTION + " TEXT, " +
                Recipe.Step.COLUMN_THUMBNAIL_URL + " TEXT, " +
                Recipe.Step.COLUMN_VIDEO_URL + " TEXT, " +
                Recipe.Step.COLUMN_SORT_ORDER + " INTEGER" +
                ");";

        sqLiteDatabase.execSQL(createRecipeTableQuery);
        sqLiteDatabase.execSQL(createRecipeIngredientTableQuery);
        sqLiteDatabase.execSQL(createRecipeStepTableQuery);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

    }
}
