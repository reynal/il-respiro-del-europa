package ldpc;

//package ldpc;
import java.util.*;
import java.math.*;
import java.io.*;
import javax.swing.*;

import display.SentencesAnimator;

/**
 * LDPC decoder class Skeleton of class taken from
 * https://github.com/chsasank/LDPC-Codes Author: Sasank Chilamkurthy
 * Implementation taken from
 * http://read.pudn.com/downloads40/sourcecode/windows/comm/141638/SumProductDecoder.java__.htm//
 * SumProductDecodere.java // // log-domain sum-product decoding algorithm // //
 * (C) Tadashi Wadayama, 2002
 **/

public class LdpcDecoder {

	public int tmp_bit[]; // tentative decision word
	// public double posterior[]; // log likelihood ratio

	private double alpha[][];
	private double beta[][];
	private double damping;

	public int n; // code length
	public int m; // number of parity bits
	public int rmax; // maximum number of row weight
	public int cmax; // maximum number of column weight
	public int row_weight[]; // weight of each column
	public int col_weight[]; // weight of each row
	public int row_list[][];
	public int col_list_r[][];
	public int col_list_c[][];

	double lambda[];

	/**
	 * Initializes decoder with parity check matrix read from a specially formated
	 * file. Files should be in format specified here:
	 * http://www.inference.phy.cam.ac.uk/mackay/codes/alist.html chaos: code
	 * adapted to match alist format
	 * 
	 */
	public LdpcDecoder() throws IOException {

		damping = 1.0;
		String filename = "rate0.50_irreg_dvbs2_N64800.alist";

		BufferedReader file = new BufferedReader(new FileReader("resource/" + filename));

		String line = file.readLine();
		StringTokenizer tokenizer = new StringTokenizer(line);
		// n = number of bits (columns)
		// m = number of checks (rows)
		n = Integer.parseInt(tokenizer.nextToken());
		m = Integer.parseInt(tokenizer.nextToken());
		// System.out.println("n = " + n);
		// System.out.println("m = " + m);

		line = file.readLine();
		tokenizer = new StringTokenizer(line);
		// cmax = max col weight = max left degree (bit nodes)
		cmax = Integer.parseInt(tokenizer.nextToken());
		// rmax = max row weight = max right (check) degree
		rmax = Integer.parseInt(tokenizer.nextToken());

		col_weight = new int[n];
		line = file.readLine();
		// System.out.println(input);
		tokenizer = new StringTokenizer(line);
		for (int i = 0; i <= n - 1; i++) {
			col_weight[i] = Integer.parseInt(tokenizer.nextToken());
		}

		row_weight = new int[m];
		line = file.readLine();
		tokenizer = new StringTokenizer(line);
		for (int i = 0; i <= m - 1; i++)
			row_weight[i] = Integer.parseInt(tokenizer.nextToken());

		int v;
		int counter[] = new int[n];
		for (int i = 0; i <= n - 1; i++)
			counter[i] = 0;
		row_list = new int[m][rmax];
		col_list_r = new int[n][cmax];
		col_list_c = new int[n][cmax];

		// chaos: alist format has left connections first, skip them
		for (int i = 0; i < n; i++)
			line = file.readLine();

		for (int j = 0; j <= m - 1; j++) {
			line = file.readLine();
			tokenizer = new StringTokenizer(line);
			for (int i = 0; i <= row_weight[j] - 1; i++) {
				v = Integer.parseInt(tokenizer.nextToken()) - 1;
				row_list[j][i] = v;
				col_list_r[v][counter[v]] = j;
				col_list_c[v][counter[v]] = i;
				counter[v]++;
			}
		}
		file.close();
	}

	/**
	 * (re)init the decoder state with a new BSC noise figure. Should be followed by
	 * calls to nextIteration()
	 */
	public void initState() {

		double bsc_p = 0.09; // TODO : doit dependre du souffle !?
		this.lambda = generateBSCnoise(64800, bsc_p); // 204; 204.33.486.txt
		// lambda: log likelihood ratio

		alpha = new double[m][rmax];
		beta = new double[m][rmax];
		tmp_bit = new int[n];
		// initialization of beta
		for (int i = 0; i <= m - 1; i++) {
			for (int j = 0; j <= row_weight[i] - 1; j++) {
				beta[i][j] = 0.0;
			}
		}

	}
	
	public void injectSomeMoreNoise(double intensity) {
		
		
		
	}

	/** Runs one decoder iteration and return the beliefs - die können benutzt werden, um Noise im BufferImage zu fabrizieren */
	public double[] nextIteration() {

		double[] posterior = new double[n];

		// row operation
		for (int i = 0; i <= m - 1; i++) {
			double sum = 0.0;
			int prod = 1;
			for (int k = 0; k <= row_weight[i] - 1; k++) {
				double tmp = lambda[row_list[i][k]] + beta[i][k];
				sum = sum + gallager_f(Math.abs(tmp));
				prod = prod * sign(tmp);
			}
			for (int j = 0; j <= row_weight[i] - 1; j++) {
				double tmp = lambda[row_list[i][j]] + beta[i][j];
				alpha[i][j] = damping * prod * sign(tmp) * gallager_f(sum - gallager_f(Math.abs(tmp)));
			}
		}

		// column operation
		double sum = 0.0;
		double[] q0 = new double[n];

		for (int i = 0; i <= n - 1; i++) {
			sum = 0.0;
			for (int k = 0; k <= col_weight[i] - 1; k++)
				sum += alpha[col_list_r[i][k]][col_list_c[i][k]];
			for (int j = 0; j <= col_weight[i] - 1; j++) {
				beta[col_list_r[i][j]][col_list_c[i][j]] = sum - alpha[col_list_r[i][j]][col_list_c[i][j]];
			}
			posterior[i] = lambda[i] + sum;
			q0[i] = Math.exp(posterior[i]) / (1 + Math.exp(posterior[i])); // beliefs
			if (posterior[i] > 0)
				tmp_bit[i] = 0;
			else
				tmp_bit[i] = 1;
		}

		// SR pending: if (ldpc != null) ldpc.updateImages(q0);

		return q0;

	}

	//
	double getPER(double[] q0) {
		
		// CLAUDIO !!!
		return Double.NaN;
	}

	/**
	 * Generate binary symmetric noise
	 * 
	 * @param n: vector length
	 * @param p: error probability
	 * @return: noise vector
	 */
	private double[] generateBSCnoise(int n, double p) {

		double ret[] = new double[n];
		double llr = Math.log((1 - p) / p);
		for (int i = 0; i < n; i++)
			ret[i] = (Math.random() < p ? -llr : llr);
		return ret;
	}

	// sign function
	public int sign(double x) {
		if (x >= 0.0)
			return 1;
		else
			return -1;
	}

	// Gallager's f function
	public double gallager_f(double x) {
		double y;
		double a = Math.exp(x);
		if (x < 0.00001)
			y = 12.21;
		// 12.21 = gallager_f(0.00001)
		else
			y = Math.log((a + 1.0) / (a - 1.0));
		return y;
	}

	// --------------------------
	public static void main(String[] args) throws IOException {
		LdpcDecoder decoder = new LdpcDecoder();
		decoder.initState();
	}

}
