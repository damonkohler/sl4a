#!/usr/bin/env python
#
# Copyright (C) 2008, 2009 Google Inc.
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


# This module is used for version 2 of the Google Data APIs.


__author__ = 'j.s@google.com (Jeff Scudder)'


import unittest
import gdata.client
import gdata.gauth
import gdata.data
import atom.mock_http_core
import StringIO


class ClientLoginTest(unittest.TestCase):

  def test_token_request(self):
    client = gdata.client.GDClient()
    client.http_client = atom.mock_http_core.SettableHttpClient(200, 'OK', 
        'SID=DQAAAGgA...7Zg8CTN\n'
        'LSID=DQAAAGsA...lk8BBbG\n'
        'Auth=DQAAAGgA...dk3fA5N', {'Content-Type': 'text/plain'})
    token = client.request_client_login_token('email', 'pw', 'cp', 'test')
    self.assertTrue(isinstance(token, gdata.gauth.ClientLoginToken))
    self.assertEqual(token.token_string, 'DQAAAGgA...dk3fA5N')

    # Test a server response without a ClientLogin token.`
    client.http_client.set_response(200, 'OK', 'SID=12345\nLSID=34567', {})
    self.assertRaises(gdata.client.ClientLoginTokenMissing,
        client.request_client_login_token, 'email', 'pw', '', '')

    # Test a 302 redirect from the server on a login request.
    client.http_client.set_response(302, 'ignored', '', {})
    # TODO: change the exception class to one in gdata.client.
    self.assertRaises(gdata.client.BadAuthenticationServiceURL,
        client.request_client_login_token, 'email', 'pw', '', '')

    # Test a CAPTCHA challenge from the server
    client.http_client.set_response(403, 'Access Forbidden', 
        'Url=http://www.google.com/login/captcha\n'
        'Error=CaptchaRequired\n'
        'CaptchaToken=DQAAAGgA...dkI1LK9\n'
        # TODO: verify this sample CAPTCHA URL matches an
        # actual challenge from the server.
        'CaptchaUrl=Captcha?ctoken=HiteT4bVoP6-yFkHPibe7O9EqxeiI7lUSN', {})
    try:
      token = client.request_client_login_token('email', 'pw', '', '')
      self.fail('should raise a CaptchaChallenge on a 403 with a '
                'CaptchRequired error.')
    except gdata.client.CaptchaChallenge, challenge:
      self.assertEquals(challenge.captcha_url, 
          'http://www.google.com/accounts/'
          'Captcha?ctoken=HiteT4bVoP6-yFkHPibe7O9EqxeiI7lUSN')
      self.assertEquals(challenge.captcha_token, 'DQAAAGgA...dkI1LK9')

    # Test an unexpected response, a 404 for example.
    client.http_client.set_response(404, 'ignored', '', {})
    self.assertRaises(gdata.client.ClientLoginFailed,
        client.request_client_login_token, 'email', 'pw', '', '')

  def test_client_login(self):
    client = gdata.client.GDClient()
    client.http_client = atom.mock_http_core.SettableHttpClient(200, 'OK', 
        'SID=DQAAAGgA...7Zg8CTN\n'
        'LSID=DQAAAGsA...lk8BBbG\n'
        'Auth=DQAAAGgA...dk3fA5N', {'Content-Type': 'text/plain'})
    client.client_login('me@example.com', 'password', 'wise', 'unit test')
    self.assertTrue(isinstance(client.auth_token, gdata.gauth.ClientLoginToken))
    self.assertEqual(client.auth_token.token_string, 'DQAAAGgA...dk3fA5N')


class AuthSubTest(unittest.TestCase):

  def test_get_and_upgrade_token(self):
    client = gdata.client.GDClient()
    client.http_client = atom.mock_http_core.SettableHttpClient(200, 'OK', 
        'Token=UpgradedTokenVal\n'
        'Extra data', {'Content-Type': 'text/plain'})

    page_url = 'http://example.com/showcalendar.html?token=CKF50YzIHxCTKMAg'

    client.auth_token = gdata.gauth.AuthSubToken.from_url(page_url)

    self.assertTrue(isinstance(client.auth_token, gdata.gauth.AuthSubToken))
    self.assertEqual(client.auth_token.token_string, 'CKF50YzIHxCTKMAg')

    upgraded = client.upgrade_token()

    self.assertTrue(isinstance(client.auth_token, gdata.gauth.AuthSubToken))
    self.assertEqual(client.auth_token.token_string, 'UpgradedTokenVal')
    self.assertEqual(client.auth_token, upgraded)

    # Ensure passing in a token returns without modifying client's auth_token.
    client.http_client.set_response(200, 'OK', 'Token=4567', {})
    upgraded = client.upgrade_token(
        gdata.gauth.AuthSubToken.from_url('?token=1234'))
    self.assertEqual(upgraded.token_string, '4567')
    self.assertEqual(client.auth_token.token_string, 'UpgradedTokenVal')
    self.assertNotEqual(client.auth_token, upgraded)

    # Test exception cases
    client.auth_token = None
    self.assertRaises(gdata.client.UnableToUpgradeToken, client.upgrade_token,
                      None)
    self.assertRaises(gdata.client.UnableToUpgradeToken, client.upgrade_token)


class OAuthTest(unittest.TestCase):

  def test_hmac_flow(self):
    client = gdata.client.GDClient()
    client.http_client = atom.mock_http_core.SettableHttpClient(
        200, 'OK', 'oauth_token=ab3cd9j4ks7&oauth_token_secret=ZXhhbXBsZS',
        {})
    request_token = client.get_oauth_token(
        ['http://example.com/service'], 'http://example.net/myapp',
        'consumer', consumer_secret='secret')
    # Check that the response was correctly parsed.
    self.assertEqual(request_token.token, 'ab3cd9j4ks7')
    self.assertEqual(request_token.token_secret, 'ZXhhbXBsZS')
    self.assertEqual(request_token.auth_state, gdata.gauth.REQUEST_TOKEN)

    # Also check the Authorization header which was sent in the request.
    auth_header = client.http_client.last_request.headers['Authorization']
    self.assertTrue('OAuth' in auth_header)
    self.assertTrue(
        'oauth_callback="http%3A%2F%2Fexample.net%2Fmyapp"' in auth_header)
    self.assertTrue('oauth_version="1.0"' in auth_header)
    self.assertTrue('oauth_signature_method="HMAC-SHA1"' in auth_header)
    self.assertTrue('oauth_consumer_key="consumer"' in auth_header)

    # Check generation of the authorization URL.
    authorize_url = request_token.generate_authorization_url()
    self.assertTrue(str(authorize_url).startswith(
        'https://www.google.com/accounts/OAuthAuthorizeToken'))
    self.assertTrue('oauth_token=ab3cd9j4ks7' in str(authorize_url))

    # Check that the token information from the browser's URL is parsed.
    redirected_url = (
        'http://example.net/myapp?oauth_token=CKF5zz&oauth_verifier=Xhhbas')
    gdata.gauth.authorize_request_token(request_token, redirected_url)
    self.assertEqual(request_token.token, 'CKF5zz')
    self.assertEqual(request_token.verifier, 'Xhhbas')
    self.assertEqual(request_token.auth_state,
                     gdata.gauth.AUTHORIZED_REQUEST_TOKEN)

    # Check that the token upgrade response was correctly parsed.
    client.http_client.set_response(
        200, 'OK', 'oauth_token=3cd9Fj417&oauth_token_secret=Xhrh6bXBs', {})
    access_token = client.get_access_token(request_token)
    self.assertEqual(request_token.token, '3cd9Fj417')
    self.assertEqual(request_token.token_secret, 'Xhrh6bXBs')
    self.assertTrue(request_token.verifier is None)
    self.assertEqual(request_token.auth_state, gdata.gauth.ACCESS_TOKEN)
    self.assertEqual(request_token.token, access_token.token)
    self.assertEqual(request_token.token_secret, access_token.token_secret)
    self.assertTrue(access_token.verifier is None)
    self.assertEqual(request_token.auth_state, access_token.auth_state)

    # Also check the Authorization header which was sent in the request.
    auth_header = client.http_client.last_request.headers['Authorization']
    self.assertTrue('OAuth' in auth_header)
    self.assertTrue('oauth_callback="' not in auth_header)
    self.assertTrue('oauth_version="1.0"' in auth_header)
    self.assertTrue('oauth_verifier="Xhhbas"' in auth_header)
    self.assertTrue('oauth_signature_method="HMAC-SHA1"' in auth_header)
    self.assertTrue('oauth_consumer_key="consumer"' in auth_header)


class RequestTest(unittest.TestCase):

  def test_simple_request(self):
    client = gdata.client.GDClient()
    client.http_client = atom.mock_http_core.EchoHttpClient()
    response = client.request('GET', 'https://example.com/test')
    self.assertEqual(response.getheader('Echo-Host'), 'example.com:None')
    self.assertEqual(response.getheader('Echo-Uri'), '/test')
    self.assertEqual(response.getheader('Echo-Scheme'), 'https')
    self.assertEqual(response.getheader('Echo-Method'), 'GET')

    http_request = atom.http_core.HttpRequest(
        uri=atom.http_core.Uri(scheme='http', host='example.net', port=8080),
        method='POST', headers={'X': 1})
    http_request.add_body_part('test', 'text/plain')
    response = client.request(http_request=http_request)
    self.assertEqual(response.getheader('Echo-Host'), 'example.net:8080')
    # A Uri with path set to None should default to /.
    self.assertEqual(response.getheader('Echo-Uri'), '/')
    self.assertEqual(response.getheader('Echo-Scheme'), 'http')
    self.assertEqual(response.getheader('Echo-Method'), 'POST')
    self.assertEqual(response.getheader('Content-Type'), 'text/plain')
    self.assertEqual(response.getheader('X'), '1')
    self.assertEqual(response.read(), 'test')

    # Use the same request object from above, but overwrite the request path
    # by passing in a URI.
    response = client.request(uri='/new/path?p=1', http_request=http_request)
    self.assertEqual(response.getheader('Echo-Host'), 'example.net:8080')
    self.assertEqual(response.getheader('Echo-Uri'), '/new/path?p=1')
    self.assertEqual(response.getheader('Echo-Scheme'), 'http')
    self.assertEqual(response.read(), 'test')

  def test_gdata_version_header(self):
    client = gdata.client.GDClient()
    client.http_client = atom.mock_http_core.EchoHttpClient()

    response = client.request('GET', 'http://example.com')
    self.assertEqual(response.getheader('GData-Version'), None)

    client.api_version = '2'
    response = client.request('GET', 'http://example.com')
    self.assertEqual(response.getheader('GData-Version'), '2')

  def test_redirects(self):
    client = gdata.client.GDClient()
    client.http_client = atom.mock_http_core.MockHttpClient()
    # Add the redirect response for the initial request.
    first_request = atom.http_core.HttpRequest('http://example.com/1', 
                                               'POST')
    client.http_client.add_response(first_request, 302, None, 
        {'Location': 'http://example.com/1?gsessionid=12'})
    second_request = atom.http_core.HttpRequest(
        'http://example.com/1?gsessionid=12', 'POST')
    client.http_client.AddResponse(second_request, 200, 'OK', body='Done')

    response = client.Request('POST', 'http://example.com/1')
    self.assertEqual(response.status, 200)
    self.assertEqual(response.reason, 'OK')
    self.assertEqual(response.read(), 'Done')

    redirect_loop_request = atom.http_core.HttpRequest(
        'http://example.com/2?gsessionid=loop', 'PUT')
    client.http_client.add_response(redirect_loop_request, 302, None, 
        {'Location': 'http://example.com/2?gsessionid=loop'})
    try:
      response = client.request(method='PUT', uri='http://example.com/2?gsessionid=loop')
      self.fail('Loop URL should have redirected forever.')
    except gdata.client.RedirectError, err:
      self.assert_(str(err).startswith('Too many redirects from server'))

  def test_exercise_exceptions(self):
    # TODO
    pass

  def test_converter_vs_desired_class(self):

    def bad_converter(string):
      return 1
  
    class TestClass(atom.core.XmlElement):
      _qname = '{http://www.w3.org/2005/Atom}entry'
    
    client = gdata.client.GDClient()
    client.http_client = atom.mock_http_core.EchoHttpClient()
    test_entry = gdata.data.GDEntry()
    result = client.post(test_entry, 'http://example.com')
    self.assertTrue(isinstance(result, gdata.data.GDEntry))
    result = client.post(test_entry, 'http://example.com', converter=bad_converter)
    self.assertEquals(result, 1)
    result = client.post(test_entry, 'http://example.com', desired_class=TestClass)
    self.assertTrue(isinstance(result, TestClass))


class QueryTest(unittest.TestCase):

  def test_query_modifies_request(self):
    request = atom.http_core.HttpRequest()
    gdata.client.Query(
        text_query='foo', categories=['a', 'b']).modify_request(request)
    self.assertEqual(request.uri.query, {'q': 'foo', 'categories': 'a,b'})

  def test_client_uses_query_modification(self):
    """If the Query is passed as an unexpected param it should apply"""
    client = gdata.client.GDClient()
    client.http_client = atom.mock_http_core.EchoHttpClient()
    query = gdata.client.Query(max_results=7)

    client.http_client = atom.mock_http_core.SettableHttpClient(
        201, 'CREATED', gdata.data.GDEntry().ToString(), {})
    response = client.get('https://example.com/foo', a_random_param=query)
    self.assertEqual(
        client.http_client.last_request.uri.query['max-results'], '7')


class VersionConversionTest(unittest.TestCase):

  def test_use_default_version(self):
    self.assertEquals(gdata.client.get_xml_version(None), 1)

  def test_str_to_int_version(self):
    self.assertEquals(gdata.client.get_xml_version('1'), 1)
    self.assertEquals(gdata.client.get_xml_version('2'), 2)
    self.assertEquals(gdata.client.get_xml_version('2.1.2'), 2)
    self.assertEquals(gdata.client.get_xml_version('10.4'), 10)


def suite():
  return unittest.TestSuite((unittest.makeSuite(ClientLoginTest, 'test'),
                             unittest.makeSuite(AuthSubTest, 'test'),
                             unittest.makeSuite(OAuthTest, 'test'),
                             unittest.makeSuite(RequestTest, 'test'),
                             unittest.makeSuite(VersionConversionTest, 'test'),
                             unittest.makeSuite(QueryTest, 'test')))


if __name__ == '__main__':
  unittest.main()
