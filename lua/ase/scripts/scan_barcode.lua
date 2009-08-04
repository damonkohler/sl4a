require "android"

barcode = android.scanBarcode()
android.printDict(barcode.result)
android.webSearch(barcode.result.SCAN_RESULT)
