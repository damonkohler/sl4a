#!/usr/bin/python
#
# Copyright (C) 2006 Google Inc.
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

__author__ = 'api.jscudder (Jeff Scudder)'

import unittest
import getpass
try:
  from xml.etree import ElementTree
except ImportError:
  from elementtree import ElementTree
import gdata.service
import gdata
import gdata.auth
import atom
import atom.service
import atom.token_store
import gdata.base
import os.path
from gdata import test_data
import atom.mock_http
import atom.mock_http_core


username = ''
password = ''
test_image_location = '../testimage.jpg'
test_image_name = 'testimage.jpg'


class GDataServiceMediaUnitTest(unittest.TestCase):

  def setUp(self):
    self.gd_client = gdata.service.GDataService()
    self.gd_client.email = username
    self.gd_client.password = password
    self.gd_client.service = 'lh2'
    self.gd_client.source = 'GDataService Media "Unit" Tests'
    try:
      self.gd_client.ProgrammaticLogin()
    except gdata.service.CaptchaRequired:
      self.fail('Required Captcha')
    except gdata.service.BadAuthentication:
      self.fail('Bad Authentication')
    except gdata.service.Error:
      self.fail('Login Error')
    
    # create a test album
    gd_entry = gdata.GDataEntry()
    gd_entry.title = atom.Title(text='GData Test Album')
    gd_entry.category.append(atom.Category(
        scheme='http://schemas.google.com/g/2005#kind',
        term='http://schemas.google.com/photos/2007#album'))
    
    self.album_entry = self.gd_client.Post(gd_entry, 
        'http://picasaweb.google.com/data/feed/api/user/' + username)
    
  def tearDown(self):
    album_entry = self.gd_client.Get(self.album_entry.id.text)
    self.gd_client.Delete(album_entry.GetEditLink().href)

  def testSourceGeneratesUserAgentHeader(self):
    self.gd_client.source = 'GoogleInc-ServiceUnitTest-1'
    self.assert_(self.gd_client.additional_headers['User-Agent'].startswith(
        'GoogleInc-ServiceUnitTest-1 GData-Python'))
    
  def testMedia1(self):
    # Create media-only
    ms = gdata.MediaSource()
    ms.setFile(test_image_location, 'image/jpeg')
    media_entry = self.gd_client.Post(None, 
        self.album_entry.GetFeedLink().href, media_source = ms)
    self.assert_(media_entry is not None)
    self.assert_(isinstance(media_entry, gdata.GDataEntry))
    self.assert_(media_entry.IsMedia())
    
    # Update media & metadata
    ms = gdata.MediaSource()
    ms.setFile(test_image_location, 'image/jpeg')
    media_entry.summary = atom.Summary(text='Test Image')
    media_entry2 = self.gd_client.Put(media_entry,
        media_entry.GetEditLink().href, media_source = ms)
    self.assert_(media_entry2 is not None)
    self.assert_(isinstance(media_entry2, gdata.GDataEntry))
    self.assert_(media_entry2.IsMedia())
    self.assert_(media_entry2.summary.text == 'Test Image')
    
    # Read media binary
    imageSource = self.gd_client.GetMedia(media_entry2.GetMediaURL())
    self.assert_(isinstance(imageSource, gdata.MediaSource))
    self.assert_(imageSource.content_type == 'image/jpeg')
    self.assert_(imageSource.content_length)
    
    imageData = imageSource.file_handle.read()
    self.assert_(imageData)
    
    # Delete entry
    response = self.gd_client.Delete(media_entry2.GetEditLink().href)
    self.assert_(response)
    
  def testMedia2(self):
    # Create media & metadata
    ms = gdata.MediaSource()
    ms.setFile(test_image_location, 'image/jpeg')
    new_media_entry = gdata.GDataEntry()
    new_media_entry.title = atom.Title(text='testimage1.jpg')
    new_media_entry.summary = atom.Summary(text='Test Image')
    new_media_entry.category.append(atom.Category(scheme = 
        'http://schemas.google.com/g/2005#kind', term = 
        'http://schemas.google.com/photos/2007#photo'))
    media_entry = self.gd_client.Post(new_media_entry,
        self.album_entry.GetFeedLink().href, media_source = ms)
    self.assert_(media_entry is not None)
    self.assert_(isinstance(media_entry, gdata.GDataEntry))
    self.assert_(media_entry.IsMedia())
    self.assert_(media_entry.summary.text == 'Test Image')
    
    # Update media only
    ms = gdata.MediaSource()
    ms.setFile(test_image_location, 'image/jpeg')
    media_entry = self.gd_client.Put(None, media_entry.GetEditMediaLink().href,
        media_source = ms)
    self.assert_(media_entry is not None)
    self.assert_(isinstance(media_entry, gdata.GDataEntry))
    self.assert_(media_entry.IsMedia())
    
    # Delete entry
    response = self.gd_client.Delete(media_entry.GetEditLink().href)
    self.assert_(response)

  def testMediaConstructorDefaults(self):

    ms = gdata.MediaSource()
    ms.setFile(test_image_location, 'image/jpeg')
    
    self.assert_(ms is not None)
    self.assert_(isinstance(ms, gdata.MediaSource))
    self.assertEquals(ms.file_name, test_image_name)
    self.assertEquals(ms.content_type, 'image/jpeg')
  
  def testMediaConstructorWithFilePath(self):

    ms = gdata.MediaSource(file_path=test_image_location,
                           content_type='image/jpeg')
    
    self.assert_(ms is not None)
    self.assert_(isinstance(ms, gdata.MediaSource))
    self.assertEquals(ms.file_name, test_image_name)
    self.assertEquals(ms.content_type, 'image/jpeg')
   
  def testMediaConstructorWithFileHandle(self):

    fh = open(test_image_location, 'r')
    len = os.path.getsize(test_image_location)
    ms = gdata.MediaSource(fh, 'image/jpeg', len, file_name=test_image_location)
    
    self.assert_(ms is not None)
    self.assert_(isinstance(ms, gdata.MediaSource))
    self.assertEquals(ms.file_name, test_image_location)
    self.assertEquals(ms.content_type, 'image/jpeg')


class GDataServiceUnitTest(unittest.TestCase):
  
  def setUp(self):
    self.gd_client = gdata.service.GDataService()
    self.gd_client.email = username
    self.gd_client.password = password
    self.gd_client.service = 'gbase'
    self.gd_client.source = 'GDataClient "Unit" Tests'

  def testProperties(self):
    email_string = 'Test Email'
    password_string = 'Passwd'

    self.gd_client.email = email_string
    self.assertEquals(self.gd_client.email, email_string)
    self.gd_client.password = password_string
    self.assertEquals(self.gd_client.password, password_string)

  def testCorrectLogin(self):
    try:
      self.gd_client.ProgrammaticLogin()
      self.assert_(isinstance(
          self.gd_client.token_store.find_token(
              'http://base.google.com/base/feeds/'),
          gdata.auth.ClientLoginToken))
      self.assert_(self.gd_client.captcha_token is None)
      self.assert_(self.gd_client.captcha_url is None)
    except gdata.service.CaptchaRequired:
      self.fail('Required Captcha')

  def testDefaultHttpClient(self):
    self.assert_(isinstance(self.gd_client.http_client, 
                            atom.http.HttpClient))


  def testGet(self):
    try:
      self.gd_client.ProgrammaticLogin()
    except gdata.service.CaptchaRequired:
      self.fail('Required Captcha')
    except gdata.service.BadAuthentication:
      self.fail('Bad Authentication')
    except gdata.service.Error:
      self.fail('Login Error')
    self.gd_client.additional_headers = {'X-Google-Key': 
                                               'ABQIAAAAoLioN3buSs9KqIIq9V' +
                                               'mkFxT2yXp_ZAY8_ufC3CFXhHIE' +
                                               '1NvwkxRK8C1Q8OWhsWA2AIKv-c' +
                                               'VKlVrNhQ'}
    self.gd_client.server = 'base.google.com'
    result = self.gd_client.Get('/base/feeds/snippets?bq=digital+camera')
    self.assert_(result is not None)
    self.assert_(isinstance(result, atom.Feed))

  def testGetWithAuthentication(self):
    try:
      self.gd_client.ProgrammaticLogin()
    except gdata.service.CaptchaRequired:
      self.fail('Required Captcha')
    except gdata.service.BadAuthentication:
      self.fail('Bad Authentication')
    except gdata.service.Error:
      self.fail('Login Error')
    self.gd_client.additional_headers = {'X-Google-Key':
                                               'ABQIAAAAoLioN3buSs9KqIIq9V' +
                                               'mkFxT2yXp_ZAY8_ufC3CFXhHIE' +
                                               '1NvwkxRK8C1Q8OWhsWA2AIKv-c' +
                                               'VKlVrNhQ'}
    self.gd_client.server = 'base.google.com'
    result = self.gd_client.Get('/base/feeds/items?bq=digital+camera')
    self.assert_(result is not None)
    self.assert_(isinstance(result, atom.Feed))

  def testGetEntry(self):
    try:
      self.gd_client.ProgrammaticLogin()
    except gdata.service.CaptchaRequired:
      self.fail('Required Captcha')
    except gdata.service.BadAuthentication:
      self.fail('Bad Authentication')
    except gdata.service.Error:
      self.fail('Login Error')
    self.gd_client.server = 'base.google.com'
    try:
      result = self.gd_client.GetEntry('/base/feeds/items?bq=digital+camera')
      self.fail(
          'Result from server in GetEntry should have raised an exception')
    except gdata.service.UnexpectedReturnType:
      pass

  def testGetFeed(self):
    try:
      self.gd_client.ProgrammaticLogin()
    except gdata.service.CaptchaRequired:
      self.fail('Required Captcha')
    except gdata.service.BadAuthentication:
      self.fail('Bad Authentication')
    except gdata.service.Error:
      self.fail('Login Error')
    self.gd_client.server = 'base.google.com'
    result = self.gd_client.GetFeed('/base/feeds/items?bq=digital+camera')
    self.assert_(result is not None)
    self.assert_(isinstance(result, atom.Feed))

  def testGetWithResponseTransformer(self):
    # Query Google Base and interpret the results as a GBaseSnippetFeed.
    feed = self.gd_client.Get(
        'http://www.google.com/base/feeds/snippets?bq=digital+camera',
        converter=gdata.base.GBaseSnippetFeedFromString)
    self.assertEquals(isinstance(feed, gdata.base.GBaseSnippetFeed), True)

  def testPostPutAndDelete(self):
    try:
      self.gd_client.ProgrammaticLogin()
    except gdata.service.CaptchaRequired:
      self.fail('Required Captcha')
    except gdata.service.BadAuthentication:
      self.fail('Bad Authentication')
    except gdata.service.Error:
      self.fail('Login Error')
    self.gd_client.additional_headers = {'X-Google-Key':
                                               'ABQIAAAAoLioN3buSs9KqIIq9V' +
                                               'mkFxT2yXp_ZAY8_ufC3CFXhHIE' +
                                               '1NvwkxRK8C1Q8OWhsWA2AIKv-c' +
                                               'VKlVrNhQ'}
    self.gd_client.server = 'base.google.com'

    # Insert a new item
    response = self.gd_client.Post(test_data.TEST_BASE_ENTRY, 
        '/base/feeds/items')
    self.assert_(response is not None)
    self.assert_(isinstance(response, atom.Entry))
    self.assert_(response.category[0].term == 'products')

    # Find the item id of the created item
    item_id = response.id.text.lstrip(
        'http://www.google.com/base/feeds/items/')
    self.assert_(item_id is not None)
    
    updated_xml = gdata.base.GBaseItemFromString(test_data.TEST_BASE_ENTRY)
    # Change one of the labels in the item
    updated_xml.label[2].text = 'beach ball'
    # Update the item
    response = self.gd_client.Put(updated_xml, 
        '/base/feeds/items/%s' % item_id)
    self.assert_(response is not None)
    new_base_item = gdata.base.GBaseItemFromString(str(response))
    self.assert_(isinstance(new_base_item, atom.Entry))
    
    # Delete the item the test just created.
    response = self.gd_client.Delete('/base/feeds/items/%s' % item_id)
    self.assert_(response)

  def testPostPutAndDeleteWithConverters(self):
    try:
      self.gd_client.ProgrammaticLogin()
    except gdata.service.CaptchaRequired:
      self.fail('Required Captcha')
    except gdata.service.BadAuthentication:
      self.fail('Bad Authentication')
    except gdata.service.Error:
      self.fail('Login Error')
    self.gd_client.additional_headers = {'X-Google-Key':
                                               'ABQIAAAAoLioN3buSs9KqIIq9V' +
                                               'mkFxT2yXp_ZAY8_ufC3CFXhHIE' +
                                               '1NvwkxRK8C1Q8OWhsWA2AIKv-c' +
                                               'VKlVrNhQ'}
    self.gd_client.server = 'base.google.com'

    # Insert a new item
    response = self.gd_client.Post(test_data.TEST_BASE_ENTRY,
        '/base/feeds/items', converter=gdata.base.GBaseItemFromString)
    self.assert_(response is not None)
    self.assert_(isinstance(response, atom.Entry))
    self.assert_(isinstance(response, gdata.base.GBaseItem))
    self.assert_(response.category[0].term == 'products')

    updated_xml = gdata.base.GBaseItemFromString(test_data.TEST_BASE_ENTRY)
    # Change one of the labels in the item
    updated_xml.label[2].text = 'beach ball'
    # Update the item
    response = self.gd_client.Put(updated_xml,
        response.id.text,
        converter=gdata.base.GBaseItemFromString)
    self.assertEquals(response is not None, True)
    self.assertEquals(isinstance(response, gdata.base.GBaseItem), True)

    # Delete the item the test just created.
    response = self.gd_client.Delete(response.id.text)
    self.assert_(response)

  def testCaptchaUrlGeneration(self):
    # Populate the mock server with a pairing for a ClientLogin request to a
    # CAPTCHA challenge.
    mock_client = atom.mock_http.MockHttpClient()
    captcha_response = atom.mock_http.MockResponse(
        body="""Url=http://www.google.com/login/captcha
Error=CaptchaRequired
CaptchaToken=DQAAAGgAdkI1LK9
CaptchaUrl=Captcha?ctoken=HiteT4b0Bk5Xg18_AcVoP6-yFkHPibe7O9EqxeiI7lUSN
""", status=403, reason='Access Forbidden')
    mock_client.add_response(captcha_response, 'POST', 
        'https://www.google.com/accounts/ClientLogin')

    # Set the exising client's handler so that it will make requests to the
    # mock service instead of the real server.
    self.gd_client.http_client = mock_client

    try:
      self.gd_client.ProgrammaticLogin()
      self.fail('Login attempt should have caused a CAPTCHA challenge.')
    except gdata.service.CaptchaRequired, error:
      self.assertEquals(self.gd_client.captcha_url, 
          ('https://www.google.com/accounts/Captcha?ctoken=HiteT4b0Bk5Xg18_'
           'AcVoP6-yFkHPibe7O9EqxeiI7lUSN'))


class DeleteWithUrlParamsTest(unittest.TestCase):

  def setUp(self):
    self.gd_client = gdata.service.GDataService()
    # Set the client to echo the request back in the response.
    self.gd_client.http_client.v2_http_client = (
        atom.mock_http_core.SettableHttpClient(200, 'OK', '', {}))
    
  def testDeleteWithUrlParams(self):
    self.assertTrue(self.gd_client.Delete('http://example.com/test', 
        {'TestHeader': '123'}, {'urlParam1': 'a', 'urlParam2': 'test'}))
    request = self.gd_client.http_client.v2_http_client.last_request
    self.assertEqual(request.uri.host, 'example.com')
    self.assertEqual(request.uri.path, '/test')
    self.assertEqual(request.uri.query, 
        {'urlParam1': 'a', 'urlParam2': 'test'})

  def testDeleteWithSessionId(self):
    self.gd_client._SetSessionId('test_session_id')
    self.assertTrue(self.gd_client.Delete('http://example.com/test', 
        {'TestHeader': '123'}, {'urlParam1': 'a', 'urlParam2': 'test'}))
    request = self.gd_client.http_client.v2_http_client.last_request
    self.assertEqual(request.uri.host, 'example.com')
    self.assertEqual(request.uri.path, '/test')
    self.assertEqual(request.uri.query, {'urlParam1': 'a', 
        'urlParam2': 'test', 'gsessionid': 'test_session_id'})
      

class QueryTest(unittest.TestCase):

  def setUp(self):
    self.query = gdata.service.Query()

  def testQueryShouldBehaveLikeDict(self):
    try:
      self.query['zap']
      self.fail()
    except KeyError:
      pass
    self.query['zap'] = 'x'
    self.assert_(self.query['zap'] == 'x')

  def testContructorShouldRejectBadInputs(self):
    test_q = gdata.service.Query(params=[1,2,3,4])
    self.assert_(len(test_q.keys()) == 0)

  def testTextQueryProperty(self):
    self.assert_(self.query.text_query is None)
    self.query['q'] = 'test1'
    self.assert_(self.query.text_query == 'test1')
    self.query.text_query = 'test2'
    self.assert_(self.query.text_query == 'test2')

  def testOrderByQueryProperty(self): 
    self.assert_(self.query.orderby is None) 
    self.query['orderby'] = 'updated' 
    self.assert_(self.query.orderby == 'updated') 
    self.query.orderby = 'starttime' 
    self.assert_(self.query.orderby == 'starttime') 

  def testQueryShouldProduceExampleUris(self):
    self.query.feed = '/base/feeds/snippets'
    self.query.text_query = 'This is a test'
    self.assert_(self.query.ToUri() == '/base/feeds/snippets?q=This+is+a+test')

  def testCategoriesFormattedCorrectly(self):
    self.query.feed = '/x'
    self.query.categories.append('Fritz')
    self.query.categories.append('Laurie')
    self.assert_(self.query.ToUri() == '/x/-/Fritz/Laurie')
    # The query's feed should not have been changed
    self.assert_(self.query.feed == '/x')
    self.assert_(self.query.ToUri() == '/x/-/Fritz/Laurie')

  def testCategoryQueriesShouldEscapeOrSymbols(self):
    self.query.feed = '/x'
    self.query.categories.append('Fritz|Laurie')
    self.assert_(self.query.ToUri() == '/x/-/Fritz%7CLaurie')

  def testTypeCoercionOnIntParams(self):
    self.query.feed = '/x'
    self.query.max_results = 10
    self.query.start_index = 5
    self.assert_(isinstance(self.query.max_results, str))
    self.assert_(isinstance(self.query.start_index, str))
    self.assertEquals(self.query['max-results'], '10')
    self.assertEquals(self.query['start-index'], '5')

  def testPassInCategoryListToConstructor(self):
    query = gdata.service.Query(feed='/feed/sample', categories=['foo', 'bar',
            'eggs|spam'])
    url = query.ToUri()
    self.assert_(url.find('/foo') > -1)
    self.assert_(url.find('/bar') > -1)
    self.assert_(url.find('/eggs%7Cspam') > -1)


class GetNextPageInFeedTest(unittest.TestCase):

  def setUp(self):
    self.gd_client = gdata.service.GDataService()

  def testGetNextPage(self):
    feed = self.gd_client.Get(
        'http://www.google.com/base/feeds/snippets?max-results=2',
        converter=gdata.base.GBaseSnippetFeedFromString)
    self.assert_(len(feed.entry) > 0)
    first_id = feed.entry[0].id.text
    feed2 = self.gd_client.GetNext(feed)
    self.assert_(len(feed2.entry) > 0)
    next_id = feed2.entry[0].id.text
    self.assert_(first_id != next_id)
    self.assert_(feed2.__class__ == feed.__class__)


class ScopeLookupTest(unittest.TestCase):

  def testLookupScopes(self):
    scopes = gdata.service.lookup_scopes('cl')
    self.assertEquals(scopes, gdata.service.CLIENT_LOGIN_SCOPES['cl'])
    scopes = gdata.service.lookup_scopes(None)
    self.assert_(scopes is None)
    scopes = gdata.service.lookup_scopes('UNKNOWN_SERVICE')
    self.assert_(scopes is None)


class TokenLookupTest(unittest.TestCase):

  def setUp(self):
    self.client = gdata.service.GDataService()

  def testSetAndGetClientLoginTokenWithNoService(self):
    self.assert_(self.client.auth_token is None)
    self.client.SetClientLoginToken('foo')
    self.assert_(self.client.auth_token is None)
    self.assert_(self.client.token_store.find_token(
        atom.token_store.SCOPE_ALL) is not None)
    self.assertEquals(self.client.GetClientLoginToken(), 'foo')
    self.client.SetClientLoginToken('foo2')
    self.assertEquals(self.client.GetClientLoginToken(), 'foo2')

  def testSetAndGetClientLoginTokenWithService(self):
    self.client.service = 'cp'
    self.client.SetClientLoginToken('bar')
    self.assertEquals(self.client.GetClientLoginToken(), 'bar')
    # Changing the service should cause the token to no longer be found.
    self.client.service = 'gbase'
    self.client.current_token = None
    self.assert_(self.client.GetClientLoginToken() is None)

  def testSetAndGetClientLoginTokenWithScopes(self):
    scopes = gdata.service.CLIENT_LOGIN_SCOPES['cl'][:]
    scopes.extend(gdata.service.CLIENT_LOGIN_SCOPES['gbase'])
    self.client.SetClientLoginToken('baz', scopes=scopes)
    self.client.current_token = None
    self.assert_(self.client.GetClientLoginToken() is None)
    self.client.service = 'cl'
    self.assertEquals(self.client.GetClientLoginToken(), 'baz')
    self.client.service = 'gbase'
    self.assertEquals(self.client.GetClientLoginToken(), 'baz')
    self.client.service = 'wise'
    self.assert_(self.client.GetClientLoginToken() is None)

  def testLookupUsingTokenStore(self):
    scopes = gdata.service.CLIENT_LOGIN_SCOPES['cl'][:]
    scopes.extend(gdata.service.CLIENT_LOGIN_SCOPES['gbase'])
    self.client.SetClientLoginToken('baz', scopes=scopes)
    token = self.client.token_store.find_token(
        'http://www.google.com/calendar/feeds/foo')
    self.assertEquals(token.get_token_string(), 'baz')
    self.assertEquals(token.auth_header, '%s%s' % (
        gdata.auth.PROGRAMMATIC_AUTH_LABEL, 'baz'))
    token = self.client.token_store.find_token(
        'http://www.google.com/calendar/')
    self.assert_(isinstance(token, gdata.auth.ClientLoginToken) == False)
    token = self.client.token_store.find_token(
        'http://www.google.com/base/feeds/snippets')
    self.assertEquals(token.get_token_string(), 'baz')


if __name__ == '__main__':
  print ('GData Service Media Unit Tests\nNOTE: Please run these tests only '
         'with a test  account. The tests may delete or update your data.')
  username = raw_input('Please enter your username: ')
  password = getpass.getpass()
  unittest.main()
