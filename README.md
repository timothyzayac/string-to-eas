# string-to-eas

A Java program that converts any arbitrary ASCII string into a decodable .wav file, following official Audio Frequency Shift Keying protocols specified for the Emergency Alert System.

## What?

This is a relatively simple program I wrote after using [eas.tools](https://eas.tools) and getting inspired by the idea of the EAS's encoding holding something other than the typical SAME encoded message.

You can, of course, also encode a traditional SAME message, which works the same as an official EAS transmission, but the `stringToAlert()` method provided in the Encoder.java file allows you to input an arbitrary ASCII-compliant string and recieve an output audio file encoding your string with the same Audio Frequency shift Keying protocol used for the official SAME header bursts in an actual emergency alert. Put whatever you want in this thing!

## Todo

- Better error handling
- JavaDoc + comment everything
- Overhaul (understand + reimplement) AI-assisted code in the raw PCM generation
- Command line support with args
- User interface!
    - Allow custom baud/mark/space rates for weird alternate universe headers!
    - Disable / use custom preamble!

## How-to

To use this program in its current state, just download the zipped code. Once it's downloaded, you can modify the `stringToAlert()` call, changing the "alertText" and "filename" whatever parameters you'd like.

If you want to verify that your alert message decodes correctly, you can input the raw audio using the "Upload Audio File" button on [eas.tools' Alert Decoder](https://eas.tools/?tool=decoder).