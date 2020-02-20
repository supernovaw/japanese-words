package gui.elements;

import gui.Bounds;
import gui.Element;
import gui.Scene;
import gui.Theme;

import java.awt.*;

public class Label extends Element {
	private String text;
	private int alignHorizontal;

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
		g.drawString(text, alignStringX(g, text, x(), w(), alignHorizontal), centerStringY(g, y() + h() / 2));
	}
}
