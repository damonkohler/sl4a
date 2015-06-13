# Introduction #
Here is where you can find the latest intermediate releases, bugfixes and updates.

# Details #

Release 6 is now out there. See Release\_Notes.

Updated Help: [API Help](http://www.mithril.com.au/android/doc).

[Updated API Ref](unofficial_apiref.md)

## Development releases ##
Latest development release is here: http://www.mithril.com.au/android/sl4a_r6x.apk

r6x03 - fixed 'file not found' bug on first install (28-Jun-2013)

r6x02 - put in code to make sure local connection is ipv4

r6x01 - added cameraId to cameraCapturePicture to handle devices with no backward facing camera.

# Latest Python #
The development of Python for Android has moved to here:
http://code.google.com/p/python-for-android/

Older versions of Python would not cope properly with hashlib and other modules with shared library dependencies on Gingerbread (2.3) and above. Also, new versions of Python have much better support for external modules.

### Notes for Java interpreters ###
Beanshell and Rhino can both directly access the android api. However, many Android api calls required a context, which, due to the way they are run, these interpreters don't have. A solution is being sought... suggestions appreciated.

# Honeycomb remix #
Liam Green-Hughes has produced a version of [SL4A for Honeycomb](http://www.greenhughes.com/content/sl4a-tablet-remix-release-announcement)