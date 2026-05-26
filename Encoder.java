import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class Encoder {

    public byte preamble = (byte) 0xAB;

    public Encoder(){
    }

    // the bit stuffing does nothing to the data bc it just specifies another 0 like a leading zero in decimal.
    // thats why the & 0x7F bit mask just adds a 0 cause 0x7F is just 01111111 which is Freaking awesome
    // and so an 8 bit with a null bit representation of a 7 bit ascii does literally nothing except for add a leading zero which doesnt change its value #heckyeah

    // the preamble is just that byte 16 times, 8 bits (0b10101011), or 2 hexes (0xAB) 0xAB is the exact hex representation of the binary which in turn is just a number in decimal (0d171)

    /**
     * 
     * @param text the ASCII-compliant string to convert into US.ASCII
     * @return an array of bytes in which each byte includes the 7 bit ascii encoding and an eighth null bit
     * to form a full eight bit byte.
     */
    public static byte[] stringToEightBitAscii(String text){
        if(text == null || text.matches("[ -~]")){
            throw new IllegalArgumentException("Illegal string.");
        }

        byte[] sevenBytes = text.getBytes(java.nio.charset.StandardCharsets.US_ASCII); // basic latin block in unicode, seven bits
        byte[] eightBytes = new byte[sevenBytes.length];

        for(int i = 0; i < sevenBytes.length; i++){
            byte safeNewByte = (byte) (sevenBytes[i] & 0b01111111); // masks to ensure eighth null bit
            eightBytes[i] = safeNewByte;
        }

        return eightBytes;
    }
    
    /**
     * 
     * @param bytes the array of bytes to reverse.
     * @return an array of bytes with all bytes having reversed bit order as the input. this is done to 
     * transmit bits by "least significant digit", in order to comply with 
     * NOAA WEATHER RADIO (NWR) SPECIFIC AREA MESSAGE ENCODING (SAME) A.1.1.1
     */
    public static byte[] byteReverse(byte[] bytes){
        byte[] easBytes = new byte[bytes.length];
        
        for(int i = 0; i < bytes.length; i++){
            byte safeNewByte = (byte) (Integer.reverse(bytes[i]) >>> 24); // reverses digits of char casted as integer and then moves bits back to front
            easBytes[i] = safeNewByte;
        }

        return easBytes;
    }

    /**
     * 
     * @param bytes a byte array consisting of ASCII encoded characters.
     * @return a String consisting of the ASCII decoded characters from the input array.
     */
    public static String eightBitAsciiToString(byte[] bytes){
        return new String(bytes, StandardCharsets.US_ASCII);
    }

    /**
     * 
     * @param bytes the array of bytes to prepend
     * @return the array bytes with the preamble byte 0xAB prepended 16 times.
     * Note that 0xAB is dec 171 which doesn't decode as an ASCII value in seven bit ASCII.
     */
    public static byte[] prependPreamble(byte[] bytes){
        byte preamble = (byte) 0xAB;
        
        byte[] withPreamble = new byte[bytes.length + 16];
        for(int i = 0; i < 16; i++){
            withPreamble[i] = preamble;
        }
        for(int i = 0; i < bytes.length; i++){
            withPreamble[i+16] = bytes[i];
        }

        return withPreamble;
    }

    /**
     * 
     * @param alertText ascii text to be converted to an EAS alert text
     * @param filename filename of output wav
     * @throws IOException
     */
    public static void stringToAlert(String alertText, String filename) throws IOException{
        byte[] data = stringToEightBitAscii(alertText);
        data = prependPreamble(data);
        data = byteReverse(data);
        data = AFSKToAudio.generateRawAudio(data);
        AFSKToAudio.pcmToWave(data, filename);
    }

    /**
     * String to alert with parameters for all AFSK frequencies
     * @param alertText ascii text to be converted
     * @param filename filename of output wav
     * @param sampleRate the number of audio samples per second.
     * @param baudRate the number of signal changes per second
     * @param markFrequency audio frequency representing binary 1
     * @param spaceFrequency audio frequency representing binary 0
     * @throws IOException for when wav cannot be made
     */
    public static void stringToAlert(String alertText, String filename, int sampleRate, double baudRate, double markFrequency, double spaceFrequency) throws IOException{
        byte[] data = stringToEightBitAscii(alertText);
        data = prependPreamble(data);
        data = byteReverse(data);
        data = AFSKToAudio.generateRawAudio(data, sampleRate, baudRate, markFrequency, spaceFrequency);
        AFSKToAudio.pcmToWave(data, filename);
    }

    /**
     * main method expecting an alert text string and an output filename string.
     * @param args string array of 
     * @throws IOException
     */
    public static void main(String[] args) throws IOException { // minimum params
        if(args.length != 2){
            System.err.println("Error: wrong number of String arguments, expected 2, got " + args.length);
            System.err.println("Expected usage: java -jar string-to-eas.jar <alert text> <output filename>");
        }
        stringToAlert(args[0], args[1]);
    }

    // /**
    //  * A (currently non-functional) method that allows custom parameters for frequencies.
    //  * @param args
    //  * @param iargs
    //  * @throws IOException
    //  */
    // public static void main(String[] args, double[] iargs) throws IOException{
    //     if(args.length != 2){
    //         System.err.println("Error: wrong number of String arguments, expected 2, got " + args.length);
    //         System.err.println("Expected usage: java -jar string-to-eas.jar <alert text>, <output filename>, <sample rate>, <baud rate>, <mark frequency>, <space frequency>");
    //     }
    //     if(iargs.length != 4){
    //         System.err.println("Error: wrong number of doubke arguments, expected 4, got " + args.length);
    //         System.err.println("Expected usage: java -jar string-to-eas.jar <alert text>, <output filename>, <sample rate>, <baud rate>, <mark frequency>, <space frequency>");
    //     }
    //     stringToAlert(args[0], args[1], ((int) (iargs[0])), iargs[1], iargs[2], iargs[3]);
    // }

}
