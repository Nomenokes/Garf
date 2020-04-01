package model;

import java.awt.Color;

class PhysicsPixel {
	//package-private variables should be open to access by other classes
	Coord pos;
	Color color;
	int priority;
	int collisionLayer;
	
	//protected is used to mark that only subclasses should access (think c++ protected)
	protected boolean dead;
	protected final Model model;
	
	boolean removed;
	
	PhysicsPixel(Model model, Color color, int priority, int collisionLayer) {
		this.model = model;
		this.color = color;
		this.priority = priority;
		this.collisionLayer = collisionLayer;
		this.dead = false;
		removed = false;
	}
	
	void tick() {
		if(removed) System.out.println("t");
		if(dead) throw new IllegalStateException("A pixel has been ticked after dying");
	}
	void die() {
		if (!dead) model.queueRemove(this, pos);
		dead = true;
	}
	void moveTo(Coord pos){
		this.pos = pos;
	}
	
//	@Override
//	public boolean equals(Object other){
//		return this == other;
//	}
}

class TrailPixel extends PhysicsPixel {
	protected int life;
	TrailPixel(Model model, Color color, int priority, int collisionLayer, int life) {
		super(model, color, priority, collisionLayer);
		this.life = life;
	}

	@Override
	void tick() {
		super.tick();
		life--;
		if (life < 0) die();
	}
}

abstract class TrailingPixel extends TrailPixel {
	protected FloatCoord truePos;
	TrailingPixel(Model model, Color color, int priority, int collisionLayer, int life){
		super(model, color, priority, collisionLayer, life);
	}
	@Override
	void tick(){
		super.tick();
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
					passThrough(new Coord(new FloatCoord(x, y)));
					if (dead) break;
				}

				if (!dead) {
					model.queueMove(this, pos, newIntPos);
				}
			}
			truePos = newPos;
		}
	}
	@Override
	void moveTo(Coord pos){
		super.moveTo(pos);
		this.truePos = new FloatCoord(pos);
	}
	
	protected abstract void passThrough(Coord pos);

	protected abstract FloatCoord move();
}