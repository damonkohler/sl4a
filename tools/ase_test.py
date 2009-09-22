#!/usr/bin/python

import subprocess
import sys

APK = 'AndroidScriptingEnvironment.apk'
APK_PATH = '../android/AndroidScriptingEnvironment/bin/' + APK
SCRIPT_MANAGER = 'com.google.ase/com.google.ase.ScriptManager'


def adb(args):
  print 'Executing: adb ' + args
  p = subprocess.Popen(['adb'] + args.split())
  if p.wait() != 0:
    print 'Failed!'
    sys.exit(1)


def start_activity(intent):
  adb('shell am start -n ' + intent)


adb('uninstall com.google.ase')
adb('shell rm /sdcard/*.zip')
adb('shell rm -r /sdcard/ase')
adb('install ' + APK_PATH)
start_activity(SCRIPT_MANAGER)
