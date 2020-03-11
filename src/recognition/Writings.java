package recognition;

import cards.Cards;
import main.Assets;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

// utility class for writings
public final class Writings {
	// if an answer has inaccuracy value above this, it is considered wrong
	public static final double CORRECT_ANSWER_THRESHOLD = 0.3;

	// map of Unicode code points and associated writings
	private static HashMap<Integer, CharacterWriting> characterWritings;

	// those 2 maps have the same size and key set
	private static HashMap<String, WordWriting> wordWritings;
	private static HashMap<String, WrittenAnswer> comparisonInfo;

	public static void init() {
		loadWritings();
		wordWritings = new HashMap<>();
		comparisonInfo = new HashMap<>();
	}

	private static void loadWritings() {
		ByteBuffer buffer;

		// load writings file
		try {
			buffer = ByteBuffer.wrap(Assets.loadBytes("writings"));
		} catch (FileNotFoundException e) {
			System.err.println("Writings file doesn't exist");
			System.exit(1);
			return;
		} catch (IOException e) {
			e.printStackTrace();
			return;
		}

		// read writings
		characterWritings = new HashMap<>();
		while (buffer.hasRemaining()) {
			int symbol = buffer.getInt();
			buffer.position(buffer.position() + 4); // skip length info

			CharacterWriting writing = new CharacterWriting(buffer);
			characterWritings.put(symbol, writing);
		}
	}

	// checks if all of the word's characters are in the writings map
	public static boolean isSupported(String word) {
		for (int codePoint : word.codePoints().toArray())
			if (!characterWritings.containsKey(codePoint))
				return false;
		return true;
	}

	public static CharacterWriting get(int character) {
		return characterWritings.get(character);
	}

	/* In order to be correct an answer needs to:
	 * have the same number of strokes
	 * be more similar to answer than any word in Cards.currentList
	 * pass CORRECT_ANSWER_THRESHOLD
	 *
	 * The threshold check is needed because an incorrect
	 * answer may simply coincide with the expected
	 * one while being completely dissimilar to it.
	 */
	public static boolean isCorrect(WrittenAnswer answer, String word) {
		WrittenAnswer expectedAnswer = getComparisonInfo(word);
		double difference = expectedAnswer.compareToWritten(answer);
		if (difference > CORRECT_ANSWER_THRESHOLD)
			return false;

		List<ComparisonResultEntry> results = new ArrayList<>(Cards.getCurrentSize());
		results.add(new ComparisonResultEntry(word, difference));
		Cards.forEachCurrent(card -> {
			String w = card.getWord();
			if (w.equals(word)) // if this is the already calculated value
				return;
			double diff = getComparisonInfo(w).compareToWritten(answer);
			results.add(new ComparisonResultEntry(w, diff));
		});

		results.sort(Comparator.comparingDouble(ComparisonResultEntry::getDifference));
		String mostSimilar = results.get(0).getWord();
		return mostSimilar.equals(word);
	}

	public static WordWriting getWriting(String word) {
		return wordWritings.get(word);
	}

	public static WrittenAnswer getComparisonInfo(String word) {
		return comparisonInfo.get(word);
	}

	/* When a Card is created, it calls this method to add word writings
	 * here. Some cards have the same kanji but different readings, i.e.
	 * 月 as がつ and 月 as つき, so there is a check if it is already in the maps.
	 */
	public static void register(String word) {
		if (!wordWritings.containsKey(word)) {
			int characters = word.codePoints().toArray().length;
			WordWriting ww = new WordWriting(word);
			WrittenAnswer wa = new WrittenAnswer(ww, characters);
			wordWritings.put(word, ww);
			comparisonInfo.put(word, wa);
		}
	}
}
