package display;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.awt.image.RescaleOp;

import javax.swing.*;

import application.Preferences;


/**
 * Correspond a un video projecteur. 
 * 
 * @author sydxrey
 *
 */
@SuppressWarnings("serial")
public class Projector extends JWindow {

	private final static boolean DEBUG = false;

	private float alpha; // alpha composite
	private double noiseAlphaMax = 1.0f;
	private float brightness = 1.0f;
	private BufferedImage bufferedImage = null;

	private String sentence="initial test string";
	
	private int[] pix; // tmp pixels for messing up image
	private int[] q0i;

	/**
	 * 
	 * @param screen une des deux sorties video (0 ou 1)
	 */
	public Projector(int screen) {

		super(GraphicsEnvironment.getLocalGraphicsEnvironment().getScreenDevices()[screen].getDefaultConfiguration()); // pour afficher sur ecran I ou II

		this.alpha = 1.0f;
		Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
		String str = Preferences.getPreferences().getStringProperty(Preferences.Key.WINDOW_SIZE); // "fullscreen" or "debug"
		if (str.contentEquals("debug")) dim.height /= 2;		
		initBufferedImage(dim);
		add(new GraphicsPanel());
		setSize(dim);
		// setLocation(0, 0); // attention : ramene la frame sur l'écran de gauche !
		setVisible(true);
		//setSentence ("initial phrase");
	}
	
	private void initBufferedImage(Dimension dim) {
		bufferedImage = new BufferedImage(dim.width, dim.height, BufferedImage.TYPE_INT_ARGB);
		pix = new int[4*dim.width];
        /*Graphics2D g = (Graphics2D)bufferedImage.getGraphics();
        g.setColor(Color.BLACK);
        g.fillRect(0, 0, bufferedImage.getWidth(), bufferedImage.getHeight());*/
	}
	
	/**
	 * Change the alpha composite parameter for this video projector
	 * @param alpha
	 */
	public void setAlpha(double alpha) {
		this.alpha = (float)alpha;
		//repaint();
	}
	
	/**
	 * Change the max noise alpha composite parameter for this video projector
	 * @param alpha
	 */
	public void setNoiseAlphaMax(double alpha) {
		this.noiseAlphaMax = (float)alpha;
		//repaint();
	}
	
	/**
	 * Set a new sentence and do net yet repaint (add effects first)
	 * @param phrase
	 */
	public void setSentence (String sentence) {
		this.sentence = sentence;
	}
	
	/**
	 * update the buffered image used by paintComponent()
	 */
	void updateBufferedImage() {
		
        Graphics2D g = (Graphics2D)bufferedImage.getGraphics();
        
        // fill background in black:
        g.setColor(Color.BLACK);
        g.fillRect(0, 0, bufferedImage.getWidth(), bufferedImage.getHeight());

        // enable anti aliasing for strings
        RenderingHints rh = new RenderingHints(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g.setRenderingHints(rh);        
        
        // draw sentence in white with alpha transparency:
        g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha)); // alpha=0 => transparent pixel
        g.setPaint(Color.WHITE);
        g.setFont(new Font("Serif", Font.PLAIN, 60));
 //       int textWidth = g.getFontMetrics().stringWidth(sentence);
        this.drawString(g,sentence, 0, 0);

        // add noise from ldpc decoder:
        //q0i=null; //debug
        //int textWidth = g.getFontMetrics().stringWidth(sentence);
        //int textHeight = g.getFontMetrics().getHeight();
        //g.drawString(sentence, (getWidth()-textWidth)/2, (getHeight()+textHeight)/2);

        // add noise from ldpc decoder:
        //q0i=null; //debug
        if (q0i == null) return;
        final int n = q0i.length;
        final int width = bufferedImage.getWidth() & 0xfffc; // must be multiple of 4
		final int height = bufferedImage.getHeight() & 0xfffc;

        //int alpha []= q0*255.0 % 0xff;

        for (int cy=0;cy<height;cy+=4) {   
        	bufferedImage.getRGB(0,cy,width,4,pix,0,width);
            for (int cx=0;cx<width;cx+=4) {
            	// starts low (transparent, hence light gray), goes large (opaque, dark) in the end                
            	int a = q0i[(251*(cx/4)+(257)*(cy/4))%n] << 24; 	
            	pix[cx] = (pix[cx] & 0xffffff) | a;
            	pix[cx+1] = (pix[cx+1] & 0xffffff) | a;
            	pix[cx+2] = (pix[cx+2] & 0xffffff) | a;
            	pix[cx+3] = (pix[cx+3] & 0xffffff) | a;
            	pix[cx+width] = (pix[cx+width] & 0xffffff) | a;
            	pix[cx+1+width] = (pix[cx+1+width] & 0xffffff) | a;
            	pix[cx+2+width] = (pix[cx+2+width] & 0xffffff) | a;
            	pix[cx+3+width] = (pix[cx+3+width] & 0xffffff) | a;
            	pix[cx+width*2] = (pix[cx+width*2] & 0xffffff) | a;
            	pix[cx+1+width*2] = (pix[cx+1+width*2] & 0xffffff) | a;
            	pix[cx+2+width*2] = (pix[cx+2+width*2] & 0xffffff) | a;
            	pix[cx+3+width*2] = (pix[cx+3+width*2] & 0xffffff) | a;
            	pix[cx+width*3] = (pix[cx+width*3] & 0xffffff) | a;
            	pix[cx+1+width*3] = (pix[cx+1+width*3] & 0xffffff) | a;
            	pix[cx+2+width*3] = (pix[cx+2+width*3] & 0xffffff) | a;
            	pix[cx+3+width*3] = (pix[cx+3+width*3] & 0xffffff) | a;
            }
            bufferedImage.setRGB(0,cy,width,4,pix,0,width);
        }
	}
	
    /**
     * Draw a multiline string into a graphics
     * @param g: graphics
     * @param text
     * @param x: hor offset
     * @param y
     */
    private void drawString(Graphics g, String text, int x, int y) {
    	//int textWidth = 0;
        int textHeight = 0; 
        int lineHeight = g.getFontMetrics().getHeight();
        for (String line : text.split("\\|")) {
        	//textWidth = Math.max(textWidth,g.getFontMetrics().stringWidth(line));
        	textHeight += lineHeight;
        }
        x += (getWidth())/2; // center for all lines
        y += (getHeight()-textHeight)/2; // center of text block 
        //System.out.println("x="+x);
        //System.out.println("y="+y);
        for (String line : text.split("\\|"))
            g.drawString(line, x-g.getFontMetrics().stringWidth(line)/2, y += lineHeight);
    }
	
	public void messUpDisplay (double[] q0) {
		if (q0 == null) return;
		if (q0i == null) q0i = new int[q0.length]; // length shall not change
		//System.out.println("mess up here");
		try {
			for (int i=0; i<q0.length; i++) q0i[i] = (int)(255*(q0[i]-1)*noiseAlphaMax+255);
		} catch (NullPointerException e) {
			e.printStackTrace(); // rustine de syd ! TODO NullPointerException incomprehensible...
		}
	}
	
	public void messUpDisplay (double[] q0, int t0, int t1, int p) {	
		if (q0 == null) return;
		if (q0i == null) {
			q0i = new int[q0.length]; // length shall not change
			for (int i=0; i<q0.length; i++) q0i[i] = (int)(255*(q0[i]-1)*noiseAlphaMax+255);
		}
		else {
			//for (int i=0; i<q0.length; i++) q0i[i] = (int)(255*q0[i]*noiseAlphaMax);			
			int len = q0.length;
			//t0 = t0*(len/p); // start of stride to copy
			//t1 = (t1*(len/p)+len-1) % len; // end of stride
			//System.out.println("last = "+t0+" cur = "+t1);
			for (int i=t1; i<len; i+=p) 
				q0i[i] = (int)(255*q0[i]*noiseAlphaMax);
		}
	}
	
	// ------------------------------------------------------------------------------------
	

	class GraphicsPanel extends JPanel {

	    public GraphicsPanel(){
	    	super();
	    	//setBackground(Color.BLACK);
	    }
	    
	    @Override
	    protected void paintComponent(Graphics oldg) {
	    	
	        super.paintComponent(oldg);
	        updateBufferedImage();
	        
	        Graphics2D g = (Graphics2D) oldg;

	        //brightness = 1.0f; // TODO DEBUG SYD
	        /*g.drawImage(bufferedImage, new RescaleOp(
	        			new float[]{brightness, brightness, brightness, 1f}, // scale factors for red, green, blue, alpha
	        			new float[]{0, 0, 0, 0}, // offsets for red, green, blue, alpha
	        			null), // You can supply RenderingHints here if you want to //TODO : anti aliasing
	        		0, 0);*/
	        //g.drawImage(bufferedImage, new AffineTransform(1f,0f,0f,1f,0f,0f), null);
	        g.drawImage(bufferedImage,0,0,Color.WHITE,null);
	    }
	}

	
	// --------------------------------- test ---------------------------------

	public static void main(String[] args) throws Exception {

		new Projector(0);
		//new Projector(AnimationConstants.SCREEN_1);
	}


}
