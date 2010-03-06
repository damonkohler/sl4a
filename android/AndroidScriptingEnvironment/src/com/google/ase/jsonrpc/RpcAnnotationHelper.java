/*
 * Copyright (C) 2009 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package com.google.ase.jsonrpc;

import java.lang.annotation.Annotation;

/**
 * A helper class that helps extract descriptions and default values from
 * parameter annotations.
 *
 * @author Felix Arends (felix.arends@gmail.com)
 *
 */
public class RpcAnnotationHelper {
  /** This is a utility class. We don't want instances of it */
  private RpcAnnotationHelper() {
  }

  /**
   * Extracts the parameter description from its annotations.
   *
   * @param annotations the annotations of the parameter
   * @return the description of the parameter
   */
  public static String getDescription(Annotation[] annotations) {
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
        return ((RpcOptionalString) a).description();
      }
    }
    return "(no description)";
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
   * Returns whether the default value is specified
   * for a specific parameter.
   *
   * @param annotations annotations of the parameter
   */
  public static boolean hasDefaultValue(Annotation[] annotations) {
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
        return false;
      } else if (a instanceof RpcOptionalString) {
        return false;
      }
    }
    return false;
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
