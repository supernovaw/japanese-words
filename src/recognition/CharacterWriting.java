package recognition;

import java.nio.ByteBuffer;
import java.util.List;

// stores loaded writings from KanjiVG
public class CharacterWriting {
	// this number was specified in all KanjiVG files
	public static final int CHARACTER_BOX_SIZE = 109;

	private Stroke[] strokes;

	CharacterWriting(ByteBuffer buffer) {
		int strokesAmt = buffer.getInt();
		strokes = new Stroke[strokesAmt];
		for (int i = 0; i < strokesAmt; i++)
			strokes[i] = new Stroke(buffer);
	}

	/* Creation of WordWriting requires shifting characters before adding
	 * their strokes to the list. This method adds strokes of this object
	 * to a list with shifting them accordingly to their position in a word
	 */
	void addStrokesTranslated(int charPos, List<Stroke> toList) {
		for (Stroke s : strokes)
			toList.add(new Stroke(s, charPos));
	}
}
