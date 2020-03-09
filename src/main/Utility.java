package main;

import render.Renderer;

import javax.imageio.ImageIO;
import java.awt.Color;
import java.awt.image.BufferedImage;

public class Utility {
	private Utility(){}

	public static BufferedImage readFile(String name){
		try {
			return ImageIO.read(Renderer.class.getResourceAsStream(name));
		} catch (Exception e) {
			e.printStackTrace();
			throw new IllegalArgumentException("Could not find resource: " + name);
		}
	}
	
	public static interface PixelReader {
		public void init(int width, int height);
		public void add(int x, int y, Color color);
	}
	public static void initializeTexture(String name, PixelReader creator){
		BufferedImage img = Utility.readFile(name);
		final int width = img.getWidth();
		final int height = img.getHeight();
		int[] pixels = img.getRGB(0, 0, width, height, null, 0, width);
		int startX = -width / 2;
		int startY = -height / 2;
		int endX = startX + width;
		int endY = startY + height;
		int pixelsIndex = 0;
		int white = Color.WHITE.getRGB();
		System.out.println("Successfully loaded image: " + name);
		
		for(int y = startY; y < endY; y++){
			for(int x = startX; x < endX; x++){
				if(pixels[pixelsIndex] != white) {
					creator.add(x, y, new Color(pixels[pixelsIndex]));
				}
				pixelsIndex++;
			}
		}
	}
}
