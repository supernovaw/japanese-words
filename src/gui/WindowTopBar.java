package gui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;

public class WindowTopBar extends Element {
	// close, hide and maximize/minimize button colors
	private static final Color RED_BUTTON = new Color(252, 91, 87);
	private static final Color YELLOW_BUTTON = new Color(229, 191, 60);
	private static final Color GREEN_BUTTON = new Color(87, 192, 56);

	// strip color
	private static final Color BAR_COLOR = new Color(0, 0, 0, 35);

	/* a point in which mouse started to drag the bar,
	 * used to calculate new window location while dragging
	 * in order to keep mouse at the same position relative
	 * to window position
	 */
	private Point holdBarPoint;

	private HoverCalc hoverCalc; // animation for hovering over bounds (3 buttons)
	private HoverCalc holdButton1, holdButton2, holdButton3; // hold animation for each of 3 buttons
	private int buttonHeld;

	/* This element is special because it is displayed atop
	 * all scenes and isn't attached to a particular one,
	 * which means it's scene field is null. Repainting and
	 * accessing container size is done directly using window instance.
	 */
	private Window containerWindow;
	private JFrame frame; // frame instance is used to relocate window directly

	protected WindowTopBar(Window containerWindow, JFrame f) {
		super(null, new Bounds(0, 0, 90, 30, -1, -1));
		frame = f;
		this.containerWindow = containerWindow;

		Runnable repaint = () -> containerWindow.repaint(getBounds().getRectangle());
		hoverCalc = new HoverCalc(200, repaint);
		int buttonsHoldAnimationPeriod = 100;
		holdButton1 = new HoverCalc(buttonsHoldAnimationPeriod, repaint);
		holdButton2 = new HoverCalc(buttonsHoldAnimationPeriod, repaint);
		holdButton3 = new HoverCalc(buttonsHoldAnimationPeriod, repaint);
	}

	// decreases RGB values by portion of 'b' and applies alpha parameter
	private static Color blackout(Color c, double b, int applyAlpha) {
		double m = 1 - b;
		return new Color((int) (c.getRed() * m), (int) (c.getGreen() * m), (int) (c.getBlue() * m), applyAlpha);
	}

	@Override
	protected void paint(Graphics2D g) {
		int buttonsR = 7; // buttons radius
		double noFocusAlpha = 0.3; // when no focus, use this alpha

		/* when buttons are held, they black
		 * out this much (1.0 - full blackout)
		 */
		double buttonsBlackout = 0.3;

		int buttonsY = y() + h() / 2;
		int closeX = x() + w() / 6;
		int hideX = x() + w() / 2;
		int resizeX = x() + w() - w() / 6;

		int alpha = (int) ((noFocusAlpha + (1 - noFocusAlpha) * hoverCalc.getSine()) * 255);

		// amount of blackout for each button
		double redB = holdButton1.getSine() * buttonsBlackout;
		double yellowB = holdButton2.getSine() * buttonsBlackout;
		double greenB = holdButton3.getSine() * buttonsBlackout;

		// finds colors with needed blackout and alpha
		Color red = blackout(RED_BUTTON, redB, alpha);
		Color yellow = blackout(YELLOW_BUTTON, yellowB, alpha);
		Color green = blackout(GREEN_BUTTON, greenB, alpha);

		// paint black transparent bar indicating drag area and window top
		g.setColor(BAR_COLOR);
		g.fillRect(0, y(), containerWindow.getSize().width, h());

		// fill 3 circles for buttons
		g.setColor(red);
		g.fillOval(closeX - buttonsR, buttonsY - buttonsR, buttonsR * 2, buttonsR * 2);
		g.setColor(yellow);
		g.fillOval(hideX - buttonsR, buttonsY - buttonsR, buttonsR * 2, buttonsR * 2);
		g.setColor(green);
		g.fillOval(resizeX - buttonsR, buttonsY - buttonsR, buttonsR * 2, buttonsR * 2);
	}

	@Override
	protected void mousePressed(MouseEvent e) {
		if (!SwingUtilities.isLeftMouseButton(e))
			return;
		if (e.getY() > y() + h() || e.getY() < y()) // if above or below bar
			return;

		if (contains(e.getPoint())) { // press on one of the buttons
			double part = 3d * (e.getX() - x()) / w();
			if (part < 1) {
				buttonHeld = 1;
				holdButton1.setHovered(true);
			} else if (part < 2) {
				buttonHeld = 2;
				holdButton2.setHovered(true);
			} else {
				buttonHeld = 3;
				holdButton3.setHovered(true);
			}
		} else { // prepare for dragging
			holdBarPoint = e.getPoint();
		}
	}

	@Override
	protected void mouseReleased(MouseEvent e) {
		if (!SwingUtilities.isLeftMouseButton(e))
			return;

		switch (buttonHeld) {
			case 1: // close
				System.exit(0);
				break;
			case 2: // hide
				frame.setState(Frame.ICONIFIED);
				hoverCalc.setHovered(false);
				break;
			case 3: // maximize / minimize
				maximizeOrMinimize();
				break;
		}

		holdBarPoint = null; // stop dragging

		// release button in case it was held
		buttonHeld = 0;
		holdButton1.setHovered(false);
		holdButton2.setHovered(false);
		holdButton3.setHovered(false);
	}

	@Override
	protected void mouseDragged(MouseEvent e) {
		mouseMoved(e);
		if (!SwingUtilities.isLeftMouseButton(e))
			return;

		// if mouse goes outside a button, remove hold animation
		if (getButton(e.getPoint()) != buttonHeld) {
			buttonHeld = 0;
			holdButton1.setHovered(false);
			holdButton2.setHovered(false);
			holdButton3.setHovered(false);
		}

		if (holdBarPoint == null || containerWindow.fullscreen)
			return; // if not started dragging ignore, also ignore if fullscreen
		// relocate window
		int offsetX = e.getX() - holdBarPoint.x, offsetY = e.getY() - holdBarPoint.y;
		int x = frame.getX() + offsetX, y = frame.getY() + offsetY;
		frame.setLocation(x, y);
	}

	@Override
	protected void mouseMoved(MouseEvent e) {
		hoverCalc.setHovered(contains(e.getPoint()));
	}

	private void maximizeOrMinimize() {
		if (containerWindow.fullscreen) { // exit fullscreen
			frame.setExtendedState(Frame.NORMAL);
			hoverCalc.setHovered(contains(containerWindow.getMousePosition()));
		} else { // go fullscreen
			frame.setExtendedState(Frame.MAXIMIZED_BOTH);
		}

		containerWindow.fullscreen = !containerWindow.fullscreen;
	}

	// returns number of button (1, 2, 3) that corresponds to a point
	private int getButton(Point p) {
		if (!getBounds().contains(p))
			return 0;
		double part = 3d * (p.getX() - x()) / w();

		if (part < 1)
			return 1;
		else if (part < 2)
			return 2;
		else
			return 3;
	}
}
