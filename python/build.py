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

import compileall
import glob
import os
import re
import subprocess
import shutil
import sys
import zipfile


def run(cmd, exit=True, cwd=None):
  print cmd
  if subprocess.Popen(cmd.split(), cwd=cwd).wait() != 0:
    if exit:
      print 'Failed!'
      sys.exit(1)
    else:
      print 'Ignoring failure.'


def find(directory, pattern=None, exclude=None):
  print 'Looking for paths in %r matching %r' % (directory, pattern)
  matches = []
  misses = []
  if exclude is None:
    exclude = []
  directory = os.path.abspath(directory)
  for root, dirs, files in os.walk(directory):
    for basename in dirs + files:
      if basename in exclude:
        if basename in dirs:
          dirs.remove(basename)
        continue
      path = os.path.join(root, basename)
      if pattern is None or re.search(pattern, path):
        matches.append(path)
      else:
        misses.append(path)
  print 'Found %d matches and %d misses' % (len(matches), len(misses))
  return matches, misses


def rm(path):
  print 'Deleting %r' % path
  try:
    if os.path.isdir(path):
      shutil.rmtree(path)
    else:
      os.remove(path)
  except OSError:
    pass


def strip(path):
  run('arm-eabi-strip %s' % path)


def zipup(out_path, in_path, top, exclude=None, prefix=''):
  zip_file = zipfile.ZipFile(out_path, 'w', compression=zipfile.ZIP_DEFLATED)
  for path in find(in_path, exclude=exclude)[0]:
    if not os.path.isdir(path):
      arcname = prefix + path[len(top):].lstrip('/')
      print 'Adding %s to %s' % (arcname, out_path)
      zip_file.write(path, arcname)
  zip_file.close()


# Find Android source path and put it in the environment.
gcc_path = subprocess.Popen(['which', 'arm-eabi-gcc'],
                            stdout=subprocess.PIPE).communicate()[0]
match = re.match(r'(.*)/prebuilt', gcc_path)
if match is None:
  print 'Could not find arm-eabi-gcc on your path.'
  sys.exit(1)
android_src = match.group(1)
os.environ['ANDROID_SRC'] = android_src

agcc_path = subprocess.Popen(['which', 'agcc'],
                             stdout=subprocess.PIPE).communicate()[0]
if agcc_path == '':
  print 'Could not find agcc on your path.'
  sys.exit(1)

pwd = os.getcwd()
os.chdir('src')

assert os.path.exists('Parser/hostpgen'), 'hostpgen not found'

run('make')
#run('make install -k', False)
run('make install')

assert os.path.exists('android'), 'build result not found'

print 'Installing xmppy.'
xmpppy_path = os.path.join(pwd, 'xmpppy', 'xmpp')
compileall.compile_dir(xmpppy_path)
shutil.copytree(xmpppy_path, 'android/python/lib/python2.6/xmpp')

print 'Installing BeautifulSoup.'
beautifulsoup_path = os.path.join(pwd, 'BeautifulSoup')
compileall.compile_dir(beautifulsoup_path)
shutil.copy(os.path.join(beautifulsoup_path, 'BeautifulSoup.pyc'),
            'android/python/lib/python2.6/BeautifulSoup.pyc')

print 'Installing gdata.'
gdata_path = os.path.join(pwd, 'gdata')
run('python setup.py build', cwd=gdata_path)
gdata_build_path = os.path.join(gdata_path, 'build')
gdata_result_path = os.path.join(gdata_build_path,
                                 os.listdir(gdata_build_path)[0])
compileall.compile_dir(gdata_result_path)
shutil.copytree(os.path.join(gdata_result_path, 'gdata'),
                'android/python/lib/python2.6/gdata')
shutil.copytree(os.path.join(gdata_result_path, 'atom'),
                'android/python/lib/python2.6/atom')

print 'Installing python-twitter.'
twitter_path = os.path.join(pwd, 'python-twitter')
compileall.compile_dir(twitter_path)
shutil.copy(os.path.join(twitter_path, 'twitter.pyc'),
            'android/python/lib/python2.6/twitter.pyc')

print 'Installing simplejson.'
simplejson_path = os.path.join(pwd, 'python-twitter', 'simplejson')
compileall.compile_dir(simplejson_path)
shutil.copytree(simplejson_path, 'android/python/lib/python2.6/simplejson')

print 'Removing unecessary files and directories from installation.'
map(rm, find('android/python/bin', 'python$')[1])
map(rm, find('android', '\.py$')[0])
map(rm, find('android', '\.c$')[0])
map(rm, find('android', 'test')[0])
map(rm, find('android', '\.pyo$')[0])

rm('android/python/share')
rm('android/python/include')
rm('android/python/lib/libpython2.6.a')

map(strip, find('android', '\.so$')[0])
strip('android/python/bin/python')

libs_to_remove = [
    'compiler',
    'config',
    'curses',
    'distutils',
    'hotshot',
    'idlelib',
    'lib2to3',
    'lib-old',
    'lib-tk',
    'multiprocessing',
    'site-packages',
    ]
for lib in libs_to_remove:
  rm('android/python/lib/python2.6/'+lib)

# Remove any existing zip files.
for p in glob.glob(os.path.join(pwd, '*.zip')):
  rm(p)

print 'Zipping up standard library.'
libs = os.path.join(pwd, 'src/android/python/lib/python2.6')
# Copy in ASE's Android module.
shutil.copy(os.path.join(pwd, 'ase', 'android.py'),
            'android/python/lib/python2.6')
zipup(os.path.join(pwd, 'python_extras.zip'), libs, libs,
      exclude=['lib-dynload'], prefix='python/')
map(rm, find(libs, exclude=['lib-dynload'])[0])

print 'Zipping up Python interpreter for deployment.'
zipup(os.path.join(pwd, 'python.zip'),
      os.path.join(pwd, 'src', 'android', 'python'),
      os.path.join(pwd, 'src', 'android'))

print 'Zipping up Python scripts.'
zipup(os.path.join(pwd, 'python_scripts.zip'),
      os.path.join(pwd, 'ase', 'scripts'),
      os.path.join(pwd, 'ase', 'scripts'))

print 'Done.'
