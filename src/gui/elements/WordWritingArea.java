package gui.elements;

import gui.*;
import recognition.Curves;
import recognition.WrittenAnswer;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.geom.Line2D;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class WordWritingArea extends Element {
	/* Smoothness parameter (when animating stroke removal, AWT
	 * curves are not available and have to be fully pre-calculated)
	 */
	private static final int CURVE_PARTS_PRECISION = 16;

	private final int expandRepaint; // the stroke can go slightly outside of bounds due to its width
	private Stroke graphicsStroke;
	// used to detect whether or not the mouse started dragging from within the bounds
	private boolean writingCurrently;
	private List<List<Point2D>> writtenWord;
	private Runnable onWritingReturned;

	// strokeRemove has object parameters List<Point2D> (path), double[] (lengths), double (total length)
	private OneWayAnimating strokeRemove;
	// writingRemove has object parameters List<List<Point2D>>, double[][], double[] (as in strokeRemove)
	private OneWayAnimating writingRemove;

	public WordWritingArea(Runnable onWritingReturned, Scene container, Bounds bounds) {
		super(container, bounds);
		writtenWord = new ArrayList<>();
		this.onWritingReturned = onWritingReturned;

		float strokeWidth = 4f;
		expandRepaint = (int) Math.ceil(strokeWidth / 2f);
		graphicsStroke = new BasicStroke(strokeWidth, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
		strokeRemove = new OneWayAnimating(400, this, expandRepaint);
		writingRemove = new OneWayAnimating(400, this, expandRepaint);
	}

	@Override
	protected void paint(Graphics2D g) {
		int tx = x(), ty = y();
		g.translate(tx, ty);

		g.setColor(Theme.getBG(30));
		g.fillRect(0, 0, w(), h());
		g.setColor(Theme.getFG());

		g.fill(graphicsStroke.createStrokedShape(getPath()));
		animateStrokeRemoval(g);
		animateDrawingRemoval(g);

		g.translate(-tx, -ty);
	}

	private void animateStrokeRemoval(Graphics2D g) {
		double phase = strokeRemove.getPhase();
		if (phase == 1)
			return;
		// noinspection unchecked
		List<Point2D> removedStroke = (List<Point2D>) strokeRemove.getParameter(0);
		double[] lengths = (double[]) strokeRemove.getParameter(1);
		double totalLength = (double) strokeRemove.getParameter(2);
		paintFadingStroke(removedStroke, lengths, totalLength, phase, g);
	}

	private void animateDrawingRemoval(Graphics2D g) {
		double phase = writingRemove.getPhase();
		if (phase == 1)
			return;

		// noinspection unchecked
		List<List<Point2D>> paths = (List<List<Point2D>>) writingRemove.getParameter(0);
		double[][] lengths = (double[][]) writingRemove.getParameter(1);
		double[] totalLengths = (double[]) writingRemove.getParameter(2);

		// sizes 1 and 2 are special cases
		if (paths.size() == 1) {
			paintFadingStroke(paths.get(0), lengths[0], totalLengths[0], phase, g);
		} else if (paths.size() == 2) {
			double phase0 = phase * 4 / 3;
			double phase1 = phase * 4 / 3 - 1 / 3d;

			if (phase0 < 1)
				paintFadingStroke(paths.get(0), lengths[0], totalLengths[0], phase0, g);

			if (phase1 > 0)
				paintFadingStroke(paths.get(1), lengths[1], totalLengths[1], phase1, g);
			else {
				g.setColor(Theme.getFG());
				g.fill(graphicsStroke.createStrokedShape(Curves.getPath(paths.get(1))));
			}
		} else {
			double pieceDuration = 0.5d / (paths.size() - 1);
			double strokePhaseDuration = 0.5d;
			for (int i = 0; i < paths.size(); i++) {
				double startPhase = pieceDuration * i;
				double endPhase = startPhase + strokePhaseDuration;

				if (phase < startPhase) {
					g.setColor(Theme.getFG());
					g.fill(graphicsStroke.createStrokedShape(Curves.getPath(paths.get(i))));
				} else if (phase < endPhase) {
					double strokePhase = (phase - startPhase) / strokePhaseDuration;
					paintFadingStroke(paths.get(i), lengths[i], totalLengths[i], strokePhase, g);
				}
			}
		}
	}

	// paint a stroke with cutting and fading out
	private void paintFadingStroke(List<Point2D> stroke, double[] lengths, double totalLength,
								   double phase, Graphics2D g) {
		double fadeAlpha = HoverCalc.easeSine(1d - phase);
		double cutPhase = HoverCalc.easeCubicInOut(phase);
		g.setColor(Theme.getFG(fadeAlpha));
		List<Point2D> cut = Curves.cutStroke(stroke, lengths, totalLength, cutPhase);
		g.fill(graphicsStroke.createStrokedShape(Curves.getPath(cut)));
	}

	// returns all strokes in a Path2D
	private Path2D getPath() {
		Path2D path = new Path2D.Double();
		writtenWord.forEach(stroke -> {
			if (stroke.size() < 2)
				return;
			boolean isLast = writtenWord.indexOf(stroke) == writtenWord.size() - 1;
			boolean hideLastPart = isLast && writingCurrently; // hide last part of a line that's drawn currently
			path.append(Curves.getSmoothStrokePath(stroke, !hideLastPart), false);
		});
		return path;
	}

	private void removeStroke() {
		if (writtenWord.isEmpty())
			return;

		List<Point2D> removedStroke = writtenWord.remove(writtenWord.size() - 1);
		removedStroke = Curves.smooth(removedStroke, CURVE_PARTS_PRECISION);
		double[] distances = new double[removedStroke.size()];
		double totalStrokeLength = 0;
		for (int i = 1; i < distances.length; i++) {
			distances[i] = removedStroke.get(i - 1).distance(removedStroke.get(i));
			totalStrokeLength += distances[i];
		}

		if (writingCurrently)
			writingCurrently = false;

		strokeRemove.animate(removedStroke, distances, totalStrokeLength);
	}

	// removes all strokes and starts the animation
	public WrittenAnswer flushWriting() {
		if (writtenWord.isEmpty())
			return new WrittenAnswer(Collections.emptyList());
		List<List<Point2D>> removed = new ArrayList<>(writtenWord.size());
		for (List<Point2D> stroke : writtenWord)
			removed.add(Curves.smooth(stroke, CURVE_PARTS_PRECISION));
		writtenWord.clear();

		double[][] lengths = new double[removed.size()][];
		double[] totalLengths = new double[removed.size()];

		for (int i = 0; i < removed.size(); i++) {
			List<Point2D> stroke = removed.get(i);
			lengths[i] = new double[stroke.size()];
			for (int j = 1; j < stroke.size(); j++) {
				lengths[i][j] = stroke.get(j - 1).distance(stroke.get(j));
				totalLengths[i] += lengths[i][j];
			}
		}

		writingRemove.animate(removed, lengths, totalLengths);
		return new WrittenAnswer(removed);
	}

	@Override
	protected void mousePressed(MouseEvent e) {
		if (!contains(e.getPoint()))
			return;

		if (SwingUtilities.isLeftMouseButton(e)) {
			Point translated = e.getPoint();
			translated.translate(-x(), -y());

			writtenWord.add(new ArrayList<>(Collections.singletonList(translated)));
			writingCurrently = true;
		} else if (SwingUtilities.isMiddleMouseButton(e)) {
			if (!writingCurrently)
				onWritingReturned.run();
		} else if (SwingUtilities.isRightMouseButton(e)) {
			removeStroke();
		}
	}

	@Override
	protected void mouseReleased(MouseEvent e) {
		if (!SwingUtilities.isLeftMouseButton(e))
			return;
		if (!writingCurrently || writtenWord.isEmpty() && !contains(e.getPoint()))
			return;
		// check if the last stroke is too short and remove if necessary
		List<Point2D> lastStroke = writtenWord.get(writtenWord.size() - 1);
		boolean isLastStrokeValid = lastStroke.size() >= 2; // 2 is the minimum stroke size
		if (!isLastStrokeValid)
			writtenWord.remove(writtenWord.size() - 1);
		writingCurrently = false;
		repaintExp();
	}

	@Override
	protected void mouseDragged(MouseEvent e) {
		if (!SwingUtilities.isLeftMouseButton(e) || !writingCurrently)
			return;
		Point p = e.getPoint();
		p.translate(-x(), -y());

		if (contains(e.getPoint())) { // just add a point
			writtenWord.get(writtenWord.size() - 1).add(p);
		} else { // process going outside of bounds (find a point where mouse left the bounds)
			List<Point2D> lastStroke = writtenWord.get(writtenWord.size() - 1);
			Point2D lastPoint = lastStroke.get(lastStroke.size() - 1);

			Point2D exitPoint = getExitPoint(new Rectangle(0, 0, w(), h()), lastPoint, p);
			lastStroke.add(exitPoint);
			writingCurrently = false; // stop reacting to dragging (finish writing this stroke)
		}
		repaintExp();
	}

	private void repaintExp() {
		repaint(getBounds().getRectangleExpanded(expandRepaint));
	}

	private static Point2D getExitPoint(Rectangle2D rect, Point2D p1, Point2D p2) {
		Point2D cornerNW = new Point2D.Double(rect.getX(), rect.getY());
		Point2D cornerSW = new Point2D.Double(rect.getX(), rect.getMaxY());
		Point2D cornerNE = new Point2D.Double(rect.getMaxX(), rect.getY());
		Point2D cornerSE = new Point2D.Double(rect.getMaxX(), rect.getMaxY());

		Point2D intersection;
		if ((intersection = getIntersection(cornerNW, cornerNE, p1, p2)) != null)
			return intersection; // top side
		if ((intersection = getIntersection(cornerNW, cornerSW, p1, p2)) != null)
			return intersection; // left side
		if ((intersection = getIntersection(cornerNE, cornerSE, p1, p2)) != null)
			return intersection; // right side
		if ((intersection = getIntersection(cornerSW, cornerSE, p1, p2)) != null)
			return intersection; // bottom side
		throw new IllegalArgumentException("line of " + p1 + " and " + p2 + " doesn't intersect " + rect + " bounds");
	}

	private static Point2D getIntersection(Point2D l1start, Point2D l1end, Point2D l2start, Point2D l2end) {
		if (!new Line2D.Double(l1start, l1end).intersectsLine(new Line2D.Double(l2start, l2end)))
			return null;

		double slope = (l2end.getY() - l2start.getY()) / (l2end.getX() - l2start.getX());
		double offsetY = l2start.getY() - slope * l2start.getX();

		if (l1start.getY() == l1end.getY()) { // if l1 is horizontal
			double resultX = (l1start.getY() - offsetY) / slope;
			return new Point2D.Double(resultX, l1start.getY());
		}
		if (l1start.getX() == l1end.getX()) { // if l1 is vertical
			double resultY = slope * l1start.getX() + offsetY;
			return new Point2D.Double(l1start.getX(), resultY);
		}
		throw new IllegalArgumentException("l1 is neither horizontal or vertical (" + l1start + " - " + l1end + ")");
	}

	@Override
	protected void onDisplay() {
		strokeRemove.setDisplayed(true);
		writingRemove.setDisplayed(true);
	}

	@Override
	protected void onShut() {
		strokeRemove.setDisplayed(false);
		writingRemove.setDisplayed(false);
	}
}
