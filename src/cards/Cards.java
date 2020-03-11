package cards;

import main.Assets;
import recognition.Writings;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public class Cards {
	private static Map<String, List<Card>> wordsFiles;
	private static List<String> filesList;

	private static List<Card> currentList;

	public static void init() {
		Writings.init();
		loadWordFiles();
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
		currentList = new ArrayList<>(wordsFiles.get(filesList.get(0)));
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

	public static void forEachCurrent(Consumer<Card> c) {
		currentList.forEach(c);
	}

	public static void setCurrentList(List<String> fileNames) {
		currentList.clear();
		fileNames.forEach(name -> currentList.addAll(wordsFiles.get(name)));
	}

	public static int getCurrentSize() {
		return currentList.size();
	}
}
