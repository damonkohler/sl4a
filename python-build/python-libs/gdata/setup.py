#!/usr/bin/python
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


import sys
from distutils.core import setup

required = []

if sys.version_info[:3] < (2, 5, 0):
  required.append('elementtree')

setup(
    name='gdata',
    version='2.0.1',
    description='Python client library for Google data APIs',
    long_description = """\
The Google data Python client library makes it easy to interact with
Google services through the Google Data APIs. This library provides data
models and service modules for the the following Google data services:
- Google Calendar data API
- Google Contacts data API
- Google Spreadsheets data API
- Google Document List data APIs
- Google Base data API
- Google Apps Provisioning API
- Google Apps Email Migration API
- Google Apps Email Settings API
- Picasa Web Albums Data API
- Google Code Search Data API
- YouTube Data API
- Google Webmaster Tools Data API
- Blogger Data API
- Google Health API
- Google Book Search API
- Google Analytics API
- Google Finance API
- core Google data API functionality
The core Google data code provides sufficient functionality to use this
library with any Google data API (even if a module hasn't been written for
it yet). For example, this client can be used with the Notebook API. This
library may also be used with any Atom Publishing Protocol service (AtomPub).
""",
    author='Jeffrey Scudder',
    author_email='j.s@google.com',
    license='Apache 2.0',
    url='http://code.google.com/p/gdata-python-client/',
    packages=['atom', 'gdata', 'gdata.calendar', 'gdata.base',
        'gdata.spreadsheet', 'gdata.apps', 'gdata.apps.emailsettings',
        'gdata.apps.migration', 'gdata.apps.groups',
        'gdata.docs', 'gdata.codesearch', 'gdata.books',
        'gdata.photos', 'gdata.exif', 'gdata.geo', 'gdata.media',
        'gdata.contacts', 'gdata.youtube', 'gdata.webmastertools',
        'gdata.blogger', 'gdata.alt', 'gdata.oauth', 'gdata.tlslite',
        'gdata.Crypto', 'gdata.Crypto.Cipher', 'gdata.Crypto.Hash',
        'gdata.Crypto.Protocol', 'gdata.Crypto.PublicKey', 'gdata.Crypto.Util',
        'gdata.tlslite.utils', 'gdata.tlslite.integration', 'gdata.health',
        'gdata.analytics', 'gdata.finance'],
    package_dir = {'gdata':'src/gdata', 'atom':'src/atom'},
    install_requires=required
)
