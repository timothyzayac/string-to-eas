import java.io.ByteArrayInputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;

import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import java.io.File;
import java.io.IOException;

public class AFSKToAudio {
    private static final int SAMPLE_RATE = 44100; // samples per second
    private static final double BAUD_RATE = 520.83; // bits per second
    private static final double MARK_RATE = 2083.3; // bitwise 1
    private static final double SPACE_RATE = 1562.5; //bitwise 0
    private static final int SAMPLES_PER_BIT = (int) (SAMPLE_RATE / BAUD_RATE);
    
    /**
     * 
     * @param data the binary data to generate raw pcm data for
     * @return raw pcm data
     */
    public static byte[] generateRawAudio(byte[] data){
        int totalBits = data.length * 8;
        int totalSamples = (int) SAMPLES_PER_BIT * totalBits; // length of raw data array

        ByteBuffer buffer = ByteBuffer.allocate(totalSamples * 2);
        buffer.order(ByteOrder.BIG_ENDIAN);
        
        double phase = 0;
        
        for(int i = 0; i < data.length; i++){
            byte cur = data[i];
            for(int j = 7; j >= 0; j--){ // msb first since we already flipped it
                int bit = (cur >> j) & 1;
                double freq;

                if(bit == 1){
                    freq = MARK_RATE;
                }
                else{
                    freq = SPACE_RATE;
                }

                // sample per bit individual (making sine waves holy)
                for(int s = 0; s < SAMPLES_PER_BIT; s++){
                    short sample = (short) (Math.sin(phase) * Short.MAX_VALUE); // makes sample of phase of frequency and normalizes it
                    buffer.putShort(sample);


                    phase = phase + ((2 * Math.PI * freq) / SAMPLE_RATE);
                }


                phase = phase % (2 * Math.PI);
            }
        }
        return buffer.array();
    }

    /**
     * i will be honest i am not quite sure about this
     * @param pcmData raw pcm data to convert to a wav file
     * @param filename output file name of the wav containing pcmData
     * @throws IOException
     */
    public static void pcmToWave(byte[] pcmData, String filename) throws IOException{
        File wavFile = new File(filename + ".wav");
        
        AudioFormat format = new AudioFormat(44100, 16, 1, true, true);

        try(ByteArrayInputStream bais = new ByteArrayInputStream(pcmData);
        AudioInputStream ais = new AudioInputStream(bais, format, pcmData.length / format.getFrameSize())){
            AudioSystem.write(ais, AudioFileFormat.Type.WAVE, wavFile);
        } catch (IOException e){
            System.err.println("error saving wav file.");
        }

    }
}
