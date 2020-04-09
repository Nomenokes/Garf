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
		private Map<Integer, Set<PhysicsPixel>> byLayer;
		// sorted by render priority
		private SortedSet<PhysicsPixel> byPriority;
		private Coord coord;
		Bucket(Coord coord) {
			this.coord = coord;
			byLayer = new HashMap<>();
			byPriority = new TreeSet<>((p1, p2) ->
					p1.priority == p2.priority ?
							p2.hashCode() - p1.hashCode() : //if priority is same, subtract hash codes to take equality into account
							p2.priority - p1.priority); //else order by priority
		}

		Color render() {
			if (byPriority.isEmpty()) return null;
			PhysicsPixel first = byPriority.first();
			return first.color;
		}

		void addPixel(PhysicsPixel p) {
			byLayer.computeIfAbsent(p.collisionLayer, k -> new HashSet<>()).add(p);
			byPriority.add(p);
		}
		void removePixel(PhysicsPixel p) {
			byLayer.get(p.collisionLayer).remove(p);//TODO remove sub-bucket
			byPriority.remove(p);
		}
		Set<PhysicsPixel> collision(int collisionLayer) {
			Set<PhysicsPixel> ret = byLayer.get(collisionLayer);
			return ret == null ? Collections.emptySet() : ret;
		}
	}
	
	//a stored call to move
	private static class QueueMove {
		final PhysicsPixel pixel;
		final Coord pos;
		QueueMove(PhysicsPixel pixel, Coord pos) {
			this.pixel = pixel;
			this.pos = pos;
		}
	}

	private Map<Coord, Bucket> pixelsByCoord; //TODO integer overflow
	private Map<PhysicsPixel, Coord> coordsByPixel;
	private LinkedList<QueueMove> addQueue;
	private LinkedList<PhysicsPixel> removeQueue;
	private LinkedList<QueueMove> moveQueue;
	private final Player player;
	private Phase phase;

	public Model() {
		pixelsByCoord = new HashMap<>();
		coordsByPixel = new HashMap<>();
		addQueue = new LinkedList<>();
		removeQueue = new LinkedList<>();
		moveQueue = new LinkedList<>();
		player = new Player(this, 0, 0, 1);
		phase = /*/new Phase(this, 1);/*/new FacePhase(this, 1, null, null, new FloatCoord(0, 0), 0);/**/
		add(new DeadlyTrail(this, new Coord(1, 1), Color.red, 10000), new Coord(1,1));
	}

	public void tick(IController controller, IRenderer renderer) {
		phase = phase.tick();
		player.move(controller, renderer);

		coordsByPixel.forEach((p, c) -> p.tick());

		HashSet<PhysicsPixel> moved = new HashSet<>();
		int length = addQueue.size();
		for (int i = 0; i < length; i++) {
			QueueMove pop = addQueue.pop();
			if(moved.add(pop.pixel)) add(pop.pixel, pop.pos);
		}
		length = removeQueue.size();
		for (int i = 0; i < length; i++) {
			PhysicsPixel p = removeQueue.pop();
			if(moved.add(p)) remove(p);
		}
		length = moveQueue.size();
		for (int i = 0; i < length; i++) {
			QueueMove pop = moveQueue.pop();
			if(moved.add(pop.pixel)) move(pop.pixel, pop.pos);
		}
	}
	public void render(IRenderer renderer) {
		renderer.clear();
		renderer.setCenter(new Coord(player.getCameraPos()));
		pixelsByCoord.forEach((coord, bucket) -> renderer.draw(coord, bucket.render(), 1));
		renderer.render();
	}

	void add(PhysicsPixel p, Coord pos) {
		coordsByPixel.put(p, pos);
		pixelsByCoord.computeIfAbsent(pos, k -> new Bucket(pos)).addPixel(p);
	}
	void remove(PhysicsPixel p) {
		Coord pos = coordsByPixel.get(p);
		if(pos == null) throw new IllegalStateException("attempted to remove untracked pixel");
		coordsByPixel.remove(p);
		pixelsByCoord.get(pos).removePixel(p);//TODO remove bucket
	}
	void move(PhysicsPixel p, Coord to) {
		remove(p);
		add(p, to);
	}
	void queueAdd(PhysicsPixel p, Coord pos) {
		addQueue.push(new QueueMove(p, pos));
	}
	void queueRemove(PhysicsPixel p) {
		removeQueue.push(p);
	}
	void queueMove(PhysicsPixel p, Coord to) {
		moveQueue.push(new QueueMove(p, to));
	}

	Set<PhysicsPixel> collision(Coord pos, int collisionLayer) {
		Bucket b = pixelsByCoord.get(pos);
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
			FloatCoord mouse = renderer.mousePosition();
			FloatCoord vel = Utility.normalize(new FloatCoord(mouse.x - x, mouse.y - y));
			Coord pos = new Coord(getCameraPos());
			model.add(new StandardGoodProjectile(model, new FloatCoord(pos), 500, vel.x, vel.y), pos);
		}
	}
	FloatCoord getCameraPos() {
		return new FloatCoord(x, y);
	}
	FloatCoord getTargetPos() {
		return new FloatCoord(x, y);
	}
}

