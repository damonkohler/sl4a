Generated at commit `b'changeset:   1366:ce4a3f399607'`

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

**Bluetooth4Facade**

  * [bluetoothMakeConnectable](#bluetoothmakeconnectable)
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
  * [bluetoothMakeUndiscoverable](#bluetoothmakeundiscoverable)
  * [bluetoothGetRemoteDeviceName](#bluetoothgetremotedevicename)
  * [bluetoothGetLocalName](#bluetoothgetlocalname)
  * [bluetoothSetLocalName](#bluetoothsetlocalname)
  * [bluetoothGetScanMode](#bluetoothgetscanmode)
  * [bluetoothGetConnectedDeviceName](#bluetoothgetconnecteddevicename)
  * [checkBluetoothState](#checkbluetoothstate)
  * [bluetoothFactoryReset](#bluetoothfactoryreset)
  * [toggleBluetoothState](#togglebluetoothstate)
  * [bluetoothStop](#bluetoothstop)
  * [bluetoothGetLocalAddress](#bluetoothgetlocaladdress)
  * [bluetoothDiscoveryStart](#bluetoothdiscoverystart)
  * [bluetoothDiscoveryCancel](#bluetoothdiscoverycancel)
  * [bluetoothIsDiscovering](#bluetoothisdiscovering)
  * [bluetoothGetDiscoveredDevices](#bluetoothgetdiscovereddevices)
  * [bluetoothConfigHciSnoopLog](#bluetoothconfighcisnooplog)
  * [bluetoothGetControllerActivityEnergyInfo](#bluetoothgetcontrolleractivityenergyinfo)
  * [bluetoothIsHardwareTrackingFiltersAvailable](#bluetoothishardwaretrackingfiltersavailable)
  * [bluetoothGetLeState](#bluetoothgetlestate)
  * [bluetoothEnableBLE](#bluetoothenableble)
  * [bluetoothDisableBLE](#bluetoothdisableble)
  * [bluetoothListenForBleStateChange](#bluetoothlistenforblestatechange)
  * [bluetoothStopListeningForBleStateChange](#bluetoothstoplisteningforblestatechange)
  * [bluetoothStartListeningForAdapterStateChange](#bluetoothstartlisteningforadapterstatechange)
  * [bluetoothStopListeningForAdapterStateChange](#bluetoothstoplisteningforadapterstatechange)

**BluetoothA2dpFacade**

  * [bluetoothA2dpIsReady](#bluetootha2dpisready)
  * [bluetoothA2dpSetPriority](#bluetootha2dpsetpriority)
  * [bluetoothA2dpConnect](#bluetootha2dpconnect)
  * [bluetoothA2dpDisconnect](#bluetootha2dpdisconnect)
  * [bluetoothA2dpGetConnectedDevices](#bluetootha2dpgetconnecteddevices)

**BluetoothA2dpSinkFacade**

  * [bluetoothA2dpSinkSetPriority](#bluetootha2dpsinksetpriority)
  * [bluetoothA2dpSinkGetPriority](#bluetootha2dpsinkgetpriority)
  * [bluetoothA2dpSinkIsReady](#bluetootha2dpsinkisready)
  * [bluetoothA2dpSinkConnect](#bluetootha2dpsinkconnect)
  * [bluetoothA2dpSinkDisconnect](#bluetootha2dpsinkdisconnect)
  * [bluetoothA2dpSinkGetConnectedDevices](#bluetootha2dpsinkgetconnecteddevices)
  * [bluetoothA2dpSinkGetConnectionStatus](#bluetootha2dpsinkgetconnectionstatus)

**BluetoothAvrcpFacade**

  * [bluetoothAvrcpIsReady](#bluetoothavrcpisready)
  * [bluetoothAvrcpGetConnectedDevices](#bluetoothavrcpgetconnecteddevices)
  * [bluetoothAvrcpDisconnect](#bluetoothavrcpdisconnect)

**BluetoothConnectionFacade**

  * [bluetoothStartConnectionStateChangeMonitor](#bluetoothstartconnectionstatechangemonitor)
  * [bluetoothStartPairingHelper](#bluetoothstartpairinghelper)
  * [bluetoothGetConnectedDevices](#bluetoothgetconnecteddevices)
  * [bluetoothGetConnectedLeDevices](#bluetoothgetconnectedledevices)
  * [bluetoothIsDeviceConnected](#bluetoothisdeviceconnected)
  * [bluetoothGetConnectedDevicesOnProfile](#bluetoothgetconnecteddevicesonprofile)
  * [bluetoothDiscoverAndConnect](#bluetoothdiscoverandconnect)
  * [bluetoothDiscoverAndBond](#bluetoothdiscoverandbond)
  * [bluetoothUnbond](#bluetoothunbond)
  * [bluetoothConnectBonded](#bluetoothconnectbonded)
  * [bluetoothDisconnectConnected](#bluetoothdisconnectconnected)
  * [bluetoothDisconnectConnectedProfile](#bluetoothdisconnectconnectedprofile)
  * [bluetoothChangeProfileAccessPermission](#bluetoothchangeprofileaccesspermission)

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

**BluetoothHfpClientFacade**

  * [bluetoothHfpClientIsReady](#bluetoothhfpclientisready)
  * [bluetoothHfpClientSetPriority](#bluetoothhfpclientsetpriority)
  * [bluetoothHfpClientGetPriority](#bluetoothhfpclientgetpriority)
  * [bluetoothHfpClientConnect](#bluetoothhfpclientconnect)
  * [bluetoothHfpClientDisconnect](#bluetoothhfpclientdisconnect)
  * [bluetoothHfpClientGetConnectedDevices](#bluetoothhfpclientgetconnecteddevices)
  * [bluetoothHfpClientGetConnectionStatus](#bluetoothhfpclientgetconnectionstatus)

**BluetoothHidFacade**

  * [bluetoothHidIsReady](#bluetoothhidisready)
  * [bluetoothHidConnect](#bluetoothhidconnect)
  * [bluetoothHidDisconnect](#bluetoothhiddisconnect)
  * [bluetoothHidGetConnectedDevices](#bluetoothhidgetconnecteddevices)
  * [bluetoothHidGetConnectionStatus](#bluetoothhidgetconnectionstatus)
  * [bluetoothHidSetReport](#bluetoothhidsetreport)
  * [bluetoothHidGetReport](#bluetoothhidgetreport)
  * [bluetoothHidSendData](#bluetoothhidsenddata)
  * [bluetoothHidVirtualUnplug](#bluetoothhidvirtualunplug)
  * [testByte](#testbyte)

**BluetoothHspFacade**

  * [bluetoothHspIsReady](#bluetoothhspisready)
  * [bluetoothHspSetPriority](#bluetoothhspsetpriority)
  * [bluetoothHspConnect](#bluetoothhspconnect)
  * [bluetoothHspDisconnect](#bluetoothhspdisconnect)
  * [bluetoothHspGetConnectedDevices](#bluetoothhspgetconnecteddevices)
  * [bluetoothHspGetConnectionStatus](#bluetoothhspgetconnectionstatus)

**BluetoothLeAdvertiseFacade**

  * [bleGenBleAdvertiseCallback](#blegenbleadvertisecallback)
  * [bleBuildAdvertiseData](#blebuildadvertisedata)
  * [bleBuildAdvertiseSettings](#blebuildadvertisesettings)
  * [bleStopBleAdvertising](#blestopbleadvertising)
  * [bleStartBleAdvertising](#blestartbleadvertising)
  * [bleStartBleAdvertisingWithScanResponse](#blestartbleadvertisingwithscanresponse)
  * [bleGetAdvertiseSettingsMode](#blegetadvertisesettingsmode)
  * [bleGetAdvertiseSettingsTxPowerLevel](#blegetadvertisesettingstxpowerlevel)
  * [bleGetAdvertiseSettingsIsConnectable](#blegetadvertisesettingsisconnectable)
  * [bleGetAdvertiseDataIncludeTxPowerLevel](#blegetadvertisedataincludetxpowerlevel)
  * [bleGetAdvertiseDataManufacturerSpecificData](#blegetadvertisedatamanufacturerspecificdata)
  * [bleGetAdvertiseDataIncludeDeviceName](#blegetadvertisedataincludedevicename)
  * [bleGetAdvertiseDataServiceData](#blegetadvertisedataservicedata)
  * [bleGetAdvertiseDataServiceUuids](#blegetadvertisedataserviceuuids)
  * [bleSetAdvertiseDataSetServiceUuids](#blesetadvertisedatasetserviceuuids)
  * [bleAddAdvertiseDataServiceData](#bleaddadvertisedataservicedata)
  * [bleAddAdvertiseDataManufacturerId](#bleaddadvertisedatamanufacturerid)
  * [bleSetAdvertiseSettingsAdvertiseMode](#blesetadvertisesettingsadvertisemode)
  * [bleSetAdvertiseSettingsTxPowerLevel](#blesetadvertisesettingstxpowerlevel)
  * [bleSetAdvertiseSettingsIsConnectable](#blesetadvertisesettingsisconnectable)
  * [bleSetAdvertiseDataIncludeTxPowerLevel](#blesetadvertisedataincludetxpowerlevel)
  * [bleSetAdvertiseSettingsTimeout](#blesetadvertisesettingstimeout)
  * [bleSetAdvertiseDataIncludeDeviceName](#blesetadvertisedataincludedevicename)

**BluetoothLeScanFacade**

  * [bleGenScanCallback](#blegenscancallback)
  * [bleGenLeScanCallback](#blegenlescancallback)
  * [bleGenFilterList](#blegenfilterlist)
  * [bleBuildScanFilter](#blebuildscanfilter)
  * [bleBuildScanSetting](#blebuildscansetting)
  * [bleStopBleScan](#blestopblescan)
  * [bleStopClassicBleScan](#blestopclassicblescan)
  * [bleStartBleScan](#blestartblescan)
  * [bleStartClassicBleScan](#blestartclassicblescan)
  * [bleStartClassicBleScanWithServiceUuids](#blestartclassicblescanwithserviceuuids)
  * [bleFlushPendingScanResults](#bleflushpendingscanresults)
  * [bleSetScanSettingsCallbackType](#blesetscansettingscallbacktype)
  * [bleSetScanSettingsReportDelayMillis](#blesetscansettingsreportdelaymillis)
  * [bleSetScanSettingsScanMode](#blesetscansettingsscanmode)
  * [bleSetScanSettingsResultType](#blesetscansettingsresulttype)
  * [bleGetScanSettingsCallbackType](#blegetscansettingscallbacktype)
  * [bleGetScanSettingsReportDelayMillis](#blegetscansettingsreportdelaymillis)
  * [bleGetScanSettingsScanMode](#blegetscansettingsscanmode)
  * [bleGetScanSettingsScanResultType](#blegetscansettingsscanresulttype)
  * [bleGetScanFilterManufacturerId](#blegetscanfiltermanufacturerid)
  * [bleGetScanFilterDeviceAddress](#blegetscanfilterdeviceaddress)
  * [bleGetScanFilterDeviceName](#blegetscanfilterdevicename)
  * [bleGetScanFilterManufacturerData](#blegetscanfiltermanufacturerdata)
  * [bleGetScanFilterManufacturerDataMask](#blegetscanfiltermanufacturerdatamask)
  * [bleGetScanFilterServiceData](#blegetscanfilterservicedata)
  * [bleGetScanFilterServiceDataMask](#blegetscanfilterservicedatamask)
  * [bleGetScanFilterServiceUuid](#blegetscanfilterserviceuuid)
  * [bleGetScanFilterServiceUuidMask](#blegetscanfilterserviceuuidmask)
  * [bleSetScanFilterDeviceAddress](#blesetscanfilterdeviceaddress)
  * [bleSetScanFilterManufacturerData](#blesetscanfiltermanufacturerdata)
  * [bleSetScanFilterServiceData](#blesetscanfilterservicedata)
  * [bleSetScanFilterServiceUuid](#blesetscanfilterserviceuuid)
  * [bleSetScanFilterDeviceName](#blesetscanfilterdevicename)
  * [bleSetScanSettingsMatchMode](#blesetscansettingsmatchmode)
  * [bleGetScanSettingsMatchMode](#blegetscansettingsmatchmode)
  * [bleSetScanSettingsNumOfMatches](#blesetscansettingsnumofmatches)
  * [bleGetScanSettingsNumberOfMatches](#blegetscansettingsnumberofmatches)

**BluetoothMapClientFacade**

  * [bluetoothMapClientConnect](#bluetoothmapclientconnect)
  * [mapSendMessage](#mapsendmessage)
  * [bluetoothMapClientIsReady](#bluetoothmapclientisready)
  * [bluetoothMapClientDisconnect](#bluetoothmapclientdisconnect)
  * [bluetoothMapClientGetConnectedDevices](#bluetoothmapclientgetconnecteddevices)

**BluetoothMapFacade**

  * [bluetoothMapIsReady](#bluetoothmapisready)
  * [bluetoothMapDisconnect](#bluetoothmapdisconnect)
  * [bluetoothMapGetConnectedDevices](#bluetoothmapgetconnecteddevices)
  * [bluetoothMapGetClient](#bluetoothmapgetclient)

**BluetoothMediaFacade**

  * [bluetoothMediaPassthrough](#bluetoothmediapassthrough)
  * [bluetoothMediaGetCurrentMediaMetaData](#bluetoothmediagetcurrentmediametadata)
  * [bluetoothMediaGetActiveMediaSessions](#bluetoothmediagetactivemediasessions)
  * [bluetoothMediaConnectToCarMBS](#bluetoothmediaconnecttocarmbs)
  * [bluetoothMediaPhoneSL4AMBSStart](#bluetoothmediaphonesl4ambsstart)
  * [bluetoothMediaPhoneSL4AMBSStop](#bluetoothmediaphonesl4ambsstop)
  * [bluetoothMediaHandleMediaCommandOnPhone](#bluetoothmediahandlemediacommandonphone)

**BluetoothPanFacade**

  * [bluetoothPanSetBluetoothTethering](#bluetoothpansetbluetoothtethering)
  * [bluetoothPanIsReady](#bluetoothpanisready)
  * [bluetoothPanGetConnectedDevices](#bluetoothpangetconnecteddevices)
  * [bluetoothPanIsTetheringOn](#bluetoothpanistetheringon)

**BluetoothPbapClientFacade**

  * [bluetoothPbapClientIsReady](#bluetoothpbapclientisready)
  * [bluetoothPbapClientSetPriority](#bluetoothpbapclientsetpriority)
  * [bluetoothPbapClientGetPriority](#bluetoothpbapclientgetpriority)
  * [bluetoothPbapClientConnect](#bluetoothpbapclientconnect)
  * [bluetoothPbapClientDisconnect](#bluetoothpbapclientdisconnect)
  * [bluetoothPbapClientGetConnectedDevices](#bluetoothpbapclientgetconnecteddevices)
  * [bluetoothPbapClientGetConnectionStatus](#bluetoothpbapclientgetconnectionstatus)

**BluetoothRfcommFacade**

  * [bluetoothRfcommBeginConnectThread](#bluetoothrfcommbeginconnectthread)
  * [bluetoothRfcommKillConnThread](#bluetoothrfcommkillconnthread)
  * [bluetoothRfcommEndConnectThread](#bluetoothrfcommendconnectthread)
  * [bluetoothRfcommEndAcceptThread](#bluetoothrfcommendacceptthread)
  * [bluetoothRfcommActiveConnections](#bluetoothrfcommactiveconnections)
  * [bluetoothRfcommGetConnectedDeviceName](#bluetoothrfcommgetconnecteddevicename)
  * [bluetoothRfcommBeginAcceptThread](#bluetoothrfcommbeginacceptthread)
  * [bluetoothRfcommWrite](#bluetoothrfcommwrite)
  * [bluetoothRfcommRead](#bluetoothrfcommread)
  * [bluetoothRfcommWriteBinary](#bluetoothrfcommwritebinary)
  * [bluetoothRfcommReadBinary](#bluetoothrfcommreadbinary)
  * [bluetoothRfcommReadReady](#bluetoothrfcommreadready)
  * [bluetoothRfcommReadLine](#bluetoothrfcommreadline)
  * [bluetoothRfcommStop](#bluetoothrfcommstop)

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

  * [contactsDisplayContactPickList](#contactsdisplaycontactpicklist)
  * [contactsDisplayPhonePickList](#contactsdisplayphonepicklist)
  * [contactsGetAttributes](#contactsgetattributes)
  * [contactsGetContactIds](#contactsgetcontactids)
  * [contactsGetAllContacts](#contactsgetallcontacts)
  * [contactsGetContactById](#contactsgetcontactbyid)
  * [contactsGetCount](#contactsgetcount)
  * [contactsEraseAll](#contactseraseall)
  * [contactsQueryContent](#contactsquerycontent)
  * [queryAttributes](#queryattributes)
  * [importVcf](#importvcf)
  * [exportVcf](#exportvcf)

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

**GattClientFacade**

  * [gattClientConnectGatt](#gattclientconnectgatt)
  * [gattClientDiscoverServices](#gattclientdiscoverservices)
  * [gattClientGetServices](#gattclientgetservices)
  * [gattClientAbortReliableWrite](#gattclientabortreliablewrite)
  * [gattClientBeginReliableWrite](#gattclientbeginreliablewrite)
  * [gattClientRequestMtu](#gattclientrequestmtu)
  * [gattClientDisconnect](#gattclientdisconnect)
  * [gattClientClose](#gattclientclose)
  * [gattExecuteReliableWrite](#gattexecutereliablewrite)
  * [gattClientGetConnectedDevices](#gattclientgetconnecteddevices)
  * [gattGetDevice](#gattgetdevice)
  * [gattClientGetDevicesMatchingConnectionStates](#gattclientgetdevicesmatchingconnectionstates)
  * [gattClientGetServiceUuidList](#gattclientgetserviceuuidlist)
  * [gattClientReadCharacteristic](#gattclientreadcharacteristic)
  * [gattClientReadDescriptor](#gattclientreaddescriptor)
  * [gattClientWriteDescriptor](#gattclientwritedescriptor)
  * [gattClientDescriptorSetValue](#gattclientdescriptorsetvalue)
  * [gattClientWriteCharacteristic](#gattclientwritecharacteristic)
  * [gattClientCharacteristicSetValue](#gattclientcharacteristicsetvalue)
  * [gattClientCharacteristicSetWriteType](#gattclientcharacteristicsetwritetype)
  * [gattClientReadRSSI](#gattclientreadrssi)
  * [gattClientRefresh](#gattclientrefresh)
  * [gattClientRequestConnectionPriority](#gattclientrequestconnectionpriority)
  * [gattClientSetCharacteristicNotification](#gattclientsetcharacteristicnotification)
  * [gattCreateGattCallback](#gattcreategattcallback)
  * [gattClientGetDiscoveredServicesCount](#gattclientgetdiscoveredservicescount)
  * [gattClientGetDiscoveredServiceUuid](#gattclientgetdiscoveredserviceuuid)
  * [gattClientGetDiscoveredCharacteristicUuids](#gattclientgetdiscoveredcharacteristicuuids)
  * [gattClientGetDiscoveredDescriptorUuids](#gattclientgetdiscovereddescriptoruuids)

**GattServerFacade**

  * [gattServerOpenGattServer](#gattserveropengattserver)
  * [gattServerAddService](#gattserveraddservice)
  * [gattServerClearServices](#gattserverclearservices)
  * [gattServerGetConnectedDevices](#gattservergetconnecteddevices)
  * [gattServerSendResponse](#gattserversendresponse)
  * [gattServerNotifyCharacteristicChanged](#gattservernotifycharacteristicchanged)
  * [gattServerCreateService](#gattservercreateservice)
  * [gattServiceAddCharacteristic](#gattserviceaddcharacteristic)
  * [gattServerAddCharacteristicToService](#gattserveraddcharacteristictoservice)
  * [gattServerClose](#gattserverclose)
  * [gattGetConnectedDevices](#gattgetconnecteddevices)
  * [gattGetServiceUuidList](#gattgetserviceuuidlist)
  * [gattGetService](#gattgetservice)
  * [gattServerCharacteristicAddDescriptor](#gattservercharacteristicadddescriptor)
  * [gattServerCreateBluetoothGattCharacteristic](#gattservercreatebluetoothgattcharacteristic)
  * [gattServerCharacteristicSetValue](#gattservercharacteristicsetvalue)
  * [gattServerCreateGattServerCallback](#gattservercreategattservercallback)
  * [gattServerCreateBluetoothGattDescriptor](#gattservercreatebluetoothgattdescriptor)

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

**TelecomCallFacade**

  * [telecomCallGetCallById](#telecomcallgetcallbyid)

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

  * [wifiAddNetwork](#wifiaddnetwork)
  * [wifiConnect](#wificonnect)
  * [wifiEnableNetwork](#wifienablenetwork)
  * [wifiEnterpriseConnect](#wifienterpriseconnect)
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

## bluetoothMediaPassthrough

```
void bluetoothMediaPassthrough( String passthruCmd)

Simulate a passthrough command
```

## bluetoothMediaGetCurrentMediaMetaData

```
Map<String, String> bluetoothMediaGetCurrentMediaMetaData()

Gets the Metadata of currently playing Media
```

## bluetoothMediaGetActiveMediaSessions

```
List<String> bluetoothMediaGetActiveMediaSessions()

Get the current active Media Sessions
```

## bluetoothMediaConnectToCarMBS

```
void bluetoothMediaConnectToCarMBS()

Connect a MediaBrowser to the A2dpMediaBrowserservice in the Carkitt
```

## bluetoothMediaPhoneSL4AMBSStart

```
void bluetoothMediaPhoneSL4AMBSStart()

Start the BluetoothSL4AAudioSrcMBS on Phone.
```

## bluetoothMediaPhoneSL4AMBSStop

```
void bluetoothMediaPhoneSL4AMBSStop()

Stop the BluetoothSL4AAudioSrcMBS running on Phone.
```

## bluetoothMediaHandleMediaCommandOnPhone

```
void bluetoothMediaHandleMediaCommandOnPhone(String command)

Media Commands on the Phone's BluetoothAvrcpMBS.
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

## bluetoothPbapClientIsReady

```
Boolean bluetoothPbapClientIsReady()

Is PbapClient profile ready.
```

## bluetoothPbapClientSetPriority

```
void bluetoothPbapClientSetPriority( String deviceStr, Integer priority)

Set priority of the profile
```

## bluetoothPbapClientGetPriority

```
Integer bluetoothPbapClientGetPriority( String deviceStr)

Get priority of the profile
```

## bluetoothPbapClientConnect

```
Boolean bluetoothPbapClientConnect( String deviceStr)

Connect to an PBAP Client device.
```

## bluetoothPbapClientDisconnect

```
Boolean bluetoothPbapClientDisconnect( String deviceStr)

Disconnect an PBAP Client device.
```

## bluetoothPbapClientGetConnectedDevices

```
List<BluetoothDevice> bluetoothPbapClientGetConnectedDevices()

Get all the devices connected through PBAP Client.
```

## bluetoothPbapClientGetConnectionStatus

```
Integer bluetoothPbapClientGetConnectionStatus( String deviceID)

Get the connection status of a device.
```

## bluetoothStartConnectionStateChangeMonitor

```
void bluetoothStartConnectionStateChangeMonitor( String deviceID)

Start monitoring state changes for input device.
```

## bluetoothStartPairingHelper

```
void bluetoothStartPairingHelper( Boolean autoConfirm)

Start intercepting all bluetooth connection pop-ups.
```

## bluetoothGetConnectedDevices

```
List<BluetoothDevice> bluetoothGetConnectedDevices()

Return a list of devices connected through bluetooth
```

## bluetoothGetConnectedLeDevices

```
List<BluetoothDevice> bluetoothGetConnectedLeDevices(Integer profile)

Return a list of devices connected through bluetooth LE
```

## bluetoothIsDeviceConnected

```
Boolean bluetoothIsDeviceConnected(String deviceID)

Return true if a bluetooth device is connected.
```

## bluetoothGetConnectedDevicesOnProfile

```
List<BluetoothDevice> bluetoothGetConnectedDevicesOnProfile( Integer profileId)

Return list of connected bluetooth devices over a profile

Returns List of devices connected over the profile
```

## bluetoothDiscoverAndConnect

```
Boolean bluetoothDiscoverAndConnect( String deviceID)

Connect to a specified device once it's discovered.

Returns Whether discovery started successfully.
```

## bluetoothDiscoverAndBond

```
Boolean bluetoothDiscoverAndBond( String deviceID)

Bond to a specified device once it's discovered.

Returns Whether discovery started successfully. 
```

## bluetoothUnbond

```
Boolean bluetoothUnbond( String deviceID)

Unbond a device.

Returns Whether the device was successfully unbonded.
```

## bluetoothConnectBonded

```
void bluetoothConnectBonded( String deviceID)

Connect to a device that is already bonded.
```

## bluetoothDisconnectConnected

```
void bluetoothDisconnectConnected( String deviceID)

Disconnect from a device that is already connected.
```

## bluetoothDisconnectConnectedProfile

```
void bluetoothDisconnectConnectedProfile( String deviceID, JSONArray profileSet )

Disconnect on a profile from a device that is already connected.
```

## bluetoothChangeProfileAccessPermission

```
void bluetoothChangeProfileAccessPermission( String deviceID, Integer profileID, Integer access )

Change permissions for a profile.
```

## bleGenScanCallback

```
Integer bleGenScanCallback()

Generate a new myScanCallback Object
```

## bleGenLeScanCallback

```
Integer bleGenLeScanCallback()

Generate a new myScanCallback Object
```

## bleGenFilterList

```
Integer bleGenFilterList()

Generate a new Filter list
```

## bleBuildScanFilter

```
Integer bleBuildScanFilter( Integer filterIndex )

Generate a new Filter list
```

## bleBuildScanSetting

```
Integer bleBuildScanSetting()

Generate a new scan settings Object
```

## bleStopBleScan

```
void bleStopBleScan( Integer index)

Stops an ongoing ble advertisement scan
```

## bleStopClassicBleScan

```
void bleStopClassicBleScan( Integer index)

Stops an ongoing classic ble scan
```

## bleStartBleScan

```
void bleStartBleScan( Integer filterListIndex, Integer scanSettingsIndex, Integer callbackIndex )

Starts a ble advertisement scan
```

## bleStartClassicBleScan

```
boolean bleStartClassicBleScan( Integer leCallbackIndex )

Starts a classic ble advertisement scan
```

## bleStartClassicBleScanWithServiceUuids

```
boolean bleStartClassicBleScanWithServiceUuids( Integer leCallbackIndex, String[] serviceUuidList )

Starts a classic ble advertisement scan with service Uuids
```

## bleFlushPendingScanResults

```
void bleFlushPendingScanResults( Integer callbackIndex )

Gets the results of the ble ScanCallback
```

## bleSetScanSettingsCallbackType

```
void bleSetScanSettingsCallbackType( Integer callbackType)

Set the scan setting's callback type
```

## bleSetScanSettingsReportDelayMillis

```
void bleSetScanSettingsReportDelayMillis( Long reportDelayMillis)

Set the scan setting's report delay millis
```

## bleSetScanSettingsScanMode

```
void bleSetScanSettingsScanMode( Integer scanMode)

Set the scan setting's scan mode
```

## bleSetScanSettingsResultType

```
void bleSetScanSettingsResultType( Integer scanResultType)

Set the scan setting's scan result type
```

## bleGetScanSettingsCallbackType

```
Integer bleGetScanSettingsCallbackType( Integer index )

Get ScanSetting's callback type
```

## bleGetScanSettingsReportDelayMillis

```
Long bleGetScanSettingsReportDelayMillis( Integer index)

Get ScanSetting's report delay milliseconds
```

## bleGetScanSettingsScanMode

```
Integer bleGetScanSettingsScanMode( Integer index)

Get ScanSetting's scan mode
```

## bleGetScanSettingsScanResultType

```
Integer bleGetScanSettingsScanResultType( Integer index)

Get ScanSetting's scan result type
```

## bleGetScanFilterManufacturerId

```
Integer bleGetScanFilterManufacturerId( Integer index, Integer filterIndex)

Get ScanFilter's Manufacturer Id
```

## bleGetScanFilterDeviceAddress

```
String bleGetScanFilterDeviceAddress( Integer index, Integer filterIndex)

Get ScanFilter's device address
```

## bleGetScanFilterDeviceName

```
String bleGetScanFilterDeviceName( Integer index, Integer filterIndex)

Get ScanFilter's device name
```

## bleGetScanFilterManufacturerData

```
byte[] bleGetScanFilterManufacturerData( Integer index, Integer filterIndex)

Get ScanFilter's manufacturer data
```

## bleGetScanFilterManufacturerDataMask

```
byte[] bleGetScanFilterManufacturerDataMask( Integer index, Integer filterIndex)

Get ScanFilter's manufacturer data mask
```

## bleGetScanFilterServiceData

```
byte[] bleGetScanFilterServiceData( Integer index, Integer filterIndex)

Get ScanFilter's service data
```

## bleGetScanFilterServiceDataMask

```
byte[] bleGetScanFilterServiceDataMask( Integer index, Integer filterIndex)

Get ScanFilter's service data mask
```

## bleGetScanFilterServiceUuid

```
String bleGetScanFilterServiceUuid( Integer index, Integer filterIndex)

Get ScanFilter's service uuid
```

## bleGetScanFilterServiceUuidMask

```
String bleGetScanFilterServiceUuidMask( Integer index, Integer filterIndex)

Get ScanFilter's service uuid mask
```

## bleSetScanFilterDeviceAddress

```
void bleSetScanFilterDeviceAddress( String macAddress )

Add filter \"macAddress\" to existing ScanFilter
```

## bleSetScanFilterManufacturerData

```
void bleSetScanFilterManufacturerData( Integer manufacturerDataId, byte[] manufacturerData, byte[] manufacturerDataMask )

Add filter \"manufacturereDataId and/or manufacturerData\" to existing ScanFilter
```

## bleSetScanFilterServiceData

```
void bleSetScanFilterServiceData( String serviceUuid, byte[] serviceData, byte[] serviceDataMask )

Add filter \"serviceData and serviceDataMask\" to existing ScanFilter 
```

## bleSetScanFilterServiceUuid

```
void bleSetScanFilterServiceUuid( String serviceUuid, String serviceMask )

Add filter \"serviceUuid and/or serviceMask\" to existing ScanFilter
```

## bleSetScanFilterDeviceName

```
void bleSetScanFilterDeviceName( String name )

Sets the scan filter's device name
```

## bleSetScanSettingsMatchMode

```
void bleSetScanSettingsMatchMode( Integer mode)

Set the scan setting's match mode
```

## bleGetScanSettingsMatchMode

```
int bleGetScanSettingsMatchMode( Integer scanSettingsIndex )

Get the scan setting's match mode
```

## bleSetScanSettingsNumOfMatches

```
void bleSetScanSettingsNumOfMatches( Integer matches)

Set the scan setting's number of matches
```

## bleGetScanSettingsNumberOfMatches

```
int bleGetScanSettingsNumberOfMatches( Integer scanSettingsIndex)

Get the scan setting's number of matches
```

## telecomCallGetCallById

```
Call telecomCallGetCallById(String callId)

Get call by particular Id
```

## bluetoothMapIsReady

```
Boolean bluetoothMapIsReady()

Is Map profile ready.
```

## bluetoothMapDisconnect

```
Boolean bluetoothMapDisconnect( String deviceID)

Disconnect an MAP device.
```

## bluetoothMapGetConnectedDevices

```
List<BluetoothDevice> bluetoothMapGetConnectedDevices()

Get all the devices connected through MAP.
```

## bluetoothMapGetClient

```
BluetoothDevice bluetoothMapGetClient()

Get the currently connected remote Bluetooth device (PCE).
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

## gattServerOpenGattServer

```
int gattServerOpenGattServer(Integer index)

Open new gatt server
```

## gattServerAddService

```
void gattServerAddService(Integer index, Integer serviceIndex)

Add service to bluetooth gatt server
```

## gattServerClearServices

```
void gattServerClearServices( Integer index)

Clear services from bluetooth gatt server
```

## gattServerGetConnectedDevices

```
List<BluetoothDevice> gattServerGetConnectedDevices( Integer gattServerIndex)

Return a list of connected gatt devices.
```

## gattServerSendResponse

```
void gattServerSendResponse( Integer gattServerIndex, Integer bluetoothDeviceIndex, Integer requestId, Integer status, Integer offset, byte[] value)

Send a response after a write.
```

## gattServerNotifyCharacteristicChanged

```
void gattServerNotifyCharacteristicChanged( Integer gattServerIndex, Integer bluetoothDeviceIndex, Integer characteristicIndex, Boolean confirm)

Notify that characteristic was changed.
```

## gattServerCreateService

```
int gattServerCreateService(String uuid, Integer serviceType)

Create new bluetooth gatt service
```

## gattServiceAddCharacteristic

```
void gattServiceAddCharacteristic( Integer index, String serviceUuid, Integer characteristicIndex)

Add a characteristic to a bluetooth gatt service
```

## gattServerAddCharacteristicToService

```
void gattServerAddCharacteristicToService(Integer index, Integer characteristicIndex  )

Add a characteristic to a bluetooth gatt service
```

## gattServerClose

```
void gattServerClose(Integer index)

Close a bluetooth gatt
```

## gattGetConnectedDevices

```
List<BluetoothDevice> gattGetConnectedDevices( Integer index)

Get a list of Bluetooth Devices connnected to the bluetooth gatt
```

## gattGetServiceUuidList

```
ArrayList<String> gattGetServiceUuidList(Integer index)

Get the service from an input UUID
```

## gattGetService

```
BluetoothGattService gattGetService(Integer index, String uuid)

Get the service from an input UUID
```

## gattServerCharacteristicAddDescriptor

```
void gattServerCharacteristicAddDescriptor(Integer index, Integer descriptorIndex)

add descriptor to blutooth gatt characteristic
```

## gattServerCreateBluetoothGattCharacteristic

```
int gattServerCreateBluetoothGattCharacteristic( String characteristicUuid, Integer property, Integer permission)

Create a new Characteristic object
```

## gattServerCharacteristicSetValue

```
void gattServerCharacteristicSetValue(Integer index, byte[] value)

add descriptor to blutooth gatt characteristic
```

## gattServerCreateGattServerCallback

```
Integer gattServerCreateGattServerCallback()

Create a new GattCallback object
```

## gattServerCreateBluetoothGattDescriptor

```
int gattServerCreateBluetoothGattDescriptor( String descriptorUuid, Integer permissions)

Create a new Descriptor object
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

## bluetoothMapClientConnect

```
Boolean bluetoothMapClientConnect( String device)

Connect to an MAP MSE device.
```

## mapSendMessage

```
Boolean mapSendMessage( String deviceID, String[] phoneNumbers, String message)

Send a (text) message via bluetooth.
```

## bluetoothMapClientIsReady

```
Boolean bluetoothMapClientIsReady()

Is Map profile ready.
```

## bluetoothMapClientDisconnect

```
Boolean bluetoothMapClientDisconnect( String deviceID)

Disconnect an MAP device.
```

## bluetoothMapClientGetConnectedDevices

```
List<BluetoothDevice> bluetoothMapClientGetConnectedDevices()

Get all the devices connected through MAP.
```

## bleGenBleAdvertiseCallback

```
Integer bleGenBleAdvertiseCallback()

Generate a new myAdvertisement Object
```

## bleBuildAdvertiseData

```
Integer bleBuildAdvertiseData()

Constructs a new Builder obj for AdvertiseData and returns its index
```

## bleBuildAdvertiseSettings

```
Integer bleBuildAdvertiseSettings()

Constructs a new Builder obj for AdvertiseData and returns its index
```

## bleStopBleAdvertising

```
void bleStopBleAdvertising( Integer index)

Stops an ongoing ble advertisement
```

## bleStartBleAdvertising

```
void bleStartBleAdvertising( Integer callbackIndex, Integer dataIndex, Integer settingsIndex )

Starts ble advertisement
```

## bleStartBleAdvertisingWithScanResponse

```
void bleStartBleAdvertisingWithScanResponse( Integer callbackIndex, Integer dataIndex, Integer settingsIndex, Integer scanResponseIndex )

Starts ble advertisement
```

## bleGetAdvertiseSettingsMode

```
int bleGetAdvertiseSettingsMode( Integer index)

Get ble advertisement settings mode
```

## bleGetAdvertiseSettingsTxPowerLevel

```
int bleGetAdvertiseSettingsTxPowerLevel( Integer index)

Get ble advertisement settings tx power level
```

## bleGetAdvertiseSettingsIsConnectable

```
boolean bleGetAdvertiseSettingsIsConnectable( Integer index)

Get ble advertisement settings isConnectable value
```

## bleGetAdvertiseDataIncludeTxPowerLevel

```
Boolean bleGetAdvertiseDataIncludeTxPowerLevel( Integer index)

Get ble advertisement data include tx power level
```

## bleGetAdvertiseDataManufacturerSpecificData

```
byte[] bleGetAdvertiseDataManufacturerSpecificData( Integer index, Integer manufacturerId)

Get ble advertisement data manufacturer specific data
```

## bleGetAdvertiseDataIncludeDeviceName

```
Boolean bleGetAdvertiseDataIncludeDeviceName( Integer index)

Get ble advertisement include device name
```

## bleGetAdvertiseDataServiceData

```
byte[] bleGetAdvertiseDataServiceData( Integer index, String serviceUuid)

Get ble advertisement Service Data
```

## bleGetAdvertiseDataServiceUuids

```
List<ParcelUuid> bleGetAdvertiseDataServiceUuids( Integer index)

Get ble advertisement Service Uuids
```

## bleSetAdvertiseDataSetServiceUuids

```
void bleSetAdvertiseDataSetServiceUuids( String[] uuidList )

Set ble advertisement data service uuids
```

## bleAddAdvertiseDataServiceData

```
void bleAddAdvertiseDataServiceData( String serviceDataUuid, byte[] serviceData )

Set ble advertise data service uuids
```

## bleAddAdvertiseDataManufacturerId

```
void bleAddAdvertiseDataManufacturerId( Integer manufacturerId, byte[] manufacturerSpecificData )

Set ble advertise data manufacturerId
```

## bleSetAdvertiseSettingsAdvertiseMode

```
void bleSetAdvertiseSettingsAdvertiseMode( Integer advertiseMode )

Set ble advertise settings advertise mode
```

## bleSetAdvertiseSettingsTxPowerLevel

```
void bleSetAdvertiseSettingsTxPowerLevel( Integer txPowerLevel )

Set ble advertise settings tx power level
```

## bleSetAdvertiseSettingsIsConnectable

```
void bleSetAdvertiseSettingsIsConnectable( Boolean value )

Set ble advertise settings isConnectable value
```

## bleSetAdvertiseDataIncludeTxPowerLevel

```
void bleSetAdvertiseDataIncludeTxPowerLevel( Boolean includeTxPowerLevel )

Set ble advertisement data include tx power level
```

## bleSetAdvertiseSettingsTimeout

```
void bleSetAdvertiseSettingsTimeout( Integer timeoutSeconds )

Set ble advertisement data include tx power level
```

## bleSetAdvertiseDataIncludeDeviceName

```
void bleSetAdvertiseDataIncludeDeviceName( Boolean includeDeviceName )

Set ble advertisement data include device name
```

## bluetoothRfcommBeginConnectThread

```
void bluetoothRfcommBeginConnectThread( String address, String uuid)

Begins a thread initiate an Rfcomm connection over Bluetooth. 
```

## bluetoothRfcommKillConnThread

```
void bluetoothRfcommKillConnThread()

Kill thread
```

## bluetoothRfcommEndConnectThread

```
void bluetoothRfcommEndConnectThread()

Close an active Rfcomm Client socket
```

## bluetoothRfcommEndAcceptThread

```
void bluetoothRfcommEndAcceptThread()

Close an active Rfcomm Server socket
```

## bluetoothRfcommActiveConnections

```
Map<String, String> bluetoothRfcommActiveConnections()

Returns active Bluetooth connections.
```

## bluetoothRfcommGetConnectedDeviceName

```
String bluetoothRfcommGetConnectedDeviceName( String connID)

Returns the name of the connected device.
```

## bluetoothRfcommBeginAcceptThread

```
void bluetoothRfcommBeginAcceptThread( String uuid, Integer timeout)

Begins a thread to accept an Rfcomm connection over Bluetooth. 
```

## bluetoothRfcommWrite

```
void bluetoothRfcommWrite(String ascii, String connID)

Sends ASCII characters over the currently open Bluetooth connection.
```

## bluetoothRfcommRead

```
String bluetoothRfcommRead( Integer bufferSize, String connID)

Read up to bufferSize ASCII characters.
```

## bluetoothRfcommWriteBinary

```
void bluetoothRfcommWriteBinary( String base64, String connID)

Send bytes over the currently open Bluetooth connection.
```

## bluetoothRfcommReadBinary

```
String bluetoothRfcommReadBinary( Integer bufferSize, String connID)

Read up to bufferSize bytes and return a chunked, base64 encoded string.
```

## bluetoothRfcommReadReady

```
Boolean bluetoothRfcommReadReady( String connID)

Returns True if the next read is guaranteed not to block.
```

## bluetoothRfcommReadLine

```
String bluetoothRfcommReadLine( String connID)

Read the next line.
```

## bluetoothRfcommStop

```
void bluetoothRfcommStop( String connID)

Stops Bluetooth connection.
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

## bluetoothAvrcpIsReady

```
Boolean bluetoothAvrcpIsReady()

Is Avrcp profile ready.
```

## bluetoothAvrcpGetConnectedDevices

```
List<BluetoothDevice> bluetoothAvrcpGetConnectedDevices()

Get all the devices connected through AVRCP.
```

## bluetoothAvrcpDisconnect

```
void bluetoothAvrcpDisconnect()

Close AVRCP connection.
```

## bluetoothHfpClientIsReady

```
Boolean bluetoothHfpClientIsReady()

Is HfpClient profile ready.
```

## bluetoothHfpClientSetPriority

```
void bluetoothHfpClientSetPriority( String deviceStr, Integer priority)

Set priority of the profile
```

## bluetoothHfpClientGetPriority

```
Integer bluetoothHfpClientGetPriority( String deviceStr)

Get priority of the profile
```

## bluetoothHfpClientConnect

```
Boolean bluetoothHfpClientConnect( String deviceStr)

Connect to an HFP Client device.
```

## bluetoothHfpClientDisconnect

```
Boolean bluetoothHfpClientDisconnect( String deviceStr)

Disconnect an HFP Client device.
```

## bluetoothHfpClientGetConnectedDevices

```
List<BluetoothDevice> bluetoothHfpClientGetConnectedDevices()

Get all the devices connected through HFP Client.
```

## bluetoothHfpClientGetConnectionStatus

```
Integer bluetoothHfpClientGetConnectionStatus( String deviceID)

Get the connection status of a device.
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

## ttsSpeak

```
void ttsSpeak(String message)

Speaks the provided message via TTS.
```

## gattClientConnectGatt

```
int gattClientConnectGatt( Integer index, String macAddress, Boolean autoConnect, Integer transport )

Create a gatt connection
```

## gattClientDiscoverServices

```
boolean gattClientDiscoverServices( Integer index )

Trigger discovering of services on the BluetoothGatt object
```

## gattClientGetServices

```
List<BluetoothGattService> gattClientGetServices( Integer index )

Get the services from the BluetoothGatt object
```

## gattClientAbortReliableWrite

```
void gattClientAbortReliableWrite( Integer index )

Abort reliable write of a bluetooth gatt
```

## gattClientBeginReliableWrite

```
boolean gattClientBeginReliableWrite( Integer index )

Begin reliable write of a bluetooth gatt
```

## gattClientRequestMtu

```
boolean gattClientRequestMtu( Integer index, Integer mtu )

true, if the new MTU value has been requested successfully
```

## gattClientDisconnect

```
void gattClientDisconnect( Integer index )

Disconnect a bluetooth gatt
```

## gattClientClose

```
void gattClientClose( Integer index)

Close a Bluetooth GATT object
```

## gattExecuteReliableWrite

```
boolean gattExecuteReliableWrite( Integer index )

Execute reliable write on a bluetooth gatt
```

## gattClientGetConnectedDevices

```
List<BluetoothDevice> gattClientGetConnectedDevices( Integer index )

Get a list of Bluetooth Devices connnected to the bluetooth gatt
```

## gattGetDevice

```
BluetoothDevice gattGetDevice( Integer index )

Get the remote bluetooth device this GATT client targets to
```

## gattClientGetDevicesMatchingConnectionStates

```
List<BluetoothDevice> gattClientGetDevicesMatchingConnectionStates( Integer index, int[] states )

Get the bluetooth devices matching input connection states
```

## gattClientGetServiceUuidList

```
ArrayList<String> gattClientGetServiceUuidList( Integer index )

Get the service from an input UUID
```

## gattClientReadCharacteristic

```
boolean gattClientReadCharacteristic( Integer gattIndex, Integer discoveredServiceListIndex, Integer serviceIndex, String characteristicUuid)

Reads the requested characteristic from the associated remote device.
```

## gattClientReadDescriptor

```
boolean gattClientReadDescriptor( Integer gattIndex, Integer discoveredServiceListIndex, Integer serviceIndex, String characteristicUuid, String descriptorUuid)

Reads the value for a given descriptor from the associated remote device
```

## gattClientWriteDescriptor

```
boolean gattClientWriteDescriptor( Integer gattIndex, Integer discoveredServiceListIndex, Integer serviceIndex, String characteristicUuid, String descriptorUuid)

Write the value of a given descriptor to the associated remote device
```

## gattClientDescriptorSetValue

```
boolean gattClientDescriptorSetValue( Integer gattIndex, Integer discoveredServiceListIndex, Integer serviceIndex, String characteristicUuid, String descriptorUuid, String value)

Write the value of a given descriptor to the associated remote device
```

## gattClientWriteCharacteristic

```
boolean gattClientWriteCharacteristic( Integer gattIndex, Integer discoveredServiceListIndex, Integer serviceIndex, String characteristicUuid)

Write the value of a given characteristic to the associated remote device
```

## gattClientCharacteristicSetValue

```
boolean gattClientCharacteristicSetValue( Integer gattIndex, Integer discoveredServiceListIndex, Integer serviceIndex, String characteristicUuid, String value)

Write the value of a given characteristic to the associated remote device
```

## gattClientCharacteristicSetWriteType

```
boolean gattClientCharacteristicSetWriteType( Integer gattIndex, Integer discoveredServiceListIndex, Integer serviceIndex, String characteristicUuid, Integer writeType)

Set write type of a given characteristic to the associated remote device
```

## gattClientReadRSSI

```
boolean gattClientReadRSSI( Integer index )

Read the RSSI for a connected remote device
```

## gattClientRefresh

```
boolean gattClientRefresh( Integer index )

Clears the internal cache and forces a refresh of the services from the remote device
```

## gattClientRequestConnectionPriority

```
boolean gattClientRequestConnectionPriority( Integer index, Integer connectionPriority )

Request a connection parameter update. from the Bluetooth Gatt
```

## gattClientSetCharacteristicNotification

```
boolean gattClientSetCharacteristicNotification( Integer gattIndex, Integer discoveredServiceListIndex, Integer serviceIndex, String characteristicUuid, Boolean enable )

Sets the characteristic notification of a bluetooth gatt
```

## gattCreateGattCallback

```
Integer gattCreateGattCallback()

Create a new GattCallback object
```

## gattClientGetDiscoveredServicesCount

```
int gattClientGetDiscoveredServicesCount( Integer index )

Get Bluetooth Gatt Services
```

## gattClientGetDiscoveredServiceUuid

```
String gattClientGetDiscoveredServiceUuid( Integer index, Integer serviceIndex )

Get Bluetooth Gatt Service Uuid
```

## gattClientGetDiscoveredCharacteristicUuids

```
ArrayList<String> gattClientGetDiscoveredCharacteristicUuids( Integer index, Integer serviceIndex )

Get Bluetooth Gatt Services
```

## gattClientGetDiscoveredDescriptorUuids

```
ArrayList<String> gattClientGetDiscoveredDescriptorUuids ( Integer index, Integer serviceIndex, String characteristicUuid )

Get Bluetooth Gatt Services
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

## recognizeSpeech

```
String recognizeSpeech( final String prompt, final String language, final String languageModel)

Recognizes user's speech and returns the most likely result.

Returns An empty string in case the speech cannot be recongnized.
```

## generateDtmfTones

```
void generateDtmfTones( String phoneNumber, Integer toneDuration)

Generate DTMF tones for the given phone number.
```

## bluetoothHspIsReady

```
Boolean bluetoothHspIsReady()

Is Hsp profile ready.
```

## bluetoothHspSetPriority

```
void bluetoothHspSetPriority( String deviceStr, Integer priority)

Set priority of the profile
```

## bluetoothHspConnect

```
Boolean bluetoothHspConnect( String device)

Connect to an HSP device.
```

## bluetoothHspDisconnect

```
Boolean bluetoothHspDisconnect( String device)

Disconnect an HSP device.
```

## bluetoothHspGetConnectedDevices

```
List<BluetoothDevice> bluetoothHspGetConnectedDevices()

Get all the devices connected through HSP.
```

## bluetoothHspGetConnectionStatus

```
Integer bluetoothHspGetConnectionStatus( String deviceID)

Get the connection status of a device.
```

## bluetoothHidIsReady

```
Boolean bluetoothHidIsReady()

Is Hid profile ready.
```

## bluetoothHidConnect

```
Boolean bluetoothHidConnect( String device)

Connect to an HID device.
```

## bluetoothHidDisconnect

```
Boolean bluetoothHidDisconnect( String device)

Disconnect an HID device.
```

## bluetoothHidGetConnectedDevices

```
List<BluetoothDevice> bluetoothHidGetConnectedDevices()

Get all the devices connected through HID.
```

## bluetoothHidGetConnectionStatus

```
Integer bluetoothHidGetConnectionStatus( String deviceID)

Get the connection status of a device.
```

## bluetoothHidSetReport

```
Boolean bluetoothHidSetReport( String deviceID, String type, String report)

Send Set_Report command to the connected HID input device.
```

## bluetoothHidGetReport

```
Boolean bluetoothHidGetReport( String deviceID, String type, String reportId, Integer buffSize)

Send Get_Report command to the connected HID input device.
```

## bluetoothHidSendData

```
Boolean bluetoothHidSendData( String deviceID, String report)

Send data to a connected HID device.
```

## bluetoothHidVirtualUnplug

```
Boolean bluetoothHidVirtualUnplug( String deviceID)

Send virtual unplug to a connected HID device.
```

## testByte

```
byte[] testByte()

Test byte transfer.
```

## bluetoothA2dpIsReady

```
Boolean bluetoothA2dpIsReady()

Is A2dp profile ready.
```

## bluetoothA2dpSetPriority

```
void bluetoothA2dpSetPriority( String deviceStr, Integer priority)

Set priority of the profile
```

## bluetoothA2dpConnect

```
Boolean bluetoothA2dpConnect( String deviceID)

Connect to an A2DP device.
```

## bluetoothA2dpDisconnect

```
Boolean bluetoothA2dpDisconnect( String deviceID)

Disconnect an A2DP device.
```

## bluetoothA2dpGetConnectedDevices

```
List<BluetoothDevice> bluetoothA2dpGetConnectedDevices()

Get all the devices connected through A2DP.
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

## bluetoothA2dpSinkSetPriority

```
void bluetoothA2dpSinkSetPriority( String deviceStr, Integer priority)

Set priority of the profile
```

## bluetoothA2dpSinkGetPriority

```
Integer bluetoothA2dpSinkGetPriority( String deviceStr)

get priority of the profile
```

## bluetoothA2dpSinkIsReady

```
Boolean bluetoothA2dpSinkIsReady()

Is A2dpSink profile ready.
```

## bluetoothA2dpSinkConnect

```
Boolean bluetoothA2dpSinkConnect( String deviceStr)

Connect to an A2DP Sink device.
```

## bluetoothA2dpSinkDisconnect

```
Boolean bluetoothA2dpSinkDisconnect( String deviceStr)

Disconnect an A2DP Sink device.
```

## bluetoothA2dpSinkGetConnectedDevices

```
List<BluetoothDevice> bluetoothA2dpSinkGetConnectedDevices()

Get all the devices connected through A2DP Sink.
```

## bluetoothA2dpSinkGetConnectionStatus

```
Integer bluetoothA2dpSinkGetConnectionStatus( String deviceID)

Get the connection status of a device.
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

## bluetoothMakeConnectable

```
void bluetoothMakeConnectable()

Requests that the device be made connectable.
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

## bluetoothMakeUndiscoverable

```
void bluetoothMakeUndiscoverable()

Requests that the device be not discoverable.
```

## bluetoothGetRemoteDeviceName

```
String bluetoothGetRemoteDeviceName( String address)

Queries a remote device for it's name or null if it can't be resolved
```

## bluetoothGetLocalName

```
String bluetoothGetLocalName()

Get local Bluetooth device name
```

## bluetoothSetLocalName

```
boolean bluetoothSetLocalName( String name)

Sets the Bluetooth visible device name

Returns true on success
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

## bluetoothFactoryReset

```
boolean bluetoothFactoryReset()

Factory reset bluetooth settings.

Returns True if successful.
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

If the local Bluetooth adapter is currentlyin the device discovery process.
```

## bluetoothGetDiscoveredDevices

```
Collection<BluetoothDevice> bluetoothGetDiscoveredDevices()

Get all the discovered bluetooth devices.
```

## bluetoothConfigHciSnoopLog

```
boolean bluetoothConfigHciSnoopLog( Boolean value )

Enable or disable the Bluetooth HCI snoop log
```

## bluetoothGetControllerActivityEnergyInfo

```
String bluetoothGetControllerActivityEnergyInfo( Integer value )

Get Bluetooth controller activity energy info.
```

## bluetoothIsHardwareTrackingFiltersAvailable

```
boolean bluetoothIsHardwareTrackingFiltersAvailable()

Return true if hardware has entriesavailable for matching beacons.
```

## bluetoothGetLeState

```
int bluetoothGetLeState()

Gets the current state of LE.
```

## bluetoothEnableBLE

```
boolean bluetoothEnableBLE()

Enables BLE functionalities.
```

## bluetoothDisableBLE

```
boolean bluetoothDisableBLE()

Disables BLE functionalities.
```

## bluetoothListenForBleStateChange

```
boolean bluetoothListenForBleStateChange()

Listen for a Bluetooth LE State Change.
```

## bluetoothStopListeningForBleStateChange

```
boolean bluetoothStopListeningForBleStateChange()

Stop Listening for a Bluetooth LE State Change.
```

## bluetoothStartListeningForAdapterStateChange

```
boolean bluetoothStartListeningForAdapterStateChange()

Listen for Bluetooth State Changes.
```

## bluetoothStopListeningForAdapterStateChange

```
boolean bluetoothStopListeningForAdapterStateChange()

Stop Listening for Bluetooth State Changes.
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

## wifiAddNetwork

```
Integer wifiAddNetwork(JSONObject wifiConfig)

Add a network.
```

## wifiConnect

```
Boolean wifiConnect(JSONObject config)

Connects a wifi network by ssid

Returns True if the operation succeeded.
```

## wifiEnableNetwork

```
Boolean wifiEnableNetwork(Integer netId, Boolean disableOthers)

Enable a configured network. Initiate a connection if disableOthers is true

Returns True if the operation succeeded.
```

## wifiEnterpriseConnect

```
void wifiEnterpriseConnect(JSONObject config)

Connect to a wifi network that uses Enterprise authentication methods.
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

## contactsDisplayContactPickList

```
Intent contactsDisplayContactPickList()

Displays a list of contacts to pick from.

Returns "A map of result values." 
```

## contactsDisplayPhonePickList

```
String contactsDisplayPhonePickList()

Displays a list of phone numbers to pick from.

Returns "The selected phone number." 
```

## contactsGetAttributes

```
List<String> contactsGetAttributes()

Returns a List of all possible attributes for contacts.
```

## contactsGetContactIds

```
List<Integer> contactsGetContactIds()

Returns a List of all contact IDs.
```

## contactsGetAllContacts

```
List<JSONObject> contactsGetAllContacts( JSONArray attributes)

Returns a List of all contacts.

Returns a List of contacts as Maps
```

## contactsGetContactById

```
JSONObject contactsGetContactById( Integer id, JSONArray attributes)

Returns contacts by ID.
```

## contactsGetCount

```
Integer contactsGetCount()

Returns the number of contacts.
```

## contactsEraseAll

```
void contactsEraseAll()

Erase all contacts in phone book.
```

## contactsQueryContent

```
List<JSONObject> contactsQueryContent( String uri, JSONArray attributes, String selection, JSONArray selectionArgs, String order)

Content Resolver Query

Returns result of query as Maps
```

## queryAttributes

```
JSONArray queryAttributes( String uri)

Content Resolver Query Attributes

Returns "a list of available columns for a given content uri" 
```

## importVcf

```
void importVcf( String uri)

Launches VCF import.
```

## exportVcf

```
void exportVcf( String path)

Launches VCF export.
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

## bluetoothPanSetBluetoothTethering

```
void bluetoothPanSetBluetoothTethering( Boolean enable)

Set Bluetooth Tethering
```

## bluetoothPanIsReady

```
Boolean bluetoothPanIsReady()

Is Pan profile ready.
```

## bluetoothPanGetConnectedDevices

```
List<BluetoothDevice> bluetoothPanGetConnectedDevices()

Get all the devices connected through PAN
```

## bluetoothPanIsTetheringOn

```
Boolean bluetoothPanIsTetheringOn()

Is tethering on.
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

