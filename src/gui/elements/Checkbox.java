package gui.elements;

import gui.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.geom.*;

public class Checkbox extends Element {
	// corners round arc
	private static final int RND_CORNERS = 8;
	// amount of pixels to expand each side when hovering
	private static final int EXPAND = 2;

	private int checkboxSize = 16; // side of square
	private int checkboxX, checkboxY; // square offset from bounds start

	private String text;
	private boolean checked;
	private HoverCalc hoverCalc;
	private HoverCalc activationCalc;
	private boolean hold;
	private Area textArea;

	public Checkbox(String text, Scene container, Bounds bounds) {
		super(container, bounds);
		hoverCalc = new HoverCalc(120, this);
		activationCalc = new HoverCalc(160, this);

		this.text = text;
		checkboxX = EXPAND + 1; // gap distance from left bounds side to fit when expanding outline
		checkboxY = (h() - checkboxSize) / 2;
	}

	@Override
	protected void paint(Graphics2D g) {
		g.setColor(Theme.getFG());
		g.setFont(Theme.getUIFont());
		if (textArea == null)
			textArea = getTextArea(text, 10 + checkboxX + checkboxSize, centerStringY(g, h() / 2), g);
		g.fill(textArea.createTransformedArea(AffineTransform.getTranslateInstance(x(), y())));

		paintBorderAndFill(g);

		double activationPhase = activationCalc.getPhase();

		if (activationPhase != 1) {
			double fillInsideAlpha = HoverCalc.easeSine(activationPhase);
			g.setComposite(AlphaComposite.getInstance(AlphaComposite.DST_OUT, 1f - (float) fillInsideAlpha));
			g.fill(getCheckboxRound()); // cut inner area accordingly to activationCalc
		}

		if (activationPhase != 0) {
			// part of mark to paint, starting from left and finishing at right
			double markRatio = HoverCalc.easeCubicInOut(activationPhase);
			g.setComposite(AlphaComposite.DstOut); // fill mark as 0 alpha color
			g.fill(getMarkArea(markRatio));
		}

		g.setComposite(AlphaComposite.SrcOver); // reset composite for further use by other elements
	}

	private void paintBorderAndFill(Graphics2D g) {
		double hoverPhase = hoverCalc.getCubicOut();
		// when mouse hovers the checkbox, it expands
		float stokeOuterWidth = (float) (EXPAND * hoverPhase + 1);

		// stroke width is multiplied by 2 because it counts for both inner and outer
		Stroke outerStroke = new BasicStroke(stokeOuterWidth * 2f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_ROUND);
		RoundRectangle2D checkboxRound = getCheckboxRound();

		Area checkboxOuterArea = new Area(outerStroke.createStrokedShape(checkboxRound));
		checkboxOuterArea.add(new Area(checkboxRound)); // fill box middle
		g.fill(checkboxOuterArea);
	}

	private Shape getMarkArea(double part) { // returns mark area, expanding it from left to right
		float markThickness = 1.5f;

		int startX = x() + checkboxX, startY = y() + checkboxY;
		Point2D point1 = new Point2D.Double(startX + checkboxSize * .2, startY + checkboxSize * .55);
		Point2D point2 = new Point2D.Double(startX + checkboxSize * .4, startY + checkboxSize * .75);
		Point2D point3 = new Point2D.Double(startX + checkboxSize * .8, startY + checkboxSize * .30);
		Path2D path = getCutPath(new Point2D[]{point1, point2, point3}, part);

		Stroke stroke = new BasicStroke(markThickness, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
		return stroke.createStrokedShape(path); // give path a BasicStroke outline
	}

	// cuts a path so that only 'part' fraction of its length is left starting from array start
	private Path2D getCutPath(Point2D[] path, double part) {
		Path2D result = new Path2D.Double(Path2D.WIND_NON_ZERO, path.length);

		if (part == 1) {
			for (int i = 1; i < path.length; i++)
				result.append(new Line2D.Double(path[i - 1], path[i]), true);
			return result;
		}

		double len = 0;
		double[] dist = new double[path.length - 1];
		for (int i = 0; i < path.length - 1; i++) {
			dist[i] = path[i + 1].distance(path[i]);
			len += dist[i]; // find total length
		}
		len *= part; // find length needed

		for (int i = 0; len != 0; i++) {
			if (len >= dist[i]) // if it's not the last segment of path, insert full line
				result.append(new Line2D.Double(path[i], path[i + 1]), true);
			else {
				// only insert part of last segment
				double finalPart = len / dist[i];
				Point2D mid = new Point2D.Double((1 - finalPart) * path[i].getX() + finalPart * path[i + 1].getX(),
						(1 - finalPart) * path[i].getY() + finalPart * path[i + 1].getY());
				result.append(new Line2D.Double(path[i], mid), true);
				break;
			}
			len -= dist[i];
		}
		return result;
	}

	private RoundRectangle2D.Double getCheckboxRound() {
		return new RoundRectangle2D.Double(x() + checkboxX, y() + checkboxY,
				checkboxSize, checkboxSize, RND_CORNERS, RND_CORNERS);
	}

	private void click() {
		checked = !checked;
	}

	@Override
	protected void mousePressed(MouseEvent e) {
		if (!SwingUtilities.isLeftMouseButton(e))
			return;
		if (!contains(e.getPoint()))
			return;
		hold = true; // remember if holding inside buttons
	}

	@Override
	protected void mouseReleased(MouseEvent e) {
		if (!SwingUtilities.isLeftMouseButton(e))
			return;
		if (hold) { // only perform click if mouse originally pressed inside bounds
			hold = false;
			click();
			activationCalc.setHovered(checked);
		}
	}

	@Override
	protected void mouseMoved(MouseEvent e) {
		hoverCalc.setHovered(contains(e.getPoint()));
	}

	@Override
	protected void mouseDragged(MouseEvent e) {
		mouseMoved(e);
		if (!SwingUtilities.isLeftMouseButton(e))
			return;
		if (!contains(e.getPoint())) {
			hold = false; // when dragging outside bounds, lose focus (hold)
		}
	}

	@Override
	protected void onDisplay() {
		hoverCalc.setDisplayed(true);
		activationCalc.setDisplayed(true);
	}

	@Override
	protected void onShut() {
		hoverCalc.shut();
		activationCalc.shut();
		hold = false;
	}
}
