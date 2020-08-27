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
	public static final double DECAY_S = 10.; // s
	
	private Timer timer;
	private double time;
	
	private Projector projector1, projector2;	
	
	private double chaosIntensity; // increases suddenly on breathing detection, otherwise decreases naturally over time
	
	private double decayFactor; // chaosIntensity gets multiplied by this factor over time 
	
	
	/**
	 * 
	 */
	public SentencesAnimator() {

		this.time = 0.0;
		this.chaosIntensity = 1.0;
		this.decayFactor = Math.exp(-TIMER_PERIOD_MS / (DECAY_S * 1000.)); // TODO : load DECAY_S from property file
		System.out.println(decayFactor);
		
		projector1 = new Projector(SCREEN_0);
		projector2 = new Projector(SCREEN_1);

		timer = new Timer(TIMER_PERIOD_MS, this);
		
		start();
	}
	
	public void start() {
		timer.start();
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		time++;	
		chaosIntensity *= decayFactor;
		System.out.println(chaosIntensity);

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
		// TODO : make it go to zero slowly (20'/30')
		alpha = Math.max(alpha, 0);
		alpha = Math.min(alpha, 1);

		projector1.setAlpha(alpha * chaosIntensity);
		projector2.setAlpha((1-alpha) * chaosIntensity);
		
		projector1.repaint();
		projector2.repaint();
		
	}
	
	private double sine(double amp, double freq) {
		return amp * Math.sin(2.0 * Math.PI * freq * time);
	}
	
	/**
	 * Call this from the audio thread every time a breathing detection occurs.
	 * @param force b/w 0 and 1
	 */
	public void breath(double force) {
		
		
	}
	
	private void pickNewSentencePair() {
		
		projector1.sentence = "Screen 0";
		projector2.sentence = "Screen 1";
	}
	
	// --------------------------------- test ---------------------------------

	public static void main(String[] args) throws Exception {

		new SentencesAnimator();
	}

}
