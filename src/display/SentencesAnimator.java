package display;

import java.awt.Dimension;
import java.awt.GraphicsEnvironment;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.logging.Logger;

import javax.swing.Timer;

import application.Preferences;
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
	private int timerPeriodMs = 50; // ms
	private int decoderIterationPeriod = 10; // = 10 * TIMER_PERIOD_MS = 100ms
	
	private Projector projector1, projector2; // 1=a l'entree, 2=au fond
	
	private LdpcDecoder ldpcDec;
	private float per;
	
	private double chaosIntensity; // set by audio thread depending on breath detection
	private double chaosIntensityNewSentencesThresholdHIGH = 0.9; // with hysteresis
	private double chaosIntensityNewSentencesThresholdLOW = 0.1;
	private boolean isCanPickNewSentence = true; // pour gerer l'hysteresis
	private boolean pickNewSentences = true; // to avoid race conditions
	
	private double meanAlpha = 0.5;
	private double deltaMeanAlpha;
	private double ampSine; // modulation of alpha, aka sin(wt)
	public static final double DELTA_MEAN_ALPHA = 0.001; // rate of increase of <alpha> over time (large => the final sentence gets quickly selected)
	
	double alphaMax1 = 1.0;
	double alphaMax2 = 1.0;
	/**
	 * @throws IOException if sentence file can't be loaded 
	 */
	public SentencesAnimator(UserInterface ui) throws IOException {
		
		this.ui = ui;
		if (ui!= null) ui.setSentencesAnimator(this);
		
		// loading from properties.txt:
		chaosIntensityNewSentencesThresholdHIGH = Preferences.getPreferences().getDoubleProperty(Preferences.Key.CHAOS_NEW_SENTENCES_THR_HIGH);
		chaosIntensityNewSentencesThresholdLOW = Preferences.getPreferences().getDoubleProperty(Preferences.Key.CHAOS_NEW_SENTENCES_THR_LOW);
		timerPeriodMs = Preferences.getPreferences().getIntProperty(Preferences.Key.TIMER_PERIOD_MS); 
		decoderIterationPeriod = Preferences.getPreferences().getIntProperty(Preferences.Key.DECODER_ITERATION_PERIOD);
		alphaMax1 = Preferences.getPreferences().getDoubleProperty(Preferences.Key.ALPHA_MAX1);
		alphaMax2 = Preferences.getPreferences().getDoubleProperty(Preferences.Key.ALPHA_MAX2);
		
		
		this.time = 0;
		lastModTime = 0;
		sentencesFileReader = new SentencesFileReader();
		
		projector1 = new Projector(0);
		if (GraphicsEnvironment.getLocalGraphicsEnvironment().getScreenDevices().length > 1)
			projector2 = new Projector(1);
		else {
			projector2 = new Projector(0);
			Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
			projector2.setLocation(0, dim.height/2);
		}
		projector1.setNoiseAlphaMax(alphaMax1);
		projector2.setNoiseAlphaMax(alphaMax2);
		
		ldpcDec = new LdpcDecoder(); 

		timer = new Timer(timerPeriodMs, this);
		
		start();

	}
	
	public void start() {
		timer.start();
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		
		//System.out.println(chaosIntensity);
		
		// following code was in setChaosIntensity and could be called asynchronously
		if (pickNewSentences) {
			pickNewSentencePair();
			ldpcDec.initState();
			projector1.messUpDisplay(ldpcDec.q0);  // full copy of q0
		    projector2.messUpDisplay(ldpcDec.q0);
			pickNewSentences = false;
			time = 0;
		}
		else {
			int curModTime = (time % decoderIterationPeriod);
			if (curModTime == 0) {
				ldpcDec.nextIteration();
				//per = (float)ldpcDec.getPER(); // not used right now		    
				//System.out.println("per = "+per);
			}
			// partial copy of q0
			projector1.messUpDisplay(ldpcDec.q0,lastModTime,curModTime,decoderIterationPeriod);  
			projector2.messUpDisplay(ldpcDec.q0,lastModTime,curModTime,decoderIterationPeriod);	
		    lastModTime = curModTime;
			
		}
		
		
		// make alpha evolve towards one sentence only with sine modulation waning off:		
		meanAlpha += deltaMeanAlpha;
		meanAlpha = Math.max(meanAlpha, 0);
		meanAlpha = Math.min(meanAlpha, 1);
		//ampSine *= 0.99;
		ampSine = chaosIntensity;
		//System.out.println("<alpha>= "+meanAlpha + ", ampSine=" + ampSine);

		double a1=0.35;
		double a2=0.25;
		double a3=0.15;
		double T1 = 3.5; // secondes
		double T2 = 1.75;
		double T3 = 1.05;
		double f1 = 0.001 * timerPeriodMs / T1;
		double f2 = 0.001 * timerPeriodMs / T2;
		double f3 = 0.001 * timerPeriodMs / T3;
		
		float alpha = (float)(meanAlpha + sine(a1, f1) + sine(a2, f2) + sine(a3, f3) + per * (Math.random()-0.5));
		alpha = Math.max(alpha, 0);
		alpha = Math.min(alpha, 1);
		//alpha = 0.5f; // debug
		
		//System.out.println("alpha= "+alpha);
		//System.out.println("chaosIntensity= "+chaosIntensity);
        
        //projector1.messUpImage(q0,(1.0f-per)*alpha); 
        //projector2.messUpImage(q0,per*(1-alpha));

		//chaosIntensity = 1.0f;
		projector1.setAlpha(alphaMax1 * alpha * chaosIntensity);  // TODO debug
		projector2.setAlpha(alphaMax2 * (1-alpha) * chaosIntensity); 
		//projector1.setAlpha(0.0f);
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
		
		// si chaos > HIGH et que precedemment on est a un moment passe en dessous de LOW, c'est ok !
		// sinon il faut attendre de passer en dessous de LOW pour remettre le "flag de permission" a true:
		if (isCanPickNewSentence  && intensity > this.chaosIntensityNewSentencesThresholdHIGH) {
			pickNewSentences = true; //pickNewSentencePair();
			isCanPickNewSentence = false; // hysteresis mgmt
		}
		
		if (intensity < this.chaosIntensityNewSentencesThresholdLOW) {
			isCanPickNewSentence = true;
		}
		
		this.chaosIntensity = intensity;
				
	}
	
	private void pickNewSentencePair() {
		
		meanAlpha = 0.5;
		ampSine = 1.0;
		deltaMeanAlpha = (Math.random() > 0.5 ? DELTA_MEAN_ALPHA : -DELTA_MEAN_ALPHA);
		//deltaMeanAlpha = DELTA_MEAN_ALPHA; // TODO DBG
		try {
			sentencesFileReader = new SentencesFileReader();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		String[] ss = sentencesFileReader.fetchNewPair();
		projector1.setSentence(ss[0]);
		projector2.setSentence(ss[1]);
	    if (this.ui != null) {
	    	ui.setSentence1(ss[0]);
	    	ui.setSentence2(ss[1]);
	    }
		
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
