package audio;

import static application.Main.out;
import static audio.AudioConstants.BUF_LEN_SAMPLES;
import static audio.AudioConstants.SAMPLE_FREQ;
import static audio.AudioConstants.FFT_SIZE;
//import static audio.AudioConstants.I200HZ;
//import static audio.AudioConstants.I500HZ;
//import static audio.AudioConstants.I1000HZ;
//import static audio.AudioConstants.I2300HZ;
//import static audio.AudioConstants.I5000HZ;

import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.logging.Logger;
import java.util.Arrays;

import javax.sound.sampled.TargetDataLine;

/**
 * A discriminating algorithm between a breath audio signal and a simple conversation of visitors in front of the microphone.
 * 
 * @author sydxrey
 * 
 * TODO (pour claudio) :
 * -
 *
 */
public class BreathDetector {

	private static final Logger LOGGER = Logger.getLogger("confLogger");
	
	// FREQ_MIN currently ignored
	public static final double FREQ_MIN = 100; // filter out frequencies below this value (in Hz) 
	public static final double SILENCE_THRESHOLD_DB = -75; // -55 anything below is considered silence
	public static final double HARMONIC_RATIO_THRESHOLD = 15.0; // 

	public static final int I200HZ = 200*FFT_SIZE/SAMPLE_FREQ;
	public static final int I500HZ = 500*FFT_SIZE/SAMPLE_FREQ;
	public static final int I1000HZ = 1000*FFT_SIZE/SAMPLE_FREQ;
	public static final int I2300HZ = 2300*FFT_SIZE/SAMPLE_FREQ;
	public static final int I5000HZ = 5000*FFT_SIZE/SAMPLE_FREQ;

	
	// --------------
	
	private AudioSignal signal;
	
	private Complex[] x = new Complex[BUF_LEN_SAMPLES]; // temp buff for FFT computation
	
	private double[] fftWindow = new double[BUF_LEN_SAMPLES]; // Hann window (or sth)
	private double[] fftAbs = new double[BUF_LEN_SAMPLES/2]; // array of |Y(nu)| from nu=0 to nu=0.5
	private double fftAbsMaxValue; // max of |Y(nu)|
	private double fftAbsMean; // mean of |Y(nu)| over whole spectrum
	private double fftHarmonicRatio; // ratio of max to mean
	private double[] feature = new double[10];
	private double fAbsMax;
	private double fCentroid;
	
	private final int freqMinIdx; // anything freq below is zeroed

	/**
	 * Creates a breath detector for the given signal
	 */
	public BreathDetector(AudioSignal signal){

		this.signal = signal;

		// init FFT window:
		for (int i=0; i<BUF_LEN_SAMPLES; i++) {
			double x = Math.sin(Math.PI * i / (BUF_LEN_SAMPLES-1));			
			fftWindow[i] = x * x;
		}

		freqMinIdx = 1 ;//(int)(FREQ_MIN * BUF_LEN_SAMPLES / SAMPLE_FREQ);
		LOGGER.info("freqMinIdx="+freqMinIdx);

	}

	/**
	 * @return true if the current signal resembles a breath
	 * 
	 * TODO : la je regarde juste le ratio entre le peak du maximum et la moyenne. Il faudrait regarder la repartition des peaks (entropie),
	 * et aussi comme tu le suggeres Claudio, la forme en 1/f de la DSP.
	 */
	public boolean isBreath() { 
		
		if (signal.level_dB() < SILENCE_THRESHOLD_DB) {
			//out("BreathDectector: signal below threshold");
			return false;
		}

		// reinit FFT temp buffer:
		for (int i = 0; i < BUF_LEN_SAMPLES; i++) {
			x[i] = new Complex(signal.getSample(i) * fftWindow[i], 0);
		}
		Complex[] y = FFT.fft(x);
		
		// remove DC and frequencies below "FREQ_MIN" in calculations below:
		for (int i=0; i<freqMinIdx; i++) fftAbs[i] = 0;

		// compute spectrum mean and peak:
		fftAbsMaxValue=0;
		fftAbsMean=0;
		fAbsMax = 0;
		for (int i=freqMinIdx; i<BUF_LEN_SAMPLES/2; i++) { 
			double u = y[i].abs();
			fftAbs[i] = u;
			fftAbsMean += u; 
			if (u > fftAbsMaxValue) {
				fftAbsMaxValue = u;
				fAbsMax = i;
			}
		}
		fAbsMax *= SAMPLE_FREQ/FFT_SIZE;
		fftAbsMean /= (BUF_LEN_SAMPLES/2);
		
		fftAbsMaxValue *= 0.9; // need it only for comparison
		fCentroid = 0;
		double fCdenom = 0;
		for (int i=freqMinIdx; i<BUF_LEN_SAMPLES/2; i++) { 
			if (fftAbs[i] > fftAbsMaxValue) {
				fCentroid += i*fftAbs[i];
				fCdenom += fftAbs[i]; 				
			}
		}
		fCentroid /= fCdenom;
		fCentroid *= SAMPLE_FREQ/FFT_SIZE;

		// compute features as per NEWCAS 2013 paper
		double band1 = 0.0;
		for (int i = I200HZ; i < I500HZ; i++) band1 += fftAbs[i];
		double band2 = 0.0;
		for (int i = I500HZ; i < I1000HZ; i++) band2 += fftAbs[i];
		double band3 = 0.0;
		for (int i = I1000HZ; i < I2300HZ; i++) band3 += fftAbs[i];
		double band4 = 0.0;
		for (int i = I2300HZ; i < I5000HZ; i++) band4 += fftAbs[i];

		Complex[] acf = FFT.cacf_fft(y);
		//Complex[] acf = FFT.convolve(x,x);
		//testing: save acf to fft_abs
//		for (int i=0; i<BUF_LEN_SAMPLES/2; i++) { 
//			fftAbs[i] = acf[i].re();
//			//if (acf[i].im()*acf[i].im() > 0.0001) {
//			//	out("imaginary part in acf");
//			//}
//		}
		
		feature[0] = fftAbsMaxValue / fftAbsMean; // harmonic ratio, Syd
		feature[1] = Math.log(band1/band2);
		feature[2] = Math.log(band1/band3);
		feature[3] = Math.log(band1/band4);
		feature[4] = Math.log(band2/band3);
		feature[5] = Math.log(band2/band4);
		feature[6] = Math.log(band3/band4);
		feature[7] = fCentroid;
		feature[8] = fAbsMax;
		feature[9] = rel_max_acf_peak(acf);
		
		//out(Arrays.toString(feature));
		//out("BreathDectector: FFT mean=" + fftAbsMean);
		//out("BreathDectector: FFT max=" + fftAbsMaxValue);
		//out("BreathDectector: FFT harmonic ratio=" + (fftAbsMaxValue/fftAbsMean));

		double score = 0.0;
		if (feature[0]>50) // feature 0 > 45 is voice
			score -= 1.0;
		if (feature[2]<-2.0) // clutter
			score -= 1.0;
		if (feature[3]<-2.0) // clutter
			score -= 0.8;
		if (feature[6]<-1.5) // clutter
			score -= 0.8;
		if ((feature[8]<1000) & (feature[9]>0.38)) // voice
			score -= 1.0;
		if ((feature[7]>1000) & (feature[7]<3000) & (feature[8]>1000) & (feature[8]<3000) )
			score += 1.0; // breath
		if (feature[9]<0.38)
			score += 0.2; // breath
		if (feature[9]<0.25)
			score += 0.9; // breath
		
		return (score>0.0);
//		if (fftAbsMaxValue / fftAbsMean > HARMONIC_RATIO_THRESHOLD) return false;
//		else return true;
				
	}
	/**
	 * Seeks max peak in beginning of acf and divides by acf[0]
	 * @param acf: autocorr fct (stored in real part)
	 * @return peak/acf[0]
	 */
	private double rel_max_acf_peak(Complex [] acf) {
		int m = 5; // peak within +- m points
		double mp = 1e-6;
		double lmax = acf[0].re();
		double lmin = acf[0].re();
		int imin = 0;
		int imax = 0;
		for (int i=1; i<SAMPLE_FREQ/80; i++) { //down to 80 Hz or so 
		    if (acf[i].re() > lmax) {
		      lmax = acf[i].re();
		      imax = i;
		    } else if (acf[i].re() < lmin) {
		      lmin = acf[i].re();
		      imin = i;
		    }
		    if (i-imin == m) { // seek next max now
		      lmax = acf[i].re();
		      imax = i;
		    } else if ((imax-imin>=m) && (i-imax == m)) { // peak found
		    	if (lmax>mp)
		    		mp = lmax;		     
		
		    	lmin = acf[i].re(); // seek next min
		    	imin = i;
		    }   
		}
		return mp/acf[0].re();
	}
	
	/**
	 * Save the buffer containing the FFT to the given stream as an array of doubles.
	 * 
	 * Loading file in matlab: 
	 * 			fid = fopen('fft.bin','r');
	 *			X=fread(fid, size_in_bytes, 'double', 'ieee-be'); 
	 *			fclose(fid);
	 * 
	 * @param dos a valid data output stream (ex : new DataOutputStream(new BufferedOutputStream(new FileOutputStream("fft.bin"))))
	 */
	public void saveToFile(DataOutputStream dos) {
		
		//save to file:
		try {
			for (int i=0; i<BUF_LEN_SAMPLES/2; i++) { 
				dos.writeDouble(fftAbs[i]);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}		
}
