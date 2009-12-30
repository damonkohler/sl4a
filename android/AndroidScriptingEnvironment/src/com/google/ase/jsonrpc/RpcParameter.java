package com.google.ase.jsonrpc;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * An annotation that is used to document the parameters of an RPC.
 * 
 * @author Felix Arends (felix.arends@gmail.com)
 * 
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.PARAMETER)
public @interface RpcParameter {
  /**
   * The value returned by defaultValue if a parameter is required.
   */
  final static public Object REQUIRED = new Object();

  /**
   * A description of the RPC parameter. There is no need to include the
   * parameter type (this information will be generated automatically).
   */
  public String value();
}
