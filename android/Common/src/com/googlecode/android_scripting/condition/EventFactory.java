package com.googlecode.android_scripting.condition;

import java.io.Serializable;

import android.content.Context;

public interface EventFactory extends Serializable {
  public Event create(Context context);
}
