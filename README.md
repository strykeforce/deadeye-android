# deadeye

Stryke Force Vision Application

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

## Resources

-   <https://developers.google.com/android/images>
-   <https://lifehacker.com/how-to-flash-a-rom-to-your-android-phone-30885281>
-   <http://www.androidtipsandhacks.com/root/twrp-the-complete-guide-to-using-recovery-on-android/>
-   <http://www.androidtipsandhacks.com/root/fastboot-mac-linux-recovery/>
-   <https://www.xda-developers.com/how-to-install-custom-rom-android/>
