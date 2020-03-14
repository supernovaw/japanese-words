package gui.elements;

import gui.*;

import java.awt.*;
import java.awt.geom.Area;

public class Label extends Element {
	private String text;
	private int alignHorizontal;
	private Area textArea;
	private Font font;
	// textChangeAnimate has object parameter Area (old textArea) or String if no Area available
	private OneWayAnimating textChangeAnimate;

	public Label(String text, int alignHorizontal, Font f, Scene container, Bounds bounds) {
		super(container, bounds);
		this.text = text;
		this.alignHorizontal = alignHorizontal;
		font = f.deriveFont(h() / 1.5f);
		textChangeAnimate = new OneWayAnimating(500, this);
	}

	public Label(String text, Font f, Scene container, Bounds bounds) {
		this(text, 0, f, container, bounds);
	}

	public Label(String text, int alignHorizontal, Scene container, Bounds bounds) {
		this(text, alignHorizontal, Theme.getUIFont(), container, bounds);
	}

	public Label(String text, Scene container, Bounds bounds) {
		this(text, Theme.getUIFont(), container, bounds);
	}

	public void changeText(String newText) {
		if (textArea == null) { // if the value hasn't been set since last changeText run
			textChangeAnimate.animate(text);
		} else {
			textChangeAnimate.animate(textArea);
		}
		textArea = null; // cause it to be updated on the next repaint
		text = newText;
	}

	// changes text of this label immediately with no animation
	public void setText(String newText) {
		textArea = null; // cause it to be updated on the next repaint
		text = newText;
	}

	@Override
	protected void paint(Graphics2D g) {
		g.setFont(font);

		if (textArea == null) {
			double textX = alignStringX(g, text, 0, w(), alignHorizontal);
			double textY = centerStringY(g, h() / 2);
			textArea = getTextArea(text, textX, textY, g);
		}

		int tx = x(), ty = y();
		g.translate(tx, ty);

		double fadePhase = textChangeAnimate.getSine();
		if (fadePhase == 1) { // just paint current text
			g.setColor(Theme.getFG());
			g.fill(textArea);
		} else { // paint current and fading text
			g.setColor(Theme.getFG(fadePhase));
			g.fill(textArea);
			g.setColor(Theme.getFG(1d - fadePhase));

			Object textParameter = textChangeAnimate.getParameter(0);
			if (textParameter instanceof String) {
				String s = (String) textParameter;
				double textX = alignStringX(g, s, 0, w(), alignHorizontal);
				double textY = centerStringY(g, h() / 2);
				textParameter = getTextArea(text, textX, textY, g);
				textChangeAnimate.setParameter(0, textParameter);
			}
			g.fill((Area) textParameter);
		}

		g.translate(-tx, -ty);
	}

	@Override
	protected void onDisplay() {
		textChangeAnimate.setDisplayed(true);
	}

	@Override
	protected void onShut() {
		textChangeAnimate.setDisplayed(false);
	}
}
