package audio;

import java.io.*;
import java.util.*;

import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.TargetDataLine;

import application.Preferences;

import static audio.AudioConstants.*;

/**
 * 
 * @author sydxrey
 *
 *         Audio input ports are sources; audio output ports are targets.
 * 
 */
public class Microphone extends Thread {

	TargetDataLine line;
	AudioSignal audioSignal;

	public Microphone(AudioSignal audioSignal) throws LineUnavailableException {

		this.audioSignal = audioSignal;
		
		String str = Preferences.getPreferences().getStringProperty(Preferences.Key.MIXER);
		System.out.println("Loading mixer " + str);
		line = AudioHub.obtainInputLine(str);
		line.addLineListener(e -> System.out.println(e));
		line.open();
	}


	public void run() {

		line.start();

		int ii = 0;
		while (audioSignal.acquire(line) != -1) {
			System.out.println(audioSignal.level_dB());
			if (ii++ > 10) break;
			//break;
		}

		line.stop();
		line.close();
		
		audioSignal.closeOutputStream();
	}


	public static void main(String[] args) throws Exception {

		System.out.println("microphone");
		AudioSignal s = new AudioSignal();
		Microphone m = new Microphone(s);
		m.start();

	}

}
