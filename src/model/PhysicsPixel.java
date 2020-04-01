package model;

import java.awt.Color;

class PhysicsPixel {
	//package-private variables should be open to access by other classes
	Color color;
	int priority;
	int collisionLayer;
	
	//protected is used to mark that only subclasses should access (think c++ protected)
	protected boolean dead;
	protected final Model model;
	
	
	PhysicsPixel(Model model, Color color, int priority, int collisionLayer) {
		this.model = model;
		this.color = color;
		this.priority = priority;
		this.collisionLayer = collisionLayer;
		this.dead = false;
	}
	
	void tick() {
		
	}
	void die() {
		if (!dead) model.queueRemove(this);
		dead = true;
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
	protected Coord roundedPos;
	TrailingPixel(Model model, FloatCoord pos, Color color, int priority, int collisionLayer, int life){
		super(model, color, priority, collisionLayer, life);
		this.truePos = pos;
		this.roundedPos = new Coord(pos);
	}
	@Override
	void tick(){
		super.tick();
		if(!dead) {
			FloatCoord oldTruePos = truePos;
			FloatCoord newTruePos = move();
			Coord newRoundedPos = new Coord(newTruePos);
			if (!roundedPos.equals(newRoundedPos)) {
				float distX = (newTruePos.x - oldTruePos.x);
				float distY = (newTruePos.y - oldTruePos.y);
				float distance = (float)Math.sqrt(distX * distX + distY * distY);
				float incX = distX / distance;
				float incY = distY / distance;
				float x = oldTruePos.x;
				float y = oldTruePos.y;

				for (int i = 0; i <= distance; i++) {
					x += incX;
					y += incY;
					passThrough(new Coord(new FloatCoord(x, y)));
					if (dead) break;
				}

				if (!dead) {
					model.queueMove(this, newRoundedPos);
				}
			}
			truePos = newTruePos;
			roundedPos = newRoundedPos;
		}
	}
	
	protected abstract void passThrough(Coord pos);

	protected abstract FloatCoord move();
}