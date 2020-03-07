package model;

import control.IController;
import render.IRenderer;

import java.awt.*;
import java.util.*;

public class Model {
	static final int LAYER_EVIL_PROJECTILE = 1, 
			LAYER_EVIL_TRAIL = 2,
			LAYER_EVIL_INVINCIBLE_EYE = 3,
			LAYER_EVIL_TEMP_EYE = 4,
			LAYER_GOOD_BULLET = 5,
			LAYER_GOOD_PLAYER = 6;
	
	private static class Bucket {
		// organized by collision layer
		private Map<Integer, Set<PhysicsPixel>> collisionPixels;
		// sorted by render priority
		private SortedSet<PhysicsPixel> renderPixels;
		Bucket() {
			collisionPixels = new HashMap<>();
			renderPixels = new TreeSet<>(Comparator.comparingInt(p -> -p.priority));
		}

		Color render() {
			if (renderPixels.isEmpty()) return null;
			PhysicsPixel first = renderPixels.first();
			return first.color;
		}
		void tick(Model model) {
			renderPixels.forEach(physicsPixel -> physicsPixel.tick(model));
		}

		void addPixel(PhysicsPixel p) {
			collisionPixels.computeIfAbsent(p.collisionLayer, k -> new HashSet<>()).add(p);
			renderPixels.add(p);
		}
		void removePixel(PhysicsPixel p) {
			collisionPixels.get(p.collisionLayer).remove(p);
			renderPixels.remove(p);//TODO remove bucket
		}
		Set<PhysicsPixel> collision(int collisionLayer) {
			Set<PhysicsPixel> ret = collisionPixels.get(collisionLayer);
			return ret == null ? Collections.emptySet() : ret;
		}
	}
	private static class PixelMove {
		PhysicsPixel pixel;
		Coord pos;
		PixelMove(PhysicsPixel pixel, Coord pos) {
			this.pixel = pixel;
			this.pos = pos;
		}
	}
	private static class Player {
		static final float COS45 = 0.70710678118f;
		float x, y, speed;
		void move(IController controller){
			float moveX = 0;
			float moveY = 0;
			if(controller.right() != controller.left()){
				if(controller.up() != controller.down()) moveX = speed * COS45;
				else moveX = speed;
				if(controller.left()) moveX = -moveX;
			}
			if(controller.up() != controller.down()){
				if(controller.left() != controller.right()) moveY = speed * COS45;
				else moveY = speed;
				if(controller.up()) moveY = -moveY;
			}
			x += moveX;
			y += moveY;
		}
	}

	private Map<Coord, Bucket> buckets;
	private LinkedList<PhysicsPixel> addQueue;
	private LinkedList<PhysicsPixel> removeQueue;
	private LinkedList<PixelMove> moveQueue;
	private final Player player;

	public Model() {
		buckets = new HashMap<>();
		addQueue = new LinkedList<>();
		removeQueue = new LinkedList<>();
		moveQueue = new LinkedList<>();
		player = new Player(){{ speed = 1; }};
		add(new StandardEvilProjectile(new Coord(-100, -100), 10, 10, 120));
	}

	public void tick(IController controller) {
		player.move(controller);
		
		buckets.forEach((coord, bucket) -> bucket.tick(this));

		int length = addQueue.size();
		for (int i = 0; i < length; i++) {
			add(addQueue.pop());
		}
		length = removeQueue.size();
		for (int i = 0; i < length; i++) {
			remove(removeQueue.pop());
		}
		length = moveQueue.size();
		for (int i = 0; i < length; i++) {
			PixelMove pop = moveQueue.pop();
			move(pop.pixel, pop.pos);
		}
	}
	public void render(IRenderer renderer) {
		renderer.clear();
		renderer.setCenter((int)player.x, (int)player.y);
		buckets.forEach((coord, bucket) -> renderer.draw(coord, bucket.render(), 1));
		renderer.render();
	}

	private void add(PhysicsPixel p) {
		buckets.computeIfAbsent(p.pos, k -> new Bucket()).addPixel(p);
	}
	private void remove(PhysicsPixel p) {
		buckets.get(p.pos).removePixel(p);
	}
	private void move(PhysicsPixel p, Coord pos) {
		remove(p);
		p.pos = pos;
		add(p);
	}
	void queueAdd(PhysicsPixel p) {
		addQueue.push(p);
	}
	void queueRemove(PhysicsPixel p) {
		removeQueue.push(p);
	}
	void queueMove(PhysicsPixel p, Coord pos) {
		moveQueue.push(new PixelMove(p, pos));
	}

	Set<PhysicsPixel> collision(Coord pos, int collisionLayer) {
		Bucket b = buckets.get(pos);
		if (b == null) return Collections.emptySet();
		return b.collision(collisionLayer);
	}
}

class ToothAttack {
	int count = 0;
	void tick(Model model) {

	}
}

