package gui.scenes;

import cards.Card;
import cards.CardsMode;
import cards.LearningScene;
import gui.Bounds;
import gui.Theme;
import gui.Window;
import gui.elements.Button;
import gui.elements.Label;
import gui.elements.WordWritingArea;
import recognition.Writings;
import recognition.WrittenAnswer;

public class SceneLearningMeaningWriting extends LearningScene {
	private CardsMode mode;

	private Label meaning;
	private WordWritingArea writingArea;
	private Label hint;

	public SceneLearningMeaningWriting(Window holder) {
		super(holder);

		meaning = new Label("", Theme.getFontEnglish(), this, new Bounds(0, -155, 700, 60, 0, 0));
		writingArea = new WordWritingArea(this::handleInput, this, new Bounds(0, 0, 700, 250, 0, 0));
		hint = new Label("", Theme.getFontJapanese(), this, new Bounds(0, 175, 500, 100, 0, 0));

		addElement(meaning);
		addElement(writingArea);
		addElement(hint);
		addElement(new Button("Return", () -> changeScene(sceneMain), this, new Bounds(-65, -10, 125, 30, 1, 1)));
	}

	private void handleInput() {
		WrittenAnswer answer = writingArea.flushWriting();
		boolean correct = Writings.isCorrect(answer, mode.getCurrent().getWord());
		mode.next(this, correct);
	}

	@Override
	public void setMode(CardsMode m) {
		mode = m;
	}

	@Override
	public void setCard(Card c) {
		meaning.setText(c.getMeaning());
	}

	@Override
	public void changeCard(Card c) {
		meaning.changeText(c.getMeaning());
	}

	@Override
	public void setHint(Card c) {
		hint.changeText(c.getWord());
	}

	@Override
	protected void removeHint() {
		hint.changeText("");
	}
}
