#!/usr/bin/python
#
# Copyright (C) 2008 Google Inc.
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
from gdata import test_data
import gdata.blogger
import atom


class BlogEntryTest(unittest.TestCase):

  def testBlogEntryFromString(self):
    entry = gdata.blogger.BlogEntryFromString(test_data.BLOG_ENTRY)
    self.assertEquals(entry.GetBlogName(), 'blogName')
    self.assertEquals(entry.GetBlogId(), 'blogID')
    self.assertEquals(entry.title.text, 'Lizzy\'s Diary')

  def testBlogPostFeedFromString(self):
    feed = gdata.blogger.BlogPostFeedFromString(test_data.BLOG_POSTS_FEED)
    self.assertEquals(len(feed.entry), 1)
    self.assert_(isinstance(feed, gdata.blogger.BlogPostFeed))
    self.assert_(isinstance(feed.entry[0], gdata.blogger.BlogPostEntry))
    self.assertEquals(feed.entry[0].GetPostId(), 'postID')
    self.assertEquals(feed.entry[0].GetBlogId(), 'blogID')
    self.assertEquals(feed.entry[0].title.text, 'Quite disagreeable')

  def testCommentFeedFromString(self):
    feed = gdata.blogger.CommentFeedFromString(test_data.BLOG_COMMENTS_FEED)
    self.assertEquals(len(feed.entry), 1)
    self.assert_(isinstance(feed, gdata.blogger.CommentFeed))
    self.assert_(isinstance(feed.entry[0], gdata.blogger.CommentEntry))
    self.assertEquals(feed.entry[0].GetBlogId(), 'blogID')
    self.assertEquals(feed.entry[0].GetCommentId(), 'commentID')
    self.assertEquals(feed.entry[0].title.text, 'This is my first comment')
    self.assertEquals(feed.entry[0].in_reply_to.source, 
        'http://blogName.blogspot.com/feeds/posts/default/postID')
    self.assertEquals(feed.entry[0].in_reply_to.ref, 
        'tag:blogger.com,1999:blog-blogID.post-postID')
    self.assertEquals(feed.entry[0].in_reply_to.href, 
        'http://blogName.blogspot.com/2007/04/first-post.html')
    self.assertEquals(feed.entry[0].in_reply_to.type, 'text/html') 

  def testIdParsing(self):
    entry = gdata.blogger.BlogEntry()
    entry.id = atom.Id(
        text='tag:blogger.com,1999:user-146606542.blog-4023408167658848')
    self.assertEquals(entry.GetBlogId(), '4023408167658848')
    entry.id = atom.Id(text='tag:blogger.com,1999:blog-4023408167658848')
    self.assertEquals(entry.GetBlogId(), '4023408167658848')


class InReplyToTest(unittest.TestCase):

  def testToAndFromString(self):
    in_reply_to = gdata.blogger.InReplyTo(href='http://example.com/href',
        ref='http://example.com/ref', source='http://example.com/my_post',
        type='text/html')
    xml_string = str(in_reply_to)
    parsed = gdata.blogger.InReplyToFromString(xml_string)
    self.assertEquals(parsed.source, in_reply_to.source)
    self.assertEquals(parsed.href, in_reply_to.href)
    self.assertEquals(parsed.ref, in_reply_to.ref)
    self.assertEquals(parsed.type, in_reply_to.type)


class CommentEntryTest(unittest.TestCase):

  def testToAndFromString(self):
    comment = gdata.blogger.CommentEntry(content=atom.Content(text='Nifty!'),
        in_reply_to=gdata.blogger.InReplyTo(
            source='http://example.com/my_post'))
    parsed = gdata.blogger.CommentEntryFromString(str(comment))
    self.assertEquals(parsed.in_reply_to.source, comment.in_reply_to.source)
    self.assertEquals(parsed.content.text, comment.content.text)


if __name__ == '__main__':
  unittest.main()
