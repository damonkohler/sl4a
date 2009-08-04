#!/usr/bin/python
#
# Copyright (C) 2007 Google Inc.
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#      http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.

# This file demonstrates how to use the Google Data API's Python client library
# to interface with the Codesearch service.


__author__ = 'vbarathan@gmail.com (Prakash Barathan)'


from gdata import service
import gdata.codesearch.service
import gdata
import atom
import getopt
import sys


class CodesearchExample:

  def __init__(self):
    """Creates a GData service instance to talk to Codesearch service."""
    self.service = gdata.codesearch.service.CodesearchService(
        source='Codesearch_Python_Sample-1.0')

  def PrintCodeSnippets(self, query):
    """Prints the codesearch results for given query."""
    feed = self.service.GetSnippetsFeed(query)
    print feed.title.text + " Results for '" +  query + "'"
    print '============================================'
    for entry in feed.entry:
      print "" + entry.title.text
      for match in entry.match:
        print "\tline#" + match.line_number + ":" + match.text.replace('\n', '')
    print


def main():
  """The main function runs the CodesearchExample application with user
  specified query."""

  # parse command line options
  try:
    opts, args = getopt.getopt(sys.argv[1:], "", ["query="])
  except getopt.error, msg:
    print ('python CodesearchExample.py --query [query_text]')
    sys.exit(2)

  query = ''

  # Process options
  for o, a in opts:
    if o == "--query":
      query = a

  if query == '':
    print ('python CodesearchExample.py --query [query]')
    sys.exit(2)

  sample = CodesearchExample()
  sample.PrintCodeSnippets(query)


if __name__ == '__main__':
  main()
