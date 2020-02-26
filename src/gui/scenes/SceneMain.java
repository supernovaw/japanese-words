package gui.scenes;

import gui.Bounds;
import gui.Scene;
import gui.Window;
import gui.elements.Button;

public class SceneMain extends Scene {
	public SceneMain(Window holder) {
		super(holder);
		addElement(new Button("Proceed to example scene", () -> changeScene(sceneExample),
				this, new Bounds(0, 0, 270, 30, 0, 0)));
	}
}
