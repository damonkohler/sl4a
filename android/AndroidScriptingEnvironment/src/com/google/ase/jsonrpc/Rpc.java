package com.google.ase.jsonrpc;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * The {@link Rpc} annotation is used to annotate server-side implementations of
 * RPCs. It describes meta-information (currently a brief documentation of the
 * function), and marks a function as the implementation of an RPC.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@Documented
public @interface Rpc {
  /**
   * Returns brief description of the function. Should be limited to one or two
   * sentences.
   */
  String description();

  /**
   * Gives a brief description of the functions return value (and the underlying
   * data structure).
   */
  String returns() default "";
}
