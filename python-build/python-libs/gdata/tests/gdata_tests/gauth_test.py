#!/usr/bin/env python
#
# Copyright (C) 2009 Google Inc.
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
import gdata.gauth
import atom.http_core
import gdata.test_config as conf


PRIVATE_TEST_KEY = """
    -----BEGIN PRIVATE KEY-----
    MIICdgIBADANBgkqhkiG9w0BAQEFAASCAmAwggJcAgEAAoGBALRiMLAh9iimur8V
    A7qVvdqxevEuUkW4K+2KdMXmnQbG9Aa7k7eBjK1S+0LYmVjPKlJGNXHDGuy5Fw/d
    7rjVJ0BLB+ubPK8iA/Tw3hLQgXMRRGRXXCn8ikfuQfjUS1uZSatdLB81mydBETlJ
    hI6GH4twrbDJCR2Bwy/XWXgqgGRzAgMBAAECgYBYWVtleUzavkbrPjy0T5FMou8H
    X9u2AC2ry8vD/l7cqedtwMPp9k7TubgNFo+NGvKsl2ynyprOZR1xjQ7WgrgVB+mm
    uScOM/5HVceFuGRDhYTCObE+y1kxRloNYXnx3ei1zbeYLPCHdhxRYW7T0qcynNmw
    rn05/KO2RLjgQNalsQJBANeA3Q4Nugqy4QBUCEC09SqylT2K9FrrItqL2QKc9v0Z
    zO2uwllCbg0dwpVuYPYXYvikNHHg+aCWF+VXsb9rpPsCQQDWR9TT4ORdzoj+Nccn
    qkMsDmzt0EfNaAOwHOmVJ2RVBspPcxt5iN4HI7HNeG6U5YsFBb+/GZbgfBT3kpNG
    WPTpAkBI+gFhjfJvRw38n3g/+UeAkwMI2TJQS4n8+hid0uus3/zOjDySH3XHCUno
    cn1xOJAyZODBo47E+67R4jV1/gzbAkEAklJaspRPXP877NssM5nAZMU0/O/NGCZ+
    3jPgDUno6WbJn5cqm8MqWhW1xGkImgRk+fkDBquiq4gPiT898jusgQJAd5Zrr6Q8
    AO/0isr/3aa6O6NLQxISLKcPDk2NOccAfS/xOtfOz4sJYM3+Bs4Io9+dZGSDCA54
    Lw03eHTNQghS0A==
    -----END PRIVATE KEY-----"""


class AuthSubTest(unittest.TestCase):

  def test_generate_request_url(self):
    url = gdata.gauth.generate_auth_sub_url('http://example.com', 
        ['http://example.net/scope1'])
    self.assertTrue(isinstance(url, atom.http_core.Uri))
    self.assertEqual(url.query['secure'], '0')
    self.assertEqual(url.query['session'], '1')
    self.assertEqual(url.query['scope'], 'http://example.net/scope1')
    self.assertEqual(atom.http_core.Uri.parse_uri(
        url.query['next']).query['auth_sub_scopes'],
        'http://example.net/scope1')
    self.assertEqual(atom.http_core.Uri.parse_uri(url.query['next']).path, 
        '/')
    self.assertEqual(atom.http_core.Uri.parse_uri(url.query['next']).host, 
        'example.com')

  def test_from_url(self):
    token_str = gdata.gauth.auth_sub_string_from_url(
        'http://example.com?token=123abc')[0]
    self.assertEqual(token_str, '123abc')

  def test_from_http_body(self):
    token_str = gdata.gauth.auth_sub_string_from_body('Something\n'
        'Token=DQAA...7DCTN\n'
        'Expiration=20061004T123456Z\n')
    self.assertEqual(token_str, 'DQAA...7DCTN')

  def test_modify_request(self):
    token = gdata.gauth.AuthSubToken('tval')
    request = atom.http_core.HttpRequest()
    token.modify_request(request)
    self.assertEqual(request.headers['Authorization'], 'AuthSub token=tval')

  def test_create_and_upgrade_tokens(self):
    token = gdata.gauth.AuthSubToken.from_url(
        'http://example.com?token=123abc')
    self.assertTrue(isinstance(token, gdata.gauth.AuthSubToken))
    self.assertEqual(token.token_string, '123abc')
    self.assertEqual(token.scopes, [])
    token._upgrade_token('Token=456def')
    self.assertEqual(token.token_string, '456def')
    self.assertEqual(token.scopes, [])


class SecureAuthSubTest(unittest.TestCase):

  def test_build_data(self):
    request = atom.http_core.HttpRequest(method='PUT')
    request.uri = atom.http_core.Uri.parse_uri('http://example.com/foo?a=1')
    data = gdata.gauth.build_auth_sub_data(request, 1234567890, 'mynonce')
    self.assertEqual(data,
                     'PUT http://example.com/foo?a=1 1234567890 mynonce')

  def test_generate_signature(self):
    request = atom.http_core.HttpRequest(
        method='GET', uri=atom.http_core.Uri(host='example.com', path='/foo',
                                             query={'a': '1'}))
    data = gdata.gauth.build_auth_sub_data(request, 1134567890, 'p234908')
    self.assertEqual(data,
                     'GET http://example.com/foo?a=1 1134567890 p234908')
    self.assertEqual(
        gdata.gauth.generate_signature(data, PRIVATE_TEST_KEY),
        'GeBfeIDnT41dvLquPgDB4U5D4hfxqaHk/5LX1kccNBnL4BjsHWU1djbEp7xp3BL9ab'
        'QtLrK7oa/aHEHtGRUZGg87O+ND8iDPR76WFXAruuN8O8GCMqCDdPduNPY++LYO4MdJ'
        'BZNY974Nn0m6Hc0/T4M1ElqvPhl61fkXMm+ElSM=')


class TokensToAndFromBlobsTest(unittest.TestCase):

  def test_client_login_conversion(self):
    token = gdata.gauth.ClientLoginToken('test|key')
    copy = gdata.gauth.token_from_blob(gdata.gauth.token_to_blob(token))
    self.assertEqual(token.token_string, copy.token_string)
    self.assertTrue(isinstance(copy, gdata.gauth.ClientLoginToken))

  def test_authsub_conversion(self):
    token = gdata.gauth.AuthSubToken('test|key')
    copy = gdata.gauth.token_from_blob(gdata.gauth.token_to_blob(token))
    self.assertEqual(token.token_string, copy.token_string)
    self.assertTrue(isinstance(copy, gdata.gauth.AuthSubToken))
    
    scopes = ['http://example.com', 'http://other||test', 'thir|d']
    token = gdata.gauth.AuthSubToken('key-=', scopes)
    copy = gdata.gauth.token_from_blob(gdata.gauth.token_to_blob(token))
    self.assertEqual(token.token_string, copy.token_string)
    self.assertTrue(isinstance(copy, gdata.gauth.AuthSubToken))
    self.assertEqual(token.scopes, scopes)

  def test_join_and_split(self):
    token_string = gdata.gauth._join_token_parts('1x', 'test|string', '%x%',
                                                 '', None)
    self.assertEqual(token_string, '1x|test%7Cstring|%25x%25||')
    token_type, a, b, c, d = gdata.gauth._split_token_parts(token_string)
    self.assertEqual(token_type, '1x')
    self.assertEqual(a, 'test|string')
    self.assertEqual(b, '%x%')
    self.assertTrue(c is None)
    self.assertTrue(d is None)

  def test_secure_authsub_conversion(self):
    token = gdata.gauth.SecureAuthSubToken(
        '%^%', 'myRsaKey', ['http://example.com', 'http://example.org'])
    copy = gdata.gauth.token_from_blob(gdata.gauth.token_to_blob(token))
    self.assertEqual(copy.token_string, '%^%')
    self.assertEqual(copy.rsa_private_key, 'myRsaKey')
    self.assertEqual(copy.scopes,
                     ['http://example.com', 'http://example.org'])

    token = gdata.gauth.SecureAuthSubToken(rsa_private_key='f',
                                           token_string='b')
    blob = gdata.gauth.token_to_blob(token)
    self.assertEqual(blob, '1s|b|f')
    copy = gdata.gauth.token_from_blob(blob)
    self.assertEqual(copy.token_string, 'b')
    self.assertEqual(copy.rsa_private_key, 'f')
    self.assertEqual(copy.scopes, [])

    token = gdata.gauth.SecureAuthSubToken(None, '')
    blob = gdata.gauth.token_to_blob(token)
    self.assertEqual(blob, '1s||')
    copy = gdata.gauth.token_from_blob(blob)
    self.assertEqual(copy.token_string, None)
    self.assertEqual(copy.rsa_private_key, None)
    self.assertEqual(copy.scopes, [])

    token = gdata.gauth.SecureAuthSubToken('', None)
    blob = gdata.gauth.token_to_blob(token)
    self.assertEqual(blob, '1s||')
    copy = gdata.gauth.token_from_blob(blob)
    self.assertEqual(copy.token_string, None)
    self.assertEqual(copy.rsa_private_key, None)
    self.assertEqual(copy.scopes, [])

    token = gdata.gauth.SecureAuthSubToken(
        None, None, ['http://example.net', 'http://google.com'])
    blob = gdata.gauth.token_to_blob(token)
    self.assertEqual(
        blob, '1s|||http%3A%2F%2Fexample.net|http%3A%2F%2Fgoogle.com')
    copy = gdata.gauth.token_from_blob(blob)
    self.assertTrue(copy.token_string is None)
    self.assertTrue(copy.rsa_private_key is None)
    self.assertEqual(copy.scopes, ['http://example.net', 'http://google.com'])

  def test_oauth_rsa_conversion(self):
    token = gdata.gauth.OAuthRsaToken(
        'consumerKey', 'myRsa', 't', 'secret',
        gdata.gauth.AUTHORIZED_REQUEST_TOKEN, 'http://example.com/next',
        'verifier')
    blob = gdata.gauth.token_to_blob(token)
    self.assertEqual(
        blob, '1r|consumerKey|myRsa|t|secret|2|http%3A%2F%2Fexample.com'
            '%2Fnext|verifier')
    copy = gdata.gauth.token_from_blob(blob)
    self.assertTrue(isinstance(copy, gdata.gauth.OAuthRsaToken))
    self.assertEqual(copy.consumer_key, token.consumer_key)
    self.assertEqual(copy.rsa_private_key, token.rsa_private_key)
    self.assertEqual(copy.token, token.token)
    self.assertEqual(copy.token_secret, token.token_secret)
    self.assertEqual(copy.auth_state, token.auth_state)
    self.assertEqual(copy.next, token.next)
    self.assertEqual(copy.verifier, token.verifier)

    token = gdata.gauth.OAuthRsaToken(
        '', 'myRsa', 't', 'secret', 0)
    blob = gdata.gauth.token_to_blob(token)
    self.assertEqual(blob, '1r||myRsa|t|secret|0||')
    copy = gdata.gauth.token_from_blob(blob)
    self.assertTrue(isinstance(copy, gdata.gauth.OAuthRsaToken))
    self.assertFalse(copy.consumer_key == token.consumer_key)
    self.assertTrue(copy.consumer_key is None)
    self.assertEqual(copy.rsa_private_key, token.rsa_private_key)
    self.assertEqual(copy.token, token.token)
    self.assertEqual(copy.token_secret, token.token_secret)
    self.assertEqual(copy.auth_state, token.auth_state)
    self.assertEqual(copy.next, token.next)
    self.assertTrue(copy.next is None)
    self.assertEqual(copy.verifier, token.verifier)
    self.assertTrue(copy.verifier is None)

    token = gdata.gauth.OAuthRsaToken(
        rsa_private_key='myRsa', token='t', token_secret='secret',
        auth_state=gdata.gauth.ACCESS_TOKEN, verifier='v', consumer_key=None)
    blob = gdata.gauth.token_to_blob(token)
    self.assertEqual(blob, '1r||myRsa|t|secret|3||v')
    copy = gdata.gauth.token_from_blob(blob)
    self.assertEqual(copy.consumer_key, token.consumer_key)
    self.assertTrue(copy.consumer_key is None)
    self.assertEqual(copy.rsa_private_key, token.rsa_private_key)
    self.assertEqual(copy.token, token.token)
    self.assertEqual(copy.token_secret, token.token_secret)
    self.assertEqual(copy.auth_state, token.auth_state)
    self.assertEqual(copy.next, token.next)
    self.assertTrue(copy.next is None)
    self.assertEqual(copy.verifier, token.verifier)

  def test_oauth_hmac_conversion(self):
    token = gdata.gauth.OAuthHmacToken(
        'consumerKey', 'consumerSecret', 't', 'secret',
        gdata.gauth.REQUEST_TOKEN, 'http://example.com/next', 'verifier')
    blob = gdata.gauth.token_to_blob(token)
    self.assertEqual(
        blob, '1h|consumerKey|consumerSecret|t|secret|1|http%3A%2F%2F'
            'example.com%2Fnext|verifier')
    copy = gdata.gauth.token_from_blob(blob)
    self.assertTrue(isinstance(copy, gdata.gauth.OAuthHmacToken))
    self.assertEqual(copy.consumer_key, token.consumer_key)
    self.assertEqual(copy.consumer_secret, token.consumer_secret)
    self.assertEqual(copy.token, token.token)
    self.assertEqual(copy.token_secret, token.token_secret)
    self.assertEqual(copy.auth_state, token.auth_state)
    self.assertEqual(copy.next, token.next)
    self.assertEqual(copy.verifier, token.verifier)

    token = gdata.gauth.OAuthHmacToken(
        consumer_secret='c,s', token='t', token_secret='secret',
        auth_state=7, verifier='v', consumer_key=None)
    blob = gdata.gauth.token_to_blob(token)
    self.assertEqual(blob, '1h||c%2Cs|t|secret|7||v')
    copy = gdata.gauth.token_from_blob(blob)
    self.assertTrue(isinstance(copy, gdata.gauth.OAuthHmacToken))
    self.assertEqual(copy.consumer_key, token.consumer_key)
    self.assertTrue(copy.consumer_key is None)
    self.assertEqual(copy.consumer_secret, token.consumer_secret)
    self.assertEqual(copy.token, token.token)
    self.assertEqual(copy.token_secret, token.token_secret)
    self.assertEqual(copy.auth_state, token.auth_state)
    self.assertEqual(copy.next, token.next)
    self.assertTrue(copy.next is None)
    self.assertEqual(copy.verifier, token.verifier)

  def test_illegal_token_types(self):
    class MyToken(object):
      pass

    token = MyToken()
    self.assertRaises(gdata.gauth.UnsupportedTokenType,
                      gdata.gauth.token_to_blob, token)

    blob = '~~z'
    self.assertRaises(gdata.gauth.UnsupportedTokenType,
                      gdata.gauth.token_from_blob, blob)



class OAuthHmacTokenTests(unittest.TestCase):

  def test_build_base_string(self):
    request = atom.http_core.HttpRequest('http://example.com/', 'GET')
    base_string = gdata.gauth.build_oauth_base_string(
        request, 'example.org', '12345', gdata.gauth.HMAC_SHA1, 1246301653,
        '1.0')
    self.assertEqual(
        base_string, 'GET&http%3A%2F%2Fexample.com%2F&oauth_callback%3Doob%2'
        '6oauth_consumer_key%3Dexample.org%26oauth_nonce%3D12345%26oauth_sig'
        'nature_method%3DHMAC-SHA1%26oauth_timestamp%3D1246301653%26oauth_ve'
        'rsion%3D1.0')

    # Test using example from documentation.
    request = atom.http_core.HttpRequest(
        'http://www.google.com/calendar/feeds/default/allcalendars/full'
        '?orderby=starttime', 'GET')
    base_string = gdata.gauth.build_oauth_base_string(
        request, 'example.com', '4572616e48616d6d65724c61686176',
        gdata.gauth.RSA_SHA1, 137131200, '1.0', token='1%2Fab3cd9j4ks73hf7g',
        next='http://googlecodesamples.com/oauth_playground/index.php')
    self.assertEqual(
        base_string, 'GET&http%3A%2F%2Fwww.google.com%2Fcalendar%2Ffeeds%2Fd'
        'efault%2Fallcalendars%2Ffull&oauth_callback%3Dhttp%253A%252F%252Fgo'
        'oglecodesamples.com%252Foauth_playground%252Findex.php%26oauth_cons'
        'umer_key%3Dexample.com%26oauth_nonce%3D4572616e48616d6d65724c616861'
        '76%26oauth_signature_method%3DRSA-SHA1%26oauth_timestamp%3D13713120'
        '0%26oauth_token%3D1%25252Fab3cd9j4ks73hf7g%26oauth_version%3D1.0%26'
        'orderby%3Dstarttime')

    # Test various defaults.
    request = atom.http_core.HttpRequest('http://eXample.COM', 'get')
    base_string = gdata.gauth.build_oauth_base_string(
        request, 'example.org', '12345', gdata.gauth.HMAC_SHA1, 1246301653,
        '1.0')
    self.assertEqual(
        base_string, 'GET&http%3A%2F%2Fexample.com%2F&oauth_callback%3Doob%2'
        '6oauth_consumer_key%3Dexample.org%26oauth_nonce%3D12345%26oauth_sig'
        'nature_method%3DHMAC-SHA1%26oauth_timestamp%3D1246301653%26oauth_ve'
        'rsion%3D1.0')
    
    request = atom.http_core.HttpRequest('https://eXample.COM:443', 'get')
    base_string = gdata.gauth.build_oauth_base_string(
        request, 'example.org', '12345', gdata.gauth.HMAC_SHA1, 1246301653,
        '1.0', 'http://googlecodesamples.com/oauth_playground/index.php')
    self.assertEqual(
        base_string, 'GET&https%3A%2F%2Fexample.com%2F&oauth_callback%3Dhttp'
        '%253A%252F%252Fgooglecodesamples.com%252Foauth_playground%252Findex'
        '.php%26oauth_consumer_key%3Dexample.org%26oauth_nonce%3D12345%26oau'
        'th_signature_method%3DHMAC-SHA1%26oauth_timestamp%3D1246301653%26oa'
        'uth_version%3D1.0')

    request = atom.http_core.HttpRequest('http://eXample.COM:443', 'get')
    base_string = gdata.gauth.build_oauth_base_string(
        request, 'example.org', '12345', gdata.gauth.HMAC_SHA1, 1246301653,
        '1.0')
    self.assertEqual(
        base_string, 'GET&http%3A%2F%2Fexample.com%3A443%2F&oauth_callback%3'
        'Doob%26oauth_consumer_key%3De'
        'xample.org%26oauth_nonce%3D12345%26oauth_signature_method%3DHMAC-SH'
        'A1%26oauth_timestamp%3D1246301653%26oauth_version%3D1.0')

    request = atom.http_core.HttpRequest(
        atom.http_core.Uri(host='eXample.COM'), 'GET')
    base_string = gdata.gauth.build_oauth_base_string(
        request, 'example.org', '12345', gdata.gauth.HMAC_SHA1, 1246301653,
        '1.0', next='oob')
    self.assertEqual(
        base_string, 'GET&http%3A%2F%2Fexample.com%2F&oauth_callback%3Doob%2'
        '6oauth_consumer_key%3Dexample.org%26oauth_nonce%3D12345%26oauth_sig'
        'nature_method%3DHMAC-SHA1%26oauth_timestamp%3D1246301653%26oauth_ve'
        'rsion%3D1.0')

    request = atom.http_core.HttpRequest(
        'https://www.google.com/accounts/OAuthGetRequestToken', 'GET')
    request.uri.query['scope'] = ('https://docs.google.com/feeds/'
                                  ' http://docs.google.com/feeds/')
    base_string = gdata.gauth.build_oauth_base_string(
        request, 'anonymous', '48522759', gdata.gauth.HMAC_SHA1, 1246489532,
        '1.0', 'http://googlecodesamples.com/oauth_playground/index.php')
    self.assertEqual(
        base_string, 'GET&https%3A%2F%2Fwww.google.com%2Faccounts%2FOAuthGet'
        'RequestToken&oauth_callback%3Dhttp%253A%252F%252Fgooglecodesamples.'
        'com%252Foauth_playground%252Findex.php%26oauth_consumer_key%3Danony'
        'mous%26oauth_nonce%3D4852275'
        '9%26oauth_signature_method%3DHMAC-SHA1%26oauth_timestamp%3D12464895'
        '32%26oauth_version%3D1.0%26scope%3Dhttps%253A%252F%252Fdocs.google.'
        'com%252Ffeeds%252F%2520http%253A%252F%252Fdocs.google.com%252Ffeeds'
        '%252F')
        
  def test_generate_hmac_signature(self):
    # Use the example from the OAuth playground:
    # http://googlecodesamples.com/oauth_playground/
    request = atom.http_core.HttpRequest(
        'https://www.google.com/accounts/OAuthGetRequestToken?'
        'scope=http%3A%2F%2Fwww.blogger.com%2Ffeeds%2F', 'GET')
    signature = gdata.gauth.generate_hmac_signature(
        request, 'anonymous', 'anonymous', '1246491360', 
        'c0155b3f28697c029e7a62efff44bd46', '1.0', 
        next='http://googlecodesamples.com/oauth_playground/index.php')
    self.assertEqual(signature, '5a2GPdtAY3LWYv8IdiT3wp1Coeg=')

    # Try the same request but with a non escaped Uri object.
    request = atom.http_core.HttpRequest(
        'https://www.google.com/accounts/OAuthGetRequestToken', 'GET')
    request.uri.query['scope'] = 'http://www.blogger.com/feeds/'
    signature = gdata.gauth.generate_hmac_signature(
        request, 'anonymous', 'anonymous', '1246491360', 
        'c0155b3f28697c029e7a62efff44bd46', '1.0',
        'http://googlecodesamples.com/oauth_playground/index.php')
    self.assertEqual(signature, '5a2GPdtAY3LWYv8IdiT3wp1Coeg=')

    # A different request also checked against the OAuth playground.
    request = atom.http_core.HttpRequest(
        'https://www.google.com/accounts/OAuthGetRequestToken', 'GET')
    request.uri.query['scope'] = ('https://www.google.com/analytics/feeds/ '
                                  'http://www.google.com/base/feeds/ '
                                  'http://www.google.com/calendar/feeds/')
    signature = gdata.gauth.generate_hmac_signature(
        request, 'anonymous', 'anonymous', 1246491797, 
        '33209c4d7a09be4eb1d6ff18e00f8548', '1.0', 
        next='http://googlecodesamples.com/oauth_playground/index.php')
    self.assertEqual(signature, 'kFAgTTFDIWz4/xAabIlrcZZMTq8=')


class OAuthRsaTokenTests(unittest.TestCase):

  def test_generate_rsa_signature(self):
    request = atom.http_core.HttpRequest(
        'https://www.google.com/accounts/OAuthGetRequestToken?'
        'scope=http%3A%2F%2Fwww.blogger.com%2Ffeeds%2F', 'GET')
    signature = gdata.gauth.generate_rsa_signature(
        request, 'anonymous', PRIVATE_TEST_KEY, '1246491360',
        'c0155b3f28697c029e7a62efff44bd46', '1.0',
        next='http://googlecodesamples.com/oauth_playground/index.php')
    self.assertEqual(
        signature,
        'bfMantdttKaTrwoxU87JiXmMeXhAiXPiq79a5XmLlOYwwlX06Pu7CafMp7hW1fPeZtL'
        '4o9Sz3NvPI8GECCaZk7n5vi1EJ5/wfIQbddrC8j45joBG6gFSf4tRJct82dSyn6bd71'
        'knwPZH1sKK46Y0ePJvEIDI3JDd7pRZuMM2sN8=')


class OAuthHeaderTest(unittest.TestCase):

  def test_generate_auth_header(self):
    header = gdata.gauth.generate_auth_header(
        'consumerkey', 1234567890, 'mynonce', 'unknown_sig_type', 'sig')
    self.assertTrue(header.startswith('OAuth'))
    self.assertTrue(header.find('oauth_nonce="mynonce"') > -1)
    self.assertTrue(header.find('oauth_timestamp="1234567890"') > -1)
    self.assertTrue(header.find('oauth_consumer_key="consumerkey"') > -1)
    self.assertTrue(
        header.find('oauth_signature_method="unknown_sig_type"') > -1)
    self.assertTrue(header.find('oauth_version="1.0"') > -1)
    self.assertTrue(header.find('oauth_signature="sig"') > -1)

    header = gdata.gauth.generate_auth_header(
        'consumer/key', 1234567890, 'ab%&33', '', 'ab/+-_=')
    self.assertTrue(header.find('oauth_nonce="ab%25%2633"') > -1)
    self.assertTrue(header.find('oauth_consumer_key="consumer%2Fkey"') > -1)
    self.assertTrue(header.find('oauth_signature_method=""') > -1)
    self.assertTrue(header.find('oauth_signature="ab%2F%2B-_%3D"') > -1)


class OAuthGetRequestToken(unittest.TestCase):

  def test_request_hmac_request_token(self):
    request = gdata.gauth.generate_request_for_request_token(
        'anonymous', gdata.gauth.HMAC_SHA1, 
        ['http://www.blogger.com/feeds/', 
         'http://www.google.com/calendar/feeds/'], 
        consumer_secret='anonymous')
    request_uri = str(request.uri)
    self.assertTrue('http%3A%2F%2Fwww.blogger.com%2Ffeeds%2F' in request_uri)
    self.assertTrue(
        'http%3A%2F%2Fwww.google.com%2Fcalendar%2Ffeeds%2F' in request_uri)
    auth_header = request.headers['Authorization']
    self.assertTrue('oauth_consumer_key="anonymous"' in auth_header)
    self.assertTrue('oauth_signature_method="HMAC-SHA1"' in auth_header)
    self.assertTrue('oauth_version="1.0"' in auth_header)
    self.assertTrue('oauth_signature="' in auth_header)
    self.assertTrue('oauth_nonce="' in auth_header)
    self.assertTrue('oauth_timestamp="' in auth_header)

  def test_request_rsa_request_token(self):
    request = gdata.gauth.generate_request_for_request_token(
        'anonymous', gdata.gauth.RSA_SHA1, 
        ['http://www.blogger.com/feeds/', 
         'http://www.google.com/calendar/feeds/'], 
        rsa_key=PRIVATE_TEST_KEY)
    request_uri = str(request.uri)
    self.assertTrue('http%3A%2F%2Fwww.blogger.com%2Ffeeds%2F' in request_uri)
    self.assertTrue(
        'http%3A%2F%2Fwww.google.com%2Fcalendar%2Ffeeds%2F' in request_uri)
    auth_header = request.headers['Authorization']
    self.assertTrue('oauth_consumer_key="anonymous"' in auth_header)
    self.assertTrue('oauth_signature_method="RSA-SHA1"' in auth_header)
    self.assertTrue('oauth_version="1.0"' in auth_header)
    self.assertTrue('oauth_signature="' in auth_header)
    self.assertTrue('oauth_nonce="' in auth_header)
    self.assertTrue('oauth_timestamp="' in auth_header)

  def test_extract_token_from_body(self):
    body = ('oauth_token=4%2F5bNFM_efIu3yN-E9RrF1KfZzOAZG&oauth_token_secret='
            '%2B4O49V9WUOkjXgpOobAtgYzy&oauth_callback_confirmed=true')
    token, secret = gdata.gauth.oauth_token_info_from_body(body)
    self.assertEqual(token, '4/5bNFM_efIu3yN-E9RrF1KfZzOAZG')
    self.assertEqual(secret, '+4O49V9WUOkjXgpOobAtgYzy')

  def test_hmac_request_token_from_body(self):
    body = ('oauth_token=4%2F5bNFM_efIu3yN-E9RrF1KfZzOAZG&oauth_token_secret='
            '%2B4O49V9WUOkjXgpOobAtgYzy&oauth_callback_confirmed=true')
    request_token = gdata.gauth.hmac_token_from_body(body, 'myKey',
                                                     'mySecret', True)
    self.assertEqual(request_token.consumer_key, 'myKey')
    self.assertEqual(request_token.consumer_secret, 'mySecret')
    self.assertEqual(request_token.token, '4/5bNFM_efIu3yN-E9RrF1KfZzOAZG')
    self.assertEqual(request_token.token_secret, '+4O49V9WUOkjXgpOobAtgYzy')
    self.assertEqual(request_token.auth_state, gdata.gauth.REQUEST_TOKEN)

  def test_rsa_request_token_from_body(self):
    body = ('oauth_token=4%2F5bNFM_efIu3yN-E9RrF1KfZzOAZG&oauth_token_secret='
            '%2B4O49V9WUOkjXgpOobAtgYzy&oauth_callback_confirmed=true')
    request_token = gdata.gauth.rsa_token_from_body(body, 'myKey',
                                                    'rsaKey', True)
    self.assertEqual(request_token.consumer_key, 'myKey')
    self.assertEqual(request_token.rsa_private_key, 'rsaKey')
    self.assertEqual(request_token.token, '4/5bNFM_efIu3yN-E9RrF1KfZzOAZG')
    self.assertEqual(request_token.token_secret, '+4O49V9WUOkjXgpOobAtgYzy')
    self.assertEqual(request_token.auth_state, gdata.gauth.REQUEST_TOKEN)


class OAuthAuthorizeToken(unittest.TestCase):

  def test_generate_authorization_url(self):
    url = gdata.gauth.generate_oauth_authorization_url('/+=aosdpikk')
    self.assertTrue(str(url).startswith(
        'https://www.google.com/accounts/OAuthAuthorizeToken'))
    self.assertTrue('oauth_token=%2F%2B%3Daosdpikk' in str(url))

  def test_extract_auth_token(self):
    url = ('http://www.example.com/test?oauth_token='
           'CKF50YzIHxCT85KMAg&oauth_verifier=123zzz')
    token = gdata.gauth.oauth_token_info_from_url(url)
    self.assertEqual(token[0], 'CKF50YzIHxCT85KMAg')
    self.assertEqual(token[1], '123zzz')


def suite():
  return conf.build_suite([AuthSubTest, TokensToAndFromBlobsTest,
                           OAuthHmacTokenTests, OAuthRsaTokenTests,
                           OAuthHeaderTest, OAuthGetRequestToken,
                           OAuthAuthorizeToken])


if __name__ == '__main__':
  unittest.main()
