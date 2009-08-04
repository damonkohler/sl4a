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


__author__ = 'api.jhartmann@gmail.com (Jochen Hartmann)'

import unittest
from gdata import test_data
import gdata.youtube
import gdata.youtube.service
import atom

YOUTUBE_TEMPLATE = '{http://gdata.youtube.com/schemas/2007}%s'
YT_FORMAT = YOUTUBE_TEMPLATE % ('format')


class VideoEntryTest(unittest.TestCase):

  def setUp(self):
    self.video_feed = gdata.youtube.YouTubeVideoFeedFromString(
        test_data.YOUTUBE_VIDEO_FEED)

  def testCorrectXmlParsing(self):
    self.assertEquals(self.video_feed.id.text,
      'http://gdata.youtube.com/feeds/api/standardfeeds/top_rated')
    self.assertEquals(len(self.video_feed.entry), 2)
    for entry in self.video_feed.entry:
      if (entry.id.text ==
          'http://gdata.youtube.com/feeds/api/videos/C71ypXYGho8'):

        self.assertEquals(entry.published.text, '2008-03-20T10:17:27.000-07:00')
        self.assertEquals(entry.updated.text, '2008-05-14T04:26:37.000-07:00')
        self.assertEquals(entry.category[0].scheme,
            'http://gdata.youtube.com/schemas/2007/keywords.cat')
        self.assertEquals(entry.category[0].term, 'karyn')
        self.assertEquals(entry.category[1].scheme,
            'http://gdata.youtube.com/schemas/2007/keywords.cat')
        self.assertEquals(entry.category[1].term, 'garcia')
        self.assertEquals(entry.category[2].scheme,
            'http://gdata.youtube.com/schemas/2007/keywords.cat')
        self.assertEquals(entry.category[2].term, 'me')
        self.assertEquals(entry.category[3].scheme,
            'http://schemas.google.com/g/2005#kind')
        self.assertEquals(entry.category[3].term,
            'http://gdata.youtube.com/schemas/2007#video')
        self.assertEquals(entry.title.text,
            'Me odeio por te amar - KARYN GARCIA')
        self.assertEquals(entry.content.text, 'http://www.karyngarcia.com.br')
        self.assertEquals(entry.link[0].rel, 'alternate')
        self.assertEquals(entry.link[0].href,
            'http://www.youtube.com/watch?v=C71ypXYGho8')
        self.assertEquals(entry.link[1].rel,
            'http://gdata.youtube.com/schemas/2007#video.related')
        self.assertEquals(entry.link[1].href,
            'http://gdata.youtube.com/feeds/api/videos/C71ypXYGho8/related')
        self.assertEquals(entry.link[2].rel, 'self')
        self.assertEquals(entry.link[2].href,
            ('http://gdata.youtube.com/feeds/api/standardfeeds'
             '/top_rated/C71ypXYGho8'))
        self.assertEquals(entry.author[0].name.text, 'TvKarynGarcia')
        self.assertEquals(entry.author[0].uri.text,
            'http://gdata.youtube.com/feeds/api/users/tvkaryngarcia')

        self.assertEquals(entry.media.title.text,
            'Me odeio por te amar - KARYN GARCIA')
        self.assertEquals(entry.media.description.text,
            'http://www.karyngarcia.com.br')
        self.assertEquals(entry.media.keywords.text,
            'amar, boyfriend, garcia, karyn, me, odeio, por, te')
        self.assertEquals(entry.media.duration.seconds, '203')
        self.assertEquals(entry.media.category[0].label, 'Music')
        self.assertEquals(entry.media.category[0].scheme,
            'http://gdata.youtube.com/schemas/2007/categories.cat')
        self.assertEquals(entry.media.category[0].text, 'Music')
        self.assertEquals(entry.media.category[1].label, 'test111')
        self.assertEquals(entry.media.category[1].scheme,
            'http://gdata.youtube.com/schemas/2007/developertags.cat')
        self.assertEquals(entry.media.category[1].text, 'test111')
        self.assertEquals(entry.media.category[2].label, 'test222')
        self.assertEquals(entry.media.category[2].scheme,
            'http://gdata.youtube.com/schemas/2007/developertags.cat')
        self.assertEquals(entry.media.category[2].text, 'test222')
        self.assertEquals(entry.media.content[0].url,
            'http://www.youtube.com/v/C71ypXYGho8')
        self.assertEquals(entry.media.content[0].type,
            'application/x-shockwave-flash')
        self.assertEquals(entry.media.content[0].medium, 'video')
        self.assertEquals(
            entry.media.content[0].extension_attributes['isDefault'], 'true')
        self.assertEquals(
            entry.media.content[0].extension_attributes['expression'], 'full')
        self.assertEquals(
            entry.media.content[0].extension_attributes['duration'], '203')
        self.assertEquals(
            entry.media.content[0].extension_attributes[YT_FORMAT], '5')
        self.assertEquals(entry.media.content[1].url,
            ('rtsp://rtsp2.youtube.com/ChoLENy73wIaEQmPhgZ2pXK9CxMYDSANFEgGDA'
             '==/0/0/0/video.3gp'))
        self.assertEquals(entry.media.content[1].type, 'video/3gpp')
        self.assertEquals(entry.media.content[1].medium, 'video')
        self.assertEquals(
            entry.media.content[1].extension_attributes['expression'], 'full')
        self.assertEquals(
            entry.media.content[1].extension_attributes['duration'], '203')
        self.assertEquals(
            entry.media.content[1].extension_attributes[YT_FORMAT], '1')
        self.assertEquals(entry.media.content[2].url,
            ('rtsp://rtsp2.youtube.com/ChoLENy73wIaEQmPhgZ2pXK9CxMYESARFEgGDA=='
             '/0/0/0/video.3gp'))
        self.assertEquals(entry.media.content[2].type, 'video/3gpp')
        self.assertEquals(entry.media.content[2].medium, 'video')
        self.assertEquals(
            entry.media.content[2].extension_attributes['expression'], 'full')
        self.assertEquals(
            entry.media.content[2].extension_attributes['duration'], '203')
        self.assertEquals(
            entry.media.content[2].extension_attributes[YT_FORMAT], '6')
        self.assertEquals(entry.media.player.url,
            'http://www.youtube.com/watch?v=C71ypXYGho8')
        self.assertEquals(entry.media.thumbnail[0].url,
            'http://img.youtube.com/vi/C71ypXYGho8/2.jpg')
        self.assertEquals(entry.media.thumbnail[0].height, '97')
        self.assertEquals(entry.media.thumbnail[0].width, '130')
        self.assertEquals(entry.media.thumbnail[0].extension_attributes['time'],
            '00:01:41.500')
        self.assertEquals(entry.media.thumbnail[1].url,
            'http://img.youtube.com/vi/C71ypXYGho8/1.jpg')
        self.assertEquals(entry.media.thumbnail[1].height, '97')
        self.assertEquals(entry.media.thumbnail[1].width, '130')
        self.assertEquals(entry.media.thumbnail[1].extension_attributes['time'], 
            '00:00:50.750')
        self.assertEquals(entry.media.thumbnail[2].url,
            'http://img.youtube.com/vi/C71ypXYGho8/3.jpg')
        self.assertEquals(entry.media.thumbnail[2].height, '97')
        self.assertEquals(entry.media.thumbnail[2].width, '130')
        self.assertEquals(entry.media.thumbnail[2].extension_attributes['time'],
            '00:02:32.250')
        self.assertEquals(entry.media.thumbnail[3].url,
            'http://img.youtube.com/vi/C71ypXYGho8/0.jpg')
        self.assertEquals(entry.media.thumbnail[3].height, '240')
        self.assertEquals(entry.media.thumbnail[3].width, '320')
        self.assertEquals(entry.media.thumbnail[3].extension_attributes['time'],
            '00:01:41.500')

        self.assertEquals(entry.statistics.view_count, '138864')
        self.assertEquals(entry.statistics.favorite_count, '2474')
        self.assertEquals(entry.rating.min, '1')
        self.assertEquals(entry.rating.max, '5')
        self.assertEquals(entry.rating.num_raters, '4626')
        self.assertEquals(entry.rating.average, '4.95')
        self.assertEquals(entry.comments.feed_link[0].href,
            ('http://gdata.youtube.com/feeds/api/videos/'
             'C71ypXYGho8/comments'))
        self.assertEquals(entry.comments.feed_link[0].count_hint, '27')

        self.assertEquals(entry.GetSwfUrl(),
            'http://www.youtube.com/v/C71ypXYGho8')
        self.assertEquals(entry.GetYouTubeCategoryAsString(), 'Music')


class VideoEntryPrivateTest(unittest.TestCase):

  def setUp(self):
    self.entry = gdata.youtube.YouTubeVideoEntryFromString(
        test_data.YOUTUBE_ENTRY_PRIVATE)

  def testCorrectXmlParsing(self):
    self.assert_(isinstance(self.entry,
        gdata.youtube.YouTubeVideoEntry))
    self.assert_(self.entry.media.private)


class VideoFeedTest(unittest.TestCase):

  def setUp(self):
    self.feed = gdata.youtube.YouTubeVideoFeedFromString(
        test_data.YOUTUBE_VIDEO_FEED)

  def testCorrectXmlParsing(self):
      self.assertEquals(self.feed.id.text,
          'http://gdata.youtube.com/feeds/api/standardfeeds/top_rated')
      self.assertEquals(self.feed.generator.text, 'YouTube data API')
      self.assertEquals(self.feed.generator.uri, 'http://gdata.youtube.com/')
      self.assertEquals(len(self.feed.author), 1)
      self.assertEquals(self.feed.author[0].name.text, 'YouTube')
      self.assertEquals(len(self.feed.category), 1)
      self.assertEquals(self.feed.category[0].scheme,
          'http://schemas.google.com/g/2005#kind')
      self.assertEquals(self.feed.category[0].term,
          'http://gdata.youtube.com/schemas/2007#video')
      self.assertEquals(self.feed.items_per_page.text, '25')
      self.assertEquals(len(self.feed.link), 4)
      self.assertEquals(self.feed.link[0].href,
          'http://www.youtube.com/browse?s=tr')
      self.assertEquals(self.feed.link[0].rel, 'alternate')
      self.assertEquals(self.feed.link[1].href,
          'http://gdata.youtube.com/feeds/api/standardfeeds/top_rated')
      self.assertEquals(self.feed.link[1].rel,
          'http://schemas.google.com/g/2005#feed')
      self.assertEquals(self.feed.link[2].href,
          ('http://gdata.youtube.com/feeds/api/standardfeeds/top_rated?'
           'start-index=1&max-results=25'))
      self.assertEquals(self.feed.link[2].rel, 'self')
      self.assertEquals(self.feed.link[3].href,
          ('http://gdata.youtube.com/feeds/api/standardfeeds/top_rated?'
           'start-index=26&max-results=25'))
      self.assertEquals(self.feed.link[3].rel, 'next')
      self.assertEquals(self.feed.start_index.text, '1')
      self.assertEquals(self.feed.title.text, 'Top Rated')
      self.assertEquals(self.feed.total_results.text, '100')
      self.assertEquals(self.feed.updated.text, '2008-05-14T02:24:07.000-07:00')
      self.assertEquals(len(self.feed.entry), 2)


class YouTubePlaylistFeedTest(unittest.TestCase):

  def setUp(self):
    self.feed = gdata.youtube.YouTubePlaylistFeedFromString(
        test_data.YOUTUBE_PLAYLIST_FEED)

  def testCorrectXmlParsing(self):
    self.assertEquals(len(self.feed.entry), 1)
    self.assertEquals(
        self.feed.category[0].scheme, 'http://schemas.google.com/g/2005#kind')
    self.assertEquals(self.feed.category[0].term,
        'http://gdata.youtube.com/schemas/2007#playlistLink')


class YouTubePlaylistEntryTest(unittest.TestCase):

  def setUp(self):
    self.feed = gdata.youtube.YouTubePlaylistFeedFromString(
        test_data.YOUTUBE_PLAYLIST_FEED)

  def testCorrectXmlParsing(self):
    for entry in self.feed.entry:
      self.assertEquals(entry.category[0].scheme,
          'http://schemas.google.com/g/2005#kind')
      self.assertEquals(entry.category[0].term,
          'http://gdata.youtube.com/schemas/2007#playlistLink')
      self.assertEquals(entry.description.text,
          'My new playlist Description')
      self.assertEquals(entry.feed_link[0].href,
          'http://gdata.youtube.com/feeds/playlists/8BCDD04DE8F771B2')
      self.assertEquals(entry.feed_link[0].rel,
          'http://gdata.youtube.com/schemas/2007#playlist')


class YouTubePlaylistVideoFeedTest(unittest.TestCase):

  def setUp(self):
    self.feed = gdata.youtube.YouTubePlaylistVideoFeedFromString(
        test_data.YOUTUBE_PLAYLIST_VIDEO_FEED)

  def testCorrectXmlParsing(self):
    self.assertEquals(len(self.feed.entry), 1)
    self.assertEquals(self.feed.category[0].scheme,
        'http://schemas.google.com/g/2005#kind')
    self.assertEquals(self.feed.category[0].term,
        'http://gdata.youtube.com/schemas/2007#playlist')
    self.assertEquals(self.feed.category[1].scheme,
        'http://gdata.youtube.com/schemas/2007/tags.cat')
    self.assertEquals(self.feed.category[1].term, 'videos')
    self.assertEquals(self.feed.category[2].scheme,
        'http://gdata.youtube.com/schemas/2007/tags.cat')
    self.assertEquals(self.feed.category[2].term, 'python')


class YouTubePlaylistVideoEntryTest(unittest.TestCase):

  def setUp(self):
    self.feed = gdata.youtube.YouTubePlaylistVideoFeedFromString(
        test_data.YOUTUBE_PLAYLIST_VIDEO_FEED)

  def testCorrectXmlParsing(self):
    self.assertEquals(len(self.feed.entry), 1)
    for entry in self.feed.entry:
      self.assertEquals(entry.position.text, '1')


class YouTubeVideoCommentFeedTest(unittest.TestCase):

  def setUp(self):
    self.feed = gdata.youtube.YouTubeVideoCommentFeedFromString(
        test_data.YOUTUBE_COMMENT_FEED)

  def testCorrectXmlParsing(self):
    self.assertEquals(len(self.feed.category), 1)
    self.assertEquals(self.feed.category[0].scheme,
        'http://schemas.google.com/g/2005#kind')
    self.assertEquals(self.feed.category[0].term,
        'http://gdata.youtube.com/schemas/2007#comment')
    self.assertEquals(len(self.feed.link), 4)
    self.assertEquals(self.feed.link[0].rel, 'related')
    self.assertEquals(self.feed.link[0].href,
        'http://gdata.youtube.com/feeds/videos/2Idhz9ef5oU')
    self.assertEquals(self.feed.link[1].rel, 'alternate')
    self.assertEquals(self.feed.link[1].href,
        'http://www.youtube.com/watch?v=2Idhz9ef5oU')
    self.assertEquals(self.feed.link[2].rel,
        'http://schemas.google.com/g/2005#feed')
    self.assertEquals(self.feed.link[2].href,
        'http://gdata.youtube.com/feeds/videos/2Idhz9ef5oU/comments')
    self.assertEquals(self.feed.link[3].rel, 'self')
    self.assertEquals(self.feed.link[3].href,
        ('http://gdata.youtube.com/feeds/videos/2Idhz9ef5oU/comments?'
         'start-index=1&max-results=25'))
    self.assertEquals(len(self.feed.entry), 3)


class YouTubeVideoCommentEntryTest(unittest.TestCase):

  def setUp(self):
    self.feed = gdata.youtube.YouTubeVideoCommentFeedFromString(
        test_data.YOUTUBE_COMMENT_FEED)

  def testCorrectXmlParsing(self):
    self.assertEquals(len(self.feed.entry), 3)
    self.assert_(isinstance(self.feed.entry[0],
        gdata.youtube.YouTubeVideoCommentEntry))

    for entry in self.feed.entry:
      if (entry.id.text ==
          ('http://gdata.youtube.com/feeds/videos/'
           '2Idhz9ef5oU/comments/91F809A3DE2EB81B')):
        self.assertEquals(entry.category[0].scheme,
            'http://schemas.google.com/g/2005#kind')
        self.assertEquals(entry.category[0].term,
            'http://gdata.youtube.com/schemas/2007#comment')
        self.assertEquals(entry.link[0].href,
            'http://gdata.youtube.com/feeds/videos/2Idhz9ef5oU')
        self.assertEquals(entry.link[0].rel, 'related')
        self.assertEquals(entry.content.text, 'test66')


class YouTubeVideoSubscriptionFeedTest(unittest.TestCase):

  def setUp(self):
    self.feed = gdata.youtube.YouTubeSubscriptionFeedFromString(
        test_data.YOUTUBE_SUBSCRIPTION_FEED)

  def testCorrectXmlParsing(self):

    self.assertEquals(len(self.feed.category), 1)
    self.assertEquals(self.feed.category[0].scheme,
        'http://schemas.google.com/g/2005#kind')
    self.assertEquals(self.feed.category[0].term,
        'http://gdata.youtube.com/schemas/2007#subscription')
    self.assertEquals(len(self.feed.link), 4)
    self.assertEquals(self.feed.link[0].rel, 'related')
    self.assertEquals(self.feed.link[0].href,
        'http://gdata.youtube.com/feeds/users/andyland74')
    self.assertEquals(self.feed.link[1].rel, 'alternate')
    self.assertEquals(self.feed.link[1].href,
        'http://www.youtube.com/profile_subscriptions?user=andyland74')
    self.assertEquals(self.feed.link[2].rel,
        'http://schemas.google.com/g/2005#feed')
    self.assertEquals(self.feed.link[2].href,
        'http://gdata.youtube.com/feeds/users/andyland74/subscriptions')
    self.assertEquals(self.feed.link[3].rel, 'self')
    self.assertEquals(self.feed.link[3].href,
        ('http://gdata.youtube.com/feeds/users/andyland74/subscriptions?'
         'start-index=1&max-results=25'))
    self.assertEquals(len(self.feed.entry), 1)


class YouTubeVideoSubscriptionEntryTest(unittest.TestCase):

  def setUp(self):
    self.feed = gdata.youtube.YouTubeSubscriptionFeedFromString(
        test_data.YOUTUBE_SUBSCRIPTION_FEED)

  def testCorrectXmlParsing(self):
    for entry in self.feed.entry:
      self.assertEquals(len(entry.category), 2)
      self.assertEquals(entry.category[0].scheme,
          'http://gdata.youtube.com/schemas/2007/subscriptiontypes.cat')
      self.assertEquals(entry.category[0].term, 'channel')
      self.assertEquals(entry.category[1].scheme,
          'http://schemas.google.com/g/2005#kind')
      self.assertEquals(entry.category[1].term,
          'http://gdata.youtube.com/schemas/2007#subscription')
      self.assertEquals(len(entry.link), 3)
      self.assertEquals(entry.link[0].href,
          'http://gdata.youtube.com/feeds/users/andyland74')
      self.assertEquals(entry.link[0].rel, 'related')
      self.assertEquals(entry.link[1].href,
          'http://www.youtube.com/profile_videos?user=NBC')
      self.assertEquals(entry.link[1].rel, 'alternate')
      self.assertEquals(entry.link[2].href,
          ('http://gdata.youtube.com/feeds/users/andyland74/subscriptions/'
           'd411759045e2ad8c'))
      self.assertEquals(entry.link[2].rel, 'self')
      self.assertEquals(len(entry.feed_link), 1)
      self.assertEquals(entry.feed_link[0].href,
          'http://gdata.youtube.com/feeds/api/users/nbc/uploads')
      self.assertEquals(entry.feed_link[0].rel,
          'http://gdata.youtube.com/schemas/2007#user.uploads')
      self.assertEquals(entry.username.text, 'NBC')


class YouTubeVideoResponseFeedTest(unittest.TestCase):

  def setUp(self):
    self.feed = gdata.youtube.YouTubeVideoFeedFromString(
        test_data.YOUTUBE_VIDEO_RESPONSE_FEED)

  def testCorrectXmlParsing(self):
    self.assertEquals(len(self.feed.category), 1)
    self.assertEquals(self.feed.category[0].scheme,
        'http://schemas.google.com/g/2005#kind')
    self.assertEquals(self.feed.category[0].term,
        'http://gdata.youtube.com/schemas/2007#video')
    self.assertEquals(len(self.feed.link), 4)
    self.assertEquals(self.feed.link[0].href,
        'http://gdata.youtube.com/feeds/videos/2c3q9K4cHzY')
    self.assertEquals(self.feed.link[0].rel, 'related')
    self.assertEquals(self.feed.link[1].href,
        'http://www.youtube.com/video_response_view_all?v=2c3q9K4cHzY')
    self.assertEquals(self.feed.link[1].rel, 'alternate')
    self.assertEquals(self.feed.link[2].href,
        'http://gdata.youtube.com/feeds/videos/2c3q9K4cHzY/responses')
    self.assertEquals(self.feed.link[2].rel,
        'http://schemas.google.com/g/2005#feed')
    self.assertEquals(self.feed.link[3].href,
        ('http://gdata.youtube.com/feeds/videos/2c3q9K4cHzY/responses?'
         'start-index=1&max-results=25'))
    self.assertEquals(self.feed.link[3].rel, 'self')
    self.assertEquals(len(self.feed.entry), 1)


class YouTubeVideoResponseEntryTest(unittest.TestCase):

  def setUp(self):
    self.feed = gdata.youtube.YouTubeVideoFeedFromString(
        test_data.YOUTUBE_VIDEO_RESPONSE_FEED)

  def testCorrectXmlParsing(self):
    for entry in self.feed.entry:
      self.assert_(isinstance(entry, gdata.youtube.YouTubeVideoEntry))


class YouTubeContactFeed(unittest.TestCase):

  def setUp(self):
    self.feed = gdata.youtube.YouTubeContactFeedFromString(
        test_data.YOUTUBE_CONTACTS_FEED)

  def testCorrectXmlParsing(self):
    self.assertEquals(len(self.feed.entry), 2)
    self.assertEquals(self.feed.category[0].scheme,
        'http://schemas.google.com/g/2005#kind')
    self.assertEquals(self.feed.category[0].term,
        'http://gdata.youtube.com/schemas/2007#friend')


class YouTubeContactEntry(unittest.TestCase):

  def setUp(self):
    self.feed= gdata.youtube.YouTubeContactFeedFromString(
        test_data.YOUTUBE_CONTACTS_FEED)

  def testCorrectXmlParsing(self):
    for entry in self.feed.entry:
      if (entry.id.text == ('http://gdata.youtube.com/feeds/users/'
          'apitestjhartmann/contacts/testjfisher')):
        self.assertEquals(entry.username.text, 'testjfisher')
        self.assertEquals(entry.status.text, 'pending')


class YouTubeUserEntry(unittest.TestCase):

  def setUp(self):
    self.feed = gdata.youtube.YouTubeUserEntryFromString(
        test_data.YOUTUBE_PROFILE)

  def testCorrectXmlParsing(self):
    self.assertEquals(self.feed.author[0].name.text, 'andyland74')
    self.assertEquals(self.feed.books.text, 'Catch-22')
    self.assertEquals(self.feed.category[0].scheme,
        'http://gdata.youtube.com/schemas/2007/channeltypes.cat')
    self.assertEquals(self.feed.category[0].term, 'Standard')
    self.assertEquals(self.feed.category[1].scheme,
        'http://schemas.google.com/g/2005#kind')
    self.assertEquals(self.feed.category[1].term,
        'http://gdata.youtube.com/schemas/2007#userProfile')
    self.assertEquals(self.feed.company.text, 'Google')
    self.assertEquals(self.feed.gender.text, 'm')
    self.assertEquals(self.feed.hobbies.text, 'Testing YouTube APIs')
    self.assertEquals(self.feed.hometown.text, 'Somewhere')
    self.assertEquals(len(self.feed.feed_link), 6)
    self.assertEquals(self.feed.feed_link[0].count_hint, '4')
    self.assertEquals(self.feed.feed_link[0].href,
        'http://gdata.youtube.com/feeds/users/andyland74/favorites')
    self.assertEquals(self.feed.feed_link[0].rel,
        'http://gdata.youtube.com/schemas/2007#user.favorites')
    self.assertEquals(self.feed.feed_link[1].count_hint, '1')
    self.assertEquals(self.feed.feed_link[1].href,
        'http://gdata.youtube.com/feeds/users/andyland74/contacts')
    self.assertEquals(self.feed.feed_link[1].rel,
        'http://gdata.youtube.com/schemas/2007#user.contacts')
    self.assertEquals(self.feed.feed_link[2].count_hint, '0')
    self.assertEquals(self.feed.feed_link[2].href,
        'http://gdata.youtube.com/feeds/users/andyland74/inbox')
    self.assertEquals(self.feed.feed_link[2].rel,
        'http://gdata.youtube.com/schemas/2007#user.inbox')
    self.assertEquals(self.feed.feed_link[3].count_hint, None)
    self.assertEquals(self.feed.feed_link[3].href,
        'http://gdata.youtube.com/feeds/users/andyland74/playlists')
    self.assertEquals(self.feed.feed_link[3].rel,
        'http://gdata.youtube.com/schemas/2007#user.playlists')
    self.assertEquals(self.feed.feed_link[4].count_hint, '4')
    self.assertEquals(self.feed.feed_link[4].href,
        'http://gdata.youtube.com/feeds/users/andyland74/subscriptions')
    self.assertEquals(self.feed.feed_link[4].rel,
        'http://gdata.youtube.com/schemas/2007#user.subscriptions')
    self.assertEquals(self.feed.feed_link[5].count_hint, '1')
    self.assertEquals(self.feed.feed_link[5].href,
        'http://gdata.youtube.com/feeds/users/andyland74/uploads')
    self.assertEquals(self.feed.feed_link[5].rel,
        'http://gdata.youtube.com/schemas/2007#user.uploads')
    self.assertEquals(self.feed.first_name.text, 'andy')
    self.assertEquals(self.feed.last_name.text, 'example')
    self.assertEquals(self.feed.link[0].href,
        'http://www.youtube.com/profile?user=andyland74')
    self.assertEquals(self.feed.link[0].rel, 'alternate')
    self.assertEquals(self.feed.link[1].href,
        'http://gdata.youtube.com/feeds/users/andyland74')
    self.assertEquals(self.feed.link[1].rel, 'self')
    self.assertEquals(self.feed.location.text, 'US')
    self.assertEquals(self.feed.movies.text, 'Aqua Teen Hungerforce')
    self.assertEquals(self.feed.music.text, 'Elliott Smith')
    self.assertEquals(self.feed.occupation.text, 'Technical Writer')
    self.assertEquals(self.feed.published.text, '2006-10-16T00:09:45.000-07:00')
    self.assertEquals(self.feed.school.text, 'University of North Carolina')
    self.assertEquals(self.feed.statistics.last_web_access,
        '2008-02-25T16:03:38.000-08:00')
    self.assertEquals(self.feed.statistics.subscriber_count, '1')
    self.assertEquals(self.feed.statistics.video_watch_count, '21')
    self.assertEquals(self.feed.statistics.view_count, '9')
    self.assertEquals(self.feed.thumbnail.url,
        'http://i.ytimg.com/vi/YFbSxcdOL-w/default.jpg')
    self.assertEquals(self.feed.title.text, 'andyland74 Channel')
    self.assertEquals(self.feed.updated.text, '2008-02-26T11:48:21.000-08:00')
    self.assertEquals(self.feed.username.text, 'andyland74')

if __name__ == '__main__':
  unittest.main()
