package audio;

import java.util.Arrays;

import javax.sound.sampled.*;

/**
 * 
 * @author sydxrey
 *
 * Audio input ports are sources; audio output ports are targets.
 * 
 */
public class Microphone {

	public static void main(String[] args) throws Exception {
		
		//openMicrophoneLine();
		
		//Line.Info[] microphones = AudioSystem.getSourceLineInfo(Port.Info.MICROPHONE);
		//describeLineInfos(microphones);
	}
	
	public static void describeLineInfos(Line.Info[] infos, Mixer mixer) throws LineUnavailableException {
		
		for (Line.Info info: infos) {
			Line line = mixer.getLine(info);
			System.out.println("\t" + info + ", " 
					+ line 
					+ (line instanceof Port ? ", PORT" : "") 
					+ (line instanceof SourceDataLine ? ", Can be written to" : "")
					+ (line instanceof TargetDataLine ? ", Can be read from" : "")); 
		}
	}
	
	// keeps SourceDataLine (aka microphones), discard others: 
	public static Line listInputLines() throws LineUnavailableException {
		
		for (Mixer.Info mixerInfo: AudioSystem.getMixerInfo()) {
			
			Mixer mixer = AudioSystem.getMixer(mixerInfo);			
			
			//Line.Info[] lineInfos = mixer.getSourceLineInfo();
			Line.Info[] lineInfos = mixer.getTargetLineInfo();
			
			if (lineInfos.length>0) {
				
				System.out.println("MIXER : " + mixerInfo.getDescription() + " by " + mixerInfo.getVendor() + " with " + lineInfos.length + " sources");
				
				for (Line.Info info : lineInfos) {
					//if (info instanceof Port.Info) {
						//return line;
					//}
				}
			}
		}
		return null;
	}
	
	public static void openMicrophoneLine() throws LineUnavailableException {
		
		Line line = AudioSystem.getLine(Port.Info.MICROPHONE);
		System.out.println(line.getLineInfo() + ", "+ line);
		
		
	}
}
