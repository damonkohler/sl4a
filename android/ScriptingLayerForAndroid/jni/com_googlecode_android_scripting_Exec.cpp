/*
 * Copyright (C) 2007 The Android Open Source Project
 * Copyright (C) 2009 Google, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

#include "com_googlecode_android_scripting_Exec.h"

#include <errno.h>
#include <fcntl.h>
#include <stdlib.h>
#include <sys/ioctl.h>
#include <sys/types.h>
#include <sys/wait.h>
#include <termios.h>
#include <unistd.h>
#include <stdio.h>

#include "android/log.h"

#define LOG_TAG "Exec"
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__)

int CreateSubprocess(const char* cmd, char* args[], char* vars[], char *wkdir, pid_t* pid) {
  char* devname;
  int ptm = open("/dev/ptmx", O_RDWR);
  if(ptm < 0){
    LOGE("Cannot open /dev/ptmx: %s\n", strerror(errno));
    return -1;
  }
  fcntl(ptm, F_SETFD, FD_CLOEXEC);

  if (grantpt(ptm) || unlockpt(ptm) ||
      ((devname = (char*) ptsname(ptm)) == 0)) {
    LOGE("Trouble with /dev/ptmx: %s\n", strerror(errno));
    return -1;
  }

  *pid = fork();
  if(*pid < 0) {
    LOGE("Fork failed: %s\n", strerror(errno));
    return -1;
  }

  if(*pid == 0){
    int pts;
    setsid();
    pts = open(devname, O_RDWR);
    if(pts < 0) {
      exit(-1);
    }
    dup2(pts, 0);
    dup2(pts, 1);
    dup2(pts, 2);
    close(ptm);
    if (wkdir) chdir(wkdir);
    execve(cmd, args, vars);
    exit(-1);
  } else {
    return ptm;
  }
}

void JNU_ThrowByName(JNIEnv* env, const char* name, const char* msg) {
  jclass clazz = env->FindClass(name);
  if (clazz != NULL) {
    env->ThrowNew(clazz, msg);
  }
  env->DeleteLocalRef(clazz);
}

char* JNU_GetStringNativeChars(JNIEnv* env, jstring jstr) {
  if (jstr == NULL) {
    return NULL;
  }
  jbyteArray bytes = 0;
  jthrowable exc;
  char* result = 0;
  if (env->EnsureLocalCapacity(2) < 0) {
    return 0; /* out of memory error */
  }
  jclass Class_java_lang_String = env->FindClass("java/lang/String");
  jmethodID MID_String_getBytes = env->GetMethodID(
      Class_java_lang_String, "getBytes", "()[B");
  bytes = (jbyteArray) env->CallObjectMethod(jstr, MID_String_getBytes);
  exc = env->ExceptionOccurred();
  if (!exc) {
    jint len = env->GetArrayLength(bytes);
    result = (char*) malloc(len + 1);
    if (result == 0) {
      JNU_ThrowByName(env, "java/lang/OutOfMemoryError", 0);
      env->DeleteLocalRef(bytes);
      return 0;
    }
    env->GetByteArrayRegion(bytes, 0, len, (jbyte*) result);
    result[len] = 0; /* NULL-terminate */
  } else {
    env->DeleteLocalRef(exc);
  }
  env->DeleteLocalRef(bytes);
  return result;
}

int JNU_GetFdFromFileDescriptor(JNIEnv* env, jobject fileDescriptor) {
  jclass Class_java_io_FileDescriptor = env->FindClass("java/io/FileDescriptor");
  jfieldID descriptor = env->GetFieldID(Class_java_io_FileDescriptor, "descriptor", "I");
  return env->GetIntField(fileDescriptor, descriptor);
}

JNIEXPORT jobject JNICALL Java_com_googlecode_android_1scripting_Exec_createSubprocess(
    JNIEnv* env, jclass clazz, jstring cmd, jobjectArray argArray, jobjectArray varArray,
    jstring workingDirectory,
    jintArray processIdArray) {
  char* cmd_native = JNU_GetStringNativeChars(env, cmd);
  char* wkdir_native = JNU_GetStringNativeChars(env, workingDirectory);
  pid_t pid;
  jsize len = 0;
  if (argArray) {
    len = env->GetArrayLength(argArray);
  }
  char* args[len + 2];
  args[0] = cmd_native;
  for (int i = 0; i < len; i++) {
    jstring arg = (jstring) env->GetObjectArrayElement(argArray, i);
    char* arg_native = JNU_GetStringNativeChars(env, arg);
    args[i + 1] = arg_native;
  }
  args[len + 1] = NULL;

  len = 0;
  if (varArray) {
    len = env->GetArrayLength(varArray);
  }
  char* vars[len + 1];
  for (int i = 0; i < len; i++) {
    jstring var = (jstring) env->GetObjectArrayElement(varArray, i);
    char* var_native = JNU_GetStringNativeChars(env, var);
    vars[i] = var_native;
  }
  vars[len] = NULL;

  int ptm = CreateSubprocess(cmd_native, args, vars, wkdir_native, &pid);
  if (processIdArray) {
    if (env->GetArrayLength(processIdArray) > 0) {
      jboolean isCopy;
      int* proccessId = (int*) env->GetPrimitiveArrayCritical(processIdArray, &isCopy);
      if (proccessId) {
        *proccessId = (int) pid;
        env->ReleasePrimitiveArrayCritical(processIdArray, proccessId, 0);
      }
    }
  }

  jclass Class_java_io_FileDescriptor =
      env->FindClass("java/io/FileDescriptor");
  jmethodID init = env->GetMethodID(Class_java_io_FileDescriptor, "<init>", "()V");
  jobject result = env->NewObject(Class_java_io_FileDescriptor, init);

  if (!result) {
    LOGE("Couldn't create a FileDescriptor.");
  } else {
    jfieldID descriptor = env->GetFieldID(Class_java_io_FileDescriptor, "descriptor", "I");
    env->SetIntField(result, descriptor, ptm);
  }
  return result;
}

JNIEXPORT void JNICALL Java_com_googlecode_android_1scripting_Exec_setPtyWindowSize(
    JNIEnv* env, jclass clazz, jobject fileDescriptor, jint row, jint col, jint xpixel,
    jint ypixel) {
  struct winsize sz;
  int fd = JNU_GetFdFromFileDescriptor(env, fileDescriptor);
  if (env->ExceptionOccurred() != NULL) {
    return;
  }
  sz.ws_row = row;
  sz.ws_col = col;
  sz.ws_xpixel = xpixel;
  sz.ws_ypixel = ypixel;
  ioctl(fd, TIOCSWINSZ, &sz);
}

JNIEXPORT jint JNICALL Java_com_googlecode_android_1scripting_Exec_waitFor(JNIEnv* env, jclass clazz, jint procId) {
  int status;
  waitpid(procId, &status, 0);
  int result = 0;
  if (WIFEXITED(status)) {
    result = WEXITSTATUS(status);
  }
  return result;
}
