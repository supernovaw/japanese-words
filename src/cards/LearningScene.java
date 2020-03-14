package cards;

import gui.Scene;
import gui.Window;

// unites common options of learning scenes
public abstract class LearningScene extends Scene {
	public LearningScene(Window holder) {
		super(holder);
	}

	public abstract void setMode(CardsMode m);

	// sets a card immediately with no animation
	public abstract void setCard(Card c);

	// changes the card with smooth animation
	public abstract void changeCard(Card c);

	// the hint should be removed after a new card is set
	public abstract void setHint(Card c);

	protected abstract void removeHint();

	@Override
	protected void onShut() {
		super.onShut();
		removeHint();
	}
}
