package display;

import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import java.io.*;

import javax.imageio.*;
import javax.swing.*;

@SuppressWarnings("serial")
public class WaterEffect extends JPanel implements Runnable, MouseMotionListener {

	String str;
	int width, height, hwidth, hheight;
	MemoryImageSource source;
	Image image, offImage;
	Graphics offGraphics;
	int i, a, b;
	int MouseX, MouseY;
	int fps, delay, size;

	short ripplemap[];
	int texture[];
	int ripple[];
	int oldind, newind, mapind;
	int riprad;
	Image im;

	Thread animatorThread;
	boolean frozen = false;

	public WaterEffect() throws Exception {

		addMouseMotionListener(this);
		
		im = ImageIO.read(new File("resource/test-water.jpg"));		

//		MediaTracker mt = new MediaTracker(this);
//		mt.addImage(im, 0);
//
//        try {
//            mt.waitForID(0);
//        } catch (InterruptedException e) {
//            return;
//        }
        
		// How many milliseconds between frames?
		fps = 10;
		delay = 1000 / fps;

		width = im.getWidth(this);
		height = im.getHeight(this);
		hwidth = width >> 1;
		hheight = height >> 1;
		riprad = 3;

		size = width * (height + 2) * 2;
		ripplemap = new short[size];
		ripple = new int[width * height];
		texture = new int[width * height];
		oldind = width;
		newind = width * (height + 3);

		PixelGrabber pg = new PixelGrabber(im, 0, 0, width, height, texture, 0, width);
		pg.grabPixels();

		source = new MemoryImageSource(width, height, ripple, 0, width);
		source.setAnimated(true);
		source.setFullBufferUpdates(true);

		image = createImage(source);
		offImage = createImage(width, height);
		offGraphics = offImage.getGraphics();

		animatorThread = new Thread(this);
		animatorThread.start();
		
	}

	public void mouseEntered(MouseEvent e) {
	}

	public void mouseDragged(MouseEvent e) {
	}

	public void mouseExited(MouseEvent e) {
	}

	public void mouseMoved(MouseEvent e) {
		disturb(e.getX(), e.getY());
	}

	public void run() {

		while (true) {
			newframe();
			source.newPixels();
			offGraphics.drawImage(image, 0, 0, width, height, null);
			repaint();

			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				break;
			}
		}
	}

	@Override
	public void paintComponent(Graphics g) {
		g.drawImage(offImage, 0, 0, this);
	}

	public void disturb(int dx, int dy) {
		for (int j = dy - riprad; j < dy + riprad; j++) {
			for (int k = dx - riprad; k < dx + riprad; k++) {
				if (j >= 0 && j < height && k >= 0 && k < width) {
					ripplemap[oldind + (j * width) + k] += 512;
				}
			}
		}
	}

	public void newframe() {
		// Toggle maps each frame
		i = oldind;
		oldind = newind;
		newind = i;

		i = 0;
		mapind = oldind;
		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				short data = (short) ((ripplemap[mapind - width] + ripplemap[mapind + width] + ripplemap[mapind - 1]
						+ ripplemap[mapind + 1]) >> 1);
				data -= ripplemap[newind + i];
				data -= data >> 5;
				ripplemap[newind + i] = data;

				// where data=0 then still, where data>0 then wave
				data = (short) (1024 - data);

				// offsets
				a = ((x - hwidth) * data / 1024) + hwidth;
				b = ((y - hheight) * data / 1024) + hheight;

				// bounds check
				if (a >= width)
					a = width - 1;
				if (a < 0)
					a = 0;
				if (b >= height)
					b = height - 1;
				if (b < 0)
					b = 0;

				ripple[i] = texture[a + (b * width)];
				mapind++;
				i++;
			}
		}
	}
	
	public static void main(String[] args) throws Exception {
		
		JFrame f = new JFrame("test water effect");
		WaterEffect we = new WaterEffect();
		
	}

}
