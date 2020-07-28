package display;

import static display.AnimationConstants.SCREEN_0;
import static display.AnimationConstants.SCREEN_1;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Timer;


public class SentencesAnimator implements ActionListener {
	
	public static final int TIMER_PERIOD_MS = 50; // ms
	
	private Timer timer;
	private double time;
	
	Projector projector1, projector2;	
	
	public SentencesAnimator() {

		this.time = 0.0;
		
		projector1 = new Projector(SCREEN_0);
		projector2 = new Projector(SCREEN_1);

		timer = new Timer(TIMER_PERIOD_MS, this);
	}
	
	public void start() {
		timer.start();
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		time++;		

		Projector p = projector1;
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
		p.setAlpha(alpha);
		
		projector2.setAlpha(1-alpha);
	}
	
	public double sine(double amp, double freq) {
		return amp * Math.sin(2.0 * Math.PI * freq * time);
	}
	
	// --------------------------------- test ---------------------------------

	public static void main(String[] args) throws Exception {

		new SentencesAnimator().start();
	}

}
