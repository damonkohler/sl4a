## Background ##
This page maintains an active list of Layout Properties that are having issues when used with FullScreenUI facade in Scripting Layer for Android [R5](https://code.google.com/p/android-scripting/source/detail?r=5) to simplify debugging by the SL4A developers.

Issue fixes are currently being incorporated in the development branch releases **[r5x](http://android-scripting.googlecode.com/files/sl4a_r5.apk)** till the next major release. You may wish to consider upgrading to r5x if you are using FullScreenUI extensively. For release notes, see [Unofficial](Unofficial.md).

## How to Report ##
To report an issue, pls. use the [New Issue button](http://code.google.com/p/android-scripting/issues/list) in the SL4A issues tracker. These issues will be collated & added to this list. To help simplify tracing the issue - request you to pls follow the following guidelines -

  * In the Summary, pls write : **FullScreenUI Layout Property Issue: <android property - eg.  android:textSize >**
  * Pls. include full code from the View in the layout where you are having an issue with a property - eg. for the issue with android:textSize property in TextView -
```

<TextView
android:layout_width="fill_parent"
android:layout_height="wrap_content"
android:text="Calories this week"
android:gravity="center"
android:textSize="18sp" />
```
  * Pls. capture & "print" the results of **droid.fullShow(layout)** to the terminal window & attach a screenshot of the terminal after your script ends - or at least type out any error message visible. You can do this by -
    1. (For Python Users) In the section where you're calling droid.follShow(layout) write the code as: **print droid.fullShow(layout)**
    1. Run you script from within SL4A with the **terminal** option ie - the 1st icon with a black rectangle when you select a script
    1. When your program exits, take a **screenshot** of the terminal window and include as attachment in the issue OR note down any error message - in this example below _"TextView:textStyle Property not found"_
<img src='http://android-scripting.googlecode.com/files/FullScreenUI-issue.png' width='360' height='600'></img>

## Understanding Error Messages & Finding Work-Arounds ##
<font color='#ff0000'><b>Update:</b> While this example below serves as an explanation, this issue on textColor is fixed as of development release r5x08 - see <a href='Unofficial.md'>Unofficial</a></font>

The reason these issues currently arise is because internally, a custom parser implements layout parsing in SL4A and creates the layout. To set properties, it tries to call a Java "setter" function of the View calss corresponding to a property.

Many of the issues observed till now seem to arise because either the android:property doesn't have a exactly corresponding setProperty() function or because the parameters expected by setProperty() is in a different format vs. what the layout XML would accept.

This understanding can be used to identify work-arounds for the time being while SL4A developers work on the issues. For example -
<pre>
android:textColor="#ffffffff"<br>
</pre>
doesn't work & instead shows the error
<pre>
Unknown value #ffffffff<br>
</pre>
This probably means that the setTextColor() function which would have been called by SL4A to implement the property was expecting the color parameter in a different format.

Looking up the function reference for [setTextColor()](http://developer.android.com/reference/android/widget/TextView.html#setTextColor(int)), we see that it is expecting a standard Java int as a parameter. In Java (and many other programming languages) you can write a hex number with the prefix 0x, so by replacing # with 0x as follows, we find a fairly good work-around :-)
<pre>
android:textColor="0xffffffff"<br>
</pre>

## Current List of Layout Property Issues ##

## Generic View Properties ##
<table cellpadding='5' width='700' cellspacing='0' border='1'><tr><td width='100'>Property</td><td><h3><del>android:textSize</del></h3><font color='#ff0000'>Fixed as of development release r5x08 - see <a href='Unofficial.md'>Unofficial</a></font></td></tr><tr><td>Issue</td><td>Partially works - specifying a size in integers works (pixels?), specifying the size in "sp" or "dp" doesn't work. </td></tr><tr><td>Error Example</td><td><pre><code>&lt;TextView android:layout_width="fill_parent"<br>
android:layout_height="wrap_content"<br>
android:text="This is the textview"<br>
android:gravity="center"<br>
android:textSize="18sp" /&gt; </code></pre></td></tr><tr><td>Error Message</td><td>setTextSize:20sp:java.lang.NumberFormatException</td></tr><tr><td>Work-arounds</td><td>specify the size in integers eg. - android:textSize="18"</td></tr></table><br>

<table cellpadding='5' width='700' cellspacing='0' border='1'><tr><td width='100'>Property</td><td><h3><del>android:textStyle</del></h3><font color='#ff0000'>Fixed as of development release r5x14 - see <a href='Unofficial.md'>Unofficial</a></font></td></tr><tr><td>Issue</td><td>Does not change the text style like bold, italic etc</td></tr><tr><td>Error Example</td><td><pre><code> &lt;TextView android:layout_width="fill_parent"<br>
android:layout_height="wrap_content"<br>
android:text="This is the textview"<br>
android:gravity="center"<br>
android:textStyle="bold" /&gt;</code></pre></td></tr><tr><td>Error Message</td><td>TextView:textStyle Property not found</td></tr><tr><td>Work-arounds</td><td>None as of now</td></tr></table><br>

<table cellpadding='5' width='700' cellspacing='0' border='1'><tr><td width='100'>Property</td><td><h3><del>android:textColor</del></h3><font color='#ff0000'>Fixed as of development release r5x08 - see <a href='Unofficial.md'>Unofficial</a></font></td></tr><tr><td>Issue</td><td>Color entered in the XML standard of "#AARRGGBB" format doesn't work</td></tr><tr><td>Error Example</td><td><pre><code>&lt;TextView android:gravity="left"<br>
android:layout_width="wrap_content"<br>
android:layout_height="wrap_content"<br>
android:text="sample text"<br>
android:textSize="16"<br>
android:textColor="#ff00ff66" /&gt;</code></pre></td></tr><tr><td>Error Message</td><td>Unknown value #ff00ff66</td></tr><tr><td>Work-arounds</td><td>specify color as hex integer in the Java/C/C++ format with 0x prefix - ie android:textColor="0xff00ff66"</td></tr></table><br>

<table cellpadding='5' width='700' cellspacing='0' border='1'><tr><td width='100'><del>Property</td></del><td><h3>android:nextFocusDown, android:nextFocusForward, android:nextFocusLeft, android:nextFocusRight, android:nextFocusUp<del></h3></del><font color='#ff0000'>Fixed as of development release r5x15 - see <a href='Unofficial.md'>Unofficial</a></font></td></tr><tr><td>Issue</td><td>Does not set the next focus correctly since the corresponding function is of the form <a href='http://developer.android.com/reference/android/view/View.html#setNextFocusUpId(int)'>setNextFocusUpId(int nextFocusUpId)</a> - the function name includes an Id suffix & takes the next focus view's id as parameter</td></tr><tr><td>Error Example</td><td><pre><code>&lt;EditText<br>
android:id="@+id/Edittext"<br>
android:layout_width="wrap_content"<br>
android:layout_height="wrap_content"<br>
android:nextFocusDown="@+id/buttonNext"<br>
android:text="SampleText" /&gt;</code></pre></td></tr><tr><td>Error Message</td><td>For a Focusable View: nextFocusDown Property not found</td></tr><tr><td>Work-arounds</td><td>none currently</td></tr></table><br>


<h2>LinearLayout Related Properties</h2>

<table cellpadding='5' width='700' cellspacing='0' border='1'><tr><td width='100'>Property</td><td><h3><del>android:layout_weight</del></h3><font color='#ff0000'>Fixed as of development release r5x14 - see <a href='Unofficial.md'>Unofficial</a></font></td></tr><tr><td>Issue</td><td>Does not work to set any View's layout weight in LinearLayout</td></tr><tr><td>Error Example</td><td><pre><code>&lt;LinearLayout 	android:layout_width="fill_parent"<br>
android:layout_height="wrap_content"<br>
android:orientation="horizontal"&gt;<br>
&lt;Button<br>
android:id="@+id/but1"<br>
android:text="Button1"<br>
android:textSize="18"<br>
android:layout_width="wrap_content"<br>
android:layout_height="wrap_content"<br>
android:layout_weight="1"/&gt;<br>
&lt;Button<br>
android:id="@+id/but2"<br>
android:text="Button2"<br>
android:textSize="18"<br>
android:layout_width="wrap_content"<br>
android:layout_height="wrap_content"<br>
android:layout_weight="1"/&gt;<br>
<br>
<br>
Unknown end tag for &lt;/LinearLayout&gt;<br>
<br>
 </code></pre></td></tr><tr><td>Error Message</td><td>For Any View : layout_weight Property not found</td></tr><tr><td>Work-arounds</td><td>specify layout_width and layout_height in pixels, sp or dp eg. android:layout_width="155"</td></tr></table><br>

<h2>RelativeLayout Related Properties</h2>

<table cellpadding='5' width='700' cellspacing='0' border='1'><tr><td width='100'>Property</td><td><h4><del>android:layout_above, android:layout_alignBaseline, android:layout_alignBottom, android:layout_alignLeft, android:layout_alignParentBottom, android:layout_alignParentLeft, android:layout_alignParentRight, android:layout_alignParentTop, android:layout_alignRight, android:layout_alignTop, android:layout_alignWithParentIfMissing, android:layout_below, android:layout_centerHorizontal, android:layout_centerInParent, android:layout_centerVertical, android:layout_toLeftOf, android:layout_toRightOf</del></h4><font color='#ff0000'>Fixed as of development release r5x15 - see <a href='Unofficial.md'>Unofficial</a></font></td></tr><tr><td>Issue</td><td>Does not work to set any View's layout position in LinearLayout. Observed issues with layout_alignParentTop, layout_above but likely to be an issue for all properties</td></tr><tr><td>Error Example</td><td><pre><code>&lt;ScrollView<br>
android:id="@+id/scrollView1"<br>
android:layout_height="wrap_content"<br>
android:scrollbars="vertical"<br>
android:layout_alignParentTop="true"<br>
android:layout_width="fill_parent"<br>
android:layout_above="@id/Controls"&gt;</code></pre></td></tr><tr><td>Error Message</td><td>For Any View:layout_alignParentTop Property not found</td></tr><tr><td>Work-arounds</td><td>none currently</td></tr></table><br>

<h2>Issues specific to certain Views</h2>

<h3>EditText</h3>
<table cellpadding='5' width='700' cellspacing='0' border='1'><tr><td width='100'>Property</td><td><h3><del>android:digits</del></h3><font color='#ff0000'>Fixed as of development release r5x14 - see <a href='Unofficial.md'>Unofficial</a></font></td></tr><tr><td>Issue</td><td>Not able to set which digits should be allowed in edittext since corresponding Java Function is not found - however, see discussion on <a href='http://stackoverflow.com/questions/7300490/set-edittext-digits-programatically'>StackOverflow</a> on a possible alternative</td></tr><tr><td>Error Example</td><td><pre><code>&lt;EditText<br>
android:id="@+id/RSAKey"<br>
android:layout_width="wrap_content"<br>
android:layout_height="wrap_content"<br>
android:digits="0123456789"<br>
android:ems="10"<br>
android:inputType="phone" /&gt;</code></pre></td></tr><tr><td>Error Message</td><td>EditText:digits Property not found</td></tr><tr><td>Work-arounds</td><td>none currently</td></tr></table><br>