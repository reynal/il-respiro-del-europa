package display;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.RescaleOp;

import javax.swing.*;

/**
 * Correspond a un video projecteur. 
 * 
 * @author sydxrey
 *
 */
@SuppressWarnings("serial")
public class Projector extends JWindow {

	private final static boolean DEBUG = true;

	private AnimationConstants screen;
	private float alpha; // alpha composite
	private BufferedImage img = null;
	private GraphicsPanel gp = null;
	private String sentence="";

	/**
	 * 
	 * @param screen une des deux sorties video
	 */
	public Projector(AnimationConstants screen) {

		super(GraphicsEnvironment.getLocalGraphicsEnvironment().getScreenDevices()[screen.ordinal()]
				.getDefaultConfiguration()); // pour afficher sur ecran I ou II

		this.screen = screen;
		this.alpha = 1.0f;
				
		Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
		if (DEBUG) dim.setSize(1000, 300);
		img = new BufferedImage(dim.width, dim.height, BufferedImage.TYPE_INT_ARGB);
		gp = new GraphicsPanel(img);
		add(gp);
		setSize(dim);
		// setLocation(0, 0); // attention : ramene la frame sur l'écran de gauche !
		setVisible(true);
		//setSentence ("initial phrase");
	}
	
	public AnimationConstants getScreenConstants() {
		return screen;
	}
	
	/**
	 * Change the alpha composite parameter for this video projector
	 * @param alpha
	 */
	public void setAlpha(double alpha) {
		this.alpha = (float)alpha;
		repaint();
	}
	
	/**
	 * Set a new sentence and do net yet repaint (add effects first)
	 * @param phrase
	 */
	public void setSentence (String phrase) {
		sentence = phrase;
        Graphics g = img.getGraphics();
        g.setColor(Color.BLACK);
        g.fillRect(0, 0, img.getWidth(), img.getHeight());
        g.setColor(Color.WHITE);
        g.setFont(new Font("SansSerif", Font.PLAIN, 36));
        int x1 = 10;
        int y1 = g.getFontMetrics().getAscent();
        g.drawString(sentence, x1, y1);
        //repaint();
	}
	
	public void modImage (double[] q0, float brightness) {
		gp.modImage(q0, brightness);
	}
	
	// ------------------------------------------------------------------------------------
	
//	class GraphicPanel extends JPanel {
//		
//		public GraphicPanel() {
//			super();
//			this.setBackground(Color.BLACK);
//		}
//
//		@Override
//		public void paintComponent(Graphics oldg) {
//			
//			super.paintComponent(oldg);
//
//			Graphics2D g = (Graphics2D) oldg;
//			
//			g.setPaint(Color.WHITE);
//            g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));			
//            Font font = new Font("Serif", Font.PLAIN, 50); // TODO : improve font! (TTF)
//            g.setFont(font);
//            int textWidth = g.getFontMetrics().stringWidth(sentence);
//			g.drawString(sentence, 0.5f*(getWidth()-textWidth), 0.5f*getHeight());
//
//			//g.fill(new Ellipse2D.Double(100, 100, 200, 200));
//
//		}
//	}

	class GraphicsPanel extends JPanel {

	    private BufferedImage image;
	    private float br;

	    public GraphicsPanel(BufferedImage image){
	    	super();
	        this.image = image;
	        setPreferredSize(new Dimension(image.getWidth(), image.getHeight()));
	        br = 1.0f;
	    }
	    
	    public void modImage (double[] q0, float brightness) {
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
	    
	    
	    @Override
	    protected void paintComponent(Graphics oldg) {
	    	
	        super.paintComponent(oldg);
	        
	        Graphics2D g = (Graphics2D) oldg;

	        g.drawImage(image, new RescaleOp(
	        new float[]{br, br, br, 1f}, // scale factors for red, green, blue, alpha
	        new float[]{0, 0, 0, 0}, // offsets for red, green, blue, alpha
	        null), // You can supply RenderingHints here if you want to //TODO : anti aliasing
	        0, 0);
	    }
	}

	
	// --------------------------------- test ---------------------------------

	public static void main(String[] args) throws Exception {

		new Projector(AnimationConstants.SCREEN_0);
		//new Projector(AnimationConstants.SCREEN_1);
	}


}
