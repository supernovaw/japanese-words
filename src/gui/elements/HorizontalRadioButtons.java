package gui.elements;

import gui.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;

public class HorizontalRadioButtons extends Element {
	private static final int TRANSITION_PERIOD = 300; // time to switch buttons
	private static final int OUTLINE_WIDTH = 1; // outline stroke width
	private static final int OUTLINE_WIDTH_HOVERED = 3;
	private static final int RND_CORNERS = 15;

	private int buttonsAmt;
	private String[] buttonsText;

	private int selected;

	/* Coordinates of last selected button, used in transition animation.
	 * Just saving button index would have the same effect except that when
	 * a new button is selected without last animation finishing, those
	 * numbers take position of where the rectangle was while selecting
	 * a new button and are used to animate motion from that point (with no
	 * jumping from one place to another).
	 */
	private double selectionOldStart, selectionOldEnd;
	private long transitionStartTime;
	// used to skip calculation of phase and straightaway paint pre-rendered button
	private boolean animationFinished = true;
	private AnimatingElement animatingElement; // used to animate transition
	private HoverCalc hoverCalc; // used to animate hovering

	private Area[] selectionAreas; // when not animating, use pre-rendered buttons
	private int[] textWidthSums; // used to calculate button positions (based on string widths)
	private int margin; // margins between bounds, separators and text, based on bounds height and font size
	private RoundRectangle2D.Double roundBounds; // based on RND_CORNERS, has X and Y equal to 0
	private Area frameArea; // default width outline and separators between buttons
	// area with text of all buttons located accordingly to string widths and margin, no bounds X and Y offset
	private Area textArea;

	public HorizontalRadioButtons(String[] buttonsText, Scene container, Bounds bounds) {
		super(container, bounds);
		this.buttonsText = buttonsText;
		buttonsAmt = buttonsText.length;
		renderPaintingComponents();

		animatingElement = new AnimatingElement(this);
		hoverCalc = new HoverCalc(120, this, OUTLINE_WIDTH_HOVERED);
	}

	@Override
	protected void paint(Graphics2D g) {
		Area buttonArea;

		if (animationFinished) { // case for no transition animation, just take pre-rendered area
			buttonArea = selectionAreas[selected];
		} else {
			long passed = System.currentTimeMillis() - transitionStartTime;

			if (passed < TRANSITION_PERIOD) { // paint transition animation
				double phase = (double) passed / TRANSITION_PERIOD;
				buttonArea = paint(selected, selectionOldStart, selectionOldEnd, phase);
			} else { // if animation has finished
				buttonArea = selectionAreas[selected];

				// only stop repaint calls, when spare delay passed
				if (passed > TRANSITION_PERIOD + HoverCalc.AFT_STABILIZED_SPARE_DELAY) {
					animationFinished = true;
					animatingElement.setActive(false);
				}
			}
		}

		int translateX = x(), translateY = y(); // fix to make sure translation is done equally in both directions
		g.translate(translateX, translateY);
		fillBackground(g);
		g.setColor(Theme.getFG());
		g.fill(buttonArea);

		double hover = hoverCalc.getCubicOut();
		if (hover != 0) { // if hovering, paint expanded outline atop previously painted
			double strokeW = OUTLINE_WIDTH + hover * (OUTLINE_WIDTH_HOVERED - OUTLINE_WIDTH);
			// stroke width is multiplied by 2 because it counts for both inner and outer
			Area expandedOutline = new Area(new BasicStroke(2f * (float) strokeW).createStrokedShape(roundBounds));
			expandedOutline.subtract(new Area(roundBounds)); // remove inner part of stroke
			g.fill(expandedOutline);
		}
		g.translate(-translateX, -translateY);
	}

	// prepare necessary values for quick repaint in general cases, etc.
	private void renderPaintingComponents() {
		FontMetrics fm = new Canvas().getFontMetrics(Theme.getUIFont());

		textWidthSums = new int[buttonsAmt + 1];
		for (int i = 0, sum = 0; i < buttonsAmt; i++) {
			sum += fm.stringWidth(buttonsText[i]);
			textWidthSums[i + 1] = sum;
		}

		textArea = new Area();
		// size between buttons text and separator lines (or bounds left/right sides)
		margin = h() / 3;
		int stringY = (fm.getAscent() - fm.getDescent() + h()) / 2;
		for (int i = 0; i < buttonsAmt; i++)
			textArea.add(getTextArea(fm, buttonsText[i], margin + getSeparatorX(i), stringY));

		roundBounds = new RoundRectangle2D.Double(0, 0,
				getSeparatorX(buttonsAmt), h() - 1, RND_CORNERS, RND_CORNERS);
		frameArea = new Area(new BasicStroke(2f * (float) OUTLINE_WIDTH).createStrokedShape(roundBounds));
		frameArea.subtract(new Area(roundBounds)); // remove inner stroke part

		for (int i = 1; i < buttonsAmt; i++) { // add separator lines
			int x = getSeparatorX(i);
			/* X is cast to int because rectangle should be placed evenly with screen pixels.
			 * When OUTLINE_WIDTH is odd and X is a non-integer, line is "stretched" to a width
			 * of one more pixel than it should be with losing opacity in left and right
			 * pixel lines. (Division by double and then casting to int is redundant, but it
			 * prevents IDE from warning about integer division in floating-point context)
			 */
			frameArea.add(new Area(new Rectangle2D.Double((int) (x - OUTLINE_WIDTH / 2d), 0, OUTLINE_WIDTH, h() - 1)));
		}

		selectionAreas = new Area[buttonsAmt];
		for (int i = 0; i < buttonsAmt; i++) // pre-render for cases with no animation
			selectionAreas[i] = paint(i);
	}

	private void fillBackground(Graphics2D g) {
		int fillBgAlpha = 50;
		int fillBG = Theme.getBG().getRGB();
		// cut alpha part and insert 'fillBgAlpha'
		fillBG = (fillBG & 0xffffff) | (fillBgAlpha << 24);
		g.setColor(new Color(fillBG, true));
		g.fill(roundBounds);
	}

	// paint transition animation between current and previously selected buttons
	private Area paint(int selected, double selectedOldStart, double selectedOldEnd, double phase) {
		double startNew = getSeparatorX(selected);
		double endNew = getSeparatorX(selected + 1);
		double start = interpolateBorderTransition(selectedOldStart, startNew, phase);
		double end = interpolateBorderTransition(selectedOldEnd, endNew, phase);

		return paint(start, end);
	}

	// paint button without animations
	private Area paint(int selected) {
		double start = getSeparatorX(selected);
		double end = getSeparatorX(selected + 1);
		return paint(start, end);
	}

	// paint button with specified selection bounds
	private Area paint(double selectionStart, double selectionEnd) {
		Rectangle2D.Double selectedRect = new Rectangle2D.Double(
				selectionStart, 0, selectionEnd - selectionStart, h() - 1);
		Area selectedArea = new Area(selectedRect);
		selectedArea.intersect(new Area(roundBounds)); // round edges of selected rectangle if needed

		Area result = new Area(textArea); // add text
		result.exclusiveOr(selectedArea); // text XOR selection rect
		result.add(frameArea); // add outline and separating lines
		return result;
	}

	// similar to Graphics.drawString, but applied on Area
	private Area getTextArea(FontMetrics fm, String text, int x, int y) {
		Area area = new Area(fm.getFont().createGlyphVector(fm.getFontRenderContext(), text).getOutline());
		area.transform(AffineTransform.getTranslateInstance(x, y));
		return area;
	}

	// smoothen motion of selection rectangle bounds
	private double interpolateBorderTransition(double oldX, double newX, double phase) {
		phase = HoverCalc.easeCubicOut(phase);
		return (1 - phase) * oldX + phase * newX;
	}

	/* Returns X of one of separators (or left/right
	 * bounds) as shown ((0) to (3) are input values)
	 *  .-----------------------------------------------------.
	 *  |                 |                 |                 |
	 * (0)    text[0]    (1)    text[1]    (2)    text[2]    (3)
	 *  |                 |                 |                 |
	 *  `-----------------------------------------------------`
	 */
	private int getSeparatorX(int i) {
		return textWidthSums[i] + 2 * i * margin;
	}

	// reverse for getSeparatorX, returns button index
	private int getButtonIndex(int x) {
		if (x < 0)
			return -1;
		for (int i = 0; i < buttonsAmt; i++) {
			if (x < getSeparatorX(i + 1))
				return i;
		}
		return -1;
	}

	private void proceedClick(Point p) {
		int newIndex = getButtonIndex(p.x - x());
		if (newIndex == selected)
			return; // ignore if clicked the selected button

		// apply selectionOldStart and selectionOldEnd depending on situation
		if (animationFinished) { // if animation is already finished, just apply old start and end X's
			selectionOldStart = getSeparatorX(selected);
			selectionOldEnd = getSeparatorX(selected + 1);
		} else { // if changing selection while previous animation is running
			long time = System.currentTimeMillis();
			double phase = (double) (time - transitionStartTime) / TRANSITION_PERIOD;
			if (phase > 1)
				phase = 1;

			// characteristics of previous animation
			double startGoesFrom = selectionOldStart;
			double endGoesFrom = selectionOldEnd;

			double startGoesTo = getSeparatorX(selected);
			double endGoesTo = getSeparatorX(selected + 1);

			// where selection rectangle currently is
			double currentStartPos = interpolateBorderTransition(startGoesFrom, startGoesTo, phase);
			double currentEndPos = interpolateBorderTransition(endGoesFrom, endGoesTo, phase);

			// apply its position to animate from, towards the new selection
			selectionOldStart = currentStartPos;
			selectionOldEnd = currentEndPos;
		}
		selected = newIndex;
		transitionStartTime = System.currentTimeMillis();
		animatingElement.setActive(true);
		animationFinished = false;
	}

	@Override
	protected void mousePressed(MouseEvent e) {
		if (!SwingUtilities.isLeftMouseButton(e))
			return;
		if (!roundBounds.contains(e.getX() - x(), e.getY() - y()))
			return;
		proceedClick(e.getPoint());
	}

	@Override
	protected void mouseMoved(MouseEvent e) {
		Point translated = new Point(e.getX() - x(), e.getY() - y());
		hoverCalc.setHovered(roundBounds.contains(translated));
	}

	@Override
	protected void mouseDragged(MouseEvent e) {
		mouseMoved(e);
	}
}
