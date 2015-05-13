package com.sweetlab.diskpicasso;

import android.graphics.Bitmap;

import java.io.File;

/**
 * Journal entry. Each entry is unique, identified by a unique identity.
 * Immutable DAO.
 */
public class CacheEntry {
    private static final long IDENTITY_MULTIPLIER = 31L;
    private final int mWidth;
    private final int mHeight;
    private final Bitmap.Config mConfig;
    private final File mFile;
    private final int mByteSize;
    private final long mIdentity;
    private final String mSourceFilePath;
    private boolean mIsLandscape;

    /**
     * Helper method to calculate unique identity given parameters.
     *
     * @param sourcePath A path.
     * @param width      A width.
     * @param height     A height.
     * @param config     A bitmap config.
     * @return Unique identity.
     */
    public static long calcIdentity(String sourcePath, int width, int height, Bitmap.Config config) {
        return sourcePath.hashCode() + (width * IDENTITY_MULTIPLIER) + (height * IDENTITY_MULTIPLIER) + config.hashCode();
    }

    /**
     * Create a cache entry to be persisted.
     *
     * @param originalPath   The path to original file.
     * @param cacheFile      The cache file.
     * @param cachedWidth    The width of cached image.
     * @param cachedHeight   The height of cached image.
     * @param cachedConfig   The bitmap config of cached image.
     * @param cachedByteSize The byte size of the cached image.
     */
    public CacheEntry(String originalPath, File cacheFile, int cachedWidth, int cachedHeight, Bitmap.Config cachedConfig, int cachedByteSize) {
        mSourceFilePath = originalPath;
        mFile = cacheFile;
        mWidth = cachedWidth;
        mHeight = cachedHeight;
        mConfig = cachedConfig;
        mByteSize = cachedByteSize;
        mIdentity = calcIdentity(originalPath, cachedWidth, cachedHeight, cachedConfig);
        mIsLandscape = mWidth >= mHeight;
    }

    /**
     * The original image path.
     *
     * @return The path to the original image file.
     */
    public String getOriginalFilePath() {
        return mSourceFilePath;
    }

    /**
     * Get a file representing the cache file.
     *
     * @return The cache file.
     */
    public File getCacheFile() {
        return mFile;
    }

    /**
     * Get the cache image width.
     *
     * @return The cache image width.
     */
    public int getWidth() {
        return mWidth;
    }

    /**
     * Get the cached image height.
     *
     * @return The cache image height.
     */
    public int getHeight() {
        return mHeight;
    }

    /**
     * Get the cached bitmap config.
     *
     * @return THe cache bitmap config.
     */
    public Bitmap.Config getConfig() {
        return mConfig;
    }

    /**
     * Get the cached image byte size.
     *
     * @return The cache bitmap size.
     */
    public int getByteSize() {
        return mByteSize;
    }

    /**
     * Get the identity if this cached entry.
     *
     * @return The identity for this cache entry.
     */
    public long getIdentity() {
        return mIdentity;
    }

    /**
     * Get if the cached dimensions are landscape. Equal width and height return true as well.
     *
     * @return True if so.
     */
    public boolean isLandscape() {
        return mIsLandscape;
    }

    @Override
    public String toString() {
        return mSourceFilePath + " w = " + getWidth() + " h = " + getHeight() + " config = " + getConfig();
    }
}
