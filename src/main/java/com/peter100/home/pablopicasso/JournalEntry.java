package com.peter100.home.pablopicasso;

import android.graphics.Bitmap;

import java.io.File;

/**
 * Journal entry. Each entry is unique, identified by a unique identity.
 * Immutable DAO.
 */
public class JournalEntry {
    private static final long IDENTITY_MULTIPLIER = 31L;
    private final int mWidth;
    private final int mHeight;
    private final Bitmap.Config mConfig;
    private final File mFile;
    private final int mByteSize;
    private final long mIdentity;
    private final String mSourceFilePath;

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
    public JournalEntry(String originalPath, File cacheFile, int cachedWidth, int cachedHeight, Bitmap.Config cachedConfig, int cachedByteSize) {
        mSourceFilePath = originalPath;
        mFile = cacheFile;
        mWidth = cachedWidth;
        mHeight = cachedHeight;
        mConfig = cachedConfig;
        mByteSize = cachedByteSize;
        mIdentity = calcIdentity(originalPath, cachedWidth, cachedHeight, cachedConfig);
    }

    /**
     * The original image path.
     *
     * @return
     */
    public String getOriginalFilePath() {
        return mSourceFilePath;
    }

    /**
     * Get a file representing the cache file.
     *
     * @return
     */
    public File getCacheFile() {
        return mFile;
    }

    /**
     * Get the cache image width.
     *
     * @return
     */
    public int getWidth() {
        return mWidth;
    }

    /**
     * Get the cached image height.
     *
     * @return
     */
    public int getHeight() {
        return mHeight;
    }

    /**
     * Get the cached bitmap config.
     *
     * @return
     */
    public Bitmap.Config getConfig() {
        return mConfig;
    }

    /**
     * Get the cached image byte size.
     *
     * @return
     */
    public int getByteSize() {
        return mByteSize;
    }

    /**
     * Get the identity if this cached entry.
     *
     * @return
     */
    public long getIdentity() {
        return mIdentity;
    }
}
