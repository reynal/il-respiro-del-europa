package audio;

import java.io.*;

import java.util.logging.Logger;

import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.TargetDataLine;

import application.Preferences;
import display.SentencesAnimator;

import static audio.AudioConstants.*;
import static application.Main.out;

/**
 * Main class for audio signal acquisition. This class is basically responsible
 * for opening, properly init'ing and closing underlying hardware.
 * 
 * Recorded data goes into an AudioSignal object.
 * 
 * @author sydxrey
 */
public class Microphone extends Thread {

	private static final Logger LOGGER = Logger.getLogger("confLogger");
	private TargetDataLine line;
	private AudioSignal audioSignal;
	private DataOutputStream waveDos, fftDos;
	private BreathDetector breathDetector; 
	private boolean isRunning; // makes it possible to "terminate" thread
	private SentencesAnimator sentencesAnimator;
	
	public static final boolean DEBUG_SAVE_WAVE = false;
	public static final boolean DEBUG_SAVE_FFT = false;
	

	/**
	 * Creates a Microphone that can record audio from a mic and feed an AudioSignal.
	 * @throws FileNotFoundException if I/O error with WAVE_FILE_DBG (debug only)
	 */
	public Microphone(SentencesAnimator sentencesAnimator) throws FileNotFoundException {

		this.sentencesAnimator = sentencesAnimator;
		audioSignal = new AudioSignal();
		breathDetector = new BreathDetector(audioSignal);
		
		waveDos = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(WAVE_FILE_DBG)));
		fftDos = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(FFT_FILE_DBG)));
		
		initShutdownHook();
		initAudioLine();
		
		start();
		
	}
	
	/**
	 * Init the underlying hardware.
	 */
	public void initAudioLine() {
		
		String str = Preferences.getPreferences().getStringProperty(Preferences.Key.MIXER);
		LOGGER.info("Opening mixer " + str);
		try {
			line = AudioHub.obtainInputLine(str);
			line.addLineListener(e -> System.out.println(e));
			line.open();
			line.start();
		} catch (LineUnavailableException e) { e.printStackTrace(); }
	}

	/**
	 * Make sure every piece of hardware gets properly closed.  
	 */
	public void closeHardware() {
		
		line.stop();
		line.close();
		try {
			waveDos.close();
			fftDos.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}
	
	/**
	 * Sets the piece of code that gets called when a SIGINT occurs (aka CTRL^C)
	 */
	public void initShutdownHook() {

		Runtime.getRuntime().addShutdownHook(new Thread() {
			public void run() {
				terminate();
				closeHardware();
			}
		});
	}
	
	/**
	 * Terminates thread as soon as possible.
	 */
	public void terminate() {
		isRunning = false;
	}
	
	/**
	 * Thread code. Basically an infinite loop that continuously triggers audio sample recording and
	 * save buffer to file if in DEBUG mode, for debugging purpose e.g. in Matlab. 
	 */
	@Override
	public void run() {
		
		isRunning = true;
		LOGGER.info("Microphone thread started");

		while (isRunning && audioSignal.acquire(line) != -1) {
			//breathDetector.isBreath();
			out("BreathDetector : " + (breathDetector.isBreath() ? "BREATH!!!" : ""));
			//out("Breath power =" + audioSignal.level_dB() + " dB");
			if (DEBUG_SAVE_WAVE) audioSignal.saveToFile(waveDos);
			if (DEBUG_SAVE_FFT) breathDetector.saveToFile(fftDos);
			if (breathDetector.isBreath()) sentencesAnimator.breath(1.0); // TODO CLaudio => tenir compte de fenetre glissante
		}
		
		LOGGER.info("Microphone thread terminated");
	}

	// --------------------------------- test ---------------------------------
	
	public static void main(String[] args) throws Exception {

		Microphone m = new Microphone(new SentencesAnimator());

	}

}
