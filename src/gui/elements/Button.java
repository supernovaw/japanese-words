package gui.elements;

import gui.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.geom.Area;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;

public class Button extends Element {
	// corners round arc
	private static final int RND_CORNERS = 12;
	// amount of pixels to expand each side when hovering
	private static final int EXPAND = 2;

	private String text;
	private Runnable run;
	private HoverCalc hoverCalc, holdCalc;

	public Button(String text, Runnable r, Scene container, Bounds bounds) {
		super(container, bounds);
		run = r;
		this.text = text;
		hoverCalc = new HoverCalc(120, this, EXPAND + 1);
		holdCalc = new HoverCalc(150, this, EXPAND);
	}

	private void click() {
		run.run();
	}

	@Override
	protected void paint(Graphics2D g) {
		g.setColor(Theme.getBG(50)); // fill background with this alpha
		g.fill(getButtonForm());

		g.setFont(Theme.getUIFont());
		g.setColor(Theme.getFG());

		double holdPhase = holdCalc.getCubicOut();
		double hoverPhase = hoverCalc.getCubicOut();

		// paint text and fill (in filled areas, text is engraved)
		Area area = new Area();
		if (holdPhase == 0) { // just paint button text
			area.add(getTextArea(g));
		} else { // fill button to animate holding
			Area textArea = getTextArea(g);

			double fillWidth = holdPhase * w() / 2;
			Rectangle2D.Double fillRect = new Rectangle2D.Double(x() + w() / 2d - fillWidth - 1,
					y() - 1, fillWidth * 2 + 1, h() + 1);
			Area fillArea = new Area(fillRect); // the rectangle which gets wider when holding

			Area innerText = new Area(textArea);
			innerText.intersect(fillArea); // the only part of text within animating rectangle

			area.add(textArea);
			area.add(fillArea);
			area.subtract(innerText);
		}
		// round corners and cut text in case it's out of bounds
		area.intersect(new Area(getButtonForm()));

		// button outline (1.0f is for not hovering)
		double strokeOuterWidth = hoverPhase * EXPAND + 1f;
		// stroke width is multiplied by 2 because it counts for both inner and outer
		Stroke stroke = new BasicStroke((float) strokeOuterWidth * 2f);
		Area outerStroke = new Area(stroke.createStrokedShape(getButtonForm()));
		// remove inner part of stroke
		outerStroke.subtract(new Area(getButtonForm())); // remove inner stroke
		area.add(outerStroke);

		g.fill(area);
	}

	private Area getTextArea(Graphics2D g) {
		return getTextArea(text, centerStringX(g, text, x() + w() / 2), centerStringY(g, y() + h() / 2), g);
	}

	private RoundRectangle2D.Double getButtonForm() {
		return new RoundRectangle2D.Double(
				x(), y(), w() - 1, h() - 1, RND_CORNERS, RND_CORNERS);
	}

	@Override
	protected void mousePressed(MouseEvent e) {
		if (!SwingUtilities.isLeftMouseButton(e))
			return;
		if (!getButtonForm().contains(e.getPoint()))
			return;
		holdCalc.setHovered(true);
	}

	@Override
	protected void mouseReleased(MouseEvent e) {
		if (!SwingUtilities.isLeftMouseButton(e))
			return;
		if (holdCalc.isHovered()) {
			holdCalc.setHovered(false);
			click();
		}
	}

	@Override
	protected void mouseMoved(MouseEvent e) {
		hoverCalc.setHovered(getButtonForm().contains(e.getPoint()));
	}

	@Override
	protected void mouseDragged(MouseEvent e) {
		mouseMoved(e);
		if (!SwingUtilities.isLeftMouseButton(e))
			return;
		if (!getButtonForm().contains(e.getPoint())) {
			holdCalc.setHovered(false); // when dragging outside bounds, lose focus (hold)
		}
	}
}
