package model;

import java.util.Objects;

public class FloatCoord {
	public final float x, y;
	public FloatCoord(float x, float y){
		this.x = x;
		this.y = y;
	}
	public FloatCoord(Coord copy){
		this(copy.x, copy.y);
	}
	@Override
	public boolean equals(Object other){
		if(other == null || other.getClass() != getClass()) return false;
		FloatCoord pother = (FloatCoord)other;
		return Math.abs(pother.x - x) < 0.0001 && Math.abs(pother.y - y) < 0.0001;
	}
	@Override
	public int hashCode(){
		return Objects.hash(x, y);
	}
	@Override
	public String toString(){
		return "(" + x + ", " + y + ")";
	}
}
