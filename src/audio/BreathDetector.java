package audio;

import static application.Main.out;
import static audio.AudioConstants.BUF_LEN_SAMPLES;
import static audio.AudioConstants.SAMPLE_FREQ;

import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.logging.Logger;

import javax.sound.sampled.TargetDataLine;

/**
 * A discriminating algorithm between a breath audio signal and a simple conversation of visitors in front of the microphone.
 * 
 * @author sydxrey
 *
 */
public class BreathDetector {

	private static final Logger LOGGER = Logger.getLogger("confLogger");
	
	public static final double FREQ_MIN = 100; // filter out frequencies below this value (in Hz) 
	public static final double SILENCE_THRESHOLD_DB = -55; // anything below is considered silence
	public static final double HARMONIC_RATIO_THRESHOLD = 15.0; // 

	
	// --------------
	
	private AudioSignal signal;
	
	private Complex[] x = new Complex[BUF_LEN_SAMPLES]; // temp buff for FFT computation
	
	private double[] fftWindow = new double[BUF_LEN_SAMPLES]; // Hann window (or sth)
	private double[] fftAbs = new double[BUF_LEN_SAMPLES/2]; // array of |Y(nu)| from nu=0 to nu=0.5
	private double fftAbsMaxValue; // max of |Y(nu)|
	private double fftAbsMean; // mean of |Y(nu)| over whole spectrum
	private double fftHarmonicRatio; // ratio of max to mean

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

		freqMinIdx = (int)(FREQ_MIN * BUF_LEN_SAMPLES / SAMPLE_FREQ);
		LOGGER.info("freqMinIdx="+freqMinIdx);

	}

	/**
	 * @return the intensity of breathing in the current signal, or 0.0 if no breathing detected
	 */
	public double measureBreath() { 
		
		if (signal.level_dB() < SILENCE_THRESHOLD_DB) {
			out("BreathDectector: signal below threshold");
			return 0.0;
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
		for (int i=freqMinIdx; i<BUF_LEN_SAMPLES/2; i++) { 
			double u = y[i].abs();
			fftAbs[i] = u;
			fftAbsMean += u; 
			if (u > fftAbsMaxValue) {
				fftAbsMaxValue = u;
			}
		}
		fftAbsMean /= (BUF_LEN_SAMPLES/2);
		
		out("BreathDectector: FFT mean=" + fftAbsMean);
		out("BreathDectector: FFT max=" + fftAbsMaxValue);
		out("BreathDectector: FFT harmonic ratio=" + (fftAbsMaxValue/fftAbsMean));
		
		if (fftAbsMaxValue / fftAbsMean > HARMONIC_RATIO_THRESHOLD) return 0.0;
		else return fftAbsMaxValue / fftAbsMean;
				
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
