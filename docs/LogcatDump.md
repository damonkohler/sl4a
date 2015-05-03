# Introduction #

Bug reports are significantly more useful when logcat output is attached. Logcat is just a list of debugging messages that Android produces.

First, reproduce the bug. Then use one of these methods to send the logca dump.

## From your computer ##

To get started, follow the instructions on the Android developer site for [setting up the SDK](http://developer.android.com/sdk/index.html). After that:

  1. Make sure USB debugging is enabled under Settings > Applications > Development.
  1. Reproduce the bug.
  1. Plug in your phone over USB.
  1. Run `adb logcat -d > logcat.txt` from a terminal.
  1. File an issue and attach `logcat.txt`.

## From sl4a ##
In sl4a (release 3x and above), hit:
```
  MENU-->View-->Logcat
```
From there, you have your choice of Sharing:
```
  MENU-->Share
```
... which will pop up a list of applications willing to do something with a text file,

or

```
  MENU-->Copy
```
... which will copy the entire contents into clipboard, from whence you can paste it anywhere that takes your fancy.

## That's a lot of stuff! ##
Yes, there's a lot of text included in a logcat dump. It contains all the messages from everything that has been happening recently on your phone, not just for sl4a. For brevity and security reasons, we are looking for two sorts of messages.

Messages tagged with **sl4a**:
```
V/sl4a.SimpleServer:195(26033): Bound to 127.0.0.1:39137
V/sl4a.SimpleServer$ConnectionThread:88(26033): Server thread 16 started.
V/sl4a.JsonRpcServer:74(26033): Received: {"params": [], "id": 0, "method": "checkWifiState"}
V/sl4a.JsonRpcServer:117(26033): Sent: {"error":null,"id":0,"result":false}
V/sl4a.JsonRpcServer:74(26033): Received: {"params": [true], "id": 1, "method": "toggleWifiState"}
V/sl4a.JsonRpcServer:117(26033): Sent: {"error":null,"id":1,"result":true}
V/sl4a.JsonRpcServer:74(26033): Received: {"params": [], "id": 2, "method": "wifiGetConnectionInfo"}
V/sl4a.JsonRpcServer:117(26033): Sent: {"error":null,"id":2,"result":{"mac_address":"xx:xx:xx:xx:xx:xx","ip_address":0,"hidden_ssid":false,"rssi":-200,"network_id":-1,"link_speed":24,"supplicant_state":"uninitialized"}}
V/sl4a.SimpleServer$ConnectionThread:99(26033): Server thread 16 died.
```

Actual stack traces:
```
E/AndroidRuntime( 8451): FATAL EXCEPTION: main
E/AndroidRuntime( 8451): java.lang.RuntimeException: Unable to start service com.googlecode.android_scripting.activity.ScriptingLayerService@45fbc5c8 with Intent { act=com.googlecode.android_scripting.action.LAUNCH_TERMINAL flg=0x10000000 cmp=com.googlecode.android_scripting/.activity.ScriptingLayerService (has extras) }: java.lang.NullPointerException
E/AndroidRuntime( 8451): 	at android.app.ActivityThread.handleServiceArgs(ActivityThread.java:3282)
E/AndroidRuntime( 8451): 	at android.app.ActivityThread.access$3600(ActivityThread.java:135)
E/AndroidRuntime( 8451): 	at android.app.ActivityThread$H.handleMessage(ActivityThread.java:2211)
E/AndroidRuntime( 8451): 	at android.os.Handler.dispatchMessage(Handler.java:99)
E/AndroidRuntime( 8451): 	at android.os.Looper.loop(Looper.java:144)
E/AndroidRuntime( 8451): 	at android.app.ActivityThread.main(ActivityThread.java:4937)
E/AndroidRuntime( 8451): 	at java.lang.reflect.Method.invokeNative(Native Method)
E/AndroidRuntime( 8451): 	at java.lang.reflect.Method.invoke(Method.java:521)
E/AndroidRuntime( 8451): 	at com.android.internal.os.ZygoteInit$MethodAndArgsCaller.run(ZygoteInit.java:868)
E/AndroidRuntime( 8451): 	at com.android.internal.os.ZygoteInit.main(ZygoteInit.java:626)
E/AndroidRuntime( 8451): 	at dalvik.system.NativeStart.main(Native Method)
E/AndroidRuntime( 8451): Caused by: java.lang.NullPointerException
E/AndroidRuntime( 8451): 	at com.googlecode.android_scripting.activity.ScriptingLayerService.addProcess(ScriptingLayerService.java:249)
E/AndroidRuntime( 8451): 	at com.googlecode.android_scripting.activity.ScriptingLayerService.onStart(ScriptingLayerService.java:182)
E/AndroidRuntime( 8451): 	at android.app.Service.onStartCommand(Service.java:420)
E/AndroidRuntime( 8451): 	at android.app.ActivityThread.handleServiceArgs(ActivityThread.java:3267)
E/AndroidRuntime( 8451): 	... 10 more
```

Trimming your log to just these is probably a good idea.  A note with the stack trace, though: the actual error message we're interested in here starts at the "Caused by:" line, about 13 lines down. This is typical. If you are sending a stack trace, make sure you get all of it.

## Could this be a security issue? ##
Well, yes. Quite apart the sl4a messages, there could be anything in that log, possibly including user ids and password. Probably not, because most programmers know better, but a quick check is a good idea.

A specific issue is that some sl4a messages may contain more information than you are comfortable with sharing. All rpc messages and replies are logged, and this could theoretically contain sensitive information. An example above is _wifiGetConnectionInfo_, which returns details about the current connection. In theory, a determined hacker could use the details on the MAC address and ssid to stage an attack on your phone. I personally feel the risk is low, but opinions vary. If in doubt, anonymize your data. It's probably a good habit to get into in any case.