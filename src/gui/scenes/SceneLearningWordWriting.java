package gui.scenes;

import gui.Bounds;
import gui.Scene;
import gui.Window;
import gui.elements.Button;
import gui.elements.WordWritingArea;

public class SceneLearningWordWriting extends Scene {
	public SceneLearningWordWriting(Window holder) {
		super(holder);

		addElement(new WordWritingArea(this, new Bounds(0, 0, 1000, 500, 0, 0)));
		addElement(new Button("Return", () -> changeScene(sceneMain), this, new Bounds(-65, -10, 125, 30, 1, 1)));
	}
}
