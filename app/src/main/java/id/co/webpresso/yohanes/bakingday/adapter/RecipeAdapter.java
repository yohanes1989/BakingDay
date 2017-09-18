package id.co.webpresso.yohanes.bakingday.adapter;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.text.DecimalFormat;

import id.co.webpresso.yohanes.bakingday.R;
import id.co.webpresso.yohanes.bakingday.model.Recipe;
import id.co.webpresso.yohanes.bakingday.provider.RecipeProvider;
import id.co.webpresso.yohanes.bakingday.ui.RecipeDetailActivity;

public class RecipeAdapter extends RecyclerView.Adapter<RecipeAdapter.RecipeViewHolder> {
    private Cursor cursor;

    @Override
    public RecipeViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View recipeCardView = layoutInflater.inflate(R.layout.recipe_card, parent, false);

        return new RecipeViewHolder(recipeCardView);
    }

    @Override
    public void onBindViewHolder(RecipeViewHolder holder, int position) {
        holder.bind();
    }

    @Override
    public int getItemCount() {
        return this.cursor != null ? cursor.getCount() : 0;
    }

    public void setCursor(Cursor cursor) {
        this.cursor = cursor;

        if (cursor != null) {
            this.cursor.moveToFirst();
        }

        notifyDataSetChanged();
    }

    class RecipeViewHolder extends RecyclerView.ViewHolder
        implements View.OnClickListener{
        public Recipe recipe;
        ImageView imageView;
        TextView titleTextView;
        TextView servingsTextView;
        TextView recipeViewTextView;

        public RecipeViewHolder(View itemView) {
            super(itemView);

            imageView = (ImageView) itemView.findViewById(R.id.recipeImage);
            titleTextView = (TextView) itemView.findViewById(R.id.recipeName);
            servingsTextView = (TextView) itemView.findViewById(R.id.recipeServings);
            recipeViewTextView = (TextView) itemView.findViewById(R.id.recipeViewText);

            itemView.setOnClickListener(this);
        }

        public void bind() {
            Context context = itemView.getContext();

            cursor.moveToPosition(getAdapterPosition());
            recipe = new Recipe(cursor);

            if (!recipe.getImage().isEmpty()) {
                Picasso
                        .with(this.itemView.getContext())
                        .load(recipe.getImage())
                        .fit()
                        .into(imageView);

                imageView.setVisibility(View.VISIBLE);
            }

            titleTextView.setText(recipe.getName());

            DecimalFormat servingsDecimalFormat = new DecimalFormat("#");

            servingsTextView.setText(
                    context.getResources().getQuantityString(R.plurals.serving_size, recipe.getServings().intValue(), servingsDecimalFormat.format(recipe.getServings()))
            );
        }

        @Override
        public void onClick(View view) {
            Intent intent = new Intent(view.getContext(), RecipeDetailActivity.class);
            intent.setData(RecipeProvider.CONTENT_URI
                            .buildUpon()
                            .appendPath(String.valueOf(recipe._id))
                            .build()
            );

            view.getContext().startActivity(intent);
        }
    }
}
