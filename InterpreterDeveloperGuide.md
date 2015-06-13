**Note:** The new interpreter API is in **DRAFT**.

## Introduction ##

Part of the SL4A project is to define an API for others to develop new interpreters that SL4A (or any other compatible project) can support. Currently, this standard is for interpreters that can be run as a binary in a separate process. This standard will be extended in the future to also support running JVM based interpreters in process.

  * [The Easy Way](#The_Easy_Way.md) is a step-by-step description of how to build an interpreter APK that is compatible with SL4A.
  * [The Way of Samurai](#The_Way_of_Samurai.md) describes how to use the [interpreter.jar](http://android-scripting.googlecode.com/hg/android/InterpreterForAndroidTemplate/libs/interpreter.jar) in your own project to interface with SL4A.
  * [The Way of Zen](#The_Way_of_Zen.md) describes the API in detail.

## The Easy Way ##

Everything that is required for creating a simple interpreter APK is provided in the template project:
  * an activity with an install/uninstall button,
  * an interpreter installer and uninstaller,
  * a zip file downloader and extractor,
  * and a content provider that describes how to use the interpreter.

  1. Download interpreter [template project archive](http://android-scripting.googlecode.com/hg/android/interpreter_for_android_template.zip).
  1. Import the template project into Eclipse: `File > Import > Existing Projects into Workspace`, click on `Select archive file` and fill in the path to your copy of interpreter\_for\_android\_template.zip.
  1. Set the ANDROID\_SDK variable, as described in the [compilation instructions](CompilingASE.md).
  1. Build the project. If Eclipse complains that gen folder is missing and/or there are build path errors, Clean/Build/Refresh should solve the problem.
  1. Rename the project and the default package `com.dummy.fooforandroid -> your_package_name` (we suggest using `Refactor > Rename`). It is important that this is unique because SL4A uses package names to identify interpreters.
  1. Update the 'package' property in AndroidManifest.xml `package="your_package_name"`.
  1. Rename the main activity `FooMain -> Your_Activity_Name`.
  1. Update `android:name` activity property in the AndroidManifest.xml `<activity android:name=".Your_Activity_Name">`.
  1. Change `android:mimeType` property of intent-filter in the AndroidManifest.xml `<data android:mimeType="script/.Your_interpreter_file_extension"/>`.
  1. Rename the content provider `FooProvider -> Your_Provider_Name`.
  1. Update `android:name` and `android:authorities` provider properties in the AndroidManifest.xml `<provider android:name=".Your_Provider_Name"`, `android:authorities="your_package_name.your_provider_name">`.
  1. Rename the application `res > values > strings.xml > app_name`.
  1. Update the [interpreter description](#Implementing_the_Interpreter_Descriptor.md).
  1. Update the [interpreter provider](http://code.google.com/p/android-scripting/source/browse/android/InterpreterForAndroid/src/com/googlecode/android_scripting/interpreter/InterpreterProvider.java).
  1. If your interpreter requires any additional setup up as part of the installation (e.g., creating temp/cache folders), you can do that in the installer's [setup method](http://code.google.com/p/android-scripting/source/browse/android/PythonForAndroid/src/com/googlecode/pythonforandroid/PythonInstaller.java).
  1. Well behaved interpreters should clean up after themselves in the uninstaller cleanup method.
  1. Export your project as an APK, install it, and check that SL4A discovers your new interpreter.

### Creating Your Interpreter Archives ###

SL4A interpreters are distributed in up to three zip files:

  * xxxx\_rxx.zip will be extracted to internal memory. This zip contains files that must be marked as executable. This is not possible in external storage space.
  * xxxx\_extras\_rxx.zip will be extracted to external storage. Tis zip contains files that don't need to be marked as executable. As much of the interpreter as possible should be included in this zip in order to avoid taking up unnecessary space in internal memory. However, there are security implications associated with using external storage since any user or process can alter the data there.
  * xxxx\_scripts\_rxx.zip will be extracted to the scripts folder. This zip contains example scripts.

When creating an interpreter archive (xxxx\_rxx.zip and xxxx\_extras\_rxx.zip), make sure that it contains a single folder with all of the files inside, the name of this folder has to match the name of the interpreter (as returned by descriptor's getName()). Script archives should contain a single folder named "scripts" with all of the example scripts inside.

## The Way of Samurai ##

If your project doesn't require the functionality provided by the interpreter template project, or you want to write you own installer, uninstaller, etc. from scratch, you can use the [interpreter.jar](http://android-scripting.googlecode.com/hg/android/InterpreterForAndroidTemplate/libs/interpreter.jar).
  1. Add a `libs` folder to your project and copy interpreter.jar into it.
  1. Right-click on the project `> Properties > Java Build Path` and go to `Libraries` tab.
  1. Click on `Add JARs...`, select `your_project/libs/interpreter.jar` and click `OK`.

The jar contains the `Utils` and `InterpreterForAndroid` projects from [the SL4A repository](http://code.google.com/p/android-scripting/source/browse/#hg/android). Instead of using the jar, you can checkout these projects from the repository and add them directly to your project's `Java Build Path > Projects`. Be sure to set the `ANDROID_SDK` variable as described in [Compiling SL4A](CompilingASE.md).

In order for SL4A to discover your interpreter, add the following intent-filter to the main activity in your `AndroidManifest.xml`:
```
  <intent-filter>
    <action android:name="com.googlecode.android_scripting.DISCOVER_INTERPRETERS"/>
    <category android:name="android.intent.category.LAUNCHER"/>
    <data android:mimeType="script/.Your_interpreter_file_extension"/>
  </intent-filter>
```

Additionally, broadcast an intent in the following way to notify SL4A that your interpreter has been installed or uninstalled:
```
  Intent intent = new Intent();
  String id = "you_package_name";  // e.g. getClass().getPackage().getName().  
  intent.setData(Uri.parse("package:" + id));  // SL4A uses "package" DataScheme
  if (isInterpreterInstalled) {
      intent.setAction(InterpreterConstants.ACTION_INTERPRETER_ADDED); // Interpreter was successfully installed.
  } else {
      intent.setAction(InterpreterConstants.ACTION_INTERPRETER_REMOVED); // Interpreter was uninstalled.
  }
  // ...
  sendBroadcast(intent);
```

Finally, make sure to provide an [interpreter descriptor](#Implementing_the_Interpreter_Descriptor.md) and extend [InterpreterProvider](http://code.google.com/p/android-scripting/source/browse/android/InterpreterForAndroid/src/com/googlecode/android_scripting/interpreter/InterpreterProvider.java) with your own implementation.

## The Way of Zen ##

If you decide to create an interpreter APK completely from scratch, here are several things you'll need to do in order to be compatible with SL4A:

  * In your `AndroidManifest.xml`, declare an intent-filter with `com.googlecode.android_scripting.DISCOVER_INTERPRETERS` action as described above [above](#The_Way_of_Samurai.md).
  * Declare and implement a content provider:
    * Make sure the authority is `your_package_name.provider_name`.
    * The provider needs to have three tables: `com.googlecode.android_scripting.base`, `com.googlecode.android_scripting.env` and `com.googlecode.android_scripting.args`.
    * On receiving a query for `com.googlecode.android_scripting.base`, the provider should respond with a single row cursor that contains all the column names defined in [InterpreterPropertyNames](http://code.google.com/p/android-scripting/source/browse/android/Utils/src/com/googlecode/android_scripting/interpreter/InterpreterPropertyNames.java) (see also  [how to implement an interpreter descriptor](#Implementing_the_Interpreter_Descriptor.md)).
    * On receiving a query for `com.googlecode.android_scripting.env`, the response should be a single row cursor where each column's name represents an environment variable name and each column's data containing the value of the variable (for example, see [PythonProvider](http://code.google.com/p/android-scripting/source/browse/android/PythonForAndroid/src/com/googlecode/pythonforandroid/PythonProvider.java)).
    * In case the query is for `com.googlecode.android_scripting.args`, the response is a single row cursor with values representing command line arguments requred to execute your interpreter (make sure that the order of values in the cursor is consistent with order of arguments in the command line).
  * Send a broadcast intent when the interpreter is added or removed as described [above](#The_Way_of_Samurai.md).

## Implementing the Interpreter Descriptor ##

The interpreter descriptor provides interpreter details (e.g. name, file extension, etc.), installation details (e.g. the location of the interpreter archive) and execution information (e.g. binary path and arguments). See the [InterpreterDescriptor](http://code.google.com/p/android-scripting/source/browse/android/Utils/src/com/googlecode/android_scripting/interpreter/InterpreterDescriptor.java) class.

In order for the default installer to find your archives, location values returned by the getInterpreterArchiveUrl(), getExtrasArchiveUrl() and getScriptsArchiveUrl() should follow normal URL syntax.

## Android Proxy RPC Client ##

RPC API, server, AP\_PORT, AP\_HOST, etc.

### RPC Authentication ###

SL4A enforces per-script security sandboxing by requiring all scrits to be authenticated by the corresponding RPC server. In order for the authentication to succeed, a script has to send the correct handshake secret to the corresponding server. This is accomplished by:

  1. reading the `AP_HANDSHAKE` environment variable
  1. and then calling the RPC method `_authenticate` with the value of `AP_HANDSHAKE` as an argument.

The `_authenticate` method must be the _first_ RPC call and should take place during the initialization of the Android library (for example, see [Rhino's](http://code.google.com/p/android-scripting/source/browse/rhino/ase/android.js) or [Python's](http://code.google.com/p/android-scripting/source/browse/python/ase/android.py) Android module).