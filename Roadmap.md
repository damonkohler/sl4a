# Introduction #

Currently, the SL4A project provides:

  * Scripting language interpreters that have been compiled to run on the Android platform.
  * An abstraction layer above the Android API that simplifies common use cases (e.g. reading sensors, displaying dialogs, changing settings, etc.)
  * IPC to the abstraction layer from processes that do not have access to the native Android APIs.
  * An on-device development environment for supported scripting languages.

These concepts are all tightly coupled. Our goal is to reduce coupling to allow developers to more easily extend the SL4A system. The vision for each of these concepts is described below.

# Interpreters #

Interpreters are installed as separate APKs and [intent filters](http://developer.android.com/guide/topics/intents/intents-filters.html) will be used to find an appropriate interpreter at runtime.

## Interpreters as Binaries ##

The interpreter provides a small activity which provides the path to the binary (or executable JAR) with started for result. The calling activity will then execute the interpreter subprocess itself thus giving the interpreter the permissions of the calling activity.

The interpreter's response should also describe its capabilities:

  * The command to execute it as an interactive interpreter.
  * The command to execute it with a script.

The response should also include any environment variables or flags that should be set.

## Interpreters as Libraries ##

It is also possible to load an installed interpreter from a JAR file. The interpreter provides a small activity which provides the path to the JAR file. The calling activity loads the JAR and executes scripts using the interpreter library's API.

## Security ##

Any interpreter related data that is not copied to the interpreter activity's private data directory could be modified by any other process on the device. The interpreter installation should change the permissions of the directory tree leading up to the interpreter binary or JAR to be 755. This will allow other activities to load or execute the interpreter but not modify it. The launched interpreter will have the same permissions as the calling activity.

# Android API Facades #

To be continued...

# Facade API via JSON RPC #

To be continued...

# Mobile Development Environment #

To be continued...