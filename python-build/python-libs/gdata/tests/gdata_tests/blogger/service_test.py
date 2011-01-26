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


"""Unit tests to exercise server interactions for blogger."""

__author__ = 'api.jscudder (Jeffrey Scudder)'

import unittest
import getpass
import atom
from gdata import test_data
import gdata.blogger
import gdata.blogger.service

username = ''
password = ''
test_blog_id = ''

class BloggerCrudTests(unittest.TestCase):
  
  def setUp(self):
    self.client = gdata.blogger.service.BloggerService(email=username, 
        password=password, source='GoogleInc-PythonBloggerUnitTests-1')
    # TODO: if the test_blog_id is not set, get the list of the user's blogs
    # and prompt for which blog to add the test posts to.
    self.client.ProgrammaticLogin()

  def testPostDraftUpdateAndDelete(self):
    new_entry = gdata.blogger.BlogPostEntry(title=atom.Title(
        text='Unit Test Post'))
    new_entry.content = atom.Content('text', None, 'Hello World')
    # Make this post a draft so it will not appear publicly on the blog.
    new_entry.control = atom.Control(draft=atom.Draft(text='yes'))
    new_entry.AddLabel('test')

    posted = self.client.AddPost(new_entry, blog_id=test_blog_id)

    self.assertEquals(posted.title.text, new_entry.title.text)
    # Should be one category in the posted entry for the 'test' label.
    self.assertEquals(len(posted.category), 1)
    self.assert_(isinstance(posted, gdata.blogger.BlogPostEntry))

    # Change the title and add more labels.
    posted.title.text = 'Updated'
    posted.AddLabel('second')
    updated = self.client.UpdatePost(entry=posted)

    self.assertEquals(updated.title.text, 'Updated')
    self.assertEquals(len(updated.category), 2)

    # Cleanup and delete the draft blog post.
    self.client.DeletePost(entry=posted)

  def testAddComment(self):
    # Create a test post to add comments to.
    new_entry = gdata.blogger.BlogPostEntry(title=atom.Title(
            text='Comments Test Post'))
    new_entry.content = atom.Content('text', None, 'Hello Comments')
    target_post = self.client.AddPost(new_entry, blog_id=test_blog_id)

    blog_id = target_post.GetBlogId()
    post_id = target_post.GetPostId()

    new_comment = gdata.blogger.CommentEntry()
    new_comment.content = atom.Content(text='Test comment')
    posted = self.client.AddComment(new_comment, blog_id=blog_id, 
        post_id=post_id)
    self.assertEquals(posted.content.text, new_comment.content.text)

    # Cleanup and delete the comment test blog post.
    self.client.DeletePost(entry=target_post)


class BloggerQueryTests(unittest.TestCase):

  def testConstructBlogQuery(self):
    pass

  def testConstructBlogQuery(self):
    pass

  def testConstructBlogQuery(self):
    pass


if __name__ == '__main__':
  print ('NOTE: Please run these tests only with a test account. ' +
         'The tests may delete or update your data.')
  username = raw_input('Please enter your username: ')
  password = getpass.getpass()
  test_blog_id = raw_input('Please enter the blog id for the test blog: ')
  unittest.main()

