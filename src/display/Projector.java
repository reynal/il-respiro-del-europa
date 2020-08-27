package display;

import java.awt.*;

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
	String sentence="";

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
		add(new GraphicPanel());
		if (DEBUG) setSize(1000,300);
		else setSize(dim);
		// setLocation(0, 0); // attention : ramene la frame sur l'écran de gauche !
		setVisible(true);
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
	
	
	// ------------------------------------------------------------------------------------
	
	class GraphicPanel extends JPanel {
		
		public GraphicPanel() {
			super();
			this.setBackground(Color.BLACK);
		}

		@Override
		public void paintComponent(Graphics oldg) {
			
			super.paintComponent(oldg);

			Graphics2D g = (Graphics2D) oldg;
			
			g.setPaint(Color.WHITE);
            g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));			
            Font font = new Font("Serif", Font.PLAIN, 50); // TODO : improve font! (TTF)
            g.setFont(font);
            int textWidth = g.getFontMetrics().stringWidth(sentence);
			g.drawString(sentence, 0.5f*(getWidth()-textWidth), 0.5f*getHeight());

			//g.fill(new Ellipse2D.Double(100, 100, 200, 200));

		}
	}


	
	// --------------------------------- test ---------------------------------

	public static void main(String[] args) throws Exception {

		new Projector(AnimationConstants.SCREEN_0);
		//new Projector(AnimationConstants.SCREEN_1);
	}


}
