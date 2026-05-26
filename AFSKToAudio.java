import java.io.ByteArrayInputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import java.io.File;
import java.io.IOException;

public class AFSKToAudio {

    // EAS official specified rates.
    private static final int SAMPLE_RATE = 44100; // samples per second
    private static final double BAUD_RATE = 520.83; // bits per second
    private static final double MARK_RATE = 2083.3; // bitwise 1
    private static final double SPACE_RATE = 1562.5; //bitwise 0
    

    /**
     * This method was AI assisted. I think I know what it's doing, but I want to gain a strong grasp on this if
     * I want to be putting out code that utilizes the concept of direct writing of PCM data.
     * 
     * @param data the binary data to generate raw pcm data for
     * @param sampleRate the number of audio samples per second.
     * @param baudRate the number of signal changes per second
     * @param markRate audio frequency representing a binary 1.
     * @param spaceRate audio frequency representing a binary 0.
     * @return raw pcm data array of bytes
     */
    public static byte[] generateRawAudio(byte[] data, int sampleRate, double baudRate, double markRate, double spaceRate){
        int samplesPerBit = (int) (sampleRate / baudRate);
        
        int totalBits = data.length * 8;
        int totalSamples = (int) samplesPerBit * totalBits; // length of raw data array

        ByteBuffer buffer = ByteBuffer.allocate(totalSamples * 2);
        buffer.order(ByteOrder.BIG_ENDIAN);
        
        double phase = 0;
        
        for(int i = 0; i < data.length; i++){
            byte cur = data[i];
            for(int j = 7; j >= 0; j--){ // msb first since we already flipped it
                int bit = (cur >> j) & 1;
                double freq;

                if(bit == 1){
                    freq = markRate;
                }
                else{
                    freq = spaceRate;
                }

                // sample per bit individual (making sine waves holy)
                for(int s = 0; s < samplesPerBit; s++){
                    short sample = (short) (Math.sin(phase) * Short.MAX_VALUE); // makes sample of phase of frequency and normalizes it
                    buffer.putShort(sample);


                    phase = phase + ((2 * Math.PI * freq) / sampleRate);
                }


                phase = phase % (2 * Math.PI);
            }
        }
        return buffer.array();
    }

    /**
     * 
     * @param data the binary data to generate raw PCM data for
     * @return raw PCM data array of bytes
     */
    public static byte[] generateRawAudio(byte[] data){
        return generateRawAudio(data, SAMPLE_RATE, BAUD_RATE, MARK_RATE, SPACE_RATE);
    }

    /**
     * i will be honest i am not quite sure about this
     * 
     * @param pcmData raw pcm data to convert to a wav file
     * @param filename output file name of the wav containing pcmData
     * 
     * @throws IOException
     */
    public static void pcmToWave(byte[] pcmData, String filename) throws IOException{
        File wavFile = new File(filename + ".wav");
        
        AudioFormat format = new AudioFormat(44100, 16, 1, true, true);

        try(ByteArrayInputStream bais = new ByteArrayInputStream(pcmData);
        AudioInputStream ais = new AudioInputStream(bais, format, pcmData.length / format.getFrameSize())){
            AudioSystem.write(ais, AudioFileFormat.Type.WAVE, wavFile);
        } catch (IOException e){ // odd. figure out what IOException this may throw.
            System.err.println("error saving wav file.");
        }

    }
}
