package gui;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;

public abstract class Element {
	private final Scene container;
	private final Bounds bounds;

	public Element(Scene container, Bounds bounds) {
		this.container = container;
		this.bounds = bounds;
	}

	protected abstract void paint(Graphics2D g);

	protected final Scene getContainer() {
		return container;
	}

	protected final void onContainerSizeChange(Dimension newContainerSize) {
		bounds.onContainerSizeChange(newContainerSize);
	}

	protected final int x() {
		return bounds.getX();
	}

	protected final int y() {
		return bounds.getY();
	}

	protected final int w() {
		return bounds.getWidth();
	}

	protected final int h() {
		return bounds.getHeight();
	}

	protected final Bounds getBounds() {
		return bounds;
	}

	protected final boolean contains(Point p) {
		return bounds.contains(p);
	}

	protected final Point getMousePosition() {
		return container.getMousePosition();
	}

	protected final void repaint() {
		container.repaint(bounds.getRectangle());
	}

	protected final void repaint(Rectangle area) {
		container.repaint(area);
	}

	// 9 Swing listeners
	protected void mousePressed(MouseEvent e) {
	}

	protected void mouseReleased(MouseEvent e) {
	}

	protected void mouseClicked(MouseEvent e) {
	}

	protected void mouseMoved(MouseEvent e) {
	}

	protected void mouseDragged(MouseEvent e) {
	}

	protected void mouseWheelMoved(MouseEvent e) {
	}

	protected void keyPressed(KeyEvent e) {
	}

	protected void keyReleased(KeyEvent e) {
	}

	protected void keyTyped(KeyEvent e) {
	}

	public static int centerStringX(Graphics2D g, String s, int x) {
		return x - g.getFontMetrics().stringWidth(s) / 2;
	}

	public static int centerStringY(Graphics2D g, String s, int y) {
		FontMetrics fm = g.getFontMetrics();
		return y + (fm.getAscent() - fm.getDescent()) / 2;
	}
}
