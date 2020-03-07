package model;

import java.awt.Color;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.function.Function;

class PhysicsPixel {
	Coord pos;
	Color color;
	int priority;
	int collisionLayer;
	
	protected boolean dead;
	
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
		this.truePos = new FloatCoord(pos);
	}

	@Override
	void tick(Model model) {
		super.tick(model);
		FloatCoord oldPos = truePos;
		FloatCoord newPos = move();
		Coord newIntPos = new Coord(newPos);
		if (!dead && !pos.equals(newIntPos)) {
			float distX = (newPos.x - oldPos.x);
			float distY = (newPos.y - oldPos.y);
			float distance = (float)Math.sqrt(distX * distX + distY * distY);
			float incX = distX / distance;
			float incY = distY / distance;
			float x = oldPos.x;
			float y = oldPos.y;
			
			for(int i = 0; i <= distance; i++){
				x += incX;
				y += incY;
				check(model, new Coord(new FloatCoord(x, y)));
				if(dead) break;
			}
			
			if(!dead){
				truePos = newPos;
				model.queueMove(this, newIntPos);
			}
		}
	}
	private void check(Model model, Coord pos){
		LinkedList<PhysicsPixel> hitTotal = new LinkedList<>();
		for(Integer layer : hitLayers){
			hitTotal.addAll(model.collision(pos, layer));
		}
		if(!hitTotal.isEmpty()) hit(model, hitTotal);
		else passThrough(model, pos);
	}

	protected void passThrough(Model model, Coord pos) {}
	/**
	 * 
	 * @param hit called in order of collision layers, no specific ordering within each layer. Can be mutated (pop recommended).
	 */
	protected void hit(Model model, LinkedList<PhysicsPixel> hit) {}
	protected FloatCoord move() {
		return truePos;
	}
}

class StandardEvilProjectile extends Projectile {
	protected float vX, vY;
	StandardEvilProjectile(Coord pos, float vX, float vY, int life) {
		super(pos, Color.ORANGE, 5, Model.LAYER_EVIL_PROJECTILE, life, new LinkedList<Integer>(){{ 
			push(Model.LAYER_GOOD_PLAYER); 
			push(Model.LAYER_GOOD_BULLET);
		}});
		this.vX = vX;
		this.vY = vY;
	}
	@Override
	protected void passThrough(Model model, Coord pos){
		model.queueAdd(new EvilTrail(pos, (int)(Math.random() * 20 + 20)));
	}
	@Override
	protected void hit(Model model, LinkedList<PhysicsPixel> hit){
		hit.pop().die(model);
		this.die(model);
	}
	@Override
	protected FloatCoord move() {
		return new FloatCoord(truePos.x + vX, truePos.y + vY);
	}
}

class EvilTrail extends TrailPixel {
	EvilTrail(Coord pos, int life) {
		super(pos, Color.BLACK, 2, Model.LAYER_EVIL_TRAIL, life);
	}
}
