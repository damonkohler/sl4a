#!/usr/bin/python


__author__ = "James Sams <sams.james@gmail.com>"


import unittest
from gdata import test_data
import gdata.books
import atom


class BookEntryTest(unittest.TestCase):

    def testBookEntryFromString(self):
        entry = gdata.books.Book.FromString(test_data.BOOK_ENTRY)
        self.assert_(isinstance(entry, gdata.books.Book))
        self.assertEquals([x.text for x in entry.creator], ['John Rawls'])
        self.assertEquals(entry.date.text, '1999')
        self.assertEquals(entry.format.text, '538 pages')
        self.assertEquals([x.text for x in entry.identifier],                   
           ['b7GZr5Btp30C', 'ISBN:0198250541', 'ISBN:9780198250548'])
        self.assertEquals([x.text for x in entry.publisher],
            ['Oxford University Press'])
        self.assertEquals(entry.subject, None)
        self.assertEquals([x.text for x in entry.dc_title],
            ['A theory of justice'])
        self.assertEquals(entry.viewability.value,
            'http://schemas.google.com/books/2008#view_partial')
        self.assertEquals(entry.embeddability.value,
            'http://schemas.google.com/books/2008#embeddable')
        self.assertEquals(entry.review, None)
        self.assertEquals([getattr(entry.rating, x) for x in
            ("min", "max", "average", "value")], ['1', '5', '4.00', None])
        self.assertEquals(entry.GetThumbnailLink().href,
            'http://bks0.books.google.com/books?id=b7GZr5Btp30C&printsec=frontcover&img=1&zoom=5&sig=ACfU3U121bWZsbjBfVwVRSK2o982jJTd1w&source=gbs_gdata')
        self.assertEquals(entry.GetInfoLink().href,
            'http://books.google.com/books?id=b7GZr5Btp30C&ie=ISO-8859-1&source=gbs_gdata')
        self.assertEquals(entry.GetPreviewLink(), None)
        self.assertEquals(entry.GetAnnotationLink().href,
            'http://www.google.com/books/feeds/users/me/volumes')
        self.assertEquals(entry.get_google_id(), 'b7GZr5Btp30C')

    def testBookFeedFromString(self):
        feed = gdata.books.BookFeed.FromString(test_data.BOOK_FEED)
        self.assert_(isinstance(feed, gdata.books.BookFeed))
        self.assertEquals( len(feed.entry), 1)
        self.assert_(isinstance(feed.entry[0], gdata.books.Book))

if __name__ == "__main__":
    unittest.main()
