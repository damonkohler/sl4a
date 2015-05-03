# Running Python from a shell #

The necessary environment settings required to run python from a shell script can be found here:

http://code.google.com/p/python-for-android/source/browse/python-build/standalone_python.sh

To use, make the script executable and run from the shell:
```
chmod a+x standalone_python.sh
./standalone_python.sh
```

The script at time of writing looks like this:
```
#! /bin/sh

export EXTERNAL_STORAGE=/mnt/storage
PYTHONPATH=/mnt/storage/com.googlecode.pythonforandroid/extras/python
PYTHONPATH=${PYTHONPATH}:/data/data/com.googlecode.pythonforandroid/files/python/lib/python2.6/lib-dynload
export PYTHONPATH
export TEMP=/mnt/storage/com.googlecode.pythonforandroid/extras/python/tmp
export PYTHON_EGG_CACHE=$TEMP
export PYTHONHOME=/data/data/com.googlecode.pythonforandroid/files/python
export LD_LIBRARY_PATH=/data/data/com.googlecode.pythonforandroid/files/python/lib
/data/data/com.googlecode.pythonforandroid/files/python/bin/python "$@"
```

Note that you can pass arguments to python, ie:

```
./standalone_python.sh hello.Py
```