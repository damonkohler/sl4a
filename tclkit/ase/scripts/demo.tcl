# responses look like:
# {"error": null, "id": 1, "result": null}
# {"error": null, "id": 2, "result": 0}
# {"error": null, "id": 3, "result": {"data": "content://contacts/people/3#Internt;end"}}
package require Tcl
package require android
set android [android new]

puts "auto_path: $auto_path"

$android vibrate

#$android getRingerVolume
#$android showContacts
#$android getInput "title string" "Enter something"
#$android scanBarcode
#$android pickContact
#$android webSearch "android scripting tclsh"

#set time [clock format [clock seconds] -format "%I %M %p on %A, %B %e %Y."]
#$android speak $time

$android makeToast "You are using Tcl [info patchlevel] on Android!!"

set pkgs [$android getRunningPackages]


flush stdout
after 1000 {set ::forever 1} ; vwait ::forever
