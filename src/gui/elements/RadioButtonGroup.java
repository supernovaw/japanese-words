package gui.elements;

import java.util.ArrayList;

public class RadioButtonGroup {
	private ArrayList<RadioButton> buttons;
	private int selected = -1;

	public RadioButtonGroup() {
		buttons = new ArrayList<>();
	}

	public void add(RadioButton b) {
		buttons.add(b);
	}

	void push(RadioButton b) {
		for (int i = 0; i < buttons.size(); i++) {
			boolean eq = buttons.get(i) == b;
			buttons.get(i).setEnabled(eq);
			if (eq)
				selected = i;
		}
	}

	public int getSelectedIndex() {
		return selected;
	}
}
