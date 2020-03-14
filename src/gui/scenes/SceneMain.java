package gui.scenes;

import cards.Cards;
import gui.Bounds;
import gui.Scene;
import gui.Window;
import gui.elements.Button;

public class SceneMain extends Scene {
	public SceneMain(Window holder) {
		super(holder);
		addElement(new Button("Select mode and words", () -> changeScene(sceneModeWordsSelection),
				this, new Bounds(0, -60, 270, 30, 0, 0)));
		addElement(new Button("Go!", () -> Cards.start(this), this, new Bounds(0, -20, 270, 30, 0, 0)));
		addElement(new Button("Progress", () -> {
		}, this, new Bounds(0, 20, 270, 30, 0, 0)));
		addElement(new Button("Settings", () -> {
		}, this, new Bounds(-70, 60, 130, 30, 0, 0)));
		addElement(new Button("About", () -> {
		}, this, new Bounds(70, 60, 130, 30, 0, 0)));
	}
}
