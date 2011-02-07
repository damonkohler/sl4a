/*
 * Copyright (C) 2010 Google Inc.
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

package com.googlecode.android_scripting.facade;

import android.app.Activity;
import android.content.Intent;

import com.googlecode.android_scripting.Constants;
import com.googlecode.android_scripting.jsonrpc.RpcReceiver;
import com.googlecode.android_scripting.rpc.Rpc;
import com.googlecode.android_scripting.rpc.RpcParameter;

import java.io.Serializable;

/**
 * Allows you to return results to a startActivityForResult call.
 * 
 * @author Alexey Reznichenko (alexey.reznichenko@gmail.com)
 */
public class ActivityResultFacade extends RpcReceiver {

  private static final String sRpcDescription =
      "Sets the result of a script execution. Whenever the script APK is called via "
          + "startActivityForResult(), the resulting intent will contain " + Constants.EXTRA_RESULT
          + " extra with the given value.";
  private static final String sCodeDescription =
      "The result code to propagate back to the originating activity, often RESULT_CANCELED (0) "
          + "or RESULT_OK (-1)";

  private Activity mActivity = null;
  private Intent mResult = null;
  private int mResultCode;

  public ActivityResultFacade(FacadeManager manager) {
    super(manager);
  }

  @Rpc(description = sRpcDescription)
  public synchronized void setResultBoolean(
      @RpcParameter(name = "resultCode", description = sCodeDescription) Integer resultCode,
      @RpcParameter(name = "resultValue") Boolean resultValue) {
    mResult = new Intent();
    mResult.putExtra(Constants.EXTRA_RESULT, resultValue.booleanValue());
    mResultCode = resultCode;
    if (mActivity != null) {
      setResult();
    }
  }

  @Rpc(description = sRpcDescription)
  public synchronized void setResultByte(
      @RpcParameter(name = "resultCode", description = sCodeDescription) Integer resultCode,
      @RpcParameter(name = "resultValue") Byte resultValue) {
    mResult = new Intent();
    mResult.putExtra(Constants.EXTRA_RESULT, resultValue.byteValue());
    mResultCode = resultCode;
    if (mActivity != null) {
      setResult();
    }
  }

  @Rpc(description = sRpcDescription)
  public synchronized void setResultShort(
      @RpcParameter(name = "resultCode", description = sCodeDescription) Integer resultCode,
      @RpcParameter(name = "resultValue") Short resultValue) {
    mResult = new Intent();
    mResult.putExtra(Constants.EXTRA_RESULT, resultValue.shortValue());
    mResultCode = resultCode;
    if (mActivity != null) {
      setResult();
    }
  }

  @Rpc(description = sRpcDescription)
  public synchronized void setResultChar(
      @RpcParameter(name = "resultCode", description = sCodeDescription) Integer resultCode,
      @RpcParameter(name = "resultValue") Character resultValue) {
    mResult = new Intent();
    mResult.putExtra(Constants.EXTRA_RESULT, resultValue.charValue());
    mResultCode = resultCode;
    if (mActivity != null) {
      setResult();
    }
  }

  @Rpc(description = sRpcDescription)
  public synchronized void setResultInteger(
      @RpcParameter(name = "resultCode", description = sCodeDescription) Integer resultCode,
      @RpcParameter(name = "resultValue") Integer resultValue) {
    mResult = new Intent();
    mResult.putExtra(Constants.EXTRA_RESULT, resultValue.intValue());
    mResultCode = resultCode;
    if (mActivity != null) {
      setResult();
    }
  }

  @Rpc(description = sRpcDescription)
  public synchronized void setResultLong(
      @RpcParameter(name = "resultCode", description = sCodeDescription) Integer resultCode,
      @RpcParameter(name = "resultValue") Long resultValue) {
    mResult = new Intent();
    mResult.putExtra(Constants.EXTRA_RESULT, resultValue.longValue());
    mResultCode = resultCode;
    if (mActivity != null) {
      setResult();
    }
  }

  @Rpc(description = sRpcDescription)
  public synchronized void setResultFloat(
      @RpcParameter(name = "resultCode", description = sCodeDescription) Integer resultCode,
      @RpcParameter(name = "resultValue") Float resultValue) {
    mResult = new Intent();
    mResult.putExtra(Constants.EXTRA_RESULT, resultValue.floatValue());
    mResultCode = resultCode;
    if (mActivity != null) {
      setResult();
    }
  }

  @Rpc(description = sRpcDescription)
  public synchronized void setResultDouble(
      @RpcParameter(name = "resultCode", description = sCodeDescription) Integer resultCode,
      @RpcParameter(name = "resultValue") Double resultValue) {
    mResult = new Intent();
    mResult.putExtra(Constants.EXTRA_RESULT, resultValue.doubleValue());
    mResultCode = resultCode;
    if (mActivity != null) {
      setResult();
    }
  }

  @Rpc(description = sRpcDescription)
  public synchronized void setResultString(
      @RpcParameter(name = "resultCode", description = sCodeDescription) Integer resultCode,
      @RpcParameter(name = "resultValue") String resultValue) {
    mResult = new Intent();
    mResult.putExtra(Constants.EXTRA_RESULT, resultValue);
    mResultCode = resultCode;
    if (mActivity != null) {
      setResult();
    }
  }

  @Rpc(description = sRpcDescription)
  public synchronized void setResultBooleanArray(
      @RpcParameter(name = "resultCode", description = sCodeDescription) Integer resultCode,
      @RpcParameter(name = "resultValue") Boolean[] resultValue) {
    mResult = new Intent();
    boolean[] array = new boolean[resultValue.length];
    for (int i = 0; i < resultValue.length; i++) {
      array[i] = resultValue[i];
    }
    mResult.putExtra(Constants.EXTRA_RESULT, array);
    mResultCode = resultCode;
    if (mActivity != null) {
      setResult();
    }
  }

  @Rpc(description = sRpcDescription)
  public synchronized void setResultByteArray(
      @RpcParameter(name = "resultCode", description = sCodeDescription) Integer resultCode,
      @RpcParameter(name = "resultValue") Byte[] resultValue) {
    mResult = new Intent();
    byte[] array = new byte[resultValue.length];
    for (int i = 0; i < resultValue.length; i++) {
      array[i] = resultValue[i];
    }
    mResult.putExtra(Constants.EXTRA_RESULT, array);
    mResultCode = resultCode;
    if (mActivity != null) {
      setResult();
    }
  }

  @Rpc(description = sRpcDescription)
  public synchronized void setResultShortArray(
      @RpcParameter(name = "resultCode", description = sCodeDescription) Integer resultCode,
      @RpcParameter(name = "resultValue") Short[] resultValue) {
    mResult = new Intent();
    short[] array = new short[resultValue.length];
    for (int i = 0; i < resultValue.length; i++) {
      array[i] = resultValue[i];
    }
    mResult.putExtra(Constants.EXTRA_RESULT, array);
    mResultCode = resultCode;
    if (mActivity != null) {
      setResult();
    }
  }

  @Rpc(description = sRpcDescription)
  public synchronized void setResultCharArray(
      @RpcParameter(name = "resultCode", description = sCodeDescription) Integer resultCode,
      @RpcParameter(name = "resultValue") Character[] resultValue) {
    mResult = new Intent();
    char[] array = new char[resultValue.length];
    for (int i = 0; i < resultValue.length; i++) {
      array[i] = resultValue[i];
    }
    mResult.putExtra(Constants.EXTRA_RESULT, array);
    mResultCode = resultCode;
    if (mActivity != null) {
      setResult();
    }
  }

  @Rpc(description = sRpcDescription)
  public synchronized void setResultIntegerArray(
      @RpcParameter(name = "resultCode", description = sCodeDescription) Integer resultCode,
      @RpcParameter(name = "resultValue") Integer[] resultValue) {
    mResult = new Intent();
    int[] array = new int[resultValue.length];
    for (int i = 0; i < resultValue.length; i++) {
      array[i] = resultValue[i];
    }
    mResult.putExtra(Constants.EXTRA_RESULT, array);
    mResultCode = resultCode;
    if (mActivity != null) {
      setResult();
    }
  }

  @Rpc(description = sRpcDescription)
  public synchronized void setResultLongArray(
      @RpcParameter(name = "resultCode", description = sCodeDescription) Integer resultCode,
      @RpcParameter(name = "resultValue") Long[] resultValue) {
    mResult = new Intent();
    long[] array = new long[resultValue.length];
    for (int i = 0; i < resultValue.length; i++) {
      array[i] = resultValue[i];
    }
    mResult.putExtra(Constants.EXTRA_RESULT, array);
    mResultCode = resultCode;
    if (mActivity != null) {
      setResult();
    }
  }

  @Rpc(description = sRpcDescription)
  public synchronized void setResultFloatArray(
      @RpcParameter(name = "resultCode", description = sCodeDescription) Integer resultCode,
      @RpcParameter(name = "resultValue") Float[] resultValue) {
    mResult = new Intent();
    float[] array = new float[resultValue.length];
    for (int i = 0; i < resultValue.length; i++) {
      array[i] = resultValue[i];
    }
    mResult.putExtra(Constants.EXTRA_RESULT, array);
    mResultCode = resultCode;
    if (mActivity != null) {
      setResult();
    }
  }

  @Rpc(description = sRpcDescription)
  public synchronized void setResultDoubleArray(
      @RpcParameter(name = "resultCode", description = sCodeDescription) Integer resultCode,
      @RpcParameter(name = "resultValue") Double[] resultValue) {
    mResult = new Intent();
    double[] array = new double[resultValue.length];
    for (int i = 0; i < resultValue.length; i++) {
      array[i] = resultValue[i];
    }
    mResult.putExtra(Constants.EXTRA_RESULT, array);
    mResultCode = resultCode;
    if (mActivity != null) {
      setResult();
    }
  }

  @Rpc(description = sRpcDescription)
  public synchronized void setResultStringArray(
      @RpcParameter(name = "resultCode", description = sCodeDescription) Integer resultCode,
      @RpcParameter(name = "resultValue") String[] resultValue) {
    mResult = new Intent();
    mResult.putExtra(Constants.EXTRA_RESULT, resultValue);
    mResultCode = resultCode;
    if (mActivity != null) {
      setResult();
    }
  }

  @Rpc(description = sRpcDescription)
  public synchronized void setResultSerializable(
      @RpcParameter(name = "resultCode", description = sCodeDescription) Integer resultCode,
      @RpcParameter(name = "resultValue") Serializable resultValue) {
    mResult = new Intent();
    mResult.putExtra(Constants.EXTRA_RESULT, resultValue);
    mResultCode = resultCode;
    if (mActivity != null) {
      setResult();
    }
  }

  public synchronized void setActivity(Activity activity) {
    mActivity = activity;
    if (mResult != null) {
      setResult();
    }
  }

  private void setResult() {
    mActivity.setResult(mResultCode, mResult);
    mActivity.finish();
  }

  @Override
  public void shutdown() {
  }
}
