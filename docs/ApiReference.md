Having trouble? Got questions? Check the [FAQ](FAQ.md) or try the
[SL4A discussion group](http://groups.google.com/group/android-scripting).

More detailed API Help [here](http://www.mithril.com.au/android/doc)

**ActivityResultFacade**
  * [setResultBoolean](#setResultBoolean)
  * [setResultBooleanArray](#setResultBooleanArray)
  * [setResultByte](#setResultByte)
  * [setResultByteArray](#setResultByteArray)
  * [setResultChar](#setResultChar)
  * [setResultCharArray](#setResultCharArray)
  * [setResultDouble](#setResultDouble)
  * [setResultDoubleArray](#setResultDoubleArray)
  * [setResultFloat](#setResultFloat)
  * [setResultFloatArray](#setResultFloatArray)
  * [setResultInteger](#setResultInteger)
  * [setResultIntegerArray](#setResultIntegerArray)
  * [setResultLong](#setResultLong)
  * [setResultLongArray](#setResultLongArray)
  * [setResultSerializable](#setResultSerializable)
  * [setResultShort](#setResultShort)
  * [setResultShortArray](#setResultShortArray)
  * [setResultString](#setResultString)
  * [setResultStringArray](#setResultStringArray)
**AndroidFacade**
  * [environment](#environment)
  * [getClipboard](#getClipboard)
  * [getConstants](#getConstants)
  * [getInput](#getInput)
  * [getIntent](#getIntent)
  * [getPackageVersion](#getPackageVersion)
  * [getPackageVersionCode](#getPackageVersionCode)
  * [getPassword](#getPassword)
  * [log](#log)
  * [makeIntent](#makeIntent)
  * [makeToast](#makeToast)
  * [notify](#notify)
  * [requiredVersion](#requiredVersion)
  * [sendBroadcast](#sendBroadcast)
  * [sendBroadcastIntent](#sendBroadcastIntent)
  * [sendEmail](#sendEmail)
  * [setClipboard](#setClipboard)
  * [startActivity](#startActivity)
  * [startActivityForResult](#startActivityForResult)
  * [startActivityForResultIntent](#startActivityForResultIntent)
  * [startActivityIntent](#startActivityIntent)
  * [vibrate](#vibrate)
**ApplicationManagerFacade**
  * [forceStopPackage](#forceStopPackage)
  * [getLaunchableApplications](#getLaunchableApplications)
  * [getRunningPackages](#getRunningPackages)
  * [launch](#launch)
**BatteryManagerFacade**
  * [batteryCheckPresent](#batteryCheckPresent)
  * [batteryGetHealth](#batteryGetHealth)
  * [batteryGetLevel](#batteryGetLevel)
  * [batteryGetPlugType](#batteryGetPlugType)
  * [batteryGetStatus](#batteryGetStatus)
  * [batteryGetTechnology](#batteryGetTechnology)
  * [batteryGetTemperature](#batteryGetTemperature)
  * [batteryGetVoltage](#batteryGetVoltage)
  * [batteryStartMonitoring](#batteryStartMonitoring)
  * [batteryStopMonitoring](#batteryStopMonitoring)
  * [readBatteryData](#readBatteryData)
**BluetoothFacade** Requires API Level 5.
  * [bluetoothAccept](#bluetoothAccept)
  * [bluetoothActiveConnections](#bluetoothActiveConnections)
  * [bluetoothConnect](#bluetoothConnect)
  * [bluetoothDiscoveryCancel](#bluetoothDiscoveryCancel)
  * [bluetoothDiscoveryStart](#bluetoothDiscoveryStart)
  * [bluetoothGetConnectedDeviceName](#bluetoothGetConnectedDeviceName)
  * [bluetoothGetLocalAddress](#bluetoothGetLocalAddress)
  * [bluetoothGetLocalName](#bluetoothGetLocalName)
  * [bluetoothGetRemoteDeviceName](#bluetoothGetRemoteDeviceName)
  * [bluetoothGetScanMode](#bluetoothGetScanMode)
  * [bluetoothIsDiscovering](#bluetoothIsDiscovering)
  * [bluetoothMakeDiscoverable](#bluetoothMakeDiscoverable)
  * [bluetoothRead](#bluetoothRead)
  * [bluetoothReadBinary](#bluetoothReadBinary)
  * [bluetoothReadLine](#bluetoothReadLine)
  * [bluetoothReadReady](#bluetoothReadReady)
  * [bluetoothSetLocalName](#bluetoothSetLocalName)
  * [bluetoothStop](#bluetoothStop)
  * [bluetoothWrite](#bluetoothWrite)
  * [bluetoothWriteBinary](#bluetoothWriteBinary)
  * [checkBluetoothState](#checkBluetoothState)
  * [toggleBluetoothState](#toggleBluetoothState)
**CameraFacade**
  * [cameraCapturePicture](#cameraCapturePicture)
  * [cameraInteractiveCapturePicture](#cameraInteractiveCapturePicture)
**CommonIntentsFacade**
  * [pick](#pick)
  * [scanBarcode](#scanBarcode)
  * [search](#search)
  * [view](#view)
  * [viewContacts](#viewContacts)
  * [viewHtml](#viewHtml)
  * [viewMap](#viewMap)
**ContactsFacade**
  * [contactsGet](#contactsGet)
  * [contactsGetAttributes](#contactsGetAttributes)
  * [contactsGetById](#contactsGetById)
  * [contactsGetCount](#contactsGetCount)
  * [contactsGetIds](#contactsGetIds)
  * [pickContact](#pickContact)
  * [pickPhone](#pickPhone)
  * [queryAttributes](#queryAttributes)
  * [queryContent](#queryContent)
**EventFacade**
  * [eventClearBuffer](#eventClearBuffer)
  * [eventGetBrodcastCategories](#eventGetBrodcastCategories)
  * [eventPoll](#eventPoll)
  * [eventPost](#eventPost)
  * [eventRegisterForBroadcast](#eventRegisterForBroadcast)
  * [eventUnregisterForBroadcast](#eventUnregisterForBroadcast)
  * [eventWait](#eventWait)
  * [eventWaitFor](#eventWaitFor)
  * [postEvent](#postEvent)
  * [receiveEvent](#receiveEvent)
  * [startEventDispatcher](#startEventDispatcher)
  * [stopEventDispatcher](#stopEventDispatcher)
  * [waitForEvent](#waitForEvent)
**LocationFacade**
  * [geocode](#geocode)
  * [getLastKnownLocation](#getLastKnownLocation)
  * [locationProviderEnabled](#locationProviderEnabled)
  * [locationProviders](#locationProviders)
  * [readLocation](#readLocation)
  * [startLocating](#startLocating)
  * [stopLocating](#stopLocating)
**MediaPlayerFacade**
  * [mediaIsPlaying](#mediaIsPlaying)
  * [mediaPlay](#mediaPlay)
  * [mediaPlayClose](#mediaPlayClose)
  * [mediaPlayInfo](#mediaPlayInfo)
  * [mediaPlayList](#mediaPlayList)
  * [mediaPlayPause](#mediaPlayPause)
  * [mediaPlaySeek](#mediaPlaySeek)
  * [mediaPlaySetLooping](#mediaPlaySetLooping)
  * [mediaPlayStart](#mediaPlayStart)
**MediaRecorderFacade**
  * [recorderCaptureVideo](#recorderCaptureVideo)
  * [recorderStartMicrophone](#recorderStartMicrophone)
  * [recorderStartVideo](#recorderStartVideo)
  * [recorderStop](#recorderStop)
  * [startInteractiveVideoRecording](#startInteractiveVideoRecording)
**PhoneFacade**
  * [checkNetworkRoaming](#checkNetworkRoaming)
  * [getCellLocation](#getCellLocation)
  * [getDeviceId](#getDeviceId)
  * [getDeviceSoftwareVersion](#getDeviceSoftwareVersion)
  * [getLine1Number](#getLine1Number)
  * [getNeighboringCellInfo](#getNeighboringCellInfo)
  * [getNetworkOperator](#getNetworkOperator)
  * [getNetworkOperatorName](#getNetworkOperatorName)
  * [getNetworkType](#getNetworkType)
  * [getPhoneType](#getPhoneType)
  * [getSimCountryIso](#getSimCountryIso)
  * [getSimOperator](#getSimOperator)
  * [getSimOperatorName](#getSimOperatorName)
  * [getSimSerialNumber](#getSimSerialNumber)
  * [getSimState](#getSimState)
  * [getSubscriberId](#getSubscriberId)
  * [getVoiceMailAlphaTag](#getVoiceMailAlphaTag)
  * [getVoiceMailNumber](#getVoiceMailNumber)
  * [phoneCall](#phoneCall)
  * [phoneCallNumber](#phoneCallNumber)
  * [phoneDial](#phoneDial)
  * [phoneDialNumber](#phoneDialNumber)
  * [readPhoneState](#readPhoneState)
  * [startTrackingPhoneState](#startTrackingPhoneState)
  * [stopTrackingPhoneState](#stopTrackingPhoneState)
**PreferencesFacade**
  * [prefGetAll](#prefGetAll)
  * [prefGetValue](#prefGetValue)
  * [prefPutValue](#prefPutValue)
**SensorManagerFacade**
  * [readSensors](#readSensors)
  * [sensorsGetAccuracy](#sensorsGetAccuracy)
  * [sensorsGetLight](#sensorsGetLight)
  * [sensorsReadAccelerometer](#sensorsReadAccelerometer)
  * [sensorsReadMagnetometer](#sensorsReadMagnetometer)
  * [sensorsReadOrientation](#sensorsReadOrientation)
  * [startSensing](#startSensing)
  * [startSensingThreshold](#startSensingThreshold)
  * [startSensingTimed](#startSensingTimed)
  * [stopSensing](#stopSensing)
**SettingsFacade**
  * [checkAirplaneMode](#checkAirplaneMode)
  * [checkRingerSilentMode](#checkRingerSilentMode)
  * [checkScreenOn](#checkScreenOn)
  * [getMaxMediaVolume](#getMaxMediaVolume)
  * [getMaxRingerVolume](#getMaxRingerVolume)
  * [getMediaVolume](#getMediaVolume)
  * [getRingerVolume](#getRingerVolume)
  * [getScreenBrightness](#getScreenBrightness)
  * [getScreenTimeout](#getScreenTimeout)
  * [getVibrateMode](#getVibrateMode)
  * [setMediaVolume](#setMediaVolume)
  * [setRingerVolume](#setRingerVolume)
  * [setScreenBrightness](#setScreenBrightness)
  * [setScreenTimeout](#setScreenTimeout)
  * [toggleAirplaneMode](#toggleAirplaneMode)
  * [toggleRingerSilentMode](#toggleRingerSilentMode)
  * [toggleVibrateMode](#toggleVibrateMode)
**SignalStrengthFacade** Requires API Level 7.
  * [readSignalStrengths](#readSignalStrengths)
  * [startTrackingSignalStrengths](#startTrackingSignalStrengths)
  * [stopTrackingSignalStrengths](#stopTrackingSignalStrengths)
**SmsFacade**
  * [smsDeleteMessage](#smsDeleteMessage)
  * [smsGetAttributes](#smsGetAttributes)
  * [smsGetMessageById](#smsGetMessageById)
  * [smsGetMessageCount](#smsGetMessageCount)
  * [smsGetMessageIds](#smsGetMessageIds)
  * [smsGetMessages](#smsGetMessages)
  * [smsMarkMessageRead](#smsMarkMessageRead)
  * [smsSend](#smsSend)
**SpeechRecognitionFacade**
  * [recognizeSpeech](#recognizeSpeech)
**TextToSpeechFacade** Requires API Level 4.
  * [ttsIsSpeaking](#ttsIsSpeaking)
  * [ttsSpeak](#ttsSpeak)
**ToneGeneratorFacade**
  * [generateDtmfTones](#generateDtmfTones)
**UiFacade**
  * [addContextMenuItem](#addContextMenuItem)
  * [addOptionsMenuItem](#addOptionsMenuItem)
  * [clearContextMenu](#clearContextMenu)
  * [clearOptionsMenu](#clearOptionsMenu)
  * [dialogCreateAlert](#dialogCreateAlert)
  * [dialogCreateDatePicker](#dialogCreateDatePicker)
  * [dialogCreateHorizontalProgress](#dialogCreateHorizontalProgress)
  * [dialogCreateInput](#dialogCreateInput)
  * [dialogCreatePassword](#dialogCreatePassword)
  * [dialogCreateSeekBar](#dialogCreateSeekBar)
  * [dialogCreateSpinnerProgress](#dialogCreateSpinnerProgress)
  * [dialogCreateTimePicker](#dialogCreateTimePicker)
  * [dialogDismiss](#dialogDismiss)
  * [dialogGetInput](#dialogGetInput)
  * [dialogGetPassword](#dialogGetPassword)
  * [dialogGetResponse](#dialogGetResponse)
  * [dialogGetSelectedItems](#dialogGetSelectedItems)
  * [dialogSetCurrentProgress](#dialogSetCurrentProgress)
  * [dialogSetItems](#dialogSetItems)
  * [dialogSetMaxProgress](#dialogSetMaxProgress)
  * [dialogSetMultiChoiceItems](#dialogSetMultiChoiceItems)
  * [dialogSetNegativeButtonText](#dialogSetNegativeButtonText)
  * [dialogSetNeutralButtonText](#dialogSetNeutralButtonText)
  * [dialogSetPositiveButtonText](#dialogSetPositiveButtonText)
  * [dialogSetSingleChoiceItems](#dialogSetSingleChoiceItems)
  * [dialogShow](#dialogShow)
  * [fullDismiss](#fullDismiss)
  * [fullKeyOverride](#fullKeyOverride)
  * [fullQuery](#fullQuery)
  * [fullQueryDetail](#fullQueryDetail)
  * [fullSetList](#fullSetList)
  * [fullSetProperty](#fullSetProperty)
  * [fullSetTitle](#fullSetTitle)
  * [fullShow](#fullShow)
  * [webViewShow](#webViewShow)
**WakeLockFacade**
  * [wakeLockAcquireBright](#wakeLockAcquireBright)
  * [wakeLockAcquireDim](#wakeLockAcquireDim)
  * [wakeLockAcquireFull](#wakeLockAcquireFull)
  * [wakeLockAcquirePartial](#wakeLockAcquirePartial)
  * [wakeLockRelease](#wakeLockRelease)
**WebCamFacade** Requires API Level 8.
  * [cameraStartPreview](#cameraStartPreview)
  * [cameraStopPreview](#cameraStopPreview)
  * [webcamAdjustQuality](#webcamAdjustQuality)
  * [webcamStart](#webcamStart)
  * [webcamStop](#webcamStop)
**WifiFacade**
  * [checkWifiState](#checkWifiState)
  * [toggleWifiState](#toggleWifiState)
  * [wifiDisconnect](#wifiDisconnect)
  * [wifiGetConnectionInfo](#wifiGetConnectionInfo)
  * [wifiGetScanResults](#wifiGetScanResults)
  * [wifiLockAcquireFull](#wifiLockAcquireFull)
  * [wifiLockAcquireScanOnly](#wifiLockAcquireScanOnly)
  * [wifiLockRelease](#wifiLockRelease)
  * [wifiReassociate](#wifiReassociate)
  * [wifiReconnect](#wifiReconnect)
  * [wifiStartScan](#wifiStartScan)


### <sub>addContextMenuItem</sub> ###
```
addContextMenuItem(
 String label: label for this menu item,
 String event: event that will be generated on menu item click,
 Object eventData[optional])

Adds a new item to context menu.
```

### <sub>addOptionsMenuItem</sub> ###
```
addOptionsMenuItem(
 String label: label for this menu item,
 String event: event that will be generated on menu item click,
 Object eventData[optional],
 String iconName[optional]: Android system menu icon, see                       
http://developer.android.com/reference/android/R.drawable.html)

Adds a new item to options menu.
```

### <sub>batteryCheckPresent</sub> ###
```
batteryCheckPresent()

Returns the most recently received battery presence data.

Requires API Level 5.
```

### <sub>batteryGetHealth</sub> ###
```
batteryGetHealth()

Returns the most recently received battery health data:
1 - unknown;
2 - good;
3 - overheat;
4 - dead;
5 - over voltage;
6 - unspecified failure;
```

### <sub>batteryGetLevel</sub> ###
```
batteryGetLevel()

Returns the most recently received battery level (percentage).

Requires API Level 5.
```

### <sub>batteryGetPlugType</sub> ###
```
batteryGetPlugType()

Returns the most recently received plug type data:
-1 - unknown
0 - unplugged;
1 - power source is an AC charger
2 - power source is a USB port
```

### <sub>batteryGetStatus</sub> ###
```
batteryGetStatus()

Returns  the most recently received battery status data:
1 - unknown;
2 - charging;
3 - discharging;
4 - not charging;
5 - full;
```

### <sub>batteryGetTechnology</sub> ###
```
batteryGetTechnology()

Returns the most recently received battery technology data.

Requires API Level 5.
```

### <sub>batteryGetTemperature</sub> ###
```
batteryGetTemperature()

Returns the most recently received battery temperature.

Requires API Level 5.
```

### <sub>batteryGetVoltage</sub> ###
```
batteryGetVoltage()

Returns the most recently received battery voltage.

Requires API Level 5.
```

### <sub>batteryStartMonitoring</sub> ###
```
batteryStartMonitoring()

Starts tracking battery state.

Generates "battery" events.
```

### <sub>batteryStopMonitoring</sub> ###
```
batteryStopMonitoring()

Stops tracking battery state.
```

### <sub>bluetoothAccept</sub> ###
```
bluetoothAccept(
 String uuid[optional, default 457807c0-4897-11df-9879-0800200c9a66],
 Integer timeout[optional, default 0]: How long to wait for a new connection, 0 
is wait for ever)

Listens for and accepts a Bluetooth connection. Blocks until the connection is  
established or fails.

Requires API Level 5.
```

### <sub>bluetoothActiveConnections</sub> ###
```
bluetoothActiveConnections()

Returns active Bluetooth connections.

Requires API Level 5.
```

### <sub>bluetoothConnect</sub> ###
```
bluetoothConnect(
 String uuid[optional, default 457807c0-4897-11df-9879-0800200c9a66]: The UUID  
passed here must match the UUID used by the server device.,
 String address[optional]: The user will be presented with a list of discovered 
devices to choose from if an address is not provided.)

Connect to a device over Bluetooth. Blocks until the connection is established  
or fails.

Returns:
  True if the connection was established successfully.

Requires API Level 5.
```

### <sub>bluetoothDiscoveryCancel</sub> ###
```
bluetoothDiscoveryCancel()

Cancel the current device discovery process.

Returns:
  true on success, false on error

Requires API Level 5.
```

### <sub>bluetoothDiscoveryStart</sub> ###
```
bluetoothDiscoveryStart()

Start the remote device discovery process. 

Returns:
  true on success, false on error

Requires API Level 5.
```

### <sub>bluetoothGetConnectedDeviceName</sub> ###
```
bluetoothGetConnectedDeviceName(
 String connID[optional, default null]: Connection id)

Returns the name of the connected device.

Requires API Level 5.
```

### <sub>bluetoothGetLocalAddress</sub> ###
```
bluetoothGetLocalAddress()

Returns the hardware address of the local Bluetooth adapter. 

Requires API Level 5.
```

### <sub>bluetoothGetLocalName</sub> ###
```
bluetoothGetLocalName()

Gets the Bluetooth Visible device name

Requires API Level 5.
```

### <sub>bluetoothGetRemoteDeviceName</sub> ###
```
bluetoothGetRemoteDeviceName(
 String address: Bluetooth Address For Target Device)

Queries a remote device for it's name or null if it can't be resolved

Requires API Level 5.
```

### <sub>bluetoothGetScanMode</sub> ###
```
bluetoothGetScanMode()

Gets the scan mode for the local dongle.
Return values:
-1 when Bluetooth is disabled.
0 if non discoverable and non connectable.
1 connectable non discoverable.
3 connectable and discoverable.

Requires API Level 5.
```

### <sub>bluetoothIsDiscovering</sub> ###
```
bluetoothIsDiscovering()

Return true if the local Bluetooth adapter is currently in the device discovery 
process. 

Requires API Level 5.
```

### <sub>bluetoothMakeDiscoverable</sub> ###
```
bluetoothMakeDiscoverable(
 Integer duration[optional, default 300]: period of time, in seconds, during    
which the device should be discoverable)

Requests that the device be discoverable for Bluetooth connections.

Requires API Level 5.
```

### <sub>bluetoothRead</sub> ###
```
bluetoothRead(
 Integer bufferSize[optional, default 4096],
 String connID[optional, default null]: Connection id)

Read up to bufferSize ASCII characters.

Requires API Level 5.
```

### <sub>bluetoothReadBinary</sub> ###
```
bluetoothReadBinary(
 Integer bufferSize[optional, default 4096],
 String connID[optional, default ]: Connection id)

Read up to bufferSize bytes and return a chunked, base64 encoded string.

Requires API Level 5.
```

### <sub>bluetoothReadLine</sub> ###
```
bluetoothReadLine(
 String connID[optional, default null]: Connection id)

Read the next line.

Requires API Level 5.
```

### <sub>bluetoothReadReady</sub> ###
```
bluetoothReadReady(
 String connID[optional, default ]: Connection id)

Returns True if the next read is guaranteed not to block.

Requires API Level 5.
```

### <sub>bluetoothSetLocalName</sub> ###
```
bluetoothSetLocalName(
 String name: New local name)

Sets the Bluetooth Visible device name, returns True on success

Requires API Level 5.
```

### <sub>bluetoothStop</sub> ###
```
bluetoothStop(
 String connID[optional, default null]: Connection id)

Stops Bluetooth connection.

Requires API Level 5.
```

### <sub>bluetoothWrite</sub> ###
```
bluetoothWrite(
 String ascii,
 String connID[optional, default ]: Connection id)

Sends ASCII characters over the currently open Bluetooth connection.

Requires API Level 5.
```

### <sub>bluetoothWriteBinary</sub> ###
```
bluetoothWriteBinary(
 String base64: A base64 encoded String of the bytes to be sent.,
 String connID[optional, default ]: Connection id)

Send bytes over the currently open Bluetooth connection.

Requires API Level 5.
```

### <sub>cameraCapturePicture</sub> ###
```
cameraCapturePicture(
 String targetPath,
 Boolean useAutoFocus[optional, default true])

Take a picture and save it to the specified path.

Returns:
 A map of Booleans autoFocus and takePicture where True indicates success.
```

### <sub>cameraInteractiveCapturePicture</sub> ###
```
cameraInteractiveCapturePicture(
 String targetPath)

Starts the image capture application to take a picture and saves it to the      
specified path.
```

### <sub>cameraStartPreview</sub> ###
```
cameraStartPreview(
 Integer resolutionLevel[optional, default 0]: increasing this number provides  
higher resolution,
 Integer jpegQuality[optional, default 20]: a number from 0-100,
 String filepath[optional]: Path to store jpeg files.)

Start Preview Mode. Throws 'preview' events.

Returns:
  True if successful

Requires API Level 8.
```

### <sub>cameraStopPreview</sub> ###
```
cameraStopPreview()

Stop the preview mode.

Requires API Level 8.
```

### <sub>checkAirplaneMode</sub> ###
```
checkAirplaneMode()

Checks the airplane mode setting.

Returns:
  True if airplane mode is enabled.
```

### <sub>checkBluetoothState</sub> ###
```
checkBluetoothState()

Checks Bluetooth state.

Returns:
  True if Bluetooth is enabled.

Requires API Level 5.
```

### <sub>checkNetworkRoaming</sub> ###
```
checkNetworkRoaming()

Returns true if the device is considered roaming on the current network, for    
GSM purposes.
```

### <sub>checkRingerSilentMode</sub> ###
```
checkRingerSilentMode()

Checks the ringer silent mode setting.

Returns:
  True if ringer silent mode is enabled.
```

### <sub>checkScreenOn</sub> ###
```
checkScreenOn()

Checks if the screen is on or off (requires API level 7).

Returns:
  True if the screen is currently on.
```

### <sub>checkWifiState</sub> ###
```
checkWifiState()

Checks Wifi state.

Returns:
  True if Wifi is enabled.
```

### <sub>clearContextMenu</sub> ###
```
clearContextMenu()

Removes all items previously added to context menu.
```

### <sub>clearOptionsMenu</sub> ###
```
clearOptionsMenu()

Removes all items previously added to options menu.
```

### <sub>contactsGet</sub> ###
```
contactsGet(
 JSONArray attributes[optional])

Returns a List of all contacts.

Returns:
  a List of contacts as Maps
```

### <sub>contactsGetAttributes</sub> ###
```
contactsGetAttributes()

Returns a List of all possible attributes for contacts.
```

### <sub>contactsGetById</sub> ###
```
contactsGetById(
 Integer id,
  JSONArray attributes[optional])

Returns contacts by ID.
```

### <sub>contactsGetCount</sub> ###
```
contactsGetCount()

Returns the number of contacts.
```

### <sub>contactsGetIds</sub> ###
```
contactsGetIds()

Returns a List of all contact IDs.
```

### <sub>dialogCreateAlert</sub> ###
```
dialogCreateAlert(
 String title[optional],
  String message[optional])

Create alert dialog.
```

### <sub>dialogCreateDatePicker</sub> ###
```
dialogCreateDatePicker(
 Integer year[optional, default 1970],
 Integer month[optional, default 1],
 Integer day[optional, default 1])

Create date picker dialog.
```

### <sub>dialogCreateHorizontalProgress</sub> ###
```
dialogCreateHorizontalProgress(
 String title[optional],
 String message[optional],
 Integer maximum progress[optional, default 100])

Create a horizontal progress dialog.
```

### <sub>dialogCreateInput</sub> ###
```
dialogCreateInput(
 String title[optional, default Value]: title of the input box,
 String message[optional, default Please enter value:]: message to display      
above the input box,
 String defaultText[optional]: text to insert into the input box,
 String inputType[optional]: type of input data, ie number or text)

Create a text input dialog.
```

### <sub>dialogCreatePassword</sub> ###
```
dialogCreatePassword(
 String title[optional, default Password]: title of the input box,
 String message[optional, default Please enter password:]: message to display   
above the input box)

Create a password input dialog.
```

### <sub>dialogCreateSeekBar</sub> ###
```
dialogCreateSeekBar(
 Integer starting value[optional, default 50],
 Integer maximum value[optional, default 100],
 String title,
  String message)

Create seek bar dialog.
```

### <sub>dialogCreateSpinnerProgress</sub> ###
```
dialogCreateSpinnerProgress(
 String title[optional],
 String message[optional],
 Integer maximum progress[optional, default 100])

Create a spinner progress dialog.
```

### <sub>dialogCreateTimePicker</sub> ###
```
dialogCreateTimePicker(
 Integer hour[optional, default 0],
 Integer minute[optional, default 0],
 Boolean is24hour[optional, default false]: Use 24 hour clock)

Create time picker dialog.
```

### <sub>dialogDismiss</sub> ###
```
dialogDismiss()

Dismiss dialog.
```

### <sub>dialogGetInput</sub> ###
```
dialogGetInput(
 String title[optional, default Value]: title of the input box,
 String message[optional, default Please enter value:]: message to display      
above the input box,
 String defaultText[optional]: text to insert into the input box)

Queries the user for a text input.
```

### <sub>dialogGetPassword</sub> ###
```
dialogGetPassword(
 String title[optional, default Password]: title of the password box,
 String message[optional, default Please enter password:]: message to display   
above the input box)

Queries the user for a password.
```

### <sub>dialogGetResponse</sub> ###
```
dialogGetResponse()

Returns dialog response.
```

### <sub>dialogGetSelectedItems</sub> ###
```
dialogGetSelectedItems()

This method provides list of items user selected.

Returns:
  Selected items
```

### <sub>dialogSetCurrentProgress</sub> ###
```
dialogSetCurrentProgress(
 Integer current)

Set progress dialog current value.
```

### <sub>dialogSetItems</sub> ###
```
dialogSetItems(
  JSONArray items)

Set alert dialog list items.
```

### <sub>dialogSetMaxProgress</sub> ###
```
dialogSetMaxProgress(
  Integer max)

Set progress dialog maximum value.
```

### <sub>dialogSetMultiChoiceItems</sub> ###
```
dialogSetMultiChoiceItems(
 JSONArray items,
 JSONArray selected[optional]: list of selected items)

Set dialog multiple choice items and selection.
```

### <sub>dialogSetNegativeButtonText</sub> ###
```
dialogSetNegativeButtonText(
  String text)

Set alert dialog button text.
```

### <sub>dialogSetNeutralButtonText</sub> ###
```
dialogSetNeutralButtonText(
  String text)

Set alert dialog button text.
```

### <sub>dialogSetPositiveButtonText</sub> ###
```
dialogSetPositiveButtonText(
 String text)

Set alert dialog positive button text.
```

### <sub>dialogSetSingleChoiceItems</sub> ###
```
dialogSetSingleChoiceItems(
 JSONArray items,
 Integer selected[optional, default 0]: selected item index)

Set dialog single choice items and selected item.
```

### <sub>dialogShow</sub> ###
```
dialogShow()

Show dialog.
```

### <sub>environment</sub> ###
```
environment()

A map of various useful environment details
```

### <sub>eventClearBuffer</sub> ###
```
eventClearBuffer()

Clears all events from the event buffer.
```

### <sub>eventGetBrodcastCategories</sub> ###
```
eventGetBrodcastCategories()

Lists all the broadcast signals we are listening for
```

### <sub>eventPoll</sub> ###
```
eventPoll(
 Integer number_of_events[optional, default 1])

Returns and removes the oldest n events (i.e. location or sensor update, etc.)  
from the event buffer.

Returns:
  A List of Maps of event properties.
```

### <sub>eventPost</sub> ###
```
eventPost(
 String name: Name of event,
 String data: Data contained in event.,
 Boolean enqueue[optional, default null]: Set to False if you don't want your   
events to be added to the event queue, just dispatched.)

Post an event to the event queue.
```

### <sub>eventRegisterForBroadcast</sub> ###
```
eventRegisterForBroadcast(
 String category,
 Boolean enqueue[optional, default true]: Should this events be added to the    
event queue or only dispatched)

Registers a listener for a new broadcast signal
```

### <sub>eventUnregisterForBroadcast</sub> ###
```
eventUnregisterForBroadcast(
 String category)

Stop listening for a broadcast signal
```

### <sub>eventWait</sub> ###
```
eventWait(
 Integer timeout[optional]: the maximum time to wait)

Blocks until an event occurs. The returned event is removed from the buffer.

Returns:
  Map of event properties.
```

### <sub>eventWaitFor</sub> ###
```
eventWaitFor(
 String eventName,
 Integer timeout[optional]: the maximum time to wait (in ms))

Blocks until an event with the supplied name occurs. The returned event is not  
removed from the buffer.

Returns:
  Map of event properties.
```

### <sub>forceStopPackage</sub> ###
```
forceStopPackage(
 String packageName: name of package)

Force stops a package.
```

### <sub>fullDismiss</sub> ###
```
fullDismiss()

Dismiss Full Screen.
```

### <sub>fullKeyOverride</sub> ###
```
fullKeyOverride(
 JSONArray keycodes: List of keycodes to override,
 Boolean enable[optional, default true]: Turn overriding or off)

Override default key actions
```

### <sub>fullQuery</sub> ###
```
fullQuery()

Get Fullscreen Properties
```

### <sub>fullQueryDetail</sub> ###
```
fullQueryDetail(
 String id: id of layout widget)

Get fullscreen properties for a specific widget
```

### <sub>fullSetList</sub> ###
```
fullSetList(
 String id: id of layout widget,
 JSONArray list: List to set)

Attach a list to a fullscreen widget
```

### <sub>fullSetProperty</sub> ###
```
fullSetProperty(
 String id: id of layout widget,
 String property: name of property to set,
 String value: value to set property to)

Set fullscreen widget property
```

### <sub>fullSetTitle</sub> ###
```
fullSetTitle(
 String title: Activity Title)

Set the Full Screen Activity Title
```

### <sub>fullShow</sub> ###
```
fullShow(
 String layout: String containing View layout,
 String title[optional]: Activity Title)

Show Full Screen.
```

### <sub>generateDtmfTones</sub> ###
```
generateDtmfTones(
 String phoneNumber,
 Integer toneDuration[optional, default 100]: duration of each tone in          
milliseconds)

Generate DTMF tones for the given phone number.
```

### <sub>geocode</sub> ###
```
geocode(
 Double latitude,
 Double longitude,
 Integer maxResults[optional, default 1]: maximum number of results)

Returns a list of addresses for the given latitude and longitude.

Returns:
  A list of addresses.
```

### <sub>getCellLocation</sub> ###
```
getCellLocation()

Returns the current cell location.
```

### <sub>getClipboard</sub> ###
```
getClipboard()

Read text from the clipboard.

Returns:
  The text in the clipboard.
```

### <sub>getConstants</sub> ###
```
getConstants(
 String classname: Class to get constants from)

Get list of constants (static final fields) for a class
```

### <sub>getDeviceId</sub> ###
```
getDeviceId()

Returns the unique device ID, for example, the IMEI for GSM and the MEID for    
CDMA phones. Return null if device ID is not available.
```

### <sub>getDeviceSoftwareVersion</sub> ###
```
getDeviceSoftwareVersion()

Returns the software version number for the device, for example, the IMEI/SV    
for GSM phones. Return null if the software version is not available.
```

### <sub>getInput</sub> ###
```
getInput(
 String title[optional, default SL4A Input]: title of the input box,
 String message[optional, default Please enter value:]: message to display      
above the input box)

Queries the user for a text input.

Deprecated in r3! Please use dialogGetInput instead.
```

### <sub>getIntent</sub> ###
```
getIntent()

Returns the intent that launched the script.
```

### <sub>getLastKnownLocation</sub> ###
```
getLastKnownLocation()

Returns the last known location of the device.

Returns:
  A map of location information by provider.
```

### <sub>getLaunchableApplications</sub> ###
```
getLaunchableApplications()

Returns a list of all launchable application class names.
```

### <sub>getLine1Number</sub> ###
```
getLine1Number()

Returns the phone number string for line 1, for example, the MSISDN for a GSM   
phone. Return null if it is unavailable.
```

### <sub>getMaxMediaVolume</sub> ###
```
getMaxMediaVolume()

Returns the maximum media volume.
```

### <sub>getMaxRingerVolume</sub> ###
```
getMaxRingerVolume()

Returns the maximum ringer volume.
```

### <sub>getMediaVolume</sub> ###
```
getMediaVolume()

Returns the current media volume.
```

### <sub>getNeighboringCellInfo</sub> ###
```
getNeighboringCellInfo()

Returns the neighboring cell information of the device.
```

### <sub>getNetworkOperator</sub> ###
```
getNetworkOperator()

Returns the numeric name (MCC+MNC) of current registered operator.
```

### <sub>getNetworkOperatorName</sub> ###
```
getNetworkOperatorName()

Returns the alphabetic name of current registered operator.
```

### <sub>getNetworkType</sub> ###
```
getNetworkType()

Returns a the radio technology (network type) currently in use on the device.
```

### <sub>getPackageVersion</sub> ###
```
getPackageVersion(
  String packageName)

Returns package version name.
```

### <sub>getPackageVersionCode</sub> ###
```
getPackageVersionCode(
  String packageName)

Returns package version code.
```

### <sub>getPassword</sub> ###
```
getPassword(
 String title[optional, default SL4A Password Input]: title of the input box,
 String message[optional, default Please enter password:]: message to display   
above the input box)

Queries the user for a password.

Deprecated in r3! Please use dialogGetPassword instead.
```

### <sub>getPhoneType</sub> ###
```
getPhoneType()

Returns the device phone type.
```

### <sub>getRingerVolume</sub> ###
```
getRingerVolume()

Returns the current ringer volume.
```

### <sub>getRunningPackages</sub> ###
```
getRunningPackages()

Returns a list of packages running activities or services.

Returns:
  List of packages running activities.
```

### <sub>getScreenBrightness</sub> ###
```
getScreenBrightness()

Returns the screen backlight brightness.

Returns:
  the current screen brightness between 0 and 255
```

### <sub>getScreenTimeout</sub> ###
```
getScreenTimeout()

Returns the current screen timeout in seconds.

Returns:
  the current screen timeout in seconds.
```

### <sub>getSimCountryIso</sub> ###
```
getSimCountryIso()

Returns the ISO country code equivalent for the SIM provider's country code.
```

### <sub>getSimOperator</sub> ###
```
getSimOperator()

Returns the MCC+MNC (mobile country code + mobile network code) of the provider 
of the SIM. 5 or 6 decimal digits.
```

### <sub>getSimOperatorName</sub> ###
```
getSimOperatorName()

Returns the Service Provider Name (SPN).
```

### <sub>getSimSerialNumber</sub> ###
```
getSimSerialNumber()

Returns the serial number of the SIM, if applicable. Return null if it is       
unavailable.
```

### <sub>getSimState</sub> ###
```
getSimState()

Returns the state of the device SIM card.
```

### <sub>getSubscriberId</sub> ###
```
getSubscriberId()

Returns the unique subscriber ID, for example, the IMSI for a GSM phone. Return 
null if it is unavailable.
```

### <sub>getVibrateMode</sub> ###
```
getVibrateMode(
 Boolean ringer[optional])

Checks Vibration setting. If ringer=true then query Ringer setting, else query  
Notification setting

Returns:
  True if vibrate mode is enabled.
```

### <sub>getVoiceMailAlphaTag</sub> ###
```
getVoiceMailAlphaTag()

Retrieves the alphabetic identifier associated with the voice mail number.
```

### <sub>getVoiceMailNumber</sub> ###
```
getVoiceMailNumber()

Returns the voice mail number. Return null if it is unavailable.
```

### <sub>launch</sub> ###
```
launch(
  String className)

Start activity with the given class name.
```

### <sub>locationProviderEnabled</sub> ###
```
locationProviderEnabled(
 String provider: Name of location provider)

Ask if provider is enabled
```

### <sub>locationProviders</sub> ###
```
locationProviders()

Returns availables providers on the phone
```

### <sub>log</sub> ###
```
log(
  String message)

Writes message to logcat.
```

### <sub>makeIntent</sub> ###
```
makeIntent(
 String action,
 String uri[optional],
 String type[optional]: MIME type/subtype of the URI,
 JSONObject extras[optional]: a Map of extras to add to the Intent,
 JSONArray categories[optional]: a List of categories to add to the Intent,
 String packagename[optional]: name of package. If used, requires classname to  
be useful,
 String classname[optional]: name of class. If used, requires packagename to be 
useful,
 Integer flags[optional]: Intent flags)

Create an Intent.

Returns:
  An object representing an Intent
```

### <sub>makeToast</sub> ###
```
makeToast(
  String message)

Displays a short-duration Toast notification.
```

### <sub>mediaIsPlaying</sub> ###
```
mediaIsPlaying(
 String tag[optional, default default]: string identifying resource)

Checks if media file is playing.

Returns:
  true if playing
```

### <sub>mediaPlay</sub> ###
```
mediaPlay(
 String url: url of media resource,
 String tag[optional, default default]: string identifying resource,
 Boolean play[optional, default true]: start playing immediately)

Open a media file

Returns:
  true if play successful
```

### <sub>mediaPlayClose</sub> ###
```
mediaPlayClose(
 String tag[optional, default default]: string identifying resource)

Close media file

Returns:
  true if successful
```

### <sub>mediaPlayInfo</sub> ###
```
mediaPlayInfo(
 String tag[optional, default default]: string identifying resource)

Information on current media

Returns:
  Media Information
```

### <sub>mediaPlayList</sub> ###
```
mediaPlayList()

Lists currently loaded media

Returns:
  List of Media Tags
```

### <sub>mediaPlayPause</sub> ###
```
mediaPlayPause(
 String tag[optional, default default]: string identifying resource)

pause playing media file

Returns:
  true if successful
```

### <sub>mediaPlaySeek</sub> ###
```
mediaPlaySeek(
 Integer msec: Position in millseconds,
 String tag[optional, default default]: string identifying resource)

Seek To Position

Returns:
  New Position (in ms)
```

### <sub>mediaPlaySetLooping</sub> ###
```
mediaPlaySetLooping(
 Boolean enabled[optional, default true],
 String tag[optional, default default]: string identifying resource)

Set Looping

Returns:
  True if successful
```

### <sub>mediaPlayStart</sub> ###
```
mediaPlayStart(
 String tag[optional, default default]: string identifying resource)

start playing media file

Returns:
  true if successful
```

### <sub>notify</sub> ###
```
notify(
 String title: title,
 String message)

Displays a notification that will be canceled when the user clicks on it.
```

### <sub>phoneCall</sub> ###
```
phoneCall(
  String uri)

Calls a contact/phone number by URI.
```

### <sub>phoneCallNumber</sub> ###
```
phoneCallNumber(
  String phone number)

Calls a phone number.
```

### <sub>phoneDial</sub> ###
```
phoneDial(
  String uri)

Dials a contact/phone number by URI.
```

### <sub>phoneDialNumber</sub> ###
```
phoneDialNumber(
  String phone number)

Dials a phone number.
```

### <sub>pick</sub> ###
```
pick(
 String uri)

Display content to be picked by URI (e.g. contacts)

Returns:
  A map of result values.
```

### <sub>pickContact</sub> ###
```
pickContact()

Displays a list of contacts to pick from.

Returns:
  A map of result values.
```

### <sub>pickPhone</sub> ###
```
pickPhone()

Displays a list of phone numbers to pick from.

Returns:
  The selected phone number.
```

### <sub>postEvent</sub> ###
```
rpcPostEvent(
 String name,
 String data)

Post an event to the event queue.

Deprecated in r4! Please use eventPost instead.
```

### <sub>prefGetAll</sub> ###
```
prefGetAll(
 String filename[optional]: Desired preferences file. If not defined, uses the  
default Shared Preferences.)

Get list of Shared Preference Values

Returns:
  Map of key,value
```

### <sub>prefGetValue</sub> ###
```
prefGetValue(
 String key,
 String filename[optional]: Desired preferences file. If not defined, uses the  
default Shared Preferences.)

Read a value from shared preferences
```

### <sub>prefPutValue</sub> ###
```
prefPutValue(
 String key,
 Object value,
 String filename[optional]: Desired preferences file. If not defined, uses the  
default Shared Preferences.)

Write a value to shared preferences
```

### <sub>queryAttributes</sub> ###
```
queryAttributes(
 String uri: The URI, using the content:// scheme, for the content to           
retrieve.)

Content Resolver Query Attributes

Returns:
  a list of available columns for a given content uri
```

### <sub>queryContent</sub> ###
```
queryContent(
 String uri: The URI, using the content:// scheme, for the content to           
retrieve.,
 JSONArray attributes[optional]: A list of which columns to return. Passing     
null will return all columns,
 String selection[optional]: A filter declaring which rows to return,
 JSONArray selectionArgs[optional]: You may include ?s in selection, which will 
be replaced by the values from selectionArgs,
 String order[optional]: How to order the rows)

Content Resolver Query

Returns:
  result of query as Maps
```

### <sub>readBatteryData</sub> ###
```
readBatteryData()

Returns the most recently recorded battery data.
```

### <sub>readLocation</sub> ###
```
readLocation()

Returns the current location as indicated by all available providers.

Returns:
  A map of location information by provider.
```

### <sub>readPhoneState</sub> ###
```
readPhoneState()

Returns the current phone state and incoming number.

Returns:
  A Map of "state" and "incomingNumber"
```

### <sub>readSensors</sub> ###
```
readSensors()

Returns the most recently recorded sensor data.
```

### <sub>readSignalStrengths</sub> ###
```
readSignalStrengths()

Returns the current signal strengths.

Returns:
  A map of "gsm_signal_strength"

Requires API Level 7.
```

### <sub>receiveEvent</sub> ###
```
receiveEvent()

Returns and removes the oldest event (i.e. location or sensor update, etc.)     
from the event buffer.

Returns:
 Map of event properties.

Deprecated in r4! Please use eventPoll instead.
```

### <sub>recognizeSpeech</sub> ###
```
recognizeSpeech(
 String prompt[optional]: text prompt to show to the user when asking them to   
speak,
 String language[optional]: language override to inform the recognizer that it  
should expect speech in a language different than the one set in the            
java.util.Locale.getDefault(),
 String languageModel[optional]: informs the recognizer which speech model to   
prefer (see android.speech.RecognizeIntent))

Recognizes user's speech and returns the most likely result.

Returns:
  An empty string in case the speech cannot be recongnized.
```

### <sub>recorderCaptureVideo</sub> ###
```
recorderCaptureVideo(
 String targetPath,
 Integer duration[optional],
 Boolean recordAudio[optional, default true])

Records video (and optionally audio) from the camera and saves it to the given  
location. 
Duration specifies the maximum duration of the recording session. 
If duration is not provided this method will return immediately and the         
recording will only be stopped 
when recorderStop is called or when a scripts exits. 
Otherwise it will block for the time period equal to the duration argument.
```

### <sub>recorderStartMicrophone</sub> ###
```
recorderStartMicrophone(
 String targetPath)

Records audio from the microphone and saves it to the given location.
```

### <sub>recorderStartVideo</sub> ###
```
recorderStartVideo(
 String targetPath,
 Integer duration[optional, default 0],
 Integer videoSize[optional, default 1])

Records video from the camera and saves it to the given location. 
Duration specifies the maximum duration of the recording session. 
If duration is 0 this method will return and the recording will only be stopped 

when recorderStop is called or when a scripts exits. 
Otherwise it will block for the time period equal to the duration argument.
videoSize: 0=160x120, 1=320x240, 2=352x288, 3=640x480, 4=800x480.
```

### <sub>recorderStop</sub> ###
```
recorderStop()

Stops a previously started recording.
```

### <sub>requiredVersion</sub> ###
```
requiredVersion(
 Integer requiredVersion)

Checks if version of SL4A is greater than or equal to the specified version.
```

### <sub>scanBarcode</sub> ###
```
scanBarcode()

Starts the barcode scanner.

Returns:
  A Map representation of the result Intent.
```

### <sub>search</sub> ###
```
search(
  String query)

Starts a search for the given query.
```

### <sub>sendBroadcast</sub> ###
```
sendBroadcast(
 String action,
 String uri[optional],
 String type[optional]: MIME type/subtype of the URI,
 JSONObject extras[optional]: a Map of extras to add to the Intent,
 String packagename[optional]: name of package. If used, requires classname to  
be useful,
 String classname[optional]: name of class. If used, requires packagename to be 
useful)

Send a broadcast.
```

### <sub>sendBroadcastIntent</sub> ###
```
sendBroadcastIntent(
 Intent intent: Intent in the format as returned from makeIntent)

Send Broadcast Intent
```

### <sub>sendEmail</sub> ###
```
sendEmail(
 String to: A comma separated list of recipients.,
 String subject,
 String body,
 String attachmentUri[optional])

Launches an activity that sends an e-mail message to a given recipient.
```

### <sub>sensorsGetAccuracy</sub> ###
```
sensorsGetAccuracy()

Returns the most recently received accuracy value.
```

### <sub>sensorsGetLight</sub> ###
```
sensorsGetLight()

Returns the most recently received light value.
```

### <sub>sensorsReadAccelerometer</sub> ###
```
sensorsReadAccelerometer()

Returns the most recently received accelerometer values.

Returns:
  a List of Floats [(acceleration on the) X axis, Y axis, Z axis].
```

### <sub>sensorsReadMagnetometer</sub> ###
```
sensorsReadMagnetometer()

Returns the most recently received magnetic field values.

Returns:
 a List of Floats [(magnetic field value for) X axis, Y axis, Z axis].
```

### <sub>sensorsReadOrientation</sub> ###
```
sensorsReadOrientation()

Returns the most recently received orientation values.

Returns:
  a List of Doubles [azimuth, pitch, roll].
```

### <sub>setClipboard</sub> ###
```
setClipboard(
  String text)

Put text in the clipboard.
```

### <sub>setMediaVolume</sub> ###
```
setMediaVolume(
  Integer volume)

Sets the media volume.
```

### <sub>setResultBoolean</sub> ###
```
setResultBoolean(
 Integer resultCode: The result code to propagate back to the originating       
activity, often RESULT_CANCELED (0) or RESULT_OK (-1),
 Boolean resultValue)

Sets the result of a script execution. Whenever the script APK is called via    
startActivityForResult(), the resulting intent will contain SCRIPT_RESULT extra 
with the given value.
```

### <sub>setResultBooleanArray</sub> ###
```
setResultBooleanArray(
 Integer resultCode: The result code to propagate back to the originating       
activity, often RESULT_CANCELED (0) or RESULT_OK (-1),
 Boolean[] resultValue)

Sets the result of a script execution. Whenever the script APK is called via    
startActivityForResult(), the resulting intent will contain SCRIPT_RESULT extra 
with the given value.
```

### <sub>setResultByte</sub> ###
```
setResultByte(
 Integer resultCode: The result code to propagate back to the originating       
activity, often RESULT_CANCELED (0) or RESULT_OK (-1),
 Byte resultValue)

Sets the result of a script execution. Whenever the script APK is called via    
startActivityForResult(), the resulting intent will contain SCRIPT_RESULT extra 
with the given value.
```

### <sub>setResultByteArray</sub> ###
```
setResultByteArray(
 Integer resultCode: The result code to propagate back to the originating       
activity, often RESULT_CANCELED (0) or RESULT_OK (-1),
 Byte[] resultValue)

Sets the result of a script execution. Whenever the script APK is called via    
startActivityForResult(), the resulting intent will contain SCRIPT_RESULT extra 
with the given value.
```

### <sub>setResultChar</sub> ###
```
setResultChar(
 Integer resultCode: The result code to propagate back to the originating       
activity, often RESULT_CANCELED (0) or RESULT_OK (-1),
 Character resultValue)

Sets the result of a script execution. Whenever the script APK is called via    
startActivityForResult(), the resulting intent will contain SCRIPT_RESULT extra 
with the given value.
```

### <sub>setResultCharArray</sub> ###
```
setResultCharArray(
 Integer resultCode: The result code to propagate back to the originating       
activity, often RESULT_CANCELED (0) or RESULT_OK (-1),
 Character[] resultValue)

Sets the result of a script execution. Whenever the script APK is called via    
startActivityForResult(), the resulting intent will contain SCRIPT_RESULT extra 
with the given value.
```

### <sub>setResultDouble</sub> ###
```
setResultDouble(
 Integer resultCode: The result code to propagate back to the originating       
activity, often RESULT_CANCELED (0) or RESULT_OK (-1),
 Double resultValue)

Sets the result of a script execution. Whenever the script APK is called via    
startActivityForResult(), the resulting intent will contain SCRIPT_RESULT extra 
with the given value.
```

### <sub>setResultDoubleArray</sub> ###
```
setResultDoubleArray(
 Integer resultCode: The result code to propagate back to the originating       
activity, often RESULT_CANCELED (0) or RESULT_OK (-1),
 Double[] resultValue)

Sets the result of a script execution. Whenever the script APK is called via    
startActivityForResult(), the resulting intent will contain SCRIPT_RESULT extra 
with the given value.
```

### <sub>setResultFloat</sub> ###
```
setResultFloat(
 Integer resultCode: The result code to propagate back to the originating       
activity, often RESULT_CANCELED (0) or RESULT_OK (-1),
 Float resultValue)

Sets the result of a script execution. Whenever the script APK is called via    
startActivityForResult(), the resulting intent will contain SCRIPT_RESULT extra 
with the given value.
```

### <sub>setResultFloatArray</sub> ###
```
setResultFloatArray(
 Integer resultCode: The result code to propagate back to the originating       
activity, often RESULT_CANCELED (0) or RESULT_OK (-1),
 Float[] resultValue)

Sets the result of a script execution. Whenever the script APK is called via    
startActivityForResult(), the resulting intent will contain SCRIPT_RESULT extra 
with the given value.
```

### <sub>setResultInteger</sub> ###
```
setResultInteger(
 Integer resultCode: The result code to propagate back to the originating       
activity, often RESULT_CANCELED (0) or RESULT_OK (-1),
 Integer resultValue)

Sets the result of a script execution. Whenever the script APK is called via    
startActivityForResult(), the resulting intent will contain SCRIPT_RESULT extra 
with the given value.
```

### <sub>setResultIntegerArray</sub> ###
```
setResultIntegerArray(
 Integer resultCode: The result code to propagate back to the originating       
activity, often RESULT_CANCELED (0) or RESULT_OK (-1),
 Integer[] resultValue)

Sets the result of a script execution. Whenever the script APK is called via    
startActivityForResult(), the resulting intent will contain SCRIPT_RESULT extra 
with the given value.
```

### <sub>setResultLong</sub> ###
```
setResultLong(
 Integer resultCode: The result code to propagate back to the originating       
activity, often RESULT_CANCELED (0) or RESULT_OK (-1),
 Long resultValue)

Sets the result of a script execution. Whenever the script APK is called via    
startActivityForResult(), the resulting intent will contain SCRIPT_RESULT extra 
with the given value.
```

### <sub>setResultLongArray</sub> ###
```
setResultLongArray(
 Integer resultCode: The result code to propagate back to the originating       
activity, often RESULT_CANCELED (0) or RESULT_OK (-1),
 Long[] resultValue)

Sets the result of a script execution. Whenever the script APK is called via    
startActivityForResult(), the resulting intent will contain SCRIPT_RESULT extra 
with the given value.
```

### <sub>setResultSerializable</sub> ###
```
setResultSerializable(
 Integer resultCode: The result code to propagate back to the originating       
activity, often RESULT_CANCELED (0) or RESULT_OK (-1),
 Serializable resultValue)

Sets the result of a script execution. Whenever the script APK is called via    
startActivityForResult(), the resulting intent will contain SCRIPT_RESULT extra 
with the given value.
```

### <sub>setResultShort</sub> ###
```
setResultShort(
 Integer resultCode: The result code to propagate back to the originating       
activity, often RESULT_CANCELED (0) or RESULT_OK (-1),
 Short resultValue)

Sets the result of a script execution. Whenever the script APK is called via    
startActivityForResult(), the resulting intent will contain SCRIPT_RESULT extra 
with the given value.
```

### <sub>setResultShortArray</sub> ###
```
setResultShortArray(
 Integer resultCode: The result code to propagate back to the originating       
activity, often RESULT_CANCELED (0) or RESULT_OK (-1),
 Short[] resultValue)

Sets the result of a script execution. Whenever the script APK is called via    
startActivityForResult(), the resulting intent will contain SCRIPT_RESULT extra 
with the given value.
```

### <sub>setResultString</sub> ###
```
setResultString(
 Integer resultCode: The result code to propagate back to the originating       
activity, often RESULT_CANCELED (0) or RESULT_OK (-1),
 String resultValue)

Sets the result of a script execution. Whenever the script APK is called via    
startActivityForResult(), the resulting intent will contain SCRIPT_RESULT extra 
with the given value.
```

### <sub>setResultStringArray</sub> ###
```
setResultStringArray(
 Integer resultCode: The result code to propagate back to the originating       
activity, often RESULT_CANCELED (0) or RESULT_OK (-1),
 String[] resultValue)

Sets the result of a script execution. Whenever the script APK is called via    
startActivityForResult(), the resulting intent will contain SCRIPT_RESULT extra 
with the given value.
```

### <sub>setRingerVolume</sub> ###
```
setRingerVolume(
  Integer volume)

Sets the ringer volume.
```

### <sub>setScreenBrightness</sub> ###
```
setScreenBrightness(
 Integer value: brightness value between 0 and 255)

Sets the the screen backlight brightness.

Returns:
  the original screen brightness.
```

### <sub>setScreenTimeout</sub> ###
```
setScreenTimeout(
 Integer value)

Sets the screen timeout to this number of seconds.

Returns:
  The original screen timeout.
```

### <sub>smsDeleteMessage</sub> ###
```
smsDeleteMessage(
 Integer id)

Deletes a message.

Returns:
  True if the message was deleted
```

### <sub>smsGetAttributes</sub> ###
```
smsGetAttributes()

Returns a List of all possible message attributes.
```

### <sub>smsGetMessageById</sub> ###
```
smsGetMessageById(
 Integer id: message ID,
 JSONArray attributes[optional])

Returns message attributes.
```

### <sub>smsGetMessageCount</sub> ###
```
smsGetMessageCount(
 Boolean unreadOnly,
 String folder[optional, default inbox])

Returns the number of messages.
```

### <sub>smsGetMessageIds</sub> ###
```
smsGetMessageIds(
 Boolean unreadOnly,
 String folder[optional, default inbox])

Returns a List of all message IDs.
```

### <sub>smsGetMessages</sub> ###
```
smsGetMessages(
 Boolean unreadOnly,
 String folder[optional, default inbox],
 JSONArray attributes[optional])

Returns a List of all messages.

Returns:
  a List of messages as Maps
```

### <sub>smsMarkMessageRead</sub> ###
```
smsMarkMessageRead(
 JSONArray ids: List of message IDs to mark as read.,
 Boolean read)

Marks messages as read.

Returns:
  number of messages marked read
```

### <sub>smsSend</sub> ###
```
smsSend(
 String destinationAddress: typically a phone number,
 String text)

Sends an SMS.
```

### <sub>startActivity</sub> ###
```
startActivity(
 String action,
 String uri[optional],
 String type[optional]: MIME type/subtype of the URI,
 JSONObject extras[optional]: a Map of extras to add to the Intent,
 Boolean wait[optional]: block until the user exits the started activity,
 String packagename[optional]: name of package. If used, requires classname to  
be useful,
 String classname[optional]: name of class. If used, requires packagename to be 
useful)

Starts an activity.
```

### <sub>startActivityForResult</sub> ###
```
startActivityForResult(
 String action,
 String uri[optional],
 String type[optional]: MIME type/subtype of the URI,
 JSONObject extras[optional]: a Map of extras to add to the Intent,
 String packagename[optional]: name of package. If used, requires classname to  
be useful,
 String classname[optional]: name of class. If used, requires packagename to be 
useful)

Starts an activity and returns the result.

Returns:
  A Map representation of the result Intent.
```

### <sub>startActivityForResultIntent</sub> ###
```
startActivityForResultIntent(
 Intent intent: Intent in the format as returned from makeIntent)

Starts an activity and returns the result.

Returns:
  A Map representation of the result Intent.
```

### <sub>startActivityIntent</sub> ###
```
startActivityIntent(
 Intent intent: Intent in the format as returned from makeIntent,
 Boolean wait[optional]: block until the user exits the started activity)

Start Activity using Intent
```

### <sub>startEventDispatcher</sub> ###
```
startEventDispatcher(
 Integer port[optional, default 0]: Port to use)

Opens up a socket where you can read for events posted
```

### <sub>startInteractiveVideoRecording</sub> ###
```
startInteractiveVideoRecording(
 String path)

Starts the video capture application to record a video and saves it to the      
specified path.
```

### <sub>startLocating</sub> ###
```
startLocating(
 Integer minDistance[optional, default 60000]: minimum time between updates in  
milliseconds,
 Integer minUpdateDistance[optional, default 30]: minimum distance between      
updates in meters)

Starts collecting location data.

Generates "location" events.
```

### <sub>startSensing</sub> ###
```
startSensing(
 Integer sampleSize[optional, default 5]: number of samples for calculating     
average readings)

Starts recording sensor data to be available for polling.

Deprecated in 4! Please use startSensingTimed or startSensingThreshhold         
instead.
```

### <sub>startSensingThreshold</sub> ###
```
startSensingThreshold(
 Integer sensorNumber: 1 = Orientation, 2 = Accelerometer, 3 = Magnetometer and 
4 = Light,
 Integer threshold: Threshold level for chosen sensor (integer),
 Integer axis: 0 = No axis, 1 = X, 2 = Y, 3 = X+Y, 4 = Z, 5= X+Z, 6 = Y+Z, 7 =  
X+Y+Z)

Records to the Event Queue sensor data exceeding a chosen threshold.

Generates "threshold" events.
```

### <sub>startSensingTimed</sub> ###
```
startSensingTimed(
 Integer sensorNumber: 1 = All, 2 = Accelerometer, 3 = Magnetometer and 4 =     
Light,
 Integer delayTime: Minimum time between readings in milliseconds)

Starts recording sensor data to be available for polling.

Generates "sensors" events.
```

### <sub>startTrackingPhoneState</sub> ###
```
startTrackingPhoneState()

Starts tracking phone state.

Generates "phone" events.
```

### <sub>startTrackingSignalStrengths</sub> ###
```
startTrackingSignalStrengths()

Starts tracking signal strengths.

Generates "signal_strengths" events.

Requires API Level 7.
```

### <sub>stopEventDispatcher</sub> ###
```
stopEventDispatcher()

Stops the event server, you can't read in the port anymore
```

### <sub>stopLocating</sub> ###
```
stopLocating()

Stops collecting location data.
```

### <sub>stopSensing</sub> ###
```
stopSensing()

Stops collecting sensor data.
```

### <sub>stopTrackingPhoneState</sub> ###
```
stopTrackingPhoneState()

Stops tracking phone state.
```

### <sub>stopTrackingSignalStrengths</sub> ###
```
stopTrackingSignalStrengths()

Stops tracking signal strength.

Requires API Level 7.
```

### <sub>toggleAirplaneMode</sub> ###
```
toggleAirplaneMode(
 Boolean enabled[optional])

Toggles airplane mode on and off.

Returns:
  True if airplane mode is enabled.
```

### <sub>toggleBluetoothState</sub> ###
```
toggleBluetoothState(
 Boolean enabled[optional],
 Boolean prompt[optional, default true]: Prompt the user to confirm changing    
the Bluetooth state.)

Toggle Bluetooth on and off.

Returns:
  True if Bluetooth is enabled.

Requires API Level 5.
```

### <sub>toggleRingerSilentMode</sub> ###
```
toggleRingerSilentMode(
 Boolean enabled[optional])

Toggles ringer silent mode on and off.

Returns:
  True if ringer silent mode is enabled.
```

### <sub>toggleVibrateMode</sub> ###
```
toggleVibrateMode(
 Boolean enabled[optional],
 Boolean ringer[optional])

Toggles vibrate mode on and off. If ringer=true then set Ringer setting, else   
set Notification setting

Returns:
  True if vibrate mode is enabled.
```

### <sub>toggleWifiState</sub> ###
```
toggleWifiState(
 Boolean enabled[optional])

Toggle Wifi on and off.

Returns:
  True if Wifi is enabled.
```

### <sub>ttsIsSpeaking</sub> ###
```
ttsIsSpeaking()

Returns True if speech is currently in progress.

Requires API Level 4.
```

### <sub>ttsSpeak</sub> ###
```
ttsSpeak(
  String message)

Speaks the provided message via TTS.

Requires API Level 4.
```

### <sub>vibrate</sub> ###
```
vibrate(
 Integer duration[optional, default 300]: duration in milliseconds)

Vibrates the phone or a specified duration in milliseconds.
```

### <sub>view</sub> ###
```
view(
 String uri,
 String type[optional]: MIME type/subtype of the URI,
 JSONObject extras[optional]: a Map of extras to add to the Intent)

Start activity with view action by URI (i.e. browser, contacts, etc.).
```

### <sub>viewContacts</sub> ###
```
viewContacts()

Opens the list of contacts.
```

### <sub>viewHtml</sub> ###
```
viewHtml(
 String path: the path to the HTML file)

Opens the browser to display a local HTML file.
```

### <sub>viewMap</sub> ###
```
viewMap(
 String query, e.g. pizza, 123 My Street)

Opens a map search for query (e.g. pizza, 123 My Street).
```

### <sub>waitForEvent</sub> ###
```
waitForEvent(
 String eventName,
 Integer timeout[optional]: the maximum time to wait)

Blocks until an event with the supplied name occurs. The returned event is not  
removed from the buffer.

Returns:
 Map of event properties.

Deprecated in r4! Please use eventWaitFor instead.
```

### <sub>wakeLockAcquireBright</sub> ###
```
wakeLockAcquireBright()

Acquires a bright wake lock (CPU on, screen bright).
```

### <sub>wakeLockAcquireDim</sub> ###
```
wakeLockAcquireDim()

Acquires a dim wake lock (CPU on, screen dim).
```

### <sub>wakeLockAcquireFull</sub> ###
```
wakeLockAcquireFull()

Acquires a full wake lock (CPU on, screen bright, keyboard bright).
```

### <sub>wakeLockAcquirePartial</sub> ###
```
wakeLockAcquirePartial()

Acquires a partial wake lock (CPU on).
```

### <sub>wakeLockRelease</sub> ###
```
wakeLockRelease()

Releases the wake lock.
```

### <sub>webViewShow</sub> ###
```
webViewShow(
 String url,
 Boolean wait[optional]: block until the user exits the WebView)

Display a WebView with the given URL.
```

### <sub>webcamAdjustQuality</sub> ###
```
webcamAdjustQuality(
 Integer resolutionLevel[optional, default 0]: increasing this number provides  
higher resolution,
 Integer jpegQuality[optional, default 20]: a number from 0-100)

Adjusts the quality of the webcam stream while it is running.

Requires API Level 8.
```

### <sub>webcamStart</sub> ###
```
webcamStart(
 Integer resolutionLevel[optional, default 0]: increasing this number provides  
higher resolution,
 Integer jpegQuality[optional, default 20]: a number from 0-100,
 Integer port[optional, default 0]: If port is specified, the webcam service    
will bind to port, otherwise it will pick any available port.)

Starts an MJPEG stream and returns a Tuple of address and port for the stream.

Requires API Level 8.
```

### <sub>webcamStop</sub> ###
```
webcamStop()

Stops the webcam stream.

Requires API Level 8.
```

### <sub>wifiDisconnect</sub> ###
```
wifiDisconnect()

Disconnects from the currently active access point.

Returns:
  True if the operation succeeded.
```

### <sub>wifiGetConnectionInfo</sub> ###
```
wifiGetConnectionInfo()

Returns information about the currently active access point.
```

### <sub>wifiGetScanResults</sub> ###
```
wifiGetScanResults()

Returns the list of access points found during the most recent Wifi scan.
```

### <sub>wifiLockAcquireFull</sub> ###
```
wifiLockAcquireFull()

Acquires a full Wifi lock.
```

### <sub>wifiLockAcquireScanOnly</sub> ###
```
wifiLockAcquireScanOnly()

Acquires a scan only Wifi lock.
```

### <sub>wifiLockRelease</sub> ###
```
wifiLockRelease()

Releases a previously acquired Wifi lock.
```

### <sub>wifiReassociate</sub> ###
```
wifiReassociate()

Reassociates with the currently active access point.

Returns:
  True if the operation succeeded.
```

### <sub>wifiReconnect</sub> ###
```
wifiReconnect()

Reconnects to the currently active access point.

Returns:
  True if the operation succeeded.
```

### <sub>wifiStartScan</sub> ###
```
wifiStartScan()

Starts a scan for Wifi access points.

Returns:
  True if the scan was initiated successfully.
```

<!---
 vi: ft=markdown:et:fdm=marker
 -->
