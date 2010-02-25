import time
import android

droid = android.Android()

# print welcome message
dialog = droid.dialogCreateAlert()['result']
droid.dialogSetTitle(dialog, 'User Interace')
droid.dialogSetMessage(dialog,
  "Welcome to ASE UI test. In next few "
  "steps we will demonstrate some of the "
  "basics in handling user interface.")
droid.dialogSetButton(dialog, 0, "Ok")
droid.dialogShow(dialog)

while droid.dialogGetResponse(dialog)['result'] == 0:
  time.sleep(0.5)

droid.dialogDismiss(dialog)

# spinner progress
dialog = droid.dialogCreateProgress(0)['result']
droid.dialogSetMessage(dialog,
  "This is simple spinner progress "
  "without title.")

droid.dialogShow(dialog)
time.sleep(2)
droid.dialogDismiss(dialog)

# spinner progress
dialog = droid.dialogCreateProgress(0)['result']
droid.dialogSetTitle(dialog, "Spinner progress")
droid.dialogSetMessage(dialog,
  "This is simple spinner progress "
  "*WITH* title and message.")

droid.dialogShow(dialog)
time.sleep(2)
droid.dialogDismiss(dialog)

# horizontal progress
dialog = droid.dialogCreateProgress(1)['result']
droid.dialogSetMessage(dialog,
  "This is horizontal progress "
  "without title.")

droid.dialogProgressSetMax(dialog, 50)
droid.dialogShow(dialog)

for x in range(0, 50):
  time.sleep(0.1)
  droid.dialogProgressSetCurrent(dialog, x+1)

droid.dialogDismiss(dialog)

# horizontal progress with title
dialog = droid.dialogCreateProgress(1)['result']
droid.dialogSetTitle(dialog, "Horizontal progress")
droid.dialogSetMessage(dialog,
  "This is horizontal progress "
  "*WITH* title and message.")

droid.dialogProgressSetMax(dialog, 50)
droid.dialogShow(dialog)

for x in range(0, 50):
  time.sleep(0.1)
  droid.dialogProgressSetCurrent(dialog, x+1)

droid.dialogDismiss(dialog)

# alert box
dialog = droid.dialogCreateAlert()['result']
droid.dialogSetTitle(dialog, "Alert box")
droid.dialogSetMessage(dialog,
  "Message for alert dialog box. "
  "This alert has no buttons and "
  "will be closed in 2 seconds!")

droid.dialogShow(dialog)
time.sleep(2)
droid.dialogDismiss(dialog)

# alert box with 3 options
dialog = droid.dialogCreateAlert()['result']
droid.dialogSetTitle(dialog, "Alert box")
droid.dialogSetMessage(dialog,
  "This alert box has 3 buttons and "
  "we'll wait for you to select one. "
  "After that, toast will be made "
  "containing your selection.")

buttons = {
  -1: 'Button 1',
  -2: 'Button 2',
  -3: 'Button 3',
  }

droid.dialogSetButton(dialog, 0, buttons[-1])
droid.dialogSetButton(dialog, 1, buttons[-2])
droid.dialogSetButton(dialog, 2, buttons[-3])

droid.dialogShow(dialog)

response = 0
while response == 0:
  response = droid.dialogGetResponse(dialog)['result']
  time.sleep(0.5)

droid.makeToast(buttons[response])
droid.dialogDismiss(dialog)

# print farewell message
dialog = droid.dialogCreateAlert()['result']
droid.dialogSetTitle(dialog, 'User Interace')
droid.dialogSetMessage(dialog,
  "Well that was it. We sure hope you "
  "like how all this is progressing and "
  "make sure all your wishes and critics "
  "reach our discussion group. Have fun!")
droid.dialogSetButton(dialog, 0, "Bye")
droid.dialogShow(dialog)

while droid.dialogGetResponse(dialog)['result'] == 0:
  time.sleep(0.5)

droid.dialogDismiss(dialog)
droid.exit()
