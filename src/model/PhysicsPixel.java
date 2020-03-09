package model;

import java.awt.Color;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.function.Function;

class PhysicsPixel {
	//package-private variables should be open to access by other classes
	Coord pos;
	//pos should only be changed by the model, not by a pixel itself
	Color color;
	int priority;
	int collisionLayer;
	
	//protected is used to mark that only subclasses should access (think c++ protected)
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
		System.out.println("ded");
	}
	
//	@Override
//	public boolean equals(Object other){
//		return this == other;
//	}
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

abstract class TrailingPixel extends TrailPixel {
	protected FloatCoord truePos;
	TrailingPixel(Coord pos, Color color, int priority, int collisionLayer, int life){
		super(pos, color, priority, collisionLayer, life);
		this.truePos = new FloatCoord(pos);
	}
	@Override
	void tick(Model model){
		super.tick(model);
		if(!dead) {
			FloatCoord oldPos = truePos;
			FloatCoord newPos = move();
			Coord newIntPos = new Coord(newPos);
			if (!pos.equals(newIntPos)) {
				float distX = (newPos.x - oldPos.x);
				float distY = (newPos.y - oldPos.y);
				float distance = (float)Math.sqrt(distX * distX + distY * distY);
				float incX = distX / distance;
				float incY = distY / distance;
				float x = oldPos.x;
				float y = oldPos.y;

				for (int i = 0; i <= distance; i++) {
					x += incX;
					y += incY;
					passThrough(model, new Coord(new FloatCoord(x, y)));
					if (dead) break;
				}

				if (!dead) {
					truePos = newPos;
					model.queueMove(this, newIntPos);
				}
			}
		}
	}
	
	protected abstract void passThrough(Model model, Coord pos);

	protected abstract FloatCoord move();
}