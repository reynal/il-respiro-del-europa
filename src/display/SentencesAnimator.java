package display;

import static display.AnimationConstants.SCREEN_0;
import static display.AnimationConstants.SCREEN_1;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;

import javax.swing.Timer;

import application.UserInterface;

/**
 * Class responsible for the visual animation of the sentence display
 * on the two video projectors.
 * 
 * @author sydxrey
 *
 */
public class SentencesAnimator implements ActionListener {
	
	private UserInterface ui;
	private SentencesFileReader sentencesFileReader;
	
	private Timer timer;
	private double time;
	public static final int TIMER_PERIOD_MS = 10; // ms
	
	private Projector projector1, projector2;
	
	private LdpcDecoder ldpcDec;
	
	private double chaosIntensity; // set by audio thread depending on breath detection
	
	/**
	 * @throws IOException if sentence file can't be loaded 
	 */
	public SentencesAnimator(UserInterface ui) throws IOException {
		
		this.ui = ui;
		this.time = 0.0;
		sentencesFileReader = new SentencesFileReader();
		
		projector1 = new Projector(SCREEN_0);
		projector2 = new Projector(SCREEN_1);
		ldpcDec = new LdpcDecoder(); 
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
		//alpha = 0f; // debug

		// TODO slow down how often decoder is called (at time % interval?) 
		double[] q0 = ldpcDec.nextIteration();
		float per = (float)ldpcDec.getPER();
        //System.out.println("per = "+per);
		
        // TODO as done here, always the first projector "wins", thus always "even" sentences
        projector1.modImage(q0,(1.0f-per)*alpha); 
        projector2.modImage(q0,per*(1-alpha));

        // TODO setAlpha does nothing, include this into modImage (as third param?)
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
	public void setChaosIntensity(double intensity) {
		
		// if sudden increase:
		if (intensity > chaosIntensity) {
			
		}
		
		chaosIntensity = intensity;
		
		// TODO : do something !
		
	}
	
	private void pickNewSentencePair() {
		
		//TODO : pick from a file
		String[] ss = sentencesFileReader.fetchNewPair();
		projector1.setSentence(ss[0]);
		projector2.setSentence(ss[1]);
		ldpcDec.initState();
	}
	
	// --------------------------------- test ---------------------------------

	public static void main(String[] args) throws Exception {

		SentencesAnimator sa = new SentencesAnimator(null);
		
		Thread t = new Thread(() -> {
			while(true) {
			sa.setChaosIntensity(Math.random()); 
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
