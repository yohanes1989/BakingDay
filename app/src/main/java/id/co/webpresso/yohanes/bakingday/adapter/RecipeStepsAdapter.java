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
import java.util.List;

import id.co.webpresso.yohanes.bakingday.R;
import id.co.webpresso.yohanes.bakingday.model.Recipe;
import id.co.webpresso.yohanes.bakingday.provider.RecipeProvider;
import id.co.webpresso.yohanes.bakingday.ui.RecipeDetailActivity;
import id.co.webpresso.yohanes.bakingday.ui.RecipeDetailFragment;

public class RecipeStepsAdapter extends RecyclerView.Adapter<RecipeStepsAdapter.RecipeStepViewHolder> {
    private List<Recipe.Step> steps;
    private RecipeDetailFragment.OnStepClickListener onStepClickListener;

    public RecipeStepsAdapter(RecipeDetailFragment.OnStepClickListener onStepClickListener) {
        this.onStepClickListener = onStepClickListener;
    }

    @Override
    public RecipeStepViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View recipeStepCardView = layoutInflater.inflate(R.layout.recipe_step_card, parent, false);

        return new RecipeStepViewHolder(recipeStepCardView);
    }

    @Override
    public void onBindViewHolder(RecipeStepViewHolder holder, int position) {
        holder.bind();
    }

    @Override
    public int getItemCount() {
        return this.steps != null ? steps.size() : 0;
    }

    public void setSteps(List<Recipe.Step> steps) {
        this.steps = steps;

        notifyDataSetChanged();
    }

    class RecipeStepViewHolder extends RecyclerView.ViewHolder
        implements View.OnClickListener{
        Recipe.Step recipeStep;
        TextView stepNumber;
        TextView stepShortDescription;

        public RecipeStepViewHolder(View itemView) {
            super(itemView);

            stepNumber = (TextView) itemView.findViewById(R.id.recipeDetailStepNumber);
            stepShortDescription = (TextView) itemView.findViewById(R.id.recipeDetailStepShortDescription);

            itemView.setOnClickListener(this);
        }

        public void bind() {
            recipeStep = steps.get(getAdapterPosition());

            stepNumber.setText(String.valueOf(getAdapterPosition() + 1));

            stepShortDescription.setText(recipeStep.getShortDescription());
        }

        @Override
        public void onClick(View view) {
            onStepClickListener.onStepClick(getAdapterPosition() + 1);
        }
    }
}
