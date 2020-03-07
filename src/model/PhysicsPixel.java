package model;

import java.awt.Color;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.function.Function;

class PhysicsPixel {
	Coord pos;
	Color color;
	int priority;
	int collisionLayer;
	boolean dead;
	PhysicsPixel(Coord pos, Color color, int priority, int collisionLayer) {
		if (pos == null) throw new IllegalArgumentException("pos is null");
		this.pos = pos;
		this.color = color;
		this.priority = priority;
		this.collisionLayer = collisionLayer;
		this.dead = false;
	}
	void tick(Model model) {}
	void die(Model model) {
		if (!dead) model.queueRemove(this);
		dead = true;
	}
}

class TrailPixel extends PhysicsPixel {
	protected int life;
	TrailPixel(Coord pos, Color color, int priority, int collisionLayer, int life) {
		super(pos, color, priority, collisionLayer);
		this.life = life;
	}

	@Override
	void tick(Model model) {
		super.tick(model);
		life--;
		if (life < 0) die(model);
	}
}

abstract class Projectile extends TrailPixel {
	protected FloatCoord truePos;
	protected List<Integer> hitLayers;
	Projectile(Coord pos, Color color, int priority, int collisionLayer, int life, List<Integer> hitLayers) {
		super(pos, color, priority, collisionLayer, life);
		this.hitLayers = hitLayers;
	}

	@Override
	void tick(Model model) {
		super.tick(model);
		truePos = move();
		Coord newPos = new Coord(truePos);
		if (!dead && !pos.equals(newPos)) {
			//float distance = 
			passThrough(pos);
			model.queueMove(this, newPos);
		}
	}

	protected void passThrough(Coord pos) {}
	protected void hit() {}
	protected FloatCoord move() {
		return truePos;
	}
}

class EvilProjectile extends Projectile {
	EvilProjectile(Coord pos, int life) {
		super(pos, Color.ORANGE, 5, Model.LAYER_EVIL_PROJECTILE, life, new LinkedList<Integer>(){{ 
			push(Model.LAYER_GOOD_PLAYER); 
			push(Model.LAYER_GOOD_BULLET);
		}});
	}
}

class EvilTrail extends TrailPixel {
	EvilTrail(Coord pos, int life) {
		super(pos, Color.BLACK, 2, Model.LAYER_EVIL_TRAIL, life);
	}
}
