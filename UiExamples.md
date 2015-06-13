# Introduction #

The UiFacade has some powerful features for managing a wide variety of dialog boxes and menus.
It also supports the powerful [webView](UsingWebView.md) function.
Help files are available [here](http://www.mithril.com.au/android/doc/UiFacade.html)

Here is some sample code to make things event clearer.

_All of these examples assume you are running in a foreground terminal._
# Basic Alert Box #
Bring up a basic alert box.

## uitest.py ##
```
import android
droid=android.Android()
droid.dialogCreateAlert("I like swords.","Do you like swords?")
droid.dialogSetPositiveButtonText("Yes")
droid.dialogSetNegativeButtonText("No")
droid.dialogShow()
response=droid.dialogGetResponse().result
droid.dialogDismiss()
if response.has_key("which"):
  result=response["which"]
  if result=="positive":
    print "Yay! I like swords too!"
  elif result=="negative":
    print "Oh. How sad."
elif response.has_key("canceled"): # Yes, I know it's mispelled.
  print "You can't even make up your mind?"
else:
  print "Unknown response=",response

print "Done"
```

# Lists #

There are a variety of list functions available. It's not immediately clear what the differences are.
  * dialogSetItems - gives a list of choices. Returns immediately on selection.
  * dialogSetSingleChoiceItems - Radio group. Choose one, have to close dialog box to proceed.
  * dialogSetMultiChoiceItems - Check Boxes. Choose several items.

This should demonstrate the differences:

## uilist.py ##
```
# Test of Lists
import android,sys
droid=android.Android()

#Choose which list type you want.
def getlist():
  droid.dialogCreateAlert("List Types")
  droid.dialogSetItems(["Items","Single","Multi"])
  droid.dialogShow()
  result=droid.dialogGetResponse().result
  if result.has_key("item"):
    return result["item"]
  else:
    return -1

#Choose List
listtype=getlist()
if listtype<0:
  print "No item chosen"
  sys.exit()

options=["Red","White","Blue","Charcoal"]
droid.dialogCreateAlert("Colors")
if listtype==0:
  droid.dialogSetItems(options)
elif listtype==1:
  droid.dialogSetSingleChoiceItems(options)
elif listtype==2:
  droid.dialogSetMultiChoiceItems(options)
droid.dialogSetPositiveButtonText("OK")
droid.dialogSetNegativeButtonText("Cancel")
droid.dialogShow()
result=droid.dialogGetResponse().result
# droid.dialogDismiss() # In most modes this is not needed.
if result==None:
  print "Time out"
elif result.has_key("item"):
  item=result["item"];
  print "Chosen item=",item,"=",options[item]
else:
  print "Result=",result
  print "Selected=",droid.dialogGetSelectedItems().result
print "Done"
```

# Events #
In version 4x, (see [Unofficial Releases](Unofficial.md)), _dialog_ events have been added, meaning you can continue to process in the background while waiting on a user response.

## uipoll.py ##
```
# Demonstrate use of modal dialog. Process location events while
# waiting for user input.
import android
droid=android.Android()
droid.dialogCreateAlert("I like swords.","Do you like swords?")
droid.dialogSetPositiveButtonText("Yes")
droid.dialogSetNegativeButtonText("No")
droid.dialogShow()
droid.startLocating()
while True: # Wait for events for up to 10 seconds.
  response=droid.eventWait(10000).result
  if response==None: # No events to process. exit.
    break
  if response["name"]=="dialog": # When you get a dialog event, exit loop
    break
  print response # Probably a location event.

# Have fallen out of loop. Close the dialog 
droid.dialogDismiss()
if response==None:
  print "Timed out."
else:
  rdialog=response["data"] # dialog response is stored in data.
  if  rdialog.has_key("which"):
    result=rdialog["which"]
    if result=="positive":
      print "Yay! I like swords too!"
    elif result=="negative":
      print "Oh. How sad."
  elif rdialog.has_key("canceled"): # Yes, I know it's mispelled.
    print "You can't even make up your mind?"
  else:
    print "Unknown response=",response
print droid.stopLocating()
print "Done"
```

## uiseek.py ##
```
# Test of Seekbar events.
import android
droid=android.Android()
droid.dialogCreateSeekBar(50,100,"I like swords.","How much you like swords?")
droid.dialogSetPositiveButtonText("Yes")
droid.dialogSetNegativeButtonText("No")
droid.dialogShow()
looping=True
while looping: # Wait for events for up to 10 secnds.from the menu.
  response=droid.eventWait(10000).result
  if response==None: # No events to process. exit.
    break
  if response["name"]=="dialog":
    looping=False # Fall out of loop unless told otherwise.
    data=response["data"]
    if data.has_key("which"):
      which=data["which"]
      if which=="seekbar":
	print "Progress=",data["progress"]," User input=",data["fromuser"]
	looping=True  # Keep Looping
	
# Have fallen out of loop. Close the dialog 
droid.dialogDismiss()
if response==None:
  print "Timed out."
else:
  rdialog=response["data"] # dialog response is stored in data.
  if  rdialog.has_key("which"):
    result=rdialog["which"]
    if result=="positive":
      print "Yay! I like swords too!"
    elif result=="negative":
      print "Oh. How sad."
  elif rdialog.has_key("canceled"): # Yes, I know it's mispelled.
    print "You can't even make up your mind?"
  print "You like swords this much: ",rdialog["progress"]  

print "Done"
```

# Menus #
This will add several menu options to your menu tree. Access by hitting MENU. When pressed, will trigger an event.

## uimenu.py ##
```
import android
droid=android.Android()

droid.addOptionsMenuItem("Silly","silly",None,"star_on")
droid.addOptionsMenuItem("Sensible","sensible","I bet.","star_off")
droid.addOptionsMenuItem("Off","off",None,"ic_menu_revert")

print "Hit menu to see extra options."
print "Will timeout in 10 seconds if you hit nothing."

while True: # Wait for events from the menu.
  response=droid.eventWait(10000).result
  if response==None:
    break
  print response
  if response["name"]=="off":
    break
print "And done."
```