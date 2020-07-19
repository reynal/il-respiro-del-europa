package audio;

import java.util.*;

import javax.sound.sampled.*;

public class AudioHub {
	
	public static final int SAMPLE_FREQ = 24000;
	public static final int SAMPLE_SIZE_BYTE = 1;
	public static final int CHANNELS = 1;
	public static final boolean IS_BIG_ENDIAN = true;
	public static final boolean IS_SIGNED = true;

	/** 
	 * Return a line that's appropriate for recording sound from a microphone.
	 * example of use: TargetDataLine line = obtainInputLine("Unknown USB Audio Device");
	 * @param mixerName
	 */
	public static TargetDataLine obtainInputLine(String mixerName) throws LineUnavailableException {
		
		// fetch first mixer having the given name:
		Mixer.Info info = Arrays.stream(AudioSystem.getMixerInfo()).filter(e -> e.getName().equalsIgnoreCase(mixerName)).findFirst().get();
		
		TargetDataLine line = AudioSystem.getTargetDataLine(new AudioFormat(SAMPLE_FREQ, SAMPLE_SIZE_BYTE*8, CHANNELS, IS_BIG_ENDIAN, IS_SIGNED), info);
				
		return line;
		
	}
	
	/**
	 * List every mixer on the system
	 */
	public static void printMixers()  {
		
		System.out.println("Mixers:");
		Arrays.stream(AudioSystem.getMixerInfo()).forEach(e -> System.out.println("\t " + e.getName() + "(" + e.getDescription() + " by " + e.getVendor() + ")"));
		
	}

	/**
	 * List every target data line for each mixer on the system
	 */
	public static void printTargetDataLines() {
		
		Arrays.stream(AudioSystem.getMixerInfo()).forEach(e -> printTargetDataLines(e));
	}
	
	/**
	 * List all target data lines for the given mixer
	 */
	public static void printTargetDataLines(Mixer.Info info) {
		
		System.out.println("Input lines for mixer name \""+info.getName() + "\":");
		Arrays.stream(AudioSystem.getMixer(info).getTargetLineInfo()).filter(e -> e.getLineClass().equals(TargetDataLine.class)).forEach(e -> System.out.println("\t"+e));
		
	}

	public static Object[] listMixers() {
		
		return Arrays.stream(AudioSystem.getMixerInfo()).map(e -> AudioSystem.getMixer(e)).toArray();
		
	}

	public static void main(String[] args) throws Exception {
		
		printTargetDataLines();
		TargetDataLine line = obtainInputLine("Unknown USB Audio Device");
		System.out.println(line);
	}
	
}
