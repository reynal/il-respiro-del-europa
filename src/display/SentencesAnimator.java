package display;

import java.awt.Dimension;
import java.awt.GraphicsEnvironment;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.logging.Logger;

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
	
	private static final Logger LOGGER = Logger.getLogger("confLogger");
	
	private UserInterface ui;
	private SentencesFileReader sentencesFileReader;
	
	private Timer timer;
	private int time;
	private int lastModTime;
	public static final int TIMER_PERIOD_MS = 10; // ms
	public static final int DECODER_ITERATION_PERIOD = 10; // = 10 * TIMER_PERIOD_MS = 100ms
	public int decoder_iteration_period = DECODER_ITERATION_PERIOD; 
	
	private Projector projector1, projector2;
	
	private LdpcDecoder ldpcDec;
	private double[] q0 = null;
	private float per;
	
	private double chaosIntensity; // set by audio thread depending on breath detection
	private double meanAlpha = 0.5;
	private double deltaMeanAlpha;
	private double ampSine; // modulation of alpha, aka sin(wt)
	public static final double DELTA_MEAN_ALPHA = 0.001; // rate of increase of <alpha> over time (large => the final sentence gets quickly selected)
	
	/**
	 * @throws IOException if sentence file can't be loaded 
	 */
	public SentencesAnimator(UserInterface ui) throws IOException {
		
		this.ui = ui;
		if (ui!= null) ui.setSentencesAnimator(this);
		this.time = 0;
		lastModTime = 0;
		sentencesFileReader = new SentencesFileReader();
		
		projector1 = new Projector(0);
		if (GraphicsEnvironment.getLocalGraphicsEnvironment().getScreenDevices().length > 1)
			projector2 = new Projector(1);
		else
			{
			projector2 = new Projector(0);
			Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
			projector2.setLocation(dim.width/2+5, 0);
			}
		ldpcDec = new LdpcDecoder(); 

		timer = new Timer(TIMER_PERIOD_MS, this);
		
		start();

		pickNewSentencePair();
	}
	
	public void start() {
		timer.start();
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		
		//System.out.println(chaosIntensity);
		
		int curModTime = (time % decoder_iteration_period);
		if (curModTime == 0) {
			q0 = ldpcDec.nextIteration();
			per = (float)ldpcDec.getPER();
		    
			//System.out.println("per = "+per);
		}
		//else { // partial copy of q0
			projector1.messUpDisplay(ldpcDec.q0,lastModTime,curModTime,decoder_iteration_period);  
		    projector2.messUpDisplay(ldpcDec.q0,lastModTime,curModTime,decoder_iteration_period);
			
		//}		
	    lastModTime = curModTime;
		
		// make alpha evolve towards one sentence only with sine modulation waning off:		
		meanAlpha += deltaMeanAlpha;
		meanAlpha = Math.max(meanAlpha, 0);
		meanAlpha = Math.min(meanAlpha, 1);
		ampSine *= 0.99; 
		//System.out.println("<alpha>= "+meanAlpha + ", ampSine=" + ampSine);

		double a1=0.35;
		double a2=0.25;
		double a3=0.15;
		double T1 = 3.5; // secondes
		double T2 = 1.75;
		double T3 = 1.05;
		double f1 = 0.001 * TIMER_PERIOD_MS / T1;
		double f2 = 0.001 * TIMER_PERIOD_MS / T2;
		double f3 = 0.001 * TIMER_PERIOD_MS / T3;
		
		float alpha = (float)(meanAlpha + sine(a1, f1) + sine(a2, f2) + sine(a3, f3) + per * (Math.random()-0.5));
		alpha = Math.max(alpha, 0);
		alpha = Math.min(alpha, 1);
		//alpha = 0.5f; // debug
		
		//System.out.println("alpha= "+alpha);
		//System.out.println("chaosIntensity= "+chaosIntensity);
        
        //projector1.messUpImage(q0,(1.0f-per)*alpha); 
        //projector2.messUpImage(q0,per*(1-alpha));

		//chaosIntensity = 1.0f;
		projector1.setAlpha(alpha * chaosIntensity);  // TODO alpha1
		projector2.setAlpha((1-alpha) * chaosIntensity); 
		//projector1.setAlpha(1.0f);
		//projector2.setAlpha(1.0f);

		projector1.repaint();
		projector2.repaint();

		time++;	

	}
	
	private double sine(double amp, double freq) {
		return ampSine * amp * Math.sin(2.0 * Math.PI * freq * time);
	}
	
	/**
	 * Called from the audio thread every time a breathing detection occurs.
	 * @param force b/w 0 and 1
	 */
	public void setChaosIntensity(double intensity) {
		
		// if sudden increase:
		if (intensity > chaosIntensity) {
			
			if (intensity > 0.5) pickNewSentencePair();
			
		}
		
		this.chaosIntensity = intensity;
		
		// TODO : do something !
		
	}
	
	private void pickNewSentencePair() {
		
		meanAlpha = 0.5;
		ampSine = 1.0;
		deltaMeanAlpha = (Math.random() > 0.5 ? DELTA_MEAN_ALPHA : -DELTA_MEAN_ALPHA);
		String[] ss = sentencesFileReader.fetchNewPair();
		projector1.setSentence(ss[0]);
		projector2.setSentence(ss[1]);
		ldpcDec.initState();
		projector1.messUpDisplay(ldpcDec.q0);  // full copy of q0
	    projector2.messUpDisplay(ldpcDec.q0);
		
		LOGGER.info("NEW SENTENCE PAIR:" + ss[0] + " & " + ss[1]);
	}
	
	// --------------------------------- test ---------------------------------

	public static void main(String[] args) throws Exception {

		SentencesAnimator sa = new SentencesAnimator(null);
		
		Thread t = new Thread(() -> {
			while(true) {
			sa.setChaosIntensity(0.8); // Math.random()); 
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
