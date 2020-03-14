package recognition;

import java.util.List;

// used in comparison of a written answer to make a list of possible words
public class ComparisonResultEntry {
	private String word;
	private double difference; // can range from 0 (most similar) to 1 (most dissimilar)

	ComparisonResultEntry(String word, double difference) {
		this.word = word;
		this.difference = difference;
	}

	String getWord() {
		return word;
	}

	double getDifference() {
		return difference;
	}

	public static boolean contains(List<ComparisonResultEntry> list, String word) {
		for (ComparisonResultEntry c : list)
			if (c.word.equals(word))
				return true;
		return false;
	}
}
