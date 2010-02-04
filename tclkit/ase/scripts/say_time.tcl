# Tcl sample that uses the text to speach API to speak the time.
package require android
set android [android new]
set time [clock format [clock seconds] -format "%I %M %p on %A, %B %e %Y."]
android speak $time
