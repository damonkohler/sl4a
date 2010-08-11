package org.connectbot.util;

import android.content.Context;
import android.preference.ListPreference;
import android.util.AttributeSet;

import java.nio.charset.Charset;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;

public class EncodingPreference extends ListPreference {

  public EncodingPreference(Context context, AttributeSet attrs) {
    super(context, attrs);

    List<CharSequence> charsetIdsList = new LinkedList<CharSequence>();
    List<CharSequence> charsetNamesList = new LinkedList<CharSequence>();

    for (Entry<String, Charset> entry : Charset.availableCharsets().entrySet()) {
      Charset c = entry.getValue();
      if (c.canEncode() && c.isRegistered()) {
        String key = entry.getKey();
        if (key.startsWith("cp")) {
          // Custom CP437 charset changes
          charsetIdsList.add("CP437");
          charsetNamesList.add("CP437");
        }
        charsetIdsList.add(entry.getKey());
        charsetNamesList.add(c.displayName());
      }
    }

    this.setEntryValues(charsetIdsList.toArray(new CharSequence[charsetIdsList.size()]));
    this.setEntries(charsetNamesList.toArray(new CharSequence[charsetNamesList.size()]));
  }
}
