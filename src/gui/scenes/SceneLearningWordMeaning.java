package gui.scenes;

import cards.Card;
import cards.Cards;
import cards.CardsMode;
import cards.LearningScene;
import gui.Bounds;
import gui.Theme;
import gui.Window;
import gui.elements.Button;
import gui.elements.Label;
import gui.elements.TextField;

public class SceneLearningWordMeaning extends LearningScene {
	private CardsMode mode;

	private Label wordLabel;
	private TextField meaningInput;
	private Label hint;

	public SceneLearningWordMeaning(Window holder) {
		super(holder);

		wordLabel = new Label("", Theme.getFontJapanese(), this, new Bounds(0, -80, 1000, 300, 0, 0));
		meaningInput = new TextField("Type word meaning here", this::handleInput, this, new Bounds(0, 100, 400, 60, 0, 0));
		hint = new Label("", Theme.getFontEnglish(), this, new Bounds(0, 180, 1000, 100, 0, 0));

		addElement(wordLabel);
		addElement(meaningInput);
		addElement(hint);
		addElement(new Button("Return", () -> changeScene(sceneMain), this, new Bounds(-65, -10, 125, 30, 1, 1)));
	}

	private void handleInput() {
		String input = meaningInput.flushText();
		String word = mode.getCurrent().getWord();
		boolean correct = Cards.isMeaningCorrect(input, word);
		mode.next(this, correct);
	}

	protected void removeHint() {
		hint.changeText("");
	}

	@Override
	public void setMode(CardsMode m) {
		mode = m;
	}

	@Override
	public void setCard(Card c) {
		meaningInput.triggerFocus();
		wordLabel.setText(c.getWord());
	}

	@Override
	public void changeCard(Card c) {
		meaningInput.triggerFocus();
		wordLabel.changeText(c.getWord());
		removeHint();
	}

	@Override
	public void setHint(Card c) {
		hint.changeText(c.getMeaning());
	}
}
