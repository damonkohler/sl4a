// Copyright 2010 Google Inc. All Rights Reserved.

package com.googlecode.android_scripting.provider;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * This proxy provides access to a list and allows for setting selection criteria, which modifies
 * the view of the list exposed to the environment. When selection is set, the proxy exposes a
 * modified list containing only such items that the String returned by the getString(item) contains
 * selection criterion set by the setSelection(String selection).
 */
public abstract class SelectableListProxy<T> implements Iterable<T> {
  protected List<T> mmBaseList;
  protected final List<T> mmSelectedList = new ArrayList<T>();
  protected String mmSelection = null;

  /**
   * Constructs a new SelectableListProxy with the specified base list.
   * 
   * @param list
   *          a list to proxify
   */
  public SelectableListProxy(List<T> list) {
    mmBaseList = list;
  }

  /**
   * Replaces the underlying base list with a new one.
   * 
   * @param list
   *          a list to proxify
   */
  public void replace(List<T> list) {
    mmBaseList = list;
    select();
  }

  /**
   * Sets the selection criterion.
   * 
   * @param selection
   *          selection criterion
   */
  public void setSelection(String selection) {
    if (selection == null || selection.length() == 0) {
      mmSelection = null;
    } else {
      mmSelection = selection.toLowerCase();
    }
    select();
  }

  private void select() {
    mmSelectedList.clear();
    if (mmSelection != null && mmBaseList != null) {
      for (T f : mmBaseList) {
        if (getString(f).contains(mmSelection)) {
          mmSelectedList.add(f);
        }
      }
    }
  }

  /**
   * Resets the selection criterion.
   */
  public void reset() {
    mmSelection = null;
  }

  /**
   * Returns the number of elements in the list according to the current selection criterion.
   * 
   * @return the number of elements in the list
   */
  public int size() {
    if (mmSelection == null) {
      if (mmBaseList == null) {
        return 0;
      }
      return mmBaseList.size();
    } else {
      return mmSelectedList.size();
    }
  }

  /**
   * Returns the element at the specified position in the list.
   * 
   * @param index
   *          index of the element to return
   * @return the element at the specified position in this list
   * @throws IndexOutOfBoundsException
   */
  public T get(int index) {
    if (mmSelection == null) {
      return mmBaseList.get(index);
    } else {
      return mmSelectedList.get(index);
    }
  }

  /**
   * Returns an iterator over the elements in the list in proper sequence.
   * 
   * @return an iterator over the elements in the list in proper sequence
   */
  @Override
  public Iterator<T> iterator() {
    if (mmSelection == null) {
      if (mmBaseList == null) {
        return mmSelectedList.iterator();
      }
      return mmBaseList.iterator();
    } else {
      return mmSelectedList.iterator();
    }
  }

  /**
   * Returns String representation of an item from the list.
   * 
   * @return String representation of an item from the list
   */
  public abstract String getString(T item);
}
