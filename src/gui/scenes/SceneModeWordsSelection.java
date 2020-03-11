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
	private RadioButton[] answerModeButtons;
	private Label warningLabel;

	private String[] fileNames;

	public SceneModeWordsSelection(Window holder) {
		super(holder);

		fileNames = Cards.getFilesList().toArray(new String[0]);
		fileCheckboxes = new Checkbox[fileNames.length];
		for (int i = 0; i < fileCheckboxes.length; i++) {
			Bounds b = new Bounds(100, 100 + i * 35, 200, 35);
			fileCheckboxes[i] = new Checkbox(fileNames[i], this, b);
			fileCheckboxes[i].setChecked(i == 0);
			addElement(fileCheckboxes[i]);
		}

		cardsModeButtons = new HorizontalRadioButtons(CARD_MODES, 1, this, new Bounds(-50, 80, 500, 40, 1, -1));
		cardsModeButtons.setSelectedButton(0);
		addElement(cardsModeButtons);

		answerModesGroup = new RadioButtonGroup();
		answerModeButtons = new RadioButton[ANSWER_MODES.length];
		for (int i = 0; i < ANSWER_MODES.length; i++) {
			Bounds b = new Bounds(-50, 170 + i * 40, 250, 40, 1, -1);
			answerModeButtons[i] = new RadioButton(ANSWER_MODES[i], i == 0, answerModesGroup, this, b);
			addElement(answerModeButtons[i]);
		}

		warningLabel = new Label("", 1, this, new Bounds(-10, -50, 500, 30, 1, 1));
		addElement(warningLabel);

		addElement(new Button("Apply", this::apply, this, new Bounds(-65, -10, 125, 30, 1, 1)));
	}

	private void apply() {
		List<String> selected = new ArrayList<>();
		for (int i = 0; i < fileNames.length; i++) {
			if (fileCheckboxes[i].isChecked())
				selected.add(fileNames[i]);
		}
		if (selected.isEmpty()) {
			warningLabel.changeText("Please select at least one words file");
		} else {
			Cards.setCurrentList(selected);
			changeScene(sceneMain);
		}
	}

	@Override
	protected void onShut() {
		super.onShut();
		warningLabel.changeText("");
	}
}
