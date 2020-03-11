package recognition;

import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;

// contains a written word as an answer used in recognition
public class WrittenAnswer {
	private List<WrittenStroke> strokes;
	/* Used to correctly fit size of a WrittenAnswer when comparing. Only
	 * set when creating from a card with an associated word, has default
	 * 0 value if created from writing an answer because it's going to be
	 * recreated for transforming anyway.
	 */
	private int characters;
	private List<StrokesConnection> strokesConnections;

	public WrittenAnswer(List<List<Point2D>> writing) {
		strokes = new ArrayList<>(writing.size());
		writing.forEach(pts -> strokes.add(new WrittenStroke(pts)));
	}

	WrittenAnswer(WordWriting loaded, int characters) {
		List<Stroke> from = loaded.getStrokes();
		strokes = new ArrayList<>(from.size());
		from.forEach(st -> strokes.add(st.toWrittenStroke()));
		this.characters = characters;
		findConnections();
	}

	// scaleToLength is the word length
	private WrittenAnswer(WrittenAnswer answer, int scaleToLength) {
		AffineTransform at = answer.getTransformForLength(scaleToLength);

		this.strokes = new ArrayList<>(answer.strokes.size());
		for (WrittenStroke ws : answer.strokes)
			strokes.add(new WrittenStroke(ws, at));

		findConnections();
	}

	private Rectangle2D getBounds() {
		Rectangle2D r = strokes.get(0).getBounds();
		for (int i = 1; i < strokes.size(); i++)
			r.add(strokes.get(i).getBounds());
		return r;
	}

	private void findConnections() {
		strokesConnections = new ArrayList<>(strokes.size() - 1);
		for (int i = 1; i < strokes.size(); i++)
			strokesConnections.add(new StrokesConnection(strokes.get(i - 1), strokes.get(i)));
	}

	/* Has to be called on a loaded WrittenAnswer instance and
	 * have the actual written answer passed as the argument.
	 * Returns 0 for the same, 1 for the most dissimilar writings
	 */
	double compareToWritten(WrittenAnswer to) {
		if (characters == 0)
			throw new Error("compareToWritten call on an instance with no characters value set");
		if (to.strokes.size() != this.strokes.size())
			return 1d;

		to = new WrittenAnswer(to, characters);

		double avgAngDifference = 0;
		for (int i = 0; i < strokes.size(); i++) {
			WrittenStroke s1 = this.strokes.get(i);
			WrittenStroke s2 = to.strokes.get(i);
			avgAngDifference += s1.compare(s2);
		}

		double angDifference = avgAngDifference / strokes.size();
		double posDifference = StrokesConnection.compare(this.strokesConnections, to.strokesConnections);

		return (angDifference + posDifference) / 2d;
	}

	// positions and scales a writing to have a similar location as loaded writings
	private AffineTransform getTransformForLength(int scaleToLength) {
		Rectangle2D bounds = getBounds();

		double neededW = CharacterWriting.CHARACTER_BOX_SIZE * scaleToLength;
		double neededH = CharacterWriting.CHARACTER_BOX_SIZE;
		double scaleX = neededW / bounds.getWidth(); // if bounds are stretched to be neededW wide
		double scaleY = neededH / bounds.getHeight(); // if bounds are stretched to be neededH high

		// resulting scale is minimum of scaleX and Y to make the resulting writing fit in needed size
		double scale = Math.min(scaleX, scaleY);
		double resultW = bounds.getWidth() * scale;
		double resultH = bounds.getHeight() * scale; // used to center the resulting writing on Y axis

		AffineTransform at = new AffineTransform();
		at.translate((neededW - resultW) / 2d, (neededH - resultH) / 2d);
		at.scale(scale, scale);
		at.translate(-bounds.getX(), -bounds.getY());

		return at;
	}
}
