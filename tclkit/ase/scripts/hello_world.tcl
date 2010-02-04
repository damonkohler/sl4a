# Display a simple message using Tcl in ASE.
package require android
set android [android new]
$android makeToast "Hello, Android!"
