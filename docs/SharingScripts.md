Currently, the preferred way to share small scripts with others is via barcode. The easiest way to generate a barcode is to use the ZXing project's online [QR Code Generator](http://zxing.appspot.com/generator/).

  1. Open the Contents drop down and choose Text.
  1. On the first line of the Text Content, enter the name of the script (e.g. hello\_world.py).
  1. Below that, paste the script content.
  1. Open the Size drop down and choose L.
  1. Click Generate.
  1. Embed or share the resulting barcode image with your friends.

To download the script to your phone:

  1. Launch SL4A or return the scripts list.
  1. Press the Menu button.
  1. Tap Add.
  1. Tap Scan Barcode.
  1. Scan the barcode and SL4A will add the script to your list.

A QR code can encode 4,296 characters of content. So, this is only effective for small scripts.

## Scripts as APKs ##

There are several ways to publish your script as an APK.
The following steps describe how to do that using Eclipse IDE.

  1. Download script [template project archive](http://android-scripting.googlecode.com/hg/android/script_for_android_template.zip).
  1. Import the template project into Eclipse: `File > Import > Existing Projects into Workspace`, click on `Select archive file` and fill in the path to your copy of script\_for\_android\_template.zip.
  1. Set the ANDROID\_SDK variable, as described in the [compilation instructions](CompilingASE.md).
  1. Build the project. If Eclipse complains that gen folder is missing and/or there are build path errors, Clean/Build/Refresh should solve the problem.
  1. Rename the project and the default package `com.dummy.fooforandroid -> your_package_name` (we suggest using `Refactor > Rename`). It is important that this is unique because Android system uses package name as an identifier of your APK.
  1. Update the `package` property in AndroidManifest.xml `package="your_package_name"`.
  1. Replace the `script.py` in `res > raw` with your script. You can either use the default name `script.your_extension` or you can name it however you like, in which case make sure to change `R.raw.script` (Script.java:10) to `R.raw.your_script_name`.
  1. Place any additional files (ie, html) into 'res > raw' with your script. They'll be unpacked into the same scripts folder.
  1. In AndroidManifest.xml uncomment all the permissions that are required to execute your script.
  1. Create and sign an APK using ADT Export Wizard as described in the [Android's Dev Guide](http://developer.android.com/guide/publishing/app-signing.html).

The following steps describe how to build an APK using Ant.

  1. Download script [template project archive](http://android-scripting.googlecode.com/hg/android/script_for_android_template.zip).
  1. Open a command-line and extract the archive: `unzip -d <path/project_directory> script_for_android_template.zip`.
  1. Set the ANDROID\_SDK variable to point to the root of your Android SDK directory: `export ANDROID_SDK=<SDK_root>`.
  1. Navigate to the root directory of your project and configure your package name: `sh configure_package.sh <your_fully_qualified_package_name>` (`sh configure_package.sh com.dummy.fooforandroid` by default).
  1. Rename the project name in build.xml.
  1. Replace the `script.py` in `res > raw` with your script. You can either use the default name `script.your_extension` or you can name it however you like, in which case make sure to change `R.raw.script` (Script.java:10) to `R.raw.your_script_name`.
  1. Place any additional files (ie, html) into 'res > raw' with your script. They'll be unpacked into the same scripts folder.
  1. In AndroidManifest.xml uncomment all the permissions that are required to execute your script.
  1. Follow [Android's Dev Guide](http://developer.android.com/guide/developing/other-ide.html#ReleaseMode) to build and sign your application.

For a really detailed walk through, see [John K's Blog](http://jokar-johnk.blogspot.com/2011/02/how-to-make-android-app-with-sl4a.html).

## Updating your Template to the latest libraries ##
Sl4a is being continually updated, whereas your standalone script will be using a static snapshot of whenever you created your copy from the template.

To get the latest version of the template, open [script\_for\_android\_template.zip](http://android-scripting.googlecode.com/hg/android/script_for_android_template.zip) and extract:
```
libs/script.jar
libs/armeabi/libcom_googlecode_android_scripting_Exec.so
```
into your project.

If using eclipse, remember to refresh and clean your project.

NB: libcom\_googlecode\_android\_scripting\_Exec.so has only been updated once so far, whereas script.jar is being constantly updated.

If you have a full version of the SL4A source installed, and wish to make your own version of script.jar etc, run build.xml in ScriptForAndroidTemplate as a Ant Build.

## Embedding Interpreters ##
There has been some work done to embed interpeters into a single APK, including support files.

From Daniel Oppenheim and Anthony Prieur:

Embedding the Python interpreter and Python scripts into an APK that doesn't require further downloads is now complete. Wiki's with tutorials have been added, and there's a demo APK in Downloads: http://code.google.com/p/android-python27/

This was also forked to a project for embedding the Perl Interpreter and Perl scripts, which is now complete as well, along with Wiki tutorials and a demo APK:
http://code.google.com/p/perl-android-apk/

We hope to add more interpreters, and welcome contributors who have experience with them.