package com.sweetlab.diskpicasso;

import android.graphics.Bitmap;

import java.util.List;

public class SizeValidator {

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
        for (CacheEntry entry : list) {
            if (isMatch(entry, resizeX, resizeY, config)) {
                return entry;
            }
        }
        return null;
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
        int width = entry.getWidth();
        int height = entry.getHeight();

        if (resizeX == 0 && resizeY == 0) {
//            Log.d("Peter100", "SizeValidator.isMatch failed, both resizeX and resizeY is zero");
            return false;
        }
        if (resizeX != 0 && resizeY != 0) {
            if ((resizeX >= resizeY) != entry.isLandscape()) {
//                Log.d("Peter100", "SizeValidator.isMatch failed on orientation " + resizeX + " " + resizeY + " " + entry.isLandscape());
                return false;
            }
        }
        if (!entry.getConfig().equals(config)) {
//            Log.d("Peter100", "SizeValidator.isMatch failed on config " + entry.getConfig() + " " + config);
            return false;
        }
        if (resizeX != 0) {
            if (!(resizeX == width || resizeX == height)) {
//                Log.d("Peter100", "SizeValidator.isMatch failed on resizeX " + resizeX + " width = " + width + " height = " + height);
                return false;
            }
        }
        if (resizeY != 0) {
            if (!(resizeY == width || resizeY == height)) {
//                Log.d("Peter100", "SizeValidator.isMatch failed on resizeY " + resizeY + " width = " + width + " height = " + height);
                return false;
            }
        }
        return true;
    }
}
