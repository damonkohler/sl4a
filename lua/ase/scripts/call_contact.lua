require "android"

contact = android.pickContact()
android.printDict(contact.result)
android.phoneCall(contact.result.data)
