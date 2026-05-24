import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

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

    public static void main(String[] args) throws IOException {
        // String testString = "Human-Readable Alert Text: An EAS Participant has issued a Required Weekly Test for Alachua, Florida; beginning at 8:30 AM on May 24th, 2026 and ending at 9:00 AM on May 24th, 2026. Message from KXYZ/FM-";
        // System.out.println(testString); // print the test string
        // System.out.println();

        // byte[] data = stringToEightBitAscii(testString); // convert the string to ascii, with an eighth null bit (leading zero) to complete an eight bit byte
        // System.out.println(Arrays.toString(data));
        // System.out.println();

        // data = prependPreamble(data);
        // System.out.println(Arrays.toString(data));

        // byte[] easData = byteReverse(data); // reverses the order of the bits, in order to comply with the required "LSB first"
        // System.out.println(Arrays.toString(easData));
        // System.out.println();

        // byte[] audio = AFSKToAudio.generateRawAudio(easData);
        // System.out.println(audio.length);
        // System.out.println();

        // AFSKToAudio.pcmToWave(audio, "long text");

        // byte[] backData = byteReverse(easData); // do it again to ensure property of involution
        // System.out.println(Arrays.toString(backData));
        // System.out.println();

        // System.out.println(eightBitAsciiToString(backData)); // convert ascii values back to a string
        // System.out.println();

        stringToAlert("Kaypooma", "kaypooma");

    }

}
