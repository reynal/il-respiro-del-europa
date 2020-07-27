package audio;

import static audio.AudioConstants.*;

import java.io.*;
import javax.sound.sampled.*;


public class AudioSignal {
	
	private byte[] samplesBuffer = new byte[BUF_LEN]; // backing buffer
	private DataOutputStream dos;

	
	public AudioSignal(String outputFile) throws FileNotFoundException {
		dos = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(outputFile)));
	}
	
	public AudioSignal() throws FileNotFoundException  {
		this("wave.bin");
	}
	
	public int acquire(TargetDataLine line) {
		int n = line.read(samplesBuffer, 0, BUF_LEN);
		if (n>0) writeToFile(); 
		return n;
	}
	
	public byte[] getBuffer() {
		return samplesBuffer;
	}
	
	public double getSample(int i) {
		return samplesBuffer[i] / 128.0;
	}
	
	public double level_dB() {
		double sum = 0;
		double s;
		for (int i = 0; i < BUF_LEN_SAMPLES; i++) {
			sum += getSample(i) * getSample(i);
		}
		sum /= BUF_LEN_SAMPLES;
		return 10 * Math.log(sum);
	}

	public void writeToFile(){
		if (dos==null) return;
		try {
			for (int i=0; i<BUF_LEN; i++) {
				dos.writeDouble(getSample(i));
			}
		} catch (IOException e) {
			e.printStackTrace();
		} 
	}
	
	public void closeOutputStream() {
		try {
			dos.flush();
			dos.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
