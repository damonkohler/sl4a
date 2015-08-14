Having trouble? Got questions? Check the [FAQ](FAQ.md) or try the
[SL4A discussion group](http://groups.google.com/group/android-scripting).

# Running Python from a shell #

The necessary environment settings required to run python from a shell script
can be found
[here - py4a](../../python-for-android/tree/master/sl4atools/standalone_python2.sh)

To use, make the script executable and run from the shell:
```
chmod a+x standalone_python2.sh
./standalone_python2.sh
```

The script at time of writing looks like this:
```
#! /system/bin/sh

export EXTERNAL_STORAGE=/mnt/storage
PYTHONPATH=/mnt/storage/com.googlecode.pythonforandroid/extras/python
PYTHONPATH=${PYTHONPATH}:/data/data/com.googlecode.pythonforandroid/files/python/lib/python2.7
export PYTHONPATH
export TEMP=/mnt/storage/com.googlecode.pythonforandroid/extras/python/tmp
export PYTHON_EGG_CACHE=$TEMP
export PYTHONHOME=/data/data/com.googlecode.pythonforandroid/files/python
export LD_LIBRARY_PATH=/data/data/com.googlecode.pythonforandroid/files/python/lib/python2.7/lib-dynload
/data/data/com.googlecode.pythonforandroid/files/python/bin/python "$@"
```

Note that you can pass arguments to python, ie:

```
./standalone_python2.sh hello.py
```

<!---
 vi: ft=markdown:et:fdm=marker
 -->
