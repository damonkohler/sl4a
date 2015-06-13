Having trouble? Got questions? Check the [FAQ](FAQ.md) or try the [SL4A discussion group](http://groups.google.com/group/android-scripting).

# Introduction #

Sometimes it's nice to be able to write scripts on your computer and run them on the phone. This is possible using SL4A's "server" support.

First, start an SL4A server from the Interpreter Manager. (From the SL4A frontpage, press Menu, then Interpreters, then Menu, then Start Server). You can either start a public or a private server (**recommended**).

If you start a public server, anyone that can reach your device's IP address can control your phone. Because of this, we recommend that you start a private server instead. However, to use a private server you will need to install the [Android SDK](http://developer.android.com/sdk/index.html) and add the adb utility to your path.

When you use a private server and set up port forwarding and environment variables as described below, then you will be able to run scripts on your computer that should also work **unmodified** when run directly on the phone. NICE. This is not true for public servers.

To use a private server, first connect your device to your computer via USB (this is not necessary if you are using a public server).

  * Note the port displayed in the SL4A notification in the Android notification area,
  * Use adb to forward the network connection to your phone, AND
  * Set the AP\_PORT environment variable.

Example: Suppose you've started a private server within SL4A which you've noted is listening on port 4321. You've connected the device to your computer with a USB cable, and you've verified that "USB debugging connected" shows up in the Android notification area.

You would then set up port forwarding like:

```
$ adb forward tcp:9999 tcp:4321
```

This command will forward all local traffic targeting port 9999 to the private-server port on the Android device (port 4321 in this example).

Next you must set up an environment variable (AP\_PORT) that the android bindings use to find the local port. Continuing with the example above, you would do:

```
$ export AP_PORT=9999  # Use set AP_PORT=9999 on Windows
```
### Specifying a fixed port ###
**New as of version r5x** (see [Unofficial](Unofficial.md)) you can specify which port you wish the server to use.
This setting can be found in **Preferences-->General-->Server Port**
This should not be set to anywhere in the common ports. I suggest setting this to something high... I use 45001

If the port is set to 0, or the specified port is already in use, another random port will be chosen.

This makes it possible to write a standard script/batch file to set up your remote control environment.

You still need to manually start the server.

## Public Servers ##
If you don't want to connect your device to the host over USB, you can use a public server. For public servers, you must additionally export the IP of the device. To extend the previous example, suppose your phone is connected over wifi on your home network and has an IP address of 192.168.0.100. You would now need to do:

```
$ export AP_PORT=4321  # Use set AP_PORT=9999 on Windows
$ export AP_HOST=192.168.0.100  # Use set AP_HOST=192.168.0.100 on Windows
```

Now you can execute Python or Lua on your PC, import the android package or module for the language, and interact with the AndroidProxy on the phone.

# For Example #

For our example, let's use Python.

First, make sure that you have Python 2.6 installed on your computer (this matches the Python version that SL4A uses on the phone).

Then, make sure you have the SL4A's [android.py](http://android-scripting.googlecode.com/hg/python/ase/android.py) module in your Python path (i.e. in site-packages or the current directory).  android.py is the **only** extra file you need; you **don't** need to do any sort of extensive SL4A install on your computer.

Then, connect your Android device via USB, start a private server, and set up adb port forwarding and the AP\_PORT environment variable as described above.

Now you can have some fun!

```
$ python
Python 2.6
Type "help", "copyright", "credits" or "license" for more information.
>>> import android  # The SL4A android.py module should be on your sys.path.
>>> droid = android.Android()  # Or android.Android(('192.168.1.101', 6789)) for a public server, where 192.168.1.101 is the public server's IP, and 6789 is its port.
>>> droid.makeToast("Hello from my computer!")
```
### Rhino Example ###
A [HOWTO for Rhino](Rhino_Remote.md).

## Copying Scripts to Your Phone ##

Once you're satisfied, you can copy script to your phone:

```
$ adb push my_script.py /sdcard/sl4a/scripts
```

Now you can execute or continue to edit the script on your phone.

# Remotely Starting SL4A #

It is possible to launch scripts on your phone via the `adb shell`:

  * Launch a script in the background.

```
$ am start -a com.googlecode.android_scripting.action.LAUNCH_BACKGROUND_SCRIPT -n com.googlecode.android_scripting/.activity.ScriptingLayerServiceLauncher -e com.googlecode.android_scripting.extra.SCRIPT_PATH /sdcard/sl4a/scripts/test.py
```

  * Launch a script in a terminal.

```
$ am start -a com.googlecode.android_scripting.action.LAUNCH_FOREGROUND_SCRIPT -n com.googlecode.android_scripting/.activity.ScriptingLayerServiceLauncher -e com.googlecode.android_scripting.extra.SCRIPT_PATH /sdcard/sl4a/scripts/test.py
```

It is also possible to launch an SL4A server via the `adb shell`:

  * Start a private server.

```
$ am start -a com.googlecode.android_scripting.action.LAUNCH_SERVER -n com.googlecode.android_scripting/.activity.ScriptingLayerServiceLauncher
```

  * Start a private server on a particular port (in this case, 45001)

```
$ am start -a com.googlecode.android_scripting.action.LAUNCH_SERVER -n com.googlecode.android_scripting/.activity.ScriptingLayerServiceLauncher --ei com.googlecode.android_scripting.extra.USE_SERVICE_PORT 45001
```

  * Start a public server.

```
$ am start -a com.googlecode.android_scripting.action.LAUNCH_SERVER -n com.googlecode.android_scripting/.activity.ScriptingLayerServiceLauncher --ez com.googlecode.android_scripting.extra.USE_PUBLIC_IP true
```

# Troubleshooting #

  * If you've mounted your SD card on your computer, the lists of scripts will appear empty in SL4A on your phone.  Don't panic.  As soon as you unmount the SD card on your computer, SL4A will display the list of scripts again.

  * If you try to set up adb port forwarding and get "no device found" errors, ensure that "USB debugging" is enabled on your phone and all proper drivers are installed on your computer.  You can enable USB debugging on your phone under Settings > Applications > Development > USB debugging.  In general, external scripting via private server will only work if "adb devices" shows your android device after you've connected it via USB.  If "adb devices" comes up empty, Google around for tips on this problem before trying to move forward with SL4A itself.