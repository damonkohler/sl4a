# Question #
Is there a way to start "Sl4a" using "activitystarter" in [app inventor](http://appinventor.googlelabs.com)?

Examples here: http://appinventor.googlelabs.com/learn/reference/other/activitystarter.html

# Answer #

Yes. Use the following setup for foreground scripts: (ie, with a terminal)

```
Action: com.googlecode.android_scripting.action.LAUNCH_FOREGROUND_SCRIPT
ExtraKey: com.googlecode.android_scripting.extra.SCRIPT_PATH
ExtraValue: <fully qualified script path>
ActivityPackage: com.googlecode.android_scripting
ActivityClass: com.googlecode.android_scripting.activity.ScriptingLayerServiceLauncher
```

To run in background, change the action to:

```
Action: com.googlecode.android_scripting.action.LAUNCH_BACKGROUND_SCRIPT
```