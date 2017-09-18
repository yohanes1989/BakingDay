package id.co.webpresso.yohanes.bakingday.model;

import android.database.Cursor;
import android.provider.BaseColumns;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

public class Recipe {
    public static final String TABLE_NAME = "recipe";
    public static final String COLUMN_NAME = "name";
    public static final String COLUMN_SERVINGS = "servings";
    public static final String COLUMN_IMAGE = "image";

    public Recipe(Cursor cursor) {
        this.cursor = cursor;

        this._id = cursor.getLong(cursor.getColumnIndex(BaseColumns._ID));
        this.name = cursor.getString(cursor.getColumnIndex(COLUMN_NAME));
        this.servings = cursor.getDouble(cursor.getColumnIndex(COLUMN_SERVINGS));
        this.image = cursor.getString(cursor.getColumnIndex(COLUMN_IMAGE));
    }

    private Cursor cursor;

    @SerializedName("id")
    public long _id;

    @SerializedName(COLUMN_NAME)
    private String name;

    @SerializedName(COLUMN_SERVINGS)
    private Double servings;

    @SerializedName(COLUMN_IMAGE)
    private String image;

    @SerializedName("ingredients")
    private List<Ingredient> ingredients;

    @SerializedName("steps")
    private List<Step> steps;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Double getServings() {
        return servings;
    }

    public void setServings(Double servings) {
        this.servings = servings;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public List<Ingredient> getIngredients() {
        return ingredients;
    }

    public void setIngredients(List<Ingredient> ingredients) {
        this.ingredients = ingredients;
    }

    public void setIngredients(Cursor ingredientCursor) {
        List<Ingredient> newIngredients = new ArrayList<>();

        ingredientCursor.moveToFirst();

        for (ingredientCursor.moveToFirst(); !ingredientCursor.isAfterLast(); ingredientCursor.moveToNext()) {
            newIngredients.add(new Ingredient(ingredientCursor));
        }

        this.ingredients = newIngredients;
    }

    public List<Step> getSteps() {
        return steps;
    }

    public void setSteps(List<Step> steps) {
        this.steps = steps;
    }

    public void setSteps(Cursor stepCursor) {
        List<Step> newSteps = new ArrayList<>();

        stepCursor.moveToFirst();

        for (stepCursor.moveToFirst(); !stepCursor.isAfterLast(); stepCursor.moveToNext()) {
            newSteps.add(new Step(stepCursor));
        }

        this.steps = newSteps;
    }

    @Override
    public String toString() {
        return getName();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Recipe) {
            Recipe recipe = (Recipe) obj;
            return recipe.getName() == getName() && recipe._id == _id;
        }

        return false;
    }

    public class Ingredient {
        public static final String TABLE_NAME = "ingredient";
        public static final String COLUMN_RECIPE_ID = "recipeId";
        public static final String COLUMN_QUANTITY = "quantity";
        public static final String COLUMN_MEASURE = "measure";
        public static final String COLUMN_INGREDIENT = "ingredient";
        public static final String COLUMN_SORT_ORDER = "sortOrder";

        public Ingredient(Cursor cursor) {
            this.cursor = cursor;

            this._id = cursor.getLong(cursor.getColumnIndex(BaseColumns._ID));
            this.quantity = cursor.getDouble(cursor.getColumnIndex(COLUMN_QUANTITY));
            this.measure = cursor.getString(cursor.getColumnIndex(COLUMN_MEASURE));
            this.ingredient = cursor.getString(cursor.getColumnIndex(COLUMN_INGREDIENT));
            this.sortOrder = cursor.getInt(cursor.getColumnIndex(COLUMN_SORT_ORDER));
        }

        private Cursor cursor;

        public long _id;

        private long recipeId;

        @SerializedName(COLUMN_QUANTITY)
        private Double quantity;

        @SerializedName(COLUMN_MEASURE)
        private String measure;

        @SerializedName(COLUMN_INGREDIENT)
        private String ingredient;

        private long sortOrder;

        public Double getQuantity() {
            return this.quantity;
        }

        public void setQuantity(Double quantity) {
            this.quantity = quantity;
        }

        public String getMeasure() {
            return measure;
        }

        public void setMeasure(String measure) {
            this.measure = measure;
        }

        public String getIngredient() {
            return ingredient;
        }

        public void setIngredient(String ingredient) {
            this.ingredient = ingredient;
        }

        public long getRecipeId() {
            return recipeId;
        }

        public void setRecipeId(long recipeId) {
            this.recipeId = recipeId;
        }

        public long getSortOrder() {
            return sortOrder;
        }

        public void setSortOrder(long sortOrder) {
            this.sortOrder = sortOrder;
        }
    }

    public class Step {
        public static final String TABLE_NAME = "step";
        public static final String COLUMN_RECIPE_ID = "recipeId";
        public static final String COLUMN_SHORT_DESCRIPTION = "shortDescription";
        public static final String COLUMN_DESCRIPTION = "description";
        public static final String COLUMN_VIDEO_URL = "videoURL";
        public static final String COLUMN_THUMBNAIL_URL = "thumbnailURL";
        public static final String COLUMN_SORT_ORDER = "sortOrder";

        private Cursor cursor;

        public long _id;

        private long recipeId;

        public Step(Cursor cursor) {
            this.cursor = cursor;

            this._id = cursor.getLong(cursor.getColumnIndex(BaseColumns._ID));
            this.shortDescription = cursor.getString(cursor.getColumnIndex(COLUMN_SHORT_DESCRIPTION));
            this.description = cursor.getString(cursor.getColumnIndex(COLUMN_DESCRIPTION));
            this.thumbnailURL = cursor.getString(cursor.getColumnIndex(COLUMN_THUMBNAIL_URL));
            this.videoURL = cursor.getString(cursor.getColumnIndex(COLUMN_VIDEO_URL));
            this.sortOrder = cursor.getInt(cursor.getColumnIndex(COLUMN_SORT_ORDER));
        }

        @SerializedName(COLUMN_SHORT_DESCRIPTION)
        private String shortDescription;

        @SerializedName(COLUMN_DESCRIPTION)
        private String description;

        @SerializedName(COLUMN_VIDEO_URL)
        private String videoURL;

        @SerializedName(COLUMN_THUMBNAIL_URL)
        private String thumbnailURL;

        @SerializedName("id")
        private long sortOrder;

        public String getShortDescription() {
            return shortDescription;
        }

        public void setShortDescription(String shortDescription) {
            this.shortDescription = shortDescription;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public String getVideoURL() {
            return videoURL;
        }

        public void setVideoURL(String videoURL) {
            this.videoURL = videoURL;
        }

        public String getThumbnailURL() {
            return thumbnailURL;
        }

        public void setThumbnailURL(String thumbnailURL) {
            this.thumbnailURL = thumbnailURL;
        }

        public long getRecipeId() {
            return recipeId;
        }

        public void setRecipeId(long recipeId) {
            this.recipeId = recipeId;
        }

        public long getSortOrder() {
            return sortOrder;
        }

        public void setSortOrder(long sortOrder) {
            this.sortOrder = sortOrder;
        }
    }
}
