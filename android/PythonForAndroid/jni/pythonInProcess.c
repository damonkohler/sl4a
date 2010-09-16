/*
 * Copyright (C) 2010 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
#include <Python.h>

#include <jni.h>
#include <stdio.h>

#include <android/log.h>

#define LOGD(args...) __android_log_print(ANDROID_LOG_DEBUG, "sl4a", args)
#define LOGW(args...) __android_log_print(ANDROID_LOG_WARN, "sl4a", args)

jint
JNI_OnLoad(JavaVM *vm, void *reserved)
{
	return JNI_VERSION_1_6;
}

void
JNI_OnUnload(JavaVM *vm, void *reserved)
{}

/**
 * Run the Python script specified by the given filename.
 */
void
Java_com_googlecode_pythonforandroid_PythonInProcessInterpreter_runScript(JNIEnv* env, jobject obj, jstring filename)
{
	const jbyte *str = NULL;
	FILE *fp = NULL;
	
//    setenv("PYTHONVERBOSE", "1", 1);
    Py_Initialize();

    str = (*env)->GetStringUTFChars(env, filename, NULL);
    if (str == NULL) {
		(*env)->ExceptionDescribe(env);
		goto done;
	}

	fp = fopen(str, "r");
	if (fp == NULL) {
		LOGW("Failed to open Python script '%s'", str);
		goto done;
	}

	PyRun_SimpleFile(fp, str);
    Py_Finalize();
	
done:
	if (str) (*env)->ReleaseStringUTFChars(env, filename, str);
	if (fp) fclose(fp);
}
