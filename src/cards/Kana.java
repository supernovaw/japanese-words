package cards;

import java.awt.event.KeyEvent;

/* Manages keyboard input using Japanese kana layout.
 * Supports both hiragana and katakana. Provides the
 * same options for both syllabaries: typing any of 46
 * gojūon table letters, their small (yōon) variants,
 * adding dakuten or handakuten to them if available
 * and typing the chōonpu character.
 */
public final class Kana {
	public static final int HIRAGANA = 0;
	public static final int KATAKANA = 1;
	public static final int DAKUTEN_KEYCODE = KeyEvent.VK_OPEN_BRACKET;
	public static final int HANDAKUTEN_KEYCODE = KeyEvent.VK_CLOSE_BRACKET;

	private static final char CHOONPU = '\u30FC'; // 'ー' 長音符 (chōonpu, prolonged sound mark)

	/* Special input cases. Chōonpu, 'wo' and sokuon are typed
	 * while holding shift, and 'n' lies outside gojūon table.
	 */
	private static final int CHOONPU_KEYCODE = KeyEvent.VK_MINUS;
	private static final int WO_LETTER_KEYCODE = KeyEvent.VK_0;
	private static final int SOKUON_KEYCODE = KeyEvent.VK_Z;
	private static final int N_LETTER_KEYCODE = KeyEvent.VK_Y;

	private static final char HIRAGANA_N = '\u3093'; // 'ん'
	private static final char KATAKANA_N = '\u30F3'; // 'ン'
	private static final char HIRAGANA_SOKUON = '\u3063'; // 'っ'
	private static final char KATAKANA_SOKUON = '\u30C3'; // 'ッ'

	/* The size of gojūon arrays. The only exception is
	 * the letter N (ん/ン) which lies outside gojūon table.
	 */
	private static final int GOJUON_ROWS = 5; // A, I, U, E, O
	private static final int GOJUON_COLUMNS = 10; // vowel, K, S, T, N, F, M, Y, R, W

	private static final KanaChar[][] HIRAGANA_GOJUON = {
			getGojuonColumn("あいうえお　　　　　　　　　　ぁぃぅぇぉ"),
			getGojuonColumn("かきくけこがぎぐげご　　　　　　　　　　"),
			getGojuonColumn("さしすせそざじずぜぞ　　　　　　　　　　"),
			getGojuonColumn("たちつてとだぢづでど　　　　　　　　　　"),
			getGojuonColumn("なにぬねの　　　　　　　　　　　　　　　"),
			getGojuonColumn("はひふへほばびぶべぼぱぴぷぺぽ　　　　　"),
			getGojuonColumn("まみむめも　　　　　　　　　　　　　　　"),
			getGojuonColumn("や　ゆ　よ　　　　　　　　　　ゃ　ゅ　ょ"),
			getGojuonColumn("らりるれろ　　　　　　　　　　　　　　　"),
			getGojuonColumn("わ　　　を　　　　　　　　　　　　　　　")
	};
	private static final KanaChar[][] KATAKANA_GOJUON = {
			getGojuonColumn("アイウエオ　　　　　　　　　　ァィゥェォ"),
			getGojuonColumn("カキクケコガギグゲゴ　　　　　　　　　　"),
			getGojuonColumn("サシスセソザジズゼゾ　　　　　　　　　　"),
			getGojuonColumn("タチツテトダヂヅデド　　　　　　　　　　"),
			getGojuonColumn("ナニヌネノ　　　　　　　　　　　　　　　"),
			getGojuonColumn("ハヒフヘホバビブベボパピプペポ　　　　　"),
			getGojuonColumn("マミムメモ　　　　　　　　　　　　　　　"),
			getGojuonColumn("ヤ　ユ　ヨ　　　　　　　　　　ャ　ュ　ョ"),
			getGojuonColumn("ラリルレロ　　　　　　　　　　　　　　　"),
			getGojuonColumn("ワ　　　ヲ　　　　　　　　　　　　　　　")
	};
	/* Keys for typing gojūon letters. There are 5 empty cells in the
	 * gojūon table, those cells have 0 values in this map. One letter
	 * ('wo') isn't in this map, because it is typed as Shift + 0
	 * (0 alone is used for typing 'wa'). Other special cases are
	 * 'n' (outside gojūon table) and sokuon.
	 */
	private static final int[][] GOJUON_KEY_MAP = {
			{KeyEvent.VK_3, KeyEvent.VK_E, KeyEvent.VK_4, KeyEvent.VK_5, KeyEvent.VK_6},
			{KeyEvent.VK_T, KeyEvent.VK_G, KeyEvent.VK_H, KeyEvent.VK_QUOTE, KeyEvent.VK_B},
			{KeyEvent.VK_X, KeyEvent.VK_D, KeyEvent.VK_R, KeyEvent.VK_P, KeyEvent.VK_C},
			{KeyEvent.VK_Q, KeyEvent.VK_A, KeyEvent.VK_Z, KeyEvent.VK_W, KeyEvent.VK_S},
			{KeyEvent.VK_U, KeyEvent.VK_I, KeyEvent.VK_1, KeyEvent.VK_COMMA, KeyEvent.VK_K},
			{KeyEvent.VK_F, KeyEvent.VK_V, KeyEvent.VK_2, KeyEvent.VK_EQUALS, KeyEvent.VK_MINUS},
			{KeyEvent.VK_J, KeyEvent.VK_N, KeyEvent.VK_BACK_SLASH, KeyEvent.VK_SLASH, KeyEvent.VK_M},
			{KeyEvent.VK_7, 0b00000000000, KeyEvent.VK_8, 0b00000000000, KeyEvent.VK_9},
			{KeyEvent.VK_O, KeyEvent.VK_L, KeyEvent.VK_PERIOD, KeyEvent.VK_SEMICOLON, KeyEvent.VK_BACK_QUOTE},
			{KeyEvent.VK_0, 0, 0, 0, 0}
	};

	/* Makes gojūon tables code more comprehensive uniting all
	 * constructor calls to a single place. Indication of 0x0
	 * char is done with the ideographic space, to make code
	 * look even. Example of difference with uneven spaces:
	 *
	 * Ideographic spaces
	 * "あいうえお　　　　　　　　　　ぁぃぅぇぉ" even
	 * "なにぬねの　　　　　　　　　　　　　　　" even
	 * "はひふへほばびぶべぼぱぴぷぺぽ　　　　　" even
	 *
	 * Normal spaces
	 * "あいうえお          ぁぃぅぇぉ" uneven
	 * "なにぬねの               " uneven
	 * "はひふへほばびぶべぼぱぴぷぺぽ     " uneven
	 */
	private static KanaChar[] getGojuonColumn(String col) {
		char[] cs = col.toCharArray();
		for (int i = 0; i < cs.length; i++)
			if (cs[i] == '\u3000') // '　' or the ideographic space character
				cs[i] = 0;
		return new KanaChar[]{
				new KanaChar(cs[0], cs[5], cs[10], cs[15]),
				new KanaChar(cs[1], cs[6], cs[11], cs[16]),
				new KanaChar(cs[2], cs[7], cs[12], cs[17]),
				new KanaChar(cs[3], cs[8], cs[13], cs[18]),
				new KanaChar(cs[4], cs[9], cs[14], cs[19])
		};
	}

	/* Converts a keycode corresponding to a letter in the gojūon table. If shift is
	 * held and there is a yōon character available, it is returned. Special cases:
	 *
	 * Shift + Minus = chōonpu (ー)
	 * Shift + 0 = を/ヲ ('wo' is the only character whose normal form typed with shift)
	 * Shift + Z = っ/ッ (scaled-down version of つ/ツ 'tsu' or sokuon, gemination character)
	 * Y = ん/ン ('n' character)
	 */
	public static char getTypedChar(int keyCode, boolean shift, int kanaType) {
		if (kanaType != HIRAGANA && kanaType != KATAKANA)
			throw new IllegalArgumentException("unknown kanaType " + kanaType);
		KanaChar[][] gojuon = kanaType == HIRAGANA ? HIRAGANA_GOJUON : KATAKANA_GOJUON;

		// special cases
		if (keyCode == CHOONPU_KEYCODE && shift)
			return CHOONPU;
		if (keyCode == WO_LETTER_KEYCODE && shift)
			return gojuon[9][4].normal; // column 10 row 5 is 'wo'
		if (keyCode == SOKUON_KEYCODE && shift)
			return kanaType == HIRAGANA ? HIRAGANA_SOKUON : KATAKANA_SOKUON;
		if (keyCode == N_LETTER_KEYCODE)
			return kanaType == HIRAGANA ? HIRAGANA_N : KATAKANA_N;

		KanaChar letter = null;
		outerLoop:
		for (int row = 0; row < GOJUON_ROWS; row++)
			for (int col = 0; col < GOJUON_COLUMNS; col++)
				if (GOJUON_KEY_MAP[col][row] == keyCode) {
					letter = gojuon[col][row];
					break outerLoop;
				}

		if (letter == null) { // if there is no letter associated with keyCode
			return 0;
		}

		if (shift && letter.yoon != 0) // if yōon is requested and is available
			return letter.yoon;
		else
			return letter.normal;
	}

	/* Return a dakuten version of some character if it is
	 * available or the argument character itself if there
	 * is no such character or if 'c' is not even known as
	 * a gojūon letter.
	 */
	public static char addDakuten(char c) {
		for (int row = 0; row < GOJUON_ROWS; row++)
			for (int col = 0; col < GOJUON_COLUMNS; col++) {
				KanaChar hiragana = HIRAGANA_GOJUON[col][row];
				KanaChar katakana = KATAKANA_GOJUON[col][row];
				// if there is no dakuten, return char argument
				if (hiragana.normal == c)
					return hiragana.dakuten == 0 ? c : hiragana.dakuten;
				if (katakana.normal == c)
					return katakana.dakuten == 0 ? c : katakana.dakuten;
			}
		return c;
	}

	/* Return a handakuten version of some character if it is
	 * available or the argument character itself if there
	 * is no such character or if 'c' is not even known as
	 * a gojūon letter.
	 */
	public static char addHandakuten(char c) {
		for (int row = 0; row < GOJUON_ROWS; row++)
			for (int col = 0; col < GOJUON_COLUMNS; col++) {
				KanaChar hiragana = HIRAGANA_GOJUON[col][row];
				KanaChar katakana = KATAKANA_GOJUON[col][row];
				// if there is no handakuten, return char argument
				if (hiragana.normal == c)
					return hiragana.handakuten == 0 ? c : hiragana.handakuten;
				if (katakana.normal == c)
					return katakana.handakuten == 0 ? c : katakana.handakuten;
			}
		return c;
	}

	// checks if a string only contains kana reading characters (including chōonpu)
	public static boolean isValidKanaReading(String reading) {
		for (int i = 0; i < reading.length(); i++) {
			char c = reading.charAt(i);
			if (!isKanaChar(c) && c != CHOONPU)
				return false;
		}
		return true;
	}

	private static boolean isKanaChar(char c) {
		// 'n' and sokuon are not listen in gojūon and need to be checked separately
		if (c == HIRAGANA_N || c == KATAKANA_N ||
				c == HIRAGANA_SOKUON || c == KATAKANA_SOKUON)
			return true;

		for (int col = 0; col < GOJUON_COLUMNS; col++)
			for (int row = 0; row < GOJUON_ROWS; row++)
				if (HIRAGANA_GOJUON[col][row].isThisLetter(c) ||
						KATAKANA_GOJUON[col][row].isThisLetter(c))
					return true;
		return false;
	}

	// represents a letter either from hiragana or katakana, featuring its versions
	private static class KanaChar {
		// normal appearance of this 五十音 (gojūon) letter
		private final char normal;

		/* Some letters have variations declared here.
		 * If a letter doesn't have this variation, the value is 0x0.
		 *
		 * 濁点, i.e. だ, ド, ぜ (dakuten)
		 * 半濁点, i.e. ぱ, ぴ, ぽ (handakuten)
		 * 拗音, i.e. ょ, ャ, ァ (yōon)
		 */
		private final char dakuten;
		private final char handakuten;
		private final char yoon;

		private KanaChar(int normal, int dakuten, int handakuten, int yoon) {
			this.normal = (char) normal;
			this.dakuten = (char) dakuten;
			this.handakuten = (char) handakuten;
			this.yoon = (char) yoon;
		}

		// checks if a char is any of this letter variants
		private boolean isThisLetter(char c) {
			return c == normal ||
					c == dakuten ||
					c == handakuten ||
					c == yoon;
		}
	}
}
