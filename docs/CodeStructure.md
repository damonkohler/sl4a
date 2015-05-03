# Introduction #

OK, you've cloned your own copy of the source code. What now?

## Top Layer ##
Unless you are working on porting your favourite interpreter, you'll be wanting the **android** folder.
This contains a bunch of eclipse projects:

  * BluetoothFacade - Bluetooth handling routines
  * Common - The common library used by all sl4a incarnations.
  * DocumentationGenerator - Standalone classes to build the API list for the wiki page, and a doclet to update the expanded help files.
  * InterpreterForAndroid - The link between the sl4a RPC and the various interpreters.
  * InterpreterForAndroidTemplate - A template for linking your favourite interpreter to sl4a
  * QuickAction - a set a libraries for producing pretty context menus.
  * ScriptForAndroidTemplate - A template for creating a standalone apk release.
  * ScriptingLayer - the layer that handles all the actual scripting.
  * ScriptingLayerForAndroid - This is the main program. It contains all the screens you see, and all the script editing and management routines. Most of the time, this is the project you will be compiling and testing.
  * ScriptingLayerForAndroidTest - a testing suite. Probably out of date.
  * SignalStrengthFacade - Handles signal strength apis
  * TextToSpeechFacade - Text to speech apis
  * Utils - Handy utility classes.
  * WebCamFacade - Webcam apis
These are not, scrictly speaking, part of sl4a. They act as descriptors for each interpreter, managing the installation of each interpreter and describing to sl4a how to start them.
  * BeanShellForAndroid
  * !JRubyForAndroid
  * LuaForAndroid
  * PerlForAndroid
  * PythonForAndroid - This is already getting out of date, as the latest PythonForAndroid has its own project now: [Py4a](http://code.google.com/p/python-for-android/)
  * RhinoForAndroid
  * TclForAndroid

## Rough guide to how it all fits together ##
The heart of sl4a is the RPC. Each script that is started is given it's own instance of the intepreter, a text screen (if started in foreground) and a tcp connection to an instance of the RPC. _(RPC = remote procedure call)_.

The main rpcHandling can be found here: Common/src/com/googlecode/android\_scripting/jsonrpc/JsonRpcServer.java

This opens a socket, and listens for incoming requests (ie, api call).
So, for example. droid.makeToast("Hello") is formed into a JSON packet:
```
{"params": ["Hello, Android!"], "id":1, "method": "makeToast"}
```

JsonRpcServer reads this, sees if it knows what method is being asked for, and passes it to the appropriate facade for processing. (id is an incrementing number)

Every part of the api is managed by a facade.

Most facades can be found in
```
Common/src/com/googlecode/android_scripting/facade/
```
(why aren't they all there? Because common is compiled with an SDK target of 3, to run on the widest range of platforms. The other facades (Bluetooth, Webcam, etc) are compiled as seperate projects, with different platform targets.)

The class that assembles these into a coherent whole and tells JsonRpcServer where to find them is:
```
/ScriptingLayer/src/com/googlecode/android_scripting/facade/FacadeConfiguration.java
```
It runs through all the available facades, sees if they can run on the current platform, and puts them into the method list if so.

### Annotations ###
Each Rpc method is tagged with an @Rpc annotation. Each parameter is also tagged with a @RpcParameter annotation. _(These annotations can be found in Common/src/com/googlecode/android\_scripting/rpc/, if you are interested.)_

FacadeConfiguration runs through, using reflection to find these tagged methods, and puts them in the list of methods available to the RPC server. This method also turns up in the api browser, making it somewhat self documenting. Javadoc can be used for more complete notes where needed.

Adding API functionality is therefore remarkably easy.

### Linking the bits ###
Each interpreter comes with a chunk of code to include in your script to access the RPC server (ie, android.py, or android.js).
This tends to be usually very short and sweet. It sets up a link to the RPC server (it knows where this is because when launched, sl4a sets the AP\_PORT and AP\_HOST environment variables). When a request is made, (ie, droid.makeToast) typically it will catch the exception that this is an unknown method, and pass the method and associated parameters through to the RPC server as a JSON request.
This then gets processed, and a result is passed back.

This structure keeps the linking code short, and means that the script doesn't need to know details about the version of sl4a that is running.

### Glossary ###
  * RPC - remote procedure call
  * API - application programming interface
  * [reflection](http://java.sun.com/developer/technicalArticles/ALT/Reflection) -  a java technique for dyanmically querying and calling classes and methods at runtime.
  * [JSON](http://www.json.org/) - a lightweight data-interchange format.
  * [annotations](http://download.oracle.com/javase/1.5.0/docs/guide/language/annotations.html) - a java method of embedding metadata in code.
  * [javadoc](http://download.oracle.com/javase/1.5.0/docs/guide/javadoc/index.html) - embedded java documentation.