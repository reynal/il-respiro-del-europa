package fan;

import java.util.*;
import java.util.logging.Logger;

/**
 * a class that can create a kind of wave using fans driven by GPIO pins on a RPi.
 * @author sydxrey
 * 
 * TODO : random period
 *
 */
public class WindWave {
	
	private static final Logger LOGGER = Logger.getLogger("confLogger");
	private Fan[] fans;
	private Timer timer;
	//private ArrayList<TimerTask> taskList = new ArrayList<TimerTask>();
	
	public static final int PERIOD_IDLE = 10000;
	public static final int DURATION_IDLE = 4000;
	
	public static final int PERIOD_CHAOS = 10000;
	public static final int DURATION_CHAOS= 4900;
		
	public static enum State {
		IDLE,
		CHAOS
	}
	// TODO : intensity ! 0-100%

	/**
	 * Start a WindWave in default state "IDLE"
	 */
	public WindWave() {
		fans = new Fan[4];
		fans[0] = new Fan(Fan.FAN_0);
		fans[1] = new Fan(Fan.FAN_1);
		fans[2] = new Fan(Fan.FAN_2);
		fans[3] = new Fan(Fan.FAN_3);
		
		setState(WindWave.State.IDLE);

	}
	
	/**
	 * Schedule a fan start/stop task for the given fan index
	 * @param fanIdx 
	 * @param initialDelay initial delay in ms
	 * @param period total period in ms
	 * @param onDuration duration of ON phase in ms
	 */
	public void schedule(int fanIdx, int initialDelay, int period, int onDuration) {
		timer.schedule(fans[fanIdx].createStartTask(), initialDelay, period);
		timer.schedule(fans[fanIdx].createStopTask(), initialDelay+onDuration, period);
	}
	
	/**
	 * change the wave state
	 */
	public void setState(State state) {
		
		if (timer != null) timer.cancel();
		timer = new Timer();

		switch (state) {
		
		case IDLE:
			schedule(0, 0, 					PERIOD_IDLE, DURATION_IDLE);
			schedule(1, PERIOD_IDLE / 2, 	PERIOD_IDLE, DURATION_IDLE);
			
			break;
		
		case CHAOS:
			schedule(0, 0, 					PERIOD_CHAOS, DURATION_CHAOS);
			schedule(1, PERIOD_CHAOS / 2, 	PERIOD_CHAOS, DURATION_CHAOS);
			break;

		}
	}
	
	public void breath(double force) {
		
		System.out.println("WindWave: breath with force "+force);
	}
	
	
	// ------------------------- test -------------------------------------
	
	public static void main(String[] args) throws InterruptedException {
		
		WindWave w = new WindWave();
		w.setState(WindWave.State.IDLE);
		Thread.sleep(20000);
		w.setState(WindWave.State.CHAOS);
	}
	
	
	
}
