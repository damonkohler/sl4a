#HOWTO run Rhino remotely.

# Introduction #

Specific instructions for running Rhino (javascript) [remotely](RemoteControl.md).

# Details #

To run rhino remotely, you need to get a copy of the version of android.js and json2.js for Rhino, and tweak slightly.

For your convenience, I've supplied tweaked versions (and a test script) [here.](http://www.mithril.com.au/android/rhino_remote.zip)

Set up a server in sl4a
View-->Interpreters-->Menu-->Start Server-->Private

Look in 'Alerts-->SL4a Service' to get remote port number, ie: 39492
From your host, run:
```
adb forward tcp:9999 tcp:39492
set AP_PORT=9999
set AP_HOST=localhost
```

Using the supplied android.js and json2.js (in zip file above) and the js.jar from [Mozilla](http://www.mozilla.org/rhino/download.html):  run the following command line:
```
java -jar js.jar testtoast.js
```

All going well, Rhino should run, and a little popup message should appear on your phone.

For you further convenience, I've put a copy of [js.jar here](http://www.mithril.com.au/android/js.jar). (js.jar is the core rhino implementation)
## Notes: ##
Changes to android.js:
  * Removed hardcode absolute path to json2.js
  * Removed authenticate step.