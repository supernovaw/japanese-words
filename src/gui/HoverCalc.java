package gui;

/* Used to calculate animation phases, i.e. when moving mouse around
 * a button to make it glow or go black. Lets the animation be stopped
 * and turned the other way while not completed. Contains ease functions
 * to make animations look smoother
 * Note: only shuts AnimatingElement repaints when requesting
 * getPhase (or one of ease functions). As long as getPhase isn't run
 * by anybody, repaints don't stop. (This causes no problems for
 * elements that always have getPhase as a part of paint method.)
 */
public final class HoverCalc {
	public static final int AFT_STABILIZED_SPARE_DELAY = 200;

	private final long hoverTransitionPeriod;
	private boolean hovered;
	private long anchorTimePoint;
	private AnimatingElement animatingElement;

	public HoverCalc(long hoverTransitionPeriod) {
		this.hoverTransitionPeriod = hoverTransitionPeriod;
	}

	public HoverCalc(long hoverTransitionPeriod, Element animating) {
		this(hoverTransitionPeriod);
		animatingElement = new AnimatingElement(animating);
	}

	public HoverCalc(long hoverTransitionPeriod, Element animating, int expand) {
		this(hoverTransitionPeriod);
		Runnable repaint = () -> animating.repaint(animating.getBounds().getRectangleExpanded(expand));
		animatingElement = new AnimatingElement(repaint);
	}

	public HoverCalc(long hoverTransitionPeriod, Runnable animate) {
		this(hoverTransitionPeriod);
		animatingElement = new AnimatingElement(animate);
	}

	public static double easeCubicInOut(double x) {
		if (x < 0.5)
			return 4 * Math.pow(x, 3);
		else
			return 1 - 4 * Math.pow(1 - x, 3);
	}

	public static double easeCubicOut(double x) {
		return 1 - Math.pow(1 - x, 3);
	}

	public static double easeSine(double x) {
		return (1 + Math.sin(Math.PI * (x - .5))) / 2;
	}

	public void setHovered(boolean h) {
		if (hovered == h)
			return;
		this.hovered = h;

		if (animatingElement != null)
			animatingElement.setActive(true);

		long t = System.currentTimeMillis();
		if (anchorTimePoint + hoverTransitionPeriod < t) {
			anchorTimePoint = t;
		} else { // change animation direction on the run
			anchorTimePoint = 2 * t - anchorTimePoint - hoverTransitionPeriod;
		}
	}

	public boolean isHovered() {
		return hovered;
	}

	public void setInitially(boolean hovered) {
		this.hovered = hovered;
	}

	public double getPhase() {
		long passed = System.currentTimeMillis() - anchorTimePoint;
		double f;

		if (passed > hoverTransitionPeriod) {
			/* Only halt animation when element is repainted with phase 1 or 0 for sure.
			 * There is a bug when repainting is shut instantly after reaching constant
			 * phase which causes element to have around 5-10% alpha when it should be
			 * gone already, without any consequent auto repaints. This is probably
			 * caused by internal swing working: if paintComponent is run, it doesn't
			 * necessarily mean that painted details in it are displayed in window.
			 * Delay here is used to make sure that the element is repainted fully in
			 * 1.0 or 0.0 final phase condition.
			 */
			if (passed > hoverTransitionPeriod + AFT_STABILIZED_SPARE_DELAY)
				if (animatingElement != null) animatingElement.setActive(false);

			f = 1;
		} else {
			f = (double) passed / hoverTransitionPeriod;
		}

		if (!hovered)
			f = 1 - f;
		return f;
	}

	public double getCubicInOut() {
		return easeCubicInOut(getPhase());
	}

	public double getCubicOut() {
		return easeCubicOut(getPhase());
	}

	public double getSine() {
		return easeSine(getPhase());
	}
}
