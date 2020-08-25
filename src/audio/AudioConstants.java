package audio;

public class AudioConstants {

	public static final int SAMPLE_FREQ = 16000;
	public static final int FFT_SIZE = 2048; // may be different from BUF_LEN
	public static final int SAMPLE_SIZE_BYTE = 1;
	public static final int CHANNELS = 1;
	public static final boolean IS_BIG_ENDIAN = true;
	public static final boolean IS_SIGNED = true;
	
	public static final int BUF_LEN_SAMPLES = 1*2048; // 1/8 seconde @ 16kHz
	public static final int BUF_LEN = SAMPLE_SIZE_BYTE * BUF_LEN_SAMPLES; // BYTES
	
	public static final String WAVE_FILE_DBG = "wave.bin";
	public static final String FFT_FILE_DBG = "fft.bin";
	
	
}
