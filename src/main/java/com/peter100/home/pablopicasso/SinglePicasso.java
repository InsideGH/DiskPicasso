package com.peter100.home.pablopicasso;

import android.content.Context;

import com.peter100.home.pablopicasso.executor.CacheExecutor;
import com.squareup.picasso.LruCache;
import com.squareup.picasso.Picasso;

/**
 * Picasso as a singleton. Needs to be initialized.
 * This class solely provides a picasso instance.
 */
public class SinglePicasso {
    private static Picasso sPicassoInstance;

    /**
     * Initialize picasso.
     *
     * @param context              Android application context preferably.
     * @param memoryCacheSizeBytes Memory cache byte size.
     * @param indicatorEnabled     Enable indicator or not.
     * @param loggingEnabled       Enable logging or not.
     */
    public synchronized static void init(Context context, int memoryCacheSizeBytes, boolean indicatorEnabled, boolean loggingEnabled) {
        if (sPicassoInstance == null) {
            Picasso.Builder builder = new Picasso.Builder(context);
            builder.executor(new CacheExecutor.Builder().build());
            builder.memoryCache(new LruCache(memoryCacheSizeBytes));
            sPicassoInstance = builder.build();
            sPicassoInstance.setIndicatorsEnabled(indicatorEnabled);
            sPicassoInstance.setLoggingEnabled(loggingEnabled);
        }
    }

    /**
     * Get picasso instance. Make sure that init has been called.
     *
     * @return
     */
    public synchronized static Picasso getPicasso() {
        return sPicassoInstance;
    }

    /**
     * Hide this to enforce initialization.
     */
    private SinglePicasso() {
    }
}
