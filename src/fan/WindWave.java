package fan;

import java.util.*;
import java.util.logging.Logger;

import application.UserInterface;

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
	private State state;
	
	public static final double BREATHE_THR = 0.8;  // breathe below, chaos above
	public static final double GENTLE_THR = 0.7; // gentle below
	public static final double IDLE_THR = 0.2; // idle below
	
	public double breath_threshold = BREATHE_THR;
	public double gentle_threshold = GENTLE_THR;
	public double idle_threshold = IDLE_THR;
	
	public static enum State {
		//    T(ms)     Offset1 Duty1   Offset2 Duty2   Offset3 Duty3   Offset4 Duty4
		CHAOS(5000, 	0, 		80, 	25, 	66, 	50, 	66, 	75, 	66),
		BREATHE(20000, 	0, 		10, 	25, 	10, 	50, 	10, 	75, 	10),
		GENTLE(10000, 	0, 		10, 	25, 	10, 	50, 	10, 	75, 	10),
		IDLE(200000, 	0, 		1, 		25, 	10, 	50, 	10, 	75, 	10);
		
		public int T, offset0, duty0, offset1, duty1, offset2, duty2, offset3, duty3; // offset et duty sont en percentage de T (entre 0 et 100)
		State(int T, int offset0, int duty0, int offset1, int duty1, int offset2, int duty2, int offset3, int duty3){
			this.T=T;
			this.offset0=offset0;
			this.offset1=offset1;
			this.offset2=offset2;
			this.offset3=offset3;
			this.duty0=duty0;
			this.duty1=duty1;
			this.duty2=duty2;
			this.duty3=duty3;
		}
	}
	

	private double chaosIntensity; // set by audio thread depending on breath detection
	
	private UserInterface ui;

	/**
	 * Start a WindWave in default state "IDLE"
	 */
	public WindWave(UserInterface ui) {
		this.ui = ui;
		if (ui!=null) ui.setWindWave(this);
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
	 * @param periodMs total period in ms
	 * @param periodOffsetPercent initial delay in % of period  (from 0 to 100)
	 * @param dutyCycle duration of ON phase in % of period (from 0 to 100)
	 */
	public void schedule(int fanIdx, int periodMs, int periodOffsetPercent, int dutyCycle) {
		
		if (dutyCycle < 0) dutyCycle = 0;
		else if (dutyCycle > 100) dutyCycle = 100;
		
		// start at periodOffset, stops at periodOffset + period * dutyCycle / 100.0
		int offset = (int)(0.01 * periodOffsetPercent * periodMs);
		int onDuration = (int)(0.01 * dutyCycle * periodMs);
		timer.schedule(fans[fanIdx].createStartTask(), offset,            periodMs); // START
		timer.schedule(fans[fanIdx].createStopTask(),  offset+onDuration, periodMs); // STOP
	}
	
	/**
	 * change the wave state
	 */
	public void setState(State state) {
		
		if (state == this.state) return; // on fait quelque chose que si ca change!
		
		if (ui!=null) ui.setWindWaveState(state);
		
		this.state = state;
		LOGGER.info("!!! New WindWave state = " + state);
		
		if (timer != null) timer.cancel();
		timer = new Timer();
		
		if (state == State.IDLE) return; // pour le moment ca s'arrete totalement
		
		schedule(0, state.T, state.offset0, state.duty0);
		schedule(1, state.T, state.offset1, state.duty1);
		schedule(2, state.T, state.offset2, state.duty2);
		schedule(3, state.T, state.offset3, state.duty3);

	}
	
	State getState() {
		return this.state;
	}
	
	/**
	 * Called from the audio thread every time a breathing detection occurs.
	 * @param force b/w 0 and 1
	 */
	public void setChaosIntensity(double intensity) {
		
		chaosIntensity = intensity;
		
		if (chaosIntensity < this.idle_threshold) setState(State.IDLE);
		else if (chaosIntensity < this.gentle_threshold) setState(State.GENTLE);
		else if (chaosIntensity < this.breath_threshold) setState(State.BREATHE);
		else setState(State.CHAOS);
		
	}
	
	
	// ------------------------- test -------------------------------------
	
	public static void main(String[] args) throws InterruptedException {
		
		// open UI:
		UserInterface ui = new UserInterface();
		
		WindWave w = new WindWave(ui);
		//w.setState(WindWave.State.IDLE);
		//Thread.sleep(20000);
		//w.setState(WindWave.State.CHAOS); // gere par l'UI
	}
	
	
	
}
