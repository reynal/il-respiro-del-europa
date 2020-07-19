package audio;

import java.io.*;
import java.util.*;

import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.TargetDataLine;

/**
 * 
 * @author sydxrey
 *
 * Audio input ports are sources; audio output ports are targets.
 * 
 */
public class Microphone extends Thread {

	public static final int BUF_LEN_SAMPLES = 8192;
	public static final int BUF_LEN = AudioHub.SAMPLE_SIZE_BYTE * BUF_LEN_SAMPLES; // BYTES
	byte[] buffer = new byte[BUF_LEN];
	DataInputStream dis;
	DataOutputStream dos;

	public Microphone() throws FileNotFoundException {
		dis = new DataInputStream(new ByteArrayInputStream(buffer));
		dos = new DataOutputStream(new BufferedOutputStream(new FileOutputStream("sound_output.bin")));
	}

	public void run() {

		TargetDataLine line;
		try {
			line = AudioHub.obtainInputLine("Unknown USB Audio Device");
			line.addLineListener(e -> System.out.println(e));
			line.open();
		} catch (LineUnavailableException e1) {
			e1.printStackTrace();
			return;
		}

		line.start();

		int n;
		int ii=0;
		while((n = line.read(buffer, 0, BUF_LEN)) != -1) {
			//System.out.println(level_dB());
			//dis.reset(); for (int i=0; i<n; i++) dos.writeByte(dis.readByte());
			//if (ii++ > 100) break;
			try {
				fft();
			} catch (IOException e) {
				e.printStackTrace();
			}
			break;
		}

		line.stop();
		line.close();
		try {
			dos.flush();
			dos.close();
		} catch (IOException e) {
			e.printStackTrace();
		} 
	}

	public void fft() throws IOException {
		Complex[] x = new Complex[BUF_LEN_SAMPLES];
		dis.reset();
		for (int i = 0; i < BUF_LEN_SAMPLES; i++) {
			x[i] = new Complex(readData(), 0);
		}
		Complex[] y = FFT.fft(x);
		//save to file:
		for (int i=0; i<BUF_LEN_SAMPLES; i++) dos.writeDouble(y[i].abs());
	}

	public double readData() throws IOException {
		switch (AudioHub.SAMPLE_SIZE_BYTE) { 
		case 1 : return dis.readByte()/128.0; 
		case 2 : return dis.readShort()/32768.0; 
		default: return 0.0;
		}
	}

	public double level_dB() throws IOException {
		double sum=0;
		double s;
		dis.reset();
		for (int i=0; i<BUF_LEN_SAMPLES; i++) {
			s = readData();
			sum += s * s;
		}
		sum /= BUF_LEN_SAMPLES;
		return 10 * Math.log(sum);

	}


	public static void main(String[] args) throws Exception {

		Microphone m = new Microphone();
		m.start();


	}

}
