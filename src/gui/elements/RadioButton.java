package gui.elements;

import gui.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.geom.Ellipse2D;

public class RadioButton extends Element {
	private int radioButtonSize = 16; // circle diameter

	private String text;
	private Area textArea;
	private int radioButtonY; // offset depending on element height
	private boolean enabled;
	private RadioButtonGroup group;
	private HoverCalc slideCalc, activationCalc;

	public RadioButton(String text, RadioButtonGroup group, Scene container, Bounds bounds) {
		super(container, bounds);
		this.text = text;
		this.group = group;
		group.add(this);
		radioButtonY = (h() - radioButtonSize) / 2;
		slideCalc = new HoverCalc(100, this);
		activationCalc = new HoverCalc(200, this);
	}

	@Override
	protected void paint(Graphics2D g) {
		g.setColor(Theme.getFG());
		if (textArea == null) {
			textArea = getTextArea(text, radioButtonSize + 10, centerStringY(g, h() / 2), Theme.getUIFont());
		}

		int slideWhenActive = 3; // distance the text slides to the right when hovered or enabled
		double textSlide = slideWhenActive * slideCalc.getCubicOut();
		g.fill(textArea.createTransformedArea(AffineTransform.getTranslateInstance(x() + textSlide, y())));

		double activationPhase = activationCalc.getPhase();
		int rbY = y() + radioButtonY;

		Ellipse2D.Double circleShape = new Ellipse2D.Double(x(), rbY, radioButtonSize, radioButtonSize);
		Area circleOutline = new Area(new BasicStroke(1f).createStrokedShape(circleShape));
		g.fill(circleOutline); // fill the outline

		g.setColor(Theme.getFG(HoverCalc.easeSine(activationPhase))); // set alpha
		if (activationPhase == 1) {
			g.fill(circleShape);
		} else if (activationPhase != 0) {
			// this phase always goes from 0 to 1 no matter is it disabling or enabling
			double innerRadiusPhase = enabled ? activationPhase : (1d - activationPhase);
			innerRadiusPhase = HoverCalc.easeCubicIn(innerRadiusPhase); // apply easing
			double innerCircleDiameter = innerRadiusPhase * radioButtonSize;
			double offsetXY = (radioButtonSize - innerCircleDiameter) / 2d; // the offset for both X and Y
			Shape innerCircle = new Ellipse2D.Double(x() + offsetXY, rbY + offsetXY,
					innerCircleDiameter, innerCircleDiameter);

			if (enabled) { // animate enabling
				g.fill(innerCircle); // paint appearing circle
			} else { // animate disabling
				Area circleArea = new Area(circleShape);
				circleArea.subtract(new Area(innerCircle));
				g.fill(circleArea); // paint appearing circle as a missing part of full circle
			}
		}
	}

	private void updateSlideCalc(boolean hover) {
		slideCalc.setHovered(hover || enabled);
	}

	protected void setEnabled(boolean enabled) { // used by RadioButtonGroup
		this.enabled = enabled;
		activationCalc.setHovered(enabled);
		updateSlideCalc(contains(getMousePosition()));
	}

	@Override
	protected void mousePressed(MouseEvent e) {
		if (!contains(e.getPoint()) || !SwingUtilities.isLeftMouseButton(e))
			return;

		activationCalc.setHovered(enabled);
		updateSlideCalc(contains(e.getPoint()));
		group.push(this); // to disable previously pressed button
	}

	@Override
	protected void mouseMoved(MouseEvent e) {
		updateSlideCalc(contains(e.getPoint()));
	}

	@Override
	protected void mouseDragged(MouseEvent e) {
		mouseMoved(e);
	}

	@Override
	protected void onDisplay() {
		slideCalc.setDisplayed(true);
		activationCalc.setDisplayed(true);
	}

	@Override
	protected void onShut() {
		updateSlideCalc(false);
		slideCalc.setDisplayed(false);
		activationCalc.setDisplayed(false);
	}
}
