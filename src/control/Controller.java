package control;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.Arrays;

public class Controller implements MouseListener, KeyListener, IController {
	private boolean m1, m2, l, r, u, d, space;
	private Component onto;

	public Controller(Component onto) {
		onto.addMouseListener(this);
		onto.addKeyListener(this);
		this.onto = onto;
		m1 = m2 = l = r = u = d = space = false;
	}

	@Override
	public void keyTyped(KeyEvent e) {

	}
	@Override
	public void keyPressed(KeyEvent e) {
		switch (e.getKeyCode()) {
			case KeyEvent.VK_A:
			case KeyEvent.VK_LEFT:
				l = true;
				break;
			case KeyEvent.VK_D:
			case KeyEvent.VK_RIGHT:
				r = true;
				break;
			case KeyEvent.VK_W:
			case KeyEvent.VK_UP:
				u = true;
				break;
			case KeyEvent.VK_S:
			case KeyEvent.VK_DOWN:
				d = true;
				break;
			case KeyEvent.VK_SPACE:
				space = true;
				break;
		}
	}
	@Override
	public void keyReleased(KeyEvent e) {
		switch (e.getKeyCode()) {
			case KeyEvent.VK_A:
			case KeyEvent.VK_LEFT:
				l = false;
				break;
			case KeyEvent.VK_D:
			case KeyEvent.VK_RIGHT:
				r = false;
				break;
			case KeyEvent.VK_W:
			case KeyEvent.VK_UP:
				u = false;
				break;
			case KeyEvent.VK_S:
			case KeyEvent.VK_DOWN:
				d = false;
				break;
			case KeyEvent.VK_SPACE:
				space = false;
				break;
		}
	}
	@Override
	public void mouseClicked(MouseEvent e) {

	}
	@Override
	public void mousePressed(MouseEvent e) {
		switch (e.getButton()) {
			case MouseEvent.BUTTON1:
				m1 = true;
				break;
			case MouseEvent.BUTTON2:
				m2 = true;
				break;
		}
	}
	@Override
	public void mouseReleased(MouseEvent e) {
		switch (e.getButton()) {
			case MouseEvent.BUTTON1:
				m1 = false;
				break;
			case MouseEvent.BUTTON2:
				m2 = false;
				break;
		}
	}
	@Override
	public void mouseEntered(MouseEvent e) {

	}
	@Override
	public void mouseExited(MouseEvent e) {

	}

	@Override
	public boolean left() { return l; }
	@Override
	public boolean right() { return r; }
	@Override
	public boolean up() { return u; }
	@Override
	public boolean down() { return d; }
	@Override
	public boolean mouse1() { return m1; }
	@Override
	public boolean mouse2() { return m2; }
	@Override
	public Point mousePos() {
		Point loc = MouseInfo.getPointerInfo().getLocation();
		Point sub = onto.getLocationOnScreen();
		return new Point(loc.x - sub.x, loc.y - sub.y);
	}
}
