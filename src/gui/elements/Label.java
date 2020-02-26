package gui.elements;

import gui.Bounds;
import gui.Element;
import gui.Scene;
import gui.Theme;

import java.awt.*;
import java.awt.geom.Area;

public class Label extends Element {
	private String text;
	private int alignHorizontal;
	private Area textArea;

	public Label(String text, int alignHorizontal, Scene container, Bounds bounds) {
		super(container, bounds);
		this.text = text;
		this.alignHorizontal = alignHorizontal;
	}

	public Label(String text, Scene container, Bounds bounds) {
		super(container, bounds);
		this.text = text;
	}

	public void setText(String text) {
		this.text = text;
		repaint();
	}

	@Override
	protected void paint(Graphics2D g) {
		g.setFont(Theme.getUIFont());
		g.setColor(Theme.getFG());

		if (textArea == null) {
			double textX = alignStringX(g, text, 0, w(), alignHorizontal);
			double textY = centerStringY(g, h() / 2);
			textArea = getTextArea(text, textX, textY, g);

		}
		int tx = x(), ty = y();
		g.translate(tx, ty);
		g.fill(textArea);
		g.translate(-tx, -ty);
	}
}
