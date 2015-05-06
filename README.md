# DiskPicasso

This is a local diskcache built upon Picasso library.
Cached images files are stored into Android application
data folder. A journal of cache entries are stored
in either a sql or realm based journal implementation.

It's uses the picasso transformation to intercept the
bitmap placement on the ImageView to make a cache
entry.

Using the original source path with additional
parameters such as desired with, height and bitmap
config, the cache can be queried to check if 
cached version exists or not.

Example of usage would be like ->


    private static final Config BITMAP_CONFIG = Config.RGB_565;

    private void loadPhoto(LocalPhoto photoInfo, ImageView imageView) {
        final int width = imageView.getMeasuredWidth();
        final int height = imageView.getMeasuredHeight();

        DiskPicasso instance = DiskPicasso.getInstance();
        RequestCreator cacheLoader = instance.getCachedLoader(photoInfo.getUrl(), width, height, BITMAP_CONFIG);

        if (cacheLoader != null) {
            cacheLoader.into(imageView);
        } else {
            instance.getLoader(photoInfo.getUrl(), BITMAP_CONFIG).fit().centerCrop().into(imageView);
        }
    }

