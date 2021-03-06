package gui;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;

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

	// enable animations that were possibly disabled in onShut
	protected void onDisplay() {
	}

	// when scene changes and the element is no longer on screen, it should stop all animations
	protected void onShut() {
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

	public static double centerStringX(Graphics2D g, String s, int x) {
		return alignStringX(g, s, x, 0, 0);
	}

	public static int centerStringY(Graphics2D g, int y) {
		FontMetrics fm = g.getFontMetrics();
		return y + (fm.getAscent() - fm.getDescent()) / 2;
	}

	public static double alignStringX(Graphics2D g, String s, float x, float w, int align) {
		switch (align) {
			case -1:
				return x;
			case 0:
				return x + (w - stringWidth(g, s)) / 2f;
			case 1:
				return x + w - stringWidth(g, s);
			default:
				throw new IllegalArgumentException("for align " + align);
		}
	}

	public static double stringWidth(Graphics2D g, String s) {
		return stringWidth(s, g.getFont());
	}

	public static double stringWidth(String s, Font font) {
		return font.getStringBounds(s, Window.fontRenderContext).getWidth();
	}

	// getTextArea should be preferably used because it behaves independently from graphics transform
	public static Area getTextArea(String text, double x, double y, Graphics2D g) {
		return getTextArea(text, x, y, g.getFont());
	}

	public static Area getTextArea(String text, double x, double y, Font f) {
		Area area = new Area(f.createGlyphVector(Window.fontRenderContext, text).getOutline());
		area.transform(AffineTransform.getTranslateInstance(x, y));
		return area;
	}
}
