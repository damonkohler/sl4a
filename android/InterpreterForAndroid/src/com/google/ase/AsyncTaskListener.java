package com.google.ase;

public interface AsyncTaskListener<T> {
  public void onTaskFinished(T result, String message);
}
