package render;

import model.Coord;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferStrategy;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class Renderer extends JFrame implements IRenderer {
	private static class ColorPair {
		int priority;
		Color color;
	}

	private Map<Coord, ColorPair> pixelMap = new HashMap<>();
	private int width, height, centerX, centerY;
	private Color backgroundColor;
	private final Canvas canvas;

	/**
	 * 
	 * @param name frame name 
	 * @param width in pixels
	 * @param height in pixels
	 */
	public Renderer(String name, int width, int height, Color backgroundColor){
		super(name);
		if(backgroundColor == null) throw new IllegalArgumentException("c is null");
		
		this.width = width;
		this.height = height;
		this.backgroundColor = backgroundColor;
		this.canvas = new Canvas();
		
		Dimension d = Toolkit.getDefaultToolkit().getScreenSize();
		int dWidth = (int)(d.width * 0.9);
		int dHeight = (int)(d.height * 0.9);
		float scaleWidth = (float)dWidth / width;
		float scaleHeight = (float)dHeight / height;
		int min = scaleWidth < scaleHeight ? dWidth : dHeight;
		this.setSize(new Dimension(dWidth, dHeight));
		canvas.setSize(new Dimension(min, min));
		
		this.add(new Panel(){{ add(canvas); }});
		this.setExtendedState(JFrame.MAXIMIZED_BOTH);;
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.setVisible(true);
		canvas.setBackground(Color.YELLOW);
		canvas.createBufferStrategy(3);
	}

	@Override
	public void setCenter(Coord center) {
		this.centerX = center.x;
		this.centerY = center.y;
	}

	@Override
	public void draw(Map<Coord, Color> pixels, int priority){
		pixels.forEach((coord, color) -> draw(coord, color, priority));
	}

	@Override
	public void draw(Coord coord, Color color, int priority) {
		if(color == null) return;
		ColorPair val = pixelMap.computeIfAbsent(coord, k -> new ColorPair());
		if(val.color == null || val.priority < priority) {
			val.priority = priority;
			val.color = color;
		}
	}

	private int[] getDrawPixels(){
		int[] pixels = new int[width * height];
		int pIndex = 0;
		
		int startX = centerX - width / 2;
		int startY = centerY - width / 2;
		int endX = startX + width;
		int endY = startY + width;
		for (int y = startY; y < endY; y++) {
				for (int x = startX; x < endX; x++) {
				Color c = backgroundColor;

				// if entry exists for this pixel, find the min priority and replace c
				Coord key = new Coord(x, y);
				ColorPair val = pixelMap.get(key);
				if(val != null) c = val.color;

				pixels[pIndex] = c.getRGB();
				pIndex++;
			}
		}
		
		return pixels;
	}
	
	@Override
	public void render(){
		int[] pixels = getDrawPixels();
		BufferStrategy b = canvas.getBufferStrategy();
		Graphics g = b.getDrawGraphics();
		BufferedImage i = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		i.setRGB(0, 0, width, height, pixels, 0, width);
		g.drawImage(i, 0, 0, canvas.getWidth(), canvas.getHeight(), null);
		g.dispose();
		b.show();
		requestFocus();
	}
	
	@Override
	public void clear(){
		pixelMap = new HashMap<>();
	}
}
