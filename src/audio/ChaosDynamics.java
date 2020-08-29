package audio;

import java.util.logging.Logger;

import application.UserInterface;

/**
 * Class responsible for computing the evolution of the "chaos intensity" over time, depending
 * on breath detection at microphone output.
 * 
 * @author sydxrey
 *
 */
public class ChaosDynamics {
	
	private static final Logger LOGGER = Logger.getLogger("confLogger");

	//public static final double ATTACK_S = 1.; // s
	public static final double DECAY_S = 2.; // s
	public final static double TIMER_PERIOD_S = (double)AudioConstants.BUF_LEN / AudioConstants.SAMPLE_FREQ;


	// the dynamics of the animation is governed by two phases:
	// DECAY : no breathing, chaosIntensity just decays to zero
	// ATTACK : after a breathing occurred, chaosIntensity increases to a target
	// value depending on the force of the breathing, then decays if nothing happens
	/*private enum StateMachine {
		ATTACK, DECAY
	}

	private StateMachine stateMachine = StateMachine.DECAY;*/

	private double chaosIntensity; // increases suddenly on breathing detection, otherwise decreases naturally over
									// time

	private double decayFactor; // in DECAY phase, chaosIntensity gets multiplied by this factor over time
	
	// private double attackFactor; // ibid, for ATTACK phase
	//private double tmpExp; // tmp variable that is init'd to 1.0, and then plays the role of exp(-t/tau) :
							// in both phases, tmpExp *= attackFactor or decayFactor,
							// and then chaosIntensity += dx and dx = deltaChaos * (1 - tmpExp)
	private UserInterface ui;

	public double chaosForceValue=1.0;

	public double breathForceValue = 0.25;

	// y[n] = a*y[n-1]+b*(x[n]+x[n-1])

	/**
	 * 
	 */
	public ChaosDynamics(UserInterface ui) {
		this.ui = ui;
		ui.setChaosDynamics(this);
		this.chaosIntensity = 0.0;
		this.decayFactor = Math.exp(-TIMER_PERIOD_S / DECAY_S); // TODO : load DECAY_S from property file
		System.out.println("decayFactor="+decayFactor);
		// this.attackFactor = Math.exp(-TIMER_PERIOD_MS / (ATTACK_S * 1000.)); // TODO
		// : load from property file
	}
	
	

	public double getChaosIntensity() {
		return chaosIntensity;
	}

	public void setDecayTime(double tau) {
		LOGGER.info("Setting decay time to " + tau);
		this.decayFactor = Math.exp(-TIMER_PERIOD_S / tau); 
	}

	public void forceChaosIntensity() {
		chaosIntensity = chaosForceValue;
	}

	/**
	 * Called from main audio thread on a regular basis (audio frame duration)
	 * 
	 * @param force b/w 0 and 1
	 */
	void updateDynamics(double breathForce) {
		
		//System.out.println("breathForce="+breathForce);
		
		if (breathForce <= 0.0) { // no breath detected
			chaosIntensity *= decayFactor;
		}
		else {
			//System.out.println("Breath detected with force " + breathForce);
			chaosIntensity += breathForce; // TODO : add attack phase, adjust formulae + implement it through a low-pass digital filter
			if (chaosIntensity > 1.0)
				chaosIntensity = 1.0;
		}
		
		//System.out.println("chaosIntensity=" + chaosIntensity);
		
		if (ui != null) ui.setChaosIntensity(chaosIntensity);
			
	}



	public void forceBreath() {
		updateDynamics(breathForceValue);
	}

}
