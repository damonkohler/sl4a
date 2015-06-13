**Release Notes: ![r6](https://code.google.com/p/android-scripting/source/detail?r=6) 10-Jul-2012**
  * FullScreenUI Updates
    * android:textSize now supports 'sp','dp','mm','in','px' and 'pt' suffixes
    * android:textColor now works, and supports #aarrggbb, #rrggbb, #argb and #rgb notations. Also supports standard html color names.
    * android:textStyle now works.
    * nextFocus functions should work. (Needs testing)
    * Should add support for all layout properties in fullscreenui. (Needs feedback - only quickly tested)(r5x15)
    * Added support for "digits" property in fullscreenui
    * Added typeface, textHintColor, textLinkColor and textHighlightColor (r5x14)
    * margin and padding properties (r5x15)
    * Added fullKeyOverride (support for [issue 622](https://code.google.com/p/android-scripting/issues/detail?id=622)) (r5x18)
    * fullShow now switches without flicker (r5x19)
    * Added ability to set activity title - see fullSetTitle and extra parameter to fullShow (r5x22)
  * Added eden's additional bluetooth discovery functions. (r5x10)
  * Fixed strange characters on full keyboards when trying to type brackets in intepreter shell. Also enabled ctrl keys on keyboards that support them. (r5x11)
  * Editor will now remember cursor location between session. (r5x12)
  * Applied patches from Agustin Henze, Issues 592, 605 and 609: Added some location functions, a bug displaying url in wrong thread, and standardizing notifications. (r5x13)
  * Included support for "Sleep" language (from tomcatalbino)
  * Included support for "Squirrel" language (from Andy Tai)
  * Included patch to deal with public servers and ipv6: (r5x16)
  * Fix to [issue 621](https://code.google.com/p/android-scripting/issues/detail?id=621), events going missing on non-blocking eventWait (rx18)
  * Fix to [issue 631](https://code.google.com/p/android-scripting/issues/detail?id=631), wrong thread error when updating lists. (r5x20)
  * Fix to NPE when setting typeface in initial layout( r5x21)
  * Fix to NPE with receiving some broadcase intents. (r5x22)
<br>