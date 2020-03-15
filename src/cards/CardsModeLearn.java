package cards;

import gui.Scene;

import java.util.ArrayList;
import java.util.List;

/* The way of learning is the following:
 *
 * 1. Learn
 * for (each card) {
 *     word, reading and meaning are shown
 *     for (3 times) {
 *         type meaning
 *         type reading
 *         write word
 *     }
 * }
 *
 * 2. Repeat
 * for (each word twice) type meaning
 * for (each word twice) type reading
 * for (each word twice) write word
 */
public class CardsModeLearn extends CardsMode {
	private static final int LOOP_LEARNING = 3;
	private static final int LOOP_REPEATING = 2;

	private List<ScheduledCard> schedule;
	private int index;
	private boolean finished;

	public CardsModeLearn(List<Card> cards) {
		schedule = new ArrayList<>();

		for (Card c : Cards.shuffle(cards)) {
			schedule.add(new ScheduledCard(Scene.sceneLearningCardIntroduction, c));
			for (int i = 0; i < LOOP_LEARNING; i++) {
				schedule.add(new ScheduledCard(Scene.sceneLearningWordMeaning, c));
				schedule.add(new ScheduledCard(Scene.sceneLearningWordReading, c));
				schedule.add(new ScheduledCard(Scene.sceneLearningMeaningWriting, c));
			}
		}

		List<ScheduledCard> toShuffle = new ArrayList<>();
		for (int i = 0; i < LOOP_REPEATING; i++) {
			for (Card c : cards)
				toShuffle.add(new ScheduledCard(Scene.sceneLearningWordMeaning, c));
			Cards.addShuffled(toShuffle, schedule);
		}

		for (int i = 0; i < LOOP_REPEATING; i++) {
			for (Card c : cards)
				toShuffle.add(new ScheduledCard(Scene.sceneLearningWordReading, c));
			Cards.addShuffled(toShuffle, schedule);
		}

		for (int i = 0; i < LOOP_REPEATING; i++) {
			for (Card c : cards)
				toShuffle.add(new ScheduledCard(Scene.sceneLearningMeaningWriting, c));
			Cards.addShuffled(toShuffle, schedule);
		}
	}

	@Override
	public void next(LearningScene source, boolean correct) {
		if (!correct) {
			source.setHint(getCurrent());
			return;
		}

		index++;
		if (schedule.size() == index) {
			finish(source);
			return;
		}

		ScheduledCard next = schedule.get(index);
		if (source == next.scene)
			source.changeCard(next.card);
		else {
			source.changeScene(next.scene);
			next.scene.setCard(next.card);
		}
	}

	private void finish(LearningScene source) {
		finished = true;
		source.changeScene(Scene.sceneMain);
	}

	@Override
	public void start(Scene from) {
		Scene.sceneLearningCardIntroduction.setMode(this);
		Scene.sceneLearningWordMeaning.setMode(this);
		Scene.sceneLearningWordReading.setMode(this);
		Scene.sceneLearningMeaningWriting.setMode(this);

		ScheduledCard current = schedule.get(index);
		from.changeScene(current.scene);
		current.scene.setCard(current.card);
	}

	@Override
	public Card getCurrent() {
		return schedule.get(index).card;
	}

	private static class ScheduledCard {
		LearningScene scene;
		Card card;

		ScheduledCard(LearningScene s, Card c) {
			scene = s;
			card = c;
		}

		@Override
		public boolean equals(Object obj) {
			if (obj == this)
				return true;
			if (obj instanceof ScheduledCard) {
				ScheduledCard c = (ScheduledCard) obj;
				return c.card.equals(card) && c.scene.equals(scene);
			}
			return false;
		}
	}

	@Override
	public boolean isFinished() {
		return finished;
	}
}
