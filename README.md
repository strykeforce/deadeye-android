# deadeye

Stryke Force Vision Application

## Major Dependencies

-   [JSON for Modern C++](https://github.com/nlohmann/json).
-   [OpenCV](https://opencv.org/releases.html) installed locally and configured in `local.properties` file, for example `opencv.dir=/opt/OpenCV-android-sdk-3.4.4`.
- [RxJava](https://github.com/ReactiveX/RxJava) and friends [RxAndroid](https://github.com/ReactiveX/RxAndroid), [RxRelay](https://github.com/JakeWharton/RxRelay) used mostly for asynchronous network traffic and connection monitoring.

## Prepare a new Nexus 5X

1.  Unlock phone and enable ADB.

2.  Install 6.0.1 factory image.

3.  Download and install [TWRP for Nexus 5X](https://twrp.me/lg/lgnexus5x.html) image file.

4.  Root with ElementalX and TWRP.

### Unlock Phone and Enable adb

1.  Android SDK is installed to enable use of fastboot.

2.  On the device, go into Settings -> About and find the Build Number and tap on it 7 times to enable developer settings.

3.  Press back and go into Developer Options and enable **USB debugging** and **OEM unlocking**.

### ElementalX Kernel

> Custom kernel for Android devices. Enhance your phone with useful features, superior performance and longer battery life. ElementalX is stable, safe and easy to install.

<https://elementalx.org>

### Factory Images for Nexus and Pixel Devices

> This page contains binary image files that allow you to restore your Nexus or Pixel device's original factory firmware. You will find these files useful if you have flashed custom builds on your device, and wish to return your device to its factory state.

<https://developers.google.com/android/images>

-   If phone doesn't reboot, try locking the phone and wiping with TWRP.

### Boot into recovery on a Google Nexus phone

Power down your Nexus phone or tablet.

Next, holding the volume down key, and keep it held down while you press the power button. The device will then boot into Fastboot mode. Press the volume down button again to cycle through the menu until the Recovery option appears. Press the power button to select this.

### Root with TWRP and SuperSU

<https://www.theandroidsoul.com/how-to-root-using-twrp-recovery/>

1.  `fastboot flash recovery twrp-3.2.2-0-bullhead.img`

### How to Back Up and Restore Your Android Phone with TWRP

> TWRP makes “nandroid” backups, which are near-complete images of your system. Instead of using them to restore individual files or apps, you use nandroid backups to restore your phone to exactly the state it was in when you backed up: the version of Android, your wallpaper, your home screen, right down to which text messages you had left unread.

### Resources

-   [Factory Images for Nexus and Pixel Devices](https://developers.google.com/android/images)
-   [How to Flash a ROM to Your Android Phone](https://lifehacker.com/how-to-flash-a-rom-to-your-android-phone-30885281)
-   [TWRP: the complete guide to using Recovery on Android](http://www.androidtipsandhacks.com/root/twrp-the-complete-guide-to-using-recovery-on-android/)
-   [Use Fastboot on a Mac, Windows or Linux computer to flash ROMs and recovery](http://www.androidtipsandhacks.com/root/fastboot-mac-linux-recovery/)
-   [How To Install Custom ROM on Android](https://www.xda-developers.com/how-to-install-custom-rom-android/)

## Android to roboRIO Networking Notes

We've looked at a couple of methods for establishing a network connection over USB between an Android vision processor and our roboRIO. Two methods are using ADB for port-mapping and using Android USB tethering.

Android tethering over USB looks to be the best solution for us as we can set it up using the native RNDIS network driver on the roboRIO. No installation of `adb` required.  This also works well for development - Windows should be good out of the box, for macOS install [HornDIS](http://joshuawise.com/horndis).

Networking on the roboRIO end is handled by the `deadeye` module in our [thirdcoast](https://github.com/strykeforce/thirdcoast) libraries.

### Addresses

The Android end of the tethered connection is hard-coded for `192.168.42.129` (see [source code][usb_near_iface_addr]) in Android 6.0 (Marshmallow).

### roboRIO Cheatsheet

-   It doesn't seem to hurt anything but if you want to delete duplicate default route: `ip route del default via 192.168.42.129`
-   Bring up USB interface: `ip link set usb0 up`

### Resources

-   [Calling Android services from ADB shell](http://ktnr74.blogspot.com/2014/09/calling-android-services-from-adb-shell.html)
-   [HorNDIS](http://joshuawise.com/horndis)
-   [LigerBots example](https://github.com/ligerbots/Steamworks2017Vision)
-   [ElementalX Kernel](https://elementalx.org)
-   [SuperSU](http://www.supersu.com)
-   [TWRP](https://twrp.me)

[usb_near_iface_addr]: https://github.com/aosp-mirror/platform_frameworks_base/blob/marshmallow-release/services/core/java/com/android/server/connectivity/Tethering.java#L110
