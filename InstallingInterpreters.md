Only shell is included with the ASE install. Other interpreters must be installed separately. To add another interpreter:

  1. Press the menu button
  1. Tap "View"
  1. Tap "Interpreters"
  1. Press the menu button
  1. Tap "Add"
  1. Select the interpreter you would like to install

The selected interpreter will be downloaded via the Android web browser. Once it's finished downloading, tap the download notification to start the installation process as you would for any other downloaded APK.

After installing the interpreter APK, start it and tap the "Install" button. The interpreter, interpreter extras, and example scripts (if available) will be downloaded to /sdcard/xxx.zip and then extracted to /data/data/com.googlecode.android\_scripting/files/xxx,
/sdcard/sl4a/extras/xxx, and /sdcard/sl4a/scripts respectively.

If you experience problems while installing interpreters:
  1. Check that your network connection is working by trying to load a web page in your browser.
  1. Try to add the interpreter again. SL4A should recover from most installation failures.
  1. Uninstall and reinstall the interpreter. You can uninstall the interpreter through the "Manage applications" menu in your device's settings.

### Troubleshooting ###

  * I tried to install an interpreter but I still only see "No scripts!" in the script manager.
> It's likely that the installation failed. Try again.
  * SL4A reported that "Extracting xxx failed."
> Delete all ZIP files from /sdcard and then try the installation again.
  * It still doesn't work! Please [file a bug](http://code.google.com/p/android-scripting/wiki/Issues?tm=3).