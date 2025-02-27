package model;

import main.Utility;

import java.awt.Color;
import java.util.LinkedList;
import java.util.List;

abstract class Projectile extends TrailingPixel {
	protected List<Integer> hitLayers;
	private LinkedList<PhysicsPixel> hitTotal;

	Projectile(Model model, FloatCoord pos, Color color, int priority, int collisionLayer, int life, List<Integer> hitLayers) {
		super(model, pos, color, priority, collisionLayer, life);
		this.hitLayers = hitLayers;
	}

	@Override
	void tick(){
		hitTotal = new LinkedList<>();
		super.tick();
		if(!hitTotal.isEmpty()) hit(hitTotal);
	}

	private void checkHit(Coord pos){
		hitLayers.forEach(layer -> hitTotal.addAll(model.collision(pos, layer)));
	}

	@Override
	protected void passThrough(Coord pos){
		checkHit(pos);
	}

	/**
	 *
	 * @param hit called in order of collision layers, no specific ordering within each layer. Can be mutated (pop recommended).
	 */
	protected abstract void hit(LinkedList<PhysicsPixel> hit);
}

abstract class GoodProjectile extends Projectile {
	GoodProjectile(Model model, FloatCoord pos, Color color, int priority, int life) {
		super(model, pos, color, priority, Model.LAYER_GOOD_BULLET, life, new LinkedList<Integer>(){{
			push(Model.LAYER_EVIL_PROJECTILE);
			push(Model.LAYER_EVIL_TRAIL);
		}});
	}
	private boolean hitInternal(PhysicsPixel hit){
		switch (hit.collisionLayer){
			case Model.LAYER_EVIL_PROJECTILE:
				return true;
			case Model.LAYER_EVIL_TRAIL:
				return false;
			default:
				throw new IllegalStateException("Good projectile collided with an illegal layer: " + hit.collisionLayer);
		}
	}
	@Override
	protected void hit(LinkedList<PhysicsPixel> hit){
		int length = hit.size();
		for(int i = 0; i < length; i++) {
			PhysicsPixel working = hit.pop();
			if(hitInternal(working)) this.die();
			working.die();
		}
	}
}
class StandardGoodProjectile extends GoodProjectile{
	protected float vX, vY;
	StandardGoodProjectile(Model model, FloatCoord pos, int life, float vX, float vY) {
		super(model, pos, Model.PRIORITY_GOOD_BLUE_PROJECTILE.color, Model.PRIORITY_GOOD_BLUE_PROJECTILE.priority, life);
		this.vX = vX;
		this.vY = vY;
	}
	@Override
	void tick(){
		super.tick();
	}
	@Override
	protected FloatCoord move() {
		return new FloatCoord(truePos.x + vX, truePos.y + vY);
	}
}

abstract class EvilProjectile extends Projectile{
	EvilProjectile(Model model, FloatCoord pos, Color color, int priority, int life) {
		super(model, pos, color, priority, Model.LAYER_EVIL_PROJECTILE, life, new LinkedList<Integer>(){{
			push(Model.LAYER_GOOD_PLAYER);
		}});
	}
	@Override
	protected void hit(LinkedList<PhysicsPixel> hit){
		hit.pop().die();
		this.die();
	}
}
class StandardEvilProjectile extends EvilProjectile {
	protected float vX, vY;
	StandardEvilProjectile(Model model, FloatCoord pos, int life, float vX, float vY) {
		super(model, pos, Model.PRIORITY_ORANGE_EVIL_PROJECTILE.color, Model.PRIORITY_ORANGE_EVIL_PROJECTILE.priority, life);
		this.vX = vX;
		this.vY = vY;
	}
	@Override
	void tick(){
		super.tick();
	}
	@Override
	protected void passThrough(Coord pos){
		super.passThrough(pos);
		model.queueAdd(new EvilTrail(model, (int)(Math.random() * 20 + 20)), pos);
	}
	@Override
	protected FloatCoord move() {
		return new FloatCoord(truePos.x + vX, truePos.y + vY);
	}
}

class EvilTrail extends TrailPixel {
	EvilTrail(Model model, int life) {
		super(model, Model.PRIORITY_BLACK_TRAIL.color, Model.PRIORITY_BLACK_TRAIL.priority, Model.LAYER_EVIL_TRAIL, life);
	}
}

class DeadlyTrail extends EvilProjectile {
	DeadlyTrail(Model model, Coord pos, Color color, int life) {
		super(model, new FloatCoord(pos), color, Model.PRIORITY_TEXTURE_PROJECTILE.priority, life);
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

	PixelRotator(FloatCoord pos, RotationalSuperPosition rotationalGetter) {
		this.rotationalGetter = rotationalGetter;
		FloatCoord relative = new FloatCoord(pos.x - rotationalGetter.center().x, pos.y - rotationalGetter.center().y);
		this.rotation = Math.atan2(relative.y, relative.x) - rotationalGetter.rotation();
		this.radius = Utility.magnitude(relative);
	}
	PixelRotator(boolean relative, FloatCoord pos, RotationalSuperPosition rotationalGetter){
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
	private FacePixel(Model model, FloatCoord pos, Color color, int life, PixelRotator rotator){
		super(model, pos, color, Model.PRIORITY_TEXTURE_PROJECTILE.priority, life);
		this.rotator = rotator;
	}
	FacePixel(Model model, FloatCoord relative, Color color, int life, PixelRotator.RotationalSuperPosition rotationalGetter) {
		this(model, relative, color, life, new PixelRotator(true, relative, rotationalGetter));
	}
	@Override
	protected FloatCoord move() {
		return rotator.move();
	}
}
class TrailingFacePixel extends FacePixel {
	private final float threshold, period;
	private final int trail, rand;
	private int passed;
	TrailingFacePixel(Model model, FloatCoord pos, Color color, int life, PixelRotator.RotationalSuperPosition rotationalGetter, float period, float distance, int trail, int rand) {
		super(model, pos, color, life, rotationalGetter);
		this.threshold = distance * 2 - 1.5f;
		this.period = (float)(period / Math.PI);
		this.trail = (int)(trail * (1 - threshold + 0.1));
		this.passed = 0;
		this.rand = rand;//(int)(rand * (1 - threshold));
	}

	@Override
	protected void passThrough(Coord pos) {
		super.passThrough(pos);
		passed++;
		if(Math.cos(passed / period) > threshold){
			model.queueAdd(new DeadlyTrail(model, pos, color, (int)(trail + rand * Math.random())), pos);
		}
	}
}