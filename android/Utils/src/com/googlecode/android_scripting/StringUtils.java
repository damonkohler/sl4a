package com.googlecode.android_scripting;

import java.util.Collection;
import java.util.Iterator;

public class StringUtils {

  private StringUtils() {
    // Utility class.
  }

  public static String join(Collection<String> collection, String delimiter) {
    StringBuffer buffer = new StringBuffer();
    Iterator<String> iter = collection.iterator();
    while (iter.hasNext()) {
      buffer.append(iter.next());
      if (iter.hasNext()) {
        buffer.append(delimiter);
      }
    }
    return buffer.toString();
  }
}
