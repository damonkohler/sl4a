#!/usr/bin/python

import googlecode_upload
import optparse
import os
import sys

PROJECT = 'android-scripting'


def upload(path, labels, options):
  if options.dryrun:
    return
  summary = os.path.basename(path)
  status, reason, url = googlecode_upload.upload_find_auth(
      path, PROJECT, summary, labels, options.user, options.password)


def upload_language(name, version, options):
  for archive in ('', '_extras', '_scripts'):
    basename = '%s%s_r%d.zip' % (name, archive, version)
    path = os.path.join(options.bindir, basename)
    if os.path.exists(path):
      print 'Uploading %s.' % path
      upload(path, (), options)
    else:
      print 'No archive %s.' % path


def main():
  parser = optparse.OptionParser(usage='googlecode-upload.py -s SUMMARY '
                                 '-p PROJECT [options] FILE')
  parser.add_option('-u', '--user', dest='user',
                    help='Your Google Code username')
  parser.add_option('-w', '--password', dest='password',
                    help='Your Google Code password')
  parser.add_option('-b', '--bindir', dest='bindir',
                    help='The binary directory')
  parser.add_option('-d', '--dryrun', action='store_true', dest='dryrun',
                    help='The binary directory')

  options, args = parser.parse_args()

  if options.user is None:
    parser.error('Username is missing.')
  if options.password is None:
    parser.error('Password is missing.')
  if options.bindir is None:
    parser.error('Bindir is missing.')

  if len(args) < 2:
    parser.error('Must specify language and version to upload.')

  upload_language(args[0], int(args[1]), options)


if __name__ == '__main__':
  sys.exit(main())
