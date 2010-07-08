require "android"

barcode = android.scanBarcode()
android.printDict(barcode.result)
android.search(barcode.result.extras.SCAN_RESULT)
