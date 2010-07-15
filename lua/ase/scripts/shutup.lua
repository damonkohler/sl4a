--Placing the phone face down will disable the ringer. Turning it face up again will enable
--the ringer.
require "android"
android.startSensing()
silent = false
while true do
  e = android.receiveEvent()
  facedown = e.result and e.result.data and  e.result.data.zforce and e.result.data.zforce < -5
  if facedown and not silent then
    android.vibrate(100)  --A short vibration to indicate we're in silent mode.
    android.toggleRingerSilentMode(true)
    silent = true
  elseif not facedown and silent then
    android.toggleRingerSilentMode(false)
    silent = false
  end
  android.sleep(1)
end
