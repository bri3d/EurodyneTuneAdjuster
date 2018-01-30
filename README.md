# EurodyneAdjuster

https://play.google.com/store/apps/details?id=com.brianledbetter.tuneadjuster&hl=en_GB

What does this do?

This adjusts the Adjustable features on Eurodyne Simos18 (MQB Golf / GTI / R / Audi S3) tunes.

Eurodyne uses the standard UDS ReadLocalIdentifier (0x22) and WriteLocalIdentifier (0x2E) services to provide and store information about boost and octane settings.
No diagnostic session or security login are required for either call on a Eurodyne tune, so the code is VERY straightforward.

No guarantees are provided for any purpose and this is not supported or endorsed by Eurodyne.

I suspect if you try this without a Eurodyne tune the app will just crash as the identifier read fails, but please don't try anyway.

# Use

Pair an ELM327 Bluetooth adapter in the Android settings. The passcode for these adapters is usually 1234.

Start the app and flip the Connect toggle in the lower left. Turn your ignition on NOW before selecting the device. It's recommended but not required to do this without the engine running, although it will work even with the car in motion. Now select your ELM327 device, usually called "OBDII." If you see the sliders appear, you're set. Otherwise, try flipping the toggle again. If the app crashes, we probably couldn't connect to your ECU.

Press Back to kill the Foreground Service and exit.

# Todo

* Error handling - on any unexpected response we should just pop a dialog and kill the connection thread.
* Properly implement UDS protocol - retries and continuation messages, as well as checking for proper responses
* Add support for logging and gauge display.
