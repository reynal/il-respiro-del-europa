package application;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

import audio.Microphone;
import display.SentencesAnimator;
import fan.WindWave;

/**
 * 
 * Claudio : display/ldpc
 * - integrer LDPC dans Projector / SentencesAnimator
 * - raffinement : quand et comment choisir une nouvelle paire de phrases ?
 * 
 * Syd : fan/application
 * - UI
 * - WindWave.breath()
 * - une classe pour récupérer une paire de phrases depuis un fichier
 * - 
 * 
 * 
 * 
 * @author sydxrey
 *
 */
public class Main {
	
	private static final Logger LOGGER = Logger.getLogger("confLogger");
	static {
		try {
			LogManager.getLogManager().readConfiguration(new FileInputStream("./logger.properties"));
		} catch (IOException exception) {
			LOGGER.log(Level.SEVERE, "Error in loading configuration", exception);
		}
	}

	static void testLogger() throws SecurityException, FileNotFoundException, IOException {

		Main.LOGGER.setLevel(Level.SEVERE);
		Main.LOGGER.info("une information");
		Main.LOGGER.warning("un warning");
		Main.LOGGER.severe("un truc grave");
		Main.LOGGER.fine("un truc fin");

		LogManager.getLogManager().readConfiguration(new FileInputStream("./logger.properties"));

		System.out.println("-----------------");

		Main.LOGGER.info("une information");
		Main.LOGGER.warning("un warning");
		Main.LOGGER.severe("un truc grave");
		Main.LOGGER.fine("un truc fin");

	}
	
	/*
	 * 
	 */
	public static void initShutdownHook() {

		Runtime.getRuntime().addShutdownHook(new Thread() {
			public void run() {
				//closeHardware();
			}
		});
	}
	
	public static void out(String str) {
		System.out.println(str);
	}
	
	public static void out(Object o) {
		System.out.println(o.toString());
	}
	
	
	// ---------------------------------------------------------------------------
	
	public static void main(String[] args) throws Exception {
		//testLogger();
				
		// open UI:
		UserInterface ui = new UserInterface();

		// start the thread for sentence animation
		SentencesAnimator sentencesAnimator = new SentencesAnimator(ui);

		// create waves with fans
		WindWave windWave = new WindWave();

		// record sound and check for breathing:
		Microphone microphone = new Microphone(sentencesAnimator, windWave, ui);
		
		// bind everything together:
		ui.setWindWave(windWave);
		ui.setSentencesAnimator(sentencesAnimator);
		ui.setMicrophone(microphone);
		ui.setVisible(true);
		
		
	}

}
