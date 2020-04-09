package model;

import main.Utility;

import java.awt.Color;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;
import java.util.function.BiConsumer;

class Phase {
	protected Model model;
	protected int difficulty;
	Phase(Model model, int difficulty){
		this.difficulty = difficulty;
		this.model = model;
	}
	Phase tick() { return this; }
}

abstract class TimedPhase extends Phase{
	protected int time;
	TimedPhase(Model model, int difficulty, int time) {
		super(model, difficulty);
		this.time = time;
	}
	@Override
	Phase tick(){
		time--;
		tickInternal();
		if(time < 0) return nextPhase();
		return this;
	}
	void tickInternal(){}
	abstract Phase nextPhase();
}

abstract class TransitionPhase extends TimedPhase {

	TransitionPhase(Model model, int difficulty, int time) {
		super(model, difficulty, time);
	}
}

//class FaceMouthTransition extends TransitionPhase {
//
//	FaceMouthTransition(int difficulty, FacePhase from) {
//		super(difficulty, time);
//	}
//	
//	@Override
//	Phase nextPhase() {
//		return new 
//	}
//}
//
//class MouthFaceTransition extends TransitionPhase {
//	
//}

class FacePhase extends TimedPhase implements PixelRotator.RotationalSuperPosition {
	static class FaceReader implements Utility.PixelReader {
		private LinkedList<BiConsumer<Model, PixelRotator.RotationalSuperPosition>> pixels = new LinkedList<>();
		private Set<Integer> trailing = new HashSet<>();
		private int height;
		@Override
		public void init(int width, int height) {
			this.height = height;
		}
		@Override
		public void add(int x, int y, Color color) {
			if (!trailing.contains(y)) {
				trailing.add(y);
				pixels.push((model, rot) -> model.add(new TrailingFacePixel(
						model,
						new FloatCoord(x, y),
						color,
						FACE_LIFE,
						rot,
						SPIKE_PERIOD,
						Math.abs((float)y / height),
						TRAIL_LENGTH,
						TRAIL_ADD
				), new Coord(x, y)));
			} else {
				pixels.push((model, rot) -> model.add(new FacePixel(
						model,
						new FloatCoord(x, y),
						color,
						FACE_LIFE,
						rot
				), new Coord(x, y)));
			}
//			pixels.push(rot -> new StandardEvilProjectile(new Coord(x, y), 1000, 0.2f, 0.2f));
		}

		void create(Model model, PixelRotator.RotationalSuperPosition caller) {
			pixels.forEach(f -> f.accept(model, caller));
		}
	}
	private static FaceReader FACE_CREATOR = new FaceReader();
	static { Utility.initializeTexture("/phases/face.png", FACE_CREATOR); }
	private static final int TRAIL_LENGTH = 60 * 8, TRAIL_ADD = 60 * 2, SPIKE_PERIOD = 5, FACE_LIFE = 12000;
	
	protected Eye left, right;
	protected FloatCoord center;
	protected double rotation;
	FacePhase(Model model, int difficulty, Eye left, Eye right, FloatCoord center, double rotation) {
		super(model, difficulty, FACE_LIFE);
		this.left = left;
		this.right = right;
		this.center = center;
		this.rotation = rotation;
		FACE_CREATOR.create(model, this);
	}

	@Override
	void tickInternal(){
		center = new FloatCoord(center.x + .1f, center.y);
	}

	@Override
	Phase nextPhase() {
		return new FacePhase(model, difficulty, left, right, center, rotation);
	}
	@Override
	public FloatCoord center() { return center; }
	@Override
	public double rotation() { return rotation; }
}


//class MouthPhase extends TimedPhase {
//	
//}