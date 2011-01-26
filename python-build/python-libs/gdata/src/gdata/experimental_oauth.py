#!/usr/bin/env python

import binascii
import urllib
import time
import random
import hmac
from gdata.tlslite.utils import keyfactory
from gdata.tlslite.utils import cryptomath

OAUTH_VERSION = '1.0'

def get_normalized_http_url(http_request):
  full_url = http_request.uri.to_string()
  return full_url[:full_url.find('?')]

def escape(s):
  if isinstance(s, unicode):
    s = s.encode('utf-8')
  return urllib.quote(s, safe='~')

# util function: nonce
# pseudorandom number
def generate_nonce(length=8):
  return ''.join([str(random.randint(0, 9)) for i in xrange(length)])

def timestamp():
  return int(time.time())

def get_normalized_parameters(http_request, oauth_params):
  params = oauth_params.copy()
  params.update(http_request.uri.query)
  if 'oauth_signature' in params:
    del params['oauth_signature']
  pairs = params.items()
  # sort lexicographically, first after key, then after value
  pairs.sort()
  # combine key value pairs in string and escape
  x = '&'.join(['%s=%s' % (escape(str(k)), escape(str(v))) for k, v in pairs])
  return x

def build_signature_base_string(http_request, oauth_params):
  return '&'.join(
      escape(http_request.method.upper()),
      escape(get_normalized_http_url(http_request)),
      escape(get_normalized_parameters(http_request, oauth_params)))

def build_hmac_signature(self, http_request, oauth_params, consumer_secret,
    token_secret):
  raw = build_signature_base_string(http_request, oauth_params)
  key = None
  hashed = None
  if token_secret:
    key = '%s&%s' % (escape(consumer_secret), escape(token_secret))
  else:
    key = '%s&' % escape(consumer_secret)
  try:
    import hashlib
    hashed = hmac.new(key, raw, hashlib.sha1) 
  except ImportError:
    import sha
    hashed = hmac.new(key, raw, sha)
  # Calculate the digest base 64.
  return binascii.b2a_base64(hashed.digest())[:-1]

#?
def build_rsa_signature(self, http_request, oauth_params, cert):
  base_string = build_signature_base_string(http_request, oauth_params)
  # Pull the private key from the certificate
  privatekey = keyfactory.parsePrivateKey(cert)
  # Sign using the key
  signed = privatekey.hashAndSign(base_string)
  return binascii.b2a_base64(signed)[:-1]

#?  
def check_signature(self, http_request, oauth_params, cert, signature):
  decoded_sig = base64.b64decode(signature);
  base_string = build_signature_base_string(http_request, oauth_params)
  # Pull the public key from the certificate
  publickey = keyfactory.parsePEMKey(cert, public=True)
  # Check the signature
  return publickey.hashAndVerify(decoded_sig, base_string)

def to_auth_header(oauth_params):
  # Create a tuple containing key value pairs with an = between.
  # Example: oauth_token="ad180jjd733klru7"
  pairs = ('%s="%s"' % (escape(k), escape(v)) for k, v in oauth_params.iteritems())
  # Place a , between each pair and return as an OAuth auth header value.
  return 'OAuth %s' % (','.join(pairs)) 



TEST_PUBLIC_CERT = """
-----BEGIN CERTIFICATE-----
MIIBpjCCAQ+gAwIBAgIBATANBgkqhkiG9w0BAQUFADAZMRcwFQYDVQQDDA5UZXN0
IFByaW5jaXBhbDAeFw03MDAxMDEwODAwMDBaFw0zODEyMzEwODAwMDBaMBkxFzAV
BgNVBAMMDlRlc3QgUHJpbmNpcGFsMIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKB
gQC0YjCwIfYoprq/FQO6lb3asXrxLlJFuCvtinTF5p0GxvQGu5O3gYytUvtC2JlY
zypSRjVxwxrsuRcP3e641SdASwfrmzyvIgP08N4S0IFzEURkV1wp/IpH7kH41Etb
mUmrXSwfNZsnQRE5SYSOhh+LcK2wyQkdgcMv11l4KoBkcwIDAQABMA0GCSqGSIb3
DQEBBQUAA4GBAGZLPEuJ5SiJ2ryq+CmEGOXfvlTtEL2nuGtr9PewxkgnOjZpUy+d
4TvuXJbNQc8f4AMWL/tO9w0Fk80rWKp9ea8/df4qMq5qlFWlx6yOLQxumNOmECKb
WpkUQDIDJEoFUzKMVuJf4KO/FJ345+BNLGgbJ6WujreoM1X/gYfdnJ/J
-----END CERTIFICATE-----
"""

TEST_PRIVATE_CERT = """
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
-----END PRIVATE KEY-----
"""
