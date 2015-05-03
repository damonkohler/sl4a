Having trouble? Got questions? Check the [FAQ](FAQ.md) or try the [SL4A discussion group](http://groups.google.com/group/android-scripting).

# Introduction #

SL4A provides high-level support for a subset of Android's APIs. With the possible exception of BeanShell, it is not possible to directly access Android APIs (which would normally be used via the Android SDK or NDK) from SL4A scripts.

All SL4A Android APIs are available to every scripting language (except shell) and are used in the same way (other than language specific method call syntax).

# API Browser #

All API documentation is available on the device from the Help menu in the script editor. Tap any method to expand its description and signature (the required arguments, optional arguments and defaults, return value/type, and general description).

All SL4A API calls return an object with three fields:

  * id: a strictly increasing, numeric id associated with the API call.
  * result: the return value of the API call, or null if there is no return value.
  * error: a description of any error that occurred or null if no error occurred.

For more details, see the android module for each language ([Python](http://code.google.com/p/android-scripting/source/browse/python/ase/android.py), [Lua](http://code.google.com/p/android-scripting/source/browse/lua/ase/android/init.lua), [Perl](http://code.google.com/p/android-scripting/source/browse/perl/src/Cross/Android.pm), [JavaScript](http://code.google.com/p/android-scripting/source/browse/rhino/ase/android.js), [JRuby](http://code.google.com/p/android-scripting/source/browse/jruby/android.rb), and [BeanShell](http://code.google.com/p/android-scripting/source/browse/beanshell/ase/android.bsh)).

# Examples #

The following examples are designed for users who are already familiar with one or more of the supported scripting languages. If you are using SL4A to learn a new language, you should start with a general tutorial for the language and then revisit this page to learn how to use it with SL4A's Android APIs.

If you have a better, more illustrative example for your favorite language, please share it with us on the [SL4A discussion group](http://groups.google.com/group/android-scripting).

Be sure to check out the [Tutorials](Tutorials.md) page and the example scripts that are distributed with the various interpreters installed via SL4A. In addition, [code generation](http://www.youtube.com/user/damonkohler#p/c/07A81E6CE96F158B/0/4bsbzLEEdQs) via the API Browser is another way to get started.

## Python ##

```
import android

droid = android.Android()
name = droid.getInput("Hello!", "What is your name?")
print name  # name is a namedtuple
droid.makeToast("Hello, %s" % name.result)
```

## Lua ##

```
require "android"

name = android.getInput("Hello!", "What is your name?")
android.printDict(name)  -- A convenience method for inspecting dicts (tables).
android.makeToast("Hello, " .. name.result)
```

## Perl ##

```
use Android;
my $a = Android->new();
$a->makeToast("Hello, Android!");
```

## JavaScript ##

```
load("/sdcard/ase/extras/rhino/android.js");
var droid = new Android();
droid.makeToast("Hello, Android!");
```

## JRuby ##

```
require "android"
droid = Android.new
droid.makeToast "Hello, Android!"
```

## BeanShell ##

```
source("/sdcard/ase/extras/bsh/android.bsh");
droid = Android();
droid.call("makeToast", "Hello, Android!");
```

## Tcl ##

```
package require android
set android [android new]
set name [$android getInput "Hello!" "What is your name?"]
$android makeToast "Hello, $name"
```