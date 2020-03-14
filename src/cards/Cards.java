package cards;

import gui.Scene;
import main.Assets;
import recognition.Writings;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class Cards {
	public static final int CARDS_MODE_LEARN = 0;
	public static final int CARDS_MODE_MASTER = 1;
	public static final int CARDS_MODE_CHECK_KNOWLEDGE = 2;

	public static final int ANSWER_MODE_WORD_MEANING = 0;
	public static final int ANSWER_MODE_WORD_READING = 1;
	public static final int ANSWER_MODE_MEANING_WRITING = 2;

	private static Map<String, List<Card>> wordsFiles;
	private static List<String> filesList;
	private static List<String> selectedFiles;
	private static int cardsMode;
	private static int answerMode;

	private static List<Card> currentList;
	private static CardsMode currentMode;

	public static void init() {
		Writings.init();
		loadWordFiles();

		setFilesList(Collections.singletonList(filesList.get(0))); // select the first file
		answerMode = ANSWER_MODE_WORD_MEANING;
		cardsMode = CARDS_MODE_LEARN;
	}

	private static void loadWordFiles() {
		File[] fileObjects = new File("words").listFiles();
		if (fileObjects == null) {
			System.err.println("Words directory doesn't exist");
			System.exit(1);
		}

		wordsFiles = new HashMap<>();
		filesList = new ArrayList<>();
		for (File f : fileObjects) {
			if (f.isFile()) {
				String fileName = f.getName();

				int dot = fileName.lastIndexOf('.');
				if (dot != -1) fileName = fileName.substring(0, dot); // remove format from name

				try {
					wordsFiles.put(fileName, readCardsFromFile(loadText(f)));
					filesList.add(fileName);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}

		filesList.sort(String::compareTo);
		currentList = new ArrayList<>();
	}

	private static List<Card> readCardsFromFile(String contents) {
		String[] lines = contents.split(System.lineSeparator());
		List<Card> cards = new ArrayList<>();
		for (String l : lines)
			cards.add(Card.createFromLine(l));
		return cards;
	}

	private static String loadText(File f) throws IOException {
		return new String(Assets.loadStream(new FileInputStream(f)), StandardCharsets.UTF_8);
	}

	public static List<String> getFilesList() {
		return filesList;
	}

	public static List<Card> getCurrentList() {
		return currentList;
	}

	public static List<String> getSelectedFiles() {
		return selectedFiles;
	}

	/* When applying resets the current mode (if it's a
	 * test, for instance) a warning should be displayed
	 */
	public static boolean warnBeforeResetting() {
		return currentMode != null && !currentMode.isFinished();
	}

	public static int getCardsMode() {
		return cardsMode;
	}

	public static int getAnswerMode() {
		return answerMode;
	}

	public static void start(Scene from) {
		if (currentList.isEmpty()) {
			Scene.sceneModeWordsSelection.setWarning("Please select at least one words file");
			from.changeScene(Scene.sceneModeWordsSelection);
		} else {
			if (currentMode == null || currentMode.isFinished())
				initCurrentMode();
			currentMode.start(from);
		}
	}

	public static void setConfiguration(List<String> fileNames, int cardsMode, int answerMode) {
		setFilesList(fileNames);
		Cards.answerMode = answerMode;
		Cards.cardsMode = cardsMode;
		currentMode = null;
	}

	private static void setFilesList(List<String> fileNames) {
		Cards.selectedFiles = fileNames;
		currentList.clear();
		fileNames.forEach(name -> currentList.addAll(wordsFiles.get(name)));
	}

	private static void initCurrentMode() {
		LearningScene answerMode;
		switch (Cards.answerMode) {
			case ANSWER_MODE_WORD_MEANING:
				answerMode = Scene.sceneLearningWordMeaning;
				break;
			case ANSWER_MODE_WORD_READING:
				answerMode = Scene.sceneLearningWordReading;
				break;
			case ANSWER_MODE_MEANING_WRITING:
				answerMode = Scene.sceneLearningMeaningWriting;
				break;
			default:
				throw new Error("Unknown answer mode " + Cards.answerMode);
		}
		switch (cardsMode) {
			case CARDS_MODE_LEARN:
				currentMode = new CardsModeLearn(currentList);
				break;
			case CARDS_MODE_MASTER:
				currentMode = new CardsModeMaster(currentList, answerMode);
				break;
			case CARDS_MODE_CHECK_KNOWLEDGE:
				currentMode = new CardsModeCheckKnowledge(currentList);
				break;
			default:
				throw new Error("Unknown cards mode " + cardsMode);
		}
	}

	/* Makes a shuffled copy of list with making sure
	 * that the first element in the resulting list will
	 * not be the same as the last in the argument list.
	 */
	static <T> List<T> shuffle(List<T> list) {
		List<T> listCopy = new ArrayList<>(list);
		List<T> result = new ArrayList<>();

		result.add(listCopy.remove(getRandom(listCopy, list.get(list.size() - 1))));
		while (!listCopy.isEmpty()) {
			int i = (int) (Math.random() * listCopy.size());
			result.add(listCopy.remove(i));
		}
		return result;
	}

	/* Returns the index of a random element from list with
	 * making sure that the chosen one will not be 'exclude'
	 */
	private static <T> int getRandom(List<T> list, T exclude) {
		int index = (int) (Math.random() * (list.size() - 1));
		if (index == list.indexOf(exclude))
			index = list.size() - 1;
		return index;
	}
}
