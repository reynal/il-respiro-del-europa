package fan;

import java.util.*;
import java.util.logging.Logger;

import application.Preferences;
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
	
	public double chaosThreshold = 0.9;
	
	public static enum State {
		CHAOS,
		IDLE;
	}
	
	private int T=5000;
	private int offset0=0; // %
	private int offset1=20;
	private int offset2=40;
	private int offset3=60;
	private int duty0=2000; // ms
	private int duty1=2000;
	private int duty2=2000;
	private int duty3=3000;
	
	private int T_idle=120000;
	private int offset0_idle=22; // %
	private int offset1_idle=32;
	private int offset2_idle=47;
	private int offset3_idle=88;
	private int duty0_idle=1000; // ms
	private int duty1_idle=1000;
	private int duty2_idle=1000;
	private int duty3_idle=1000;

	
	private double chaosIntensity; // set by audio thread depending on breath detection
	
	private UserInterface ui;

	/**
	 * Start a WindWave in default state "IDLE"
	 */
	public WindWave(UserInterface ui) {
		this.ui = ui;
		if (ui!=null) ui.setWindWave(this);
		this.chaosThreshold = Preferences.getPreferences().getDoubleProperty(Preferences.Key.CHAOS_WW_THR);
		this.T = Preferences.getPreferences().getIntProperty(Preferences.Key.WW_T);
		this.offset0 = Preferences.getPreferences().getIntProperty(Preferences.Key.WW_OFFSET0);
		this.offset1 = Preferences.getPreferences().getIntProperty(Preferences.Key.WW_OFFSET1);
		this.offset2 = Preferences.getPreferences().getIntProperty(Preferences.Key.WW_OFFSET2);
		this.offset3 = Preferences.getPreferences().getIntProperty(Preferences.Key.WW_OFFSET3);
		this.duty0 = Preferences.getPreferences().getIntProperty(Preferences.Key.WW_DUTY0);
		this.duty1 = Preferences.getPreferences().getIntProperty(Preferences.Key.WW_DUTY1);
		this.duty2 = Preferences.getPreferences().getIntProperty(Preferences.Key.WW_DUTY2);
		this.duty3 = Preferences.getPreferences().getIntProperty(Preferences.Key.WW_DUTY3);

		this.T_idle = Preferences.getPreferences().getIntProperty(Preferences.Key.WW_T);
		this.offset0_idle = Preferences.getPreferences().getIntProperty(Preferences.Key.WW_OFFSET0);
		this.offset1_idle = Preferences.getPreferences().getIntProperty(Preferences.Key.WW_OFFSET1);
		this.offset2_idle = Preferences.getPreferences().getIntProperty(Preferences.Key.WW_OFFSET2);
		this.offset3_idle = Preferences.getPreferences().getIntProperty(Preferences.Key.WW_OFFSET3);
		this.duty0_idle = Preferences.getPreferences().getIntProperty(Preferences.Key.WW_DUTY0);
		this.duty1_idle = Preferences.getPreferences().getIntProperty(Preferences.Key.WW_DUTY1);
		this.duty2_idle = Preferences.getPreferences().getIntProperty(Preferences.Key.WW_DUTY2);
		this.duty3_idle = Preferences.getPreferences().getIntProperty(Preferences.Key.WW_DUTY3);

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
	public void schedule(int fanIdx, int periodMs, int periodOffsetPercent, int dutyCycleMs) {
		
		if (dutyCycleMs < 0) dutyCycleMs = 0;
		else if (dutyCycleMs > periodMs) dutyCycleMs = periodMs;
		
		// start at periodOffset, stops at periodOffset + period * dutyCycle / 100.0
		int offset = (int)(0.01 * periodOffsetPercent * periodMs);
		int onDuration = dutyCycleMs;
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
		
		if (state == State.IDLE) {
			for (int fanIdx=0; fanIdx < 4; fanIdx++) fans[fanIdx].stop();
//			schedule(0, T_idle, offset0_idle, duty0_idle);
//			schedule(1, T_idle, offset1_idle, duty1_idle);
//			schedule(2, T_idle, offset2_idle, duty2_idle);
//			schedule(3, T_idle, offset3_idle, duty3_idle);			
			return; // pour le moment ca s'arrete totalement
		}
		
		if (state == State.CHAOS) {
			schedule(0, T, offset0, duty0);
			schedule(1, T, offset1, duty1);
			schedule(2, T, offset2, duty2);
			schedule(3, T, offset3, duty3);
			return;
		}
		

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
		if (chaosIntensity < this.chaosThreshold) setState(State.IDLE);
		else setState(State.CHAOS);
		
	}
	
	
	// ------------------------- test -------------------------------------
	
	public static void main(String[] args) throws InterruptedException {
		
		// open UI:
		UserInterface ui = new UserInterface();
		
		WindWave w = new WindWave(ui);
		w.setState(WindWave.State.IDLE);
		Thread.sleep(5000);
		w.setState(WindWave.State.CHAOS); // gere par l'UI
	}
	
	
	
}
