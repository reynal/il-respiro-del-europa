package audio;

public class AudioConstants {

	public static final int SAMPLE_FREQ = 16000;
	public static final int SAMPLE_SIZE_BYTE = 1;
	public static final int CHANNELS = 1;
	public static final boolean IS_BIG_ENDIAN = true;
	public static final boolean IS_SIGNED = true;
	
	public static final int BUF_LEN_SAMPLES = 4*8192;
	public static final int BUF_LEN = SAMPLE_SIZE_BYTE * BUF_LEN_SAMPLES; // BYTES
}
