USB Host Serial Facade
===
SL4A Facade for USB Serial devices by Android USB Host API.

It control the USB-Serial like devices
from Andoroid which has USB Host Controller
(I tested the product with Android tablet).

The sample
demonstration is also available at [youtube video](http://www.youtube.com/watch?v=EJ7qiGXaI74)

This facade developped in [My Bitbucket site](https://bitbucket.org/kuri65536/usbhostserialfacade),
you can see the commit log in it.

Requirements
===
* Android device which has USB Host controller (and enabled in that firmware).
* Android 4.0 (API14) or later.
* USB Serial devices (see [Status](#Status)).
* USB Serial devices were not handled by Android kernel.

  > I heard some android phone handle USB Serial devices
  > make /dev/ttyUSB0 in kernel level.
  > In this case, Android does not be able to handle the device
  > from OS level.

  please check Android Applications be able to grab the target USB Devices,
  such as [USB Device Info](https://play.google.com/store/apps/details?id=aws.apps.usbDeviceEnumerator).

Status
===
* probably work with USB CDC, like FTDI, Arduino or else.

* 2012/09/10: work with 78K0F0730 device (new RL78) with Tragi BIOS board.

  [M78K0F0730](http://www.marutsu.co.jp/shohin_55296/>)

* 2012/09/24: work with some pl2303 devcies.

<!--- vi: ft=markdown:et:ts=4
-->
