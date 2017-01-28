Generated at commit `changeset:   1307:36da773df6bb`

**ActivityResultFacade**

  * [setResultBoolean](#setresultboolean)
  * [setResultByte](#setresultbyte)
  * [setResultShort](#setresultshort)
  * [setResultChar](#setresultchar)
  * [setResultInteger](#setresultinteger)
  * [setResultLong](#setresultlong)
  * [setResultFloat](#setresultfloat)
  * [setResultDouble](#setresultdouble)
  * [setResultString](#setresultstring)
  * [setResultBooleanArray](#setresultbooleanarray)
  * [setResultByteArray](#setresultbytearray)
  * [setResultShortArray](#setresultshortarray)
  * [setResultCharArray](#setresultchararray)
  * [setResultIntegerArray](#setresultintegerarray)
  * [setResultLongArray](#setresultlongarray)
  * [setResultFloatArray](#setresultfloatarray)
  * [setResultDoubleArray](#setresultdoublearray)
  * [setResultStringArray](#setresultstringarray)
  * [setResultSerializable](#setresultserializable)

**AndroidFacade**

  * [setClipboard](#setclipboard)
  * [getClipboard](#getclipboard)
  * [startActivityForResult](#startactivityforresult)
  * [startActivityForResultIntent](#startactivityforresultintent)
  * [startActivity](#startactivity)
  * [sendBroadcast](#sendbroadcast)
  * [makeIntent](#makeintent)
  * [startActivityIntent](#startactivityintent)
  * [sendBroadcastIntent](#sendbroadcastintent)
  * [vibrate](#vibrate)
  * [makeToast](#maketoast)
  * [getInput](#getinput)
  * [getPassword](#getpassword)
  * [notify](#notify)
  * [getIntent](#getintent)
  * [sendEmail](#sendemail)
  * [getPackageVersionCode](#getpackageversioncode)
  * [getPackageVersion](#getpackageversion)
  * [requiredVersion](#requiredversion)
  * [log](#log)
  * [environment](#environment)
  * [getConstants](#getconstants)

**ApplicationManagerFacade**

  * [getLaunchableApplications](#getlaunchableapplications)
  * [launch](#launch)
  * [getRunningPackages](#getrunningpackages)
  * [forceStopPackage](#forcestoppackage)

**BatteryManagerFacade**

  * [readBatteryData](#readbatterydata)
  * [batteryStartMonitoring](#batterystartmonitoring)
  * [batteryStopMonitoring](#batterystopmonitoring)
  * [batteryGetStatus](#batterygetstatus)
  * [batteryGetHealth](#batterygethealth)
  * [batteryCheckPresent](#batterycheckpresent)
  * [batteryGetLevel](#batterygetlevel)
  * [batteryGetVoltage](#batterygetvoltage)
  * [batteryGetTemperature](#batterygettemperature)
  * [batteryGetTechnology](#batterygettechnology)

**BluetoothFacade**

  * [bluetoothActiveConnections](#bluetoothactiveconnections)
  * [bluetoothWriteBinary](#bluetoothwritebinary)
  * [bluetoothReadBinary](#bluetoothreadbinary)
  * [bluetoothConnect](#bluetoothconnect)
  * [bluetoothAccept](#bluetoothaccept)
  * [bluetoothMakeDiscoverable](#bluetoothmakediscoverable)
  * [bluetoothWrite](#bluetoothwrite)
  * [bluetoothReadReady](#bluetoothreadready)
  * [bluetoothRead](#bluetoothread)
  * [bluetoothReadLine](#bluetoothreadline)
  * [bluetoothGetRemoteDeviceName](#bluetoothgetremotedevicename)
  * [bluetoothGetLocalName](#bluetoothgetlocalname)
  * [bluetoothSetLocalName](#bluetoothsetlocalname)
  * [bluetoothGetScanMode](#bluetoothgetscanmode)
  * [bluetoothGetConnectedDeviceName](#bluetoothgetconnecteddevicename)
  * [checkBluetoothState](#checkbluetoothstate)
  * [toggleBluetoothState](#togglebluetoothstate)
  * [bluetoothStop](#bluetoothstop)
  * [bluetoothGetLocalAddress](#bluetoothgetlocaladdress)
  * [bluetoothDiscoveryStart](#bluetoothdiscoverystart)
  * [bluetoothDiscoveryCancel](#bluetoothdiscoverycancel)
  * [bluetoothIsDiscovering](#bluetoothisdiscovering)

**CameraFacade**

  * [cameraCapturePicture](#cameracapturepicture)
  * [cameraInteractiveCapturePicture](#camerainteractivecapturepicture)
  * [camerasList](#cameraslist)

**CommonIntentsFacade**

  * [pick](#pick)
  * [scanBarcode](#scanbarcode)
  * [view](#view)
  * [viewMap](#viewmap)
  * [viewContacts](#viewcontacts)
  * [viewHtml](#viewhtml)
  * [search](#search)

**ContactsFacade**

  * [pickContact](#pickcontact)
  * [pickPhone](#pickphone)
  * [contactsGetAttributes](#contactsgetattributes)
  * [contactsGetIds](#contactsgetids)
  * [contactsGet](#contactsget)
  * [contactsGetById](#contactsgetbyid)
  * [contactsGetCount](#contactsgetcount)
  * [queryContent](#querycontent)
  * [queryAttributes](#queryattributes)

**EventFacade**

  * [eventClearBuffer](#eventclearbuffer)
  * [eventRegisterForBroadcast](#eventregisterforbroadcast)
  * [eventUnregisterForBroadcast](#eventunregisterforbroadcast)
  * [eventGetBrodcastCategories](#eventgetbrodcastcategories)
  * [eventPoll](#eventpoll)
  * [eventWaitFor](#eventwaitfor)
  * [eventWait](#eventwait)
  * [eventPost](#eventpost)
  * [rpcPostEvent](#rpcpostevent)
  * [receiveEvent](#receiveevent)
  * [waitForEvent](#waitforevent)
  * [startEventDispatcher](#starteventdispatcher)
  * [stopEventDispatcher](#stopeventdispatcher)

**EyesFreeFacade**

  * [ttsSpeak](#ttsspeak)

**LocationFacade**

  * [locationProviders](#locationproviders)
  * [locationProviderEnabled](#locationproviderenabled)
  * [startLocating](#startlocating)
  * [readLocation](#readlocation)
  * [stopLocating](#stoplocating)
  * [getLastKnownLocation](#getlastknownlocation)
  * [geocode](#geocode)

**MediaPlayerFacade**

  * [mediaPlay](#mediaplay)
  * [mediaPlayPause](#mediaplaypause)
  * [mediaPlayStart](#mediaplaystart)
  * [mediaPlayClose](#mediaplayclose)
  * [mediaIsPlaying](#mediaisplaying)
  * [mediaPlayInfo](#mediaplayinfo)
  * [mediaPlayList](#mediaplaylist)
  * [mediaPlaySetLooping](#mediaplaysetlooping)
  * [mediaPlaySeek](#mediaplayseek)

**MediaRecorderFacade**

  * [recorderStartMicrophone](#recorderstartmicrophone)
  * [recorderStartVideo](#recorderstartvideo)
  * [recorderCaptureVideo](#recordercapturevideo)
  * [recorderStop](#recorderstop)
  * [startInteractiveVideoRecording](#startinteractivevideorecording)

**NfcManagerFacade**

  * [nfcIsEnabled](#nfcisenabled)
  * [nfcStartTrackingStateChange](#nfcstarttrackingstatechange)
  * [nfcStopTrackingStateChange](#nfcstoptrackingstatechange)

**PhoneFacade**

  * [startTrackingPhoneState](#starttrackingphonestate)
  * [readPhoneState](#readphonestate)
  * [stopTrackingPhoneState](#stoptrackingphonestate)
  * [phoneCall](#phonecall)
  * [phoneCallNumber](#phonecallnumber)
  * [phoneDial](#phonedial)
  * [phoneDialNumber](#phonedialnumber)
  * [getCellLocation](#getcelllocation)
  * [getNetworkOperator](#getnetworkoperator)
  * [getNetworkOperatorName](#getnetworkoperatorname)
  * [getNetworkType](#getnetworktype)
  * [getPhoneType](#getphonetype)
  * [getSimCountryIso](#getsimcountryiso)
  * [getSimOperator](#getsimoperator)
  * [getSimOperatorName](#getsimoperatorname)
  * [getSimSerialNumber](#getsimserialnumber)
  * [getSimState](#getsimstate)
  * [getSubscriberId](#getsubscriberid)
  * [getVoiceMailAlphaTag](#getvoicemailalphatag)
  * [getVoiceMailNumber](#getvoicemailnumber)
  * [checkNetworkRoaming](#checknetworkroaming)
  * [getDeviceId](#getdeviceid)
  * [getDeviceSoftwareVersion](#getdevicesoftwareversion)
  * [getLine1Number](#getline1number)
  * [getNeighboringCellInfo](#getneighboringcellinfo)

**PreferencesFacade**

  * [prefGetValue](#prefgetvalue)
  * [prefPutValue](#prefputvalue)
  * [prefGetAll](#prefgetall)

**SensorManagerFacade**

  * [startSensingTimed](#startsensingtimed)
  * [startSensingThreshold](#startsensingthreshold)
  * [readSensors](#readsensors)
  * [stopSensing](#stopsensing)
  * [sensorsGetAccuracy](#sensorsgetaccuracy)
  * [sensorsGetLight](#sensorsgetlight)
  * [sensorsReadAccelerometer](#sensorsreadaccelerometer)
  * [sensorsReadMagnetometer](#sensorsreadmagnetometer)
  * [sensorsReadOrientation](#sensorsreadorientation)
  * [startSensing](#startsensing)

**SettingsFacade**

  * [setScreenTimeout](#setscreentimeout)
  * [getScreenTimeout](#getscreentimeout)
  * [checkAirplaneMode](#checkairplanemode)
  * [toggleAirplaneMode](#toggleairplanemode)
  * [checkRingerSilentMode](#checkringersilentmode)
  * [toggleRingerSilentMode](#toggleringersilentmode)
  * [toggleVibrateMode](#togglevibratemode)
  * [getVibrateMode](#getvibratemode)
  * [getMaxRingerVolume](#getmaxringervolume)
  * [getRingerVolume](#getringervolume)
  * [setRingerVolume](#setringervolume)
  * [getMaxMediaVolume](#getmaxmediavolume)
  * [getMediaVolume](#getmediavolume)
  * [setMediaVolume](#setmediavolume)
  * [getScreenBrightness](#getscreenbrightness)
  * [setScreenBrightness](#setscreenbrightness)
  * [checkScreenOn](#checkscreenon)

**SignalStrengthFacade**

  * [startTrackingSignalStrengths](#starttrackingsignalstrengths)
  * [readSignalStrengths](#readsignalstrengths)
  * [stopTrackingSignalStrengths](#stoptrackingsignalstrengths)

**SmsFacade**

  * [smsSend](#smssend)
  * [smsGetMessageCount](#smsgetmessagecount)
  * [smsGetMessageIds](#smsgetmessageids)
  * [smsGetMessages](#smsgetmessages)
  * [smsGetMessageById](#smsgetmessagebyid)
  * [smsGetAttributes](#smsgetattributes)
  * [smsDeleteMessage](#smsdeletemessage)
  * [smsMarkMessageRead](#smsmarkmessageread)

**SpeechRecognitionFacade**

  * [recognizeSpeech](#recognizespeech)

**TextToSpeechFacade**

  * [ttsSpeak](#ttsspeak)
  * [ttsIsSpeaking](#ttsisspeaking)
  * [setTtsPitch](#setttspitch)

**ToneGeneratorFacade**

  * [generateDtmfTones](#generatedtmftones)

**USBHostSerialFacade**

  * [usbserialGetDeviceList](#usbserialgetdevicelist)
  * [usbserialDisconnect](#usbserialdisconnect)
  * [usbserialActiveConnections](#usbserialactiveconnections)
  * [usbserialWriteBinary](#usbserialwritebinary)
  * [usbserialReadBinary](#usbserialreadbinary)
  * [usbserialConnect](#usbserialconnect)
  * [usbserialHostEnable](#usbserialhostenable)
  * [usbserialWrite](#usbserialwrite)
  * [usbserialReadReady](#usbserialreadready)
  * [usbserialRead](#usbserialread)
  * [usbserialGetDeviceName](#usbserialgetdevicename)

**UiFacade**

  * [dialogCreateInput](#dialogcreateinput)
  * [dialogCreatePassword](#dialogcreatepassword)
  * [dialogGetInput](#dialoggetinput)
  * [dialogGetPassword](#dialoggetpassword)
  * [dialogCreateSpinnerProgress](#dialogcreatespinnerprogress)
  * [dialogCreateHorizontalProgress](#dialogcreatehorizontalprogress)
  * [dialogCreateAlert](#dialogcreatealert)
  * [dialogCreateSeekBar](#dialogcreateseekbar)
  * [dialogCreateTimePicker](#dialogcreatetimepicker)
  * [dialogCreateDatePicker](#dialogcreatedatepicker)
  * [dialogDismiss](#dialogdismiss)
  * [dialogShow](#dialogshow)
  * [dialogSetCurrentProgress](#dialogsetcurrentprogress)
  * [dialogSetMaxProgress](#dialogsetmaxprogress)
  * [dialogSetPositiveButtonText](#dialogsetpositivebuttontext)
  * [dialogSetNegativeButtonText](#dialogsetnegativebuttontext)
  * [dialogSetNeutralButtonText](#dialogsetneutralbuttontext)
  * [dialogSetItems](#dialogsetitems)
  * [dialogSetSingleChoiceItems](#dialogsetsinglechoiceitems)
  * [dialogSetMultiChoiceItems](#dialogsetmultichoiceitems)
  * [dialogGetResponse](#dialoggetresponse)
  * [dialogGetSelectedItems](#dialoggetselecteditems)
  * [webViewShow](#webviewshow)
  * [addContextMenuItem](#addcontextmenuitem)
  * [addOptionsMenuItem](#addoptionsmenuitem)
  * [clearContextMenu](#clearcontextmenu)
  * [clearOptionsMenu](#clearoptionsmenu)
  * [fullShow](#fullshow)
  * [fullDismiss](#fulldismiss)
  * [fullQuery](#fullquery)
  * [fullQueryDetail](#fullquerydetail)
  * [fullSetProperty](#fullsetproperty)
  * [fullSetList](#fullsetlist)
  * [fullSetTitle](#fullsettitle)
  * [fullKeyOverride](#fullkeyoverride)

**WakeLockFacade**

  * [wakeLockAcquireFull](#wakelockacquirefull)
  * [wakeLockAcquirePartial](#wakelockacquirepartial)
  * [wakeLockAcquireBright](#wakelockacquirebright)
  * [wakeLockAcquireDim](#wakelockacquiredim)
  * [wakeLockRelease](#wakelockrelease)

**WebCamFacade**

  * [webcamStart](#webcamstart)
  * [webcamAdjustQuality](#webcamadjustquality)
  * [webcamStop](#webcamstop)
  * [cameraStartPreview](#camerastartpreview)
  * [cameraStopPreview](#camerastoppreview)

**WifiFacade**

  * [wifiGetScanResults](#wifigetscanresults)
  * [wifiLockAcquireFull](#wifilockacquirefull)
  * [wifiLockAcquireScanOnly](#wifilockacquirescanonly)
  * [wifiLockRelease](#wifilockrelease)
  * [wifiStartScan](#wifistartscan)
  * [checkWifiState](#checkwifistate)
  * [toggleWifiState](#togglewifistate)
  * [wifiDisconnect](#wifidisconnect)
  * [wifiGetConnectionInfo](#wifigetconnectioninfo)
  * [wifiReassociate](#wifireassociate)
  * [wifiReconnect](#wifireconnect)

# Method descriptions

## generateDtmfTones

```
void generateDtmfTones( String phoneNumber, Integer toneDuration)

Generate DTMF tones for the given phone number.
```

## startTrackingSignalStrengths

```
void startTrackingSignalStrengths()

Starts tracking signal strengths.
```

## readSignalStrengths

```
Bundle readSignalStrengths()

Returns the current signal strengths.

Returns A map of \"gsm_signal_strength\"
```

## stopTrackingSignalStrengths

```
void stopTrackingSignalStrengths()

Stops tracking signal strength.
```

## pick

```
Intent pick(String uri)

Display content to be picked by URI (e.g. contacts)

Returns A map of result values.
```

## scanBarcode

```
Intent scanBarcode()

Starts the barcode scanner.

Returns A Map representation of the result Intent.
```

## view

```
void view( String uri, String type, JSONObject extras)

Start activity with view action by URI (i.e. browser, contacts, etc.).
```

## viewMap

```
void viewMap(String query)

Opens a map search for query (e.g. pizza, 123 My Street).
```

## viewContacts

```
void viewContacts()

Opens the list of contacts.
```

## viewHtml

```
void viewHtml( String path)

Opens the browser to display a local HTML file.
```

## search

```
void search(String query)

Starts a search for the given query.
```

## dialogCreateInput

```
void dialogCreateInput( final String title, final String message, final String text, final String inputType)

Create a text input dialog.
```

## dialogCreatePassword

```
void dialogCreatePassword( final String title, final String message)

Create a password input dialog.
```

## dialogGetInput

```
String dialogGetInput( final String title, final String message, final String text)

Queries the user for a text input.
```

## dialogGetPassword

```
String dialogGetPassword( final String title, final String message)

Queries the user for a password.
```

## dialogCreateSpinnerProgress

```
void dialogCreateSpinnerProgress(String title, String message, Integer max)

Create a spinner progress dialog.
```

## dialogCreateHorizontalProgress

```
void dialogCreateHorizontalProgress( String title, String message, Integer max)

Create a horizontal progress dialog.
```

## dialogCreateAlert

```
void dialogCreateAlert(String title, String message)

Create alert dialog.
```

## dialogCreateSeekBar

```
void dialogCreateSeekBar( Integer progress, Integer max, String title, String message)

Create seek bar dialog.
```

## dialogCreateTimePicker

```
void dialogCreateTimePicker( Integer hour, Integer minute, Boolean is24hour)

Create time picker dialog.
```

## dialogCreateDatePicker

```
void dialogCreateDatePicker(Integer year, Integer month, Integer day)

Create date picker dialog.
```

## dialogDismiss

```
void dialogDismiss()

Dismiss dialog.
```

## dialogShow

```
void dialogShow()

Show dialog.
```

## dialogSetCurrentProgress

```
void dialogSetCurrentProgress(Integer current)

Set progress dialog current value.
```

## dialogSetMaxProgress

```
void dialogSetMaxProgress(Integer max)

Set progress dialog maximum value.
```

## dialogSetPositiveButtonText

```
void dialogSetPositiveButtonText(String text)

Set alert dialog positive button text.
```

## dialogSetNegativeButtonText

```
void dialogSetNegativeButtonText(String text)

Set alert dialog button text.
```

## dialogSetNeutralButtonText

```
void dialogSetNeutralButtonText(String text)

Set alert dialog button text.
```

## dialogSetItems

```
void dialogSetItems(JSONArray items)

Set alert dialog list items.
```

## dialogSetSingleChoiceItems

```
void dialogSetSingleChoiceItems( JSONArray items, Integer selected)

Set dialog single choice items and selected item.
```

## dialogSetMultiChoiceItems

```
void dialogSetMultiChoiceItems( JSONArray items, JSONArray selected)

Set dialog multiple choice items and selection.
```

## dialogGetResponse

```
Object dialogGetResponse()

Returns dialog response.
```

## dialogGetSelectedItems

```
Set<Integer> dialogGetSelectedItems()

This method provides list of items user selected.

Returns Selected items
```

## webViewShow

```
void webViewShow( String url, Boolean wait)

Display a WebView with the given URL.
```

## addContextMenuItem

```
void addContextMenuItem( String label, String event, Object data)

Adds a new item to context menu.
```

## addOptionsMenuItem

```
void addOptionsMenuItem( String label, String event, Object data, String iconName)

Adds a new item to options menu.
```

## clearContextMenu

```
void clearContextMenu()

Removes all items previously added to context menu.
```

## clearOptionsMenu

```
void clearOptionsMenu()

Removes all items previously added to options menu.
```

## fullShow

```
List<String> fullShow( String layout, String title)

Show Full Screen.
```

## fullDismiss

```
void fullDismiss()

Dismiss Full Screen.
```

## fullQuery

```
Map<String, Map<String, String>> fullQuery()

Get Fullscreen Properties
```

## fullQueryDetail

```
Map<String, String> fullQueryDetail( String id)

Get fullscreen properties for a specific widget
```

## fullSetProperty

```
String fullSetProperty( String id, String property, String value)

Set fullscreen widget property
```

## fullSetList

```
String fullSetList( String id, JSONArray items)

Attach a list to a fullscreen widget
```

## fullSetTitle

```
void fullSetTitle( String title)

Set the Full Screen Activity Title
```

## fullKeyOverride

```
JSONArray fullKeyOverride( JSONArray keycodes, Boolean enable)

Override default key actions
```

## readBatteryData

```
Bundle readBatteryData()

Returns the most recently recorded battery data.
```

## batteryStartMonitoring

```
void batteryStartMonitoring()

Starts tracking battery state.
```

## batteryStopMonitoring

```
void batteryStopMonitoring()

Stops tracking battery state.
```

## batteryGetStatus

```
Integer batteryGetStatus()

Returns  the most recently received battery status data:\n1 - unknown;\n2 - charging;\n3 - discharging;\n4 - not charging;\n5 - full;
```

## batteryGetHealth

```
Integer batteryGetHealth()

Returns the most recently received battery health data:\n1 - unknown;\n2 - good;\n3 - overheat;\n4 - dead;\n5 - over voltage;\n6 - unspecified failure;
```

## batteryCheckPresent

```
Boolean batteryCheckPresent()

Returns the most recently received battery presence data.
```

## batteryGetLevel

```
Integer batteryGetLevel()

Returns the most recently received battery level (percentage).
```

## batteryGetVoltage

```
Integer batteryGetVoltage()

Returns the most recently received battery voltage.
```

## batteryGetTemperature

```
Integer batteryGetTemperature()

Returns the most recently received battery temperature.
```

## batteryGetTechnology

```
String batteryGetTechnology()

Returns the most recently received battery technology data.
```

## setResultBoolean

```
void setResultBoolean( Integer resultCode, Boolean resultValue)

sRpcDescription
```

## setResultByte

```
void setResultByte( Integer resultCode, Byte resultValue)

sRpcDescription
```

## setResultShort

```
void setResultShort( Integer resultCode, Short resultValue)

sRpcDescription
```

## setResultChar

```
void setResultChar( Integer resultCode, Character resultValue)

sRpcDescription
```

## setResultInteger

```
void setResultInteger( Integer resultCode, Integer resultValue)

sRpcDescription
```

## setResultLong

```
void setResultLong( Integer resultCode, Long resultValue)

sRpcDescription
```

## setResultFloat

```
void setResultFloat( Integer resultCode, Float resultValue)

sRpcDescription
```

## setResultDouble

```
void setResultDouble( Integer resultCode, Double resultValue)

sRpcDescription
```

## setResultString

```
void setResultString( Integer resultCode, String resultValue)

sRpcDescription
```

## setResultBooleanArray

```
void setResultBooleanArray( Integer resultCode, Boolean[] resultValue)

sRpcDescription
```

## setResultByteArray

```
void setResultByteArray( Integer resultCode, Byte[] resultValue)

sRpcDescription
```

## setResultShortArray

```
void setResultShortArray( Integer resultCode, Short[] resultValue)

sRpcDescription
```

## setResultCharArray

```
void setResultCharArray( Integer resultCode, Character[] resultValue)

sRpcDescription
```

## setResultIntegerArray

```
void setResultIntegerArray( Integer resultCode, Integer[] resultValue)

sRpcDescription
```

## setResultLongArray

```
void setResultLongArray( Integer resultCode, Long[] resultValue)

sRpcDescription
```

## setResultFloatArray

```
void setResultFloatArray( Integer resultCode, Float[] resultValue)

sRpcDescription
```

## setResultDoubleArray

```
void setResultDoubleArray( Integer resultCode, Double[] resultValue)

sRpcDescription
```

## setResultStringArray

```
void setResultStringArray( Integer resultCode, String[] resultValue)

sRpcDescription
```

## setResultSerializable

```
void setResultSerializable( Integer resultCode, Serializable resultValue)

sRpcDescription
```

## bluetoothActiveConnections

```
Map<String, String> bluetoothActiveConnections()

Returns active Bluetooth connections.
```

## bluetoothWriteBinary

```
void bluetoothWriteBinary( String base64, String connID)

Send bytes over the currently open Bluetooth connection.
```

## bluetoothReadBinary

```
String bluetoothReadBinary( Integer bufferSize, String connID)

Read up to bufferSize bytes and return a chunked, base64 encoded string.
```

## bluetoothConnect

```
String bluetoothConnect( String uuid, String address)

Connect to a device over Bluetooth. Blocks until the connection is established or fails.

Returns True if the connection was established successfully.
```

## bluetoothAccept

```
String bluetoothAccept( String uuid, Integer timeout)

Listens for and accepts a Bluetooth connection. Blocks until the connection is established or fails.
```

## bluetoothMakeDiscoverable

```
void bluetoothMakeDiscoverable( Integer duration)

Requests that the device be discoverable for Bluetooth connections.
```

## bluetoothWrite

```
void bluetoothWrite(String ascii, String connID)

Sends ASCII characters over the currently open Bluetooth connection.
```

## bluetoothReadReady

```
Boolean bluetoothReadReady( String connID)

Returns True if the next read is guaranteed not to block.
```

## bluetoothRead

```
String bluetoothRead( Integer bufferSize, String connID)

Read up to bufferSize ASCII characters.
```

## bluetoothReadLine

```
String bluetoothReadLine( String connID)

Read the next line.
```

## bluetoothGetRemoteDeviceName

```
String bluetoothGetRemoteDeviceName( String address)

Queries a remote device for it's name or null if it can't be resolved
```

## bluetoothGetLocalName

```
String bluetoothGetLocalName()

Gets the Bluetooth Visible device name
```

## bluetoothSetLocalName

```
boolean bluetoothSetLocalName( String name)

Sets the Bluetooth Visible device name, returns True on success
```

## bluetoothGetScanMode

```
int bluetoothGetScanMode()

Gets the scan mode for the local dongle.\r\nReturn values:\r\n\t-1 when Bluetooth is disabled.\r\n\t0 if non discoverable and non connectable.\r\n\r1 connectable non discoverable.\r3 connectable and discoverable.
```

## bluetoothGetConnectedDeviceName

```
String bluetoothGetConnectedDeviceName( String connID)

Returns the name of the connected device.
```

## checkBluetoothState

```
Boolean checkBluetoothState()

Checks Bluetooth state.

Returns True if Bluetooth is enabled.
```

## toggleBluetoothState

```
Boolean toggleBluetoothState( Boolean enabled, Boolean prompt)

Toggle Bluetooth on and off.

Returns True if Bluetooth is enabled.
```

## bluetoothStop

```
void bluetoothStop( String connID)

Stops Bluetooth connection.
```

## bluetoothGetLocalAddress

```
String bluetoothGetLocalAddress()

Returns the hardware address of the local Bluetooth adapter. 
```

## bluetoothDiscoveryStart

```
Boolean bluetoothDiscoveryStart()

Start the remote device discovery process. 

Returns true on success, false on error
```

## bluetoothDiscoveryCancel

```
Boolean bluetoothDiscoveryCancel()

Cancel the current device discovery process.

Returns true on success, false on error
```

## bluetoothIsDiscovering

```
Boolean bluetoothIsDiscovering()

Return true if the local Bluetooth adapter is currently in the device discovery process. 
```

## prefGetValue

```
Object prefGetValue( String key, String filename)

Read a value from shared preferences
```

## prefPutValue

```
void prefPutValue( String key, Object value, String filename)

Write a value to shared preferences
```

## prefGetAll

```
Map<String, ?> prefGetAll( String filename)

Get list of Shared Preference Values

Returns Map of key,value
```

## smsSend

```
void smsSend( String destinationAddress, String text)

Sends an SMS.
```

## smsGetMessageCount

```
Integer smsGetMessageCount(Boolean unreadOnly, String folder)

Returns the number of messages.
```

## smsGetMessageIds

```
List<Integer> smsGetMessageIds(Boolean unreadOnly, String folder)

Returns a List of all message IDs.
```

## smsGetMessages

```
List<JSONObject> smsGetMessages(Boolean unreadOnly, String folder, JSONArray attributes)

Returns a List of all messages.

Returns a List of messages as Maps
```

## smsGetMessageById

```
JSONObject smsGetMessageById( Integer id, JSONArray attributes)

Returns message attributes.
```

## smsGetAttributes

```
List<String> smsGetAttributes()

Returns a List of all possible message attributes.
```

## smsDeleteMessage

```
Boolean smsDeleteMessage(Integer id)

Deletes a message.

Returns True if the message was deleted
```

## smsMarkMessageRead

```
Integer smsMarkMessageRead( JSONArray ids, Boolean read)

Marks messages as read.

Returns number of messages marked read
```

## mediaPlay

```
boolean mediaPlay( String url, String tag, Boolean play)

Open a media file

Returns true if play successful
```

## mediaPlayPause

```
boolean mediaPlayPause( String tag)

pause playing media file

Returns true if successful
```

## mediaPlayStart

```
boolean mediaPlayStart( String tag)

start playing media file

Returns true if successful
```

## mediaPlayClose

```
boolean mediaPlayClose( String tag)

Close media file

Returns true if successful
```

## mediaIsPlaying

```
boolean mediaIsPlaying( String tag)

Checks if media file is playing.

Returns true if playing
```

## mediaPlayInfo

```
Map<String, Object> mediaPlayInfo( String tag)

Information on current media

Returns Media Information
```

## mediaPlayList

```
Set<String> mediaPlayList()

Lists currently loaded media

Returns List of Media Tags
```

## mediaPlaySetLooping

```
boolean mediaPlaySetLooping( Boolean enabled, String tag)

Set Looping

Returns True if successful
```

## mediaPlaySeek

```
int mediaPlaySeek( Integer msec, String tag)

Seek To Position

Returns New Position (in ms)
```

## pickContact

```
Intent pickContact()

Displays a list of contacts to pick from.

Returns A map of result values.
```

## pickPhone

```
String pickPhone()

Displays a list of phone numbers to pick from.

Returns The selected phone number.
```

## contactsGetAttributes

```
List<String> contactsGetAttributes()

Returns a List of all possible attributes for contacts.
```

## contactsGetIds

```
List<Integer> contactsGetIds()

Returns a List of all contact IDs.
```

## contactsGet

```
List<JSONObject> contactsGet( JSONArray attributes)

Returns a List of all contacts.

Returns a List of contacts as Maps
```

## contactsGetById

```
JSONObject contactsGetById(Integer id, JSONArray attributes)

Returns contacts by ID.
```

## contactsGetCount

```
Integer contactsGetCount()

Returns the number of contacts.
```

## queryContent

```
List<JSONObject> queryContent( String uri, JSONArray attributes, String selection, JSONArray selectionArgs, String order)

Content Resolver Query

Returns result of query as Maps
```

## queryAttributes

```
JSONArray queryAttributes( String uri)

Content Resolver Query Attributes

Returns a list of available columns for a given content uri
```

## startTrackingPhoneState

```
void startTrackingPhoneState()

Starts tracking phone state.
```

## readPhoneState

```
Bundle readPhoneState()

Returns the current phone state and incoming number.

Returns A Map of \"state\" and \"incomingNumber\"
```

## stopTrackingPhoneState

```
void stopTrackingPhoneState()

Stops tracking phone state.
```

## phoneCall

```
void phoneCall(final String uriString)

Calls a contact/phone number by URI.
```

## phoneCallNumber

```
void phoneCallNumber(final String number)

Calls a phone number.
```

## phoneDial

```
void phoneDial(final String uri)

Dials a contact/phone number by URI.
```

## phoneDialNumber

```
void phoneDialNumber(final String number)

Dials a phone number.
```

## getCellLocation

```
CellLocation getCellLocation()

Returns the current cell location.
```

## getNetworkOperator

```
String getNetworkOperator()

Returns the numeric name (MCC+MNC) of current registered operator.
```

## getNetworkOperatorName

```
String getNetworkOperatorName()

Returns the alphabetic name of current registered operator.
```

## getNetworkType

```
String getNetworkType()

Returns a the radio technology (network type) currently in use on the device.
```

## getPhoneType

```
String getPhoneType()

Returns the device phone type.
```

## getSimCountryIso

```
String getSimCountryIso()

Returns the ISO country code equivalent for the SIM provider's country code.
```

## getSimOperator

```
String getSimOperator()

Returns the MCC+MNC (mobile country code + mobile network code) of the provider of the SIM. 5 or 6 decimal digits.
```

## getSimOperatorName

```
String getSimOperatorName()

Returns the Service Provider Name (SPN).
```

## getSimSerialNumber

```
String getSimSerialNumber()

Returns the serial number of the SIM, if applicable. Return null if it is unavailable.
```

## getSimState

```
String getSimState()

Returns the state of the device SIM card.
```

## getSubscriberId

```
String getSubscriberId()

Returns the unique subscriber ID, for example, the IMSI for a GSM phone. Return null if it is unavailable.
```

## getVoiceMailAlphaTag

```
String getVoiceMailAlphaTag()

Retrieves the alphabetic identifier associated with the voice mail number.
```

## getVoiceMailNumber

```
String getVoiceMailNumber()

Returns the voice mail number. Return null if it is unavailable.
```

## checkNetworkRoaming

```
Boolean checkNetworkRoaming()

Returns true if the device is considered roaming on the current network, for GSM purposes.
```

## getDeviceId

```
String getDeviceId()

Returns the unique device ID, for example, the IMEI for GSM and the MEID for CDMA phones. Return null if device ID is not available.
```

## getDeviceSoftwareVersion

```
String getDeviceSoftwareVersion()

Returns the software version number for the device, for example, the IMEI/SV for GSM phones. Return null if the software version is not available.
```

## getLine1Number

```
String getLine1Number()

Returns the phone number string for line 1, for example, the MSISDN for a GSM phone. Return null if it is unavailable.
```

## getNeighboringCellInfo

```
List<NeighboringCellInfo> getNeighboringCellInfo()

Returns the neighboring cell information of the device.
```

## webcamStart

```
InetSocketAddress webcamStart( Integer resolutionLevel, Integer jpegQuality, Integer port)

Starts an MJPEG stream and returns a Tuple of address and port for the stream.
```

## webcamAdjustQuality

```
void webcamAdjustQuality( Integer resolutionLevel, Integer jpegQuality)

Adjusts the quality of the webcam stream while it is running.
```

## webcamStop

```
void webcamStop()

Stops the webcam stream.
```

## cameraStartPreview

```
boolean cameraStartPreview( Integer resolutionLevel, Integer jpegQuality, String filepath)

Start Preview Mode. Throws 'preview' events.

Returns True if successful
```

## cameraStopPreview

```
void cameraStopPreview()

Stop the preview mode.
```

## nfcIsEnabled

```
Boolean nfcIsEnabled()

Check if NFC hardware is enabled.
```

## nfcStartTrackingStateChange

```
void nfcStartTrackingStateChange()

Start tracking NFC hardware state changes.
```

## nfcStopTrackingStateChange

```
void nfcStopTrackingStateChange()

Stop tracking NFC hardware state changes.
```

## locationProviders

```
List<String> locationProviders()

Returns availables providers on the phone
```

## locationProviderEnabled

```
boolean locationProviderEnabled( String provider)

Ask if provider is enabled
```

## startLocating

```
void startLocating( Integer minUpdateTime, Integer minUpdateDistance)

Starts collecting location data.
```

## readLocation

```
Map<String, Location> readLocation()

Returns the current location as indicated by all available providers.

Returns A map of location information by provider.
```

## stopLocating

```
void stopLocating()

Stops collecting location data.
```

## getLastKnownLocation

```
Map<String, Location> getLastKnownLocation()

Returns the last known location of the device.

Returns A map of location information by provider.
```

## geocode

```
List<Address> geocode( Double latitude, Double longitude, Integer maxResults)

Returns a list of addresses for the given latitude and longitude.

Returns A list of addresses.
```

## setClipboard

```
void setClipboard(String text)

Put text in the clipboard.
```

## getClipboard

```
String getClipboard()

Read text from the clipboard.

Returns The text in the clipboard.
```

## startActivityForResult

```
Intent startActivityForResult( String action, String uri, String type, JSONObject extras, String packagename, String classname)

Starts an activity and returns the result.

Returns A Map representation of the result Intent.
```

## startActivityForResultIntent

```
Intent startActivityForResultIntent( Intent intent)

Starts an activity and returns the result.

Returns A Map representation of the result Intent.
```

## startActivity

```
void startActivity( String action, String uri, String type, JSONObject extras, Boolean wait, String packagename, String classname)

Starts an activity.
```

## sendBroadcast

```
void sendBroadcast( String action, String uri, String type, JSONObject extras, String packagename, String classname)

Send a broadcast.
```

## makeIntent

```
Intent makeIntent( String action, String uri, String type, JSONObject extras, JSONArray categories, String packagename, String classname, Integer flags)

Create an Intent.

Returns An object representing an Intent
```

## startActivityIntent

```
void startActivityIntent( Intent intent, Boolean wait)

Start Activity using Intent
```

## sendBroadcastIntent

```
void sendBroadcastIntent( Intent intent)

Send Broadcast Intent
```

## vibrate

```
void vibrate( Integer duration)

Vibrates the phone or a specified duration in milliseconds.
```

## makeToast

```
void makeToast(final String message)

Displays a short-duration Toast notification.
```

## getInput

```
String getInput( final String title, final String message)

Queries the user for a text input.
```

## getPassword

```
String getPassword( final String title, final String message)

Queries the user for a password.
```

## notify

```
void notify(String title, String message)

Displays a notification that will be canceled when the user clicks on it.
```

## getIntent

```
Object getIntent()

Returns the intent that launched the script.
```

## sendEmail

```
void sendEmail( final String to, final String subject, final String body, final String attachmentUri)

Launches an activity that sends an e-mail message to a given recipient.
```

## getPackageVersionCode

```
int getPackageVersionCode(final String packageName)

Returns package version code.
```

## getPackageVersion

```
String getPackageVersion(final String packageName)

Returns package version name.
```

## requiredVersion

```
boolean requiredVersion(final Integer version)

Checks if version of SL4A is greater than or equal to the specified version.
```

## log

```
void log(String message)

Writes message to logcat.
```

## environment

```
Map<String, Object> environment()

A map of various useful environment details
```

## getConstants

```
Bundle getConstants( String classname)

Get list of constants (static final fields) for a class
```

## recorderStartMicrophone

```
void recorderStartMicrophone(String targetPath)

Records audio from the microphone and saves it to the given location.
```

## recorderStartVideo

```
void recorderStartVideo(String targetPath, Integer duration, Integer videoSize)

Records video from the camera and saves it to the given location. \nDuration specifies the maximum duration of the recording session. \nIf duration is 0 this method will return and the recording will only be stopped \nwhen recorderStop is called or when a scripts exits. \nOtherwise it will block for the time period equal to the duration argument.\nvideoSize: 0=160x120, 1=320x240, 2=352x288, 3=640x480, 4=800x480.
```

## recorderCaptureVideo

```
void recorderCaptureVideo(String targetPath, Integer duration, Boolean recordAudio)

Records video (and optionally audio) from the camera and saves it to the given location. \nDuration specifies the maximum duration of the recording session. \nIf duration is not provided this method will return immediately and the recording will only be stopped \nwhen recorderStop is called or when a scripts exits. \nOtherwise it will block for the time period equal to the duration argument.
```

## recorderStop

```
void recorderStop()

Stops a previously started recording.
```

## startInteractiveVideoRecording

```
void startInteractiveVideoRecording(final String path)

Starts the video capture application to record a video and saves it to the specified path.
```

## getLaunchableApplications

```
Map<String, String> getLaunchableApplications()

Returns a list of all launchable application class names.
```

## launch

```
void launch(String className)

Start activity with the given class name.
```

## getRunningPackages

```
List<String> getRunningPackages()

Returns a list of packages running activities or services.

Returns List of packages running activities.
```

## forceStopPackage

```
void forceStopPackage( String packageName)

Force stops a package.
```

## cameraCapturePicture

```
Bundle cameraCapturePicture( final String targetPath, Boolean useAutoFocus, Integer cameraId)

Take a picture and save it to the specified path.

Returns A map of Booleans autoFocus and takePicture where True indicates success. cameraId also included.
```

## cameraInteractiveCapturePicture

```
void cameraInteractiveCapturePicture( final String targetPath)

Starts the image capture application to take a picture and saves it to the specified path.
```

## camerasList

```
Map<String, String> camerasList()

Get Camera List, Id and parameters.

Returns Map of (cameraId, information).information is comma separated and order is:canDisableShtterSound,facing,orientation.facing: 0=BACK, 1=FACE.orientation: 0,90,180,270=camera image.
```

## usbserialGetDeviceList

```
Map<String, String> usbserialGetDeviceList()

Returns USB devices reported by USB Host API.

Returns "Map of id and string information '
```

## usbserialDisconnect

```
void usbserialDisconnect( String connID )

Disconnect all USB-device.
```

## usbserialActiveConnections

```
Map<String, String> usbserialActiveConnections()

Returns active USB-device connections.

Returns "Active USB-device connections by Map UUID vs device-name." 
```

## usbserialWriteBinary

```
void usbserialWriteBinary( String base64, String connID)

Send bytes over the currently open USB Serial connection.
```

## usbserialReadBinary

```
String usbserialReadBinary( Integer bufferSize, String connID)

Read up to bufferSize bytes and return a chunked, base64 encoded string.
```

## usbserialConnect

```
String usbserialConnect( String hash, String options)

Connect to a device with USB-Host. request the connection and exit.

Returns messages the request status.
```

## usbserialHostEnable

```
Boolean usbserialHostEnable()

Requests that the host be enable for USB Serial connections.

Returns "True if the USB Device is accesible
```

## usbserialWrite

```
void usbserialWrite(String ascii, String connID)

Sends ASCII characters over the currently open USB Serial connection.
```

## usbserialReadReady

```
Boolean usbserialReadReady( String connID)

Returns True if the next read is guaranteed not to block.
```

## usbserialRead

```
String usbserialRead( String connID, Integer bufferSize)

Read up to bufferSize ASCII characters.
```

## usbserialGetDeviceName

```
String usbserialGetDeviceName( String connID)

Queries a remote device for it's name or null if it can't be resolved
```

## recognizeSpeech

```
String recognizeSpeech( final String prompt, final String language, final String languageModel)

Recognizes user's speech and returns the most likely result.

Returns An empty string in case the speech cannot be recongnized.
```

## wakeLockAcquireFull

```
void wakeLockAcquireFull()

Acquires a full wake lock (CPU on, screen bright, keyboard bright).
```

## wakeLockAcquirePartial

```
void wakeLockAcquirePartial()

Acquires a partial wake lock (CPU on).
```

## wakeLockAcquireBright

```
void wakeLockAcquireBright()

Acquires a bright wake lock (CPU on, screen bright).
```

## wakeLockAcquireDim

```
void wakeLockAcquireDim()

Acquires a dim wake lock (CPU on, screen dim).
```

## wakeLockRelease

```
void wakeLockRelease()

Releases the wake lock.
```

## ttsSpeak

```
void ttsSpeak(String message)

Speaks the provided message via TTS.
```

## startSensingTimed

```
void startSensingTimed( Integer sensorNumber, Integer delayTime)

Starts recording sensor data to be available for polling.
```

## startSensingThreshold

```
void startSensingThreshold(  Integer sensorNumber, Integer threshold, Integer axis)

Records to the Event Queue sensor data exceeding a chosen threshold.
```

## readSensors

```
Bundle readSensors()

Returns the most recently recorded sensor data.
```

## stopSensing

```
void stopSensing()

Stops collecting sensor data.
```

## sensorsGetAccuracy

```
Integer sensorsGetAccuracy()

Returns the most recently received accuracy value.
```

## sensorsGetLight

```
Float sensorsGetLight()

Returns the most recently received light value.
```

## sensorsReadAccelerometer

```
List<Float> sensorsReadAccelerometer()

Returns the most recently received accelerometer values.

Returns a List of Floats [(acceleration on the) X axis, Y axis, Z axis].
```

## sensorsReadMagnetometer

```
List<Float> sensorsReadMagnetometer()

Returns the most recently received magnetic field values.

Returns a List of Floats [(magnetic field value for) X axis, Y axis, Z axis].
```

## sensorsReadOrientation

```
List<Double> sensorsReadOrientation()

Returns the most recently received orientation values.

Returns a List of Doubles [azimuth, pitch, roll].
```

## startSensing

```
void startSensing( Integer sampleSize)

Starts recording sensor data to be available for polling.
```

## ttsSpeak

```
void ttsSpeak(String message)

Speaks the provided message via TTS.
```

## ttsIsSpeaking

```
Boolean ttsIsSpeaking()

Returns True if speech is currently in progress.
```

## setTtsPitch

```
void setTtsPitch(String pitch)

Changes the pitch of TTS speech.
```

## wifiGetScanResults

```
List<ScanResult> wifiGetScanResults()

Returns the list of access points found during the most recent Wifi scan.
```

## wifiLockAcquireFull

```
void wifiLockAcquireFull()

Acquires a full Wifi lock.
```

## wifiLockAcquireScanOnly

```
void wifiLockAcquireScanOnly()

Acquires a scan only Wifi lock.
```

## wifiLockRelease

```
void wifiLockRelease()

Releases a previously acquired Wifi lock.
```

## wifiStartScan

```
Boolean wifiStartScan()

Starts a scan for Wifi access points.

Returns True if the scan was initiated successfully.
```

## checkWifiState

```
Boolean checkWifiState()

Checks Wifi state.

Returns True if Wifi is enabled.
```

## toggleWifiState

```
Boolean toggleWifiState(Boolean enabled)

Toggle Wifi on and off.

Returns True if Wifi is enabled.
```

## wifiDisconnect

```
Boolean wifiDisconnect()

Disconnects from the currently active access point.

Returns True if the operation succeeded.
```

## wifiGetConnectionInfo

```
WifiInfo wifiGetConnectionInfo()

Returns information about the currently active access point.
```

## wifiReassociate

```
Boolean wifiReassociate()

Reassociates with the currently active access point.

Returns True if the operation succeeded.
```

## wifiReconnect

```
Boolean wifiReconnect()

Reconnects to the currently active access point.

Returns True if the operation succeeded.
```

## eventClearBuffer

```
void eventClearBuffer()

Clears all events from the event buffer.
```

## eventRegisterForBroadcast

```
boolean eventRegisterForBroadcast( String category, Boolean enqueue)

Registers a listener for a new broadcast signal
```

## eventUnregisterForBroadcast

```
void eventUnregisterForBroadcast(String category)

Stop listening for a broadcast signal
```

## eventGetBrodcastCategories

```
Set<String> eventGetBrodcastCategories()

Lists all the broadcast signals we are listening for
```

## eventPoll

```
List<Event> eventPoll( Integer number_of_events)

Returns and removes the oldest n events (i.e. location or sensor update, etc.) from the event buffer.

Returns A List of Maps of event properties.
```

## eventWaitFor

```
Event eventWaitFor( final String eventName, Integer timeout)

Blocks until an event with the supplied name occurs. The returned event is not removed from the buffer.

Returns Map of event properties.
```

## eventWait

```
Event eventWait( Integer timeout)

Blocks until an event occurs. The returned event is removed from the buffer.

Returns Map of event properties.
```

## eventPost

```
void eventPost( String name, String data, Boolean enqueue)

Post an event to the event queue.
```

## rpcPostEvent

```
void rpcPostEvent(String name, String data)

Post an event to the event queue.
```

## receiveEvent

```
Event receiveEvent()

Returns and removes the oldest event (i.e. location or sensor update, etc.) from the event buffer.

Returns Map of event properties.
```

## waitForEvent

```
Event waitForEvent( final String eventName, Integer timeout)

Blocks until an event with the supplied name occurs. The returned event is not removed from the buffer.

Returns Map of event properties.
```

## startEventDispatcher

```
int startEventDispatcher( Integer port)

Opens up a socket where you can read for events posted
```

## stopEventDispatcher

```
void stopEventDispatcher()

Stops the event server, you can't read in the port anymore
```

## setScreenTimeout

```
Integer setScreenTimeout(Integer value)

Sets the screen timeout to this number of seconds.

Returns The original screen timeout.
```

## getScreenTimeout

```
Integer getScreenTimeout()

Returns the current screen timeout in seconds.

Returns the current screen timeout in seconds.
```

## checkAirplaneMode

```
Boolean checkAirplaneMode()

Checks the airplane mode setting.

Returns True if airplane mode is enabled.
```

## toggleAirplaneMode

```
Boolean toggleAirplaneMode(Boolean enabled)

Toggles airplane mode on and off.

Returns True if airplane mode is enabled.
```

## checkRingerSilentMode

```
Boolean checkRingerSilentMode()

Checks the ringer silent mode setting.

Returns True if ringer silent mode is enabled.
```

## toggleRingerSilentMode

```
Boolean toggleRingerSilentMode(Boolean enabled)

Toggles ringer silent mode on and off.

Returns True if ringer silent mode is enabled.
```

## toggleVibrateMode

```
Boolean toggleVibrateMode(Boolean enabled, Boolean ringer)

Toggles vibrate mode on and off. If ringer=true then set Ringer setting, else set Notification setting

Returns True if vibrate mode is enabled.
```

## getVibrateMode

```
Boolean getVibrateMode(Boolean ringer)

Checks Vibration setting. If ringer=true then query Ringer setting, else query Notification setting

Returns True if vibrate mode is enabled.
```

## getMaxRingerVolume

```
int getMaxRingerVolume()

Returns the maximum ringer volume.
```

## getRingerVolume

```
int getRingerVolume()

Returns the current ringer volume.
```

## setRingerVolume

```
void setRingerVolume(Integer volume)

Sets the ringer volume.
```

## getMaxMediaVolume

```
int getMaxMediaVolume()

Returns the maximum media volume.
```

## getMediaVolume

```
int getMediaVolume()

Returns the current media volume.
```

## setMediaVolume

```
void setMediaVolume(Integer volume)

Sets the media volume.
```

## getScreenBrightness

```
Integer getScreenBrightness()

Returns the screen backlight brightness.

Returns the current screen brightness between 0 and 255
```

## setScreenBrightness

```
Integer setScreenBrightness( Integer value)

Sets the the screen backlight brightness.

Returns the original screen brightness.
```

## checkScreenOn

```
Boolean checkScreenOn()

Checks if the screen is on or off (requires API level 7).

Returns True if the screen is currently on.
```

