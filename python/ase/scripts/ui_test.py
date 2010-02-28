import time
import android

droid = android.Android()

# Show welcome message.
title = 'User Interace'
message = ('Welcome to ASE UI test. In next few '
           'steps we will demonstrate some of the '
           'basics in handling user interface.')
dialog = droid.dialogCreateAlert(title, message)['result']
droid.dialogSetButton(dialog, 0, 'Continue')
droid.dialogShow(dialog)
droid.dialogGetResponse(dialog)

# Show alert with all buttons.
title = 'Alert'
message = ('This alert box has 3 buttons and '
           'will wait for you to press one. '
           'After that, a toast will tell you '
           'which one you pressed.')
dialog = droid.dialogCreateAlert(title, message)['result']
buttons = {
  -1: 'Button 1',
  -2: 'Button 2',
  -3: 'Button 3',
  }
droid.dialogSetButton(dialog, 0, buttons[-1])
droid.dialogSetButton(dialog, 1, buttons[-2])
droid.dialogSetButton(dialog, 2, buttons[-3])
droid.dialogShow(dialog)
response = droid.dialogGetResponse(dialog)['result']
droid.makeToast(buttons[response])
droid.dialogDismiss(dialog)

# Show spinner progress.
title = 'Spinner'
message = 'This is simple spinner progress.'
dialog = droid.dialogCreateSpinnerProgress(title, message)['result']
droid.dialogShow(dialog)
time.sleep(2)
droid.dialogDismiss(dialog)

# Show horizontal progress.
title = 'Horizontal'
message = 'This is simple horizontal progress.'
dialog = droid.dialogCreateHorizontalProgress(title, message)['result']
droid.dialogSetMaxProgress(dialog, 50)
droid.dialogShow(dialog)
for x in range(0, 50):
  time.sleep(0.1)
  droid.dialogSetCurrentProgress(dialog, x+1)
droid.dialogDismiss(dialog)
