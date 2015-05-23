package com.sweetlab.diskpicasso;

import android.graphics.Bitmap;

import java.io.File;

/**
 * Journal entry. Each entry is unique, identified by two keys.
 * The file key is the reference to the original source file.
 * The primary key is based on the file key with additional uniqueness including width, height and config.
 * <p/>
 * Immutable DAO.
 */
public class CacheEntry {
    private static final long IDENTITY_MULTIPLIER = 31L;
    private final String mFileKey;
    private final long mPrimaryKey;
    private final File mFile;
    private final int mWidth;
    private final int mHeight;
    private final Bitmap.Config mConfig;
    private final int mByteSize;

    /**
     * Helper method to calculate unique key given parameters.
     *
     * @param fileKey A unique source file key.
     * @param width   A width.
     * @param height  A height.
     * @param config  A bitmap config.
     * @return Unique identity.
     */
    public static long calcPrimaryKey(String fileKey, int width, int height, Bitmap.Config config) {
        return fileKey.hashCode() + (width * IDENTITY_MULTIPLIER) + (height * IDENTITY_MULTIPLIER) + config.hashCode();
    }

    /**
     * Create a cache entry to be persisted.
     *
     * @param fileKey  The source file key.
     * @param file     The cache file.
     * @param width    The width of cached image.
     * @param height   The height of cached image.
     * @param config   The bitmap config of cached image.
     * @param byteSize The byte size of the cached image.
     */
    public CacheEntry(String fileKey, File file, int width, int height, Bitmap.Config config, int byteSize) {
        mFileKey = fileKey;
        mPrimaryKey = calcPrimaryKey(fileKey, width, height, config);
        mFile = file;
        mWidth = width;
        mHeight = height;
        mConfig = config;
        mByteSize = byteSize;
    }

    /**
     * Get the primary key.
     *
     * @return The primary key.
     */
    public long getPrimaryKey() {
        return mPrimaryKey;
    }

    /**
     * The source file key.
     *
     * @return The source file key.
     */
    public String getFileKey() {
        return mFileKey;
    }

    /**
     * Get cache file.
     *
     * @return The cache file.
     */
    public File getFile() {
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

    @Override
    public String toString() {
        return "file key " + mFileKey + " variant key = " + mPrimaryKey + " w = " + getWidth() + " h = " + getHeight() + " config = " + getConfig();
    }
}
