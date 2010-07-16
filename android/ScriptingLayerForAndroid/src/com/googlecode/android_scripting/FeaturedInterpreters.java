// Copyright 2010 Google Inc. All Rights Reserved.

package com.googlecode.android_scripting;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FeaturedInterpreters {
  private static final Map<String, URL> mInterpreters = new HashMap<String, URL>();

  static {
    String interpreters[][] =
        {
          { "BeanShell 2.0b4",
            "http://android-scripting.googlecode.com/files/beanshellforandroid.apk" },
          { "JRuby-1.4", "http://android-scripting.googlecode.com/files/jrubyforandroid.apk" },
          { "Lua 5.1.4", "http://android-scripting.googlecode.com/files/luaforandroid.apk" },
          { "Perl 5.10.1", "http://android-scripting.googlecode.com/files/perlforandroid.apk" },
          { "Python 2.6.2", "http://android-scripting.googlecode.com/files/pythonforandroid.apk" },
          { "Rhino 1.7R2", "http://android-scripting.googlecode.com/files/rhinoforandroid.apk" }, };

    for (String[] interpreter : interpreters) {
      try {
        mInterpreters.put(interpreter[0], new URL(interpreter[1]));
      } catch (MalformedURLException e) {
        Log.e(e);
      }
    }
  }

  public static List<String> getList() {
    ArrayList<String> list = new ArrayList<String>(mInterpreters.keySet());
    Collections.sort(list);
    return list;
  }

  public static URL getUrlForName(String name) {
    return mInterpreters.get(name);
  }

}
