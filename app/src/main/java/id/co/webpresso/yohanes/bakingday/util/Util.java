package id.co.webpresso.yohanes.bakingday.util;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.annotation.NonNull;

import com.google.android.exoplayer2.ExoPlayerLibraryInfo;

import id.co.webpresso.yohanes.bakingday.idling_resource.RecipeIdlingResource;

public class Util {
    public static RecipeIdlingResource idlingResource;

    /**
     * Helper static function to get Idling Resource for Espresso test
     * @return
     */
    @NonNull
    public static RecipeIdlingResource getIdlingResource() {
        if (idlingResource == null) {
            idlingResource = new RecipeIdlingResource();
        }

        return idlingResource;
    }

    /**
     * Returns a user agent string based on the given application name and the library version.
     *
     * @param context A valid context of the calling application.
     * @param applicationName String that will be prefix'ed to the generated user agent.
     * @return A user agent string generated using the applicationName and the library version.
     */
    public static String getUserAgent(Context context, String applicationName) {
        String versionName;
        try {
            String packageName = context.getPackageName();
            PackageInfo info = context.getPackageManager().getPackageInfo(packageName, 0);
            versionName = info.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            versionName = "?";
        }
        return applicationName + "/" + versionName + " (Linux;Android " + Build.VERSION.RELEASE
                + ") " + "ExoPlayerLib/" + ExoPlayerLibraryInfo.VERSION;
    }
}
