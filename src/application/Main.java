package application;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

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
		//System.out.println("ciao mundo");
		testLogger();
	}

}
