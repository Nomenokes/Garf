package model;

import java.util.Objects;

public class Coord {
	public final int x, y;
	public Coord(int x, int y){
		this.x = x;
		this.y = y;
	}
	public Coord(FloatCoord copy){
		this(Math.round(copy.x), Math.round(copy.y));
	}
	@Override
	public boolean equals(Object other){
		if(other == null || other.getClass() != getClass()) return false;
		Coord pother = (Coord)other;
		return pother.x == x && pother.y == y;
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