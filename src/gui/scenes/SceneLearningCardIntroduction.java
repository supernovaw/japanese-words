package gui.scenes;

import cards.Card;
import cards.CardsMode;
import cards.LearningScene;
import gui.Bounds;
import gui.Theme;
import gui.Window;
import gui.elements.Label;

import java.awt.event.KeyEvent;

public class SceneLearningCardIntroduction extends LearningScene {
	private CardsMode mode;

	private Label reading;
	private Label word;
	private Label meaning;

	public SceneLearningCardIntroduction(Window holder) {
		super(holder);

		reading = new Label("", Theme.getFontJapanese(), this, new Bounds(0, -150, 1000, 100, 0, 0));
		word = new Label("", Theme.getFontJapanese(), this, new Bounds(0, 0, 1000, 200, 0, 0));
		meaning = new Label("", Theme.getFontEnglish(), this, new Bounds(0, 150, 1000, 100, 0, 0));

		addElement(reading);
		addElement(word);
		addElement(meaning);
	}

	@Override
	protected void keyPressed(KeyEvent e) {
		if (e.getKeyCode() == KeyEvent.VK_SPACE || e.getKeyCode() == KeyEvent.VK_ENTER)
			mode.next(this, true);
	}

	@Override
	public void setCard(Card c) {
		reading.setText(c.getReading());
		word.setText(c.getWord());
		meaning.setText(c.getMeaning());
	}

	@Override
	public void changeCard(Card c) {
		reading.changeText(c.getReading());
		word.changeText(c.getWord());
		meaning.changeText(c.getMeaning());
	}

	@Override
	public void setMode(CardsMode m) {
		mode = m;
	}

	@Override
	public void setHint(Card c) {
	}

	@Override
	protected void removeHint() {
	}
}
