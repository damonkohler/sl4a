package com.google.ase;

public class CircularBuffer<T> {
  private final T[] mBuffer;
  private int mStart;
  private int mEnd;

  @SuppressWarnings("unchecked")
  public CircularBuffer(int size) {
    mBuffer = (T[]) new Object[size];
    mStart = 0;
    mEnd = 0;
  }

  public void add(T value) {
    mBuffer[mEnd] = value;
    mEnd = (mEnd + 1) % mBuffer.length;
    if (mEnd == mStart) {
      // At this point, we've filled the buffer and start overwriting old entries.
      mStart = (mStart + 1) % mBuffer.length;
    }
  }

  public T get() {
    if (mStart == mEnd) {
      return null;
    }
    T value = mBuffer[mStart];
    mStart = (mStart + 1) % mBuffer.length;
    return value;
  }
}
