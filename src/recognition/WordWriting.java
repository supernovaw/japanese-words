package recognition;

import java.awt.geom.Path2D;
import java.util.ArrayList;
import java.util.List;

// stores word writings made of KanjiVG character writings
public class WordWriting {
	private Path2D path;
	private List<Stroke> strokes;

	public WordWriting(String word) {
		if (word.isEmpty())
			throw new IllegalArgumentException("empty word argument");

		strokes = new ArrayList<>();
		for (int i = 0; i < word.length(); i++) {
			CharacterWriting ch = Writings.get(word.charAt(i));
			ch.addStrokesTranslated(i, strokes);
		}

		path = new Path2D.Double();
		strokes.forEach(stroke -> path.append(stroke.getPath(), false));

	}

	public Path2D getPath() {
		return path;
	}

	List<Stroke> getStrokes() {
		return strokes;
	}
}
