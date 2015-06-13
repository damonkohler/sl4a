Having trouble? Got questions? Check the [FAQ](FAQ.md) or try the [SL4A discussion group](http://groups.google.com/group/android-scripting).

## Compiling SL4A ##

If you haven't done so already, follow the instructions for [developing in Eclipse](http://developer.android.com/guide/developing/eclipse-adt.html) in the Android developer guide. Then, import all of the SL4A projects from `trunk/android/`.  You will have to set the ANDROID\_SDK classpath variable to the root of your android SDK directory.  To do this, open eclipse and go to Window -> Preferences -> Java -> Build Path -> Classpath Variables.  Click "New..." and enter "ANDROID\_SDK" for "Name", and the path of your SDK for "Path".  You will need to have at least API levels 3 and 6 installed.

### Having trouble? ###

First, check your build path. Right click on the JARs listed under libs and click Build Path, Add to Build Path for each. After that, try compiling again.

If you run into some compiler errors, don't panic. At times, it can take a few tries for everything to be generated and compile correctly. Just select Project, Clean, and clean the AndroidScriptingEnvironment project and allow it to be rebuilt. If doing this a few times doesn't fix your problems, ask for help on the [SL4A discussion group](http://groups.google.com/group/android-scripting).

Also, John K recently produced this [extremely detailed, step-by-step guide](http://jokar-johnk.blogspot.com/2011/02/how-to-make-android-app-with-sl4a.html). The first 2/3rd of which details getting the environment up and running.

## Compiling Interpreters ##

All of the interpreters exist precompiled in the source tree. They all require different steps to be compiled. See the COMPILING files in each interpreter directory for their individual compilation instructions.

## Code Structure ##
A rough guide to the structure of sl4a can be found here: CodeStructure