package com.google.ase.facade;

public class Event {

  private String mName;
  private Object mData;

  public Event(String name, Object data) {
    setName(name);
    setData(data);
  }

  public void setName(String name) {
    mName = name;
  }

  public String getName() {
    return mName;
  }

  public void setData(Object data) {
    mData = data;
  }

  public Object getData() {
    return mData;
  }

}
