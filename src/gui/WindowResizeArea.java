package gui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.geom.Line2D;
import java.awt.geom.Path2D;

public class WindowResizeArea extends Element {
	private boolean drag;

	/* addX and addY are always negative and represent the
	 * gap between dragging point and the bottom-right
	 * corner of the window to adjust window size correctly
	 */
	private int addX, addY;

	private HoverCalc hoverCalc, holdCalc;

	/* This element is special because it is displayed atop
	 * all scenes and isn't attached to a particular one,
	 * which means it's scene field is null. Repainting and
	 * accessing container size is done directly using window instance.
	 */
	private Window containerWindow;
	private JFrame frame; // frame instance is used to resize window directly

	protected WindowResizeArea(Window containerWindow, JFrame f) {
		super(null, new Bounds(0, 0, 60, 60, 1, 1));
		frame = f;
		this.containerWindow = containerWindow;
		Runnable repaint = () -> containerWindow.repaint(getBounds().getRectangle());
		hoverCalc = new HoverCalc(100, repaint);
		holdCalc = new HoverCalc(200, repaint);
	}

	@Override
	protected void paint(Graphics2D g) {
		double colorHover = hoverCalc.getCubicOut();
		double colorHold = holdCalc.getCubicOut();

		double positionPhase = hoverCalc.getCubicInOut();

		int maxAlphaHover = 70, maxAlphaHold = 110;
		double alpha = maxAlphaHover * colorHover + (maxAlphaHold - maxAlphaHover) * colorHold;

		g.setColor(new Color(255, 255, 255, (int) alpha));
		double offset = positionPhase * 2 + 10; // offset towards left-top
		paintResizeIcon(g, x() + w() - offset, y() + h() - offset);
	}

	private void paintResizeIcon(Graphics2D g, double x, double y) {
		// settings of arrow figure
		int arrLen = 10, arrThc = 3, arrDist = 27;

		int n = 14; // arrow polygon based on 3 setting values
		double[] polyXs = {0, -arrLen, -arrLen, -2 * arrThc, -arrDist + arrThc, -arrDist + arrThc, -arrDist,
				-arrDist, -arrDist + arrLen, -arrDist + arrLen, -arrDist + 2 * arrThc, -arrThc, -arrThc, 0};
		double[] polyYs = {0, 0, -arrThc, -arrThc, -arrDist + 2 * arrThc, -arrDist + arrLen, -arrDist + arrLen,
				-arrDist, -arrDist, -arrDist + arrThc, -arrDist + arrThc, -2 * arrThc, -arrLen, -arrLen};

		for (int i = 0; i < n; i++) { // shift points accordingly to arguments
			polyXs[i] += x;
			polyYs[i] += y;
		}

		// create 2D figure
		Path2D path = new Path2D.Double(Path2D.WIND_NON_ZERO, n);
		for (int i = 1; i < n; i++)
			path.append(new Line2D.Double(polyXs[i - 1], polyYs[i - 1], polyXs[i], polyYs[i]), true);

		g.fill(path);
	}

	@Override
	protected void mousePressed(MouseEvent e) {
		if (!SwingUtilities.isLeftMouseButton(e))
			return; // only left mouse button
		if (!contains(e.getPoint()) || containerWindow.fullscreen)
			return; // if outside bounds or fullscreen, ignore

		drag = true;
		addX = frame.getWidth() - e.getPoint().x;
		addY = frame.getHeight() - e.getPoint().y;
		holdCalc.setHovered(true);
	}

	@Override
	protected void mouseMoved(MouseEvent e) {
		boolean showResizeIcon;

		if (containerWindow.fullscreen) {
			showResizeIcon = false; // if fullscreen, no resize available
		} else {
			/* mouse might be outside of bounds but still dragging
			 * because window size can't be under a value set in Window
			 * if drag is true, still display icon in any case
			 */
			showResizeIcon = contains(e.getPoint()) || drag;
		}

		hoverCalc.setHovered(showResizeIcon);
	}

	@Override
	protected void mouseDragged(MouseEvent e) {
		mouseMoved(e);

		// if mouse started dragging from within the bounds, ignore
		if (!drag)
			return;

		int w = e.getX() + addX, h = e.getY() + addY;
		Dimension min = frame.getMinimumSize();
		frame.setSize(Math.max(w, min.width), Math.max(h, min.height));
	}

	@Override
	protected void mouseReleased(MouseEvent e) {
		if (SwingUtilities.isLeftMouseButton(e)) {
			// if mouse is outside bounds after resizing, disable resize icon
			hoverCalc.setHovered(contains(e.getPoint()));

			drag = false;
			holdCalc.setHovered(false);
		}
	}
}
