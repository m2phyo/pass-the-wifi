# May 16, 2013 #

Progress Made:
  * Still working on QR integration

Problems Encountered:
  * None

What I Learned:
  * Integrating Zxing Library

# May 9, 2013 #

Progress Made:
  * Studied the source code of Zxing
  * Started coding QR part

Problems Encountered:
  * None

What I Learned:
  * Zxing library
  * Barcode scanning/generation

# May 2, 2013 #

Progress Made:
  * Made changes to the payload that is written to NFC tags
  * Now supports hidden ssids
  * Initial Android Beam support

Problems Encountered:
  * None

What I Learned:
  * Android Beam API

# April 25, 2013 #

Progress Made:
  * Prepared slides for presentation.
  * Fixed some UI elements.
  * Made "connecting to WiFi" seamless with a transparent activity.
  * Fixed a bug which caused WifiActivity to hang.

Problems Encountered:
  * None

What I Learned:
  * Pending intents
  * NFC foreground dispatch
  * Transparent theme
  * Ndef formatting

# April 18, 2013 #

Progress Made:
  * Built a custom layout.
  * Implemented NFC tag functionality.

Problems Encountered:
  * None

What I Learned:
  * NFC programming

# April 11, 2013 #

Progress Made:
  * With the advice of Prof Puder, I took a break from implementing the backend of my program.
  * I am focused on building a customized UI.
  * Currently working on a custom list view layout.

Problems Encountered:
  * None

What I Learned:
  * Tutorials on building custom layouts

# April 04, 2013 #

Progress Made:
  * Done limited coding during the spring break.
  * Can now connect to any access point programmatically.
  * Can now determine the type of protection without user input.
  * Found a technical report on "How to Program an Android Application to Access and Manage the Wi-Fi Capabilities of a Smartphone" (http://bit.ly/XgCgY0) which was really helpful.
  * Started tinkering with NFC programming

Problems Encountered:
  * NFC turned out to be difficult than I initially expected
  * My plan was to finish NFC part of the implementation by this week

What I Learned:
  * I learned more about the capabilities of Android API related to wifi management.
  * I started learning about NFC programming

Plans for Next Week:
  * NFC part of the application consists of two functions
    * Reading/Writing from/to an NFC tag
    * Beaming data to another NFC enabled device through Android Beam
  * I plan to finish at least one of these. Hopefully both of them will be done by next week.



# March 21, 2013 #

Progress Made:
  * Created the application with a drop-down interface to easily switch between 2 activities (QR and NFC)
  * Started implementation of NFC part
  * Coded the part that scans for available wifi access points.
  * Implemented a spinner widget to display found access points.
  * Added a check to make sure wifi is enabled/enable it if disabled
  * Added "Protection" spinner, "Password" input field and "Generate" button (not functional yet).
  * Toast notification to let the user know that Wifi scan is in progress.

Problems Encountered:
  * Some networks seems to be problematic (e.g. SFSU).
    * Because it requires login after establishing connection?
  * Combining multiple layouts that satisfy the functional needs of the app (e.g. hiding password field when not necessary). Could not put together a set of layouts that looks as I want at this point.

What I Learned:
  * Learned about new layouts introduced with the recent Android updates
  * Possibility of identifying protection type without user input

Plans for Spring Break:
  * Finish implementation of the NFC part
  * Test to see if protection can be safely identified programatically.
  * Research layouts. Try different ones (currently using a simple, temporary one).