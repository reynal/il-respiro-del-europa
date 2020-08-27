package ldpc;

import java.io.IOException;
import java.util.*;
import java.math.*;
// For setting color of the text 
import java.awt.Color;   
// For setting font of the text 
import java.awt.Font; 
import java.awt.Graphics;
import java.awt.Graphics2D; 
import java.awt.image.BufferedImage;
import java.awt.image.RescaleOp;
import java.awt.Composite;
import java.awt.AlphaComposite;
import java.awt.Dimension;
import java.awt.Toolkit;
import javax.imageio.*;
import javax.swing.*;

import display.LdpcDecoder;

/**
 * 
 */
public class LDPC {
	
    private BufferedImage img1 = null;
    private BufferedImage img2 = null;
    private BufferedImage img12 = null;
    private JWindow w1 = new JWindow();
    private JWindow w2 = new JWindow();
    private JWindow w12 = new JWindow();
    private GraphicsPanel p1 = null;
    private GraphicsPanel p2 = null;
    private CompositeTestPanel p12 = null;
    
    
    // public double intensity = 0.0;
    
    //double getIntensity() { }
    
    
    /**
     * 
     * @throws IOException si pb ouverture du fichier contenant le code
     */
    public LDPC() throws IOException { // SR : was main()
    	
        //Let's do a simulation to check post FEC BER 
        
        
        // SR int max_iter = 40;
        
        LdpcDecoder decoder = new LdpcDecoder();

        // SR => claudio : tout ce code correspond a peu pres a SentencesAnimator et Projector...
        
        // Image dimensions 
        int width = 640, height = 320; 
  
        // Create buffered image object 
        
        img1 = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);          
        img2 = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);          
        img12 = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB); 
  
        // create random values pixel by pixel 
        // for (int y = 0; y < height; y++) 
        // { 
            // for (int x = 0; x < width; x++) 
            // { 
                // int a = 0;//(int)(Math.random()*256); //generating 
                // int r = 0;//(int)(Math.random()*256); //values 
                // int g = 128;//(int)(Math.random()*256); //less than 
                // int b = 128;//(int)(Math.random()*256); //256 
  
                // int p = (a<<24) | (r<<16) | (g<<8) | b; //pixel 
  
                // img.setRGB(x, y, p); 
            // } 
        // } 
        
        p1 = new GraphicsPanel(img1);
        w1.add(p1);
        w1.pack();
        
        
        p2 = new GraphicsPanel(img2);
        w2.add(p2);
        w2.pack();
        
        
        p12 = new CompositeTestPanel(img1, img2);
        w12.add(p12);       
        w12.pack();
        
        // Get current screen size
        int scr_width = Toolkit.getDefaultToolkit().getScreenSize().width;
        int scr_height = Toolkit.getDefaultToolkit().getScreenSize().height;
        // Get x coordinate on screen for make JWindow locate at center
        //int x = (scr_width-w.getSize().width)/2;
        // Get y coordinate on screen for make JWindow locate at center
        //int y = (scr_height-w.getSize().height)/2;
        // Set new location for JWindow
        w1.setLocation(0,0);
        w2.setLocation(scr_width/2,0);
        w12.setLocation((scr_width-width)/2,height+50);
        
        // Make JWindow visible
        w1.setVisible(true);
        w2.setVisible(true);
        w12.setVisible(true);
        
        Graphics g1 = img1.getGraphics();
        g1.setColor(Color.BLACK);
        g1.fillRect(0, 0, width, height);
        g1.setColor(Color.WHITE);
        g1.setFont(new Font("Arial Black", Font.PLAIN, 36));
        int x1 = 10;
        int y1 = g1.getFontMetrics().getAscent();
        drawString(g1,"Vorrei dirvi: Sono nato in carso,\n in una casupola col tetto di paglia", x1, y1);
        w1.repaint();
        
        Graphics g2 = img2.getGraphics();
        g2.setColor(Color.BLACK);
        g2.fillRect(0, 0, width, height);
        g2.setColor(Color.WHITE);
        g2.setFont(new Font("Arial Black", Font.PLAIN, 36));
        int x2 = x1;
        int y2 = y1+g2.getFontMetrics().getHeight();
        drawString(g2,"Vorrei dirvi: Sono nato in Croazia,\n nella grande foresta di roveri", x2, y2);        
        w2.repaint();
        
        //p12.mergeImages(img1,img2);
        p12.setComposite(BlendComposite.Lighten);
        //w12.repaint(); setComposite does repaint()
        
        // SR => claudio : en gros, we just keep this:
        
        //int numberOferrors = 0;
        //int[] rec = new int[n];
        for(int i = 0; i<10; i++){
        
            //double noise[] = BSCnoise(n, bsc_p);
            
            //rec = 
        	decoder.initState(); //, w1, p1, w2, p2);
            
            /*for(int j = 0; j<n; j++){
                if(rec[j] ==1){
                    numberOferrors = numberOferrors+1;
                    break;
                }
            }*/
        }
        
        //System.out.println("Bit error rate after decoding = "+ ((double)numberOferrors)/204000);
        //System.out.println("Compare this to ber before decoding = "+ 0.02);
        
    }
    /**
     * Will be called from ldpc decoder at each iteration
     * to update (alpha) images based on probabilities
     * and display them
     * @param q0: probabilities bit=0
     */
    public void updateImages (double[] q0) {
    	
        int n = q0.length;
        double sum = 0.0;
        for (int i=0; i<n; i++) sum += q0[i];
        double bsc_p = 0.09; // TODO : doit dependre du souffle !?
        float per = (float)Math.min(1.0, 0.5*(1.0-(sum/(double)n))/bsc_p); // proba erreur
        System.out.println("per = "+per);
        p1.modImage(q0,1.0f-per); // was q0=Pr(x_i=0)
        p2.modImage(q0,per);
        w1.repaint();
        w2.repaint();
        p12.setBrightness(1.0f-per,per);
        //w12.repaint();        
    }
    
    
    /**
     * Draw a multiline string into a graphics
     * @param g: graphics
     * @param text
     * @param x: hor position
     * @param y
     */
    private void drawString(Graphics g, String text, int x, int y) {
        int lineHeight = g.getFontMetrics().getHeight();
        for (String line : text.split("\n"))
            g.drawString(line, x, y += 2*lineHeight);
    }
}

@SuppressWarnings("serial")
class GraphicsPanel extends JPanel {

    private BufferedImage image;
    private float br;

    public GraphicsPanel(BufferedImage image){
        this.image = image;
        setPreferredSize(new Dimension(image.getWidth(), image.getHeight()));
        br = 1.0f;
    }
    public void modImage (double [] q0, float brightness) {
        int n = q0.length;
        //int alpha []= q0*255.0 % 0xff;
        
        for (int cx=0;cx<image.getWidth();cx++) {          
            for (int cy=0;cy<image.getHeight();cy++) {
                int color = image.getRGB(cx, cy) & 0xffffff;
    
                int a = (int)(q0[(cx/4+image.getWidth()*(cy/4))%n]*255.0);
                //a = (cx/16+image.getWidth()*0/16)%255;
                int newcolor = color | (a <<24);
                image.setRGB(cx, cy, newcolor);            
    
            }
    
        }
        br = brightness;
    }
    
    public void modImageInv (double [] q0) {
        int n = q0.length;
        //int alpha []= q0*255.0 % 0xff;
        
        for (int cx=0;cx<image.getWidth();cx++) {          
            for (int cy=0;cy<image.getHeight();cy++) {
                int color = image.getRGB(cx, cy) & 0xffffff;
    
                int a = 255-(int)(q0[(cx/8+image.getWidth()*(cy/8))%n]*255.0);
                //a = (cx/16+image.getWidth()*0/16)%255;
                int newcolor = color | (a <<24);
                image.setRGB(cx, cy, newcolor);            
    
            }
    
        }
    }
    
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2;
        //g.drawImage(image, 0, 0, null);
        g2 = (Graphics2D) g;
        g2.drawImage(image, new RescaleOp(
        new float[]{br, br, br, 1f}, // scale factors for red, green, blue, alpha
        new float[]{0, 0, 0, 0}, // offsets for red, green, blue, alpha
        null), // You can supply RenderingHints here if you want to //TODO : anti aliasing
        0, 0);
    }
}

// ne sert plus
class MergePanel extends JPanel {

    private BufferedImage image;

    public MergePanel(BufferedImage image){
        this.image = image;
        setPreferredSize(new Dimension(image.getWidth(), image.getHeight()));       
    }
    
    public void mergeImages (BufferedImage img1, BufferedImage img2) {
        Graphics g = image.getGraphics();
        g.drawImage(img1, 0, 0, null);
        g.drawImage(img2, 0, 0, null);
        g.dispose();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.drawImage(image, 0, 0, null);
    }
}

//taken from http://www.java2s.com/Code/Java/2D-Graphics-GUI/BlendCompositeDemo.htm
class CompositeTestPanel extends JPanel {
    private BufferedImage image = null;
    private Composite composite = AlphaComposite.Src;
    private BufferedImage imageA;
    private BufferedImage imageB;
    private float brA, brB;
    private boolean repaint = false;

    public CompositeTestPanel(BufferedImage img1, BufferedImage img2) {
        setOpaque(false);
        brA = 1.0f;
        brB = 1.0f;
        imageA = img1;
        imageB = img2;
    }

    @Override
    public Dimension getPreferredSize() {
        return new Dimension(imageA.getWidth(), imageA.getHeight());
    }

    @Override
    protected void paintComponent(Graphics g) {
        if (image == null) {
            image = new BufferedImage(imageA.getWidth(),
                                      imageA.getHeight(),
                                      BufferedImage.TYPE_INT_ARGB);
            repaint = true;
        }

        if (repaint) {
            Graphics2D g2 = image.createGraphics();
            g2.setComposite(AlphaComposite.Clear);
            g2.fillRect(0, 0, image.getWidth(), image.getHeight());

            g2.setComposite(AlphaComposite.Src);
            //g2.drawImage(imageA, 0, 0, null);            
            g2.drawImage(imageA, new RescaleOp(
            new float[]{brA, brA, brA, 1f}, // scale factors for red, green, blue, alpha
            new float[]{0, 0, 0, 0}, // offsets for red, green, blue, alpha
            null), 0, 0); // You can supply RenderingHints here if you want to
            g2.setComposite(getComposite());
            //g2.drawImage(imageB, 0, 0, null);
            g2.drawImage(imageB, new RescaleOp(
            new float[]{brB, brB, brB, 1f}, // scale factors for red, green, blue, alpha
            new float[]{0, 0, 0, 0}, // offsets for red, green, blue, alpha
            null), 0, 0); // You can supply RenderingHints here if you want to
            g2.dispose();
            System.out.println("blend repaint");    
            repaint = false;
        }

        int x = (getWidth() - image.getWidth()) / 2;
        int y = (getHeight() - image.getHeight()) / 2;
        g.drawImage(image, x, y, null);
    }

    public void setComposite(Composite composite) {
        if (composite != null) {
            this.composite = composite;
            this.repaint = true;
            repaint();
        }
    }

    public void setBrightness (float _brA, float _brB) {
        brA = _brA;
        brB = _brB;
        this.repaint = true;
        repaint();        
    }
    
    public Composite getComposite() {
        return this.composite;
    }
}

