package cards;

import gui.Scene;
import gui.scenes.SceneLearningWordMeaning;
import gui.scenes.SceneLearningWordReading;

import java.util.List;

// tests knowledge of cards using different answer modes
public class CardsModeCheckKnowledge extends CardsMode {
	private LearningScene currentScene;
	private List<Card> cards;
	private int index;
	private boolean finished;

	public CardsModeCheckKnowledge(List<Card> cardsList) {
		this.cards = Cards.shuffle(cardsList);
		currentScene = Scene.sceneLearningWordMeaning;
	}

	/* The order of modes is:
	 * Word - Meaning
	 * Word - Reading
	 * Meaning - Writing
	 */
	@Override
	public void next(LearningScene source, boolean correct) {
		index++;
		if (index == cards.size()) { // when finished loop for a learning scene
			// set new scene or finish
			if (source instanceof SceneLearningWordMeaning) {
				currentScene = Scene.sceneLearningWordReading;
			} else if (source instanceof SceneLearningWordReading) {
				currentScene = Scene.sceneLearningMeaningWriting;
			} else { // the remaining case can only be SceneLearningMeaningWriting
				finish(source);
				return;
			}
			index = 0;
			cards = Cards.shuffle(cards);
			source.changeScene(currentScene);
			currentScene.setCard(cards.get(index));
		} else {
			currentScene.changeCard(cards.get(index));
		}
	}

	private void finish(LearningScene source) {
		finished = true;
		source.changeScene(Scene.sceneMain);
	}

	@Override
	public void start(Scene from) {
		Scene.sceneLearningWordMeaning.setMode(this);
		Scene.sceneLearningWordReading.setMode(this);
		Scene.sceneLearningMeaningWriting.setMode(this);

		from.changeScene(currentScene);
		currentScene.setCard(cards.get(index));
	}

	@Override
	public Card getCurrent() {
		return cards.get(index);
	}

	@Override
	public boolean isFinished() {
		return finished;
	}
}
