package audio;

import static audio.AudioConstants.*;
import static application.Main.out;

import java.io.*;
import java.util.logging.Logger;

import javax.sound.sampled.*;


/**
 * A container for an audio signal backed by a byte buffer.
 * Buffer length is specified in AudioConstants.
 * Useful methods:
 * - acquire() fills the underlying buffer with audio data fed by a TargetDataLine.
 * - level_dB() compute the signal level in dB
 * 
 * 
 * @author sydxrey
 *
 */
public class AudioSignal {
	
	private static final Logger LOGGER = Logger.getLogger("confLogger");
	private byte[] audioSamples; // backing buffer

	// --------------------------------- Constructors ---------------------------------
	
	public AudioSignal() {
		audioSamples = new byte[BUF_LEN];
	}
	
	// --------------------------------- public methods ---------------------------------
	
	/**
	 * Fills the sample buffer with audio data fed by the given TargetDataLine.
	 * @return the number of bytes that have been read (should be equal to the buffer size)
	 */
	public int acquire(TargetDataLine line) {
		return line.read(audioSamples, 0, BUF_LEN);
	}
		
	/**
	 * @return the ith sample in the buffer normalized to [-1.0, 1.0[
	 */
	public double getSample(int i) {
		return audioSamples[i] / 128.0;
	}
	
	/**
	 * @return the signal level in dB (aka RMS) computed on the whole buffer length
	 */
	public double level_dB() {
		double sum = 0;
		for (int i = 0; i < BUF_LEN_SAMPLES; i++) {
			double x = getSample(i);
			sum += x * x;
		}
		sum /= BUF_LEN_SAMPLES;
		return 10 * Math.log(sum);
	}

	// --------------------------------- I/O ---------------------------------
	
	/**
	 * Save the current buffer content to the given stream as an array of doubles. Should be called after each call to acquire()
	 * 
	 * Loading file in matlab: 
	 * 			fid = fopen('wave.bin','r');
	 *			X=fread(fid, size_in_bytes, 'double', 'ieee-be'); 
	 *			fclose(fid);
	 * 
	 * @param dos a valid data output stream (ex : new DataOutputStream(new BufferedOutputStream(new FileOutputStream("wave.bin"))))
	 */
	public void saveToFile(DataOutputStream dos){
		
		try {
			for (int i=0; i<BUF_LEN; i++) {
				dos.writeDouble(getSample(i));
			}
		} catch (IOException e) {
			e.printStackTrace();
		} 
	}
	

	
	// --------------------------------- test ---------------------------------
	
	public static void main(String[] args) throws Exception {
		
		String file = "wave.bin";
		if (args.length>0) file = args[0];
		TargetDataLine line = AudioHub.obtainInputLine("Unknown USB Audio Device");
		DataOutputStream dos = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(file)));
		AudioSignal as = new AudioSignal();

		Runtime.getRuntime().addShutdownHook(new Thread() {
			public void run() {
				line.stop();
				line.flush();
				line.close();
				try {
					dos.close();
				} catch (IOException e) { e.printStackTrace(); }
				out("closing...");
			}
		});

		line.addLineListener(e -> System.out.println(e));
		line.open();
		line.start();
		while(true) {
			as.acquire(line);
			as.saveToFile(dos);
			out(as.level_dB());
		}
	}
}
