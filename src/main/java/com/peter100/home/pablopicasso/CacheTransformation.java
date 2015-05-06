package com.peter100.home.pablopicasso;

import android.graphics.Bitmap;

import com.peter100.home.pablopicasso.storage.DiskCache;
import com.squareup.picasso.Transformation;

/**
 * This is a disk cache write transformation. If client extend this transform they must call super.transform(Bitmap source) to ensure that cache writing happens.
 */
public class CacheTransformation implements Transformation {
    private final String mFilePath;
    private DiskCache mDiskCache;

    /**
     * Constructor.
     *
     * @param filePath File path to original image.
     */
    public CacheTransformation(String filePath) {
        mFilePath = filePath;
    }

    /**
     * Use this method to enable disk cache write.
     *
     * @param diskCache The cache to use.
     */
    public void enableDiskWrite(DiskCache diskCache) {
        mDiskCache = diskCache;
    }

    @Override
    public Bitmap transform(Bitmap source) {
        if (mDiskCache != null) {
            mDiskCache.put(mFilePath, source);
        }
        return source;
    }

    @Override
    public String key() {
        return mFilePath;
    }
}
