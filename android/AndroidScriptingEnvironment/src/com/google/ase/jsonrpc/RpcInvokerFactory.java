package com.google.ase.jsonrpc;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.math.BigInteger;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.google.ase.AseLog;

/**
 * A factory for {@link RpcInvoker} objects.
 * 
 * @author Felix Arends (felix.arends@gmail.com)
 * 
 */
public class RpcInvokerFactory {
  /**
   * Produces an RpcInvoker implementation for a given list of parameter types.
   * 
   * @param parameterTypes an array of the (possibly generic) types of the
   *        parameters
   * @return an {@link RpcInvoker} object that can invoke methods with the given
   *         parameter types
   */
  public static RpcInvoker createInvoker(final Type[] parameterTypes) {
    return new RpcInvoker() {
      @Override
      public JSONObject invoke(final Method m, final Object receiver, final JSONArray parameters)
          throws JSONException {
        Object[] args = new Object[parameterTypes.length];

        for (int i = 0; i < args.length; i++) {
          final Type parameterType = parameterTypes[i];

          if (parameterType instanceof Class<?>) {
            // Required parameter.
            try {
              args[i] = ((Class <?>)parameterType).cast(parameters.get(i));
            } catch (ClassCastException e) {
              return JsonRpcResult.error("Argument " + (i + 1) + " should be of type " +
                  ((Class<?>)parameterType).getSimpleName() + ".");
            }
          } else {
            // Optional parameter.
            final ParameterizedType parameterizedType = (ParameterizedType) parameterType;
            final Class<?> rawClass = (Class<?>) parameterizedType.getRawType();

            if (rawClass.equals(OptionalParameter.class)) {
              // NOTE (Felix Arends): Technically, this could be another
              // ParameterizedType.
              Class<?> actualType = (Class<?>) parameterizedType.getActualTypeArguments()[0];
              if (i >= parameters.length()) {
                try {
                  args[i] = rawClass.newInstance();
                } catch (IllegalAccessException e) {
                  // This should never happen: the constructor is public.
                  throw new RuntimeException(e);
                } catch (InstantiationException e) {
                  throw new RuntimeException(e);
                }
              } else {
                try {
                  args[i] = OptionalParameter.create(actualType.cast(parameters.get(i)));
                } catch (ClassCastException e) {
                  return JsonRpcResult.error("Argument " + (i + 1) + " should be of type " +
                      actualType.getSimpleName() + ".");
                }
              }
            }
          }
        }

        try {
          return JsonRpcResult.result(m.invoke(receiver, args));
        } catch (InvocationTargetException e) {
          // We re-throw this exception as a runtime exception, because we don't
          // ever expect to receive it, but we don't want to hide it completely
          // either.
          return JsonRpcResult.error(e.getCause().toString(), e.getCause());
        } catch (IllegalAccessException e) {
          // We re-throw this exception as a runtime exception, because we don't
          // ever expect to receive it, but we don't want to hide it completely
          // either.
          throw new RuntimeException(e);
        } catch (Throwable t) {
          // All other exceptions are passed back to the client.
          return JsonRpcResult.error("Exception", t);
        }
      }
    };
  }

  // This static class is not to be instantiated.
  private RpcInvokerFactory() {
  }
}
