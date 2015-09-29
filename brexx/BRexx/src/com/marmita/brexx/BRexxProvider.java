package com.marmita.brexx;

import com.googlecode.android_scripting.FileUtils;
import com.googlecode.android_scripting.interpreter.InterpreterDescriptor;
import com.googlecode.android_scripting.interpreter.InterpreterProvider;

import java.io.File;

public class BRexxProvider extends InterpreterProvider {
  @Override
  protected InterpreterDescriptor getDescriptor() { 
	File files = getContext().getFilesDir().getAbsoluteFile();
	  try {
		FileUtils.recursiveChmod(files, 0777);
	} catch (Exception e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
	return new BRexxDescriptor();
  }
}