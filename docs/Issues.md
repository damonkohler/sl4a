Have a bug or feature request to file? You've come to the right place!

Have questions? Try check the [FAQ](FAQ.md) or try the [SL4A discussion group](http://groups.google.com/group/android-scripting).

## Creating a New Issue ##

Before you continue, please read over these guidelines:

  * If you have a question, check the FAQ, [API documentation](http://code.google.com/p/android-scripting/wiki/ApiReference), etc. and try the [SL4A discussion group](http://groups.google.com/group/android-scripting) first before filing an issue.
  * To avoid duplicate issues, look over existing ones before filing a new one.
  * When filing a bug, please try to attach the [logcat output](http://developer.android.com/guide/developing/tools/adb.html#logcat). See the instructions below for how to do this.
  * To file a feature request, please select the "Enhancement request" template.
  * Issues are marked as fixed when the fix is checked in. To verify the fix, you will need to either build SL4A from head, or wait until the following release. To check if the fix has been released, look at the date it was marked fixed and the date of the latest SL4A release on the [Downloads](http://code.google.com/p/android-scripting/downloads/list) page.

Continue to the [Issues List](http://code.google.com/p/android-scripting/issues/list)

## Using Logcat ##

Bug reports are significantly more useful when logcat output is attached. To get started, follow the instructions on the Android developer site for [setting up the SDK](http://developer.android.com/sdk/index.html). After that:

  1. Make sure USB debugging is enabled under Settings > Applications > Development.
  1. Reproduce the bug.
  1. Plug in your phone over USB.
  1. Run `adb logcat -d > logcat.txt` from a terminal.
  1. File an issue and attach `logcat.txt`.