require "android"

contact = android.pickPhone()
android.printDict(contact.result)
android.phoneCall(contact.result.data)
