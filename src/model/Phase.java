package model;

import main.Utility;

import java.awt.Color;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;
import java.util.function.Function;

abstract class Phase {
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
		private LinkedList<Function<PixelRotator.RotationalSuperPosition, FacePixel>> pixels = new LinkedList<>();
		private Set<Integer> trailing = new HashSet<>();
		private int width;
		@Override
		public void init(int width, int height) {
			this.width = width;
		}
		@Override
		public void add(int x, int y, Color color) {
			if (!trailing.contains(y)) {
				trailing.add(y);
				pixels.push(rot -> new TrailingFacePixel(
						new Coord(x, y),
						color,
						FACE_LIFE,
						rot,
						SPIKE_PERIOD,
						/**/0,/*/Math.abs((float)x / width),/**/
						(int)(TRAIL_LENGTH + TRAIL_ADD * Math.random())
				));
			} else {
				pixels.push(rot -> new FacePixel(
						new Coord(x, y),
						color,
						FACE_LIFE,
						rot
				));
			}
		}

		void create(Model model, PixelRotator.RotationalSuperPosition caller) {
			pixels.forEach(f -> model.add(f.apply(caller)));
		}
	}
	private static FaceReader FACE_CREATOR = new FaceReader();
	static { Utility.initializeTexture("/phases/face.png", FACE_CREATOR); }
	private static final int TRAIL_LENGTH = 60 * 10, TRAIL_ADD = 60, SPIKE_PERIOD = 5, FACE_LIFE = 12000;
	
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