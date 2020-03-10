package cards;

public class Card {
	private String word;
	private String reading;
	private String meaning;

	private Card(String word, String reading, String meaning) {
		if (!Kana.isValidKanaReading(reading))
			throw new IllegalArgumentException(reading + " contains 1 or more non-kana characters");

		this.word = word;
		this.reading = reading;
		this.meaning = meaning;
	}

	public String getWord() {
		return word;
	}

	public String getReading() {
		return reading;
	}

	public String getMeaning() {
		return meaning;
	}

	static Card createFromLine(String line) {
		String[] split = line.split("\t");
		if (split.length != 3)
			throw new IllegalArgumentException(line + " contains incorrect number of parts (" + split.length + ")");
		return new Card(split[0], split[1], split[2]);
	}
}
