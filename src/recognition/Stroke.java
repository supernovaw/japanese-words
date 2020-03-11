package recognition;

import java.awt.*;
import java.awt.geom.*;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

// stores strokes of KanjiVG loaded writings
public class Stroke {
	// used in defining size of the points list that is only used in comparison
	private static final int CURVE_PIECES = 4;
	private static final double DT = 1d / CURVE_PIECES;

	private List<Point2D> points;
	private double[] lengths;
	private double totalLength;
	private int pointsAmt;

	private List<Shape> segments;
	private Path2D path;

	// loads Stroke from writings file
	Stroke(ByteBuffer buffer) {
		// this part fills segments list with curve shapes
		int curvesAmt = buffer.getInt();
		segments = new ArrayList<>();
		for (int i = 0; i < curvesAmt; i++)
			readCurve(buffer, this);

		findPoints();
		path = new Path2D.Float();
		segments.forEach(s -> path.append(s, true));
	}

	/* Creates a Stroke from an already existing one with shifting it to
	 * take a place of some character in a word. Used by WordWriting.
	 */
	Stroke(Stroke original, int translateToChar) {
		AffineTransform translate = AffineTransform.getTranslateInstance(
				translateToChar * CharacterWriting.CHARACTER_BOX_SIZE, 0);

		segments = Curves.copyCurves(original.segments, translate);

		pointsAmt = original.pointsAmt;
		points = new ArrayList<>(pointsAmt);
		original.points.forEach(pt -> points.add(translate.transform(pt, null)));
		lengths = original.lengths;
		totalLength = original.totalLength;

		path = new Path2D.Float();
		segments.forEach(s -> path.append(s, true));
	}

	private void findPoints() {
		pointsAmt = 1 + CURVE_PIECES * segments.size();

		points = new ArrayList<>(pointsAmt);
		// all the segments except the first have their t=0 covered with the end of previous one
		points.add(Curves.getPointOnCurve(segments.get(0), 0));
		lengths = new double[pointsAmt]; // distance to the previous point (lengths[0] is always 0)
		for (int i = 0; i < segments.size(); i++) {
			for (int j = 1; j <= CURVE_PIECES; j++) {
				Point2D pCurrent = Curves.getPointOnCurve(segments.get(i), j * DT);
				Point2D pPrev = points.get(points.size() - 1);
				points.add(pCurrent);

				double dist = pCurrent.distance(pPrev);
				lengths[i * CURVE_PIECES + j] = dist;
				totalLength += dist;
			}
		}
	}

	Path2D getPath() {
		return path;
	}

	public Point2D getPoint(double t) {
		return Curves.findPointOnPath(points, lengths, totalLength, t);
	}

	WrittenStroke toWrittenStroke() {
		return new WrittenStroke(points, lengths, totalLength);
	}

	private static void readCurve(ByteBuffer buffer, Stroke stroke) {
		byte curveType = buffer.get();
		float x1, y1, xCtrl1, yCtrl1, xCtrl2, yCtrl2, x2, y2;
		switch (curveType) {
			case 'c':
				x1 = buffer.getFloat();
				y1 = buffer.getFloat();
				xCtrl1 = buffer.getFloat();
				yCtrl1 = buffer.getFloat();
				xCtrl2 = buffer.getFloat();
				yCtrl2 = buffer.getFloat();
				x2 = buffer.getFloat();
				y2 = buffer.getFloat();

				stroke.segments.add(new CubicCurve2D.Float(x1, y1, xCtrl1, yCtrl1, xCtrl2, yCtrl2, x2, y2));
				break;
			case 'q':
				x1 = buffer.getFloat();
				y1 = buffer.getFloat();
				xCtrl1 = buffer.getFloat();
				yCtrl1 = buffer.getFloat();
				x2 = buffer.getFloat();
				y2 = buffer.getFloat();

				stroke.segments.add(new QuadCurve2D.Float(x1, y1, xCtrl1, yCtrl1, x2, y2));
				break;
			default:
				throw new IllegalArgumentException("unknown curve type " +
						curveType + " (0x" + Integer.toHexString(curveType) + ")");
		}
	}
}
