package com.sweetlab.diskpicasso;

import android.content.Context;

import com.squareup.picasso.LruCache;
import com.squareup.picasso.Picasso;
import com.sweetlab.diskpicasso.executor.CacheExecutor;

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
            builder.memoryCache(new LruCache(memoryCacheSizeBytes));

            CacheExecutor executor = new CacheExecutor.Builder().setCore(1).setMax(2).build();
            builder.executor(executor);

            sPicassoInstance = builder.build();
            sPicassoInstance.setIndicatorsEnabled(indicatorEnabled);
            sPicassoInstance.setLoggingEnabled(loggingEnabled);
        }
    }

    /**
     * Get picasso instance. Make sure that init has been called.
     *
     * @return The single instance
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
