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
