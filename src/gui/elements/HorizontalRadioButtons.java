package gui.elements;

import gui.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.geom.Area;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;

public class HorizontalRadioButtons extends Element {
	private static final int OUTLINE_WIDTH = 1; // outline stroke width
	private static final int OUTLINE_WIDTH_HOVERED = 3;
	private static final int RND_CORNERS = 15;

	private int buttonsAmt;
	private String[] buttonsText;
	private int align; // -1, 0, 1 for left/center/right

	private int selected;

	/* Object parameters are 2 doubles for start and end X's used in
	 * transition animation.
	 * Just saving button index would have the same effect except that when
	 * a new button is selected without last animation finishing, those
	 * numbers take position of where the rectangle was while selecting
	 * a new button and are used to animate motion from that point (with no
	 * jumping from one place to another).
	 */
	private OneWayAnimating transitionAnimation;
	private HoverCalc hoverCalc; // used to animate hovering

	private int offsetX;
	private Area[] selectionAreas; // when not animating, use pre-rendered buttons
	private int[] textWidthSums; // used to calculate button positions (based on string widths)
	private int margin; // margins between bounds, separators and text, based on bounds height and font size
	private RoundRectangle2D.Double roundBounds; // based on RND_CORNERS, has X and Y equal to 0
	private Area frameArea; // default width outline and separators between buttons
	// area with text of all buttons located accordingly to string widths and margin, no bounds X and Y offset
	private Area textArea;

	public HorizontalRadioButtons(String[] buttonsText, int align, Scene container, Bounds bounds) {
		super(container, bounds);
		this.buttonsText = buttonsText;
		buttonsAmt = buttonsText.length;
		this.align = align;
		renderPaintingComponents();

		transitionAnimation = new OneWayAnimating(300, this);
		hoverCalc = new HoverCalc(120, this, OUTLINE_WIDTH_HOVERED);
	}

	@Override
	protected void paint(Graphics2D g) {
		Area buttonArea;

		double buttonTransitionPhase = transitionAnimation.getPhase();
		if (buttonTransitionPhase == 1) { // case for no transition animation, just take pre-rendered area
			buttonArea = selectionAreas[selected];
		} else { // paint transition animation
			double oldStart = (double) transitionAnimation.getParameter(0);
			double oldEnd = (double) transitionAnimation.getParameter(1);
			buttonArea = paint(selected, oldStart, oldEnd, buttonTransitionPhase);
		}

		int translateX = x(), translateY = y(); // fix to make sure translation is done equally in both directions
		g.translate(translateX, translateY);

		g.setColor(Theme.getBG(50)); // fill background with this alpha
		g.fill(roundBounds);

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
		margin = h() / 3;

		offsetX = 0; // getSeparatorX should be originally based on offsetX being 0
		switch (align) {
			case -1: // leave as 0
				break;
			case 0:
				offsetX = (w() - getSeparatorX(buttonsAmt)) / 2;
				break;
			case 1:
				offsetX = w() - getSeparatorX(buttonsAmt);
				break;
			default:
				throw new IllegalArgumentException("for align=" + align);
		}

		textArea = new Area();
		// size between buttons text and separator lines (or bounds left/right sides)
		int stringY = (fm.getAscent() - fm.getDescent() + h()) / 2;
		for (int i = 0; i < buttonsAmt; i++)
			textArea.add(getTextArea(buttonsText[i], margin + getSeparatorX(i), stringY, fm.getFont()));

		roundBounds = new RoundRectangle2D.Double(offsetX, 0,
				getSeparatorX(buttonsAmt) - offsetX, h() - 1, RND_CORNERS, RND_CORNERS);
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
		return offsetX + textWidthSums[i] + 2 * i * margin;
	}

	// reverse for getSeparatorX, returns button index
	private int getButtonIndex(int x) {
		if (x < offsetX)
			return -1;
		for (int i = 0; i < buttonsAmt; i++) {
			if (x < getSeparatorX(i + 1))
				return i;
		}
		return -1;
	}

	public int getSelectedButton() {
		return selected;
	}

	private void proceedClick(Point p) {
		int newIndex = getButtonIndex(p.x - x());
		if (newIndex == selected)
			return; // ignore if clicked the selected button

		double selectionOldStart, selectionOldEnd;

		// finds where selection start and end X's are when clicking
		if (transitionAnimation.getPhase() == 1) { // no animation, take static values
			selectionOldStart = getSeparatorX(selected);
			selectionOldEnd = getSeparatorX(selected + 1);
		} else { // if the previous animation is already running
			double phase = transitionAnimation.getPhase();

			// characteristics of previous animation
			double startGoesFrom = (double) transitionAnimation.getParameter(0);
			double endGoesFrom = (double) transitionAnimation.getParameter(1);

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
		transitionAnimation.animate(selectionOldStart, selectionOldEnd);
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

	@Override
	protected void onDisplay() {
		hoverCalc.setDisplayed(true);
		transitionAnimation.setDisplayed(true);
	}

	@Override
	protected void onShut() {
		hoverCalc.shut();
		transitionAnimation.setDisplayed(false);
	}
}
