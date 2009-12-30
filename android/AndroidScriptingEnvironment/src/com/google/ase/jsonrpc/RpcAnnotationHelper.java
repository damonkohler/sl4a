package com.google.ase.jsonrpc;

import java.lang.annotation.Annotation;

public class RpcAnnotationHelper {
  /** This is a utility class.  We don't want instances of it */
  private RpcAnnotationHelper() {}
  
  /**
   * Extracts the parameter description from its annotations.
   * 
   * @param annotations the annotations of the parameter
   * @return the description of the parameter
   */
  public static String getRPCDescription(Annotation[] annotations) {
    for (Annotation a : annotations) {
      if (a instanceof RpcParameter) {
        return ((RpcParameter) a).value();
      } else if (a instanceof RpcDefaultInteger) {
        return ((RpcDefaultInteger) a).description();
      } else if (a instanceof RpcDefaultString) {
        return ((RpcDefaultString) a).description();
      } else if (a instanceof RpcDefaultBoolean) {
        return ((RpcDefaultBoolean) a).description();
      } else if (a instanceof RpcOptionalObject) {
        return ((RpcOptionalObject) a).value();
      } else if (a instanceof RpcOptionalString) {
        return ((RpcOptionalString) a).value();
      }
    }
    
    return "-";
  }
  
  /**
   * Returns the default value for a specific parameter.
   * 
   * @param annotations annotations of the parameter
   */
  public static Object getDefaultValue(Annotation[] annotations) {
    for (Annotation a : annotations) {
      if (a instanceof RpcParameter) {
        return null;
      } else if (a instanceof RpcDefaultInteger) {
        return ((RpcDefaultInteger) a).defaultValue();
      } else if (a instanceof RpcDefaultString) {
        return ((RpcDefaultString) a).defaultValue();
      } else if (a instanceof RpcDefaultBoolean) {
        return ((RpcDefaultBoolean) a).defaultValue();
      } else if (a instanceof RpcOptionalObject) {
        return null;
      } else if (a instanceof RpcOptionalString) {
        return null;
      }
    }
    
    return null;
  }
  
  /**
   * Determines whether or not this parameter is optional.
   * 
   * @param annotations annotations of the parameter
   */
  public static boolean isOptionalParameter(Annotation[] annotations) {
    for (Annotation a : annotations) {
      if (a instanceof RpcParameter) {
        return false;
      } else if (a instanceof RpcDefaultInteger) {
        return true;
      } else if (a instanceof RpcDefaultString) {
        return true;
      } else if (a instanceof RpcDefaultBoolean) {
        return true;
      } else if (a instanceof RpcOptionalObject) {
        return true;
      } else if (a instanceof RpcOptionalString) {
        return true;
      }
    }
    
    return false;
  }
}
