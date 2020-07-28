package display;

import java.awt.*;

import javax.swing.*;

/**
 * Test class for dual screen display
 * 
 * @author sydxrey
 *
 */
@SuppressWarnings("serial")
public class Projector extends JWindow {

	private AnimationConstants screen;
	private float alpha;

	public Projector(AnimationConstants screen) {

		super(GraphicsEnvironment.getLocalGraphicsEnvironment().getScreenDevices()[screen.ordinal()]
				.getDefaultConfiguration()); // pour afficher sur ecran I ou II

		this.screen = screen;
		this.alpha = 1.0f;
				
		Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
		add(new GraphicPanel());
		setSize(dim);
		// setLocation(0, 0); // attention : ramene la frame sur l'écran de gauche !
		setVisible(true);
	}
	
	public AnimationConstants getScreenConstants() {
		return screen;
	}
	
	public void setAlpha(float alpha) {
		this.alpha = alpha;
		repaint();
	}
	
	private String fetchSentence() {
		
		switch (screen) {
		case SCREEN_0:
			return "Vorrei dirvi: Sono nato in carso, in una casupola col tetto di paglia";
		case SCREEN_1:
			return "Vorrei dirvi: Sono nato in Croazia, nella grande foresta di roveri";
		}
		return null;
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
            Font font = new Font("Serif", Font.PLAIN, 50); // TODO : improve font!
            g.setFont(font);
            String s = fetchSentence();
            int textWidth = g.getFontMetrics().stringWidth(s);
			g.drawString(s, 0.5f*(getWidth()-textWidth), 0.5f*getHeight());

			//g.fill(new Ellipse2D.Double(100, 100, 200, 200));

		}
	}


	
	// --------------------------------- test ---------------------------------

	public static void main(String[] args) throws Exception {

		new Projector(AnimationConstants.SCREEN_0);
		//new Projector(AnimationConstants.SCREEN_1);
	}


}
