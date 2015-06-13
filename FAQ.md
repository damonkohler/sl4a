Still having trouble? Still got questions? Try the [SL4A discussion group](http://groups.google.com/group/android-scripting).

### What is Scripting Layer for Android (SL4A) for? ###

SL4A makes it possible to quickly prototype applications for Android on the device itself using high-level scripting languages. These scripts have access to many of the APIs available to full-fledged Android applications, but with a greatly simplified interface. Have a look at some of the example [scripts](http://code.google.com/p/android-scripting/source/browse/#hg/lua/ase/scripts) to see for yourself.

### Why SL4A? Android programs are easy enough to write. ###

That's very true. Android's development environment does make life pretty easy. But, you're tied to a computer to do your work. SL4A lets you try out an idea now, in the situation where you need it, quickly. After that, you can take what you learned and turn it into a real application!

### How does SL4A work? ###

SL4A provides [Android facades](http://code.google.com/p/android-scripting/source/browse/#hg/android/AndroidScriptingEnvironment/src/com/google/ase/facade) which make APIs available via JSON RPC calls. Only the parts of Android's APIs which has been wrapped by facades are available to interpreters.

### Does SL4A compile the supported languages to Dex bytecode or is there an additional layer of interpretation? ###

It depends on the language a bit. BeanShell is run in the JVM and has an additional layer of interpretation. Lua, Python, and Perl are actually the C versions running natively. All interaction with Android is over RPC channels. Still, the RPC API is compiled to dex so there's no additional interpretation in Java (other than reflection).

### What about performance? ###

With respect to the interpreters SL4A provides, Lua and Python are both cross compiled C binaries that run in their own process. CPython is significantly more performant than Jython (especially since Android does not currently support JIT).

As for the Android facades, the API is primarily focused on making it easier to write scripts than on the performance of those scripts. That said, remember the adage "measure first, then optimize." SL4A is about rapid development after all.

If you have performance concerns for your application, it's probably better to use the standard Android SDK (or NDK) where you'll have more control over the system.

### Are scripts first-class apps from the point of view of the system? ###

Yes, mostly. You can invoke arbitrary intents and there are also plans to support broadcast receiver scripts. That is, you could write an app that consumes photos and shows up in the list of apps when the user presses Share in the Camera app.

It is not currently possible to write a script that will exit with a result (e.g. another Android activity starts your activity for result). See [issue 239](https://code.google.com/p/android-scripting/issues/detail?id=239).

### Is this a complete API bridge, or are there restrictions? ###

[BeanShell](http://www.beanshell.org/manual/contents.html), JRuby, and Rhino basically give you a complete API bridge (you can invoke Java calls directly). See the documentation for those interpreters for instruction on how to accomplish this. Cross compiled languages like Lua are more restricted. They only have access to the APIs exposed through the RPC layer. See the [API reference](ApiReference.md) for a list of currently supported APIs. The RPC layer is easy to extend.

### What do I do with all these barcodes? ###

Barcodes are a easy way to access a web link on your phone (e.g. to download SL4A). To read the barcodes, you'll need to install the [barcode scanner](InstallingBarcodeScanner.md) application.

### Text-to-Speech (TTS) isn't working! ###

First, if you're running Cupcake or another older version of Android, make sure you've [installed TTS support](InstallingTextToSpeech.md). If you have it installed, run the TTS activity and try playing the sample sound. If you can't hear it, try turning up the volume. Once that works, try your script again in SL4A.

If you have Donut or beyond, TTS comes with the phone. Check your phone's speech synthesis settings.

### Where are the sample scripts and other interpreters? ###

As of release 0.8 alpha, interpreters and sample scripts are distributed separately. Please read InstallingInterpreters for directions on how to add them.

### Is it possible to ... from my favorite scripting language? ###

All SL4A languages share the same common set of APIs. These are [documented online](ApiReference.md) and in the [API browser](ApiBrowser.md) within SL4A itself.

### Can I run scripts as root? ###

If you have a rooted phone, you may be interested in doing so. At the moment, SL4A does not support this directly. However, [issue 184](https://code.google.com/p/android-scripting/issues/detail?id=184) is tracking this feature request.

### How do I install SL4A? ###

  1. Enable "Unknown Sources" under Application settings.
  1. Install [Barcode Scanner](InstallingBarcodeScanner.md).
  1. Scan the barcode on the [home page](http://code.google.com/p/android-scripting/) to download the APK.
  1. Tap on the downloaded APK to install SL4A.

### Can I use SL4A to write a real Android application or embed SL4A scripts in my application? ###

Yes! You can [embed your script](http://code.google.com/p/android-scripting/wiki/SharingScripts#Scripts_as_APKs) in a (mostly) stand alone APK.

### Can I run scripts periodically or at specific times? ###

Not yet. We're working on it. See [issue 271](https://code.google.com/p/android-scripting/issues/detail?id=271).

### Will SL4A work on an Android emulator? ###

Yes. Use `adb install sl4a_rnn.apk` to install it to the emulator.

### Will SL4A work on my Android device that's not built on ARM (e.g. x86)? ###

Yes and no. All cross compiled interpreters are compiled for ARM. Java based interpreters like JRuby, BeanShell, and Rhino will work.

### Can I run an SL4A script from my application? ###

Yes. See [IntentBuilders.java](http://code.google.com/p/android-scripting/source/browse/android/Common/src/com/googlecode/android_scripting/IntentBuilders.java).

### Can I install other Python modules? ###

Yes. Many pure Python modules will work if you simply add them to `/sdcard/com.googlecode.pythonforandroid/extras/python` as long as they don't depend on any C Python modules that aren't included with SL4A. You can, of course, cross compile your own C Python modules. It's not for the faint of heart, but see [here](http://code.google.com/p/python-for-android/wiki/BuildingModules) for more detail,

Python eggs are supported, to a degree. The egg will have to have been compiled using [these instructions](http://code.google.com/p/python-for-android/wiki/BuildingModules). Then, download the egg into your downloads folder (this will typically be `/sdcard/Download`, and is the default used by your browser) then use the _Install Module_ button on the Python Installer. This will only work on release 4 or above. See [python-for-android](http://code.google.com/p/python-for-android) for ymore detail.

### My script stops working when the screen turns off! WTF? ###

Your phone turns the screen off, among other things, when it goes to sleep. If an application doesn't request that it keep running when the phone is asleep, it will stop running until the phone wakes up again (i.e. you turn the screen back on). Requesting that the device remain on while your application is running is called a [wake lock](http://developer.android.com/reference/android/os/PowerManager.html).

SL4A provides APIs for acquiring and releasing wake locks. It is important to note that **wake locks significantly impact battery life**. You should always release a wake lock as soon as possible.

Instead of using a wake lock, you can also enable the "Stay awake" system setting. From your home screen tap Menu, Applications, Development, and check Stay awake. With that enabled, your phone will never sleep while charging. This does affect the amount of time it takes to charge your phone, however.

### How can I develop scripts using a real keyboard? ###

Take a look at the [remote control](RemoteControl.md) wiki page.

### Can I use SL4A for UI automation? ###

No. For security reasons, it is only possible for the `shell` user to inject UI events into arbitrary applications. See [UI/Application Excersier](http://developer.android.com/guide/developing/tools/monkey.html) in the Android documentation.

### What usage information does SL4A collect? ###

If you enable "Usage Tracking", the application will collect anonymous usage information through Google Analytics.

### Where's JavaScript? ###
JavaScript is implemented using the Mozilla Rhino project and V8. Look for "Rhino" in the interpreter downloads. Or, write an HTML script to use V8.

### Where's PHP? ###
There is a PHP interpreter, which is a [separate project](http://code.google.com/p/php-for-android/). The PHP installer and interpreter can be found there.

### What's logcat? ###
Logcat is a rotating log of messages that Android produces. It's a very useful tool for programmers to work out what exactly is going on if someone reports and error. It's a requirement in our [Issues](http://code.google.com/p/android-scripting/wiki/Issues) reporting to attach a logcat wherever possible. See [LogcatDump](LogcatDump.md) for several different ways to produce.

### SL4A won't install intepreters on an Emulator ###
In fact, SL4A works fine on emulators, having been to a large degree developed on them. But these interpreters require a bit of room, and the default AVD size for a sdcard is on 16Mb. This is inadequate. I would allocate a minimum of 128Mb, and in fact commonly set mine to 512Mb. Bear in mind that it's unusual to get an Android device with less than 2Gb these days.

### How do I run python from the Command Line? ###
See this page: RunPythonAsScript

### I found a bug and I have a great idea! ###

Please file an [issue](http://code.google.com/p/android-scripting/wiki/Issues?tm=3).