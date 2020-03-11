package recognition;

import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;

// contains angles of written and loaded strokes used in recognition
class WrittenStroke {
	private static final int ANGLES_ARRAY_LENGTH = 8;

	private List<Point2D> points;

	// totalLength isn't set when using transform constructor, but it isn't necessary
	private double[] angles;

	WrittenStroke(List<Point2D> points, double[] lengths, double totalLength) {
		this.points = points;
		calculateAngles(lengths, totalLength);
	}

	WrittenStroke(List<Point2D> points) {
		this.points = points;
		calculateAngles();
	}

	WrittenStroke(WrittenStroke stroke, AffineTransform at) {
		points = new ArrayList<>(stroke.points.size());
		for (Point2D p : stroke.points)
			points.add(at.transform(p, null));
		angles = stroke.angles;
	}

	private void calculateAngles() {
		double[] lengths = new double[points.size()];
		double totalLength = 0;
		for (int i = 1; i < points.size(); i++) {
			lengths[i] = points.get(i - 1).distance(points.get(i));
			totalLength += lengths[i];
		}

		calculateAngles(lengths, totalLength);
	}

	private void calculateAngles(double[] lengths, double totalLength) {
		angles = new double[ANGLES_ARRAY_LENGTH];
		Point2D prev = points.get(0);
		for (int i = 0; i < ANGLES_ARRAY_LENGTH; i++) {
			double t = (i + 1d) / ANGLES_ARRAY_LENGTH;
			Point2D next = getPoint(t, lengths, totalLength);
			angles[i] = Math.atan2(next.getY() - prev.getY(), next.getX() - prev.getX());
			prev = next;
		}
	}

	// finds a point on the stroke (0 < t < 1)
	private Point2D getPoint(double t, double[] lengths, double totalLength) {
		return Curves.findPointOnPath(points, lengths, totalLength, t);
	}

	double compare(WrittenStroke to) {
		return getAngleDifference(angles, to.angles);
	}

	Rectangle2D getBounds() {
		Point2D first = points.get(0);
		Rectangle2D r = new Rectangle2D.Double(first.getX(), first.getY(), 0, 0);
		for (int i = 1; i < points.size(); i++)
			r.add(points.get(i));
		return r;
	}

	Point2D getStart() {
		return points.get(0);
	}

	Point2D getEnd() {
		return points.get(points.size() - 1);
	}

	// returns a value from 0 (similar strokes) to 1 (most dissimilar)
	private static double getAngleDifference(double[] angles1, double[] angles2) {
		double[] resultArray = new double[ANGLES_ARRAY_LENGTH];
		for (int i = 0; i < resultArray.length; i++)
			resultArray[i] = getAngleDifference(angles1[i], angles2[i]);

		double resultAngle = 0; // find the average of resultArray values
		for (int i = 1; i < resultArray.length; i++)
			resultAngle += resultArray[i];
		resultAngle /= resultArray.length;
		resultAngle /= Math.PI; // as Pi is the highest possible result, make output range from 0.0 to 1.0

		return resultAngle;
	}

	// for angles between -Pi to Pi (finds the difference in the shortest direction)
	private static double getAngleDifference(double ang1, double ang2) {
		double diff = Math.abs(ang2 - ang1);
		if (diff > Math.PI)
			diff = 2 * Math.PI - diff;
		return diff;
	}
}
