package com.google.ase.condition;

import java.io.Serializable;

import android.content.Context;

public interface ConditionFactory extends Serializable {
  public Condition create(Context context);
}
