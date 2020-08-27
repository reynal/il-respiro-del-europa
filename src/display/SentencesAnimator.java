package display;

import static display.AnimationConstants.SCREEN_0;
import static display.AnimationConstants.SCREEN_1;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Timer;

/**
 * Class responsible for the visual animation of the sentence display
 * on the two video projectors.
 * 
 * @author sydxrey
 *
 */
public class SentencesAnimator implements ActionListener {
	
	public static final int TIMER_PERIOD_MS = 10; // ms
	public static final double ATTACK_S = 1.; // s
	public static final double DECAY_S = 2.; // s
	
	// the dynamics of the animation is governed by two phases:
	// DECAY : no breathing, chaosIntensity just decays to zero
	// ATTACK : after a breathing occurred, chaosIntensity increases to a target value depending on the force of the breathing, then decays if nothing happens
	private enum StateMachine {
		ATTACK, 
		DECAY
	}
	
	private StateMachine stateMachine = StateMachine.DECAY;
	
	private Timer timer;
	private double time;
	
	private Projector projector1, projector2;	
	
	private double chaosIntensity; // increases suddenly on breathing detection, otherwise decreases naturally over time
	
	private double decayFactor; // in DECAY phase, chaosIntensity gets multiplied by this factor over time
	//private double attackFactor; // ibid, for ATTACK phase
	private double tmpExp; // tmp variable that is init'd to 1.0, and then plays the role of exp(-t/tau) : 
							// in both phases, tmpExp *= attackFactor or decayFactor, 
							// and then  chaosIntensity += dx and dx = deltaChaos * (1 - tmpExp) 
	
	
	// y[n] = a*y[n-1]+b*(x[n]+x[n-1])
			
	/**
	 * 
	 */
	public SentencesAnimator() {

		this.time = 0.0;
		this.chaosIntensity = 1.0;
		this.decayFactor = Math.exp(-TIMER_PERIOD_MS / (DECAY_S * 1000.)); // TODO : load DECAY_S from property file
		//this.attackFactor = Math.exp(-TIMER_PERIOD_MS / (ATTACK_S * 1000.)); // TODO : load from property file
		
		projector1 = new Projector(SCREEN_0);
		projector2 = new Projector(SCREEN_1);
		pickNewSentencePair();

		timer = new Timer(TIMER_PERIOD_MS, this);
		
		start();
	}
	
	public void start() {
		timer.start();
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		time++;	
		switch (stateMachine) {
		case ATTACK:
			break;
		case DECAY:
			chaosIntensity *= decayFactor;
			break;
		}
		
		//System.out.println(chaosIntensity);

		double a1=0.35;
		double a2=0.25;
		double a3=0.15;
		double T1 = 3.5; // secondes
		double T2 = 1.75;
		double T3 = 1.05;
		double f1 = 0.001 * TIMER_PERIOD_MS / T1;
		double f2 = 0.001 * TIMER_PERIOD_MS / T2;
		double f3 = 0.001 * TIMER_PERIOD_MS / T3;
		
		float alpha = (float)(0.5 + sine(a1, f1) + sine(a2, f2) + sine(a3, f3) + 0.1 * Math.random());
		alpha = Math.max(alpha, 0);
		alpha = Math.min(alpha, 1);
		alpha = 0f; // debug

		projector1.setAlpha(alpha * chaosIntensity);
		projector2.setAlpha((1-alpha) * chaosIntensity);
		
		projector1.repaint();
		projector2.repaint();
		
	}
	
	private double sine(double amp, double freq) {
		return amp * Math.sin(2.0 * Math.PI * freq * time);
	}
	
	/**
	 * Called from the audio thread every time a breathing detection occurs.
	 * @param force b/w 0 and 1
	 */
	public void breath(double force) {
		
		
		if (force <= 0.0) return; // DO NOTHING if no breath detected 
		else {
			System.out.println("SentencesAnimator: breath with force "+force);
			chaosIntensity += force; // TODO : add attack phase, adjust formulae + implement it through a low-pass digital filter
			if (chaosIntensity > 1.0) chaosIntensity = 1.0;
		}
		// Code missing !
		
	}
	
	private void pickNewSentencePair() {
		
		//TODO : pick from a file
		projector1.sentence = "Screen 0";
		projector2.sentence = "Screen 1";
		// TODO : restart decoder !
	}
	
	// --------------------------------- test ---------------------------------

	public static void main(String[] args) throws Exception {

		SentencesAnimator sa = new SentencesAnimator();
		
		Thread t = new Thread(() -> {
			while(true) {
			sa.breath(Math.random()); 
			try {
				Thread.sleep((long) (10000 * Math.random()));
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			}});	
		t.start();
	}

}
