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
