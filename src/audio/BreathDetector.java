package audio;

import static audio.AudioConstants.BUF_LEN_SAMPLES;
import static audio.AudioConstants.SAMPLE_FREQ;

import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Usage:
 * @author sydxrey
 *
 */
public class BreathDetector {

	AudioSignal signal;
	double[] fftWindow = new double[BUF_LEN_SAMPLES];
	double[] fftAbs = new double[BUF_LEN_SAMPLES/2]; // array of |Y(nu)| from nu=0 to nu=0.5
	double fftAbsMaxValue;
	double fftAbsMean;

	final int freqMinIdx;
	static final double FREQ_MIN = 100; // filter out frequencies below this value (in Hz) 

	DataOutputStream dosFFT;


	BreathDetector(AudioSignal signal){

		this.signal = signal;

		// init FFT window:
		for (int i=0; i<BUF_LEN_SAMPLES; i++) {
			double x = Math.sin(Math.PI * i / (BUF_LEN_SAMPLES-1));			
			fftWindow[i] = x * x;
		}

		freqMinIdx = (int)(FREQ_MIN * BUF_LEN_SAMPLES / SAMPLE_FREQ);

		try {
			dosFFT = new DataOutputStream(new BufferedOutputStream(new FileOutputStream("fft.bin")));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}

	public void closeOutputStream() {
		if (dosFFT == null) return;
		try {
			dosFFT.flush();
			dosFFT.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}


	public void fft() {
		Complex[] x = new Complex[BUF_LEN_SAMPLES];
		for (int i = 0; i < BUF_LEN_SAMPLES; i++) {
			x[i] = new Complex(signal.getSample(i) * fftWindow[i], 0);
		}
		Complex[] y = FFT.fft(x);

		// remove DC and frequencies below 50Hz in calculations below:



		fftAbsMaxValue=0;
		fftAbsMean=0;
		for (int i=0; i<BUF_LEN_SAMPLES/2; i++) { 
			double u = y[i].abs();
			fftAbs[i] = u;
			fftAbsMean += u; 
			if (u > fftAbsMaxValue) {
				fftAbsMaxValue = u;
			}
		}
		fftAbsMean /= (BUF_LEN_SAMPLES/2);
		System.out.println("FFT mean=" + fftAbsMean);
		System.out.println("FFT max=" + fftAbsMaxValue);
		System.out.println("FFT harmonic ratio=" + (fftAbsMaxValue/fftAbsMean));
		//save to file:
		try {
			for (int i=0; i<BUF_LEN_SAMPLES/2; i++) { 
				dosFFT.writeDouble(fftAbs[i]);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}	
}
