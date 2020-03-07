package main;

import com.sun.org.apache.xpath.internal.operations.Mod;
import control.Controller;
import model.Coord;
import model.Model;
import render.Renderer;

import java.awt.*;
import java.util.Map;

public class Runner implements Runnable {
	private Renderer renderer;
	private Model model;
	private Controller controller;
	private long tickTime = 1000000000 / 60; //in ns
	private long ticksPerRender = 1;

	public Runner() {
		renderer = new Renderer("test", 256, 256, Color.WHITE);
		model = new Model();
		controller = new Controller(renderer);
	}

	@Override
	public void run() {
		long current = 0;
		long last = 0;
		long render = 0;
		while (true) {
			current = System.nanoTime();
			if (current >= last + tickTime) {
				last = current;
				model.tick(controller);
				render++;
				if(render >= ticksPerRender){
					render = 0;
					model.render(renderer);
				}
			}
		}
	}
//	@Override
//	public boolean left() { return controller.left(); }
//	@Override
//	public boolean right() { return controller.right(); }
//	@Override
//	public boolean up() { return controller.up(); }
//	@Override
//	public boolean down() { return controller.down(); }
//	@Override
//	public boolean mouse1() { return controller.mouse1(); }
//	@Override
//	public boolean mouse2() { return controller.mouse2(); }
//	@Override
//	public Point mousePos() { return controller.mousePos(); }
//	@Override
//	public void setCenter(int x, int y) { renderer.setCenter(x, y); }
//	@Override
//	public void draw(Map<Coord, Color> pixels, int priority) { renderer.draw(pixels, priority); }
//	@Override
//	public void draw(Coord coord, Color color, int priority) { renderer.draw(coord, color, priority); }
//	@Override
//	public void render() { renderer.render(); }
//	@Override
//	public void clear() { renderer.clear(); }
}
