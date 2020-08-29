package fan;

import static application.Main.out;
import static application.Platform.DESKTOP;
import static application.Platform.RASPBERRY;

import java.util.TimerTask;
import java.util.logging.Logger;
import com.pi4j.io.gpio.*;

import application.Platform;


/**
 * A fan that can be driven by a GPIO pin on a RPi4.
 * 
 * @author sydxrey
 *
 */
public class Fan {
	
	private static final Logger LOGGER = Logger.getLogger("confLogger");

	public final static com.pi4j.io.gpio.Pin FAN_0 = RaspiPin.GPIO_26; // pin 32 (GPIO_XX = wPi numbering scheme)
	public final static com.pi4j.io.gpio.Pin FAN_1 = RaspiPin.GPIO_27; // pin 36 
	public final static com.pi4j.io.gpio.Pin FAN_2 = RaspiPin.GPIO_28; // pin 38 
	public final static com.pi4j.io.gpio.Pin FAN_3 = RaspiPin.GPIO_29; // pin 40
	
	private GpioPinDigitalOutput rpiPin;
	private com.pi4j.io.gpio.Pin pinNumber;

	/**
	 * try to provision GPIO output pins for the relay shield. If this fails, which might
	 * mean we're not on a Raspberry (=> desktop platform), resorts to printf like debug
	 * to know which fan is running. 
	 * 
	 * @param pin FAN_O, FAN_1, FAN_2 or FAN_3
	 * @throws java.lang.UnsatisfiedLinkError if this code is not currently running on a RPi (this can be used to adapt the calling method)
	 */
	public Fan(com.pi4j.io.gpio.Pin pin) {
		try {
			this.pinNumber = pin;
			rpiPin = GpioFactory.getInstance().provisionDigitalOutputPin(pin, PinState.LOW);
		}		
		catch (java.lang.UnsatisfiedLinkError e) {
			LOGGER.warning("Error provisioning RPi pin " + pin + " => this code is not running on a Raspberry! Resorting to println for debugging purpose...");
		}
	}
	
	/**
	 * starts or stops fan with the given number
	 * @param active
	 */
	public void setState(boolean active) {
		
		if (rpiPin == null) out("FAN " + pinNumber + ": " + (active ? "OFF" : "ON")); // not running on RPi
		else rpiPin.setState(active);
	
	}
		
	public void start() {
		setState(false);
	}
	
	public void stop() {
		setState(true);
	}
	
	/**
	 * Makes the fan start/stop action "schedulable" by the java.util.Timer object
	 */
	class StartTask extends TimerTask {
		
		public void run() {
			//System.out.println("start fan");
			start();
		}
	}
	
	/**
	 * Makes the fan start/stop action "schedulable" by the java.util.Timer object
	 */
	class StopTask extends TimerTask {
		
		public void run() {
			//System.out.println("stop fan");
			stop();
		}
	}

	/**
	 * @return a TimerTask suited for a util.Timer object
	 */
	TimerTask createStartTask() {
		return new StartTask();
	}

	TimerTask createStopTask() {
		return new StopTask();
	}

	// ------------------------- test -------------------------------------
	
	public static void main(String[] args) throws InterruptedException {
		
		Fan v = new Fan(FAN_2);
		int i=0;
		boolean active = false;
		while(i++ < 10) {
			System.out.println(i);
			v.setState(active);
			active = !active;
			Thread.sleep((int)(1000));// * Math.random()));
		}
	}

}
