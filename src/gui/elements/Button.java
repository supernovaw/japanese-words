package gui.elements;

import gui.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;

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
		fillBackground(g);
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

	private void fillBackground(Graphics2D g) {
		int fillBgAlpha = 50;
		int fillBG = Theme.getBG().getRGB();
		// cut alpha part and insert 'fillBgAlpha'
		fillBG = (fillBG & 0xffffff) | (fillBgAlpha << 24);
		g.setColor(new Color(fillBG, true));
		g.fill(getButtonForm());
	}

	private Area getTextArea(Graphics2D g) {
		g.setFont(Theme.getUIFont());
		FontMetrics fontMetrics = g.getFontMetrics();
		Area area = new Area(g.getFont().createGlyphVector(fontMetrics.getFontRenderContext(), text).getOutline());
		int x = centerStringX(g, text, x() + w() / 2);
		int y = centerStringY(g, y() + h() / 2);
		area.transform(AffineTransform.getTranslateInstance(x, y));
		return area;
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

	/* only leaves those pixels of img, that are contained by
	 * shape (shifted by args)
	 * used to replace masking by ineffective setClip
	 * and prevent creation of artifacts on right-bottom corner
	 */
	private static void alphaMask(BufferedImage img, Shape shape, int xTranslate, int yTranslate) {
		BufferedImage mask = new BufferedImage(img.getWidth(), img.getHeight(), BufferedImage.TYPE_INT_RGB);
		Graphics2D g = (Graphics2D) mask.getGraphics();
		g.translate(xTranslate, yTranslate);
		g.fill(shape);

		for (int x = 0; x < img.getWidth(); x++) {
			for (int y = 0; y < img.getHeight(); y++) {
				int origColor = img.getRGB(x, y);
				int maskColor = mask.getRGB(x, y);

				int maskAlpha = maskColor & 0xff;
				int origAlpha = (origColor & 0xff000000) >>> 24;
				int alpha = Math.min(maskAlpha, origAlpha);

				int result = origColor & 0xffffff;
				result |= alpha << 24;

				img.setRGB(x, y, result);
			}
		}
	}
}
