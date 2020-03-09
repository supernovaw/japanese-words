package gui;

public final class OneWayAnimating {
	private final long transitionPeriod;
	private long anchorTimePoint;
	private boolean animatingCurrently;
	private AnimatingElement animatingElement;
	// key objects for the disappearing element (i.e. text String and 2 ints for X and Y)
	private Object[] previousObject;

	private OneWayAnimating(long transitionPeriod) {
		this.transitionPeriod = transitionPeriod;
	}

	public OneWayAnimating(long hoverTransitionPeriod, Element animating) {
		this(hoverTransitionPeriod);
		animatingElement = new AnimatingElement(animating);
	}

	public OneWayAnimating(long hoverTransitionPeriod, Element animating, int expand) {
		this(hoverTransitionPeriod);
		Runnable repaint = () -> animating.repaint(animating.getBounds().getRectangleExpanded(expand));
		animatingElement = new AnimatingElement(repaint);
	}

	public OneWayAnimating(long hoverTransitionPeriod, Runnable animate) {
		this(hoverTransitionPeriod);
		animatingElement = new AnimatingElement(animate);
	}

	public void animate(Object... previousObject) {
		this.previousObject = previousObject;
		anchorTimePoint = System.currentTimeMillis();
		animatingCurrently = true;

		if (animatingElement != null)
			animatingElement.setActive(true);
	}

	public void setParameter(int index, Object p) {
		if (previousObject == null) {
			previousObject = new Object[index + 1];
			previousObject[index] = p;
		} else if (previousObject.length <= index) { // if not enough size
			Object[] newArray = new Object[index + 1];
			System.arraycopy(previousObject, 0, newArray, 0, previousObject.length);
			newArray[index] = p;
			previousObject = newArray;
		} else {
			previousObject[index] = p;
		}
	}

	public Object getParameter(int index) {
		return previousObject[index];
	}

	// used to find out whether the animation is finished
	public boolean isFinished() {
		return !animatingCurrently;
	}

	public void setDisplayed(boolean displayed) {
		animatingElement.setDisplayed(displayed);
	}

	public double getPhase() {
		if (!animatingCurrently)
			return 1;

		long passed = System.currentTimeMillis() - anchorTimePoint;
		if (passed >= transitionPeriod) {
			if (passed > transitionPeriod + HoverCalc.AFT_STABILIZED_SPARE_DELAY) {
				animatingCurrently = false;
				if (animatingElement != null) animatingElement.setActive(false);
			}

			return 1;
		}

		return (double) passed / transitionPeriod;
	}

	public double getCubicInOut() {
		double phase = getPhase();
		if (phase == 1)
			return phase;
		else
			return HoverCalc.easeCubicInOut(phase);
	}

	public double getCubicOut() {
		double phase = getPhase();
		if (phase == 1)
			return phase;
		else
			return HoverCalc.easeCubicOut(phase);
	}

	public double getCubicIn() {
		double phase = getPhase();
		if (phase == 1)
			return phase;
		else
			return HoverCalc.easeCubicIn(phase);
	}

	public double getSine() {
		double phase = getPhase();
		if (phase == 1)
			return phase;
		else
			return HoverCalc.easeSine(phase);
	}
}
