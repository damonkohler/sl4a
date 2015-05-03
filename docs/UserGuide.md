Having trouble? Got questions? Check the [FAQ](FAQ.md) or try the [SL4A discussion group](http://groups.google.com/group/android-scripting).

## Introduction ##

Scripting Layer for Android (SL4A) provides interactive interpreters, script editing, and script execution for various scripting languages. The easiest way to get started is to look at the [AndroidFacadeAPI](AndroidFacadeAPI.md), the example scripts that are installed with the interpreters (see Intepreter Manager below), and the various [Tutorials](Tutorials.md).

## Starting SL4A ##

After starting SL4A, you'll see a list of all installed scripts. If this is your first time starting SL4A, you likely won't have any! So, we'll come back to the script list after you've installed an intepreter.

## Interpreters ##

Before you can do anything exciting with SL4A, you'll need to install an interpreter (only "Shell" and "HTML and JavaScript" are included by default).

The list of interpreters can be accessed from the scripts activity menu by tapping "View" and then "Interpreters." From there, new interpreters can be downloaded and installed from the menu by tapping "Add." Interpreters that support interactive terminals can be launched via the interpreter manager by tapping the interpreter name in the list.

For details, see the [installing interpreters](InstallingInterpreters.md) guide.

## Scripts ##

Once you've finished installing your interpreter(s), you can run scripts, create new ones, or edit existing ones.

To add a new script, press the menu button, choose "Add" and then choose the type of script you want to create. To edit an existing script, tap on the script name and then tap on the pencil icon.

### Menu Options ###
If "QuickActions Menu" is set in preferences, the menu that comes up when you tap a script item is:
![http://android-scripting.googlecode.com/files/sl4a_quickaction.png](http://android-scripting.googlecode.com/files/sl4a_quickaction.png)

In order:
  * Run (with Terminal)
  * Run in Background
  * Edit
  * Rename
  * Delete
  * Edit in External Editor. Check out [External Editors](UsefulLinks#External_Editors.md)

If QuickActions is not set, a text selection box will appear instead.

Hitting the Menu button will produce:
  * Add - add new script
  * View - go to Interpeters, Triggers or Logcat screens
  * Search - search for a script. Hit Back to exit search mode.
  * Preferences - Set Preferences
  * Refresh - reload script list
  * Help - display associated help files.

## Editor ##

The top text box is the filename of the script. The lower text box is the script content.

In order for SL4A to know what interpreter to launch your script with, it will look at the file extension (e.g. use .py and .lua for Python and Lua respectively).

### Menu Options ###
  * Save & Exit - Save and exit
  * Save & Run - Save the current script and execute it.
  * Preferences - Preferences screen
  * API Browser - Brings up a list of supported api functions. These can be automatically inserted into your code, which saves typing.
  * Help - display associated help files
  * Share - Share script with others, typically by email. Which options come up will depend on what applications are installed on your device.
  * Goto - go to a specific line number in your code. Also displays your current line number.


## Triggers ##

To be continued...

## Logcat ##

To be continued...

## Home Screen ##

By long tapping on the home screen, you can create a shortcut to an existing script or add a live folder that contains all of your scripts.

## Locale Plugin ##

SL4A includes a [Locale](http://www.androidlocale.com/) plugin which allows executing scripts via Locales situation engine. For example, you could launch a script when you arrive at work, or at 5PM, or when a particular Wifi network is in range.

## Preferences ##
Available preference options:
### General ###
  * **Usage Tracking** - enable gathering of anonymous Google Analytics statistics
  * **Server Port** - when starting a remote server, setting this to a non zero value will cause the server to attempt to listen on this port. It is not recommended to set this to a common port number. We recommend something like 45001
### Script Manager ###
  * **Show All Files** - allow editing of any files in the scripts folder, nut just known types. This does not work on binary files.
  * **Use QuickActions Menu** - by default, sl4a will use a [QuickAction](#Menu_Options.md) menu. Not all builds of Android can cope with this. If you have problems with this menu, or just prefer the text version, set this option to Off.
### Script Editor ###
  * **Font Size** - Choose font size
  * **Force API Browser** - If the extended help won't zoom, try setting this to use the default Android browser instead of WebView.
  * **Enable Auto Close** - The inbuilt sl4a editor will attempt to autocomplete things like braces and quotes, depending on the interpreter. This is sometimes inconvenient, so you can now turn it off.
  * **No Wrap** - If enabled, word wrapping is turned off.
  * **Auto Indent** - If enabled, a new line will be automatically indented to line up with the previous line.
### Terminal ###
  * **Scrollback Size** - size of scrollback buffer to keep in memory for each console
  * **Font Size** - Terminal font size
  * **Encode** - Character encoding for terminal font.
  * **Rotate Mode** - Behaviour of terminal when screen rotation changes.
  * **Colors** - Define terminal colors
  * **Full Screen** - Hide status bar while in console.
  * **DEL Key** - Key code sent when DEL key is hit (Backspace or Delete)
  * **Directory Shortcuts** - Select how to use Alt for '/' and Shift for Tab
  * **Camera Shortcut** - keycode to send on the camera button
  * **Keep Screen Awake** - Prevent screen from turning off when working in a console.
  * **Bumpy Arrows** - Vibrate when sending arrow keys from trackball.
  * **Hide Keyboard** - Hide soft keyboard in terminal on startup.
### Terminal Bell ###
  * **Audible Bell** - enable sound on bell.
  * **Bell Volume** - select bell volume
  * **Vibate on Bell** - vibrate on bell.
### Trigger Behaviour ###
  * **Hide Notifications** - the default behaviour on responding to triggers includes showing a notification. If the trigger event is happening too quickly, this can cause problem. Hiding notifications helps this.