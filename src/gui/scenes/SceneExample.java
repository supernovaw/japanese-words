package gui.scenes;

import gui.Bounds;
import gui.Scene;
import gui.Window;
import gui.elements.*;

public class SceneExample extends Scene {
	public SceneExample(Window holder) {
		super(holder);
		addElement(new TextField("", "Type and edit text here", this, new Bounds(50, 70, 500, 40)));
		addElement(new HorizontalRadioButtons(new String[]{"Option 1", "Option 2", "3", "4", "5"},
				-1, this, new Bounds(50, 160, 500, 40)));
		addElement(new Checkbox("Check me", this, new Bounds(50, 250, 150, 30)));
		addElement(new Label("Read me", -1, this, new Bounds(50, 330, 200, 30)));

		addElement(new Button("Return", () -> changeScene(sceneMain), this, new Bounds(50, 410, 125, 30)));
	}
}
