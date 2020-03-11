package gui.scenes;

import gui.Bounds;
import gui.Scene;
import gui.Theme;
import gui.Window;
import gui.elements.Button;
import gui.elements.Label;
import gui.elements.TextField;

public class SceneLearningWordMeaning extends Scene {
	private Label wordLabel;
	private TextField meaningInput;

	public SceneLearningWordMeaning(Window holder) {
		super(holder);

		wordLabel = new Label("言葉", Theme.getFontJapanese(), this, new Bounds(0, -100, 500, 200, 0, 0));
		meaningInput = new TextField("Type word meaning here", this::handleInput, this, new Bounds(0, 200, 400, 60, 0, 0));
		meaningInput.setKanaInput(true);

		addElement(wordLabel);
		addElement(meaningInput);
		addElement(new Button("Return", () -> changeScene(sceneMain), this, new Bounds(-65, -10, 125, 30, 1, 1)));
	}

	private void handleInput() {
		meaningInput.flushText();
	}
}
