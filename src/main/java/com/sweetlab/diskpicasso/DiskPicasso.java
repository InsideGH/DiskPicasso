package com.sweetlab.diskpicasso;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.os.Handler;
import android.os.Looper;

import com.squareup.picasso.Picasso;
import com.squareup.picasso.RequestCreator;
import com.sweetlab.diskpicasso.storage.DiskCache;
import com.sweetlab.diskpicasso.storage.UnInitializedCache;

import java.io.File;
import java.util.Collections;
import java.util.List;

/**
 * Picasso with disk caching support. Even though initialization is done in background thread, this instance is directly usable from clients.
 */
public class DiskPicasso {
    private static boolean sIsInitialized;
    private static DiskPicasso sDiskPicassoInstance;
    private final DiskCache mDiskCache;

    /**
     * Private constructor. Client must call init method.
     *
     * @param diskCache The cache to use.
     */
    private DiskPicasso(DiskCache diskCache) {
        mDiskCache = diskCache;
    }

    /**
     * Tries to find a match in the list of cache entries.
     *
     * @param list    List of cache entries
     * @param resizeX Resize x value or 0 if unknown.
     * @param resizeY Resize y value or 0 if unknown.
     * @param config  Bitmap config.
     * @return The cache entry or null of not found.
     */
    public static CacheEntry findMatch(List<CacheEntry> list, int resizeX, int resizeY, Bitmap.Config config) {
        return SizeValidator.findMatch(list, resizeX, resizeY, config);
    }

    /**
     * Validates if the cache entry matches.
     *
     * @param entry   The cache entry to validate.
     * @param resizeX Resize x value or 0 if unknown.
     * @param resizeY Resize y value or 0 if unknown.
     * @param config  Bitmap config.
     * @return True if match.
     */
    public static boolean isMatch(CacheEntry entry, int resizeX, int resizeY, Bitmap.Config config) {
        return SizeValidator.isMatch(entry, resizeX, resizeY, config);
    }

    /**
     * Initialize picasso disk cache.
     *
     * @param context Preferably android application context.
     * @param size    Size of cache in bytes.
     */
    public synchronized static void init(final Context context, int size) {
        if (sDiskPicassoInstance == null) {
            final UnInitializedCache cacheNeedInit = new DiskCache.Builder().setCacheSize(size).build(context);
            sDiskPicassoInstance = new DiskPicasso(cacheNeedInit.get());
            new Thread(new Runnable() {
                @Override
                public void run() {
                    cacheNeedInit.init();
                    new Handler(Looper.getMainLooper()).post(new Runnable() {
                        @Override
                        public void run() {
                            sIsInitialized = true;
                        }
                    });
                }
            }).start();
        }
    }

    /**
     * Get the instance.
     *
     * @return The single instance.
     */
    public synchronized static DiskPicasso getInstance() {
        return sDiskPicassoInstance;
    }

    /**
     * Get a picasso request creator for a cached image with config initialized.
     *
     * @param filePath File path to original image.
     * @param width    Cached image width.
     * @param height   Cached image height.
     * @param config   Cached bitmap config.
     * @return Request creator or null if not in cache.
     */
    public RequestCreator getCachedLoader(String filePath, int width, int height, Config config) {
        if (width != 0 && height != 0 && sIsInitialized) {
            Picasso instance = SinglePicasso.getPicasso();
            File cacheFile = mDiskCache.getExact(filePath, width, height, config);
            if (cacheFile != null) {
                return instance.load(cacheFile).config(config);
            }
        }
        return null;
    }

    /**
     * Get a picasso request creator with a post disk cache write using picasso transformation.
     *
     * @param filePath File path to original image.
     * @param config   Bitmap config to use.
     * @return Request creator with a post disk cache write.
     */
    public RequestCreator loadWithCacheWrite(String filePath, Config config) {
        RequestCreator loader = SinglePicasso.getPicasso().load(new File(filePath)).config(config);
        if (sIsInitialized) {
            CacheTransformation writeTransform = new CacheTransformation(filePath);
            writeTransform.enableDiskWrite(mDiskCache);
            loader.transform(writeTransform);
        }
        return loader;
    }

    /**
     * Get a picasso request creator with a custom transformation.
     *
     * @param filePath       File path to original image.
     * @param config         Bitmap config to use.
     * @param transformation Client custom transformation.
     * @return Request creator with a post disk cache write.
     */
    public RequestCreator loadWithCacheWrite(String filePath, Config config, CacheTransformation transformation) {
        RequestCreator loader = SinglePicasso.getPicasso().load(new File(filePath)).config(config);
        if (sIsInitialized) {
            transformation.enableDiskWrite(mDiskCache);
            loader.transform(transformation);
        }
        return loader;
    }

    /**
     * Get a list of cache entries given the provided source path.
     *
     * @param originalPath The original source path.
     * @return A unmodifiable list of entries.
     */
    public List<CacheEntry> getCacheEntries(String originalPath) {
        if (sIsInitialized) {
            return mDiskCache.get(originalPath);
        }
        return Collections.emptyList();
    }
}
