package com.google.ase.jsonrpc;

/**
 * This class marks optional parameters in RPCs. The {@code get} member function
 * returns either the supplied default value (if no value was passed by the
 * caller), or the value that was passed in.
 * 
 * @author Felix Arends (felix.arends@gmail.com)
 * 
 * @param <T> the type of the actual parameter
 */
public class OptionalParameter<T> {
  // Whether or not the parameter is set.
  final private boolean isPresent;

  // The actual parameter value, if it is set.
  final private T value;

  private OptionalParameter(boolean isPresent, T value) {
    this.isPresent = isPresent;
    this.value = value;
  }

  /**
   * A public constructor which creates an instance of the
   * {@link OptionalParameter} class indicating that the parameter value is not
   * set.
   */
  public OptionalParameter() {
    this.isPresent = false;
    this.value = null;
  }

  /**
   * Create an {@link OptionalParameter} instance indicating that the optional
   * parameter is not specified.
   * 
   * @param <T> type parameter of returned instance
   */
  public static <T> OptionalParameter<T> createAbsent() {
    return new OptionalParameter<T>(false, null);
  }

  /**
   * Creates an {@link OptionalParameter} that is present.
   * 
   * @param <T> type parameter of returned instance
   * @param value parameter value
   */
  public static <T> OptionalParameter<T> create(T value) {
    return new OptionalParameter<T>(true, value);
  }

  /**
   * Returns the value stored in this parameter, if it is present, or the
   * defaultValue otherwise.
   * 
   * @param defaultValue value to return in case the parameter wasn't set
   */
  public T get(T defaultValue) {
    if (isPresent) {
      return value;
    } else {
      return defaultValue;
    }
  }
  
  /**
   * Returns whether or not the parameter is specified.
   */
  public boolean isSet() {
    return isPresent;
  }
}
