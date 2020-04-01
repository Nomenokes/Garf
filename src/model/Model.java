package model;

import control.IController;
import main.Utility;
import render.IRenderer;

import java.awt.Color;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.concurrent.atomic.AtomicInteger;

public class Model {
	static final int LAYER_EVIL_PROJECTILE = 1,
			LAYER_EVIL_TRAIL = 2,
			LAYER_EVIL_INVINCIBLE_EYE = 3,
			LAYER_EVIL_TEMP_EYE = 4,
			LAYER_GOOD_BULLET = 5,
			LAYER_GOOD_PLAYER = 6;
	static class ColorPriority {
		final int priority;
		final Color color;
		ColorPriority(int priority, Color color) {
			this.priority = priority;
			this.color = color;
		}
	}
	static final ColorPriority PRIORITY_ORANGE_EVIL_PROJECTILE = new ColorPriority(5, Color.ORANGE),
			PRIORITY_BLACK_TRAIL = new ColorPriority(2, Color.BLACK),
			PRIORITY_TEXTURE_PROJECTILE = new ColorPriority(4, null),
			PRIORITY_GOOD_BLUE_PROJECTILE = new ColorPriority(8, Color.BLUE);

	private static class Bucket {
		// organized by collision layer
		private Map<Integer, Set<PhysicsPixel>> collisionPixels;
		// sorted by render priority
		private SortedSet<PhysicsPixel> renderPixels;
		private Coord coord;
		Bucket(Coord coord) {
			this.coord = coord;
			collisionPixels = new HashMap<>();
			renderPixels = new TreeSet<>((p1, p2) ->
					p1.priority == p2.priority ?
							p2.hashCode() - p1.hashCode() : //if priority is same, subtract hash codes to take equality into account
							p2.priority - p1.priority); //else order by priority
		}

		Color render() {
			Color ret;
			if (renderPixels.isEmpty()) ret = null;
			else {
				PhysicsPixel first = renderPixels.first();
				ret = first.color;
			}
			return ret;
		}
		void tick(Model model) {
			int size;
			if(Utility.ASSERTIONS) size = renderPixels.size();
			renderPixels.forEach(PhysicsPixel::tick);
			if(Utility.ASSERTIONS) assert size == renderPixels.size();
		}

		void addPixel(PhysicsPixel p) {
			int size;
			if(Utility.ASSERTIONS) size = renderPixels.size();
			collisionPixels.computeIfAbsent(p.collisionLayer, k -> new HashSet<>()).add(p);
			renderPixels.add(p);
			if(Utility.ASSERTIONS){
				size++;
				AtomicInteger after = new AtomicInteger();
				collisionPixels.forEach((i, sp) -> after.addAndGet(sp.size()));
				assert size == after.get();
				assert size == renderPixels.size();
			}
		}
		void removePixel(PhysicsPixel p) {
			int size;
			if(Utility.ASSERTIONS) size = renderPixels.size();
			collisionPixels.get(p.collisionLayer).remove(p);//TODO remove sub-bucket
			renderPixels.remove(p);
			if(Utility.ASSERTIONS){
				size--;
				AtomicInteger after = new AtomicInteger();
				collisionPixels.forEach((i, sp) -> after.addAndGet(sp.size()));
				assert size == after.get();
				assert size == renderPixels.size();
			}
		}
		Set<PhysicsPixel> collision(int collisionLayer) {
			Set<PhysicsPixel> ret = collisionPixels.get(collisionLayer);
			return ret == null ? Collections.emptySet() : ret;
		}
	}
	//a stored call to move
	private static class PixelMove {
		final PhysicsPixel pixel;
		final Coord pos;
		PixelMove(PhysicsPixel pixel, Coord pos) {
			this.pixel = pixel;
			this.pos = pos;
		}
	}

	private Map<Coord, Bucket> buckets;
	private LinkedList<PhysicsPixel> addQueue;
	private LinkedList<PhysicsPixel> removeQueue;
	private LinkedList<PixelMove> moveQueue;
	private final Player player;
	private Phase phase;

	public Model() {
		buckets = new HashMap<>();
		addQueue = new LinkedList<>();
		removeQueue = new LinkedList<>();
		moveQueue = new LinkedList<>();
		player = new Player(this, 0, 0, 1);
		phase = new FacePhase(this, 1, null, null, new FloatCoord(0, 0), 0);
	}

	public void tick(IController controller, IRenderer renderer) {
		phase = phase.tick();
		player.move(controller, renderer);

		int size;
		if(Utility.ASSERTIONS) size = buckets.size();
		buckets.forEach((coord, bucket) -> bucket.tick(this));
		if(Utility.ASSERTIONS) assert size == buckets.size();

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
		renderer.setCenter(new Coord(player.getCameraPos()));
		buckets.forEach((coord, bucket) -> renderer.draw(coord, bucket.render(), 1));
		renderer.render();
	}

	void add(PhysicsPixel p) {
		buckets.computeIfAbsent(p.pos, k -> new Bucket(p.pos)).addPixel(p);
	}
	void remove(PhysicsPixel p) {
		buckets.get(p.pos).removePixel(p);//TODO remove bucket
	}
	void move(PhysicsPixel p, Coord pos) {
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

class Player {
	private static final float COS45 = 0.70710678118f;
	private float x, y, speed;
	private Model model;
	Player(Model model, float x, float y, float speed) {
		this.model = model;
		this.x = x;
		this.y = y;
		this.speed = speed;
	}
	void move(IController controller, IRenderer renderer) {
		float moveX = 0;
		float moveY = 0;
		if (controller.right() != controller.left()) {
			if (controller.up() != controller.down()) moveX = speed * COS45;
			else moveX = speed;
			if (controller.left()) moveX = -moveX;
		}
		if (controller.up() != controller.down()) {
			if (controller.left() != controller.right()) moveY = speed * COS45;
			else moveY = speed;
			if (controller.up()) moveY = -moveY;
		}
		x += moveX;
		y += moveY;

		if (controller.mouse1()) {
			FloatCoord vel = Utility.normalize(renderer.transformScreenPoint(controller.mousePos()));
			model.add(new StandardGoodProjectile(model, new Coord(getCameraPos()), 300, vel.x, vel.y));
		}
	}
	FloatCoord getCameraPos() {
		return new FloatCoord(x, y);
	}
	FloatCoord getTargetPos() {
		return new FloatCoord(x, y);
	}
}

