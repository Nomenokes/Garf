package render;

import model.Coord;

import java.awt.*;
import java.util.HashMap;
import java.util.Map;

public interface IRenderer {
	public void setCenter(Coord center);
	public void draw(Map<Coord, Color> pixels, int priority);
	public void draw(Coord coord, Color color, int priority);
	public void render();
	public void clear();
}
