package cards;

import gui.Scene;

/* Manages cards working. Decides which cards and
 * learning scenes are shown and when. New instances
 * are created for running modes again (i.e. to
 * start a test again or to learn a new group).
 */
public abstract class CardsMode {
	public abstract void next(LearningScene source, boolean correct);

	// start or continue (switches to a learning scene)
	public abstract void start(Scene from);

	public abstract Card getCurrent();

	public abstract boolean isFinished();
}
