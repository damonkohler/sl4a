import android,time
droid = android.Android()
droid.makeToast('Hello, Android!')
droid.vibrate(300)

try:
  droid.startSensing()
  time.sleep(1)
  e=droid.eventPoll(1)
  droid.eventClearBuffer()
  droid.makeToast("Polled: "+str(e))
except:
  droid.makeToast("Unexpected error:"+str(sys.exc_info()[0]))

droid.makeToast("Done")  


