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

VERSION='_'+ sys.argv[1] if len(sys.argv)>1 else ''

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


pwd = os.getcwd()

print 'Installing xmppy.'
xmpppy_path = os.path.join(pwd, 'python-libs', 'xmpppy', 'xmpp')
compileall.compile_dir(xmpppy_path)
shutil.copytree(xmpppy_path, 'output/usr/lib/python2.6/xmpp')

print 'Installing BeautifulSoup.'
beautifulsoup_path = os.path.join(pwd, 'python-libs','BeautifulSoup')
compileall.compile_dir(beautifulsoup_path)
shutil.copy(os.path.join(beautifulsoup_path, 'BeautifulSoup.pyc'),
            'output/usr/lib/python2.6/BeautifulSoup.pyc')

print 'Installing gdata.'
gdata_path = os.path.join(pwd, 'python-libs', 'gdata')
run('python setup.py build', cwd=gdata_path)
gdata_build_path = os.path.join(gdata_path, 'build')
gdata_result_path = os.path.join(gdata_build_path,
                                 os.listdir(gdata_build_path)[0])
compileall.compile_dir(gdata_result_path)
shutil.copytree(os.path.join(gdata_result_path, 'gdata'),
                'output/usr/lib/python2.6/gdata')
shutil.copytree(os.path.join(gdata_result_path, 'atom'),
                'output/usr/lib/python2.6/atom')

print 'Installing python-twitter.'
twitter_path = os.path.join(pwd, 'python-libs', 'python-twitter')
compileall.compile_dir(twitter_path)
shutil.copy(os.path.join(twitter_path, 'twitter.pyc'),
            'output/usr/lib/python2.6/twitter.pyc')

print 'Installing simplejson.'
simplejson_path = os.path.join(pwd, 'python-libs', 'python-twitter', 'simplejson')
compileall.compile_dir(simplejson_path)
shutil.copytree(simplejson_path, 'output/usr/lib/python2.6/simplejson')

# Remove any existing zip files.
for p in glob.glob(os.path.join(pwd, '*.zip')):
  rm(p)

print 'Zipping up Python Libs for deployment.'
output=os.path.join(pwd, 'output')
shutil.copytree(output, 'output.temp')
map(rm, find('output.temp', '\.py$')[0])
map(rm, find('output.temp', '\.pyc$')[0])
map(rm, find('output.temp', 'python$')[0])
rm('output.temp/usr/lib/python2.6')
zipup(os.path.join(pwd, 'python-lib%s.zip' % VERSION),
      os.path.join(pwd, 'output.temp', 'usr'),
      os.path.join(pwd, 'output.temp', 'usr'))
rm('output.temp')

print 'Removing unecessary files and directories from installation.'
map(rm, find('output', '\.py$')[0])
map(rm, find('output', '\.c$')[0])
map(rm, find('output', '\.pyo$')[0])

rm('output/usr/share')
rm('output/usr/include')

map(strip, find('output', '\.so$')[0])
strip('output/usr/bin/python')

print 'Zipping up standard library.'
libs = os.path.join(pwd, 'output/usr/lib/python2.6')
# Copy in ASE's Android module.
shutil.copy(os.path.join(pwd, 'python-libs', 'ase', 'android.py'),
            'output/usr/lib/python2.6')
zipup(os.path.join(pwd, 'python_extras%s.zip' % VERSION), libs, libs,
      exclude=['lib-dynload'], prefix='python/')

map(rm, find('output', '\.py$')[0])
map(rm, find('output', '\.pyc$')[0])
map(rm, find('output', '\.doc$')[0])
map(rm, find('output', '\.egg-info$')[0])
def clean_library(lib):
    rm(os.path.join(pwd, 'output', 'usr', 'lib', 'python2.6', lib))
map (clean_library, ['ctypes', 'distutils', 'idlelib', 'plat-linux2', 'site-packages'])

print 'Zipping up Python interpreter for deployment.'
zipup(os.path.join(pwd, 'python%s.zip' % VERSION),
      os.path.join(pwd, 'output', 'usr'),
      os.path.join(pwd, 'output', 'usr'),
      exclude=['*.pyc',  '*.py'], prefix="python/")

print 'Zipping up Python scripts.'
zipup(os.path.join(pwd, 'python_scripts%s.zip' % VERSION),
      os.path.join(pwd, 'python-libs', 'ase', 'scripts'),
      os.path.join(pwd, 'python-libs', 'ase', 'scripts'))

print 'Done.'
