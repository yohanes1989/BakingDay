package id.co.webpresso.yohanes.bakingday.idling_resource;

import android.support.annotation.Nullable;
import android.support.test.espresso.IdlingResource;

import java.util.concurrent.atomic.AtomicBoolean;

public class RecipeIdlingResource implements IdlingResource {
    @Nullable private volatile ResourceCallback resourceCallback;
    private AtomicBoolean isIdle = new AtomicBoolean(true);

    @Override
    public String getName() {
        return getClass().getName();
    }

    @Override
    public boolean isIdleNow() {
        return isIdle.get();
    }

    @Override
    public void registerIdleTransitionCallback(ResourceCallback callback) {
        resourceCallback = callback;
    }

    public void setIdleState(boolean state) {
        isIdle.set(state);

        if (state && resourceCallback != null) {
            resourceCallback.onTransitionToIdle();
        }
    }
}
