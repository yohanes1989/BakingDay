package id.co.webpresso.yohanes.bakingday.ui;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.exoplayer2.DefaultLoadControl;
import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.LoadControl;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.Timeline;
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.TrackGroupArray;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelectionArray;
import com.google.android.exoplayer2.trackselection.TrackSelector;
import com.google.android.exoplayer2.ui.SimpleExoPlayerView;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.util.Util;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.RequestCreator;
import com.squareup.picasso.Target;

import id.co.webpresso.yohanes.bakingday.R;
import id.co.webpresso.yohanes.bakingday.idling_resource.RecipeIdlingResource;
import id.co.webpresso.yohanes.bakingday.model.Recipe;

public class RecipeStepDetailFragment extends Fragment {
    public static final int LOADER_RECIPE = 5;
    public static final int LOADER_RECIPE_STEPS = 6;
    public static final String ARG_RECIPE_CONTENT_PATH = "recipe_path";
    public static final String ARG_RECIPE_STEP_NUMBER = "recipe_step_number";
    public static final String TAG = RecipeStepDetailFragment.class.getSimpleName();

    public Recipe recipe;
    public Uri recipeUri;
    public Recipe.Step recipeStep;
    public int recipeStepNumber;

    public ImageView stepThumbnail;
    public TextView stepTitleText;
    public TextView stepDescriptionText;
    public Button stepNextButton;
    public Button stepPrevButton;
    public SimpleExoPlayerView videoPlayerView;

    public SimpleExoPlayer videoPlayer;
    private LayoutInflater layoutInflater;
    private static MediaSessionCompat mediaSession;
    private PlaybackStateCompat.Builder playerStateBuilder;
    private OnRecipeStepLoadedListener stepLoadedListener;
    private RecipeStepNavListener stepNavListener;

    @Nullable private RecipeIdlingResource idlingResource;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        // Only assign if parent Activity implements OnRecipeLoadedListener
        if (context instanceof RecipeStepDetailFragment.OnRecipeStepLoadedListener) {
            stepLoadedListener = (RecipeStepDetailFragment.OnRecipeStepLoadedListener) context;
        }

        try {
            stepNavListener = (RecipeStepNavListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + " must implement RecipeStepNavListener");
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        idlingResource = id.co.webpresso.yohanes.bakingday.util.Util.getIdlingResource();

        Bundle arguments = getArguments();

        if (!arguments.containsKey(ARG_RECIPE_CONTENT_PATH)) {
            throw new IllegalArgumentException("Argument ARG_RECIPE_CONTENT_PATH is required.");
        }

        setRecipeUri(Uri.parse(arguments.getString(ARG_RECIPE_CONTENT_PATH)));
        recipeStepNumber = arguments.containsKey(ARG_RECIPE_STEP_NUMBER) ? arguments.getInt(ARG_RECIPE_STEP_NUMBER) : 1;

        layoutInflater = inflater;

        final View rootView = layoutInflater.inflate(R.layout.fragment_recipe_step_detail, container, false);

        videoPlayerView = (SimpleExoPlayerView) rootView.findViewById(R.id.recipeStepDetailVideoPlayerView);
        stepThumbnail = (ImageView) rootView.findViewById(R.id.recipeStepDetailThumbnail);
        stepTitleText = (TextView) rootView.findViewById(R.id.recipeStepDetailTitle);
        stepDescriptionText = (TextView) rootView.findViewById(R.id.recipeStepDetailDescription);
        stepNextButton = (Button) rootView.findViewById(R.id.recipeStepDetailNextButton);
        stepPrevButton = (Button) rootView.findViewById(R.id.recipeStepDetailPrevButton);

        if (idlingResource != null) {
            idlingResource.setIdleState(false);
        }

        return rootView;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        releasePlayer();
    }

    /**
     * Method to tell fragment to start fetching Recipe from db
     * @param recipeUri
     */
    public void setRecipeUri(Uri recipeUri) {
        this.recipeUri = recipeUri;
        getLoaderManager().restartLoader(LOADER_RECIPE, null, loaderCallbacks);
    }

    protected void initializeMediaSession() {
        mediaSession = new MediaSessionCompat(getContext(), TAG);
        mediaSession.setFlags(MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS | MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS);
        mediaSession.setMediaButtonReceiver(null);

        playerStateBuilder = new PlaybackStateCompat.Builder()
            .setActions(PlaybackStateCompat.ACTION_PLAY |
                    PlaybackStateCompat.ACTION_PAUSE |
                    PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS |
                    PlaybackStateCompat.ACTION_PLAY_PAUSE);

        mediaSession.setPlaybackState(playerStateBuilder.build());
        mediaSession.setCallback(new VideoMediaSessionCallback());
        mediaSession.setActive(true);
    }

    protected void initializeMediaPlayer(Uri mediaUri) {
        if (videoPlayer == null) {
            TrackSelector trackSelector = new DefaultTrackSelector();
            LoadControl loadControl = new DefaultLoadControl();
            videoPlayer = ExoPlayerFactory.newSimpleInstance(getContext(), trackSelector, loadControl);
            videoPlayerView.setPlayer(videoPlayer);

            videoPlayer.addListener(new VideoPlayerListener());

            String userAgent = Util.getUserAgent(getContext(), getString(R.string.app_name));
            MediaSource mediaSource = new ExtractorMediaSource(
                    mediaUri,
                    new DefaultDataSourceFactory(getContext(), userAgent),
                    new DefaultExtractorsFactory(),
                    null,
                    null);
            videoPlayer.prepare(mediaSource);
            videoPlayer.setPlayWhenReady(true);
        }
    }

    protected void releasePlayer() {
        if (videoPlayer != null) {
            videoPlayer.stop();
            videoPlayer.release();
            videoPlayer = null;
        }
    }

    protected void renderStepDetail(Recipe.Step recipeStep) {
        Target thumbnailTarget = null;

        if (!recipeStep.getVideoURL().isEmpty()) {
            if (!recipeStep.getThumbnailURL().isEmpty()) {
                thumbnailTarget = new Target() {
                    @Override
                    public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                        videoPlayerView.setDefaultArtwork(bitmap);
                    }

                    @Override
                    public void onBitmapFailed(Drawable errorDrawable) {

                    }

                    @Override
                    public void onPrepareLoad(Drawable placeHolderDrawable) {

                    }
                };
            }

            initializeMediaSession();
            initializeMediaPlayer(Uri.parse(recipeStep.getVideoURL()));

            videoPlayerView.setVisibility(View.VISIBLE);
            stepThumbnail.setVisibility(View.GONE);
        }

        if (!recipeStep.getThumbnailURL().isEmpty()) {
            RequestCreator thumbnailRequest = Picasso.with(getContext()).load(recipeStep.getThumbnailURL());

            if (thumbnailTarget != null) {
                thumbnailRequest.into(thumbnailTarget);
            } else {
                thumbnailRequest.into(stepThumbnail);

                videoPlayerView.setVisibility(View.GONE);
                stepThumbnail.setVisibility(View.VISIBLE);
            }
        }

        stepTitleText.setText(String.format(
                getString(R.string.recipe_step_title),
                recipeStepNumber,
                recipeStep.getShortDescription()
        ));

        if (!recipeStep.getShortDescription().equals(recipeStep.getDescription())) {
            stepDescriptionText.setText(recipeStep.getDescription());
        }

        initButtons();
    }

    protected void initButtons() {
        if (recipeStepNumber >= recipe.getSteps().size()) {
            stepNextButton.setVisibility(View.INVISIBLE);
        } else {
            stepNextButton.setVisibility(View.VISIBLE);
            stepNextButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    stepNavListener.onRecipeStepNavClick(1);
                }
            });
        }

        if (recipeStepNumber <= 1) {
            stepPrevButton.setVisibility(View.INVISIBLE);
        } else {
            stepPrevButton.setVisibility(View.VISIBLE);
            stepPrevButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    stepNavListener.onRecipeStepNavClick(-1);
                }
            });
        }
    }

    public interface RecipeStepNavListener {
        void onRecipeStepNavClick(int step);
    }

    public interface OnRecipeStepLoadedListener {
        void onRecipeStepLoaded(Recipe recipe, int position);
    }

    LoaderManager.LoaderCallbacks<Cursor> loaderCallbacks = new LoaderManager.LoaderCallbacks<Cursor>() {
        @Override
        public Loader<Cursor> onCreateLoader(int id, Bundle args) {
            switch (id) {
                case LOADER_RECIPE:
                    return new CursorLoader(getContext(), recipeUri, null, null, null, null);
                case LOADER_RECIPE_STEPS:
                    Uri recipeStepsUri = recipeUri
                            .buildUpon()
                            .appendPath(Recipe.Step.TABLE_NAME)
                            .build();
                    return new CursorLoader(getContext(), recipeStepsUri, null, null, null, null);
            }

            return null;
        }

        @Override
        public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
            switch (loader.getId()) {
                case LOADER_RECIPE:
                    data.moveToFirst();

                    recipe = new Recipe(data);
                    getLoaderManager().restartLoader(LOADER_RECIPE_STEPS, null, loaderCallbacks);
                    break;
                case LOADER_RECIPE_STEPS:
                    recipe.setSteps(data);

                    data.moveToPosition(recipeStepNumber - 1);
                    recipeStep = recipe.new Step(data);
                    renderStepDetail(recipeStep);

                    if (stepLoadedListener != null) {
                        stepLoadedListener.onRecipeStepLoaded(recipe, recipeStepNumber);
                    }

                    break;
            }

            if (idlingResource != null) {
                idlingResource.setIdleState(true);
            }
        }

        @Override
        public void onLoaderReset(Loader<Cursor> loader) {

        }
    };

    private class VideoMediaSessionCallback extends MediaSessionCompat.Callback {
        @Override
        public void onPlay() {
            videoPlayer.setPlayWhenReady(true);
        }

        @Override
        public void onPause() {
            videoPlayer.setPlayWhenReady(false);
        }
    }

    private class VideoPlayerListener implements ExoPlayer.EventListener {

        @Override
        public void onTimelineChanged(Timeline timeline, Object manifest) {

        }

        @Override
        public void onTracksChanged(TrackGroupArray trackGroups, TrackSelectionArray trackSelections) {

        }

        @Override
        public void onLoadingChanged(boolean isLoading) {

        }

        @Override
        public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
            if((playbackState == ExoPlayer.STATE_READY) && playWhenReady) {
                playerStateBuilder.setState(PlaybackStateCompat.STATE_PLAYING,
                        videoPlayer.getCurrentPosition(), 1f);
            } else if((playbackState == ExoPlayer.STATE_READY)) {
                playerStateBuilder.setState(PlaybackStateCompat.STATE_PAUSED,
                        videoPlayer.getCurrentPosition(), 1f);
            }

            mediaSession.setPlaybackState(playerStateBuilder.build());
        }

        @Override
        public void onPlayerError(ExoPlaybackException error) {

        }

        @Override
        public void onPositionDiscontinuity() {

        }
    }
}
