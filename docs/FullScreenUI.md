# Introduction #

The full screen interface is a new, experimental feature that allows you to define and display a Layout, and respond to button clicks and other key events. This feature is available with the [r5](https://code.google.com/p/android-scripting/source/detail?r=5) release.

<font color='#ff0000'><b>Update:</b> </font>If you are using FullScreenUI extensively, you may wish to upgrade to the latest development branch release  **[r5x](http://www.mithril.com.au/android/sl4a_r5x.apk)** which will incorporate a series of issue fixes on layout properties till the next stable release. For the latest list of fixes, issues & work-arounds - see [FullScreenUI\_Layout\_Property\_Issues](FullScreenUI_Layout_Property_Issues.md). For info on **r5x**, see [Unofficial](Unofficial.md).

# Details #

Basically, you take a screen layout as built by the android layout
designer, and call **droid.fullShow**

This will display the screen. Button clicks and keypresses (include
the BACK and Volume keys) will be returned as events "click" and "key"
respectively.

You can query the settings for a given controil using fullQueryDetail,
or list all controls using fullQuery.

**fullSetProperty** will let you set properties.
**fullDismiss** will close the screen down.

Because I had to write my own parsing routines for the view (Android
heavily preprocesses existing layouts in the compiler) I don't promise
to support every possible property, but extensive use of reflection
means it will give most things a serious shot.

## Layouts ##
See http://developer.android.com/guide/topics/ui/index.html for general information on Layouts. The Android ADT includes a Layout editor, which I have used for my examples.
## Querying the controls ##
One you've show the screen using "fullShow", you can query the current state of the controls, provided they have an id defined.
```
  fullQueryDetail(id)
```
Properties returned are:

id, type, text, visibility, checked and tag.

More can be included on request.

In the sample code below, the line:
```
  droid.fullQueryDetail("editText1").result
```
should return:
```
  {u'id': u'editText1', 
   u'text': u'123456789', 
   u'tag': u'Tag Me', 
   u'type': u'EditText',
   u'visibility': u'0'}
```

## Setting Properties ##
You can use the fullSetProperty command to set properties of your controls, ie:
```
  droid.fullSetProperty("textView1","text","Other stuff here")
```
Properities definitely available include "text", "visible" and "checked", but you should be able to set most simple properties.
A property in this case is defined as any method the control has that starts with a "set".
As an example (taken from example code below):
```
  droid.fullSetProperty("background","backgroundColor","0xff7f0000")
```

If you look at http://developer.android.com/reference/android/view/View.html#setBackgroundColor%28int%29, you should see the relationship.
Note that case is important.

You should be able to set most simple properties that way.

When defining a property, the layout inflater will recognize standard resources, ie:
```
  print droid.fullSetProperty("imageView1","src","@android:drawable/star_big_off")
```

Note that you can only use resources already compiled into sl4a or the standard android package. In theory you could use resources from other packages, but I haven't tried it. Full form of resource property is
```
@[package:]type/name 
```
If you don't include a package id, it will default to the current package, typically sl4a.
If you use the standalone template you should be able to include your own resources in there.
### Images ###
Images can be included in the layout using "ImageView" controls. The property to set to display the image is "src".
This can either be a resource from somewhere, or a file url.
ie:
```
droid.fullSetProperty("imageView1","src","@android:drawable/star_big_off")
droid.fullSetProperty("imageView1","src","file:///sdcard/download/panda72.png")
```
### Colors ###
When setting colors, you have to define them as either #aarrggbb or 0xaarrggbb. The aa (alpha) is important. For solid colors, this should be set to ff. Ie, black=#ff000000, red=#ffff0000.

As of r5x09, colors should also recognizes #rrggbb, #rgb and #argb ans behave sensibly. Also, as a convenience, all the standard html color names are recognized. See: http://www.w3schools.com/html/html_colornames.asp . Color names are case insensitve.

### Background ###
The background property can take either a color, a resource, or a bitmap file as a value.
```
droid.fullSetProperty("button1","background","file:///sdcard/download/panda72.png")
droid.fullSetProperty("button1","background","@android:drawable/star_big_off")
droid.fullSetProperty("button1","background","#ff00ff00")
```

### Lists ###
You can associate a list of items with a Spinner or a ListView using fullSetList.
```
droid.fullSetList("listview1",["One","Two","Three"])
droid.fullSetList("spinner1",["Red","Green","Blue"])
```
ListViews will throw an "itemclick" event when an item is selected.
Spinners don't yet throw a "click" event, but the selected item will be returned in !selectedPosition.
## Responding to Events ##
Any control that supports the clickable interface will generate a "click" event when tapped. The basic properties of the control will be returned in data.

A button (including Back, Search and the volume controls) will generate a
"key" event. The data will contain a map with the keycode (as defined here: http://developer.android.com/reference/android/view/KeyEvent.html.

Some useful keycodes: BACK=4, Search=84, volume up=24, volume down=25, and menu=82. Arrow keys/trackball: up=19, left=20, right=21 and down=22

Some keys (ie, the volume keys) have a default behaviour, such as changing the ringtone volume. You can override this behaviour by calling **fullKeyOverride** with a list of the keycodes you wish to override.
```
  droid.fullKeyOverride([24,25],True)
```

## Options Menu ##
If you have an options menu defined (using [addOptionsMenuItem](http://www.mithril.com.au/android/doc/UiFacade.html#addOptionsMenuItem) it will be available in the full screen.

## Debugging ##
fullDisplay and fullSetProperty both return messages detailing any errors with the layout or properies, including any unrecognized properties.
If asking for more features, please include sample code and these debugging responses.

## Example Code ##
```
import android
droid=android.Android()

def eventloop():
  while True:
    event=droid.eventWait().result
    print event
    if event["name"]=="click":
      id=event["data"]["id"]
      if id=="button3":
        return
      elif id=="button2":
        droid.fullSetProperty("editText1","text","OK has been pressed")
      elif id=="button1":
        droid.fullSetProperty("textView1","text","Other stuff here")
	print droid.fullSetProperty("background","backgroundColor","0xff7f0000")
    elif event["name"]=="screen":
      if event["data"]=="destroy":
        return

print "Started"
layout="""<?xml version="1.0" encoding="utf-8"?>
<LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
	android:id="@+id/background"
	android:orientation="vertical" android:layout_width="match_parent"
	android:layout_height="match_parent" android:background="#ff000000">
	<LinearLayout android:layout_width="match_parent"
		android:layout_height="wrap_content" android:id="@+id/linearLayout1">
		<Button android:id="@+id/button1" android:layout_width="wrap_content"
			android:layout_height="wrap_content" android:text="Test 1"></Button>
		<Button android:id="@+id/button2" android:layout_width="wrap_content"
			android:layout_height="wrap_content" android:text="Ok"></Button>
		<Button android:id="@+id/button3" android:layout_width="wrap_content"
			android:layout_height="wrap_content" android:text="Cancel"></Button>
	</LinearLayout>
	<TextView android:layout_width="match_parent"
		android:layout_height="wrap_content" android:text="TextView"
		android:id="@+id/textView1" android:textAppearance="?android:attr/textAppearanceLarge" android:gravity="center_vertical|center_horizontal|center"></TextView>
	<EditText android:layout_width="match_parent"
		android:layout_height="wrap_content" android:id="@+id/editText1"
		android:tag="Tag Me" android:inputType="textCapWords|textPhonetic|number">
		<requestFocus></requestFocus>
	</EditText>
	<CheckBox android:layout_height="wrap_content" android:id="@+id/checkBox1" android:layout_width="234dp" android:text="Howdy, neighbors." android:checked="true"></CheckBox>
</LinearLayout>
"""
print layout
print droid.fullShow(layout)
eventloop()
print droid.fullQuery()
print "Data entered =",droid.fullQueryDetail("editText1").result
droid.fullDismiss()
```

## Mini FAQ ##
Why is my screen transparent?
You need to define a background color for your root layout, including the alpha. ie,
```
android:background="#ff000000"
```
(Actually, as of 5x13, the color values do not require an explicit alpha value)

## TODO ##
  * A function to query ALL properties, not just the basic ones.
  * ~~Including an options menu~~ (and perhaps context menus?)
  * ~~Ability to override standard actions on keypress.~~