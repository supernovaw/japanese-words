package cards;

import gui.Scene;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/* The way of learning is the following:
 *
 * All cards are evenly split into groups (chains).
 * The first part is learning chains.
 * Each card in each chain is learned like this:
 *     1. Show card (reading, word, meaning)
 *     2. Ask meaning
 *     3. Ask reading
 *     4. Ask writing
 *     Steps 2-4 are repeated LOOP_LEARNING times.
 *
 * When a chain is learned, there is an intermediate test
 * before the next chain or before the final test.
 * Intermediate tests check knowledge of last REPEAT_LAST_CHAINS
 * chains. Cards failed in a test will be asked in the next test
 * even if they're no longer in the last REPEAT_LAST_CHAINS chains.
 *
 * After the sequence of chains and intermediate tests there is a
 * final test. Final test checks knowledge of all the cards and it
 * is repeated LOOP_REPEATING times. The order of each iteration is:
 *     1. Ask each card's meaning
 *     2. Ask each card's reading
 *     3. Ask each card's writing
 */
public class CardsModeLearn extends CardsMode {
	private static final int LOOP_LEARNING = 2;
	private static final int LOOP_REPEATING = 2;
	private static final int CHAIN_AVG_SIZE = 5;
	private static final int REPEAT_LAST_CHAINS = 3;

	private List<List<Card>> scheduledChains;
	private List<Card> allCards;
	private List<ScheduledCard> currentChain;
	private List<ScheduledCard> testing;
	private List<Card> testingFailed;
	private int indexOfChain, indexInChain, indexInTest;
	private State state = State.LEARNING;
	private boolean finished;

	public CardsModeLearn(List<Card> cards) {
		allCards = cards;
		scheduledChains = distributeEvenly(Cards.shuffleUnbiased(cards));

		currentChain = new ArrayList<>();
		learningSequence(scheduledChains.get(0), currentChain);

		testing = new ArrayList<>();
		testingFailed = new ArrayList<>();
	}

	@Override
	public void next(LearningScene source, boolean correct) {
		if (!correct) {
			if (state == State.INTERMEDIATE_TESTING) { // add to the next test
				Card c = getCurrent();
				if (!testingFailed.contains(c))
					testingFailed.add(c);
			}
			source.setHint(getCurrent());
			return;
		}

		// increments index, switches between modes or finishes
		switch (state) {
			case LEARNING:
				indexInChain++;
				if (indexInChain == currentChain.size())
					initIntermediateTesting();
				break;
			case INTERMEDIATE_TESTING:
				indexInTest++;
				if (indexInTest == testing.size()) { // transition case
					if (indexOfChain + 1 == scheduledChains.size()) { // transit to FINAL_TESTING
						initFinalTesting();
					} else { // transit to LEARNING
						state = State.LEARNING;
						indexOfChain++;
						indexInChain = 0;
						currentChain.clear();
						learningSequence(scheduledChains.get(indexOfChain), currentChain);
					}
				}
				break;
			case FINAL_TESTING:
				indexInTest++;
				if (indexInTest == testing.size()) {
					finish(source);
					return;
				}
				break;
		}

		ScheduledCard next = getCurrentScheduled();
		if (source == next.scene)
			source.changeCard(next.card);
		else {
			source.changeScene(next.scene);
			next.scene.setCard(next.card);
		}
	}

	private void initIntermediateTesting() {
		state = State.INTERMEDIATE_TESTING;
		indexInTest = 0;

		testing.clear();
		int toAddSize = testingFailed.size();
		int startFromChain = Math.max(indexOfChain - REPEAT_LAST_CHAINS + 1, 0);
		for (int i = startFromChain; i <= indexOfChain; i++)
			toAddSize += scheduledChains.get(i).size();

		List<Card> toAdd = new ArrayList<>(toAddSize);
		toAdd.addAll(testingFailed);
		for (int i = startFromChain; i <= indexOfChain; i++)
			toAdd.addAll(scheduledChains.get(i));

		for (Card c : Cards.shuffleUnbiased(toAdd))
			testing.add(new ScheduledCard(Scene.sceneLearningWordMeaning, c));
		for (Card c : Cards.shuffleUnbiased(toAdd))
			testing.add(new ScheduledCard(Scene.sceneLearningWordReading, c));
		for (Card c : Cards.shuffleUnbiased(toAdd))
			testing.add(new ScheduledCard(Scene.sceneLearningMeaningWriting, c));

		testingFailed.clear();
	}

	private void initFinalTesting() {
		state = State.FINAL_TESTING;
		indexInTest = 0;

		testing.clear();
		for (int i = 0; i < LOOP_REPEATING; i++) {
			for (Card c : Cards.shuffle(allCards))
				testing.add(new ScheduledCard(Scene.sceneLearningWordMeaning, c));
			for (Card c : Cards.shuffle(allCards))
				testing.add(new ScheduledCard(Scene.sceneLearningWordReading, c));
			for (Card c : Cards.shuffle(allCards))
				testing.add(new ScheduledCard(Scene.sceneLearningMeaningWriting, c));
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

		ScheduledCard current = getCurrentScheduled();
		from.changeScene(current.scene);
		current.scene.setCard(current.card);
	}

	private ScheduledCard getCurrentScheduled() {
		if (state == State.LEARNING)
			return currentChain.get(indexInChain);
		else
			return testing.get(indexInTest);
	}

	@Override
	public Card getCurrent() {
		return getCurrentScheduled().card;
	}

	private static void learningSequence(List<Card> src, List<ScheduledCard> trg) {
		for (Card c : src) {
			trg.add(new ScheduledCard(Scene.sceneLearningCardIntroduction, c));
			for (int i = 0; i < LOOP_LEARNING; i++) {
				trg.add(new ScheduledCard(Scene.sceneLearningWordMeaning, c));
				trg.add(new ScheduledCard(Scene.sceneLearningWordReading, c));
				trg.add(new ScheduledCard(Scene.sceneLearningMeaningWriting, c));
			}
		}
	}

	/* Splits a list into some number of parts depending on list
	 * size and 'sizes' argument which is the expected size of
	 * each part. In a list with 14 elements and with 'sizes' = 4,
	 * the following chains will be chosen: 0-3, 4-6, 7-10, 11-13.
	 */
	private static <T> List<List<T>> distributeEvenly(List<T> list) {
		if (list.isEmpty())
			throw new IllegalArgumentException("Argument list is empty");

		int parts = (int) Math.round((double) list.size() / CardsModeLearn.CHAIN_AVG_SIZE);
		if (parts == 0) { // return a single chain
			return new ArrayList<>(Collections.singleton(new ArrayList<>(list)));
		}

		List<List<T>> result = new ArrayList<>();
		int chainStartIndex = 0;
		for (int i = 0; i < parts; i++) {
			int chanEndIndex = Math.round(list.size() * (i + 1f) / parts) - 1;

			List<T> chain = new ArrayList<>(chanEndIndex - chainStartIndex + 1);
			for (int j = chainStartIndex; j <= chanEndIndex; j++) {
				chain.add(list.get(j));
			}

			chainStartIndex = chanEndIndex + 1;
			result.add(chain);
		}
		return result;
	}

	@Override
	public boolean isFinished() {
		return finished;
	}

	private enum State {
		LEARNING, INTERMEDIATE_TESTING, FINAL_TESTING
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
}
