# string-to-eas

A Java program that converts any arbitrary ASCII string into a decodable .wav file, following official Audio Frequency Shift Keying protocols specified for the Emergency Alert System.

## How-to

To use this program, you can download a precompiled binary .jar file from the releases, and run it from a command line, using one series of arguments.
Expected usage: `java -jar string-to-eas.jar <alert text>, <filename, no extension>`

To compile it yourself, download the source code, ensure the manifest.txt is intact, and compile all the .java files to .class files. Afterwards, create a .jar from all the class files, and ensure the main class is `Encoder`.

If you want to verify that your alert message decodes correctly, you can input the raw audio using the "Upload Audio File" button on [eas.tools' Alert Decoder](https://eas.tools/?tool=decoder).

## What?

This is a relatively simple program I wrote after using [eas.tools](https://eas.tools) and getting inspired by the idea of the EAS's encoding holding something other than the typical SAME encoded message.

You can, of course, also encode a traditional SAME message, which works the same as an official EAS transmission, but the `stringToAlert()` method provided in the Encoder.java file allows you to input an arbitrary ASCII-compliant string and recieve an output audio file encoding your string with the same Audio Frequency shift Keying protocol used for the official SAME header bursts in an actual emergency alert. Put whatever you want in this thing!

## How?

This program uses a multi-step algorithm to reach its final result. For this example, I'll use the string "A". Reasoning behind the specification is included in hyperlinks.

### `stringToEightBitAscii()`
In the first step, a string is turned into an array of bytes containing each character's corresponding ASCII value. Since they're seven-bit values from ASCII, I make sure to "bit-stuff" a final 0 "null bit" on the left, by using a bitwise operation (`& 0x7F`) to ensure each byte contains exactly 8 bits of data. [47 CFR 11.31(a)(1)](https://www.ecfr.gov/current/title-47/part-11#p-11.31(a)(1))  
`[A]` → `[0x41]` (or `[0b01000001]`)

### `prependPreamble()`
Next, a "preamble" is added to the message, containing of 16 repeated bytes of `0xAB` (or `0b10101011`) to calibrate decoding rates for a reciever. To do this, I prepended 16 empty spaces before any given message and fill them all with the same byte, to replicate this mandate. [47 CFR 11.31(c)](https://www.ecfr.gov/current/title-47/part-11/subpart-B#p-11.31(c))  
`[0x41]` → `[0xAB], [0xAB] ...16 times total... [0xAB], [0x41]`

### `byteReverse()`
Next, to comply with the requirement that all alert data is transmitted least significant bit (LSB) first, the bit order of all the bytes is reversed iteratively. [NATIONAL WEATHER SERVICE INSTRUCTION 10-1712 code A.1.1.1](https://www.weather.gov/media/directives/010_pdfs_archived/pd01017012a.pdf)  
`[0xAB]...[0x41]` (`[0b10101011]...[0b01000001]`) → `[0xD5]...[0x82]` (`[0b11010101]...[0b10000010]`)

### `generateRawAudio()`
With this reversed data, we use an algorithm to convert the reversed binary bits into raw PCM data. This is the function that implements the actual AFSK, by reading bits, and generating an appropriate number of samples for each bit depending on the sample rate and baud rate specified in the code.
The logical flow for this incorporates iterating through every bit in the array, to determine whether we need to write samples for the mark frequency (bitwise 1) or space frequency (bitwise 0). For each binary bit, we write its equivalent number of samples, keeping track of the phase of the sine wave as to not create audible pops when listening to the audio.

### `pcmToWave()`
This final method creates a new file and writes the raw PCM data with format type WAVE.

## Todo

- Better error handling
- JavaDoc + comment everything
- Overhaul (understand + reimplement) AI-assisted code in the raw PCM generation
- ~~Command line support~~ with args
- User interface!
    - Allow custom baud/mark/space rates for weird alternate universe headers!
    - Disable / use custom preamble!