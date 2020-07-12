package ldpc;

// For setting color of the text 
import java.awt.Color;
import java.awt.Dimension;
// For setting font of the text 
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.io.IOException;

import javax.swing.JPanel;
import javax.swing.JWindow;



public class LDPC {

    public static void main(String[] args) throws IOException {
        //Let's do a simulation to check post FEC BER 
        BSC channel = new BSC(0.06);
        LdpcDecoder decoder = new LdpcDecoder("204.33.486.txt", 0.06);
        
        // Image dimensions 
        int width = 640, height = 320; 
  
        // Create buffered image object 
        BufferedImage img = null; 
        img = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB); 
  
        // create random values pixel by pixel 
        for (int y = 0; y < height; y++) 
        { 
            for (int x = 0; x < width; x++) 
            { 
                int a = 0;//(int)(Math.random()*256); //generating 
                int r = 0;//(int)(Math.random()*256); //values 
                int g = 128;//(int)(Math.random()*256); //less than 
                int b = 128;//(int)(Math.random()*256); //256 
  
                int p = (a<<24) | (r<<16) | (g<<8) | b; //pixel 
  
                img.setRGB(x, y, p); 
            } 
        } 
        JWindow w = new JWindow();
        GraphicsPanel p = new GraphicsPanel(img);
        w.add(p);
        w.pack();
        // Get current screen size
        int scr_width = Toolkit.getDefaultToolkit().getScreenSize().width;
        int scr_height = Toolkit.getDefaultToolkit().getScreenSize().height;
        // Get x coordinate on screen for make JWindow locate at center
        int x = (scr_width-w.getSize().width)/2;
        // Get y coordinate on screen for make JWindow locate at center
        int y = (scr_height-w.getSize().height)/2;
        // Set new location for JWindow
        w.setLocation(x,y);
        
        // Make JWindow visible
        w.setVisible(true);
        
        
        Graphics graphics = img.getGraphics();
        graphics.setColor(Color.BLACK);
        graphics.fillRect(0, 0, 640, 50);
        graphics.setColor(Color.LIGHT_GRAY);
        graphics.setFont(new Font("Arial Black", Font.PLAIN, 24));
        graphics.drawString("Vorrei dirvi che sono", 10, 25);
        w.repaint();
        
        int numberOferrors = 0;
        boolean[] rec = new boolean[204];
        for(int i = 0; i< 1; i++){
            
            boolean[] msg = new boolean[204]; 
            boolean[] sent = channel.send(msg);//send 0 vecor
            
            rec = decoder.Decode(sent, w, p);
            
            for(int j = 0; j<204; j++){
                if(rec[j] ==true){
                    numberOferrors = numberOferrors+1;
                    break;
                }
            }
        }
        
        System.out.println("Bit error rate after decoding = "+ ((double)numberOferrors)/204000);
        System.out.println("Compare this to ber before decoding = "+ 0.02);
        
    }
    
    public static boolean[][] toBoolean(int[][] H){
        boolean [][] Hb = new boolean[H.length][H[0].length];
        for(int i = 0; i< H.length; i++){
            for(int j = 0; j< H[0].length; j++){
                Hb[i][j] = (H[i][j]!=0);
            }
        }
        return Hb;
    }
    
    public static boolean[] toBoolean(int[] H){
        boolean [] Hb = new boolean[H.length];
        for(int i = 0; i< H.length; i++){
                Hb[i] = (H[i]!=0);
        }
        return Hb;
    }
    
    public static int[] toInt(boolean[] H){
        int[] Hi = new int[H.length];
        for(int i = 0; i< H.length; i++){
                Hi[i] = (H[i]==false)?0:1;
        }
        return Hi;
    }
}

/**
 * Contains Main method.
 * @author Sasank Chilamkurthy
 */

@SuppressWarnings("serial")
class GraphicsPanel extends JPanel {

    private BufferedImage image;
    private int x;

    public GraphicsPanel(BufferedImage image){
        this.image = image;
        setPreferredSize(new Dimension(image.getWidth(), image.getHeight()));
        x = 0;
    }
    public void modImage (double [] q0) {
        int n = q0.length;
        //int alpha []= q0*255.0 % 0xff;
        
    for (int cx=0;cx<image.getWidth();cx++) {          
        for (int cy=0;cy<image.getHeight();cy++) {
            int color = image.getRGB(cx, cy) & 0xffffff;

            int a = (int)(q0[(cx/16+image.getWidth()*(cy/16))%n]*255.0);
            //a = (cx/16+image.getWidth()*0/16)%255;
            int newcolor = color | (a <<24);
            image.setRGB(cx, cy, newcolor);            

        }

    }
        //this.image.setRGB(10+x,10,0x0f0f0f);
        //x = x+1 % (image.getWidth()-10);
    }
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.drawImage(image, 0, 0, null);
    }
}
