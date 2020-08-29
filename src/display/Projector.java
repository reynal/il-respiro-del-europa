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

	private final static boolean DEBUG = true;

	private float alpha; // alpha composite
	private float brightness = 1.0f;
	private BufferedImage bufferedImage = null;

	private String sentence="initial test string";
	private double[] q0;

	/**
	 * 
	 * @param screen une des deux sorties video (0 ou 1)
	 */
	public Projector(int screen) {

		super(GraphicsEnvironment.getLocalGraphicsEnvironment().getScreenDevices()[screen]
				.getDefaultConfiguration()); // pour afficher sur ecran I ou II

		this.alpha = 1.0f;
		Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
		String str = Preferences.getPreferences().getStringProperty(Preferences.Key.WINDOW_SIZE); // "fullscreen" or "debug"
		if (str.contentEquals("debug")) dim.setSize(1000, 300);		
		initBufferedImage(dim);
		add(new GraphicsPanel());
		setSize(dim);
		// setLocation(0, 0); // attention : ramene la frame sur l'écran de gauche !
		setVisible(true);
		//setSentence ("initial phrase");
	}
	
	private void initBufferedImage(Dimension dim) {
		bufferedImage = new BufferedImage(dim.width, dim.height, BufferedImage.TYPE_INT_ARGB);
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
        g.setFont(new Font("Serif", Font.PLAIN, 50));
        int textWidth = g.getFontMetrics().stringWidth(sentence);
        g.drawString(sentence, 0.5f*(getWidth()-textWidth), 0.5f*getHeight());

        // add noise from ldpc decoder:
        //q0=null; //debug
        if (q0 == null) return;
        int n = q0.length;
        //int alpha []= q0*255.0 % 0xff;
        for (int cx=0;cx<bufferedImage.getWidth();cx++) {          
            for (int cy=0;cy<bufferedImage.getHeight();cy++) {
                int color = bufferedImage.getRGB(cx, cy) & 0xffffff;
    
                int a = (int)(q0[(cx/4+bufferedImage.getWidth()*(cy/4))%n]*255.0); // starts low (transparent, hence light gray), goes large (opaque, dark) in the end
                //a=20; debug
                //float a = (float)q0[(cx/4+bufferedImage.getWidth()*(cy/4))%n];
                
                //a = (cx/16+image.getWidth()*0/16)%255; debug claudio
                int newcolor = color | (a <<24);
                bufferedImage.setRGB(cx, cy, newcolor);
                //g.setPaint(new Color(a,a,a));
                //g.fill(new Rectangle2D.Double(cx, cy, 1, 1));
            }
        }
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
	
	public void messUpDisplay (double[] q0) {
		this.q0 = q0;
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
	        g.drawImage(bufferedImage, new AffineTransform(1f,0f,0f,1f,0f,0f), null);

	    }
	}

	
	// --------------------------------- test ---------------------------------

	public static void main(String[] args) throws Exception {

		new Projector(0);
		//new Projector(AnimationConstants.SCREEN_1);
	}


}
