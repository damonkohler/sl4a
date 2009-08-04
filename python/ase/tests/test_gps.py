import android
import time
d = android.Android()
d.startLocating()
for i in range(10):
  e = d.receiveEvent()
  if e['result'] is None:
    print 'No result.'
  else:
    print e
    break
  time.sleep(2)
d.stopLocating()
