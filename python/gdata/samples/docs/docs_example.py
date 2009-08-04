#!/usr/bin/python
#
# Copyright (C) 2007, 2009 Google Inc.
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


__author__ = ('api.jfisher (Jeff Fisher), '
              'e.bidelman (Eric Bidelman)')


import sys
import re
import os.path
import getopt
import getpass
import gdata.docs.service
import gdata.spreadsheet.service


def truncate(content, length=15, suffix='...'):
  if len(content) <= length:
    return content
  else:
    return content[:length] + suffix


class DocsSample(object):
  """A DocsSample object demonstrates the Document List feed."""

  def __init__(self, email, password):
    """Constructor for the DocsSample object.

    Takes an email and password corresponding to a gmail account to
    demonstrate the functionality of the Document List feed.

    Args:
      email: [string] The e-mail address of the account to use for the sample.
      password: [string] The password corresponding to the account specified by
          the email parameter.

    Returns:
      A DocsSample object used to run the sample demonstrating the
      functionality of the Document List feed.
    """
    source = 'Document List Python Sample'
    self.gd_client = gdata.docs.service.DocsService()
    self.gd_client.ClientLogin(email, password, source=source)

    # Setup a spreadsheets service for downloading spreadsheets
    self.gs_client = gdata.spreadsheet.service.SpreadsheetsService()
    self.gs_client.ClientLogin(email, password, source=source)

  def _PrintFeed(self, feed):
    """Prints out the contents of a feed to the console.

    Args:
      feed: A gdata.docs.DocumentListFeed instance.
    """
    print '\n'
    if not feed.entry:
      print 'No entries in feed.\n'
    print '%-18s %-12s %s' % ('TITLE', 'TYPE', 'RESOURCE ID')
    for entry in feed.entry:
      print '%-18s %-12s %s' % (truncate(entry.title.text.encode('UTF-8')),
                                entry.GetDocumentType(),
                                entry.resourceId.text)

  def _GetFileExtension(self, file_name):
    """Returns the uppercase file extension for a file.

    Args:
      file_name: [string] The basename of a filename.

    Returns:
      A string containing the file extension of the file.
    """
    match = re.search('.*\.([a-zA-Z]{3,}$)', file_name)
    if match:
      return match.group(1).upper()
    return False

  def _UploadMenu(self):
    """Prompts that enable a user to upload a file to the Document List feed."""
    file_path = ''
    file_path = raw_input('Enter path to file: ')

    if not file_path:
      return
    elif not os.path.isfile(file_path):
      print 'Not a valid file.'
      return

    file_name = os.path.basename(file_path)
    ext = self._GetFileExtension(file_name)

    if not ext or ext not in gdata.docs.service.SUPPORTED_FILETYPES:
      print 'File type not supported. Check the file extension.'
      return
    else:
      content_type = gdata.docs.service.SUPPORTED_FILETYPES[ext]

    title = ''
    while not title:
      title = raw_input('Enter name for document: ')

    try:
      ms = gdata.MediaSource(file_path=file_path, content_type=content_type)
    except IOError:
      print 'Problems reading file. Check permissions.'
      return

    if ext in ['CSV', 'ODS', 'XLS', 'XLSX']:
      print 'Uploading spreadsheet...'
    elif ext in ['PPT', 'PPS']:
      print 'Uploading presentation...'
    else:
      print 'Uploading word processor document...'

    entry = self.gd_client.Upload(ms, title)

    if entry:
      print 'Upload successful!'
      print 'Document now accessible at:', entry.GetAlternateLink().href
    else:
      print 'Upload error.'

  def _DownloadMenu(self):
    """Prompts that enable a user to download a local copy of a document."""
    resource_id = ''
    resource_id = raw_input('Enter an resource id: ')
    file_path = ''
    file_path = raw_input('Save file to: ')

    if not file_path or not resource_id:
      return

    file_name = os.path.basename(file_path)
    ext = self._GetFileExtension(file_name)

    if not ext or ext not in gdata.docs.service.SUPPORTED_FILETYPES:
      print 'File type not supported. Check the file extension.'
      return
    else:
      content_type = gdata.docs.service.SUPPORTED_FILETYPES[ext]

    doc_type = resource_id[:resource_id.find(':')]

    # When downloading a spreadsheet, the authenticated request needs to be
    # sent with the spreadsheet service's auth token.
    if doc_type == 'spreadsheet':
      print 'Downloading spreadsheet to %s...' % (file_path,)
      docs_token = self.gd_client.GetClientLoginToken()
      self.gd_client.SetClientLoginToken(self.gs_client.GetClientLoginToken())
      self.gd_client.Export(resource_id, file_path, gid=0)
      self.gd_client.SetClientLoginToken(docs_token)
    else:
      print 'Downloading document to %s...' % (file_path,)
      self.gd_client.Export(resource_id, file_path)

  def _ListDocuments(self):
    """Retrieves and displays a list of documents based on the user's choice."""
    print 'Retrieve (all/document/folder/presentation/spreadsheet/pdf): '
    category = raw_input('Enter a category: ')

    if category == 'all':
      feed = self.gd_client.GetDocumentListFeed()
    elif category == 'folder':
      query = gdata.docs.service.DocumentQuery(categories=['folder'],
                                               params={'showfolders': 'true'})
      feed = self.gd_client.Query(query.ToUri())
    else:
      query = gdata.docs.service.DocumentQuery(categories=[category])
      feed = self.gd_client.Query(query.ToUri())

    self._PrintFeed(feed)

  def _ListAclPermissions(self):
    """Retrieves a list of a user's folders and displays them."""
    resource_id = raw_input('Enter an resource id: ')
    query = gdata.docs.service.DocumentAclQuery(resource_id)
    print '\nListing document permissions:'
    feed = self.gd_client.GetDocumentListAclFeed(query.ToUri())
    for acl_entry in feed.entry:
      print '%s - %s (%s)' % (acl_entry.role.value, acl_entry.scope.value,
                              acl_entry.scope.type)

  def _ModifyAclPermissions(self):
    """Create or updates the ACL entry on an existing document."""
    resource_id = raw_input('Enter an resource id: ')
    email = raw_input('Enter an email address: ')
    role_value = raw_input('Enter a permission (reader/writer/owner/remove): ')

    uri = gdata.docs.service.DocumentAclQuery(resource_id).ToUri()
    acl_feed = self.gd_client.GetDocumentListAclFeed(uri)

    found_acl_entry = None
    for acl_entry in acl_feed.entry:
      if acl_entry.scope.value == email:
        found_acl_entry = acl_entry
        break

    if found_acl_entry:
      if role_value == 'remove':
        # delete ACL entry
        self.gd_client.Delete(found_acl_entry.GetEditLink().href)
      else:
        # update ACL entry
        found_acl_entry.role.value = role_value
        updated_entry = self.gd_client.Put(
            found_acl_entry, found_acl_entry.GetEditLink().href,
            converter=gdata.docs.DocumentListAclEntryFromString)
    else:
      scope = gdata.docs.Scope(value=email, type='user')
      role = gdata.docs.Role(value=role_value)
      acl_entry = gdata.docs.DocumentListAclEntry(scope=scope, role=role)
      inserted_entry = self.gd_client.Post(
        acl_entry, uri, converter=gdata.docs.DocumentListAclEntryFromString)

    print '\nListing document permissions:'
    acl_feed = self.gd_client.GetDocumentListAclFeed(uri)
    for acl_entry in acl_feed.entry:
      print '%s - %s (%s)' % (acl_entry.role.value, acl_entry.scope.value,
                              acl_entry.scope.type)

  def _FullTextSearch(self):
    """Searches a user's documents for a text string.

    Provides prompts to search a user's documents and displays the results
    of such a search. The text_query parameter of the DocumentListQuery object
    corresponds to the contents of the q parameter in the feed. Note that this
    parameter searches the content of documents, not just their titles.
    """
    input = raw_input('Enter search term: ')
    query = gdata.docs.service.DocumentQuery(text_query=input)
    feed = self.gd_client.Query(query.ToUri())
    self._PrintFeed(feed)

  def _PrintMenu(self):
    """Displays a menu of options for the user to choose from."""
    print ('\nDocument List Sample\n'
           '1) List your documents.\n'
           '2) Search your documents.\n'
           '3) Upload a document.\n'
           '4) Download a document.\n'
           "5) List a document's permissions.\n"
           "6) Add/change a document's permissions.\n"
           '7) Exit.\n')

  def _GetMenuChoice(self, max):
    """Retrieves the menu selection from the user.

    Args:
      max: [int] The maximum number of allowed choices (inclusive)

    Returns:
      The integer of the menu item chosen by the user.
    """
    while True:
      input = raw_input('> ')

      try:
        num = int(input)
      except ValueError:
        print 'Invalid choice. Please choose a value between 1 and', max
        continue

      if num > max or num < 1:
        print 'Invalid choice. Please choose a value between 1 and', max
      else:
        return num

  def Run(self):
    """Prompts the user to choose funtionality to be demonstrated."""
    try:
      while True:
        self._PrintMenu()
        choice = self._GetMenuChoice(7)

        if choice == 1:
          self._ListDocuments()
        elif choice == 2:
          self._FullTextSearch()
        elif choice == 3:
          self._UploadMenu()
        elif choice == 4:
          self._DownloadMenu()
        elif choice == 5:
          self._ListAclPermissions()
        elif choice == 6:
          self._ModifyAclPermissions()
        elif choice == 7:
          print '\nGoodbye.'
          return
    except KeyboardInterrupt:
      print '\nGoodbye.'
      return


def main():
  """Demonstrates use of the Docs extension using the DocsSample object."""
  # Parse command line options
  try:
    opts, args = getopt.getopt(sys.argv[1:], '', ['user=', 'pw='])
  except getopt.error, msg:
    print 'python docs_example.py --user [username] --pw [password] '
    sys.exit(2)

  user = ''
  pw = ''
  key = ''
  # Process options
  for option, arg in opts:
    if option == '--user':
      user = arg
    elif option == '--pw':
      pw = arg

  while not user:
    print 'NOTE: Please run these tests only with a test account.'
    user = raw_input('Please enter your username: ')
  while not pw:
    pw = getpass.getpass()
    if not pw:
      print 'Password cannot be blank.'

  try:
    sample = DocsSample(user, pw)
  except gdata.service.BadAuthentication:
    print 'Invalid user credentials given.'
    return

  sample.Run()


if __name__ == '__main__':
  main()
