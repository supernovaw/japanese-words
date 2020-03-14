package gui.scenes;

import cards.Cards;
import gui.Bounds;
import gui.Scene;
import gui.Window;
import gui.elements.*;

import java.util.ArrayList;
import java.util.List;

public class SceneModeWordsSelection extends Scene {
	private static final String[] CARD_MODES = {"Learn", "Master", "Check Knowledge"};
	private static final String[] ANSWER_MODES = {"Word — Meaning", "Word — Reading", "Meaning — Word",
			"Meaning — Reading", "Reading — Word", "Reading — Meaning", "Collect Word"};

	private Checkbox[] fileCheckboxes;
	private HorizontalRadioButtons cardsModeButtons;
	private RadioButtonGroup answerModesGroup;
	private Label warningLabel;

	private String[] fileNames;

	public SceneModeWordsSelection(Window holder) {
		super(holder);

		List<String> selectedFiles = Cards.getSelectedFiles();

		fileNames = Cards.getFilesList().toArray(new String[0]);
		fileCheckboxes = new Checkbox[fileNames.length];
		for (int i = 0; i < fileCheckboxes.length; i++) {
			Bounds b = new Bounds(100, 100 + i * 35, 200, 35);
			fileCheckboxes[i] = new Checkbox(fileNames[i], this, b);
			fileCheckboxes[i].setChecked(selectedFiles.contains(fileNames[i]));
			addElement(fileCheckboxes[i]);
		}

		cardsModeButtons = new HorizontalRadioButtons(CARD_MODES, 1, this, new Bounds(-50, 80, 500, 40, 1, -1));
		cardsModeButtons.setSelectedButton(Cards.getCardsMode());
		addElement(cardsModeButtons);

		answerModesGroup = new RadioButtonGroup(Cards.getAnswerMode());
		RadioButton[] answerModeButtons = new RadioButton[ANSWER_MODES.length];
		for (int i = 0; i < ANSWER_MODES.length; i++) {
			Bounds b = new Bounds(-50, 170 + i * 40, 250, 40, 1, -1);
			answerModeButtons[i] = new RadioButton(ANSWER_MODES[i], answerModesGroup, this, b);
			addElement(answerModeButtons[i]);
		}

		warningLabel = new Label("", 1, this, new Bounds(-345, -10, 500, 30, 1, 1));
		addElement(warningLabel);

		addElement(new Button("Cancel", () -> changeScene(sceneMain), this, new Bounds(-200, -10, 125, 30, 1, 1)));
		addElement(new Button("Apply", this::apply, this, new Bounds(-65, -10, 125, 30, 1, 1)));
	}

	private void apply() {
		List<String> selected = getSelectedFiles();
		if (selected.isEmpty()) {
			warningLabel.changeText("Please select at least one words file");
			return;
		}

		int cardsMode = cardsModeButtons.getSelectedButton();
		int answerMode = answerModesGroup.getSelectedIndex();
		Cards.setConfiguration(selected, cardsMode, answerMode);
		changeScene(sceneMain);
	}

	private List<String> getSelectedFiles() {
		List<String> result = new ArrayList<>();
		for (int i = 0; i < fileNames.length; i++)
			if (fileCheckboxes[i].isChecked())
				result.add(fileNames[i]);

		return result;
	}

	public void setWarning(String w) {
		warningLabel.setText(w);
	}

	@Override
	protected void onShut() {
		super.onShut();
		warningLabel.changeText("");
	}

	@Override
	protected void onDisplay() {
		super.onDisplay();
		if (Cards.warnBeforeResetting())
			warningLabel.setText("Clicking Apply will reset the previous mode");
	}
}
