This is now mostly obsolete, partly because Python development has been moved into it's own sub-project. Check out [Building Modules](http://code.google.com/p/python-for-android/wiki/BuildingModules)

This is only needed if the python modules use native code (c or c++
files). Straight python modules need only be copied into the extras folder.

This also assumes a fairly straight forward setup, example module:
twisted or zope.

Grab latest version of my repository clone (rjmatthews62-android-
scripting)

Grab and build latest android source http://source.android.com/

I don't think you need to rebuild python, it just needs the config
files. (I **think**)

Make sure you have python installed on your host computer. (pref 2.6,
don't know if this is critical)

Grab target source tarball, ie:
http://tmrc.mit.edu/mirror/twisted/Twisted/10.2/Twisted-10.2.0.tar.bz2

Unpack into a directory of your choosing.

cd to the folder you've just unpacked, ie: cd Twisted-10.2.0

It should have setup.py in it.
In the followed shell script, change SL4A\_TRUNC to where ever you put
the android-scripting repository.
change ANDROID\_SRC to where your android source is installed.

```
export SL4A_TRUNC=/home/robbie/android-scripting
export ANDROID_SRC=/home/robbie/android-source-gingerbread
export PATH=$PATH:$ANDROID_SRC/prebuilt/linux-x86/toolchain/arm-eabi-4.3.1/bin
export PATH=$PATH:$SL4A_TRUNC/tools/agcc
export AR=arm-eabi-ar
export PYBASE=$SL4A_TRUNC/python/src
export INCLUDES="-I$PYBASE -I$PYBASE/Include"
export OPT="-DNDEBUG -g -fwrapv -O3 -Wall -Wstrict-prototypees -fno-short-enums $INCLUDES"
export CXX=agcc
export LDSHARED="agcc -shared"
export RANLIB=arm-eabi-ranlib
export CC=agcc
python -E setup.py build
```

If all goes well, you should end up with a new folder called build/
lib(host\_target). And nestled in there should be all the needed module
files, which can be copied into:
  * If vanilla python: /sdcard/com.googlecode.pythonforandroid/extras/python
  * If contains shared libs (.so): /data/data/com.googlecode.pythonforandroid/files/python/lib/python2.6.  However, unless you have a rooted Android, you will not be able to do this. The latest python installer will do this for you using "Import Module"

"Import Module" assumes zips build in the following fashion:

In the example of twisted, I had: /home/robbie/Twisted-10.2.0/build/lib.linux-x86\_64-2.6, with a single folder, "twisted".
To distribute, I went
```
cd build/lib*
zip ~/twisted.zip -r twisted
```

There is the beginnings of a precompiled module repository [here.](http://www.mithril.com.au/android/modules)
Hope that helps someone.