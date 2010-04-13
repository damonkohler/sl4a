import android
import time

droid = android.Android()
droid.toggleBluetoothState(True)
droid.dialogCreateAlert('Be a server?')
droid.dialogSetPositiveButtonText('Yes')
droid.dialogSetNegativeButtonText('No')
droid.dialogShow()
result = droid.dialogGetResponse()
is_server = result.result['which'] == 'positive'
if is_server:
  droid.bluetoothMakeDiscoverable()
  droid.bluetoothAccept()
else:
  droid.bluetoothConnect()

def receiveMessage():
  while True:
    result = droid.receiveEvent()
    if result.result is not None and result.result['name'] == 'bluetooth-read':
      return result.result['message']
    time.sleep(0.5)

if is_server:
  result = droid.getInput('Chat', 'Enter a message').result
  if result is None:
    droid.exit()
  droid.bluetoothWrite(result)

while True:
  message = receiveMessage()
  droid.dialogCreateAlert('Chat Received', message)
  droid.dialogSetPositiveButtonText('Ok')
  droid.dialogShow()
  droid.dialogGetResponse()
  result = droid.getInput('Chat', 'Enter a message').result
  if result is None:
    break
  droid.bluetoothWrite(result)

droid.exit()
