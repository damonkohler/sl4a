Introduction
===

Scripting Layer for Android (SL4A) brings scripting languages to Android by
allowing you to edit and execute scripts and interactive interpreters directly
on the Android device. These scripts have access to many of the APIs available
to full-fledged Android applications, but with a greatly simplified interface
that makes it easy to get things done.

Scripts can be run interactively in a terminal and in the background.  Python,
Perl, JRuby, Lua, BeanShell, JavaScript, Tcl, and shell are currently supported,
and we're planning to add more. See the [SL4A Video
Help](http://www.youtube.com/playlist?list=PL07A81E6CE96F158B) playlist on
YouTube for various demonstrations of SL4A's features.

SL4A is designed for developers and is _alpha_ quality software.

Instructions
===

Gradle
---
* Clone this project, and type below in your terminal:

```shell
$ cd android/ScriptingLayerForAndroid
$ echo sdk.dir=/path/to/android-sdk > local.properties
$ sh /path/to/android-stdudio/gradle/gradle-2.2.1/bin/gradle assembleDebug
Building ??% ...
$ ls build/outputs/apk/
ScriptingLayerForAndroid-arm-debug.apk
```

  Please replace /path/to strings to your environment or installed place.

* Connect you phone or launch a virtual machine, then send the apk to them:

```shell
$ /path/to/android-sdk/platform-tools/adb install build/outputs/apk/ScriptingLayerForAndroid-arm-debug.apk
```

* Install the interpreters apk in Android. (each interpreters is not build yet.)
* Enjoy scripting!

Android Studio
---
* Clone this project, and import it into Android stdudio.
* Select "Import project"
* Select the folder: cloned/project/android/Scriptinglayerforandroid
* Configure the gradle path: /path/to/android-stdudio/gradle/gradle-2.2.1
* Configure the android sdk path.

After Android Studio launch,

* Select the menu: "Build" >> "Build Module"
* Select the menu: "Run" >> "Run"
* Connect your phone or launch a virtual machine, compiled apk will be installed.


Eclipse
---
* Clone this project, and import it into Eclipse. Make sure that the Java compliance
level is at 1.6 (right-click any project, go to Properties > Java Compiler > Configure Workspace Settings
and select 1.6.

* Make sure that you have all the appropriate Android SDKs installed through Eclipse. You can determine
this by looking at what errors prop up.

* Make sure the environment variable ANDROID_SDK is set, by going to Window > Preferences > Java >
Build Path > Classpath Variables and creating a new variable ANDROID_SDK that points to the android-sdks
folder.

* Make sure you clean all projects by going to Project > Clean.

* If there are still issues, look at this to find tidbits you could possibly do:
http://jokar-johnk.blogspot.co.nz/2011/02/how-to-make-android-app-with-sl4a.html
