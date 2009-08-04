--Placing the phone face down will disable the ringer. Turning it face up again will enable
--the ringer.
require "android"
android.startSensing()
android.sleep(1)  --Give the sensors a moment to come online.
silent = false
while true do
  s = android.readSensors()
  facedown = s.result and s.result.roll and s.result.roll < -9
  if facedown and not silent then
    android.vibrate(100)  --A short vibration to indicate we're in silent mode.
    android.setRingerSilent(true)
    silent = true
  elseif not facedown and silent then
    android.setRingerSilent(false)
    silent = false
  end
  android.sleep(1)
end
