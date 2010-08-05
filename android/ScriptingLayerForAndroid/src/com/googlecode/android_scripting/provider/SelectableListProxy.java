// Copyright 2010 Google Inc. All Rights Reserved.

package com.googlecode.android_scripting.provider;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public abstract class SelectableListProxy<T> implements Iterable<T> {
  protected List<T> mmBaseList;
  protected final List<T> mmSelectedList = new ArrayList<T>();
  protected String mmQuery = null;

  public SelectableListProxy(List<T> list) {
    mmBaseList = list;
  }

  public void replace(List<T> list) {
    mmBaseList = list;
    select();
  }

  public void setQuery(String prefix) {
    if (prefix != null && prefix.length() == 0) {
      mmQuery = null;
    } else {
      mmQuery = prefix.toLowerCase();
    }
    select();
  }

  private void select() {
    mmSelectedList.clear();
    if (mmQuery != null && mmBaseList != null) {
      for (T f : mmBaseList) {
        if (getString(f).contains(mmQuery)) {
          mmSelectedList.add(f);
        }
      }
    }
  }

  public void reset() {
    mmQuery = null;
  }

  public int size() {
    if (mmQuery == null) {
      if (mmBaseList == null) {
        return 0;
      }
      return mmBaseList.size();
    } else {
      return mmSelectedList.size();
    }
  }

  public T get(int index) {
    if (mmQuery == null) {
      return mmBaseList.get(index);
    } else {
      return mmSelectedList.get(index);
    }
  }

  @Override
  public Iterator<T> iterator() {
    if (mmQuery == null) {
      if (mmBaseList == null) {
        return mmSelectedList.iterator();
      }
      return mmBaseList.iterator();
    } else {
      return mmSelectedList.iterator();
    }
  }

  public abstract String getString(T item);
}
