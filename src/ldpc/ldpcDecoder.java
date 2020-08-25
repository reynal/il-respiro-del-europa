package ldpc;

//package ldpc;
import java.util.*;
import java.math.*;
import java.io.*;
import javax.swing.*;
/**
 * LDPC decoder class
 * Skeleton of class taken from https://github.com/chsasank/LDPC-Codes 
 * Author: Sasank Chilamkurthy
 * Implementation taken from http://read.pudn.com/downloads40/sourcecode/windows/comm/141638/SumProductDecoder.java__.htm// SumProductDecodere.java  
//  
// log-domain sum-product decoding algorithm  
//  
// (C) Tadashi Wadayama, 2002  
**/

public class ldpcDecoder {
  public int tmp_bit[];     // tentative decision word  
  public double posterior[];    // log likelihood ratio  
  
  private double alpha[][];  
  private double beta[][];  
  private double damping;
    
  public int n;         // code length  
  public int m;         // number of parity bits  
  public int rmax;      // maximum number of row weight  
  public int cmax;      // maximum number of column weight  
  public int row_weight[];  // weight of each column  
  public int col_weight[];  // weight of each row  
  public int row_list[][];  
  public int col_list_r[][];  
  public int col_list_c[][];  


    
  /**
  * Initializes decoder with parity check matrix read from a specially formated file.
  * Files should be in format specified here: http://www.inference.phy.cam.ac.uk/mackay/codes/alist.html
  * chaos: code adapted to match alist format
  * @param filename, damping factor for the messages (1.0 is standard) 
  * @throws IOException
  */
  public ldpcDecoder(String filename, double _damping) throws IOException{
    damping = _damping;  
    try (BufferedReader in = new BufferedReader(new FileReader(filename)))
    {
        String input = in.readLine();  
        StringTokenizer t = new StringTokenizer(input);  
        // n = number of bits (columns)
        // m = number of checks (rows)
        n = Integer.parseInt(t.nextToken());  
        m = Integer.parseInt(t.nextToken());  
        //System.out.println("n = " + n);  
        //System.out.println("m = " + m);  
  
        input = in.readLine();  
        t = new StringTokenizer(input);
        // cmax = max col weight = max left degree (bit nodes)
        cmax = Integer.parseInt(t.nextToken());  
        // rmax = max row weight = max right (check) degree
        rmax = Integer.parseInt(t.nextToken());  
        
        col_weight = new int[n];  
        input = in.readLine(); 
        //System.out.println(input);
        t = new StringTokenizer(input);  
        for (int i = 0; i <= n-1; i++) {  
            col_weight[i] = Integer.parseInt(t.nextToken());  
        }  
        
        row_weight = new int[m];  
        input = in.readLine();  
        t = new StringTokenizer(input);  
        for (int i = 0; i <= m-1; i++) {  
            row_weight[i] = Integer.parseInt(t.nextToken());  
        }  

       
        int v;  
        int counter[] = new int[n];  
        for (int i=0; i <= n-1; i++) counter[i] = 0;  
        row_list = new int[m][rmax];  
        col_list_r = new int[n][cmax];  
        col_list_c = new int[n][cmax];  
  
        // chaos: alist format has left connections first, skip them
        for (int i=0; i<n; i++) input = in.readLine();
        
        for (int j = 0; j <= m-1; j++) {  
            input = in.readLine();  
            t = new StringTokenizer(input);  
            for (int i = 0; i <= row_weight[j]-1; i++) {  
                v = Integer.parseInt(t.nextToken())-1;  
                row_list[j][i] = v;  
                col_list_r[v][counter[v]] = j;  
                col_list_c[v][counter[v]] = i;  
                counter[v]++;  
            }                  
        }
    }
    catch (IOException e) {
        e.printStackTrace();
    }
            
  }

  /**
   * Runs the decoder iterations (up to loop_max iterations, stops early if parity check is satisfied
   * Calls LDPC.updateImages() at each iteration with the new posterior bit=0 probabilities 
   * TO FIX: hardcoded delay (should go into updateImages, resp. multithread solution)
   * @param lambda: array of channel LLR inputs (noise) 
   * @param loop_max: max number of iterations
   * @return decoded word (TO FIX: remove for efficiency)
   */
  public int[] Decode(double lambda[], int loop_max) { //, JWindow w1, GraphicsPanel p1, JWindow w2, GraphicsPanel p2){ 
    // return value: 0 if valid parity check, -1 otherwise 
    // new return value is decoded word
    // lambda: log likelihood ratio, loop_max: maximum number of iterations  
    int i,j,k;  
    int ret = -1;  
    alpha = new double[m][rmax];   
    beta  = new double[m][rmax];   
    tmp_bit= new int[n];  
                // initialization of beta  
    for (i=0; i <= m-1; i++) {  
        for (j=0; j <= row_weight[i]-1; j++) {  
            beta[i][j] = 0.0;  
        }  
    }  
  
    double sum, tmp;  
    int prod;  
    posterior = new double[n]; 
    double[] q0 = new double[n];
    int f,parity;  
    
    for (int loop = 1; loop <= loop_max; loop++) { // start of iteration  
        
        // row operation  
        for (i = 0; i <= m-1; i++) {   
            sum = 0.0; prod = 1;  
            for (k = 0; k <= row_weight[i]-1; k++) {   
                tmp = lambda[row_list[i][k]] + beta[i][k];  
                sum = sum + gallager_f(Math.abs(tmp));  
                prod = prod * sign(tmp);  
            }  
            for (j = 0; j <= row_weight[i]-1; j++) {  
                tmp = lambda[row_list[i][j]] + beta[i][j];  
                alpha[i][j] = damping*prod * sign(tmp) * gallager_f(sum-gallager_f(Math.abs(tmp)));  
            }         
        }           
        // column operation  
        sum = 0.0;  
        for (i = 0; i <= n-1; i++) {  
            sum = 0.0;  
            for (k = 0; k <= col_weight[i]-1; k++)   
                sum += alpha[col_list_r[i][k]][col_list_c[i][k]];  
            for (j = 0; j <= col_weight[i]-1; j++) {   
                beta[col_list_r[i][j]][col_list_c[i][j]]   
                = sum-alpha[col_list_r[i][j]][col_list_c[i][j]];  
            }             
            posterior[i] = lambda[i] + sum; 
            q0[i] = Math.exp(posterior[i])/(1+Math.exp(posterior[i]));
            if (posterior[i] >0) tmp_bit[i] = 0;  
            else tmp_bit[i] = 1;  
        }               
        System.out.println( "-----------------------------loop no. = " +loop);  
        // for (i = 0; i <= n-1; i++)   
            // System.out.print(posterior[i]+" ");  
        // System.out.println("\n");  
           // chaos 
        
        try
        {
            Thread.sleep(100);     
        }
        catch(InterruptedException ex)
        {
            // add logging ex
            ex.printStackTrace();
            Thread.currentThread().interrupt();
        }
       
        LDPC.updateImages(q0);
        
        // p1.modImage(q0); // was q0=Pr(x_i=0)
        // p2.modImage(q0);
        // w1.repaint();
        // w2.repaint();
 
        /* parity check  
        f = 0;  
        for (i=0; i <= m-1; i++) {  
            parity = 0;  
            for (j=0; j <= row_weight[i]-1; j++)   
            parity = (parity + tmp_bit[row_list[i][j]]) % 2;  
            if (parity == 1) f = 1;  
        }  
        if (f == 0) {  
            ret = 0;  
            break;  
        }  
        */
  
    }               // end of iterations  
    return tmp_bit; //ret;  
    
  }  
       
        
                    // sign function  
  public int sign(double x) {  
    if (x >= 0.0) return 1;  
    else return -1;  
  }  
                // Gallager's f function  
  public double gallager_f(double x) {  
    double y;  
    double a = Math.exp(x);  
    if (x < 0.00001)   
      y = 12.21;      
                // 12.21 = gallager_f(0.00001)  
    else   
      y = Math.log((a+1.0)/(a-1.0));  
    return y;  
  }  

}
