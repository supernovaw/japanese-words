package recognition;

import java.awt.*;
import java.awt.geom.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

// utility class for work with lists of points and curves
public final class Curves {
	// cuts a curve from start (t=0 means full stroke, t=0.9 means last 10% of the stroke)
	public static List<Point2D> cutStroke(List<Point2D> stroke, double[] lengths, double totalLength, double t) {
		double l = t * totalLength;
		int partIndex = -1; // if 'l' will be above all lengths, set the part to last
		for (int i = 1; i < stroke.size(); i++) {
			if (l <= lengths[i]) {
				partIndex = i - 1;
				l /= lengths[i]; // set 'l' to be between 0 and 1
				break;
			} else {
				l -= lengths[i];
			}
		}
		if (partIndex == -1) {
			Point2D last = stroke.get(stroke.size() - 1);
			return Arrays.asList(last, last);
		}
		List<Point2D> result = new ArrayList<>(stroke.size() - partIndex);
		Point2D pStart = tFunction(stroke.get(partIndex), stroke.get(partIndex + 1), l);
		result.add(pStart);
		for (int i = partIndex + 1; i < stroke.size(); i++)
			result.add(stroke.get(i));

		return result;
	}

	/* The last part should only be appended if stroke is finished and will not be edited anymore,
	 * without that the stroke end changes while writing if writing is done fast. It is caused by
	 * the way points are used to create QuadCurves.
	 */
	public static Path2D getSmoothStrokePath(List<Point2D> stroke, boolean appendLastPart) {
		if (stroke.size() < 2)
			throw new IllegalArgumentException("argument stroke has " + stroke.size() + " points");

		Path2D path = new Path2D.Double();
		path.moveTo(stroke.get(0).getX(), stroke.get(0).getY());

		if (stroke.size() == 2) {
			path.lineTo(stroke.get(1).getX(), stroke.get(1).getY());
			return path;
		}

		Point2D firstSecondMid = mid(stroke.get(0), stroke.get(1));
		path.lineTo(firstSecondMid.getX(), firstSecondMid.getY());

		// start at i = 1 because first segment is already appended
		for (int i = 1; i < stroke.size() - 1; i++) {
			Point2D pCurrent = stroke.get(i);
			Point2D pNext = mid(pCurrent, stroke.get(i + 1));

			path.quadTo(pCurrent.getX(), pCurrent.getY(), pNext.getX(), pNext.getY());
		}

		if (appendLastPart) {
			Point2D last = stroke.get(stroke.size() - 1);
			path.lineTo(last.getX(), last.getY());
		}
		return path;
	}

	/* The same as getSmoothStrokePath, but returns a set of points.
	 * Used when a stroke needs to be cut (using cutStroke); this
	 * can't be done with Path2D objects. parts argument is the
	 * smoothness of a resulting curve (1 / parts is dt in Bezier curves)
	 */
	public static List<Point2D> smooth(List<Point2D> stroke, int parts) {
		if (stroke.size() < 2)
			throw new IllegalArgumentException("argument stroke has " + stroke.size() + " points");
		if (stroke.size() == 2)
			return stroke;

		List<Point2D> result = new ArrayList<>((stroke.size() - 2) * parts + 3);
		result.add(stroke.get(0));
		result.add(mid(stroke.get(0), stroke.get(1)));

		if (stroke.size() == 1)
			return result;

		for (int i = 1; i < stroke.size() - 1; i++) {
			Point2D[] quad = new Point2D[3];
			quad[0] = mid(stroke.get(i - 1), stroke.get(i));
			quad[1] = stroke.get(i);
			quad[2] = mid(stroke.get(i + 1), stroke.get(i));

			for (int j = 1; j <= parts; j++)
				result.add(calculateBezierList(quad, (double) j / parts));
		}

		result.add(stroke.get(stroke.size() - 1));
		return result;
	}

	/* Finds a point on path that goes from start
	 * of path to end of path as t goes from 0.0 to 1.0.
	 * lengths and totalLength need to be pre-calculated.
	 */
	public static Point2D findPointOnPath(List<Point2D> path, double[] lengths, double totalLength, double t) {
		double l = t * totalLength; // set to the distance to travel along the 'path'
		int partIndex = -1; // if 'l' will be above all lengths, return last point
		for (int i = 1; i < path.size(); i++) {
			if (l <= lengths[i]) {
				partIndex = i - 1;
				l /= lengths[i]; // set 'l' to be between 0 and 1
				break;
			} else {
				l -= lengths[i];
			}
		}
		if (partIndex == -1) {
			return path.get(path.size() - 1);
		}
		Point2D p1 = path.get(partIndex);
		Point2D p2 = path.get(partIndex + 1);
		return tFunction(p1, p2, l);
	}

	/* Transforms CubicCurve2D while keeping it CubicCurve2D as there is no
	 * method provided by Java AWT to do it (AffineTransform.createTransformedShape
	 * returns a Path2D instance which is no longer a CubicCurve2D).
	 */
	public static CubicCurve2D transformCurve(CubicCurve2D c, AffineTransform at) {
		Point2D p1 = at.transform(c.getP1(), null);
		Point2D c1 = at.transform(c.getP1(), null);
		Point2D c2 = at.transform(c.getP1(), null);
		Point2D p2 = at.transform(c.getP1(), null);
		return new CubicCurve2D.Double(p1.getX(), p1.getY(), c1.getX(), c1.getY(), c2.getX(), c2.getY(), p2.getX(), p2.getY());
	}

	public static QuadCurve2D transformCurve(QuadCurve2D c, AffineTransform at) {
		Point2D p1 = at.transform(c.getP1(), null);
		Point2D c1 = at.transform(c.getCtrlPt(), null);
		Point2D p2 = at.transform(c.getP2(), null);
		return new QuadCurve2D.Double(p1.getX(), p1.getY(), c1.getX(), c1.getY(), p2.getX(), p2.getY());
	}

	// translates a list of curves and adds to a new list using transformCurve methods
	public static List<Shape> copyCurves(List<Shape> curves, AffineTransform transform) {
		List<Shape> result = new ArrayList<>(curves.size());
		for (Shape c : curves) {
			if (c instanceof CubicCurve2D) {
				result.add(transformCurve((CubicCurve2D) c, transform));
			} else if (c instanceof QuadCurve2D) {
				result.add(transformCurve((QuadCurve2D) c, transform));
			} else {
				throw new IllegalArgumentException("unsupported curve class " + c.getClass());
			}
		}
		return result;
	}

	// finds a point on curve that goes from start to end as t goes from 0.0 to 1.0.
	public static Point2D getPointOnCurve(Shape curve, double t) {
		Point2D[] bezierList;
		if (curve instanceof CubicCurve2D) {
			CubicCurve2D cc = (CubicCurve2D) curve;
			bezierList = new Point2D[]{cc.getP1(), cc.getCtrlP1(), cc.getCtrlP2(), cc.getP2()};
		} else if (curve instanceof QuadCurve2D) {
			QuadCurve2D qc = (QuadCurve2D) curve;
			bezierList = new Point2D[]{qc.getP1(), qc.getCtrlPt(), qc.getP2()};
		} else {
			throw new IllegalArgumentException("unsupported curve class: " + curve.getClass());
		}
		return calculateBezierList(bezierList, t);
	}

	// finds a point on Bezier curve with t argument going from 1.0 to 1.0
	public static Point2D calculateBezierList(Point2D[] list, double t) {
		while (list.length != 1)
			list = shortenBezierList(list, t);
		return list[0];
	}

	/* Converts a set of points to a smaller set where each point is
	 * created from 2 old points. Base function for Bezier curves.
	 */
	public static Point2D[] shortenBezierList(Point2D[] list, double t) {
		Point2D[] result = new Point2D[list.length - 1];
		for (int i = 0; i < result.length; i++)
			result[i] = tFunction(list[i], list[i + 1], t);
		return result;
	}

	// finds a point that goes from p1 to p2 as t goes from 0.0 to 1.0
	public static Point2D tFunction(Point2D p1, Point2D p2, double t) {
		return new Point2D.Double(p1.getX() + t * (p2.getX() - p1.getX()), p1.getY() + t * (p2.getY() - p1.getY()));
	}

	// finds a point between 2 points
	public static Point2D mid(Point2D p1, Point2D p2) {
		return new Point2D.Double((p1.getX() + p2.getX()) / 2, (p1.getY() + p2.getY()) / 2);
	}
}
