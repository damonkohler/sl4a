Having trouble? Got questions? Check the [FAQ](FAQ.md) or try the
[SL4A discussion group](http://groups.google.com/group/android-scripting).

## Background ##
This page maintains an active list of Layout Properties that are having issues
when used with FullScreenUI facade in Scripting Layer for Android
[R5](https://code.google.com/p/android-scripting/source/detail?r=5)
to simplify debugging by the SL4A developers.

Issue fixes are currently being incorporated in the development branch releases
**[r6x](../../releases)** till the
next major release. You may wish to consider upgrading to r5x if you are using
FullScreenUI extensively. For release notes, see [Unofficial](Unofficial.md).

## How to Report ##
To report an issue, pls. use the
[New Issue link](../README.md#issue).

These issues will be collated & added to this list.
To help simplify tracing the issue - request you to pls follow the following
guidelines -

  * In the Summary, pls write: **FullScreenUI Layout Property Issue:
    <android property - eg.  android:textSize >**
  * Pls. include full code from the View in the layout where you are having an
    issue with a property - eg. for the issue with android:textSize property in
    TextView -
```

<TextView
android:layout_width="fill_parent"
android:layout_height="wrap_content"
android:text="Calories this week"
android:gravity="center"
android:textSize="18sp" />
```
  * Pls. capture & "print" the results of **droid.fullShow(layout)** to the
    terminal window & attach a screenshot of the terminal after your script ends
    - or at least type out any error message visible. You can do this by -

    1. (For Python Users) In the section where you're calling
       droid.follShow(layout) write the code as:
       **print droid.fullShow(layout)**
    1. Run you script from within SL4A with the **terminal** option ie - the 1st
       icon with a black rectangle when you select a script
    1. When your program exits, take a **screenshot** of the terminal window
       and include as attachment in the issue OR note down any error message -
       in this example below _"TextView:textStyle Property not found"_
       ![FullScreenUI issue][FullScreenUI-issue1]

[FullScreenUI-issue1]: http://github.com/kuri65536/sl4a/wiki/images/FullScreenUI-issue.png =360x600

## Understanding Error Messages & Finding Work-Arounds ##
**Update:** While this example below serves as an explanation, this issue on
textColor is fixed as of development release r5x08 - see
[Unofficial](Unofficial.md)

The reason these issues currently arise is because internally, a custom parser
implements layout parsing in SL4A and creates the layout. To set properties, it
tries to call a Java "setter" function of the View calss corresponding to a
property.

Many of the issues observed till now seem to arise because either the
android:property doesn't have a exactly corresponding setProperty() function or
because the parameters expected by setProperty() is in a different format vs.
what the layout XML would accept.

This understanding can be used to identify work-arounds for the time being while
SL4A developers work on the issues. For example -

```xml
<pre>
android:textColor="#ffffffff"<br>
</pre>
doesn't work & instead shows the error
<pre>
Unknown value #ffffffff<br>
</pre>
```

This probably means that the setTextColor() function which would have been
called by SL4A to implement the property was expecting the color parameter in a
different format.

Looking up the function reference for
[setTextColor()](http://developer.android.com/reference/android/widget/TextView.html#setTextColor(int)),
we see that it is expecting a standard Java int as a parameter. In Java (and
many other programming languages) you can write a hex number with the prefix 0x,
so by replacing # with 0x as follows, we find a fairly good work-around :-)

```xml
    android:textColor="0xffffffff"
```

## Current List of Layout Property Issues ##

## Generic View Properties ##

|Term         |Discription |
|:------------|:--------|
|Property     |android:textSize |
|Fixed as of  |development release r5x08 - see [Unofficial](Unofficial.md) |
|Issue        |Partially works - specifying a size in integers works (pixels?), specifying the size in "sp" or "dp" doesn't work. |
|Error Example|`<TextView android:layout_width="fill_parent" android:layout_height="wrap_content" android:text="This is the textview" android:gravity="center" android:textSize="18sp" />` |
|Error Message|setTextSize:20sp:java.lang.NumberFormatException |
|Work-arounds |specify the size in integers eg. - android:textSize="18" |

<table>
<tr><td>Property</td><td>
    android:textStyle
</td></tr><tr><td>Fixed as of</td><td>
    development release r5x14 - see <a href="Unofficial.md">Unofficial</a>
</td></tr><tr><td>Issue</td><td>
    Does not change the text style like bold, italic etc
</td></tr><tr><td>Error Example</td><td><pre><code>
     &lt;TextView android:layout_width="fill_parent"
               android:layout_height="wrap_content"
               android:text="This is the textview"
               android:gravity="center"
               android:textStyle="bold" /&gt;
</code></pre>
</td></tr><tr><td>Error Message</td><td><code>
    TextView:textStyle Property not found
</code>
</td></tr><tr><td>Work-arounds</td><td>
    None as of now
</td></tr></table>

<table>
<tr><td>Property</td><td>
    android:textColor
</td></tr><tr><td>Fixed as of</td><td>
    development release r5x08 - see <a href="Unofficial.md">Unofficial</a>
</td></tr><tr><td>Issue</td><td>
    Color entered in the XML standard of "#AARRGGBB" format doesn't work
</td></tr><tr><td>Error Example</td><td><pre><code>
     &lt;TextView android:gravity="left"
               android:layout_width="wrap_content"
               android:layout_height="wrap_content"
               android:text="sample text"
               android:textSize="16"
               android:textColor="#ff00ff66" /&gt;
</code></pre>
</td></tr><tr><td>Error Message</td><td><code>
    Unknown value #ff00ff66
</code>
</td></tr><tr><td>Work-arounds</td><td>
    specify color as hex integer in the Java/C/C++ format with 0x prefix - ie
    android:textColor="0xff00ff66"
</td></tr></table>

<table>
<tr><td>Property</td><td>
    android:nextFocusDown, android:nextFocusForward,
    android:nextFocusLeft, android:nextFocusRight, android:nextFocusUp
</td></tr><tr><td>Fixed as of</td><td>
    development release r5x15 - see <a href="Unofficial.md">Unofficial</a>
</td></tr><tr><td>Issue</td><td>
    Does not set the next focus correctly since the corresponding function is of
    the form <a href="http://developer.android.com/reference/android/view/View.html#setNextFocusUpId(int)"
        >setNextFocusUpId(int nextFocusUpId)</a>
    - the function name includes an Id suffix & takes
    the next focus view's id as parameter.
</td></tr><tr><td>Error Example</td><td><pre><code>
    &lt;EditText android:id="@+id/Edittext"
              android:layout_width="wrap_content"
              android:layout_height="wrap_content"
              android:nextFocusDown="@+id/buttonNext"
              android:text="SampleText" /&gt;
</code></pre>
</td></tr><tr><td>Error Message</td><td><code>
    For a Focusable View: nextFocusDown Property not found
</code>
</td></tr><tr><td>Work-arounds</td><td>
    none currently
</td></tr></table>


### LinearLayout Related Properties ###

<table>
<tr><td>Property</td><td>
    android:layout_weight
</td></tr><tr><td>Fixed as of</td><td>
    of development release r5x14 - see <a href="Unofficial.md">Unofficial</a>
</td></tr><tr><td>Issue</td><td>
    Does not work to set any View's layout weight in LinearLayout
</td></tr><tr><td>Error Example</td><td><pre><code>
    &lt;LinearLayout android:layout_width="fill_parent"
                  android:layout_height="wrap_content"
                  android:orientation="horizontal"&gt;
        &lt;Button android:id="@+id/but1"
                android:text="Button1"
                android:textSize="18"
                android:layout_width="wrap_content"
                android:layout_height="wrap_content"
                android:layout_weight="1" /&gt;
        &lt;Button android:id="@+id/but2"
                android:text="Button2"
                android:textSize="18"
                android:layout_width="wrap_content"
                android:layout_height="wrap_content"
                android:layout_weight="1" /&gt;
    &lt;/LinearLayout&gt;
</code></pre>
</td></tr><tr><td>Error Message</td><td><code>
    For Any View: layout_weight Property not found
</code>
</td></tr><tr><td>Work-arounds</td><td>
    specify layout_width and layout_height in pixels, sp or dp eg.
    android:layout_width="155"
</td></tr></table>


### RelativeLayout Related Properties ###

<table cellpadding='5' width='700' cellspacing='0' border='1'>
<tr><td>Property</td><td>
    android:layout_above, android:layout_alignBaseline,
    android:layout_alignBottom, android:layout_alignLeft,
    android:layout_alignParentBottom, android:layout_alignParentLeft,
    android:layout_alignParentRight, android:layout_alignParentTop,
    android:layout_alignRight, android:layout_alignTop,
    android:layout_alignWithParentIfMissing,
    android:layout_below, android:layout_centerHorizontal,
    android:layout_centerInParent, android:layout_centerVertical,
    android:layout_toLeftOf, android:layout_toRightOf
</td></tr><tr><td>Fixed as of</td><td>
development release r5x15 - see [Unofficial](Unofficial.md)
</td></tr><tr><td>Issue</td><td>
    Does not work to set any View's layout position in LinearLayout. Observed
    issues with layout_alignParentTop, layout_above but likely to be an issue
    for all properties
</td></tr><tr><td>Error Example</td><td><pre></code>
    &lt;ScrollView android:id="@+id/scrollView1"
                android:layout_height="wrap_content"
                android:scrollbars="vertical"
                android:layout_alignParentTop="true"
                android:layout_width="fill_parent"
                android:layout_above="@id/Controls" /&gt;
</code></pre>
</td></tr><tr><td>Error Message</td><td>
    For Any View:layout_alignParentTop Property not found
</td></tr><tr><td>Work-arounds</td><td>
    none currently
</td></tr></table>

## Issues specific to certain Views ##

### EditText ###
<table cellpadding='5' width='700' cellspacing='0' border='1'>
<td width='100'>Property</td><td>
    android:digits
</td></tr><tr><td>Fixed as of</td><td>
development release r5x14 - see [Unofficial](Unofficial.md)
</td></tr><tr><td>Issue</td><td>
Not able to set which digits should be allowed in edittext since corresponding
Java Function is not found - however, see discussion on
<a href="http://stackoverflow.com/questions/7300490/set-edittext-digits-programatically"
  >StackOverflow</a>
on a possible alternative
</td></tr><tr><td>Error Example</td><td><pre><code>
    &lt;EditText android:id="@+id/RSAKey"
          android:layout_width="wrap_content"
          android:layout_height="wrap_content"
          android:digits="0123456789"
          android:ems="10"
          android:inputType="phone" /&gt;
</code></pre>
</td></tr><tr><td>Error Message</td><td>
    EditText:digits Property not found
</td></tr><tr><td>Work-arounds</td><td>
    none currently
</td></tr></table>

<!---
 vi: ft=markdown:et:fdm=marker
 -->
