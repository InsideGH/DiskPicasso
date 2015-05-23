package com.sweetlab.diskpicasso.filesystem;

import android.content.Context;
import android.graphics.Bitmap;

import com.sweetlab.diskpicasso.CacheEntry;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * File system to write and remove files from.
 */
public class FileSystem {
    private final String mCacheRootPath;
    private final int mQuality;
    private final Bitmap.CompressFormat mCompressFormat;

    /**
     * Constructor.
     *
     * @param context         Android application context.
     * @param compressQuality Bitmap compress quality.
     * @param format          Bitmap compress format.
     */
    public FileSystem(Context context, int compressQuality, Bitmap.CompressFormat format) {
        mQuality = compressQuality;
        mCompressFormat = format;
        mCacheRootPath = context.getCacheDir().getAbsolutePath();
    }

    /**
     * Create a new file and write bitmap into it.
     *
     * @param req Write request.
     * @return Journal entry of the result or null if fail.
     */
    public File write(WriteRequest req) throws IOException {
        final Bitmap bitmap = req.getBitmap();
        final String fileKey = req.getFileKey();

        final int width = bitmap.getWidth();
        final int height = bitmap.getHeight();
        final Bitmap.Config config = bitmap.getConfig();

        final long primaryKey = CacheEntry.calcPrimaryKey(fileKey, width, height, config);

        return writeFile(createFile(fileKey + primaryKey), bitmap, mCompressFormat, mQuality);
    }

    /**
     * Remove/delete a file based on journal entry information.
     *
     * @param entry Entry to remove.
     * @return True if success.
     */
    public boolean remove(CacheEntry entry) {
        File file = entry.getFile();
        return file.delete();
    }

    /**
     * Create file. Any directory tree structure will be created if needed. Exception is thrown
     * if fail.
     *
     * @param name Name including directories.
     * @return The file.
     */
    private File createFile(String name) throws IOException {
        File file = new File(mCacheRootPath + name);
        File dir = file.getParentFile();
        if (!dir.exists()) {
            dir.mkdirs();
        }
        if (!file.exists()) {
            file.createNewFile();
        }
        return file;
    }

    /**
     * Write bitmap to file. Exception is thrown if fails.
     *
     * @param file    The cache file.
     * @param src     The bitmap.
     * @param format  Bitmap compress format.
     * @param quality Bitmap compress quality.
     * @return File that was written.
     */
    private File writeFile(File file, Bitmap src, Bitmap.CompressFormat format,
                           int quality) throws IOException {
        BufferedOutputStream stream = null;
        try {
            stream = new BufferedOutputStream(new FileOutputStream(file));
            src.compress(format, quality, stream);
        } finally {
            if (stream != null) {
                stream.close();
            }
        }
        return file;
    }
}