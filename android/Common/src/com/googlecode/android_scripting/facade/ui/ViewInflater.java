package com.googlecode.android_scripting.facade.ui;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.text.InputType;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;

import com.googlecode.android_scripting.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.json.JSONArray;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

public class ViewInflater {
  private static XmlPullParserFactory mFactory;
  public static final String ANDROID = "http://schemas.android.com/apk/res/android";
  public static final int BASESEQ = 0x7f0f0000;
  private int mNextSeq = BASESEQ;
  private final Map<String, Integer> mIdList = new HashMap<String, Integer>();
  private final List<String> mErrors = new ArrayList<String>();
  private Context mContext;
  private DisplayMetrics mMetrics;
  private static final Map<String, Integer> mInputTypes = new HashMap<String, Integer>();

  public static XmlPullParserFactory getFactory() throws XmlPullParserException {
    if (mFactory == null) {
      mFactory = XmlPullParserFactory.newInstance();
      mFactory.setNamespaceAware(true);
    }
    return mFactory;
  }

  public static XmlPullParser getXml() throws XmlPullParserException {
    return getFactory().newPullParser();
  }

  public static XmlPullParser getXml(InputStream is) throws XmlPullParserException {
    XmlPullParser xml = getXml();
    xml.setInput(is, null);
    return xml;
  }

  public static XmlPullParser getXml(Reader ir) throws XmlPullParserException {
    XmlPullParser xml = getXml();
    xml.setInput(ir);
    return xml;
  }

  public View inflate(Activity context, XmlPullParser xml) throws XmlPullParserException,
      IOException, IllegalArgumentException, IllegalAccessException, InvocationTargetException {
    int event;
    mContext = context;
    mErrors.clear();
    mMetrics = new DisplayMetrics();
    context.getWindowManager().getDefaultDisplay().getMetrics(mMetrics);
    do {
      event = xml.next();
      if (event == XmlPullParser.END_DOCUMENT) {
        return null;
      }
    } while (event != XmlPullParser.START_TAG);
    View view = inflateView(context, xml, null);
    return view;
  }

  private void addln(Object msg) {
    Log.d(msg.toString());
  }

  @SuppressWarnings("rawtypes")
  public void setClickListener(View v, android.view.View.OnClickListener listener,
      OnItemClickListener itemListener) {
    if (v.isClickable()) {

      if (v instanceof AdapterView) {
        try {
          ((AdapterView) v).setOnItemClickListener(itemListener);
        } catch (RuntimeException e) {
          // Ignore this, not all controls support OnItemClickListener
        }
      }
      try {
        v.setOnClickListener(listener);
      } catch (RuntimeException e) {
        // And not all controls support OnClickListener.
      }
    }
    if (v instanceof ViewGroup) {
      ViewGroup vg = (ViewGroup) v;
      for (int i = 0; i < vg.getChildCount(); i++) {
        setClickListener(vg.getChildAt(i), listener, itemListener);
      }
    }
  }

  private View inflateView(Context context, XmlPullParser xml, ViewGroup root)
      throws IllegalArgumentException, IllegalAccessException, InvocationTargetException,
      XmlPullParserException, IOException {
    View view = buildView(context, xml, root);
    if (view == null) {
      return view;
    }
    int event;
    while ((event = xml.next()) != XmlPullParser.END_DOCUMENT) {
      switch (event) {
      case XmlPullParser.START_TAG:
        if (view == null || view instanceof ViewGroup) {
          inflateView(context, xml, (ViewGroup) view);
        } else {
          skipTag(xml); // Not really a view, probably, skip it.
        }
        break;
      case XmlPullParser.END_TAG:
        return view;
      }
    }
    return view;
  }

  private void skipTag(XmlPullParser xml) throws XmlPullParserException, IOException {
    int depth = xml.getDepth();
    int event;
    while ((event = xml.next()) != XmlPullParser.END_DOCUMENT) {
      if (event == XmlPullParser.END_TAG && xml.getDepth() <= depth) {
        break;
      }
    }
  }

  private View buildView(Context context, XmlPullParser xml, ViewGroup root)
      throws IllegalArgumentException, IllegalAccessException, InvocationTargetException {
    View view = viewClass(context, xml.getName());
    if (view != null) {
      for (int i = 0; i < xml.getAttributeCount(); i++) {
        String ns = xml.getAttributeNamespace(i);
        String attr = xml.getAttributeName(i);
        if (ANDROID.equals(ns)) {
          setProperty(view, root, attr, xml.getAttributeValue(i));
        }
      }
      if (root != null) {
        root.addView(view);
      }
    }

    return view;
  }

  private int getLayoutValue(String value) {
    if (value == null) {
      return 0;
    }
    if (value.equals("match_parent")) {
      return LayoutParams.FILL_PARENT;
    }
    if (value.equals("wrap_content")) {
      return LayoutParams.WRAP_CONTENT;
    }
    if (value.equals("fill_parent")) {
      return LayoutParams.FILL_PARENT;
    }
    if (value.endsWith("dp")) {
      int result =
          (int) (Integer.parseInt(value.substring(0, value.length() - 2)) * (mMetrics.density));
      return result;
    }
    return Integer.parseInt(value);
  }

  private int calcId(String value) {
    if (value == null) {
      return 0;
    }
    if (value.startsWith("@+id/")) {
      return tryGetId(value.substring(5));
    }
    if (value.startsWith("@id/")) {
      return tryGetId(value.substring(4));
    }
    try {
      return Integer.parseInt(value);
    } catch (NumberFormatException e) {
      return 0;
    }
  }

  private int tryGetId(String value) {
    Integer id = mIdList.get(value);
    if (id == null) {
      id = new Integer(mNextSeq++);
      mIdList.put(value, id);
    }
    return id;
  }

  private LayoutParams getLayoutParams(View view, ViewGroup root) {
    LayoutParams result = view.getLayoutParams();
    if (result == null) {
      result = createLayoutParams(root);
      view.setLayoutParams(result);
    }
    return result;
  }

  private LayoutParams createLayoutParams(ViewGroup root) {
    LayoutParams result = null;
    if (root != null) {
      try {
        String lookfor = root.getClass().getName() + "$LayoutParams";
        addln(lookfor);
        Class<? extends LayoutParams> clazz = Class.forName(lookfor).asSubclass(LayoutParams.class);
        if (clazz != null) {
          Constructor<? extends LayoutParams> ct = clazz.getConstructor(int.class, int.class);
          result = ct.newInstance(-1, -1);
        }
      } catch (Exception e) {
        result = null;
      }
    }
    if (result == null) {
      result = new LayoutParams(-1, -1);
    }
    return result;
  }

  public void setProperty(View view, String attr, String value) {
    try {
      setProperty(view, (ViewGroup) view.getParent(), attr, value);
    } catch (Exception e) {
      mErrors.add(e.toString());
    }
  }

  private void setProperty(View view, ViewGroup root, String attr, String value)
      throws IllegalArgumentException, IllegalAccessException, InvocationTargetException {
    addln(attr + ":" + value);
    if (attr.equals("layout_width")) {
      getLayoutParams(view, root).width = getLayoutValue(value);
    } else if (attr.equals("layout_height")) {
      getLayoutParams(view, root).height = getLayoutValue(value);
    } else if (attr.equals("layout_gravity")) {
      setIntegerField(getLayoutParams(view, root), "gravity", getInteger(Gravity.class, value));
    } else if (attr.equals("id")) {
      view.setId(calcId(value));
    } else if (attr.equals("gravity")) {
      setInteger(view, attr, getInteger(Gravity.class, value));
    } else if (attr.equals("inputType")) {
      setInteger(view, attr, getInteger(InputType.class, value));
    } else if (attr.equals("background")) {
      setBackground(view, value);
    } else if (attr.equals("src")) {
      setImage(view, value);
    } else {
      setDynamicProperty(view, attr, value);
    }
  }

  private void setBackground(View view, String value) {
    if (value.startsWith("#")) {
      view.setBackgroundColor(getColor(value));
    } else if (value.startsWith("@")) {
      setInteger(view, "backgroundResource", getInteger(view, value));
    } else {
      view.setBackgroundDrawable(getDrawable(value));
    }
  }

  private Drawable getDrawable(String value) {
    try {
      Uri uri = Uri.parse(value);
      if ("file".equals(uri.getScheme())) {
        BitmapDrawable bd = new BitmapDrawable(uri.getPath());
        return bd;
      }
    } catch (Exception e) {
      mErrors.add("failed to load drawable " + value);
    }
    return null;
  }

  private void setImage(View view, String value) {
    if (value.startsWith("@")) {
      setInteger(view, "imageResource", getInteger(view, value));
    } else {
      try {
        Uri uri = Uri.parse(value);
        if ("file".equals(uri.getScheme())) {
          Bitmap bm = BitmapFactory.decodeFile(uri.getPath());
          Method method = view.getClass().getMethod("setImageBitmap", Bitmap.class);
          method.invoke(view, bm);
        } else {
          mErrors.add("Only 'file' currently supported for images");
        }
      } catch (Exception e) {
        mErrors.add("failed to set image " + value);
      }
    }
  }

  private void setIntegerField(Object target, String fieldName, int value) {
    try {
      Field f = target.getClass().getField(fieldName);
      f.setInt(target, value);
    } catch (Exception e) {
      mErrors.add("set field)" + fieldName + " failed. " + e.toString());
    }
  }

  private int getColor(String value) {
    if (value.startsWith("#")) {
      try {
        return (int) Long.parseLong(value.substring(1), 16);
      } catch (Exception e) {
      }
    }
    mErrors.add("Unknown color " + value);
    return 0;
  }

  private int getInputType(String value) {
    int result = 0;
    Integer v = getInputTypes().get(value);
    if (v == null) {
      mErrors.add("Unkown input type " + value);
    } else {
      result = v;
    }
    return result;
  }

  private void setInteger(View view, String attr, int value) {
    String name = "set" + PCase(attr);
    Method m;
    try {
      if ((m = tryMethod(view, name, Context.class, int.class)) != null) {
        m.invoke(view, mContext, value);
      } else if ((m = tryMethod(view, name, int.class)) != null) {
        m.invoke(view, value);
      }
    } catch (Exception e) {
      addln(name + ":" + value + ":" + e.toString());
    }

  }

  private void setDynamicProperty(View view, String attr, String value)
      throws IllegalArgumentException, IllegalAccessException, InvocationTargetException {
    String name = "set" + PCase(attr);
    try {
      Method m = tryMethod(view, name, CharSequence.class);
      if (m != null) {
        m.invoke(view, value);
      } else if ((m = tryMethod(view, name, Context.class, int.class)) != null) {
        m.invoke(view, mContext, getInteger(view, value));
      } else if ((m = tryMethod(view, name, int.class)) != null) {
        m.invoke(view, getInteger(view, value));
      } else if ((m = tryMethod(view, name, float.class)) != null) {
        m.invoke(view, Float.parseFloat(value));
      } else if ((m = tryMethod(view, name, boolean.class)) != null) {
        m.invoke(view, Boolean.parseBoolean(value));
      } else if ((m = tryMethod(view, name, Object.class)) != null) {
        m.invoke(view, value);
      } else {
        mErrors.add(view.getClass().getSimpleName() + ":" + attr + " Property not found.");
      }
    } catch (Exception e) {
      addln(name + ":" + value + ":" + e.toString());
      mErrors.add(name + ":" + value + ":" + e.toString());
    }
  }

  private String PCase(String s) {
    if (s == null) {
      return null;
    }
    if (s.length() > 0) {
      return s.substring(0, 1).toUpperCase() + s.substring(1);
    }
    return "";
  }

  private Method tryMethod(Object o, String name, Class<?>... parameters) {
    Method result;
    try {
      result = o.getClass().getMethod(name, parameters);
    } catch (Exception e) {
      result = null;
    }
    return result;
  }

  public String camelCase(String s) {
    if (s == null) {
      return "";
    } else if (s.length() < 2) {
      return s.toUpperCase();
    } else {
      return s.substring(0, 1).toUpperCase() + s.substring(1).toLowerCase();
    }
  }

  private Integer getInteger(Class<?> clazz, String value) {
    Integer result = null;
    if (value.contains("|")) {
      int work = 0;
      for (String s : value.split("\\|")) {
        work |= getInteger(clazz, s);
      }
      result = work;
    } else {
      if (value.startsWith("?")) {
        result = parseTheme(value);
      } else if (value.startsWith("@")) {
        result = parseTheme(value);
      } else if (value.startsWith("0x")) {
        try {
          result = (int) Long.parseLong(value.substring(2), 16);
        } catch (NumberFormatException e) {
          result = 0;
        }
      } else {
        try {
          result = Integer.parseInt(value);
        } catch (NumberFormatException e) {
          if (clazz == InputType.class) {
            return getInputType(value);
          }
          try {
            Field f = clazz.getField(value.toUpperCase());
            result = f.getInt(null);
          } catch (Exception ex) {
            mErrors.add("Unknown value: " + value);
            result = 0;
          }
        }
      }
    }
    return result;
  }

  private Integer getInteger(View view, String value) {
    return getInteger(view.getClass(), value);
  }

  private Integer parseTheme(String value) {
    int result;
    try {
      String query = "";
      int i;
      value = value.substring(1); // skip past "?"
      i = value.indexOf(":");
      if (i >= 0) {
        query = value.substring(0, i) + ".";
        value = value.substring(i + 1);
      }
      query += "R";
      i = value.indexOf("/");
      if (i >= 0) {
        query += "$" + value.substring(0, i);
        value = value.substring(i + 1);
      }
      Class<?> clazz = Class.forName(query);
      Field f = clazz.getField(value);
      result = f.getInt(null);
    } catch (Exception e) {
      result = 0;
    }
    return result;
  }

  private View viewClass(Context context, String name) {
    View result = null;
    result = viewClassTry(context, "android.view." + name);
    if (result == null) {
      result = viewClassTry(context, "android.widget." + name);
    }
    if (result == null) {
      result = viewClassTry(context, name);
    }
    return result;
  }

  private View viewClassTry(Context context, String name) {
    View result = null;
    try {
      Class<? extends View> viewclass = Class.forName(name).asSubclass(View.class);
      if (viewclass != null) {
        Constructor<? extends View> ct = viewclass.getConstructor(Context.class);
        result = ct.newInstance(context);
      }
    } catch (Exception e) {
    }
    return result;

  }

  public Map<String, Integer> getIdList() {
    return mIdList;
  }

  public List<String> getErrors() {
    return mErrors;
  }

  public String getIdName(int id) {
    for (String key : mIdList.keySet()) {
      if (mIdList.get(key) == id) {
        return key;
      }
    }
    return null;
  }

  public int getId(String name) {
    return mIdList.get(name);
  }

  public Map<String, Map<String, String>> getViewAsMap(View v) {
    Map<String, Map<String, String>> result = new HashMap<String, Map<String, String>>();
    for (Entry<String, Integer> entry : mIdList.entrySet()) {
      View tmp = v.findViewById(entry.getValue());
      if (tmp != null) {
        result.put(entry.getKey(), getViewInfo(tmp));
      }
    }
    return result;
  }

  public Map<String, String> getViewInfo(View v) {
    Map<String, String> result = new HashMap<String, String>();
    if (v.getId() != 0) {
      result.put("id", getIdName(v.getId()));
    }
    result.put("type", v.getClass().getSimpleName());
    addProperty(v, "text", result);
    addProperty(v, "visibility", result);
    addProperty(v, "checked", result);
    addProperty(v, "tag", result);
    addProperty(v, "selectedItemPosition", result);
    addProperty(v, "progress", result);
    return result;
  }

  private void addProperty(View v, String attr, Map<String, String> dest) {
    String result = getProperty(v, attr);
    if (result != null) {
      dest.put(attr, result);
    }
  }

  private String getProperty(View v, String attr) {
    String name = PCase(attr);
    Method m = tryMethod(v, "get" + name);
    if (m == null) {
      m = tryMethod(v, "is" + name);
    }
    String result = null;
    if (m != null) {
      try {
        Object o = m.invoke(v);
        if (o != null) {
          result = o.toString();
        }
      } catch (Exception e) {
        result = null;
      }
    }
    return result;
  }

  public static Map<String, Integer> getInputTypes() {
    if (mInputTypes.size() == 0) {
      mInputTypes.put("none", 0x00000000);
      mInputTypes.put("text", 0x00000001);
      mInputTypes.put("textCapCharacters", 0x00001001);
      mInputTypes.put("textCapWords", 0x00002001);
      mInputTypes.put("textCapSentences", 0x00004001);
      mInputTypes.put("textAutoCorrect", 0x00008001);
      mInputTypes.put("textAutoComplete", 0x00010001);
      mInputTypes.put("textMultiLine", 0x00020001);
      mInputTypes.put("textImeMultiLine", 0x00040001);
      mInputTypes.put("textNoSuggestions", 0x00080001);
      mInputTypes.put("textUri", 0x00000011);
      mInputTypes.put("textEmailAddress", 0x00000021);
      mInputTypes.put("textEmailSubject", 0x00000031);
      mInputTypes.put("textShortMessage", 0x00000041);
      mInputTypes.put("textLongMessage", 0x00000051);
      mInputTypes.put("textPersonName", 0x00000061);
      mInputTypes.put("textPostalAddress", 0x00000071);
      mInputTypes.put("textPassword", 0x00000081);
      mInputTypes.put("textVisiblePassword", 0x00000091);
      mInputTypes.put("textWebEditText", 0x000000a1);
      mInputTypes.put("textFilter", 0x000000b1);
      mInputTypes.put("textPhonetic", 0x000000c1);
      mInputTypes.put("textWebEmailAddress", 0x000000d1);
      mInputTypes.put("textWebPassword", 0x000000e1);
      mInputTypes.put("number", 0x00000002);
      mInputTypes.put("numberSigned", 0x00001002);
      mInputTypes.put("numberDecimal", 0x00002002);
      mInputTypes.put("numberPassword", 0x00000012);
      mInputTypes.put("phone", 0x00000003);
      mInputTypes.put("datetime", 0x00000004);
      mInputTypes.put("date", 0x00000014);
      mInputTypes.put("time", 0x00000024);
    }
    return mInputTypes;
  }

  /** Query class (typically R.id) to extract id names */
  public void setIdList(Class<?> idClass) {
    mIdList.clear();
    for (Field f : idClass.getDeclaredFields()) {
      try {
        String name = f.getName();
        int value = f.getInt(null);
        mIdList.put(name, value);
      } catch (Exception e) {
        // Ignore
      }
    }
  }

  public void setListAdapter(View view, JSONArray items) {
    List<String> list = new ArrayList<String>();
    try {
      for (int i = 0; i < items.length(); i++) {
        list.add(items.get(i).toString());
      }
      ArrayAdapter<String> adapter;
      if (view instanceof Spinner) {
        adapter =
            new ArrayAdapter<String>(mContext, android.R.layout.simple_spinner_item,
                android.R.id.text1, list);
      } else {
        adapter =
            new ArrayAdapter<String>(mContext, android.R.layout.simple_list_item_1,
                android.R.id.text1, list);
      }
      adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
      Method m = tryMethod(view, "setAdapter", SpinnerAdapter.class);
      if (m == null) {
        m = view.getClass().getMethod("setAdapter", ListAdapter.class);
      }
      m.invoke(view, adapter);
    } catch (Exception e) {
      mErrors.add("failed to load list " + e.getMessage());
    }
  }
}
