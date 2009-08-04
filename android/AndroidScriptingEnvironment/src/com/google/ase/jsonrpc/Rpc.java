package com.google.ase.jsonrpc;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Rpc {

  String description();

  String params() default "None";

  String returns() default "None";

}
