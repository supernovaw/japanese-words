package gui.elements;

import gui.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
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
	private boolean hold;

	public Button(String text, Runnable r, Scene container, Bounds bounds) {
		super(container, bounds);
		run = r;
		this.text = text;
		hoverCalc = new HoverCalc(200, this, EXPAND);
		holdCalc = new HoverCalc(150, this, EXPAND);
	}

	@Override
	protected void paint(Graphics2D g) {
		g.setColor(Theme.getFG());
		g.setFont(Theme.getUIFont());

		double holdPhase = holdCalc.getCubicOut();
		double hoverPhase = hoverCalc.getCubicInOut();

		RoundRectangle2D buttonForm = getButtonForm();
		// shrunk version is used to clear inner button part without removing counter line
		RoundRectangle2D buttonFormShrunk = getButtonFormShrunk();

		// paint text and fill (in filled areas, text is engraved)
		if (holdPhase == 0) { // just paint button text
			paintText(g);
		} else { // fill button to animate holding
			// new image instance is used to paint text as transparent overlay (engrave)
			BufferedImage img = new BufferedImage(w() + EXPAND * 2,
					h() + EXPAND * 2, BufferedImage.TYPE_INT_ARGB);
			Graphics2D g2 = (Graphics2D) img.getGraphics();
			g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			g2.translate(EXPAND - x(), EXPAND - y());
			g2.setColor(Theme.getFG());
			g2.setFont(Theme.getUIFont());

			paintText(g2);

			double fillW = holdPhase * w() / 2; // fill button when holding
			g2.fill(new Rectangle2D.Double(x() + w() / 2d - fillW, y(), fillW * 2, h()));

			// set clip to only engrave text in middle (filled)
			// part without touching not yet filled part
			g2.setClip(new Rectangle2D.Double(x() + w() / 2d - fillW, y(), fillW * 2, h()));
			g2.setComposite(AlphaComposite.DstOut);
			paintText(g2);

			// button is drawn but edges are not round; round edges
			alphaMask(img, buttonForm, EXPAND - x(), EXPAND - y());

			g.drawImage(img, x() - EXPAND, y() - EXPAND, null);
		}

		// paint button outline which extends when hovered
		if (hoverPhase == 0) { // not hovered, 1px outline
			g.draw(buttonForm);
		} else {
			// new image instance is used to cut middle part
			// and not to change stroke in general Graphics2D
			BufferedImage img = new BufferedImage(w() + EXPAND * 2,
					h() + EXPAND * 2, BufferedImage.TYPE_INT_ARGB);
			Graphics2D g2 = (Graphics2D) img.getGraphics();
			g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			g2.translate(EXPAND - x(), EXPAND - y());

			g2.setColor(Theme.getFG());
			g2.setStroke(new BasicStroke(1f + (float) hoverPhase * EXPAND * 2));
			g2.draw(buttonForm);

			// the outline is expanded both outside and inside, remove inside filling
			g2.setComposite(AlphaComposite.DstOut);
			g2.fill(buttonFormShrunk);

			g.drawImage(img, x() - EXPAND, y() - EXPAND, null);
		}
	}

	private void paintText(Graphics2D g) {
		g.drawString(text, centerStringX(g, text, x() + w() / 2), centerStringY(g, text, y() + h() / 2));
	}

	private RoundRectangle2D.Double getButtonForm() {
		return new RoundRectangle2D.Double(
				x(), y(), w() - 1, h() - 1, RND_CORNERS, RND_CORNERS);
	}


	private RoundRectangle2D.Double getButtonFormShrunk() {
		return new RoundRectangle2D.Double(
				x() + 1, y() + 1, w() - 2, h() - 2, RND_CORNERS, RND_CORNERS);
	}

	@Override
	protected void mousePressed(MouseEvent e) {
		if (!SwingUtilities.isLeftMouseButton(e))
			return;
		if (!getButtonForm().contains(e.getPoint()))
			return;
		holdCalc.setHovered(hold = true);
	}

	@Override
	protected void mouseReleased(MouseEvent e) {
		if (!SwingUtilities.isLeftMouseButton(e))
			return;
		if (hold) {
			hold = false;
			run.run();
		}
		holdCalc.setHovered(false);
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
			holdCalc.setHovered(hold = false); // when dragging outside bounds, lose focus (hold)
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
