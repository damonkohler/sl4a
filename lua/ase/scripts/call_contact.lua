require "android"

contact = android.pickContact()
android.printDict(contact.result)
android.call(contact.result.data)
