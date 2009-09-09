#!/usr/bin/python

# Copyright (C) 2009 Google Inc.
#
# Licensed under the Apache License, Version 2.0 (the "License"); you may not
# use this file except in compliance with the License. You may obtain a copy of
# the License at
#
# http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
# WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
# License for the specific language governing permissions and limitations under
# the License.
#
# Originally written by Mike Forster.
# http://blog.michael-forster.de/2008/12/view-android-source-code-in-eclipse.html

from __future__ import with_statement  # for Python < 2.6

"""Modifies the Android SDK to build Android Scripting Environment.

This script compiles android.os.Exec from the Android source and adds the
class to the Android SDK android.jar. In addition, it copies the source files
into the SDK so that they can be browsed from Eclipse.

In order to use this script, you must have download the Android source and
installed the Android SDK.
"""

__author__ = 'Damon Kohler <damonkohler@gmail.com>'

import os
import re
import shutil
import subprocess
import sys
import zipfile


def validate_source_and_sdk_locations(src_location, sdk_location):
  if not os.path.exists(src_location):
    print 'Android source location is invalid.'
    sys.exit(1)
  if not os.path.exists(sdk_location):
    print 'SDK location is invalid.'
    sys.exit(1)
  return os.path.join(sdk_location, 'platforms', 'android-1.5')


def copy_sources(src_location, sdk_location):
  sdk = validate_source_and_sdk_locations(src_location, sdk_location)
  out = os.path.join(src_location, 'out')
  sources = os.path.join(sdk, 'sources')
  if not os.path.exists(sources):
    os.makedirs(sources)
  print 'Copying sources from  %s to %s' % (src_location, sources)
  # Some files are duplicated, copy them only once.
  written = {}
  # Iterate over all Java files.
  for dir, subdirs, files in os.walk(src_location):
    if dir.startswith(out):
      continue  # Skip copying stub files.
    for filename in [f for f in files if f.endswith('.java')]:
      # Search package name.
      source = os.path.join(dir, filename)
      with open(source) as f:
        for line in f:
          match = re.match(r'\s*package\s+([a-zA-Z0-9\._]+);', line)
          if match:
            package_path = match.group(1).replace('.', os.sep)
            try:
              os.makedirs(os.path.join(sources, package_path))
            except os.error:
              pass
            destination = os.path.join(sources, package_path, filename)
            if destination not in written:
              written[destination] = True
              shutil.copy(source, destination)
            break

def add_android_os_exec(src_location, sdk_location):
  print 'Adding android.os.Exec to android.jar'
  sdk = validate_source_and_sdk_locations(
      src_location, sdk_location)
  sources = os.path.join(sdk, 'sources')
  package_path = os.path.join(sdk, 'sources', 'android', 'os')
  print 'Compiling android.os.Exec to %s' % package_path
  android_os_exec = os.path.join(src_location, 'frameworks', 'base', 'core',
                                 'java', 'android', 'os', 'Exec.java')
  cmd = 'javac -d %s %s' % (sources, android_os_exec)
  if subprocess.call(cmd.split()) != 0:
    print 'Compilation failed.'
    sys.exit(1)
  android_jar = os.path.join(sdk, 'android.jar')
  exec_class = os.path.join(package_path, 'Exec.class')
  print 'Adding %s to %s' % (exec_class, android_jar)
  zip = zipfile.ZipFile(android_jar, 'a')
  zip.write(exec_class, 'android/os/Exec.class')
  zip.close()


if __name__ == '__main__':
  if len(sys.argv) == 3:
    src_location, sdk_location = sys.argv[1:3]
  else:
    print 'fix_android_sdk.py <android-source> <android-sdk>'
    sys.exit(1)
  try:
    copy_sources(src_location, sdk_location)
    add_android_os_exec(src_location, sdk_location)
  except KeyboardInterrupt:
    print '\nAborted.'
  else:
    print 'Done!'
