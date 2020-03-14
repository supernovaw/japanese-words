package cards;

import gui.Scene;

import java.util.ArrayList;
import java.util.List;

public class CardsModeMaster extends CardsMode {
	// when a card is answered correctly this many times in a row, it is removed from the list
	private static final int REMOVE_ON_STREAK = 3;

	private LearningScene sceneType;
	private List<MasteredCard> cards;
	private int index;
	private boolean finished;

	public CardsModeMaster(List<Card> cardsList, LearningScene sceneType) {
		this.sceneType = sceneType;
		this.cards = new ArrayList<>();
		for (Card c : Cards.shuffle(cardsList))
			this.cards.add(new MasteredCard(c));
	}

	@Override
	public void next(LearningScene source, boolean correct) {
		MasteredCard current = cards.get(index);
		if (correct) {
			current.correctAnswersStreak++;

			if (current.correctAnswersStreak == REMOVE_ON_STREAK) {
				if (cards.size() == 1) {
					finish(source);
					return;
				}

				cards.remove(index);
				index--;
			}
		} else {
			current.correctAnswersStreak = 0;
		}
		index++;
		if (index == cards.size()) {
			cards = Cards.shuffle(cards);
			index = 0;
		}

		sceneType.changeCard(cards.get(index).card);
	}

	private void finish(LearningScene source) {
		finished = true;
		source.changeScene(Scene.sceneMain);
	}

	@Override
	public void start(Scene from) {
		sceneType.setMode(this);
		sceneType.setCard(cards.get(index).card);
		from.changeScene(sceneType);
	}

	@Override
	public Card getCurrent() {
		return cards.get(index).card;
	}

	@Override
	public boolean isFinished() {
		return finished;
	}

	private static class MasteredCard {
		Card card;
		int correctAnswersStreak;

		MasteredCard(Card card) {
			this.card = card;
		}
	}
}
