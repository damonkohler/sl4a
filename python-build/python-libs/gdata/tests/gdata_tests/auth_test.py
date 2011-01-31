#!/usr/bin/env python
#
# Copyright (C) 2007, 2008 Google Inc.
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


import re
import unittest
import urllib
import gdata.auth


CONSUMER_KEY = 'www.yourwebapp.com'
CONSUMER_SECRET = 'qB1P2kCFDpRjF+/Iww4'

RSA_KEY = """-----BEGIN RSA PRIVATE KEY-----
MIICXAIBAAKBgQDVbOaFW+KXecfFJn1PIzYHnNXFxhaQ36QM0K5uSb0Y8NeQUlD2
6t8aKgnm6mcb4vaopHjjdIGWgAzM5Dt0oPIiDXo+jSQbvCIXRduuAt+0cFGb2d+L
hALk4AwB8IVIkDJWwgo5Z2OLsP2r/wQlUYKm/tnvQaevK24jNYMLWVJl2QIDAQAB
AoGAU93ERBlUVEPFjaJPUX67p4gotNvfWDSZiXOjZ7FQPnG9s3e1WyH2Y5irZXMs
61dnp+NhobfRiGtvHEB/YJgyLRk/CJDnMKslo95e7o65IE9VkcyY6Yvt7YTslsRX
Eu7T0xLEA7ON46ypCwNLeWxpJ9SWisEKu2yZJnWauCXEsgUCQQD7b2ZuhGx3msoP
YEnwvucp0UxneCvb68otfERZ1J6NfNP47QJw6OwD3r1sWCJ27QZmpvtQH1f8sCk9
t22anGG7AkEA2UzXdtQ8H1uLAN/XXX2qoLuvJK5jRswHS4GeOg4pnnDSiHg3Vbva
AxmMIL93ufvIy/xdoENwDPfcI4CbYlrDewJAGWy7W+OSIEoLsqBW+bwkHetnIXNa
ZAOkzxKoyrigS8hamupEe+xhqUaFuwXyfjobkpfCA+kXeZrKoM4CjEbR7wJAHMbf
Vd4/ZAu0edYq6DenLAgO5rWtcge9A5PTx25utovMZcQ917273mM4unGAwoGEkvcF
0x57LUx5u73hVgIdFwJBAKWGuHRwGPgTWYvhpHM0qveH+8KdU9BUt/kV4ONxIVDB
ftetEmJirqOGLECbImoLcUwQrgfMW4ZCxOioJMz/gY0=
-----END RSA PRIVATE KEY-----
"""


class AuthModuleUtilitiesTest(unittest.TestCase):

  def testGenerateClientLoginRequestBody(self):
    body = gdata.auth.GenerateClientLoginRequestBody('jo@gmail.com', 
        'password', 'test service', 'gdata.auth test')
    expected_parameters = {'Email':r'jo%40gmail.com', 'Passwd':'password',
        'service':'test+service', 'source':'gdata.auth+test',
        'accountType':'HOSTED_OR_GOOGLE'}
    self.__matchBody(body, expected_parameters)

    body = gdata.auth.GenerateClientLoginRequestBody('jo@gmail.com', 
        'password', 'test service', 'gdata.auth test', account_type='A TEST', 
        captcha_token='12345', captcha_response='test')
    expected_parameters['accountType'] = 'A+TEST'
    expected_parameters['logintoken'] = '12345' 
    expected_parameters['logincaptcha'] = 'test'
    self.__matchBody(body, expected_parameters)
    
  def __matchBody(self, body, expected_name_value_pairs):
    parameters = body.split('&')
    for param in parameters:
      (name, value) = param.split('=')
      self.assert_(expected_name_value_pairs[name] == value)

  def testGenerateClientLoginAuthToken(self):
    http_body = ('SID=DQAAAGgA7Zg8CTN\r\n'
                 'LSID=DQAAAGsAlk8BBbG\r\n'
                 'Auth=DQAAAGgAdk3fA5N')
    self.assert_(gdata.auth.GenerateClientLoginAuthToken(http_body) ==
                 'GoogleLogin auth=DQAAAGgAdk3fA5N')


class GenerateClientLoginRequestBodyTest(unittest.TestCase):

  def testPostBodyShouldMatchShortExample(self):
    auth_body = gdata.auth.GenerateClientLoginRequestBody('johndoe@gmail.com',
        'north23AZ', 'cl', 'Gulp-CalGulp-1.05')
    self.assert_(-1 < auth_body.find('Email=johndoe%40gmail.com'))
    self.assert_(-1 < auth_body.find('Passwd=north23AZ'))
    self.assert_(-1 < auth_body.find('service=cl'))
    self.assert_(-1 < auth_body.find('source=Gulp-CalGulp-1.05'))

  def testPostBodyShouldMatchLongExample(self):
    auth_body = gdata.auth.GenerateClientLoginRequestBody('johndoe@gmail.com',
        'north23AZ', 'cl', 'Gulp-CalGulp-1.05',
        captcha_token='DQAAAGgA...dkI1', captcha_response='brinmar')
    self.assert_(-1 < auth_body.find('logintoken=DQAAAGgA...dkI1'))
    self.assert_(-1 < auth_body.find('logincaptcha=brinmar'))

  def testEquivalenceWithOldLogic(self):
    email = 'jo@gmail.com'
    password = 'password'
    account_type = 'HOSTED'
    service = 'test'
    source = 'auth test'
    old_request_body = urllib.urlencode({'Email': email,
                                         'Passwd': password,
                                         'accountType': account_type,
                                         'service': service,
                                         'source': source})
    new_request_body = gdata.auth.GenerateClientLoginRequestBody(email,
         password, service, source, account_type=account_type)
    for parameter in old_request_body.split('&'):
      self.assert_(-1 < new_request_body.find(parameter))


class GenerateAuthSubUrlTest(unittest.TestCase):

  def testDefaultParameters(self):
    url = gdata.auth.GenerateAuthSubUrl('http://example.com/xyz?x=5', 
        'http://www.google.com/test/feeds')
    self.assert_(-1 < url.find(
        r'scope=http%3A%2F%2Fwww.google.com%2Ftest%2Ffeeds'))
    self.assert_(-1 < url.find(
        r'next=http%3A%2F%2Fexample.com%2Fxyz%3Fx%3D5'))
    self.assert_(-1 < url.find('secure=0'))
    self.assert_(-1 < url.find('session=1'))

  def testAllParameters(self):
    url = gdata.auth.GenerateAuthSubUrl('http://example.com/xyz?x=5',
            'http://www.google.com/test/feeds', secure=True, session=False,
            request_url='https://example.com/auth')
    self.assert_(-1 < url.find(
        r'scope=http%3A%2F%2Fwww.google.com%2Ftest%2Ffeeds'))
    self.assert_(-1 < url.find(
        r'next=http%3A%2F%2Fexample.com%2Fxyz%3Fx%3D5'))
    self.assert_(-1 < url.find('secure=1'))
    self.assert_(-1 < url.find('session=0'))
    self.assert_(url.startswith('https://example.com/auth'))


class GenerateOAuthRequestTokenUrlTest(unittest.TestCase):
  
  def testDefaultParameters(self):
    oauth_input_params = gdata.auth.OAuthInputParams(
        gdata.auth.OAuthSignatureMethod.RSA_SHA1, CONSUMER_KEY,
        rsa_key=RSA_KEY)
    scopes = [
        'http://abcd.example.com/feeds',
        'http://www.example.com/abcd/feeds'
        ]
    url = gdata.auth.GenerateOAuthRequestTokenUrl(
        oauth_input_params, scopes=scopes)
    self.assertEquals('https', url.protocol)
    self.assertEquals('www.google.com', url.host)
    self.assertEquals('/accounts/OAuthGetRequestToken', url.path)    
    self.assertEquals('1.0', url.params['oauth_version'])
    self.assertEquals('RSA-SHA1', url.params['oauth_signature_method'])
    self.assert_(url.params['oauth_nonce'])
    self.assert_(url.params['oauth_timestamp'])
    actual_scopes = url.params['scope'].split(' ')
    self.assertEquals(2, len(actual_scopes))
    for scope in actual_scopes:
      self.assert_(scope in scopes)
    self.assertEquals(CONSUMER_KEY, url.params['oauth_consumer_key'])
    self.assert_(url.params['oauth_signature'])

  def testAllParameters(self):
    oauth_input_params = gdata.auth.OAuthInputParams(
        gdata.auth.OAuthSignatureMethod.HMAC_SHA1, CONSUMER_KEY,
        consumer_secret=CONSUMER_SECRET)
    scopes = ['http://abcd.example.com/feeds']
    url = gdata.auth.GenerateOAuthRequestTokenUrl(
        oauth_input_params, scopes=scopes,
        request_token_url='https://www.example.com/accounts/OAuthRequestToken',
        extra_parameters={'oauth_version': '2.0', 'my_param': 'my_value'})
    self.assertEquals('https', url.protocol)
    self.assertEquals('www.example.com', url.host)
    self.assertEquals('/accounts/OAuthRequestToken', url.path)
    self.assertEquals('2.0', url.params['oauth_version'])
    self.assertEquals('HMAC-SHA1', url.params['oauth_signature_method'])
    self.assert_(url.params['oauth_nonce'])
    self.assert_(url.params['oauth_timestamp'])
    actual_scopes = url.params['scope'].split(' ')
    self.assertEquals(1, len(actual_scopes))
    for scope in actual_scopes:
      self.assert_(scope in scopes)
    self.assertEquals(CONSUMER_KEY, url.params['oauth_consumer_key'])
    self.assert_(url.params['oauth_signature'])
    self.assertEquals('my_value', url.params['my_param'])


class GenerateOAuthAuthorizationUrlTest(unittest.TestCase):
  
  def testDefaultParameters(self):
    token_key = 'ABCDDSFFDSG'
    token_secret = 'SDFDSGSDADADSAF'
    request_token = gdata.auth.OAuthToken(key=token_key, secret=token_secret)
    url = gdata.auth.GenerateOAuthAuthorizationUrl(request_token)
    self.assertEquals('https', url.protocol)
    self.assertEquals('www.google.com', url.host)
    self.assertEquals('/accounts/OAuthAuthorizeToken', url.path)    
    self.assertEquals(token_key, url.params['oauth_token'])

  def testAllParameters(self):
    token_key = 'ABCDDSFFDSG'
    token_secret = 'SDFDSGSDADADSAF'
    scopes = [
        'http://abcd.example.com/feeds',
        'http://www.example.com/abcd/feeds'
        ]    
    request_token = gdata.auth.OAuthToken(key=token_key, secret=token_secret,
                                          scopes=scopes)
    url = gdata.auth.GenerateOAuthAuthorizationUrl(
        request_token,
        authorization_url='https://www.example.com/accounts/OAuthAuthToken',
        callback_url='http://www.yourwebapp.com/print',
        extra_params={'permission': '1'},
        include_scopes_in_callback=True, scopes_param_prefix='token_scope')
    self.assertEquals('https', url.protocol)
    self.assertEquals('www.example.com', url.host)
    self.assertEquals('/accounts/OAuthAuthToken', url.path)    
    self.assertEquals(token_key, url.params['oauth_token'])
    expected_callback_url = ('http://www.yourwebapp.com/print?'
                             'token_scope=http%3A%2F%2Fabcd.example.com%2Ffeeds'
                             '+http%3A%2F%2Fwww.example.com%2Fabcd%2Ffeeds')
    self.assertEquals(expected_callback_url, url.params['oauth_callback'])


class GenerateOAuthAccessTokenUrlTest(unittest.TestCase):
  
  def testDefaultParameters(self):
    token_key = 'ABCDDSFFDSG'
    token_secret = 'SDFDSGSDADADSAF'
    authorized_request_token = gdata.auth.OAuthToken(key=token_key,
                                                     secret=token_secret)
    oauth_input_params = gdata.auth.OAuthInputParams(
        gdata.auth.OAuthSignatureMethod.HMAC_SHA1, CONSUMER_KEY,
        consumer_secret=CONSUMER_SECRET)    
    url = gdata.auth.GenerateOAuthAccessTokenUrl(authorized_request_token,
                                                 oauth_input_params)
    self.assertEquals('https', url.protocol)
    self.assertEquals('www.google.com', url.host)
    self.assertEquals('/accounts/OAuthGetAccessToken', url.path) 
    self.assertEquals(token_key, url.params['oauth_token'])
    self.assertEquals('1.0', url.params['oauth_version'])
    self.assertEquals('HMAC-SHA1', url.params['oauth_signature_method'])
    self.assert_(url.params['oauth_nonce'])
    self.assert_(url.params['oauth_timestamp'])
    self.assertEquals(CONSUMER_KEY, url.params['oauth_consumer_key'])
    self.assert_(url.params['oauth_signature'])    

  def testAllParameters(self):
    token_key = 'ABCDDSFFDSG'
    authorized_request_token = gdata.auth.OAuthToken(key=token_key)
    oauth_input_params = gdata.auth.OAuthInputParams(
        gdata.auth.OAuthSignatureMethod.RSA_SHA1, CONSUMER_KEY,
        rsa_key=RSA_KEY)    
    url = gdata.auth.GenerateOAuthAccessTokenUrl(
        authorized_request_token, oauth_input_params,
        access_token_url='https://www.example.com/accounts/OAuthGetAccessToken',
        oauth_version= '2.0')
    self.assertEquals('https', url.protocol)
    self.assertEquals('www.example.com', url.host)
    self.assertEquals('/accounts/OAuthGetAccessToken', url.path) 
    self.assertEquals(token_key, url.params['oauth_token'])
    self.assertEquals('2.0', url.params['oauth_version'])
    self.assertEquals('RSA-SHA1', url.params['oauth_signature_method'])
    self.assert_(url.params['oauth_nonce'])
    self.assert_(url.params['oauth_timestamp'])
    self.assertEquals(CONSUMER_KEY, url.params['oauth_consumer_key'])
    self.assert_(url.params['oauth_signature'])


class ExtractAuthSubTokensTest(unittest.TestCase):

  def testGetTokenFromUrl(self):
    url = 'http://www.yourwebapp.com/showcalendar.html?token=CKF50YzIH'
    self.assert_(gdata.auth.AuthSubTokenFromUrl(url) == 
        'AuthSub token=CKF50YzIH')
    self.assert_(gdata.auth.TokenFromUrl(url) == 'CKF50YzIH')
    url = 'http://www.yourwebapp.com/showcalendar.html?token==tokenCKF50YzIH='
    self.assert_(gdata.auth.AuthSubTokenFromUrl(url) == 
        'AuthSub token==tokenCKF50YzIH=')
    self.assert_(gdata.auth.TokenFromUrl(url) == '=tokenCKF50YzIH=')

  def testGetTokenFromHttpResponse(self):
    response_body = ('Token=DQAA...7DCTN\r\n'
        'Expiration=20061004T123456Z')
    self.assert_(gdata.auth.AuthSubTokenFromHttpBody(response_body) == 
        'AuthSub token=DQAA...7DCTN')

class CreateAuthSubTokenFlowTest(unittest.TestCase):

  def testGenerateRequest(self):
    request_url = gdata.auth.generate_auth_sub_url(next='http://example.com', 
        scopes=['http://www.blogger.com/feeds/', 
                'http://www.google.com/base/feeds/'])
    self.assertEquals(request_url.protocol, 'https')
    self.assertEquals(request_url.host, 'www.google.com')
    self.assertEquals(request_url.params['scope'], 
        'http://www.blogger.com/feeds/ http://www.google.com/base/feeds/')
    self.assertEquals(request_url.params['hd'], 'default')
    self.assert_(request_url.params['next'].find('auth_sub_scopes') > -1)
    self.assert_(request_url.params['next'].startswith('http://example.com'))

    # Use a more complicated 'next' URL.
    request_url = gdata.auth.generate_auth_sub_url(
      next='http://example.com/?token_scope=http://www.blogger.com/feeds/',
      scopes=['http://www.blogger.com/feeds/',
              'http://www.google.com/base/feeds/'])
    self.assert_(request_url.params['next'].find('auth_sub_scopes') > -1)
    self.assert_(request_url.params['next'].find('token_scope') > -1)
    self.assert_(request_url.params['next'].startswith('http://example.com/'))

  def testParseNextUrl(self):
    url = ('http://example.com/?auth_sub_scopes=http%3A%2F%2Fwww.blogger.com'
           '%2Ffeeds%2F+http%3A%2F%2Fwww.google.com%2Fbase%2Ffeeds%2F&'
           'token=my_nifty_token')
    token = gdata.auth.extract_auth_sub_token_from_url(url)
    self.assertEquals(token.get_token_string(), 'my_nifty_token')
    self.assert_(isinstance(token, gdata.auth.AuthSubToken))
    self.assert_(token.valid_for_scope('http://www.blogger.com/feeds/'))
    self.assert_(token.valid_for_scope('http://www.google.com/base/feeds/'))
    self.assert_(
        not token.valid_for_scope('http://www.google.com/calendar/feeds/'))

    # Parse a more complicated response.
    url = ('http://example.com/?auth_sub_scopes=http%3A%2F%2Fwww.blogger.com'
           '%2Ffeeds%2F+http%3A%2F%2Fwww.google.com%2Fbase%2Ffeeds%2F&'
           'token_scope=http%3A%2F%2Fwww.blogger.com%2Ffeeds%2F&'
           'token=second_token')
    token = gdata.auth.extract_auth_sub_token_from_url(url)
    self.assertEquals(token.get_token_string(), 'second_token')
    self.assert_(isinstance(token, gdata.auth.AuthSubToken))
    self.assert_(token.valid_for_scope('http://www.blogger.com/feeds/'))
    self.assert_(token.valid_for_scope('http://www.google.com/base/feeds/'))
    self.assert_(
        not token.valid_for_scope('http://www.google.com/calendar/feeds/'))

  def testParseNextWithNoToken(self):
    token = gdata.auth.extract_auth_sub_token_from_url('http://example.com/')
    self.assert_(token is None)
    token = gdata.auth.extract_auth_sub_token_from_url(
        'http://example.com/?no_token=foo&other=1')
    self.assert_(token is None)


class ExtractClientLoginTokenTest(unittest.TestCase):
  
  def testExtractFromBodyWithScopes(self):
    http_body_string = ('SID=DQAAAGgA7Zg8CTN\r\n'
                        'LSID=DQAAAGsAlk8BBbG\r\n'
                        'Auth=DQAAAGgAdk3fA5N')
    token = gdata.auth.extract_client_login_token(http_body_string, 
         ['http://docs.google.com/feeds/'])
    self.assertEquals(token.get_token_string(), 'DQAAAGgAdk3fA5N')
    self.assert_(isinstance(token, gdata.auth.ClientLoginToken))
    self.assert_(token.valid_for_scope('http://docs.google.com/feeds/'))
    self.assert_(not token.valid_for_scope('http://www.blogger.com/feeds'))


class ExtractOAuthTokensTest(unittest.TestCase):
  
  def testOAuthTokenFromUrl(self):
    scope_1 = 'http://docs.google.com/feeds/'
    scope_2 = 'http://www.blogger.com/feeds/'
    # Case 1: token and scopes both are present.
    url = ('http://dummy.com/?oauth_token_scope=http%3A%2F%2Fwww.blogger.com'
           '%2Ffeeds%2F+http%3A%2F%2Fdocs.google.com%2Ffeeds%2F&'
           'oauth_token=CMns6t7MCxDz__8B')
    token = gdata.auth.OAuthTokenFromUrl(url)
    self.assertEquals('CMns6t7MCxDz__8B', token.key)
    self.assertEquals(2, len(token.scopes))
    self.assert_(scope_1 in token.scopes)
    self.assert_(scope_2 in token.scopes)
    # Case 2: token and scopes both are present but scope_param_prefix
    # passed does not match the one present in the URL.
    url = ('http://dummy.com/?oauth_token_scope=http%3A%2F%2Fwww.blogger.com'
           '%2Ffeeds%2F+http%3A%2F%2Fdocs.google.com%2Ffeeds%2F&'
           'oauth_token=CMns6t7MCxDz__8B')
    token = gdata.auth.OAuthTokenFromUrl(url,
                                         scopes_param_prefix='token_scope')
    self.assertEquals('CMns6t7MCxDz__8B', token.key)
    self.assert_(not token.scopes)
    # Case 3: None present.
    url = ('http://dummy.com/?no_oauth_token_scope=http%3A%2F%2Fwww.blogger.com'
           '%2Ffeeds%2F+http%3A%2F%2Fdocs.google.com%2Ffeeds%2F&'
           'no_oauth_token=CMns6t7MCxDz__8B')
    token = gdata.auth.OAuthTokenFromUrl(url)
    self.assert_(token is None)
  
  def testOAuthTokenFromHttpBody(self):
    token_key = 'ABCD'
    token_secret = 'XYZ'
    # Case 1: token key and secret both present single time.
    http_body = 'oauth_token=%s&oauth_token_secret=%s' % (token_key,
                                                          token_secret)
    token = gdata.auth.OAuthTokenFromHttpBody(http_body)
    self.assertEquals(token_key, token.key)
    self.assertEquals(token_secret, token.secret)


class OAuthInputParametersTest(unittest.TestCase):
  
  def setUp(self):
    self.oauth_input_parameters_hmac = gdata.auth.OAuthInputParams(
        gdata.auth.OAuthSignatureMethod.HMAC_SHA1, CONSUMER_KEY,
        consumer_secret=CONSUMER_SECRET)
    self.oauth_input_parameters_rsa = gdata.auth.OAuthInputParams(
        gdata.auth.OAuthSignatureMethod.RSA_SHA1, CONSUMER_KEY,
        rsa_key=RSA_KEY)

  def testGetSignatureMethod(self):
    self.assertEquals(
        'HMAC-SHA1',
        self.oauth_input_parameters_hmac.GetSignatureMethod().get_name())
    rsa_signature_method = self.oauth_input_parameters_rsa.GetSignatureMethod()
    self.assertEquals('RSA-SHA1', rsa_signature_method.get_name())
    self.assertEquals(RSA_KEY, rsa_signature_method._fetch_private_cert(None))
  
  def testGetConsumer(self):
    self.assertEquals(CONSUMER_KEY,
                      self.oauth_input_parameters_hmac.GetConsumer().key)
    self.assertEquals(CONSUMER_KEY,
                      self.oauth_input_parameters_rsa.GetConsumer().key)
    self.assertEquals(CONSUMER_SECRET,
                      self.oauth_input_parameters_hmac.GetConsumer().secret)
    self.assert_(self.oauth_input_parameters_rsa.GetConsumer().secret is None)

    
class TokenClassesTest(unittest.TestCase):

  def testClientLoginToAndFromString(self):
    token = gdata.auth.ClientLoginToken()
    token.set_token_string('foo')
    self.assertEquals(token.get_token_string(), 'foo')
    self.assertEquals(token.auth_header, '%s%s' % (
        gdata.auth.PROGRAMMATIC_AUTH_LABEL, 'foo'))
    token.set_token_string(token.get_token_string())
    self.assertEquals(token.get_token_string(), 'foo')

  def testAuthSubToAndFromString(self):
    token = gdata.auth.AuthSubToken()
    token.set_token_string('foo')
    self.assertEquals(token.get_token_string(), 'foo')
    self.assertEquals(token.auth_header, '%s%s' % (
        gdata.auth.AUTHSUB_AUTH_LABEL, 'foo'))
    token.set_token_string(token.get_token_string())
    self.assertEquals(token.get_token_string(), 'foo')

  def testSecureAuthSubToAndFromString(self):
    # Case 1: no token.
    token = gdata.auth.SecureAuthSubToken(RSA_KEY)
    token.set_token_string('foo')
    self.assertEquals(token.get_token_string(), 'foo')
    token.set_token_string(token.get_token_string())
    self.assertEquals(token.get_token_string(), 'foo')
    self.assertEquals(str(token), 'foo')
    # Case 2: token is a string
    token = gdata.auth.SecureAuthSubToken(RSA_KEY, token_string='foo')
    self.assertEquals(token.get_token_string(), 'foo')
    token.set_token_string(token.get_token_string())
    self.assertEquals(token.get_token_string(), 'foo')
    self.assertEquals(str(token), 'foo')
    
  def testOAuthToAndFromString(self):
    token_key = 'ABCD'
    token_secret = 'XYZ'
    # Case 1: token key and secret both present single time.
    token_string = 'oauth_token=%s&oauth_token_secret=%s' % (token_key,
                                                             token_secret)    
    token = gdata.auth.OAuthToken()
    token.set_token_string(token_string)
    self.assert_(-1 < token.get_token_string().find(token_string.split('&')[0]))
    self.assert_(-1 < token.get_token_string().find(token_string.split('&')[1]))
    self.assertEquals(token_key, token.key)
    self.assertEquals(token_secret, token.secret)
    # Case 2: token key and secret both present multiple times with unwanted
    # parameters.
    token_string = ('oauth_token=%s&oauth_token_secret=%s&'
                    'oauth_token=%s&ExtraParams=GarbageString' % (token_key,
                                                                  token_secret,
                                                                  'LMNO'))    
    token = gdata.auth.OAuthToken()
    token.set_token_string(token_string)
    self.assert_(-1 < token.get_token_string().find(token_string.split('&')[0]))
    self.assert_(-1 < token.get_token_string().find(token_string.split('&')[1]))
    self.assertEquals(token_key, token.key)
    self.assertEquals(token_secret, token.secret)    
    # Case 3: Only token key present.
    token_string = 'oauth_token=%s' % (token_key,)
    token = gdata.auth.OAuthToken()
    token.set_token_string(token_string)
    self.assertEquals(token_string, token.get_token_string())
    self.assertEquals(token_key, token.key)
    self.assert_(not token.secret)    
    # Case 4: Only token key present.
    token_string = 'oauth_token_secret=%s' % (token_secret,)
    token = gdata.auth.OAuthToken()
    token.set_token_string(token_string)
    self.assertEquals(token_string, token.get_token_string())
    self.assertEquals(token_secret, token.secret)
    self.assert_(not token.key)
    # Case 5: None present.
    token_string = ''
    token = gdata.auth.OAuthToken()
    token.set_token_string(token_string)
    self.assert_(token.get_token_string() is None)
    self.assert_(not token.key)
    self.assert_(not token.secret)
    
  def testSecureAuthSubGetAuthHeader(self):
    # Case 1: Presence of OAuth token (in case of 3-legged OAuth)
    url = 'http://dummy.com/?q=notebook&s=true'
    token = gdata.auth.SecureAuthSubToken(RSA_KEY, token_string='foo')
    auth_header = token.GetAuthHeader('GET', url)
    self.assert_('Authorization' in auth_header)
    header_value = auth_header['Authorization']
    self.assert_(header_value.startswith(r'AuthSub token="foo"'))
    self.assert_(-1 < header_value.find(r'sigalg="rsa-sha1"'))
    self.assert_(-1 < header_value.find(r'data="'))
    self.assert_(-1 < header_value.find(r'sig="'))
    m = re.search(r'data="(.*?)"', header_value)
    self.assert_(m is not None)
    data = m.group(1)
    self.assert_(data.startswith('GET'))
    self.assert_(-1 < data.find(url))
  
  def testOAuthGetAuthHeader(self):
    # Case 1: Presence of OAuth token (in case of 3-legged OAuth)
    oauth_input_params = gdata.auth.OAuthInputParams(
        gdata.auth.OAuthSignatureMethod.RSA_SHA1, CONSUMER_KEY,
        rsa_key=RSA_KEY)    
    token = gdata.auth.OAuthToken(key='ABCDDSFFDSG',
                                  oauth_input_params=oauth_input_params)
    auth_header = token.GetAuthHeader('GET',
                                      'http://dummy.com/?q=notebook&s=true',
                                      realm='http://dummy.com')
    self.assert_('Authorization' in auth_header)
    header_value = auth_header['Authorization']
    self.assert_(-1 < header_value.find(r'OAuth realm="http://dummy.com"'))
    self.assert_(-1 < header_value.find(r'oauth_version="1.0"'))
    self.assert_(-1 < header_value.find(r'oauth_token="ABCDDSFFDSG"'))
    self.assert_(-1 < header_value.find(r'oauth_nonce="'))
    self.assert_(-1 < header_value.find(r'oauth_timestamp="'))
    self.assert_(-1 < header_value.find(r'oauth_signature="'))
    self.assert_(-1 < header_value.find(
        r'oauth_consumer_key="%s"' % CONSUMER_KEY))
    self.assert_(-1 < header_value.find(r'oauth_signature_method="RSA-SHA1"'))
    # Case 2: Absence of OAuth token (in case of 2-legged OAuth)
    oauth_input_params = gdata.auth.OAuthInputParams(
        gdata.auth.OAuthSignatureMethod.HMAC_SHA1, CONSUMER_KEY,
        consumer_secret=CONSUMER_SECRET)
    token = gdata.auth.OAuthToken(oauth_input_params=oauth_input_params)     
    auth_header = token.GetAuthHeader(
        'GET', 'http://dummy.com/?xoauth_requestor_id=user@gmail.com&q=book')
    self.assert_('Authorization' in auth_header)
    header_value = auth_header['Authorization']
    self.assert_(-1 < header_value.find(r'OAuth realm=""'))
    self.assert_(-1 < header_value.find(r'oauth_version="1.0"'))
    self.assertEquals(-1, header_value.find(r'oauth_token='))
    self.assert_(-1 < header_value.find(r'oauth_nonce="'))
    self.assert_(-1 < header_value.find(r'oauth_timestamp="'))
    self.assert_(-1 < header_value.find(r'oauth_signature="'))
    self.assert_(-1 < header_value.find(
        r'oauth_consumer_key="%s"' % CONSUMER_KEY))
    self.assert_(-1 < header_value.find(r'oauth_signature_method="HMAC-SHA1"'))
    

if __name__ == '__main__':
  unittest.main()
