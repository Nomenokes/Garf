package render;

import model.Coord;
import model.FloatCoord;

import java.awt.*;
import java.util.HashMap;
import java.util.Map;

public interface IRenderer {
	public void setCenter(Coord center);
	public void draw(Map<Coord, Color> pixels, int priority);
	public void draw(Coord coord, Color color, int priority);
	public void render();
	/**
	 * Signal the renderer to prepare for a new frame to be rendered. Any calls to {@link draw} between calling this method and calling {@link render} should be rendered on the new frame.
	 */
	public void clear();
	public FloatCoord transformScreenPoint(Point point);
	//TODO find a better place for turning screen coordinates into game coordinates (maybe keep track in model? maybe restrict clicks to directional only?)
}
