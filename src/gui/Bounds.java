package gui;

import java.awt.*;

public final class Bounds {
	private final int xParameter, yParameter; // offset values
	private final int width, height;
	private final int alignX, alignY; // -1 = left/top, 0 = center, 1 = right/bottom

	private Dimension containerSize;
	private int xCalculated, yCalculated;

	public Bounds(int x, int y, int w, int h, int alignX, int alignY) {
		if (!(alignX == -1 || alignX == 0 || alignX == 1))
			throw new IllegalArgumentException("for alignX=" + alignX);
		if (!(alignY == -1 || alignY == 0 || alignY == 1))
			throw new IllegalArgumentException("for alignY=" + alignY);

		xParameter = x;
		yParameter = y;
		width = w;
		height = h;
		this.alignX = alignX;
		this.alignY = alignY;
	}

	public Bounds(int x, int y, int w, int h) {
		this(x, y, w, h, -1, -1);
	}

	// used to calculate both X and Y position relative to screen sides
	private static int calculatePos(int position, int elementSize, int containerSize, int alignMode) {
		switch (alignMode) {
			case -1:
				return position;
			case 1:
				return containerSize - elementSize + position;
			default:
				return (containerSize - elementSize) / 2 + position;
		}
	}

	protected void onContainerSizeChange(Dimension size) {
		containerSize = size;
		calculatePosition();
	}

	private void calculatePosition() {
		xCalculated = calculatePos(xParameter, width, containerSize.width, alignX);
		yCalculated = calculatePos(yParameter, height, containerSize.height, alignY);
	}

	public int getX() {
		return xCalculated;
	}

	public int getY() {
		return yCalculated;
	}

	public int getWidth() {
		return width;
	}

	public int getHeight() {
		return height;
	}

	public boolean contains(Point p) {
		return getRectangle().contains(p);
	}

	public Rectangle getRectangle() {
		return new Rectangle(xCalculated, yCalculated, width, height);
	}

	public Rectangle getRectangleExpanded(int exp) {
		return new Rectangle(xCalculated - exp, yCalculated - exp, width + 2 * exp, height + 2 * exp);
	}
}
