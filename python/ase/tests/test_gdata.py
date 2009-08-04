import android
import gdata.docs.service

droid = android.Android()

# Create a client class which will make HTTP requests with Google Docs server.
client = gdata.docs.service.DocsService()

# Authenticate using your Google Docs email address and password.
username = droid.getInput('Username')['result']
password = droid.getInput('Password')['result']
client.ClientLogin(username, password)

# Query the server for an Atom feed containing a list of your documents.
documents_feed = client.GetDocumentListFeed()
# Loop through the feed and extract each document entry.
for document_entry in documents_feed.entry:
  # Display the title of the document on the command line.
  print document_entry.title.text
