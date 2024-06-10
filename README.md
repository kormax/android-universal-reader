# Android Universal Reader

<p float="left">
  <img src="./assets/UNIVERSAL.READER.LIVE.webp" alt="![Animation of an Android device reading an Apple VAS pass]" width=320px>
  <img src="./assets/UNIVERSAL.READER.SCREENSHOT.webp" alt="![Screenshot of an Android device reading an Apple VAS pass]" width=320px>
</p>


# Overview

This project provides a demonstration of Android NFC reader application, allowing to interact with the following Value-Added Services protocols:
- [Apple VAS](https://github.com/kormax/apple-vas) (VAS);
- [Google Smart Tap](https://github.com/kormax/google-smart-tap) (GST).


Users of this application can:
* Read VAS and GST passes from other devices:
  * View pass data;
  * View read statuses, useful in cases a read wasn't successful.
* Configure display of:
  * Logs;
  * Commands and responses; 
* Load custom reader configurations, allowing to read other passes with this application, enable/disable support for a particular protocol, or play around and configure other aspects of the protocols.


# Requirements

* Android Studio;
* An Android device with NFC hardware and a compatible Android version;
* An additional iOS/Android mobile device to read passes from.


# Usage

* Build and install the application to your device using Android Studio;
* Download and save a [`configuration.json` file](./assets/configuration.json) containing reader configuration parameters to your computer or mobile device.  
  The default configuration file is configured to read the following pass types:
  * Apple VAS:
    * [PassKit](https://pub1.pskt.io/c/gn1v07);
    * [SpringCard](https://springpass.springcard.com);
    * [Zebra](https://springpass.springcard.com);
    * PassNinja (No decryption key, unavailable for download).
  * Google Smart Tap:
    * [Google Demo Pass](https://pay.google.com/gp/v/save/eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9.eyJhdWQiOiJnb29nbGUiLCJvcmlnaW5zIjpbImh0dHA6Ly9sb2NhbGhvc3Q6ODA4MCJdLCJpc3MiOiJnb29nbGUtcGF5LWZvci1wYXNzZXMtZ3RlY2hAcGF5LXBhc3Nlcy1zbWFydC10YXAtc2FtcGxlLmlhbS5nc2VydmljZWFjY291bnQuY29tIiwiaWF0IjoxNTI5OTU2MDcwLCJ0eXAiOiJzYXZldG9hbmRyb2lkcGF5IiwicGF5bG9hZCI6eyJsb3lhbHR5T2JqZWN0cyI6W3siY2xhc3NJZCI6IjMyNjUzMjAxMTE2NDE5NTYxODMuMDYxOV9nb29nbGVEZW1vVGVzdCIsInN0YXRlIjoiYWN0aXZlIiwiaWQiOiIzMjY1MzIwMTExNjQxOTU2MTgzLjA2MTlfZ29vZ2xlRGVtb1Rlc3Qtb2JqMDEifV19fQ.MjUBdBtGyQwcE3xI-q6tVNBiApZppLMp0Op0XvB-c31Ri-JttJCzGXZvURNvKFDGXTNQQDqVBgQziuBMR_ZL0_lp7q8B5nwfSR32I0Kr220n3CezAsikaM5rKVf83UXT9fvqagnRn0QVVuS7fyLLc9nBDxRhRnkqEz2dQPgrNZ1u2AEJBPSoM6sLTeHssOWUMp7dgW6REJg7NUcczXJgLSOpAmD08G14q1qfS5T4Jb4knwPeIMnggNMjHcSBmz0z6W4DGD5Ld16nKOty4TvoDh4EevEJF7U7UQcOwIpozIXRVKs8rlqEXMObGsrk4hPM-I2p6H4DBrVcpyG8HD6Iug).
* Modify the `configuration.json` file according to your preferences:
  * Remove or modify VAS and GST configurations;
  * Configure other enumeration and boolean parameters;
  * Configure keys:
> [!TIP]
> Key data should be in PEM format.  
> Application supports key format with or without "BEGIN/END EC PRIVATE KEY" parts.  
> Base64-encoded data can also be instead encoded as a HEX string. The provided configuration file uses all of the supported variations.

> [!TIP]
> JSON keys in the example file use `snake_case` format, but `camelCase` is supported too. Some JSON keys support multiple aliases, some can be omitted and will use a default value instead.
* Start the application;
* Click on the "gear: icon;
* Tap on the "Load Configuration" menu item:
    <p float="left">
    <img src="./assets/CONFIGURATION.LOAD.webp" alt="![Load configuration menu item]" width=320px>
    </p>

* Find and select an appropriate `configuration.json` file;
* File load result should be displayed in a toast at the bottom of your screen. The successful message should look similar to the following (numbers for `vas` mean count of enabled merchants):
    <p float="left">
    <img src="./assets/CONFIGURATION.LOADED.webp" alt="![Toast containing a success message]" width=320px>
    </p>
* Loaded configuration is also saved into local preferences, so It'll be kept when closing the application and loaded automatically upon start.
* With the application open, bring any device with a viable pass to the reader (can be downloaded by following one of the URLs mentioned before), and observe the results.


# Project structure:

* `apple.vas` - Apple VAS implementation. Classes containing the core logic are:
  * `VasReaderConfiguration` - methods for reading passes;
  * `VasRegularCryptoProvider` - methods for decrypting pass data;
  * `VasReadResult`, `VasPayload` - classes defining the structure of read and decrypted data;
* `google.smarttap` - Google Smart Tap implementation. Classes containing the core logic are:
  * `SmartTapReaderConfiguration` - methods for reading passes;
  * `SmartTapRegularCryptoProvider` - methods for decrypting pass data;
  * `SmartTapResult`, `SmartTapObject`, `SmartTapObjectPass` - classes defining the structure of read and decrypted data; 
* `model`- contains definitions for serializable models used when deserializing `json` configuration file into objects that can later be `loaded` into executable implementation.
* `ndef`- contains **custom** NDEF implementation. **Do not mistake it for one built into Android**;
* `tlv.ber`- contains **custom** BerTlv implementation;
* `Utility` - contains a bunch of extension functions & helper methods;


# Known issues

* Regular Android devices are unable to perform NFC polling with `ECP` `VASUP-A`. This has the following downsides when reading passes from Apple devices:
    * There's no way to trigger automatic pass presentment on iPhone and Apple Watch devices, as this functionality uses ECP to initiate premature communication;  
    Instead, Apple devices usually recognize Android device without ECP as an EMV reader, which causes a default payment card to pop up if none is preauthorized beforehand.
    * Apple Watch devices running the latest WatchOS 10 do not respond to a reader device when a pass is selected directly if a reading device does not implement ECP.  
    **Instead, the user has to select any payment card, which will allow a pass to be read**;
    * The "Pass will be used" message under a payment card can also only appear with the help of ECP.
* Apple VAS allows reading multiple passes only if a payment card was presented to the reader (regardless of the VAS mode declared by the reader). It's only possible to read multiple passes if all of them have a unique `passTypeIdentifier`;
* Apple VAS will only display the first read pass regardless of how many other passes were read;
* Apple VAS allows to read passes only one time per payment card authentication;
* Google Smart Tap, unlike Apple VAS, can only have a single collector configuration active by design. It also supports only one pass read per session regardless of how the reading session is activated.
   * Both limitations of GST cannot be bypassed by clever tricks, as Google has thought about it and they prevent requests for other collector IDs or additional passes the until device leaves the field and enters it again only after a set debounce period.


# Potential improvements

* Add pre-built releases for less tech-savvy users;
* Add an ability to save logs & pass data:
  * Page to look at full historical data.
  * Add an ability to export saved logs and read passes;
  * Add an ability to upload read passes to a remote URL.
* Add an ability to configure the reader in-app, in addition to the configuration file:
  * Configuration page;
  * Activation and deactivation of merchant configurations;
  * Changes to the auxiliary boolean and enumeration parameters;
  * Ability to upload keys into a configuration from a local file; 
* Add new `CryptoProvider` implementations for VAS and GST:
  * `RemoteCryptoProvider` - cryptography is performed by a remote server via HTTP to prevent key leaks;
  * `KeystoreCryptoProvider` - cryptography is performed by keys injected into the underlying Keystore implementation:
    * Add a variation that performs key injection from a remote server, which also performs keystore attestation to prevent key leaks;
    * Add a variation that allows local key import.
* Add an ability to pull configuration from a remote server by URL, potentially at a repeating interval.
* Move out pass decryption for VAS outside of the communication code to prevent latency issues if the networked provider is implemented.
* Properly validate custom `Ndef`, `BerTlv`, and `Iso7816Command/Response` implementations as those were built using personal knowledge and not "by the book".
* Add more tests for utilities & core application logic;
* Validate the appropriateness of using `UByte` and `UByteArray` instead of Byte and ByteaArray.
* Improve user interface;
* (If all of the previous points get sufficiently covered). Add support for additional protocols:
  * `EMV` (for demo purposes only);
  * `Mifare DESFire`, `Plus`, `Ultralight`, and `Classic` in this order.
* For protocols with dynamic data format, add an ability to define custom data parsers via configuration.


# Notes

* Apple VAS and Google Smart Tap are the intellectual property of the companies that have developed those protocols. Usage of this application for commercial purposes might be looked upon unfavorably by respective companies:    
In addition, Apple VAS requires a compatible reader to perform ECP for fully proper operation, which cannot be done with an off-the-shelf Android device anyway.
* This application has not passed any certification or official test suites, as those are given to approved partners only. No guarantees are provided in regards to the stability or correctness of the protocol implementation provided here.
* Apple VAS and Google Smart Tap implementations were built based on protocol information gained from reverse-engineered efforts linked in the references section;
* This project has been created without much prior experience with Android development. In case you have found an issue with the app, or can propose an improvement to the source code, feel free to raise an Issue or create a Pull Request;
* Please understand that PRs and Issues regarding the private/public/unused properties and open classes won't be accepted as this project is under active development and there's no need to provide a stable API, hence everything is public by default in most cases;


# References

* [Apple VAS reverse-engineering](https://github.com/kormax/apple-vas);
* [Google Smart Tap reverse-engineering](https://github.com/kormax/google-smart-tap);
* [Smart Tap Sample App by Google](https://github.com/google-wallet/smart-tap-sample-app);
* [Android IsoDep](https://developer.android.com/reference/android/nfc/tech/IsoDep);
* [Android NfcAdapter](https://developer.android.com/reference/android/nfc/NfcAdapter).
* Apple VAS sample pass downloads:
  * [PassKit](https://pub1.pskt.io/c/gn1v07);
  * [SpringCard](https://springpass.springcard.com);
  * [Zebra](https://springpass.springcard.com).
* Google Smart Tap sample pass downloads:
  * [Google Demo Pass](https://pay.google.com/gp/v/save/eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9.eyJhdWQiOiJnb29nbGUiLCJvcmlnaW5zIjpbImh0dHA6Ly9sb2NhbGhvc3Q6ODA4MCJdLCJpc3MiOiJnb29nbGUtcGF5LWZvci1wYXNzZXMtZ3RlY2hAcGF5LXBhc3Nlcy1zbWFydC10YXAtc2FtcGxlLmlhbS5nc2VydmljZWFjY291bnQuY29tIiwiaWF0IjoxNTI5OTU2MDcwLCJ0eXAiOiJzYXZldG9hbmRyb2lkcGF5IiwicGF5bG9hZCI6eyJsb3lhbHR5T2JqZWN0cyI6W3siY2xhc3NJZCI6IjMyNjUzMjAxMTE2NDE5NTYxODMuMDYxOV9nb29nbGVEZW1vVGVzdCIsInN0YXRlIjoiYWN0aXZlIiwiaWQiOiIzMjY1MzIwMTExNjQxOTU2MTgzLjA2MTlfZ29vZ2xlRGVtb1Rlc3Qtb2JqMDEifV19fQ.MjUBdBtGyQwcE3xI-q6tVNBiApZppLMp0Op0XvB-c31Ri-JttJCzGXZvURNvKFDGXTNQQDqVBgQziuBMR_ZL0_lp7q8B5nwfSR32I0Kr220n3CezAsikaM5rKVf83UXT9fvqagnRn0QVVuS7fyLLc9nBDxRhRnkqEz2dQPgrNZ1u2AEJBPSoM6sLTeHssOWUMp7dgW6REJg7NUcczXJgLSOpAmD08G14q1qfS5T4Jb4knwPeIMnggNMjHcSBmz0z6W4DGD5Ld16nKOty4TvoDh4EevEJF7U7UQcOwIpozIXRVKs8rlqEXMObGsrk4hPM-I2p6H4DBrVcpyG8HD6Iug).
