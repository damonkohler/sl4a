package com.marmita.brexx;

import android.content.Context;

import com.googlecode.android_scripting.AsyncTaskListener;
import com.googlecode.android_scripting.FileUtils;
import com.googlecode.android_scripting.InterpreterInstaller;
import com.googlecode.android_scripting.Log;
import com.googlecode.android_scripting.exception.Sl4aException;
import com.googlecode.android_scripting.interpreter.InterpreterConstants;
import com.googlecode.android_scripting.interpreter.InterpreterDescriptor;
import com.googlecode.android_scripting.interpreter.InterpreterUtils;

import java.io.File;
import java.lang.reflect.Method;

public class BRexxInstaller extends InterpreterInstaller {

	public BRexxInstaller(InterpreterDescriptor descriptor, Context context,
			AsyncTaskListener<Boolean> listener) throws Sl4aException {
		super(descriptor, context, listener);
	}

	@Override
	protected boolean setup() {
		File tmp = new File(InterpreterConstants.SDCARD_ROOT
				+ getClass().getPackage().getName()
				+ InterpreterConstants.INTERPRETER_EXTRAS_ROOT,
				"tmp");
		if (!tmp.isDirectory()) {
			try {
				tmp.mkdir();
			} catch (SecurityException e) {
				Log.e(mContext, "Setup failed.", e);
				return false;
			}
		}
		return true;
	}	  
}
