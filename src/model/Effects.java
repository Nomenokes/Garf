package model;

import java.awt.Color;
import java.util.LinkedList;
import java.util.List;

abstract class Projectile extends TrailingPixel {
	protected List<Integer> hitLayers;
	private LinkedList<PhysicsPixel> hitTotal;

	Projectile(Coord pos, Color color, int priority, int collisionLayer, int life, List<Integer> hitLayers) {
		super(pos, color, priority, collisionLayer, life);
		this.hitLayers = hitLayers;
	}

	@Override
	void tick(Model model){
		hitTotal = new LinkedList<>();
		checkHit(model, pos);
		super.tick(model);
		if(!hitTotal.isEmpty()) hit(model, hitTotal);
	}

	private void checkHit(Model model, Coord pos){
		hitLayers.forEach(layer -> hitTotal.addAll(model.collision(pos, layer)));
	}

	@Override
	protected void passThrough(Model model, Coord pos){
		checkHit(model, pos);
	}

	/**
	 *
	 * @param hit called in order of collision layers, no specific ordering within each layer. Can be mutated (pop recommended).
	 */
	protected abstract void hit(Model model, LinkedList<PhysicsPixel> hit);
}

abstract class EvilProjectile extends Projectile{
	EvilProjectile(Coord pos, Color color, int priority, int life) {
		super(pos, color, priority, Model.LAYER_EVIL_PROJECTILE, life, new LinkedList<Integer>(){{
			push(Model.LAYER_GOOD_PLAYER);
		}});
	}
	@Override
	protected void hit(Model model, LinkedList<PhysicsPixel> hit){
		hit.pop().die(model);
		this.die(model);
	}
}
class StandardEvilProjectile extends EvilProjectile {
	protected float vX, vY;
	StandardEvilProjectile(Coord pos, int life, float vX, float vY) {
		super(pos, Color.ORANGE, 5, life);
		this.vX = vX;
		this.vY = vY;
	}
	@Override
	protected void passThrough(Model model, Coord pos){
		super.passThrough(model, pos);
		model.queueAdd(new EvilTrail(pos, (int)(Math.random() * 20 + 20)));
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

class DeadlyTrail extends EvilProjectile {
	DeadlyTrail(Coord pos, Color color, int life) {
		super(pos, color, 4, life);
	}
	@Override
	protected FloatCoord move() {
		return truePos;
	}
}


class Eye {
	
}

class PixelRotator {
	static interface RotationalSuperPosition {
		FloatCoord center();
		double rotation();
	}
	
	private final double rotation;
	private final float radius;
	private RotationalSuperPosition rotationalGetter;

	PixelRotator(Coord pos, RotationalSuperPosition rotationalGetter) {
		this.rotationalGetter = rotationalGetter;
		FloatCoord relative = new FloatCoord(pos.x - rotationalGetter.center().x, pos.y - rotationalGetter.center().y);
		this.rotation = Math.atan2(relative.y, relative.x) - rotationalGetter.rotation();
		this.radius = (float)Math.sqrt(relative.x * relative.x + relative.y * relative.y);
	}
	PixelRotator(boolean relative, Coord pos, RotationalSuperPosition rotationalGetter){
		this(pos, new RotationalSuperPosition(){
			@Override
			public double rotation() {
				return 0;
			}
			@Override
			public FloatCoord center() {
				return new FloatCoord(0, 0);
			}
		});
		this.rotationalGetter = rotationalGetter;
	}
	
	FloatCoord move(){
		float rotationX = (float)Math.cos(rotationalGetter.rotation() + rotation) * radius;
		float rotationY = (float)Math.sin(rotationalGetter.rotation() + rotation) * radius;
		return new FloatCoord(rotationalGetter.center().x + rotationX, rotationalGetter.center().y + rotationY);
	}
}

class FacePixel extends EvilProjectile {
	private PixelRotator rotator;
	private FacePixel(Color color, int life, PixelRotator rotator){
		super(new Coord(rotator.move()), color, 3, life);
		this.rotator = rotator;
	}
	FacePixel(Coord relative, Color color, int life, PixelRotator.RotationalSuperPosition rotationalGetter) {
		this(color, life, new PixelRotator(true, relative, rotationalGetter));
	}
	@Override
	protected FloatCoord move() {
		return rotator.move();
	}
}
class TrailingFacePixel extends FacePixel {
	private final float threshold;
	private final int period, trail;
	private int passed;
	TrailingFacePixel(Coord pos, Color color, int life, PixelRotator.RotationalSuperPosition rotationalGetter, int period, float distance, int trail) {
		super(pos, color, life, rotationalGetter);
		this.threshold = distance / 2 + 0.5f;
		this.period = period;
		this.trail = (int)(trail * (1 - threshold));
		this.passed = 0;
	}

	@Override
	protected void passThrough(Model model, Coord pos) {
		super.passThrough(model, pos);
		passed++;
		//if(Math.cos((float)passed / period) > threshold){
			model.queueAdd(new DeadlyTrail(pos, color, trail));
		//}
	}
}