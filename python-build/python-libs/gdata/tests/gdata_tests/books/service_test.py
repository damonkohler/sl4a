#!/usr/bin/python

__author__ = "James Sams <sams.james@gmail.com>"

import unittest
import getpass

import atom
import gdata.books
import gdata.books.service

from gdata import test_data

username = ""
password = ""

class BookCRUDTests(unittest.TestCase):
    
    def setUp(self):
        self.service = gdata.books.service.BookService(email=username, 
                password=password, source="Google-PythonGdataTest-1")
        if username and password:
            self.authenticated = True
            self.service.ProgrammaticLogin()
        else:
            self.authenticated = False

    def testPublicSearch(self):
        entry = self.service.get_by_google_id("b7GZr5Btp30C")
        self.assertEquals((entry.creator[0].text, entry.dc_title[0].text),
            ('John Rawls', 'A theory of justice'))
        feed = self.service.search_by_keyword(isbn="9780198250548")
        feed1 = self.service.search("9780198250548")
        self.assertEquals(len(feed.entry), 1)
        self.assertEquals(len(feed1.entry), 1)

    def testLibraryCrd(self):
        """
        the success of the create operations assumes the book was not already
        in the library. if it was, there will not be a failure, but a successful
        add will not actually be tested.
        """
        if not self.authenticated:
            return
        entry = self.service.get_by_google_id("b7GZr5Btp30C")
        entry = self.service.add_item_to_library(entry)
        lib = list(self.service.get_library())
        self.assert_(entry.to_dict()['title'] in 
            [x.to_dict()['title'] for x in lib])
        self.service.remove_item_from_library(entry)
        lib = list(self.service.get_library())
        self.assert_(entry.to_dict()['title'] not in 
            [x.to_dict()['title'] for x in lib])

    def testAnnotations(self):
        "annotations do not behave as expected"
        pass


if __name__ == "__main__":
    print "Please use a test account. May cause data loss."
    username = raw_input("Google Username: ").strip()
    password = getpass.getpass()
    unittest.main()
